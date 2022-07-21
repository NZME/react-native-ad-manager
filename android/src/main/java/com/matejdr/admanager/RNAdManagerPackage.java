package com.matejdr.admanager;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.List;

public class RNAdManagerPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdManageNativeManager(reactContext),
            new RNAdManagerInterstitial(reactContext)
        );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdManagerNativeViewManager(reactContext),
            new RNAdManagerAdaptiveBannerViewManager(reactContext),
            new RNAdManagerBannerViewManager(reactContext)
        );
    }
}
