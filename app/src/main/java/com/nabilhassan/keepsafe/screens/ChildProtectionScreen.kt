package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.child.*
import com.nabilhassan.keepsafe.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProtectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var level by remember { mutableStateOf(RestrictionLevel.MEDIUM) }
    var cats by remember {
        mutableStateOf(setOf(ContentCategory.ADULT, ContentCategory.GAMBLING))
    }
    var saved by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("حماية الأطفال | Child Protection", fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SafeGreen.copy(alpha = 0.1f))) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ChildCare, contentDescription = null,
                    tint = SafeGreen, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Text("وضع حماية الأطفال", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = SafeGreen)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("مستوى التقييد | Level", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        RestrictionLevel.entries.forEach { l ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = level == l, onClick = { level = l })
                Text(l.name, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("الفئات المحجوبة | Categories", fontSize = 16.sp,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        ContentCategory.entries.forEach { c ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = c in cats, onCheckedChange = { on ->
                    cats = if (on) cats + c else cats - c
                })
                Text(c.name, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                ChildProtectionManager.saveProfile(context, ChildProfile(
                    name = "Child", age = 10,
                    restrictionLevel = level,
                    blockedCategories = cats))
                saved = true
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text("حفظ | Save", fontWeight = FontWeight.Bold) }
        if (saved) {
            Spacer(Modifier.height(12.dp))
            Text("✅ تم الحفظ | Saved", color = SafeGreen,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}