package com.matejdr.utils;

import android.location.Location;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.matejdr.customClasses.CustomTargeting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Targeting {
    public static CustomTargeting[] getCustomTargeting(ReadableMap customTargeting) {
        ArrayList<CustomTargeting> list = new ArrayList<CustomTargeting>();

        for (
                ReadableMapKeySetIterator it = customTargeting.keySetIterator();
                it.hasNextKey();
        ) {
            String key = it.nextKey();
            String value = null;

            ReadableType type = customTargeting.getType(key);
            switch (type) {
                case Null:
                    // skip null
//                    list.add(new CustomTargeting(key, null));
                    break;
                case Boolean:
                    list.add(new CustomTargeting(key, Boolean.toString(customTargeting.getBoolean(key))));
                    break;
                case Number:
                    list.add(new CustomTargeting(key, Double.toString(customTargeting.getDouble(key))));
                    break;
                case String:
                    list.add(new CustomTargeting(key, customTargeting.getString(key)));
                    break;
                case Map:
                    list.add(new CustomTargeting(key, fromObject(customTargeting.getMap(key))));
                    break;
                case Array:
                    list.add(new CustomTargeting(key, fromArray(customTargeting.getArray(key))));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
            }
        }

        CustomTargeting[] targetingList = list.toArray(new CustomTargeting[list.size()]);
        return targetingList;
    }

    public static Location getLocation(ReadableMap locationObject) {
        if (
                locationObject.hasKey("latitude")
                        && locationObject.hasKey("longitude")
                        && locationObject.hasKey("accuracy")
        ) {
            Location locationClass = new Location("");
            locationClass.setLatitude(locationObject.getDouble("latitude"));
            locationClass.setLongitude(locationObject.getDouble("longitude"));
            locationClass.setAccuracy((float) locationObject.getDouble("accuracy"));

            return locationClass;
        }

        return null;
    }

    private static List<String> fromObject(ReadableMap readableMap) {
        List<String> deconstructedList = new ArrayList<>();

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    deconstructedList.add(null);
                    break;
                case Boolean:
                    deconstructedList.add(Boolean.toString(readableMap.getBoolean(key)));
                    break;
                case Number:
                    deconstructedList.add(Double.toString(readableMap.getDouble(key)));
                    break;
                case String:
                    deconstructedList.add(readableMap.getString(key));
                    break;
                case Map:
                    // skip second level maps
//                    deconstructedList.add(fromObject(readableMap.getMap(key)));
                    break;
                case Array:
                    // skip second level arrays
//                    deconstructedList.add(fromArray(readableMap.getArray(key)));
                    break;
            }
        }

        return deconstructedList;
    }

    private static List<String> fromArray(ReadableArray readableArray) {
        List<String> deconstructedList = new ArrayList<>(readableArray.size());

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    deconstructedList.add(i, null);
                    break;
                case Boolean:
                    deconstructedList.add(i, Boolean.toString(readableArray.getBoolean(i)));
                    break;
                case Number:
                    deconstructedList.add(i, Double.toString(readableArray.getDouble(i)));
                    break;
                case String:
                    deconstructedList.add(i, readableArray.getString(i));
                    break;
                case Map:
                    // skip second level maps
//                    deconstructedList.add(i, fromObject(readableArray.getMap(i)));
                    break;
                case Array:
                    // skip second level arrays
//                    deconstructedList.add(i, fromArray(readableArray.getArray(i)));
                    break;
            }
        }

        return deconstructedList;
    }
}
