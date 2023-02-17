package com.matejdr.admanager;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AppEventListener;
import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.utils.Targeting;

import java.util.ArrayList;
import java.util.List;

class AdaptiveBannerAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {
    protected AdManagerAdView adManagerAdView;
    Activity currentActivityContext;
    String[] testDevices;
    String adUnitID;
    String adPosition;
    Integer maxHeight;

    // Targeting
    Boolean hasTargeting = false;
    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String content_url;
    String publisherProvidedID;
    Location location;
    String correlator;

    int top;
    int left;
    int width;
    int height;

    public AdaptiveBannerAdView(final Context context, ReactApplicationContext applicationContext) {
        super(context);
        currentActivityContext = applicationContext.getCurrentActivity();
        applicationContext.addLifecycleEventListener(this);
        this.createAdView();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(new MeasureAndLayoutRunnable());
    }

    private void createAdView() {
        if (this.adManagerAdView != null) this.adManagerAdView.destroy();
        if (this.currentActivityContext == null) return;

        this.adManagerAdView = new AdManagerAdView(currentActivityContext);

        AdManagerAdView.LayoutParams layoutParams = new AdManagerAdView.LayoutParams(
            ReactViewGroup.LayoutParams.MATCH_PARENT,
            ReactViewGroup.LayoutParams.WRAP_CONTENT
        );
        this.adManagerAdView.setLayoutParams(layoutParams);

        this.adManagerAdView.setAppEventListener(this);
        this.adManagerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                top = adManagerAdView.getTop();
                left = adManagerAdView.getLeft();
                width = adManagerAdView.getAdSize().getWidthInPixels(getContext());
                height = adManagerAdView.getAdSize().getHeightInPixels(getContext());

                sendOnSizeChangeEvent();

                WritableMap ad = Arguments.createMap();
                ad.putString("type", "banner");

                WritableMap gadSize = Arguments.createMap();
                gadSize.putString("adSize", adManagerAdView.getAdSize().toString());
                gadSize.putDouble("width", adManagerAdView.getAdSize().getWidth());
                gadSize.putDouble("height", adManagerAdView.getAdSize().getHeight());
                ad.putMap("gadSize", gadSize);

                ad.putString("isFluid", "false");

                WritableMap measurements = Arguments.createMap();
                measurements.putInt("adWidth", width);
                measurements.putInt("adHeight", height);
                measurements.putInt("width", getMeasuredWidth());
                measurements.putInt("height", getMeasuredHeight());
                measurements.putInt("left", left);
                measurements.putInt("top", top);
                ad.putMap("measurements", measurements);

                sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_AD_LOADED, ad);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                String errorMessage = "Unknown error";
                switch (adError.getCode()) {
                    case AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        errorMessage = "Internal error, an invalid response was received from the ad server.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_INVALID_REQUEST:
                        errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_NETWORK_ERROR:
                        errorMessage = "The ad request was unsuccessful due to network connectivity.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_NO_FILL:
                        errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                        break;
                }
                WritableMap event = Arguments.createMap();
                WritableMap error = Arguments.createMap();
                error.putString("message", errorMessage);
                event.putMap("error", error);
                sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
            }

            @Override
            public void onAdOpened() {
                WritableMap event = Arguments.createMap();
                sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_AD_OPENED, event);
            }

            @Override
            public void onAdClosed() {
                WritableMap event = Arguments.createMap();
                sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_AD_CLOSED, event);
            }

        });
        this.addView(this.adManagerAdView);
    }

    private void sendOnSizeChangeEvent() {
        int width;
        int height;
        WritableMap event = Arguments.createMap();
        AdSize adSize = this.adManagerAdView.getAdSize();
        width = adSize.getWidth();
        height = adSize.getHeight();
        event.putString("type", "banner");
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            name,
            event);
    }

    public void loadBanner() {
        this.adManagerAdView.setAdSizes(getAdSize());

        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        List<String> testDevicesList = new ArrayList<>();
        if (testDevices != null) {
            for (String device : testDevices) {
                String testDevice = device;
                if (testDevice == "SIMULATOR") {
                    testDevice = AdManagerAdRequest.DEVICE_ID_EMULATOR;
                }
                testDevicesList.add(testDevice);
            }
            RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevicesList)
                .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
        }

        if (correlator == null) {
            correlator = (String) Targeting.getCorelator(adUnitID);
        }
        Bundle bundle = new Bundle();
        bundle.putString("correlator", correlator);

        adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);

        // Targeting
        if (hasTargeting) {
            if (customTargeting != null && customTargeting.length > 0) {
                for (int i = 0; i < customTargeting.length; i++) {
                    String key = customTargeting[i].key;
                    if (!key.isEmpty()) {
                        if (customTargeting[i].value != null && !customTargeting[i].value.isEmpty()) {
                            adRequestBuilder.addCustomTargeting(key, customTargeting[i].value);
                        } else if (customTargeting[i].values != null && !customTargeting[i].values.isEmpty()) {
                            adRequestBuilder.addCustomTargeting(key, customTargeting[i].values);
                        }
                    }
                }
            }
            if (categoryExclusions != null && categoryExclusions.length > 0) {
                for (int i = 0; i < categoryExclusions.length; i++) {
                    String categoryExclusion = categoryExclusions[i];
                    if (!categoryExclusion.isEmpty()) {
                        adRequestBuilder.addCategoryExclusion(categoryExclusion);
                    }
                }
            }
            if (keywords != null && keywords.length > 0) {
                for (int i = 0; i < keywords.length; i++) {
                    String keyword = keywords[i];
                    if (!keyword.isEmpty()) {
                        adRequestBuilder.addKeyword(keyword);
                    }
                }
            }
            if (content_url != null) {
                adRequestBuilder.setContentUrl(content_url);
            }
            if (publisherProvidedID != null) {
                adRequestBuilder.setPublisherProvidedId(publisherProvidedID);
            }
            if (location != null) {
                adRequestBuilder.setLocation(location);
            }
        }

        AdManagerAdRequest adRequest = adRequestBuilder.build();
        this.adManagerAdView.loadAd(adRequest);
    }

    public void setAdPosition(String adPosition) {
        this.adPosition = adPosition;
    }

    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setAdUnitID(String adUnitID) {
        if (this.adUnitID != null) {
            // We can only set adUnitID once, so when it was previously set we have
            // to recreate the view
            this.createAdView();
        }
        this.adUnitID = adUnitID;
        this.adManagerAdView.setAdUnitId(adUnitID);
    }

    public void setTestDevices(String[] testDevices) {
        this.testDevices = testDevices;
    }

    // Targeting
    public void setCustomTargeting(CustomTargeting[] customTargeting) {
        this.customTargeting = customTargeting;
    }

    public void setCategoryExclusions(String[] categoryExclusions) {
        this.categoryExclusions = categoryExclusions;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public void setContentURL(String content_url) {
        this.content_url = content_url;
    }

    public void setPublisherProvidedID(String publisherProvidedID) {
        this.publisherProvidedID = publisherProvidedID;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNAdManagerAdaptiveBannerViewManager.EVENT_APP_EVENT, event);
    }

    @Override
    public void onHostResume() {
        if (this.adManagerAdView != null) {
            this.adManagerAdView.resume();
        }
    }

    @Override
    public void onHostPause() {
        if (this.adManagerAdView != null) {
            this.adManagerAdView.pause();
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.adManagerAdView != null) {
            this.currentActivityContext = null;
            this.adManagerAdView.destroy();
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = currentActivityContext.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = getWidth();
        // If the ad width isn't known, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }
        int adWidth = (int) (adWidthPixels / density);

        if ("currentOrientationAnchored".equals(adPosition)) {
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(currentActivityContext, adWidth);
        }
        if ("currentOrientationInline".equals(adPosition)) {
            return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(currentActivityContext, adWidth);
        }
        if ("portraitInline".equals(adPosition)) {
            return AdSize.getPortraitInlineAdaptiveBannerAdSize(currentActivityContext, adWidth);
        }
        if ("landscapeInline".equals(adPosition)) {
            return AdSize.getLandscapeInlineAdaptiveBannerAdSize(currentActivityContext, adWidth);
        }
        // else if "inline"
        int adHeightPixels;
        if (maxHeight == null) {
            adHeightPixels = getHeight();
            // If the ad height isn't known, default to 350 height.
            if (adHeightPixels == 0) {
                adHeightPixels = 350;
            }
        } else {
            adHeightPixels = maxHeight;
        }
        int adMaxHeight = (int) (adHeightPixels / density);
        return AdSize.getInlineAdaptiveBannerAdSize(adWidth, adMaxHeight);
    }

    private class MeasureAndLayoutRunnable implements Runnable {
        @Override
        public void run() {
            adManagerAdView.measure(width, height);
            adManagerAdView.layout(left, top, left + width, top + height);
        }
    }
}
