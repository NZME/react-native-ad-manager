package com.matejdr.admanager;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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

class BannerAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {

    protected AdManagerAdView adView;
    Activity currentActivityContext;
    String[] testDevices;
    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;
    boolean isFluid = false;

    // Targeting
    Boolean hasTargeting = false;
    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String contentURL;
    String publisherProvidedID;
    Location location;
    String correlator;

    public BannerAdView(final Context context, ReactApplicationContext applicationContext) {
        super(context);

        try {
            currentActivityContext = applicationContext.getCurrentActivity();
            applicationContext.addLifecycleEventListener(this);
            this.createAdView();
        } catch (Exception exception) { this.onException(exception); }
    }

    private void onException(Exception exception) {
        try {
            WritableMap event = Arguments.createMap();
            WritableMap error = Arguments.createMap();
            String exceptionMessage = exception.getMessage();
            String message = exceptionMessage != null ? exceptionMessage : "Unknown error";
            error.putString("message", message);
            event.putMap("error", error);
            sendEvent(RNAdManagerBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);

            if (this.adView != null) {
                try {
                    this.adView.destroy();
                } catch (Exception innerIgnored) {}

                this.adView = null;
            }
        } catch (Exception ignored) {}
    }

    private void createAdView() {
        try {
            if (this.adView != null) {
                this.adView.destroy();
            }

            this.adView = new AdManagerAdView(currentActivityContext);

            this.adView.setAppEventListener(this);
            this.adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    try {
                        AdSize adSize = adView.getAdSize();
                        Context context = getContext();

                        int width = adSize.getWidthInPixels(context);
                        int height = adSize.getHeightInPixels(context);

                        int left = adView.getLeft();
                        int top = adView.getTop();

                        View parent = (View) adView.getParent();

                        if (parent != null) {
                            int parentWidth = parent.getWidth();

                            left = (parentWidth - width) / 2;
                        }

                        adView.measure(width, height);
                        adView.layout(left, top, left + width, top + height);

                        sendOnSizeChangeEvent();
                        WritableMap ad = Arguments.createMap();
                        ad.putString("type", "banner");

                        WritableMap gadSize = Arguments.createMap();

                        int adWidth = adSize.getWidth();
                        int adHeight = adSize.getHeight();

                        gadSize.putDouble("width", adWidth);
                        gadSize.putDouble("height", adHeight);

                        ad.putMap("gadSize", gadSize);

                        sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LOADED, ad);
                    } catch (Exception exception) { }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    try {
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
                        sendEvent(RNAdManagerBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
                    } catch (Exception exception) { }
                }

                @Override
                public void onAdOpened() {
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_OPENED, null);
                }

                @Override
                public void onAdClosed() {
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_CLOSED, null);
                }

            });

            this.addView(this.adView);
        } catch (Exception exception) {
            this.onException(exception);
        }
    }

    private class MeasureAndLayoutRunnable implements Runnable {
        @Override
        public void run() {
            updateLayout();
        }
    }

    private boolean isFluid() {
        return this.isFluid;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        try {
            if (isFluid()) {
                post(new MeasureAndLayoutRunnable());
            }
        } catch (Exception exception) { }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        try {
            if (isFluid()) {
                post(new MeasureAndLayoutRunnable());
            }
        } catch (Exception exception) { }
    }

    private static void measureAndLayout(View view, int width, int height) {
        try {
            int left = 0;
            int top = 0;

            view.measure(width, height);
            view.layout(left, top, left + width, top + height);
            view.requestLayout();
            view.invalidate();
            view.forceLayout();
        } catch (Exception exception) { }
    }

    int cachedWidth = 0;
    int cachedHeight = 0;

    private void updateLayout() {
        try {
            if (!isFluid()) {
                return;
            }

            if (adView == null) {
                return;
            }

            View parent = (View) adView.getParent();

            if (parent == null) {
                return;
            }

            int width = parent.getWidth();
            int height = parent.getHeight();

            if (cachedWidth == width && cachedHeight == height) {
                return;
            }

            cachedWidth = width;
            cachedHeight = height;

            // In case of fluid ads, every GAD view and their subviews must be laid out by hand,
            // otherwise the web view won't align to the container bounds.
            measureAndLayout(adView, width, height);

            ViewGroup child = (ViewGroup) adView.getChildAt(0);

            if (child != null) {
                measureAndLayout(child, width, height);

                ViewGroup webView = (ViewGroup) child.getChildAt(0);

                if (webView != null) {
                    measureAndLayout(webView, width, height);

                    ViewGroup internalChild = (ViewGroup) webView.getChildAt(0);

                    if (internalChild != null) {
                        measureAndLayout(internalChild, width, height);

                        ViewGroup leafNode = (ViewGroup) internalChild.getChildAt(0);

                        if (leafNode != null) {
                            measureAndLayout(leafNode, width, height);
                        }
                    }
                }
            }
        } catch (Exception exception) {
        }
    }

    private void sendOnSizeChangeEvent() {
        try {
            int width;
            int height;
            WritableMap event = Arguments.createMap();
            AdSize adSize = this.adView.getAdSize();
            width = adSize.getWidth();
            height = adSize.getHeight();
            event.putString("type", "banner");
            event.putDouble("width", width);
            event.putDouble("height", height);
            sendEvent(RNAdManagerBannerViewManager.EVENT_SIZE_CHANGE, event);
        } catch (Exception exception) { }
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        try {
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    getId(),
                    name,
                    event);
        } catch (Exception exception) { }
    }

    public void loadBanner() {
        try {
            ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
            if (this.adSize != null) {
                adSizes.add(this.adSize);
            }
            if (this.validAdSizes != null) {
                for (int i = 0; i < this.validAdSizes.length; i++) {
                    if (!adSizes.contains(this.validAdSizes[i])) {
                        adSizes.add(this.validAdSizes[i]);
                    }
                }
            }

            if (adSizes.size() == 0) {
                adSizes.add(AdSize.BANNER);
            }

            AdSize[] adSizesArray = adSizes.toArray(new AdSize[adSizes.size()]);
            this.adView.setAdSizes(adSizesArray);

            AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

            List<String> testDevicesList = new ArrayList<>();
            if (testDevices != null) {
                for (int i = 0; i < testDevices.length; i++) {
                    String testDevice = testDevices[i];
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
                if (contentURL != null) {
                    adRequestBuilder.setContentUrl(contentURL);
                }
                if (publisherProvidedID != null) {
                    adRequestBuilder.setPublisherProvidedId(publisherProvidedID);
                }
                if (location != null) {
                    adRequestBuilder.setLocation(location);
                }
            }

            AdManagerAdRequest adRequest = adRequestBuilder.build();
            this.adView.loadAd(adRequest);
        } catch (Exception exception) { this.onException(exception); }
    }

    public void setAdUnitID(String adUnitID) {
        try {
            if (this.adUnitID != null) {
                // We can only set adUnitID once, so when it was previously set we have
                // to recreate the view
                this.createAdView();
            }
            this.adUnitID = adUnitID;
            this.adView.setAdUnitId(adUnitID);
        } catch (Exception exception) { }
    }

    public void setTestDevices(String[] testDevices) {
        try {
            this.testDevices = testDevices;
        } catch (Exception exception) { }
    }

    // Targeting
    public void setCustomTargeting(CustomTargeting[] customTargeting) {
        try {
            this.customTargeting = customTargeting;
        } catch (Exception exception) { }
    }

    public void setCategoryExclusions(String[] categoryExclusions) {
        try {
            this.categoryExclusions = categoryExclusions;
        } catch (Exception exception) { }
    }

    public void setKeywords(String[] keywords) {
        try {
            this.keywords = keywords;
        } catch (Exception exception) { }
    }

    public void setContentURL(String contentURL) {
        try {
            this.contentURL = contentURL;
        } catch (Exception exception) { }
    }

    public void setPublisherProvidedID(String publisherProvidedID) {
        try {
            this.publisherProvidedID = publisherProvidedID;
        } catch (Exception exception) { }
    }

    public void setLocation(Location location) {
        try {
            this.location = location;
        } catch (Exception exception) { }
    }

    public void setAdSize(AdSize adSize) {
        try {
            this.adSize = adSize;
        } catch (Exception exception) { }
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        try {
            this.validAdSizes = adSizes;
        } catch (Exception exception) { }
    }

    public void setCorrelator(String correlator) {
        try {
            this.correlator = correlator;
        } catch (Exception exception) { }
    }

    @Override
    public void onAppEvent(String name, String info) {
        try {
            this.isFluid = true;

            this.updateLayout();

            WritableMap event = Arguments.createMap();
            event.putString("name", name);
            event.putString("info", info);
            sendEvent(RNAdManagerBannerViewManager.EVENT_APP_EVENT, event);
        } catch (Exception exception) { }
    }

    @Override
    public void onHostResume() {
        try {
            if (this.adView != null) {
                this.adView.resume();
            }
        } catch (Exception exception) { }
    }

    @Override
    public void onHostPause() {
        try {
            if (this.adView != null) {
                this.adView.pause();
            }
        } catch (Exception exception) { }
    }

    @Override
    public void onHostDestroy() {
        try {
            if (this.adView != null) {
                this.currentActivityContext = null;
                this.adView.destroy();
            }
        } catch (Exception exception) { };
    }
}
