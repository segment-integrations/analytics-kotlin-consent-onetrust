package com.segment.analytics.destinations.mydestination.testapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.onetrust.otpublishers.headless.Public.OTPublishersHeadlessSDK
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.platform.policies.CountBasedFlushPolicy
import com.segment.analytics.kotlin.core.platform.policies.FrequencyFlushPolicy
import com.segment.analytics.kotlin.destinations.consent.ConsentBlockingPlugin
import com.segment.analytics.kotlin.destinations.consent.ConsentManagementPlugin
import com.segment.analytics.kotlin.destinations.consent.onetrust.OneTrustConsentCategoryProvider
import sovran.kotlin.SynchronousStore

class MainApplication: Application() {

    companion object {
        const val TAG = "main"
        var appContext: Context? = null
            private set
        lateinit var analytics: Analytics

        var haveShownOTBanner = false
        lateinit var otPublishersHeadlessSDK: OTPublishersHeadlessSDK

        // Update these:
        private const val SEGMENT_WRITE_KEY = "<Your Segment WRITEKEY>"
        const val DOMAIN_URL  = "<Your OneTrust Domain URL>"
        const val DOMAIN_ID   = "<Your OneTrust Domain ID>"
        const val WEBHOOK_URL = "<Your webhook.site webhook url>"
    }
    

    override fun onCreate() {
        super.onCreate()

        appContext = this
        otPublishersHeadlessSDK = OTPublishersHeadlessSDK(this)

        Analytics.debugLogsEnabled = true

        analytics = Analytics(SEGMENT_WRITE_KEY, applicationContext) {
            this.collectDeviceId = true
            this.trackApplicationLifecycleEvents = true
            this.trackDeepLinks = true
            this.flushPolicies = listOf(
                CountBasedFlushPolicy(1), // Flush after each event
                FrequencyFlushPolicy(5000) // Flush after 5 Seconds
            )
        }

        // List of categories we care about; we will query OneTrust SDK locally on the status
        // of these categories when stamping an event with consent status.
        val categories = listOf<String>("C0001", "C0002")
        val consentCategoryProvider = OneTrustConsentCategoryProvider(otPublishersHeadlessSDK, categories)
        val store = SynchronousStore()
        val consentPlugin = ConsentManagementPlugin(store, consentCategoryProvider)
        val consentBlockingPlugin = ConsentBlockingPlugin("Segment.io", store, true)

        // Add the Consent Plugin directly to analytics
        analytics.add(consentPlugin)


        // Add the WebhookPlugin that will post to given WEBHOOK_URL
        val webhookDestinationPlugin = WebhookPlugin(WEBHOOK_URL)
        // Add the webhook destination plugin into the main timeline
        analytics.add(webhookDestinationPlugin)
        // Add the blocking plugin to this destination
        webhookDestinationPlugin.add(ConsentBlockingPlugin("Webhook", store))
    }
}