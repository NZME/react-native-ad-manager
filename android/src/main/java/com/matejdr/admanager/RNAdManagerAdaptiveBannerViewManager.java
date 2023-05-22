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
import com.matejdr.admanager.customClasses.CustomTargeting;
import com.matejdr.admanager.enums.TargetingEnums;
import com.matejdr.admanager.enums.TargetingEnums.TargetingTypes;
import com.matejdr.admanager.utils.Targeting;

import java.util.ArrayList;
import java.util.Map;

public class RNAdManagerAdaptiveBannerViewManager extends ViewGroupManager<AdaptiveBannerAdView> {

    public static final String REACT_CLASS = "CTKAdaptiveBannerView";

    public static final String PROP_AD_POSITION = "adPosition";
    public static final String PROP_AD_MAX_HEIGHT = "maxHeight";
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

    public RNAdManagerAdaptiveBannerViewManager(ReactApplicationContext context) {
        super();
        this.applicationContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public void onDropViewInstance(AdaptiveBannerAdView view) {
        if (view.adManagerAdView != null) {
            view.adManagerAdView.setAppEventListener(null);
            view.adManagerAdView.setAdListener(null);
            view.adManagerAdView.destroy();
        }
        super.onDropViewInstance(view);
    }

    @Override
    protected AdaptiveBannerAdView createViewInstance(ThemedReactContext themedReactContext) {
        AdaptiveBannerAdView adView = new AdaptiveBannerAdView(themedReactContext, applicationContext);
        return adView;
    }

    @Override
    public void addView(AdaptiveBannerAdView parent, View child, int index) {
        throw new RuntimeException("AdaptiveBannerAdView cannot have subviews");
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
        for (String event : events) {
            builder.put(event, MapBuilder.of("registrationName", event));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_AD_POSITION)
    public void setPropAdPosition(final AdaptiveBannerAdView view, final String positionString) {
        view.setAdPosition(positionString);
    }

    @ReactProp(name = PROP_AD_MAX_HEIGHT)
    public void setPropMaxHeight(final AdaptiveBannerAdView view, final Integer maxHeightInteger) {
        view.setMaxHeight(maxHeightInteger);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setPropAdUnitID(final AdaptiveBannerAdView view, final String adUnitID) {
        view.setAdUnitID(adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICES)
    public void setPropTestDevices(final AdaptiveBannerAdView view, final ReadableArray testDevices) {
        ReadableNativeArray nativeArray = (ReadableNativeArray) testDevices;
        ArrayList<Object> list = nativeArray.toArrayList();
        view.setTestDevices(list.toArray(new String[list.size()]));
    }

    @ReactProp(name = PROP_TARGETING)
    public void setPropTargeting(final AdaptiveBannerAdView view, final ReadableMap targetingObjects) {

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
    public void setCorrelator(final AdaptiveBannerAdView view, final String correlator) {
        view.setCorrelator(correlator);
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("loadBanner", COMMAND_LOAD_BANNER);
    }

    @Override
    public void receiveCommand(AdaptiveBannerAdView root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_LOAD_BANNER:
                root.loadBanner();
                break;
        }
    }
}
