package com.benny.pxerstudio.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by BennyKok on 28/10/2016.
 */

public class AdHelper {
    public static void checkAndInitAd(Activity activity){
        boolean adOk = !isAppInstalled(activity,"com.benny.pxerstudiopremiumkey");
        if (adOk){
            MobileAds.initialize(activity.getApplicationContext(),"ca-app-pub-9055897931653836~9849159702");
        }
    }

    public static NativeExpressAdView checkAndEnableAd(Activity activity){
        boolean adOk = !isAppInstalled(activity,"com.benny.pxerstudiopremiumkey") && isNetworkAvailable(activity);

        if (adOk){
            NativeExpressAdView adView = new NativeExpressAdView(activity);
            adView.setAdUnitId("ca-app-pub-9055897931653836/2965021303");
            adView.setAdSize(new AdSize(360,100));

            AdRequest request = new AdRequest.Builder().addTestDevice("0FBE03386270FED7E26F87DA14377E31").build();
            adView.loadAd(request);

            return adView;
        }
        return null;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
