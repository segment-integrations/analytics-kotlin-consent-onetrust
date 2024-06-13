package com.segment.analytics.kotlin.destinations.consent.onetrust

import com.onetrust.otpublishers.headless.Public.OTPublishersHeadlessSDK
import com.segment.analytics.kotlin.consent.ConsentCategoryProvider

class OneTrustConsentCategoryProvider(
    val otPublishersHeadlessSDK: OTPublishersHeadlessSDK,
    var categories: List<String> = emptyList()
) : ConsentCategoryProvider {




    override fun getCategories(): Map<String, Boolean> {
        var categoryConsentMap = HashMap<String, Boolean>()

        categories.forEach { category ->
            val consent = otPublishersHeadlessSDK.getConsentStatusForGroupId(category)
            val consentValue = when (consent) {
                1 -> true
                else -> false
            }

            categoryConsentMap.put(category, consentValue)
        }

        return categoryConsentMap
    }

    override fun setCategoryList(categories: List<String>) {
        this.categories = categories
    }
}