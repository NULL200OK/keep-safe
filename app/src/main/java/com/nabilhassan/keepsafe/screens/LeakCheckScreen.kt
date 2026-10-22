package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.security.LeakChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeakCheckScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Int?>(null) }
    var failed by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فحص تسرّب كلمة المرور", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("تُقارن كلمتك مع قاعدة التسريبات العامة (Have I Been Pwned) بطريقة خاصة: " +
            "لا تغادر كلمتك الهاتف، يُرسل فقط جزء صغير من بصمتها المشفّرة.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it; result = null; failed = false },
            label = { Text("كلمة المرور | Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                loading = true; failed = false; result = null
                scope.launch {
                    val n = LeakChecker.timesLeaked(password)
                    loading = false
                    if (n == null) failed = true else result = n
                }
            },
            enabled = password.isNotEmpty() && !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text(if (loading) "جارٍ الفحص..." else "فحص | Check", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(16.dp))
        result?.let { n ->
            val bad = n > 0
            val c = if (bad) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = c.copy(alpha = 0.15f))) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (bad) "⚠️ كلمة المرور مسرّبة" else "✅ لم نجدها في التسريبات",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c)
                    Spacer(Modifier.height(6.dp))
                    Text(if (bad) "ظهرت $n مرة في تسريبات معروفة. غيّرها فورًا في كل موقع " +
                        "تستخدمها فيه، واستخدم كلمة فريدة لكل موقع."
                    else "هذا لا يعني أنها قوية. استخدم كلمة طويلة وفريدة لكل حساب.",
                        fontSize = 13.sp)
                }
            }
        }
        if (failed) {
            Text("تعذّر الاتصال. تحقق من الإنترنت وحاول مجددًا.",
                color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
    }
}
