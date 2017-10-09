package com.benny.pxerstudio.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.benny.pxerstudio.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText(Html.fromHtml(getString(R.string.created_by_bennykok)));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setText(Html.fromHtml(getString(R.string.get_more_apps)));
        tv1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tv3 = (TextView) findViewById(R.id.tv3);
        tv3.setText(Html.fromHtml(getString(R.string.join_the_community)));
        tv3.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tv2 = (TextView) findViewById(R.id.libinfo);
        StringBuilder sb = new StringBuilder();

        sb.append(getString(R.string.brough_to_you_by));
        sb.append("<br>");

        sb.append("com.mikepenz:fastadapter-extensions");
        sb.append("<br>");
        sb.append("com.mikepenz:fastadapter");
        sb.append("<br>");
        sb.append("com.afollestad.material-dialogs:core");
        sb.append("<br>");
        sb.append("com.afollestad.material-dialogs:commons");
        sb.append("<br>");
        sb.append("de.psdev.licensesdialog:licensesdialog");
        sb.append("<br>");
        sb.append("com.github.clans:fab");
        sb.append("<br>");
        sb.append("com.google.code.gson:gson");
        sb.append("<br>");
        sb.append("com.android.support:appcompat-v7");
        sb.append("<br>");
        sb.append("com.android.support:support-v4");
        sb.append("<br>");
        sb.append("com.android.support:design");
        sb.append("<br>");
        sb.append("com.android.support:cardview-v7");
        sb.append("<br>");
        sb.append("com.android.support.constraint:constraint-layout");

        tv2.setMovementMethod(LinkMovementMethod.getInstance());
        tv2.setText(Html.fromHtml(sb.toString()));

        final Notices notices = new Notices();
        notices.addNotice(new Notice("FastAdapter", "https://github.com/mikepenz/FastAdapter", "Copyright 2016 Mike Penz", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
        notices.addNotice(new Notice("FloatingActionButton", "https://github.com/Clans/FloatingActionButton", "Copyright 2015 Dmytro Tarianyk", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Gson", "https://github.com/google/gson", "Copyright 2008 Google Inc.", new ApacheSoftwareLicense20()));

        LicensesDialog.Builder builder = new LicensesDialog.Builder(AboutActivity.this);
        builder.setIncludeOwnLicense(true);
        builder.setNotices(notices);
        builder.setTitle(getString(R.string.opensource_library));
        final LicensesDialog dialog = builder.build();

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });


        findViewById(R.id.iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (v.getAnimation() == null || (v.getAnimation() != null && v.getAnimation().hasEnded()))
                    v.animate().scaleX(1.1f).scaleY(1.1f).rotationBy(-20).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.animate().scaleX(1f).scaleY(1f).rotation(0);
                        }
                    });
            }
        });
    }
}
