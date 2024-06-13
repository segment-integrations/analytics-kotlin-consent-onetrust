package com.segment.analytics.kotlin.consent.onetrust

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.onetrust.otpublishers.headless.Public.Keys.OTBroadcastServiceKeys
import com.segment.analytics.kotlin.consent.ConsentManager
import java.lang.ref.WeakReference

class OneTrustConsentChangedNotifier(
    val contextReference: WeakReference<Context>,
    val categories: List<String>,
    val consentPlugin: ConsentManager
) {

    private val consentChangedReceiver: BroadcastReceiver? = null

    fun register() {
        if (consentChangedReceiver != null) {
            unregister()
        }

        val context = contextReference.get()
        categories.forEach {

            if (context != null) {
                context.registerReceiver(
                    OneTrustConsentChangedReceiver(consentPlugin),
                    IntentFilter(OTBroadcastServiceKeys.OT_CONSENT_UPDATED)
                )
            }
        }
    }

    fun unregister() {
        val context = contextReference.get()
        if (context != null) {
            context.unregisterReceiver(consentChangedReceiver)
        }
    }
}

class OneTrustConsentChangedReceiver(val consentPlugin: ConsentManager) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        consentPlugin.notifyConsentChanged()
    }
}