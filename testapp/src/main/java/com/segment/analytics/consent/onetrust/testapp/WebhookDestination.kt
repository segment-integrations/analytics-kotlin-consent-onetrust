package com.segment.analytics.consent.onetrust.testapp

import android.util.Log
import com.segment.analytics.kotlin.core.*
import com.segment.analytics.kotlin.core.platform.DestinationPlugin
import com.segment.analytics.kotlin.core.platform.EventPlugin
import com.segment.analytics.kotlin.core.platform.Plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WebhookPlugin(val webhookUrl: String) : DestinationPlugin() {
    override val key: String = "Webhook"
    override lateinit var analytics: Analytics
    val JSON = "application/json; charset=utf-8".toMediaType()
    val okHttpClient = OkHttpClient()
    private val TAG = MainApplication.TAG + "/WebhookDestination"


    override fun track(payload: TrackEvent): BaseEvent? {
        uploadPayload(payload)
        return payload
    }

    override fun identify(payload: IdentifyEvent): BaseEvent? {
        uploadPayload(payload)
        return payload
    }

    override fun screen(payload: ScreenEvent): BaseEvent? {
        uploadPayload(payload)
        return payload
    }

    override fun group(payload: GroupEvent): BaseEvent? {
        uploadPayload(payload)
        return payload
    }

    override fun alias(payload: AliasEvent): BaseEvent? {
        uploadPayload(payload)
        return payload
    }

    private fun uploadPayload(payload: BaseEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "uploadPayload: $payload")
            sendPayloadToWebhook(analytics, webhookUrl, payload)
        }
    }

    private suspend fun sendPayloadToWebhook(
        analytics: Analytics,
        webhookUrl: String,
        payload: BaseEvent
    ) {
        withContext(analytics.networkIODispatcher) {


            val jsonPayload = buildJsonObject {
                put("type", JsonPrimitive(payload.type.toString()))
                if (payload is TrackEvent) {
                    put("event", JsonPrimitive(payload.event))
                    put("properties", payload.properties)
                    put("context", payload.context)
                }
            }

            val payloadString = jsonPayload.toString()
            val body = payloadString.toRequestBody(JSON)
            val request = Request.Builder().url(webhookUrl).post(body).build()

            try {
                val response = okHttpClient.newCall(request).execute()
                response.body?.close()
            } catch (e: Exception) {
                println("Error ${e}")
                Log.d(MainApplication.TAG, "sendPayloadToWebhook: Error sending payload $e", e)
            }
        }
    }
}