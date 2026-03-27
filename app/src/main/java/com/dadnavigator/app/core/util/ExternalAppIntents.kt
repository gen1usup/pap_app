package com.dadnavigator.app.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the dialer for the provided phone number.
 */
fun openDialer(context: Context, phone: String) {
    if (phone.isBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}")))
}

/**
 * Opens the preferred maps app with a route query and falls back to a browser search.
 */
fun openRoute(context: Context, address: String) {
    if (address.isBlank()) return

    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=${Uri.encode(address.trim())}")
    )

    runCatching {
        context.startActivity(geoIntent)
    }.recoverCatching {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address.trim())}")
        )
        context.startActivity(browserIntent)
    }.getOrElse { error ->
        if (error !is ActivityNotFoundException) {
            throw error
        }
    }
}
