package com.benny.pxerstudio.activity

import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.parseAsHtml
import com.benny.pxerstudio.BuildConfig
import com.benny.pxerstudio.R
import com.benny.pxerstudio.databinding.ActivityAboutBinding
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.aboutAppVersion.text =
            "v" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"

        binding.aboutCreator.text = getString(R.string.creator_website).parseAsHtml()
        binding.aboutCreator.movementMethod = LinkMovementMethod.getInstance()

        binding.aboutMoreApps.text = getString(R.string.get_more_apps_link).parseAsHtml()
        binding.aboutMoreApps.movementMethod = LinkMovementMethod.getInstance()

        binding.aboutGithub.text = getString(R.string.github_link).parseAsHtml()
        binding.aboutGithub.movementMethod = LinkMovementMethod.getInstance()

        val sb = StringBuilder()
        with(sb) {
            append(getString(R.string.brought_to_you_by))
            append("<br>")

            append("androidx.appcompat:appcompat")
            append("<br>")
            append("androidx.cardview:cardview")
            append("<br>")
            append("androidx.constraintlayout:constraintlayout")
            append("<br>")
            append("com.google.android.material:material")
            append("<br>")
            append("com.afollestad.material-dialogs:core")
            append("<br>")
            append("com.afollestad.material-dialogs:files")
            append("<br>")
            append("com.afollestad.material-dialogs:input")
            append("<br>")
            append("com.mikepenz:fastadapter")
            append("<br>")
            append("com.mikepenz:fastadapter-extensions")
            append("<br>")
            append("de.psdev.licensesdialog:licensesdialog")
            append("<br>")
            append("com.github.clans:fab")
            append("<br>")
            append("com.google.code.gson:gson")
            append("<br>")
        }

        binding.aboutLibinfo.movementMethod = LinkMovementMethod.getInstance()
        binding.aboutLibinfo.text = "$sb".parseAsHtml()

        val notices = Notices()
        notices.addNotice(
            Notice(
                "Material Dialogs",
                "https://github.com/afollestad/material-dialogs",
                "Copyright (c) 2014-2016 Aidan Michael Follestad",
                MITLicense()
            )
        )
        notices.addNotice(
            Notice(
                "FastAdapter",
                "https://github.com/mikepenz/FastAdapter",
                "Copyright 2021 Mike Penz",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "FloatingActionButton",
                "https://github.com/Clans/FloatingActionButton",
                "Copyright 2015 Dmytro Tarianyk",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Gson",
                "https://github.com/google/gson",
                "Copyright 2008 Google Inc.",
                ApacheSoftwareLicense20()
            )
        )

        val builder = LicensesDialog.Builder(this@AboutActivity)
        builder.setIncludeOwnLicense(true)
        builder.setNotices(notices)
        builder.setTitle(getString(R.string.opensource_libraries))
        val dialog = builder.build()

        binding.aboutLibinfo.setOnClickListener { dialog.show() }

        binding.aboutAppIcon.setOnClickListener { v ->
            if (v.animation == null || v.animation != null && v.animation.hasEnded())
                v.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .rotationBy(-20f)
                    .withEndAction {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .rotation(0f)
                    }
        }
    }
}
