package com.github.dkus.fourflicks.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class Device {

    public static boolean hasCamera(Context c) {

        return c!=null && c.getPackageManager()!=null &&
                c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

    }

    public static boolean hasPlayServices(Context c) {

        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(c)==ConnectionResult.SUCCESS;

    }
}
