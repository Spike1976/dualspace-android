package com.dualspace.mvp.apps

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import com.dualspace.mvp.admin.DualSpaceAdminReceiver

class ProfileBridge(private val context: Context) {
    private val policy = context.getSystemService(DevicePolicyManager::class.java)
    private val launcher = context.getSystemService(LauncherApps::class.java)
    private val users = context.getSystemService(UserManager::class.java)

    val isWorkOwner: Boolean get() = policy.isProfileOwnerApp(context.packageName)
    val supportsWorkProfiles: Boolean get() = context.packageManager.hasSystemFeature("android.software.managed_users")

    fun managedProfileProvisioningIntent(): Intent =
        Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                ComponentName(context, DualSpaceAdminReceiver::class.java)
            )
        }

    data class AvailableApp(val packageName: String, val name: String, val user: UserHandle)

    fun appsByProfile(): List<AvailableApp> = try {
        launcher.profiles.flatMap { user ->
            launcher.getActivityList(null, user).map {
                AvailableApp(it.componentName.packageName, it.label.toString(), user)
            }
        }.distinctBy { it.packageName to it.user }
            .filter { it.packageName != context.packageName }
            .sortedBy { it.name.lowercase() }
    } catch (_: SecurityException) {
        emptyList()
    }

    fun launch(app: AvailableApp): String? {
        return try {
            val activity = launcher.getActivityList(app.packageName, app.user).firstOrNull()
                ?: return "No launchable activity found"
            launcher.startMainActivity(activity.componentName, app.user, null, null)
            null
        } catch (e: Exception) {
            "Could not launch: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }

    fun installInWorkProfile(packageName: String): String {
        if (!isWorkOwner) return "Open DualSpace from the Work tab to install a second copy."
        if (!Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+").matches(packageName)) {
            return "Enter a valid package name such as com.example.app"
        }
        return try {
            val receiver = ComponentName(context, DualSpaceAdminReceiver::class.java)
            if (policy.installExistingPackage(receiver, packageName)) {
                "Installed in Work profile: $packageName"
            } else {
                "Android could not enable this package in the Work profile."
            }
        } catch (e: Exception) {
            "Installation failed: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }
}
