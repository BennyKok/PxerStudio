package com.benny.pxerstudio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.iv).animate().alpha(1).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).setInterpolator(new AccelerateDecelerateInterpolator());
        findViewById(R.id.tv).animate().alpha(1).scaleY(1.1f).scaleX(1.1f).setDuration(2000L).setInterpolator(new AccelerateDecelerateInterpolator());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,DrawingActivity.class));
                finish();
            }
        },2000L);
    }
}
