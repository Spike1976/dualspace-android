package com.dualspace.mvp.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class DualSpaceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        val policy = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, DualSpaceAdminReceiver::class.java)
        if (policy.isProfileOwnerApp(context.packageName)) {
            // Android completes provisioning with a disabled managed profile.
            // Enable it so the user can see and use their Work tab.
            try {
                policy.setProfileEnabled(admin)
                Log.i("DualSpace", "Managed profile enabled")
            } catch (e: SecurityException) {
                Log.e("DualSpace", "Unable to enable managed profile", e)
            }
        }
    }
}
