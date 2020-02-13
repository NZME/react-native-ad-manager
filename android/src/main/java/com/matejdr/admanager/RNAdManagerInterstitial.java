package com.matejdr.admanager;

import android.os.Handler;
import android.os.Looper;
import android.location.Location;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.enums.TargetingEnums;
import com.matejdr.admanager.enums.TargetingEnums.TargetingTypes;
import com.matejdr.admanager.utils.Targeting;

public class RNAdManagerInterstitial extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "CTKInterstitial";

    public static final String EVENT_AD_LOADED = "interstitialAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "interstitialAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "interstitialAdOpened";
    public static final String EVENT_AD_CLOSED = "interstitialAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "interstitialAdLeftApplication";

    PublisherInterstitialAd mInterstitialAd;
    String[] testDevices;
    ReadableMap targeting;

    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String contentURL;
    String publisherProvidedID;
    Location location;

    private Promise mRequestAdPromise;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public RNAdManagerInterstitial(ReactApplicationContext reactContext) {
        super(reactContext);
        mInterstitialAd = new PublisherInterstitialAd(reactContext);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        sendEvent(EVENT_AD_CLOSED, null);
                    }
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        String errorString = "ERROR_UNKNOWN";
                        String errorMessage = "Unknown error";
                        switch (errorCode) {
                            case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                                errorString = "ERROR_CODE_INTERNAL_ERROR";
                                errorMessage = "Internal error, an invalid response was received from the ad server.";
                                break;
                            case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                                errorString = "ERROR_CODE_INVALID_REQUEST";
                                errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                                break;
                            case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                                errorString = "ERROR_CODE_NETWORK_ERROR";
                                errorMessage = "The ad request was unsuccessful due to network connectivity.";
                                break;
                            case PublisherAdRequest.ERROR_CODE_NO_FILL:
                                errorString = "ERROR_CODE_NO_FILL";
                                errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                                break;
                        }
                        WritableMap event = Arguments.createMap();
                        WritableMap error = Arguments.createMap();
                        event.putString("message", errorMessage);
                        sendEvent(EVENT_AD_FAILED_TO_LOAD, event);
                        if (mRequestAdPromise != null) {
                            mRequestAdPromise.reject(errorString, errorMessage);
                            mRequestAdPromise = null;
                        }
                    }
                    @Override
                    public void onAdLeftApplication() {
                        sendEvent(EVENT_AD_LEFT_APPLICATION, null);
                    }
                    @Override
                    public void onAdLoaded() {
                        sendEvent(EVENT_AD_LOADED, null);
                        if (mRequestAdPromise != null) {
                          mRequestAdPromise.resolve(null);
                          mRequestAdPromise = null;
                        }
                    }
                    @Override
                    public void onAdOpened() {
                        sendEvent(EVENT_AD_OPENED, null);
                    }
                });
            }
        });
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void setAdUnitID(String adUnitID) {
        if (mInterstitialAd.getAdUnitId() == null) {
            mInterstitialAd.setAdUnitId(adUnitID);
        }
    }

    @ReactMethod
    public void setTestDevices(ReadableArray testDevices) {
        ReadableNativeArray nativeArray = (ReadableNativeArray)testDevices;
        ArrayList<Object> list = nativeArray.toArrayList();
        this.testDevices = list.toArray(new String[list.size()]);
    }

    @ReactMethod
    public void setTargeting(ReadableMap targetingObjects) {
        this.targeting = targetingObjects;

        ReadableMapKeySetIterator targetings = targetingObjects.keySetIterator();

        if (targetings.hasNextKey()) {
            for (
                ReadableMapKeySetIterator it = targetingObjects.keySetIterator();
                it.hasNextKey();
            ) {
                String targetingType = it.nextKey();

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CUSTOMTARGETING))) {
                    ReadableMap customTargetingObject = targetingObjects.getMap(targetingType);
                    CustomTargeting[] customTargetingArray = Targeting.getCustomTargeting(customTargetingObject);
                    this.customTargeting = customTargetingArray;
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CATEGORYEXCLUSIONS))) {
                    ReadableArray categoryExclusionsArray = targetingObjects.getArray(targetingType);
                    ReadableNativeArray nativeArray = (ReadableNativeArray)categoryExclusionsArray;
                    ArrayList<Object> list = nativeArray.toArrayList();
                    this.categoryExclusions = list.toArray(new String[list.size()]);
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.KEYWORDS))) {
                    ReadableArray keywords = targetingObjects.getArray(targetingType);
                    ReadableNativeArray nativeArray = (ReadableNativeArray)keywords;
                    ArrayList<Object> list = nativeArray.toArrayList();
                    this.keywords = list.toArray(new String[list.size()]);
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CONTENTURL))) {
                    String contentURL = targetingObjects.getString(targetingType);
                    this.contentURL = contentURL;
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.PUBLISHERPROVIDEDID))) {
                    String publisherProvidedID = targetingObjects.getString(targetingType);
                    this.publisherProvidedID = publisherProvidedID;
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.LOCATION))) {
                    ReadableMap locationObject = targetingObjects.getMap(targetingType);
                    Location location = Targeting.getLocation(locationObject);
                    this.location = location;
                }
            }
        }

    }

    @ReactMethod
    public void requestAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                if (mInterstitialAd.isLoaded() || mInterstitialAd.isLoading()) {
                    promise.reject("E_AD_ALREADY_LOADED", "Ad is already loaded.");
                } else {
                    mRequestAdPromise = promise;
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
                        for (int i =0; i < categoryExclusions.length; i++) {
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

                    PublisherAdRequest adRequest = adRequestBuilder.build();
                    mInterstitialAd.loadAd(adRequest);
                }
            }
        });
    }

    @ReactMethod
    public void showAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    promise.resolve(null);
                } else {
                    promise.reject("E_AD_NOT_READY", "Ad is not ready.");
                }
            }
        });
    }

    @ReactMethod
    public void isReady(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                callback.invoke(mInterstitialAd.isLoaded());
            }
        });
    }
}
