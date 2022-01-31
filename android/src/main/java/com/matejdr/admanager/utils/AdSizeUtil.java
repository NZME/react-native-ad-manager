package com.matejdr.admanager.utils;

import com.google.android.gms.ads.AdSize;

public class AdSizeUtil {
    public static AdSize getAdSizeFromString(String adSize) {
        if (adSize.contains("x")) {
            String[] splitAdSizes = adSize.split("x");
            if (splitAdSizes.length == 2) {
                return new AdSize(Integer.parseInt(splitAdSizes[0]), Integer.parseInt(splitAdSizes[1]));
            }
        }

        switch (adSize) {
            case "fullBanner":
                return AdSize.FULL_BANNER;
            case "largeBanner":
                return AdSize.LARGE_BANNER;
            case "fluid":
                return AdSize.FLUID;
            case "skyscraper":
                return AdSize.WIDE_SKYSCRAPER;
            case "leaderBoard":
                return AdSize.LEADERBOARD;
            case "mediumRectangle":
                return AdSize.MEDIUM_RECTANGLE;
            case "banner":
            default:
                return AdSize.BANNER;
        }
    }
}
