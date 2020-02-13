package com.matejdr.admanager.enums;

public class TargetingEnums {

    public enum TargetingTypes {
        CUSTOMTARGETING,
        CATEGORYEXCLUSIONS,
        KEYWORDS,
        CONTENTURL,
        PUBLISHERPROVIDEDID,
        LOCATION
    }

    public static String getEnumString(TargetingTypes targetingType) {
        switch (targetingType) {
            case CUSTOMTARGETING:
                return "customTargeting";
            case CATEGORYEXCLUSIONS:
                return "categoryExclusions";
            case KEYWORDS:
                return "keywords";
            case CONTENTURL:
                return "contentURL";
            case PUBLISHERPROVIDEDID:
                return "publisherProvidedID";
            case LOCATION:
                return "location";
            default:
                return "";
        }
    }
}
