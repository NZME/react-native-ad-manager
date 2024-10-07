package com.matejdr.admanager;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

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

import com.facebook.react.modules.core.DeviceEventManagerModule;

class BannerAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {
    protected AdManagerAdView adManagerAdView;
    ReactApplicationContext currentRNcontext;
    Activity currentActivityContext;
    String[] testDevices;
    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;

    // Targeting
    Boolean hasTargeting = false;
    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String content_url;
    String publisherProvidedID;
    Location location;
    String correlator;

    Boolean servePersonalizedAds = true;

    int top;
    int left;
    int width;
    int height;

    public BannerAdView(final Context context, ReactApplicationContext applicationContext) {
        super(context);
        try {
            currentRNcontext = applicationContext;
            currentActivityContext = applicationContext.getCurrentActivity();
            applicationContext.addLifecycleEventListener(this);
            this.createAdView();
        } catch (Exception exception) {
            this.onException(exception);
        }
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

            if (this.adManagerAdView != null) {
                try {
                    this.adManagerAdView.destroy();
                } catch (Exception innerIgnored) {
                    // ignore it
                }
                this.adManagerAdView = null;
            }

        } catch (Exception e) {
            // ignore it
        }
    }

    private boolean isFluid() {
        return AdSize.FLUID.equals(this.adSize);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(new MeasureAndLayoutRunnable());
    }

    private void createAdView() {
        try {
            if (this.adManagerAdView != null)
                this.adManagerAdView.destroy();
            if (this.currentActivityContext == null)
                return;

            this.adManagerAdView = new AdManagerAdView(currentActivityContext);

            if (isFluid()) {
                AdManagerAdView.LayoutParams layoutParams = new AdManagerAdView.LayoutParams(
                        ReactViewGroup.LayoutParams.MATCH_PARENT,
                        ReactViewGroup.LayoutParams.WRAP_CONTENT);
                this.adManagerAdView.setLayoutParams(layoutParams);
            }

            this.adManagerAdView.setAppEventListener(this);
            this.adManagerAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if (isFluid()) {
                        top = 0;
                        left = 0;
                        width = getWidth();
                        height = getHeight();
                    } else {
                        top = adManagerAdView.getTop();
                        left = adManagerAdView.getLeft();
                        width = adManagerAdView.getAdSize().getWidthInPixels(getContext());
                        height = adManagerAdView.getAdSize().getHeightInPixels(getContext());
                    }

                    if (!isFluid()) {
                        sendOnSizeChangeEvent();
                    }

                    WritableMap ad = Arguments.createMap();
                    ad.putString("type", "banner");

                    WritableMap gadSize = Arguments.createMap();
                    gadSize.putString("adSize", adManagerAdView.getAdSize().toString());
                    gadSize.putDouble("width", adManagerAdView.getAdSize().getWidth());
                    gadSize.putDouble("height", adManagerAdView.getAdSize().getHeight());
                    ad.putMap("gadSize", gadSize);

                    ad.putString("isFluid", String.valueOf(isFluid()));

                    WritableMap measurements = Arguments.createMap();
                    measurements.putInt("adWidth", width);
                    measurements.putInt("adHeight", height);
                    measurements.putInt("width", getMeasuredWidth());
                    measurements.putInt("height", getMeasuredHeight());
                    measurements.putInt("left", left);
                    measurements.putInt("top", top);
                    ad.putMap("measurements", measurements);

                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LOADED, ad);
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
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
                }

                @Override
                public void onAdOpened() {
                    WritableMap event = Arguments.createMap();
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_OPENED, event);
                }

                @Override
                public void onAdClosed() {
                    WritableMap event = Arguments.createMap();
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_CLOSED, event);
                }

                @Override
                public void onAdImpression() {
                    WritableMap event = Arguments.createMap();
                    sendEvent(RNAdManagerBannerViewManager.EVENT_AD_RECORD_IMPRESSION, event);
                }

            });
            this.addView(this.adManagerAdView);
        } catch (Exception e) {
            sendErrorEvent("✅💪Error found at ad manager when createAdView(): " + e.getMessage() + "!");
            this.onException(e);
        }
    } // end of createAdView

    private void sendOnSizeChangeEvent() {
        try {
            int width;
            int height;
            WritableMap event = Arguments.createMap();
            AdSize adSize = this.adManagerAdView.getAdSize();
            width = adSize.getWidth();
            height = adSize.getHeight();
            event.putString("type", "banner");
            event.putDouble("width", width);
            event.putDouble("height", height);
            sendEvent(RNAdManagerBannerViewManager.EVENT_SIZE_CHANGE, event);
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when sendOnSizeChangeEvent(): " + e.getMessage() + "!");
        }
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        try {
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    getId(),
                    name,
                    event);
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when sendEvent(): " + e.getMessage() + "!");
        }
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
            this.adManagerAdView.setAdSizes(adSizesArray);

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
                RequestConfiguration requestConfiguration = new RequestConfiguration.Builder()
                        .setTestDeviceIds(testDevicesList)
                        .build();
                MobileAds.setRequestConfiguration(requestConfiguration);
            }

            if (correlator == null) {
                correlator = (String) Targeting.getCorelator(adUnitID);
            }
            Bundle bundle = new Bundle();
            bundle.putString("correlator", correlator);

            if (!servePersonalizedAds) {
                bundle.putInt("npa", 1);
            }

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
                // setLocation() became obsolete since GMA SDK version 21.0.0, link reference
                // below:
                // https://developers.google.com/admob/android/rel-notes
                // if (location != null) {
                // adRequestBuilder.setLocation(location);
                // }
            }

            AdManagerAdRequest adRequest = adRequestBuilder.build();
            this.adManagerAdView.loadAd(adRequest);
        } catch (Exception e) {
            sendErrorEvent("✅💪Error found at ad manager when loadBanner(): " + e.getMessage() + "!");
            this.onException(e);
        }
    } // End of loadBanner()

    // bubble up error to JS/TS level.
    public void sendErrorEvent(String errorMessage) {
        try {
            WritableMap params = Arguments.createMap();
            params.putString("error", errorMessage);
            currentRNcontext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("onError", params);
        } catch (Exception e) {
            // non-critical error, so ignore.
        }

    }

    public void setAdUnitID(String adUnitID) {
        try {
            if (this.adUnitID != null) {
                // We can only set adUnitID once, so when it was previously set we have
                // to recreate the view
                this.createAdView();
            }
            this.adUnitID = adUnitID;
            this.adManagerAdView.setAdUnitId(adUnitID);
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setAdUnitID(): " + e.getMessage() + "!");
        }
    }

    public void setTestDevices(String[] testDevices) {
        try {
            this.testDevices = testDevices;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setTestDevices(): " + e.getMessage() + "!");
        }

    }

    // Targeting
    public void setCustomTargeting(CustomTargeting[] customTargeting) {
        try {
            this.customTargeting = customTargeting;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setCustomTargeting(): " + e.getMessage() + "!");
        }
    }

    public void setCategoryExclusions(String[] categoryExclusions) {
        try {
            this.categoryExclusions = categoryExclusions;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setCategoryExclusions(): " + e.getMessage() + "!");
        }
    }

    public void setKeywords(String[] keywords) {
        try {
            this.keywords = keywords;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setKeywords(): " + e.getMessage() + "!");
        }
    }

    public void setContentURL(String content_url) {
        try {
            this.content_url = content_url;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setContentURL(): " + e.getMessage() + "!");
        }
    }

    public void setPublisherProvidedID(String publisherProvidedID) {
        try {
            this.publisherProvidedID = publisherProvidedID;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setPublisherProvidedID(): " + e.getMessage() + "!");
        }
    }

    public void setLocation(Location location) {
        try {
            this.location = location;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setLocation(): " + e.getMessage() + "!");
        }
    }

    public void setAdSize(AdSize adSize) {
        try {
            this.adSize = adSize;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setAdSize(): " + e.getMessage() + "!");
        }
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        try {
            this.validAdSizes = adSizes;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setValidAdSizes(): " + e.getMessage() + "!");
        }
    }

    public void setCorrelator(String correlator) {
        try {
            this.correlator = correlator;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setCorrelator(): " + e.getMessage() + "!");
        }
    }

    public void setServePersonalizedAds(Boolean servePersonalizedAds) {
        try {
            this.servePersonalizedAds = servePersonalizedAds;
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when setServePersonalizedAds(): " + e.getMessage() + "!");
        }
    }

    @Override
    public void onAppEvent(String name, String info) {
        try {
            WritableMap event = Arguments.createMap();
            event.putString("name", name);
            event.putString("info", info);
            sendEvent(RNAdManagerBannerViewManager.EVENT_APP_EVENT, event);
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when onAppEvent(): " + e.getMessage() + "!");
        }
    }

    @Override
    public void onHostResume() {
        try {
            if (this.adManagerAdView != null) {
                this.adManagerAdView.resume();
            }
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when onHostResume(): " + e.getMessage() + "!");
        }
    }

    @Override
    public void onHostPause() {
        try {
            if (this.adManagerAdView != null) {
                this.adManagerAdView.pause();
            }
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when onHostResume(): " + e.getMessage() + "!");
        }
    }

    @Override
    public void onHostDestroy() {
        try {
            if (this.adManagerAdView != null) {
                this.currentActivityContext = null;
                this.adManagerAdView.destroy();
            }
        } catch (Exception e) {
            sendErrorEvent("Error found at ad manager when onHostDestroy(): " + e.getMessage() + "!");
        }
    }

    private class MeasureAndLayoutRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (isFluid()) {
                    adManagerAdView.measure(
                            MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
                } else {
                    adManagerAdView.measure(width, height);
                }
                adManagerAdView.layout(left, top, left + width, top + height);
            } catch (Exception e) {
                sendErrorEvent(
                        "Error found at ad manager when MeasureAndLayoutRunnable::run(): " + e.getMessage() + "!");
            }
        }
    } // end of Runnable
}
