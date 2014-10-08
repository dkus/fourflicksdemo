package com.github.dkus.fourflicks.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class Device {

    public static boolean hasCamera(Context c) {

        return c!=null && c.getPackageManager()!=null &&
                c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

    }

    public static Intent hasCameraApp(Context c) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent.resolveActivity(c.getPackageManager())!=null ? intent : null;

    }

    public static boolean hasPlayServices(Context c) {

        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(c)==ConnectionResult.SUCCESS;

    }
}
