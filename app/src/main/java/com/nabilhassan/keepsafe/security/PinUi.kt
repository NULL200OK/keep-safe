package com.nabilhassan.keepsafe.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity

/**
 * Runs sensitive actions only after the PIN is entered (when a PIN is set).
 * Call [run] from a click handler and show [PinGuardDialog] once in the screen.
 */
class PinGuard(private val isSet: () -> Boolean) {
    var pending by mutableStateOf<(() -> Unit)?>(null)

    fun run(action: () -> Unit) {
        if (isSet()) pending = action else action()
    }
}

@Composable
fun rememberPinGuard(): PinGuard {
    val context = LocalContext.current
    return remember { PinGuard { PinLock.isSet(context) } }
}

@Composable
fun PinGuardDialog(guard: PinGuard) {
    val context = LocalContext.current
    val action = guard.pending ?: return
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { guard.pending = null },
        title = { Text("أدخل الرقم السري | Enter PIN") },
        text = { PinField(pin, error) { pin = it; error = false } },
        confirmButton = {
            TextButton(onClick = {
                if (PinLock.verify(context, pin)) { guard.pending = null; action() }
                else error = true
            }) { Text("تأكيد | OK") }
        },
        dismissButton = {
            TextButton(onClick = { guard.pending = null }) { Text("إلغاء | Cancel") }
        }
    )
}

/** Dialog to create a new PIN (4–8 digits, entered twice). */
@Composable
fun PinSetupDialog(onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رقم سري جديد | New PIN") },
        text = {
            Column {
                PinField(pin, false) { pin = it; error = null }
                Spacer(Modifier.height(8.dp))
                PinField(again, false, label = "أعد الإدخال | Repeat") { again = it; error = null }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length < 4 -> error = "٤ أرقام على الأقل | At least 4 digits"
                    pin != again -> error = "الرقمان غير متطابقين | PINs differ"
                    else -> onDone(pin)
                }
            }) { Text("حفظ | Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء | Cancel") } }
    )
}

@Composable
private fun PinField(
    value: String, error: Boolean,
    label: String = "PIN", onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> if (v.length <= 8 && v.all { it.isDigit() }) onChange(v) },
        label = { Text(label) },
        singleLine = true,
        isError = error,
        supportingText = if (error) {
            { Text("الرقم خاطئ | Wrong PIN") }
        } else null,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}

/** Full-screen lock shown at launch when a PIN is set. */
@Composable
fun PinLockScreen(activity: FragmentActivity, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val useBio = BiometricAuth.isEnabled(activity)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("التطبيق مقفل", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("App is locked", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        PinField(pin, error) { pin = it; error = false }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (PinLock.verify(activity, pin)) onUnlocked() else error = true
        }) { Text("فتح | Unlock") }
        if (useBio) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                BiometricAuth.authenticate(activity, onSuccess = onUnlocked, onFailure = { })
            }) {
                Icon(Icons.Filled.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("بصمة | Fingerprint")
            }
        }
    }
}
