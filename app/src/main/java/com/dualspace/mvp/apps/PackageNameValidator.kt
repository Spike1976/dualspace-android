package com.dualspace.mvp.apps

/** Strict enough for user-entered Android package IDs without requiring package visibility. */
object PackageNameValidator {
    private val pattern = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")
    fun isValid(value: String): Boolean =
        value.length in 3..255 && pattern.matches(value)
}
