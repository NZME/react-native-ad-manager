package com.matejdr.admanager;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;

import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.utils.Targeting;

class BannerAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {

    protected PublisherAdView adView;

    String[] testDevices;
    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;

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
        applicationContext.addLifecycleEventListener(this);
        this.createAdView();
    }

    private void createAdView() {
        if (this.adView != null) this.adView.destroy();

        final Context context = getContext();
        this.adView = new PublisherAdView(context);
        this.adView.setAppEventListener(this);
        this.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                int width = adView.getAdSize().getWidthInPixels(context);
                int height = adView.getAdSize().getHeightInPixels(context);
                int left = adView.getLeft();
                int top = adView.getTop();
                adView.measure(width, height);
                adView.layout(left, top, left + width, top + height);
                sendOnSizeChangeEvent();
                WritableMap ad = Arguments.createMap();
                ad.putString("type", "banner");

                WritableMap gadSize = Arguments.createMap();
                gadSize.putDouble("width", adView.getAdSize().getWidth());
                gadSize.putDouble("height", adView.getAdSize().getHeight());

                ad.putMap("gadSize", gadSize);

                //ad.putString("gadSize", adView.getAdSize().toString());
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LOADED, ad);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                String errorMessage = "Unknown error";
                switch (errorCode) {
                    case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        errorMessage = "Internal error, an invalid response was received from the ad server.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                        errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                        errorMessage = "The ad request was unsuccessful due to network connectivity.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NO_FILL:
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
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_OPENED, null);
            }

            @Override
            public void onAdClosed() {
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_CLOSED, null);
            }

            @Override
            public void onAdLeftApplication() {
                sendEvent(RNAdManagerBannerViewManager.EVENT_AD_LEFT_APPLICATION, null);
            }
        });
        this.addView(this.adView);
    }

    private void sendOnSizeChangeEvent() {
        int width;
        int height;
        ReactContext reactContext = (ReactContext) getContext();
        WritableMap event = Arguments.createMap();
        AdSize adSize = this.adView.getAdSize();
        if (adSize == AdSize.SMART_BANNER) {
            width = (int) PixelUtil.toDIPFromPixel(adSize.getWidthInPixels(reactContext));
            height = (int) PixelUtil.toDIPFromPixel(adSize.getHeightInPixels(reactContext));
        } else {
            width = adSize.getWidth();
            height = adSize.getHeight();
        }
        event.putString("type", "banner");
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(RNAdManagerBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event);
    }

    public void loadBanner() {
        ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
        if (this.adSize != null) {
            adSizes.add(this.adSize);
        }
        if (this.validAdSizes != null) {
            for (int i = 0; i < this.validAdSizes.length; i++) {
                adSizes.add(this.validAdSizes[i]);
            }
        }

        if (adSizes.size() == 0) {
            adSizes.add(AdSize.BANNER);
        }

        AdSize[] adSizesArray = adSizes.toArray(new AdSize[adSizes.size()]);
        this.adView.setAdSizes(adSizesArray);

        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
        if (testDevices != null) {
            for (int i = 0; i < testDevices.length; i++) {
                String testDevice = testDevices[i];
                if (testDevice == "SIMULATOR") {
                    testDevice = PublisherAdRequest.DEVICE_ID_EMULATOR;
                }
                adRequestBuilder.addTestDevice(testDevice);
            }
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

        PublisherAdRequest adRequest = adRequestBuilder.build();
        this.adView.loadAd(adRequest);
    }

    public void setAdUnitID(String adUnitID) {
        if (this.adUnitID != null) {
            // We can only set adUnitID once, so when it was previously set we have
            // to recreate the view
            this.createAdView();
        }
        this.adUnitID = adUnitID;
        this.adView.setAdUnitId(adUnitID);
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

    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    public void setPublisherProvidedID(String publisherProvidedID) {
        this.publisherProvidedID = publisherProvidedID;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        this.validAdSizes = adSizes;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNAdManagerBannerViewManager.EVENT_APP_EVENT, event);
    }

    @Override
    public void onHostResume() {
        if (this.adView != null) {
            this.adView.resume();
        }
    }

    @Override
    public void onHostPause() {
        if (this.adView != null) {
            this.adView.pause();
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.adView != null) {
            this.adView.destroy();
        }
    }
}
