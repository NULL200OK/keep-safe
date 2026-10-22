package com.nabilhassan.keepsafe.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nabilhassan.keepsafe.R
import com.nabilhassan.keepsafe.family.BlockLog
import com.nabilhassan.keepsafe.family.BlockReason
import com.nabilhassan.keepsafe.family.DnsFilter
import com.nabilhassan.keepsafe.family.FamilySettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * DNS-filtering VPN. Only DNS traffic is routed through the tunnel. Each query is
 * checked against the family-protection rules and then either answered locally
 * (blocked / safe-search redirect) or forwarded to a filtering resolver. All other
 * traffic bypasses the VPN, so the device keeps working normally while it is on.
 */
class TrafficScannerVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private var pool: ExecutorService? = null
    @Volatile private var lastAlert = 0L

    companion object {
        const val ACTION_START = "START_VPN"
        const val ACTION_STOP = "STOP_VPN"

        private const val TAG = "VPN"
        private const val VIRTUAL_DNS = "10.0.0.1"

        private val _running = MutableStateFlow(false)
        val running: StateFlow<Boolean> = _running.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        FamilySettings.init(this)
        BlockLog.init(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
        }
        return START_NOT_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        try {
            val pfd = Builder()
                .setSession("Keep Safe")
                .addAddress("10.0.0.2", 32)
                .addDnsServer(VIRTUAL_DNS)
                .addRoute(VIRTUAL_DNS, 32)
                .setMtu(1500)
                .establish()
            if (pfd == null) {
                _running.value = false
                return
            }
            vpnInterface = pfd
            pool = Executors.newFixedThreadPool(8)
            _running.value = true
            worker = Thread({ runTunnel(pfd) }, "keepsafe-vpn").also { it.start() }
            Log.i(TAG, "Started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            stopVpn()
        }
    }

    private fun runTunnel(pfd: ParcelFileDescriptor) {
        val input = FileInputStream(pfd.fileDescriptor)
        val output = FileOutputStream(pfd.fileDescriptor)
        val packet = ByteArray(32767)
        try {
            while (!Thread.currentThread().isInterrupted) {
                val len = input.read(packet)
                if (len < 0) break
                if (len == 0) { Thread.sleep(20); continue }
                val request = packet.copyOf(len)
                // Each query runs on the pool so a slow lookup never stalls the tunnel.
                pool?.execute {
                    val response = try { handleDnsPacket(request) } catch (_: Exception) { null }
                        ?: return@execute
                    try {
                        synchronized(output) { output.write(response) }
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) {
        } finally {
            _running.value = false
        }
    }

    /** Parses an IPv4/UDP DNS query, applies the filter, and builds the reply packet. */
    private fun handleDnsPacket(p: ByteArray): ByteArray? {
        if (p.size < 28 || (p[0].toInt() shr 4) != 4) return null
        val ihl = (p[0].toInt() and 0x0F) * 4
        if (p[9].toInt() != 17 || p.size < ihl + 8) return null
        val dstPort = ((p[ihl + 2].toInt() and 0xFF) shl 8) or (p[ihl + 3].toInt() and 0xFF)
        if (dstPort != 53) return null

        val dns = p.copyOfRange(ihl + 8, p.size)
        val q = DnsFilter.parseQuestion(dns)

        val reply: ByteArray
        if (q != null) {
            when (val d = DnsFilter.decide(q.name)) {
                is DnsFilter.Decision.Block -> {
                    onBlocked(q.name, d.reason)
                    reply = DnsFilter.buildReply(dns, q, null)
                }
                is DnsFilter.Decision.Redirect -> reply = DnsFilter.buildReply(dns, q, d.ip)
                DnsFilter.Decision.Forward -> {
                    reply = forward(dns) ?: return null
                    if (DnsFilter.isProviderBlock(reply)) onBlocked(q.name, BlockReason.PROVIDER)
                }
            }
        } else {
            reply = forward(dns) ?: return null
        }
        return wrap(p, ihl, reply)
    }

    private fun forward(dns: ByteArray): ByteArray? = try {
        val upstream = InetAddress.getByName(FamilySettings.level.upstream)
        DatagramSocket().use { socket ->
            protect(socket) // keep our own lookup outside the tunnel
            socket.soTimeout = 4000
            socket.send(DatagramPacket(dns, dns.size, upstream, 53))
            val buf = ByteArray(4096)
            val resp = DatagramPacket(buf, buf.size)
            socket.receive(resp)
            buf.copyOf(resp.length)
        }
    } catch (_: Exception) { null }

    private fun onBlocked(domain: String, reason: BlockReason) {
        BlockLog.record(domain, reason)
        val now = System.currentTimeMillis()
        if (!FamilySettings.alertOnBlock || reason == BlockReason.SCHEDULE) return
        if (now - lastAlert < 30_000) return
        lastAlert = now
        try {
            val b = NotificationCompat.Builder(this, "fraud_quiet")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("🛡️ تم حجب موقع | Site blocked")
                .setContentText("$domain — ${reason.labelAr}")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(7001, b.build())
        } catch (_: Exception) { }
    }

    /** Wraps a DNS reply in an IPv4/UDP packet addressed back to the querier. */
    private fun wrap(p: ByteArray, ihl: Int, reply: ByteArray): ByteArray {
        val total = ihl + 8 + reply.size
        val out = ByteArray(total)
        System.arraycopy(p, 0, out, 0, ihl + 8)
        System.arraycopy(reply, 0, out, ihl + 8, reply.size)
        System.arraycopy(p, 16, out, 12, 4)           // swap IP addresses
        System.arraycopy(p, 12, out, 16, 4)
        System.arraycopy(p, ihl + 2, out, ihl, 2)     // swap UDP ports
        System.arraycopy(p, ihl, out, ihl + 2, 2)
        out[2] = (total shr 8).toByte(); out[3] = total.toByte()
        val udpLen = 8 + reply.size
        out[ihl + 4] = (udpLen shr 8).toByte(); out[ihl + 5] = udpLen.toByte()
        out[ihl + 6] = 0; out[ihl + 7] = 0            // UDP checksum optional over IPv4
        out[10] = 0; out[11] = 0
        var sum = 0
        var i = 0
        while (i < ihl) {
            sum += ((out[i].toInt() and 0xFF) shl 8) or (out[i + 1].toInt() and 0xFF)
            i += 2
        }
        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        val csum = sum.inv() and 0xFFFF
        out[10] = (csum shr 8).toByte(); out[11] = csum.toByte()
        return out
    }

    private fun stopVpn() {
        _running.value = false
        worker?.interrupt()
        worker = null
        pool?.shutdownNow()
        pool = null
        try { vpnInterface?.close() } catch (_: Exception) { }
        vpnInterface = null
        stopSelf()
    }

    override fun onRevoke() {
        // The system (or another VPN app) took the tunnel away: tell the user.
        try {
            val b = NotificationCompat.Builder(this, "fraud_channel")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("⚠️ توقفت الحماية | Protection stopped")
                .setContentText("تم إيقاف حماية أمان. افتح التطبيق لإعادة تفعيلها.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(7002, b.build())
        } catch (_: Exception) { }
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
