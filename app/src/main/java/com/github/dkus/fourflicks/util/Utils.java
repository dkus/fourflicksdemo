package com.github.dkus.fourflicks.util;


public class Utils {

    public static String formatLocation(double latitude, double longitude) {

        return String.valueOf(latitude)+","+String.valueOf(longitude);

    }

    public static <T> T toogle(T current, T first, T second) {

        if (current==null && first==null) return second;

        if (current==null) return first;

        return current.equals(first) ? second : first;

    }

    public static <T> boolean areEquals(T first, T second) {

        return first!=null && first.equals(second);

    }

}
