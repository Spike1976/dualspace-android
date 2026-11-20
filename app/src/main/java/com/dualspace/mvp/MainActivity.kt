package com.dualspace.mvp

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dualspace.mvp.apps.ProfileBridge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bridge = ProfileBridge(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Dashboard(bridge)
            }
        }
    }
}

@Composable
private fun Dashboard(bridge: ProfileBridge) {
    var message by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    val apps = remember(refresh) { bridge.appsByProfile() }
    val personalUser = android.os.Process.myUserHandle()
    val grouped = apps.groupBy { it.packageName }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        message = if (result.resultCode == Activity.RESULT_OK) {
            "Android accepted the provisioning request. Open the Work tab when setup finishes."
        } else {
            "Provisioning ended or was cancelled. Check whether a work profile was created."
        }
        refresh++
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("DUALSPACE", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black)
                Text("Two logins. One Android phone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    if (bridge.isWorkOwner) "WORK PROFILE ADMIN" else "PERSONAL DASHBOARD",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Profile setup", fontWeight = FontWeight.Bold)
                        Text(if (bridge.isWorkOwner) "This is the managed-profile controller. Install a second app copy here." else
                            "Create a managed Work profile with Android's own consent screen. This can only succeed on supported, eligible devices.")
                        if (!bridge.isWorkOwner) {
                            Button(
                                enabled = bridge.supportsWorkProfiles,
                                onClick = {
                                    try { launcher.launch(bridge.managedProfileProvisioningIntent()) }
                                    catch (e: Exception) { message = "Provisioning unavailable: ${e.localizedMessage}" }
                                }
                            ) { Text("Create Work Profile") }
                        } else {
                            OutlinedTextField(
                                value = packageName,
                                onValueChange = { packageName = it.trim() },
                                label = { Text("Package ID from personal app") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(onClick = {
                                message = bridge.installInWorkProfile(packageName)
                                refresh++
                            }) { Text("Enable Second Copy") }
                        }
                    }
                }
            }
            if (message.isNotBlank()) {
                item { Text(message, color = MaterialTheme.colorScheme.primary) }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search installed apps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { refresh++ }) { Text("Refresh apps") }
                Text("Each app card shows the package ID. Copy it into the DualSpace icon under your Work tab to enable a second copy.",
                    style = MaterialTheme.typography.bodySmall)
            }
            items(grouped.entries.filter { (_, entries) ->
                entries.any { it.name.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
            }.sortedBy { it.value.first().name.lowercase() }) { (pkg, versions) ->
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(versions.first().name, fontWeight = FontWeight.Bold)
                        Text(pkg, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        versions.forEach { app ->
                            val isSelfUser = app.user == personalUser
                            OutlinedButton(onClick = {
                                message = bridge.launch(app) ?: "Launched ${app.name}"
                            }) { Text(if (isSelfUser) "Open this profile's copy" else "Open other profile's copy") }
                        }
                    }
                }
            }
            item {
                Text("Safety: Never remove an existing Work profile to test this app. Removing a profile deletes its local app data. This MVP does not remove profiles or read app passwords.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
