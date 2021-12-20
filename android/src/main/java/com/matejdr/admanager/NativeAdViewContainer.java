package com.matejdr.admanager;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookExtras;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AppEventListener;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd.OnCustomFormatAdLoadedListener;
import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.utils.Targeting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativeAdViewContainer extends ReactViewGroup implements AppEventListener,
        LifecycleEventListener, OnNativeAdLoadedListener,
        OnAdManagerAdViewLoadedListener, OnCustomFormatAdLoadedListener {
    public static final String AD_TYPE_BANNER = "banner";
    public static final String AD_TYPE_NATIVE = "native";
    public static final String AD_TYPE_TEMPLATE = "template";

    protected AdLoader adLoader;
    protected ReactApplicationContext applicationContext;
    protected NativeAdView nativeAdView;
    protected AdManagerAdView publisherAdView;
    protected NativeCustomFormatAd nativeCustomTemplateAd;
    protected String nativeCustomTemplateAdClickableAsset;
    protected ThemedReactContext context;

    String[] testDevices;
    String adUnitID;
    AdSize[] validAdSizes;
    AdSize adSize;
    String[] customTemplateIds;
    String[] validAdTypes = new String[]{AD_TYPE_BANNER, AD_TYPE_NATIVE, AD_TYPE_TEMPLATE};

    // Targeting
    Boolean hasTargeting = false;
    CustomTargeting[] customTargeting;
    String[] categoryExclusions;
    String[] keywords;
    String contentURL;
    String publisherProvidedID;
    Location location;
    String correlator;
    List<String> customClickTemplateIds;

    /**
     * @{RCTEventEmitter} instance used for sending events back to JS
     **/
    private final RCTEventEmitter mEventEmitter;

    /**
     * Creates new NativeAdView instance and retrieves event emitter
     *
     * @param context
     */
    public NativeAdViewContainer(ThemedReactContext context, ReactApplicationContext applicationContext) {
        super(context);
        this.context = context;
        this.applicationContext = applicationContext;
        this.applicationContext.addLifecycleEventListener(this);

        this.nativeAdView = new NativeAdView(context);
        this.publisherAdView = new AdManagerAdView(context);

        mEventEmitter = context.getJSModule(RCTEventEmitter.class);
    }

    public void loadAd(RNAdManageNativeManager.AdsManagerProperties adsManagerProperties) {
        this.testDevices = adsManagerProperties.getTestDevices();
        this.adUnitID = adsManagerProperties.getAdUnitID();
    }

    private void setupAdLoader() {
        if (adLoader != null) {
            return;
        }

        final ReactApplicationContext reactContext = this.applicationContext;

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                .build();

        ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
        if (adSize != null) {
            adSizes.add(adSize);
        }
        if (validAdSizes != null) {
            for (int i = 0; i < validAdSizes.length; i++) {
                if (!adSizes.contains(validAdSizes[i])) {
                    adSizes.add(validAdSizes[i]);
                }
            }
        }

        if (adSizes.size() == 0) {
            adSizes.add(AdSize.BANNER);
        }

        AdSize[] adSizesArray = adSizes.toArray(new AdSize[adSizes.size()]);

        List<String> validAdTypesList = Arrays.asList(validAdTypes);

        Log.e("validAdTypes", validAdTypesList.toString());
        AdLoader.Builder builder = new AdLoader.Builder(reactContext, adUnitID);
        if (validAdTypesList.contains(AD_TYPE_NATIVE)) {
            Log.e("validAdTypes", AD_TYPE_NATIVE);
            builder.forNativeAd(NativeAdViewContainer.this);
        }
        if (adSizesArray.length > 0 && validAdTypesList.contains(AD_TYPE_BANNER)) {
            Log.e("validAdTypes", AD_TYPE_BANNER);
            builder.forAdManagerAdView(NativeAdViewContainer.this, adSizesArray);
        }
        if (customTemplateIds != null && customTemplateIds.length > 0 && validAdTypesList.contains(AD_TYPE_TEMPLATE)) {
            Log.e("validAdTypes", AD_TYPE_TEMPLATE);
            for (int i = 0; i < customTemplateIds.length; i++) {
                String curCustomTemplateID = customTemplateIds[i];
                if (!curCustomTemplateID.isEmpty()) {
                    if (customClickTemplateIds != null && customClickTemplateIds.contains(curCustomTemplateID)) {
                        builder.forCustomFormatAd(curCustomTemplateID,
                                NativeAdViewContainer.this,
                                new NativeCustomFormatAd.OnCustomClickListener() {
                                    @Override
                                    public void onCustomClick(NativeCustomFormatAd ad, String assetName) {
                                        WritableMap customClick = Arguments.createMap();
                                        customClick.putString("assetName", assetName);
                                        for (String adAssetName : ad.getAvailableAssetNames()) {
                                            if (ad.getText(adAssetName) != null) {
                                                customClick.putString(adAssetName, ad.getText(adAssetName).toString());
                                            }
                                        }
                                        sendEvent(RNAdManagerNativeViewManager.EVENT_AD_CUSTOM_CLICK, customClick);
                                    }
                                });
                    } else {
                        builder.forCustomFormatAd(curCustomTemplateID, NativeAdViewContainer.this, null);
                    }
                }
            }
        }
        builder.withAdListener(new AdListener() {
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
                sendEvent(RNAdManagerNativeViewManager.EVENT_AD_FAILED_TO_LOAD, event);
            }

            @Override
            public void onAdLoaded() {
                // sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, null);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                sendEvent(RNAdManagerNativeViewManager.EVENT_AD_CLICKED, null);
            }

            @Override
            public void onAdOpened() {
                sendEvent(RNAdManagerNativeViewManager.EVENT_AD_OPENED, null);
            }

            @Override
            public void onAdClosed() {
                sendEvent(RNAdManagerNativeViewManager.EVENT_AD_CLOSED, null);
            }
        }).withNativeAdOptions(adOptions);

        adLoader = builder.build();
    }

    public void reloadAd() {
        this.setupAdLoader();

        if (adLoader != null) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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

                    Bundle fbExtras = new FacebookExtras()
                            .setNativeBanner(true)
                            .build();

                    adRequestBuilder.addNetworkExtrasBundle(FacebookAdapter.class, fbExtras);

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
                    if (adLoader != null) {
                        adLoader.loadAd(adRequest);
                    }
                }
            });
        }
    }

    public void registerViewsForInteraction(List<View> clickableViews) {
        if (nativeCustomTemplateAd != null && nativeCustomTemplateAdClickableAsset != null) {
            try {
                for (View view : clickableViews) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nativeCustomTemplateAd.performClick(nativeCustomTemplateAdClickableAsset);
                        }
                    });
                }
            } catch (Exception e) {
            }
        } else if (nativeAdView != null) {
            int viewWidth = this.getMeasuredWidth();
            int viewHeight = this.getMeasuredHeight();

            int left = 0;
            int top = 0;

            if (viewHeight <= 0) {
                viewHeight = 1500;
            }

            nativeAdView.getLayoutParams().width = viewWidth;
            nativeAdView.getLayoutParams().height = viewHeight;

            nativeAdView.measure(viewWidth, viewHeight);
            nativeAdView.layout(left, top, left + viewWidth, top + viewHeight);

            View tmpView = new View(context);
            tmpView.layout(left, top, left + viewWidth, top + viewHeight);
            nativeAdView.addView(tmpView);

            tmpView.getLayoutParams().width = viewWidth;
            tmpView.getLayoutParams().height = viewHeight;

            nativeAdView.setCallToActionView(tmpView);
        }
    }

    @Override
    public void onNativeAdLoaded(NativeAd nativeAd) {
        nativeAdView.setNativeAd(nativeAd);
        removeAllViews();
        addView(nativeAdView);

        setNativeAd(nativeAd);
    }

    @Override
    public void onAdManagerAdViewLoaded(AdManagerAdView adView) {
        this.publisherAdView = adView;
        removeAllViews();
        this.addView(adView);
        if (adView == null) {
            sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, null);
            return;
        }
        int width = adView.getAdSize().getWidthInPixels(context);
        int height = adView.getAdSize().getHeightInPixels(context);
        int left = adView.getLeft();
        int top = adView.getTop();
        adView.measure(width, height);
        adView.layout(left, top, left + width, top + height);
        sendOnSizeChangeEvent(adView);
        WritableMap ad = Arguments.createMap();
        ad.putString("type", AD_TYPE_BANNER);

        WritableMap gadSize = Arguments.createMap();
        gadSize.putDouble("width", adView.getAdSize().getWidth());
        gadSize.putDouble("height", adView.getAdSize().getHeight());

        ad.putMap("gadSize", gadSize);

        sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, ad);
    }

    @Override
    public void onCustomFormatAdLoaded(NativeCustomFormatAd nativeCustomTemplateAd) {
        this.nativeCustomTemplateAd = nativeCustomTemplateAd;
        removeAllViews();

        setNativeAd(nativeCustomTemplateAd);
    }

    /**
     * Called by the view manager when ad is loaded. Sends serialised
     * version of a native ad back to Javascript.
     *
     * @param nativeCustomTemplateAd
     */
    private void setNativeAd(NativeCustomFormatAd nativeCustomTemplateAd) {
        if (nativeCustomTemplateAd == null) {
            sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, null);
            return;
        }

        WritableMap ad = Arguments.createMap();
        ad.putString("type", AD_TYPE_TEMPLATE);
        ad.putString("templateID", nativeCustomTemplateAd.getCustomFormatId());
        for (String assetName : nativeCustomTemplateAd.getAvailableAssetNames()) {
            if (nativeCustomTemplateAd.getText(assetName) != null) {
                if (nativeCustomTemplateAdClickableAsset == null && nativeCustomTemplateAd.getText(assetName).length() > 0) {
                    nativeCustomTemplateAdClickableAsset = assetName;
                }
                ad.putString(assetName, nativeCustomTemplateAd.getText(assetName).toString());
            } else if (nativeCustomTemplateAd.getImage(assetName) != null) {
                WritableMap imageMap = Arguments.createMap();
                imageMap.putString("uri", nativeCustomTemplateAd.getImage(assetName).getUri().toString());
                imageMap.putInt("width", nativeCustomTemplateAd.getImage(assetName).getDrawable().getIntrinsicWidth());
                imageMap.putInt("height", nativeCustomTemplateAd.getImage(assetName).getDrawable().getIntrinsicHeight());
                imageMap.putDouble("scale", nativeCustomTemplateAd.getImage(assetName).getScale());
                ad.putMap(assetName, imageMap);
            }
        }

        sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, ad);

        nativeCustomTemplateAd.recordImpression();
    }

    private void setNativeAd(NativeAd nativeAd) {
        if (nativeAd == null) {
            sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, null);
            return;
        }

        WritableMap ad = Arguments.createMap();
        ad.putString("type", AD_TYPE_NATIVE);
        if (nativeAd.getHeadline() == null) {
            ad.putString("headline", null);
        } else {
            ad.putString("headline", nativeAd.getHeadline());
        }

        if (nativeAd.getBody() == null) {
            ad.putString("bodyText", null);
        } else {
            ad.putString("bodyText", nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            ad.putString("callToActionText", null);
        } else {
            ad.putString("callToActionText", nativeAd.getCallToAction());
        }

        if (nativeAd.getAdvertiser() == null) {
            ad.putString("advertiserName", null);
        } else {
            ad.putString("advertiserName", nativeAd.getAdvertiser());
        }

        if (nativeAd.getStarRating() == null) {
            ad.putString("starRating", null);
        } else {
            ad.putDouble("starRating", nativeAd.getStarRating());
        }

        if (nativeAd.getStore() == null) {
            ad.putString("storeName", null);
        } else {
            ad.putString("storeName", nativeAd.getStore());
        }

        if (nativeAd.getPrice() == null) {
            ad.putString("price", null);
        } else {
            ad.putString("price", nativeAd.getPrice());
        }

        if (nativeAd.getIcon() == null) {
            ad.putString("icon", null);
        } else {
            WritableMap icon = Arguments.createMap();
            icon.putString("uri", nativeAd.getIcon().getUri().toString());
            icon.putInt("width", nativeAd.getIcon().getDrawable().getIntrinsicWidth());
            icon.putInt("height", nativeAd.getIcon().getDrawable().getIntrinsicHeight());
            icon.putDouble("scale", nativeAd.getIcon().getScale());
            ad.putMap("icon", icon);
        }

        if (nativeAd.getImages().size() == 0) {
            ad.putArray("images", null);
        } else {
            WritableArray images = Arguments.createArray();
            for (NativeAd.Image image : nativeAd.getImages()) {
                WritableMap imageMap = Arguments.createMap();
                imageMap.putString("uri", image.getUri().toString());
                imageMap.putDouble("scale", image.getScale());
                images.pushMap(imageMap);
            }
            ad.putArray("images", images);
        }

        Bundle extras = nativeAd.getExtras();
        if (extras.containsKey(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET)) {
            String socialContext = (String) extras.get(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET);
            ad.putString("socialContext", socialContext);
        }

        sendEvent(RNAdManagerNativeViewManager.EVENT_AD_LOADED, ad);
    }


    private void sendOnSizeChangeEvent(AdManagerAdView adView) {
        int width;
        int height;
        ReactContext reactContext = (ReactContext) getContext();
        WritableMap event = Arguments.createMap();
        AdSize adSize = adView.getAdSize();
        width = adSize.getWidth();
        height = adSize.getHeight();
        event.putString("type", "banner");
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(RNAdManagerNativeViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        mEventEmitter.receiveEvent(getId(), name, event);
    }

    public void setCustomTemplateIds(String[] customTemplateIds) {
        this.customTemplateIds = customTemplateIds;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        this.validAdSizes = adSizes;
    }

    public void setValidAdTypes(String[] adTypes) {
        Log.e("validAdTypes_s", adTypes.toString());
        this.validAdTypes = adTypes;
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

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    public void setCustomClickTemplateIds(String[] customClickTemplateIds) {
        this.customClickTemplateIds = Arrays.asList(customClickTemplateIds);
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNAdManagerNativeViewManager.EVENT_APP_EVENT, event);
    }

    @Override
    public void onHostResume() {
        if (this.publisherAdView != null) {
            this.publisherAdView.resume();
        }
    }

    @Override
    public void onHostPause() {
        if (this.publisherAdView != null) {
            this.publisherAdView.pause();
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.nativeAdView != null) {
            this.nativeAdView.destroy();
        }
        if (this.publisherAdView != null) {
            this.publisherAdView.destroy();
        }
        if (this.nativeCustomTemplateAd != null) {
            this.nativeCustomTemplateAd.destroy();
        }
        if (this.nativeCustomTemplateAd != null) {
            this.nativeCustomTemplateAd.destroy();
        }
        if (this.adLoader != null) {
            this.adLoader = null;
        }
    }
}
