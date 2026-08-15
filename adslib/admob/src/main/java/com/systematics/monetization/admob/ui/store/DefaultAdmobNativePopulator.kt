package com.systematics.monetization.admob.ui.store


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.systematics.monetization.admob.R
import com.systematics.monetization.admob.models.natives.AdmobAppNativeAd
import com.systematics.monetization.core.integration.natives.NativeLayoutPopulator
import com.systematics.monetization.core.models.natives.NativeUiDataModel
import com.systematics.monetization.core.utils.dpToPx
import com.systematics.monetization.core.utils.gone
import com.systematics.monetization.core.utils.hide
import com.systematics.monetization.core.utils.show
import com.systematics.monetization.core.utils.toColor
import kotlin.math.roundToInt

class DefaultAdmobNativePopulator() : NativeLayoutPopulator<AdmobAppNativeAd> {

    override fun populate(
        context: Context,
        view: View,
        ad: AdmobAppNativeAd,
        nativeUiDataModel: NativeUiDataModel
    ) {
        val nativeAd = ad.nativeAd

        val nativeAdView = view.findViewById<NativeAdView>(R.id.nativeAdView)

        if (nativeAdView == null) {
            Log.d(TAG, "populate: No native ad view")
            return
        }

        val textPrimaryColor = nativeUiDataModel.textPrimaryColor.toColor()
        val textSecondaryColor = nativeUiDataModel.textSecondaryColor.toColor()
        val ctaTextColor = nativeUiDataModel.ctaTextColor.toColor()
        val ctaBgColor = nativeUiDataModel.ctaBgColor.toColor()
        val bgColor = nativeUiDataModel.bgColor.toColor()


        val adRoot = view.findViewById<ViewGroup>(R.id.ad_root)
        val headline = view.findViewById<TextView>(R.id.ad_headline)
        val body = view.findViewById<TextView>(R.id.ad_body)
        val cta = view.findViewById<Button>(R.id.ad_call_to_action)
        val icon = view.findViewById<ImageView>(R.id.ad_app_icon)
        val price = view.findViewById<TextView>(R.id.ad_price)
        val stars = view.findViewById<RatingBar>(R.id.ad_stars)
        val store = view.findViewById<TextView>(R.id.ad_store)
        val advertiser = view.findViewById<TextView>(R.id.ad_advertiser)
        val adIndicator = view.findViewById<TextView>(R.id.tv_ad)

        val media = view.findViewById<MediaView>(R.id.ad_media)

        adRoot?.let { r ->
            r.show()
            r.setBackgroundColor(bgColor)
        }
        headline?.let { h ->
            nativeAdView.headlineView = h
            h.show()
            h.text = nativeAd.headline
            h.setTextColor(textPrimaryColor)
        }
        media?.let { m ->
            nativeAd.mediaContent?.let {
                m.mediaContent = it
            }
        }
        body?.let { b ->
            nativeAdView.bodyView = b
            nativeAd.body?.let {
                b.show()
                b.text = it
                b.setTextColor(textSecondaryColor)

            } ?: run { b.hide() }
        }
        cta?.let { c ->
            nativeAdView.callToActionView = c
            nativeAd.callToAction?.let {
                c.show()
                c.text = it
                c.setTextColor(ctaTextColor)
                (c.background as? GradientDrawable)?.setColor(ctaBgColor) ?: run {
                    c.setBackgroundColor(ctaBgColor)
                }
            } ?: run { c.hide() }
        }
        icon?.let { i ->
            nativeAdView.iconView = i
            nativeAd.icon?.drawable?.let {
                i.show()
                i.setImageDrawable(it)
            } ?: run { i.gone() }
        }
        price?.let { p ->
            nativeAdView.priceView = p
            nativeAd.price?.let {
                p.show()
                p.setTextColor(textSecondaryColor)
                p.text = it
            } ?: run { p.hide() }
        }
        store?.let { s ->
            nativeAdView.storeView = s
            nativeAd.store?.let {
                s.show()
                s.setTextColor(textSecondaryColor)
                s.text = it
            } ?: run { s.hide() }
        }
        stars?.let { s ->
            nativeAdView.starRatingView = s
            nativeAd.starRating?.let {
                s.show()
                s.rating = it.toFloat()
            } ?: run { s.hide() }
        }
        advertiser?.let { a ->
            nativeAdView.advertiserView = a
            nativeAd.advertiser?.let {
                a.show()
                a.setTextColor(textSecondaryColor)
                a.text = it
            } ?: run { a.hide() }
        }
        adIndicator?.let { i ->
            i.show()
            (i.background as? GradientDrawable)?.setStroke(
                1.dpToPx.roundToInt(),
                textSecondaryColor
            )
                ?: run {
                    i.setBackgroundColor(textSecondaryColor)
                }
            i.setTextColor(textSecondaryColor)
        }

        nativeAdView.registerNativeAd(nativeAd, media)
    }

    companion object {

        private const val TAG = "DefaultAdmobNativePopulator"
    }
}