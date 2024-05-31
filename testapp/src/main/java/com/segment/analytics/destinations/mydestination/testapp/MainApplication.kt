package com.segment.analytics.destinations.mydestination.testapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.onetrust.otpublishers.headless.Public.OTCallback
import com.onetrust.otpublishers.headless.Public.OTPublishersHeadlessSDK
import com.onetrust.otpublishers.headless.Public.Response.OTResponse
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.core.platform.policies.CountBasedFlushPolicy
import com.segment.analytics.kotlin.core.platform.policies.FrequencyFlushPolicy
import com.segment.analytics.kotlin.destinations.consent.ConsentManager
import com.segment.analytics.kotlin.destinations.consent.onetrust.OneTrustConsentCategoryProvider
import org.json.JSONException
import org.json.JSONObject
import sovran.kotlin.SynchronousStore

class MainApplication : Application() {

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



        const val LANGUAGE_CODE = "en"
    }


    private fun getGroupIds(domainGroupData: JSONObject): List<String> {
        val result: MutableList<String> = ArrayList()
        try {
            val groups = domainGroupData.getJSONArray("Groups")
            for (i in 0 until groups.length()) {
                val group = groups.getJSONObject(i)
                val groupId = group.getString("OptanonGroupId")
                result.add(groupId)
            }
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        return result
    }

    override fun onCreate() {
        super.onCreate()

        appContext = this

        Analytics.debugLogsEnabled = true
        analytics = Analytics(SEGMENT_WRITE_KEY, applicationContext) {
            this.collectDeviceId = true
            this.trackApplicationLifecycleEvents = true
            this.trackDeepLinks = true
            this.flushPolicies = mutableListOf(
                CountBasedFlushPolicy(1), // Flush after each event
                FrequencyFlushPolicy(5000) // Flush after 5 Seconds
            )
        }

        analytics.add(WebhookPlugin(WEBHOOK_URL))

        otPublishersHeadlessSDK = OTPublishersHeadlessSDK(this)

        val consentCategoryProvider = OneTrustConsentCategoryProvider(otPublishersHeadlessSDK)
        val store = SynchronousStore()

        val consentPlugin = ConsentManager(store, consentCategoryProvider)

        analytics.add(consentPlugin)

        otPublishersHeadlessSDK.startSDK(
            DOMAIN_URL,
            DOMAIN_ID,
            LANGUAGE_CODE,
            null,
            false,
            object : OTCallback {
                override fun onSuccess(otSuccessResponse: OTResponse) {
                    // do logic to render UI getOTSDKData();
                    val otData =
                        MainApplication.otPublishersHeadlessSDK.bannerData.toString()
                    Log.d(TAG, "OT onSuccess: otData: $otData")

                    val categories =
                        getGroupIds(MainApplication.otPublishersHeadlessSDK.domainGroupData)

                    Log.d(TAG, "Setting up Analytics with categories: ${categories}")
                    consentCategoryProvider.setCategoryList(categories)

                    // This call starts the events following through the ConsentManagement Plugin
                    // The plugin will BLOCK all events until start() is called. Here we do it after
                    // we have gotten valid information from OneTrust, so you MUST enter valid OneTrust
                    // Configuration information for events to flow.
                    consentPlugin.start()
                }

                override fun onFailure(otErrorResponse: OTResponse) {
                    // Use below method to get errorCode and errorMessage.
                    val errorCode = otErrorResponse.responseCode
                    val errorDetails = otErrorResponse.responseMessage
                    // Use toString() to log complete OT response

                    Log.i(TAG, otErrorResponse.toString())
                }
            }
        )
    }
}