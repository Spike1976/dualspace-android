package com.dualspace.mvp.admin

import android.app.Activity
import android.os.Bundle

class PolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This MVP does not impose any extra compliance policy.
        setResult(RESULT_OK)
        finish()
    }
}
