package com.dualspace.mvp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.os.Process
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dualspace.mvp.apps.ProfileBridge

class MainActivity : ComponentActivity() {
    private val lifecycleRefresh = mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        lifecycleRefresh.intValue++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Protect only DualSpace UI from ordinary screenshots and screen casting.
        // This does NOT control screenshots inside WEX itself.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        val bridge = ProfileBridge(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Dashboard(bridge, lifecycleRefresh.intValue)
            }
        }
    }
}

@Composable
private fun Dashboard(bridge: ProfileBridge, externalRefresh: Int) {
    var message by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var showConsent by remember { mutableStateOf(false) }
    val myUser = Process.myUserHandle()
    val apps = remember(refresh, externalRefresh) { bridge.wexCopies() }
    val profiles = remember(refresh, externalRefresh) { bridge.visibleProfiles() }
    val wexSelf = apps.firstOrNull {
        it.packageName == ProfileBridge.WEX_PACKAGE && it.user == myUser
    }
    val wexOther = apps.firstOrNull {
        it.packageName == ProfileBridge.WEX_PACKAGE && it.user != myUser
    }

    val provision = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        message = if (result.resultCode == Activity.RESULT_OK) {
            "Android completed the setup dialog. Refresh to verify Work profile access."
        } else {
            "Profile setup ended. Refresh and check whether Android created a Work tab."
        }
        refresh++
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Create a separate Work profile?") },
            text = {
                Text(
                    "Android will create a separately managed profile. DualSpace will act as its profile administrator. " +
                    "Its apps and local data are isolated from Personal. Removing this profile later permanently deletes its local app data. " +
                    "Do not continue if your phone already has a company-managed Work profile."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showConsent = false
                    try {
                        provision.launch(bridge.managedProfileProvisioningIntent())
                    } catch (_: ActivityNotFoundException) {
                        message = "Android has no supported provisioning activity."
                    } catch (e: RuntimeException) {
                        message = "Cannot start provisioning: ${e.localizedMessage ?: "Android refused setup"}"
                    }
                }) { Text("Continue to Android") }
            },
            dismissButton = {
                TextButton(onClick = { showConsent = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("DUALSPACE", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("Two secure sessions. One phone.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (bridge.isWorkOwner) "WORK PROFILE CONTROLLER"
                    else "PERSONAL ACCOUNT DASHBOARD",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("WEX CardControl", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("Love's and Pilot", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Love's is your Personal sign-in; Pilot is your Work sign-in. " +
                            "These labels do not select, activate, or change either fuel card."
                        )
                        if (bridge.isWorkOwner) {
                            CopyStatus("Pilot (Work)", wexSelf != null)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    message = wexSelf?.let { bridge.launch(it) }
                                        ?: bridge.openWorkStore()
                                }
                            ) {
                                Icon(Icons.Outlined.BusinessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (wexSelf != null) "Open Pilot WEX" else "Find WEX in Work Store")
                            }
                            if (wexSelf == null) {
                                OutlinedButton(
                                    onClick = { message = bridge.installIfAffiliated(ProfileBridge.WEX_PACKAGE) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Try system install (affiliated devices only)") }
                            }
                        } else {
                            CopyStatus("Love's (Personal)", wexSelf != null)
                            Button(
                                onClick = {
                                    message = if (wexSelf != null) bridge.launch(wexSelf)
                                        else bridge.openStoreInCurrentProfile()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Open Love's WEX")
                            }
                            CopyStatus("Pilot (Work)", wexOther != null)
                            OutlinedButton(
                                onClick = { message = wexOther?.let { bridge.launch(it) } },
                                enabled = wexOther != null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.BusinessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Open Pilot WEX")
                            }
                            if (wexOther == null) Text(
                                "To set up Pilot, open the Work-badged DualSpace icon and install WEX through its Work store.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "DualSpace never collects WEX card numbers, PINs or passwords. " +
                            "WEX compatibility and fueling functions must be tested on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Profile status", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(if (bridge.isWorkOwner) "Work profile owner active" else
                            if (profiles.size > 1) "Android reports more than one accessible profile"
                            else "No second accessible profile detected")
                        if (!bridge.isWorkOwner && profiles.size <= 1) {
                            Button(
                                onClick = { showConsent = true },
                                enabled = bridge.canProvision(),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Set up Second Space") }
                            if (!bridge.canProvision()) Text(
                                "Work-profile provisioning is unavailable or restricted on this device.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        OutlinedButton(onClick = { refresh++ }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Refresh installed apps")
                        }
                    }
                }
            }
            if (message != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor =
                        MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(message.orEmpty())
                            TextButton(onClick = { message = null }) { Text("Dismiss") }
                        }
                    }
                }
            }
            item {
                HorizontalDivider()
                Text(
                    "Safety: Do not remove an existing company Work profile. Android deletes the " +
                    "profile's local data when it is removed. This app does not manage or transfer WEX credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CopyStatus(title: String, installed: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (installed) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
            contentDescription = null,
            tint = if (installed) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "$title: ${if (installed) "Installed" else "Not detected"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
