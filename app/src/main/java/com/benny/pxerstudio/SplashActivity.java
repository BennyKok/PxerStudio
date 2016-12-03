package com.benny.pxerstudio;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.iv).animate().alpha(1).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).setInterpolator(new AccelerateDecelerateInterpolator());
        findViewById(R.id.tv).animate().alpha(1).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).setInterpolator(new AccelerateDecelerateInterpolator());

        AdHelper.checkAndInitAd(this);

        handler = new Handler();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, DrawingActivity.class));
                    finish();
                }
            }, 2000L);
        else
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},0x456);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x456){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    Tool.toast(this,"Sorry this application require storage permission for saving your project");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    },1000);
                    return;
                }
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, DrawingActivity.class));
                    finish();
                }
            }, 2000L);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
