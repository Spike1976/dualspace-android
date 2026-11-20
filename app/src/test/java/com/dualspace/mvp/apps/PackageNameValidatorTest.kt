package com.dualspace.mvp.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNameValidatorTest {
    @Test fun acceptsWexPackage() {
        assertTrue(PackageNameValidator.isValid("com.efsllc.efsmobileapp"))
    }

    @Test fun rejectsMalformedOrUnexpectedInputs() {
        listOf("", "com", "com..example", "../secret", "com.example/evil",
            "com.example?x=1", " com.example", "com.example\nother",
            "com.example-foo").forEach {
            assertFalse("Should reject: $it", PackageNameValidator.isValid(it))
        }
    }

    @Test fun rejectsExcessiveLength() {
        assertFalse(PackageNameValidator.isValid("com." + "x".repeat(300)))
    }
}
