package com.nabilhassan.keepsafe.screens

import android.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.family.FamilySettings
import com.nabilhassan.keepsafe.security.BiometricAuth
import com.nabilhassan.keepsafe.settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentTheme by remember { mutableStateOf(SettingsManager.getTheme(context)) }
    var currentLang by remember { mutableStateOf(SettingsManager.getLanguage(context)) }
    var notifications by remember { mutableStateOf(SettingsManager.isNotificationsEnabled(context)) }
    var autoUpdate by remember { mutableStateOf(SettingsManager.isAutoUpdateEnabled(context)) }
    var beginner by remember { mutableStateOf(FamilySettings.beginnerMode) }
    var biometric by remember { mutableStateOf(BiometricAuth.isEnabled(context)) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("الإعدادات | Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))

        SettingsHeader("المظهر | Appearance")
        SettingsItem(Icons.Filled.DarkMode, "الوضع الليلي", "Dark Mode",
            currentTheme.displayAr) { showThemeDialog = true }

        SettingsHeader("اللغة | Language")
        SettingsItem(Icons.Filled.Language, "لغة التطبيق", "Language",
            currentLang.displayAr) { showLangDialog = true }

        SettingsHeader("الإشعارات | Notifications")
        SettingsSwitch(Icons.Filled.Notifications, "تفعيل الإشعارات", "Notifications",
            notifications) {
            notifications = it
            SettingsManager.setNotificationsEnabled(context, it)
        }
        SettingsSwitch(Icons.Filled.Update, "تحديث تلقائي", "Auto Update", autoUpdate) {
            autoUpdate = it
            SettingsManager.setAutoUpdateEnabled(context, it)
        }

        SettingsHeader("الأمان | Security")
        SettingsSwitch(Icons.Filled.Fingerprint, "الحماية بالبصمة", "Biometric", biometric) { enabled ->
            if (enabled && !BiometricAuth.isBiometricAvailable(context)) {
                android.widget.Toast.makeText(context,
                    "البصمة غير متاحة | Biometric unavailable",
                    android.widget.Toast.LENGTH_LONG).show()
            } else {
                biometric = enabled
                BiometricAuth.setEnabled(context, enabled)
            }
        }

        SettingsHeader("عام | General")
        SettingsSwitch(Icons.Filled.Spa, "الواجهة المبسّطة", "Simple mode", beginner) {
            beginner = it
            FamilySettings.beginnerMode = it
        }
        SettingsItem(Icons.Filled.Info, "عن التطبيق", "About", "") { onOpenAbout() }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("المظهر | Theme") },
            text = {
                Column {
                    AppTheme.entries.forEach { t ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currentTheme == t, onClick = {
                                currentTheme = t
                                SettingsManager.setTheme(context, t)
                                showThemeDialog = false
                                onThemeChanged()
                            })
                            Text("${t.displayAr} | ${t.displayEn}", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("إغلاق") }
            }
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("اللغة | Language") },
            text = {
                Column {
                    AppLanguage.entries.forEach { l ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currentLang == l, onClick = {
                                currentLang = l
                                SettingsManager.setLanguage(context, l)
                                showLangDialog = false
                                (context as? android.app.Activity)?.let { act ->
                                    AlertDialog.Builder(act)
                                        .setTitle("إعادة التشغيل | Restart")
                                        .setMessage("لتطبيق اللغة الجديدة، سيُعاد تشغيل التطبيق.\nThe app will restart.")
                                        .setPositiveButton("موافق | OK") { _, _ ->
                                            LocaleHelper.restartApp(act)
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            })
                            Text("${l.displayAr} | ${l.displayEn}", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Spacer(Modifier.height(16.dp))
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    titleAr: String, titleEn: String, value: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titleAr, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(titleEn, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (value.isNotBlank())
                Text(value, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    titleAr: String, titleEn: String,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titleAr, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(titleEn, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}