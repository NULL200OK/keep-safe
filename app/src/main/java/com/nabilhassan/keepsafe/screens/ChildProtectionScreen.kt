package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.family.*
import com.nabilhassan.keepsafe.security.*
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProtectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vpn = rememberVpnController()
    val guard = vpn.guard
    val running by TrafficScannerVpnService.running.collectAsStateWithLifecycle()
    val logVersion by BlockLog.version.collectAsStateWithLifecycle()

    var level by remember { mutableStateOf(FamilySettings.level) }
    var safeSearch by remember { mutableStateOf(FamilySettings.safeSearch) }
    var schedOn by remember { mutableStateOf(FamilySettings.scheduleEnabled) }
    var schedStart by remember { mutableIntStateOf(FamilySettings.scheduleStart) }
    var schedEnd by remember { mutableIntStateOf(FamilySettings.scheduleEnd) }
    var autoStart by remember { mutableStateOf(FamilySettings.autoStart) }
    var alerts by remember { mutableStateOf(FamilySettings.alertOnBlock) }
    var weekly by remember { mutableStateOf(FamilySettings.weeklyReport) }
    var blockList by remember { mutableStateOf(FamilySettings.customBlock) }
    var allowList by remember { mutableStateOf(FamilySettings.customAllow) }
    var hasPin by remember { mutableStateOf(PinLock.isSet(context)) }
    var showPinSetup by remember { mutableStateOf(false) }
    val warnings = remember(running) { SecurityChecker.bypassWarnings(context) }
    val recent = remember(logVersion) { BlockLog.recent(15) }
    val week = remember(logVersion) { BlockLog.counts(7) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("حماية الأسرة | Family Protection", fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (running) "الحماية مفعّلة" else "الحماية متوقفة",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("الحجب يعمل فقط عندما تكون الحماية مفعّلة", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = running, onCheckedChange = { vpn.set(it) })
            }
        }

        warnings.forEach {
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f))) {
                Text("⚠️ $it", Modifier.padding(14.dp), fontSize = 13.sp)
            }
        }

        Header("مستوى الحماية")
        ProtectionLevel.entries.forEach { l ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = level == l, onClick = {
                    guard.run { level = l; FamilySettings.level = l }
                })
                Column {
                    Text(l.titleAr, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(l.descAr, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        SwitchRow("البحث الآمن إجباريًا", "Google وBing وYouTube", safeSearch) { on ->
            guard.run { safeSearch = on; FamilySettings.safeSearch = on }
        }

        Header("جدول الحظر (وقت النوم / الدراسة)")
        SwitchRow("حظر الإنترنت في أوقات محددة", "يُحجب كل شيء في هذه الفترة", schedOn) { on ->
            guard.run { schedOn = on; FamilySettings.scheduleEnabled = on }
        }
        if (schedOn) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HourStepper("من", schedStart, Modifier.weight(1f)) { h ->
                    guard.run { schedStart = h; FamilySettings.scheduleStart = h }
                }
                HourStepper("إلى", schedEnd, Modifier.weight(1f)) { h ->
                    guard.run { schedEnd = h; FamilySettings.scheduleEnd = h }
                }
            }
        }

        Header("قائمتك المخصصة")
        DomainList("مواقع محجوبة دائمًا", blockList) { new ->
            guard.run { blockList = new; FamilySettings.customBlock = new }
        }
        Spacer(Modifier.height(8.dp))
        DomainList("مواقع مسموحة دائمًا", allowList) { new ->
            guard.run { allowList = new; FamilySettings.customAllow = new }
        }

        Header("التشغيل والتنبيهات")
        SwitchRow("تشغيل الحماية تلقائيًا عند تشغيل الهاتف", "", autoStart) { on ->
            guard.run { autoStart = on; FamilySettings.autoStart = on }
        }
        SwitchRow("إشعار عند حجب موقع", "", alerts) { on ->
            alerts = on; FamilySettings.alertOnBlock = on
        }
        SwitchRow("تقرير أسبوعي", "ملخص بما تم حجبه", weekly) { on ->
            weekly = on; FamilySettings.weeklyReport = on
        }

        Header("الرقم السري")
        Text("يمنع إيقاف الحماية أو تغيير الإعدادات دون إذنك.", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { guard.run { showPinSetup = true } }) {
                Text(if (hasPin) "تغيير الرقم" else "تعيين رقم سري")
            }
            if (hasPin) OutlinedButton(onClick = {
                guard.run { PinLock.clear(context); hasPin = false }
            }) { Text("إزالة") }
        }

        Header("ما تم حجبه هذا الأسبوع")
        if (week.isEmpty()) {
            Text("لا شيء بعد.", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            week.forEach { (r, n) -> Text("• ${r.labelAr}: $n", fontSize = 13.sp) }
        }
        Spacer(Modifier.height(8.dp))
        val fmt = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
        recent.forEach {
            Text("${fmt.format(Date(it.time))}  ${it.domain}  (${it.reason.labelAr})",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (recent.isNotEmpty()) {
            TextButton(onClick = { guard.run { BlockLog.clear() } }) { Text("مسح السجل") }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showPinSetup) {
        PinSetupDialog(
            onDone = { PinLock.set(context, it); hasPin = true; showPinSetup = false },
            onDismiss = { showPinSetup = false })
    }
}

@Composable
private fun Header(t: String) {
    Spacer(Modifier.height(18.dp))
    Text(t, fontSize = 15.sp, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            if (sub.isNotBlank()) Text(sub, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun HourStepper(label: String, hour: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp)
        TextButton(onClick = { onChange((hour + 23) % 24) }) { Text("−") }
        Text("%02d:00".format(hour), fontWeight = FontWeight.Bold)
        TextButton(onClick = { onChange((hour + 1) % 24) }) { Text("+") }
    }
}

@Composable
private fun DomainList(title: String, items: Set<String>, onChange: (Set<String>) -> Unit) {
    var text by remember { mutableStateOf("") }
    var bad by remember { mutableStateOf(false) }
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text, onValueChange = { text = it; bad = false },
            placeholder = { Text("example.com") }, singleLine = true, isError = bad,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            val d = FamilySettings.normalizeDomain(text)
            if (d == null) bad = true else { onChange(items + d); text = "" }
        }) { Text("إضافة") }
    }
    items.sorted().forEach { d ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(d, Modifier.weight(1f), fontSize = 13.sp)
            IconButton(onClick = { onChange(items - d) }) {
                Icon(Icons.Filled.Close, contentDescription = "Remove")
            }
        }
    }
}
