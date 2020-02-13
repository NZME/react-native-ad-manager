package com.matejdr.admanager.customClasses;

import java.util.List;

public class CustomTargeting {
    public String key;
    public String value;
    public List<String> values;

    public CustomTargeting(String k, String v) {
        key = k;
        value = v;
    }

    public CustomTargeting(String k, List<String> v) {
        key = k;
        values = v;
    }
}
