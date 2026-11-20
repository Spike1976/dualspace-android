package com.dualspace.mvp.apps

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Process
import android.os.UserHandle
import com.dualspace.mvp.admin.DualSpaceAdminReceiver

/**
 * Android is the security boundary: each application copy belongs to a distinct user profile.
 * Never inspect third-party app storage, credentials, card numbers, or session tokens.
 */
class ProfileBridge(private val context: Context) {
    private val policy = context.getSystemService(DevicePolicyManager::class.java)
    private val launcher = context.getSystemService(LauncherApps::class.java)

    companion object {
        const val WEX_PACKAGE = "com.efsllc.efsmobileapp"
        const val WEX_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.efsllc.efsmobileapp"
    }

    data class AvailableApp(
        val packageName: String,
        val name: String,
        val user: UserHandle
    )

    val isWorkOwner: Boolean get() = policy.isProfileOwnerApp(context.packageName)
    val supportsWorkProfiles: Boolean
        get() = context.packageManager.hasSystemFeature("android.software.managed_users")

    /** Only the personal profile is permitted to begin provisioning. */
    fun managedProfileProvisioningIntent(): Intent =
        Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                ComponentName(context, DualSpaceAdminReceiver::class.java)
            )
        }

    fun canProvision(): Boolean =
        !isWorkOwner && supportsWorkProfiles &&
            managedProfileProvisioningIntent().resolveActivity(context.packageManager) != null

    fun visibleProfiles(): List<UserHandle> =
        try { launcher.profiles } catch (_: RuntimeException) { listOf(Process.myUserHandle()) }

    fun appsByProfile(): List<AvailableApp> =
        try {
            visibleProfiles().flatMap { user ->
                try {
                    launcher.getActivityList(null, user).map {
                        AvailableApp(it.componentName.packageName, it.label.toString(), user)
                    }
                } catch (_: RuntimeException) {
                    emptyList()
                }
            }.distinctBy { it.packageName to it.user }
                .filterNot { it.packageName == context.packageName }
                .sortedWith(compareBy({ it.name.lowercase() }, { it.packageName }))
        } catch (_: RuntimeException) { emptyList() }

    fun findApp(packageName: String, user: UserHandle): AvailableApp? =
        appsByProfile().firstOrNull { it.packageName == packageName && it.user == user }

    fun launch(app: AvailableApp): String? =
        try {
            val activity = launcher.getActivityList(app.packageName, app.user).firstOrNull()
                ?: return "App has no launch activity in that profile."
            launcher.startMainActivity(activity.componentName, app.user, null, null)
            null
        } catch (e: SecurityException) {
            "Android blocked access to this profile. Open the app from its Work tab."
        } catch (e: RuntimeException) {
            "Could not launch this copy: ${e.localizedMessage ?: "Android launch error"}"
        }

    /**
     * Calls the Play Store inside the context that invokes this function.
     * This MUST be called from the Work-badged DualSpace copy for a Work install.
     * Presence of a Work Play Store is not guaranteed on independently provisioned profiles.
     */
    fun openWorkStore(packageName: String = WEX_PACKAGE): String? {
        if (!isWorkOwner) return "Open DualSpace from the Work tab first."
        if (!PackageNameValidator.isValid(packageName)) return "Invalid Android package name."
        val intents = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .setPackage("com.android.vending"),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return null
            } catch (_: RuntimeException) {
                // Try next supported store route.
            }
        }
        return "No app store is available inside this Work profile. The managed profile may not be configured for app distribution."
    }

    /**
     * Optional path only for affiliated work profiles. Most consumer-created
     * work profiles are NOT affiliated. Never use this as the normal install flow.
     */
    fun installIfAffiliated(packageName: String): String {
        if (!isWorkOwner) return "Must run in the managed profile."
        if (!isValidPackageName(packageName)) return "Invalid package name."
        if (!policy.isAffiliatedUser) {
            return "This consumer Work profile is not affiliated. Use the Work Play Store if available."
        }
        return try {
            val admin = ComponentName(context, DualSpaceAdminReceiver::class.java)
            if (policy.installExistingPackage(admin, packageName)) {
                "Android enabled the package in Work. Refresh the app list."
            } else {
                "Android could not enable that package."
            }
        } catch (_: SecurityException) {
            "Android denied package installation for this profile."
        } catch (e: RuntimeException) {
            "Package installation failed: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

}
