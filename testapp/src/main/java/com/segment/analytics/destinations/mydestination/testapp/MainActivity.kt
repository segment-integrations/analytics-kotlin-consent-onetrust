package com.segment.analytics.destinations.mydestination.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.onetrust.otpublishers.headless.Public.OTCallback
import com.onetrust.otpublishers.headless.Public.Response.OTResponse
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = MainApplication.TAG + "/activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.sendTrackEventButton)
        button.setOnClickListener {v ->
            val sdf = SimpleDateFormat("M/dd/yyyy hh:mm:ss.SSS")
            val currentDate = sdf.format(Date())
            MainApplication.analytics.track("Test Event ${currentDate}")

            if (v != null) {
                Snackbar.make(v, "Track Event created.", Snackbar.LENGTH_LONG).show()
            }
        }


        setupOneTrust()
    }

    private fun setupOneTrust() {
        MainApplication.otPublishersHeadlessSDK.startSDK(
            MainApplication.DOMAIN_URL,
            MainApplication.DOMAIN_ID,
            "en",
            null,
            false,
            object : OTCallback {
                override fun onSuccess(p0: OTResponse) {
                    Log.d(TAG, "onSuccess: SDK Started")
                    if (!MainApplication.haveShownOTBanner) {
                        MainApplication.otPublishersHeadlessSDK.showBannerUI(this@MainActivity)
                        MainApplication.haveShownOTBanner = true
                    }
                }

                override fun onFailure(p0: OTResponse) {
                    Log.d(TAG, "onFailure: Failed to start SDK")
                }

            }
        )
    }
}