package com.matejdr.admanager;

import android.location.Location;
import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.ads.AdSize;
import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.enums.TargetingEnums;
import com.matejdr.admanager.enums.TargetingEnums.TargetingTypes;
import com.matejdr.admanager.utils.AdSizeUtil;
import com.matejdr.admanager.utils.Targeting;

import java.util.ArrayList;
import java.util.Map;

public class RNAdManagerBannerViewManager extends ViewGroupManager<BannerAdView> {

    public static final String REACT_CLASS = "CTKBannerView";

    public static final String PROP_AD_SIZE = "adSize";
    public static final String PROP_VALID_AD_SIZES = "validAdSizes";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICES = "testDevices";
    public static final String PROP_TARGETING = "targeting";
    public static final String PROP_CORRELATOR = "correlator";

    public static final String EVENT_SIZE_CHANGE = "onSizeChange";
    public static final String EVENT_AD_LOADED = "onAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "onAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "onAdOpened";
    public static final String EVENT_AD_CLOSED = "onAdClosed";
    public static final String EVENT_APP_EVENT = "onAppEvent";

    public static final int COMMAND_LOAD_BANNER = 1;
    private final ReactApplicationContext applicationContext;

    public RNAdManagerBannerViewManager(ReactApplicationContext context) {
        super();
        this.applicationContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public void onDropViewInstance(BannerAdView view) {
        if (view.adManagerAdView != null) {
            view.adManagerAdView.setAppEventListener(null);
            view.adManagerAdView.setAdListener(null);
            view.adManagerAdView.destroy();
        }
        super.onDropViewInstance(view);
    }

    @Override
    protected BannerAdView createViewInstance(ThemedReactContext themedReactContext) {
        BannerAdView adView = new BannerAdView(themedReactContext, applicationContext);
        return adView;
    }

    @Override
    public void addView(BannerAdView parent, View child, int index) {
        throw new RuntimeException("BannerAdView cannot have subviews");
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        String[] events = {
            EVENT_SIZE_CHANGE,
            EVENT_AD_LOADED,
            EVENT_AD_FAILED_TO_LOAD,
            EVENT_AD_OPENED,
            EVENT_AD_CLOSED,
            EVENT_APP_EVENT
        };
        for (int i = 0; i < events.length; i++) {
            builder.put(events[i], MapBuilder.of("registrationName", events[i]));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_AD_SIZE)
    public void setPropAdSize(final BannerAdView view, final String sizeString) {
        AdSize adSize = AdSizeUtil.getAdSizeFromString(sizeString);
        view.setAdSize(adSize);
    }

    @ReactProp(name = PROP_VALID_AD_SIZES)
    public void setPropValidAdSizes(final BannerAdView view, final ReadableArray adSizeStrings) {
        ReadableNativeArray nativeArray = (ReadableNativeArray) adSizeStrings;
        ArrayList<Object> list = nativeArray.toArrayList();
        String[] adSizeStringsArray = list.toArray(new String[list.size()]);
        AdSize[] adSizes = new AdSize[list.size()];

        for (int i = 0; i < adSizeStringsArray.length; i++) {
            String adSizeString = adSizeStringsArray[i];
            adSizes[i] = AdSizeUtil.getAdSizeFromString(adSizeString);
        }
        view.setValidAdSizes(adSizes);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setPropAdUnitID(final BannerAdView view, final String adUnitID) {
        view.setAdUnitID(adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICES)
    public void setPropTestDevices(final BannerAdView view, final ReadableArray testDevices) {
        ReadableNativeArray nativeArray = (ReadableNativeArray) testDevices;
        ArrayList<Object> list = nativeArray.toArrayList();
        view.setTestDevices(list.toArray(new String[list.size()]));
    }

    @ReactProp(name = PROP_TARGETING)
    public void setPropTargeting(final BannerAdView view, final ReadableMap targetingObjects) {

        ReadableMapKeySetIterator targetings = targetingObjects.keySetIterator();

        if (targetings.hasNextKey()) {
            for (
                ReadableMapKeySetIterator it = targetingObjects.keySetIterator();
                it.hasNextKey();
            ) {
                String targetingType = it.nextKey();

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CUSTOMTARGETING))) {
                    view.hasTargeting = true;
                    ReadableMap customTargetingObject = targetingObjects.getMap(targetingType);
                    CustomTargeting[] customTargetingArray = Targeting.getCustomTargeting(customTargetingObject);
                    view.setCustomTargeting(customTargetingArray);
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CATEGORYEXCLUSIONS))) {
                    view.hasTargeting = true;
                    ReadableArray categoryExclusionsArray = targetingObjects.getArray(targetingType);
                    ReadableNativeArray nativeArray = (ReadableNativeArray) categoryExclusionsArray;
                    ArrayList<Object> list = nativeArray.toArrayList();
                    view.setCategoryExclusions(list.toArray(new String[list.size()]));
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.KEYWORDS))) {
                    view.hasTargeting = true;
                    ReadableArray keywords = targetingObjects.getArray(targetingType);
                    ReadableNativeArray nativeArray = (ReadableNativeArray) keywords;
                    ArrayList<Object> list = nativeArray.toArrayList();
                    view.setKeywords(list.toArray(new String[list.size()]));
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.CONTENTURL))) {
                    view.hasTargeting = true;
                    String content_url = targetingObjects.getString(targetingType);
                    view.setContentURL(content_url);
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.PUBLISHERPROVIDEDID))) {
                    view.hasTargeting = true;
                    String publisherProvidedID = targetingObjects.getString(targetingType);
                    view.setPublisherProvidedID(publisherProvidedID);
                }

                if (targetingType.equals(TargetingEnums.getEnumString(TargetingTypes.LOCATION))) {
                    view.hasTargeting = true;
                    ReadableMap locationObject = targetingObjects.getMap(targetingType);
                    Location location = Targeting.getLocation(locationObject);
                    view.setLocation(location);
                }
            }
        }
    }

    @ReactProp(name = PROP_CORRELATOR)
    public void setCorrelator(final BannerAdView view, final String correlator) {
        view.setCorrelator(correlator);
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("loadBanner", COMMAND_LOAD_BANNER);
    }

    @Override
    public void receiveCommand(BannerAdView root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_LOAD_BANNER:
                root.loadBanner();
                break;
        }
    }
}
