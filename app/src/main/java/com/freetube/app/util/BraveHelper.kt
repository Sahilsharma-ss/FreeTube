package com.freetube.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object BraveHelper {

    val BRAVE_PACKAGES = listOf(
        "com.brave.browser",
        "com.brave.browser_beta",
        "com.brave.browser_nightly",
        "com.brave.browser_release"
    )

    /**
     * Finds which Brave package is installed, if any.
     */
    fun getInstalledBravePackage(context: Context): String? {
        val pm = context.packageManager

        // 1. Direct getPackageInfo check for all known Brave package IDs
        for (pkg in BRAVE_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: Exception) {}
        }

        // 2. Query activities resolving web intents to catch any Brave variant
        try {
            val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            val resolveInfos = pm.queryIntentActivities(testIntent, 0)
            for (info in resolveInfos) {
                val pkgName = info.activityInfo.packageName
                if (BRAVE_PACKAGES.contains(pkgName) || pkgName.contains("brave", ignoreCase = true)) {
                    return pkgName
                }
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Checks if Brave is installed on this device.
     */
    fun isBraveInstalled(context: Context): Boolean {
        return getInstalledBravePackage(context) != null
    }

    /**
     * Tries to launch the URL directly in Brave Browser.
     * Returns true if successfully launched, false if Brave is not available.
     */
    fun launchUrlInBrave(context: Context, url: String): Boolean {
        val targetPackage = getInstalledBravePackage(context) ?: "com.brave.browser"

        val braveIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(braveIntent)
            true
        } catch (_: ActivityNotFoundException) {
            // Also try with default com.brave.browser if a variant was selected
            if (targetPackage != "com.brave.browser") {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            setPackage("com.brave.browser")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                    true
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Fallback to launch in any available browser.
     */
    fun launchUrlInAnyBrowser(context: Context, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Open Brave's Google Play Store page.
     */
    fun openBravePlayStore(context: Context) {
        val storeIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.brave.browser")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(storeIntent)
        } catch (_: Exception) {}
    }
}
