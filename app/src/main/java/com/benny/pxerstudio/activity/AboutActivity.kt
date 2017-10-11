package com.benny.pxerstudio.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView

import com.benny.pxerstudio.R

import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val tv = findViewById(R.id.tv) as TextView
        tv.text = Html.fromHtml(getString(R.string.created_by_bennykok))
        tv.movementMethod = LinkMovementMethod.getInstance()

        val tv1 = findViewById(R.id.tv1) as TextView
        tv1.text = Html.fromHtml(getString(R.string.get_more_apps))
        tv1.movementMethod = LinkMovementMethod.getInstance()

        val tv3 = findViewById(R.id.tv3) as TextView
        tv3.text = Html.fromHtml(getString(R.string.join_the_community))
        tv3.movementMethod = LinkMovementMethod.getInstance()

        val tv2 = findViewById(R.id.libinfo) as TextView
        val sb = StringBuilder()

        with(sb) {
            append(getString(R.string.brough_to_you_by))
            append("<br>")

            append("com.mikepenz:fastadapter-extensions")
            append("<br>")
            append("com.mikepenz:fastadapter")
            append("<br>")
            append("com.afollestad.material-dialogs:core")
            append("<br>")
            append("com.afollestad.material-dialogs:commons")
            append("<br>")
            append("de.psdev.licensesdialog:licensesdialog")
            append("<br>")
            append("com.github.clans:fab")
            append("<br>")
            append("com.google.code.gson:gson")
            append("<br>")
            append("com.android.support:appcompat-v7")
            append("<br>")
            append("com.android.support:support-v4")
            append("<br>")
            append("com.android.support:design")
            append("<br>")
            append("com.android.support:cardview-v7")
            append("<br>")
            append("com.android.support.constraint:constraint-layout")
        }

        tv2.movementMethod = LinkMovementMethod.getInstance()
        tv2.text = Html.fromHtml(sb.toString())

        val notices = Notices()
        notices.addNotice(Notice("FastAdapter", "https://github.com/mikepenz/FastAdapter", "Copyright 2016 Mike Penz", ApacheSoftwareLicense20()))
        notices.addNotice(Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", MITLicense()))
        notices.addNotice(Notice("FloatingActionButton", "https://github.com/Clans/FloatingActionButton", "Copyright 2015 Dmytro Tarianyk", ApacheSoftwareLicense20()))
        notices.addNotice(Notice("Gson", "https://github.com/google/gson", "Copyright 2008 Google Inc.", ApacheSoftwareLicense20()))

        val builder = LicensesDialog.Builder(this@AboutActivity)
        builder.setIncludeOwnLicense(true)
        builder.setNotices(notices)
        builder.setTitle(getString(R.string.opensource_library))
        val dialog = builder.build()

        tv2.setOnClickListener { dialog.show() }

        findViewById(R.id.iv).setOnClickListener { v ->
            if (v.animation == null || v.animation != null && v.animation.hasEnded())
                v.animate().scaleX(1.1f).scaleY(1.1f).rotationBy(-20f).withEndAction { v.animate().scaleX(1f).scaleY(1f).rotation(0f) }
        }
    }
}
