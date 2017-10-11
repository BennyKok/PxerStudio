package com.benny.pxerstudio.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.benny.pxerstudio.R
import com.benny.pxerstudio.pxerexportable.ExportingUtils
import com.benny.pxerstudio.util.AdHelper
import com.benny.pxerstudio.util.Tool
import com.google.android.gms.ads.AdListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import java.io.File
import java.io.FileFilter
import java.util.*

class ProjectManagerActivity : AppCompatActivity() {

    internal var projects = ArrayList<File>()
    internal lateinit var fa: FastAdapter<Item>
    internal lateinit var ia: ItemAdapter<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_manager)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val rv = findViewById(R.id.rv) as RecyclerView

        //Comment this line out and the if statement if you forked this repo or downloaded the code
        val adView = AdHelper.checkAndEnableAd(this)
        if (adView != null) {
            val fl = FrameLayout(this)
            fl.visibility = View.GONE

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    fl.visibility = View.VISIBLE
                    super.onAdLoaded()
                }
            }

            //ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyle);
            //progressBar.setIndeterminate(true);

            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)

            val lp2 = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp2.gravity = Gravity.CENTER

            //fl.addView(progressBar,lp2);
            fl.addView(adView)
            (findViewById(R.id.content_project_manager) as RelativeLayout).addView(fl, lp)

            fl.id = R.id.ad_view
            (rv.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.BELOW, R.id.ad_view)
        }

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        fa = FastAdapter()
        ia = ItemAdapter()

        fa.withSelectable(false)
        rv.adapter = ia.wrap(fa)

        projects.clear()

        //Find all projects
        val parent = File(ExportingUtils.getProjectPath())
        if (parent.exists()) {
            val temp = parent.listFiles(PxerFileFilter())
            for (i in temp!!.indices) {
                projects.add(temp[i])
            }
            if (projects.size >= 1) {
                findViewById(R.id.noProjectFound).visibility = View.GONE

                for (i in projects.indices) {
                    val mName = projects[i].name.substring(0, projects[i].name.lastIndexOf('.'))
                    val mPath = projects[i].path
                    ia.add(Item(mName, mPath))
                }

                fa.withOnClickListener { _, _, item, _ ->
                    val newIntent = Intent()
                    newIntent.putExtra("selectedProjectPath", item.path)

                    setResult(Activity.RESULT_OK, newIntent)
                    finish()
                    true
                }

                fa.withOnLongClickListener { v, _, _, position ->
                    val pm = PopupMenu(v.context, v)
                    pm.inflate(R.menu.menu_popup_project)
                    pm.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.rename -> Tool.promptTextInput(this@ProjectManagerActivity, getString(R.string.rename)).input(null, projects[position].name, false) { _, input ->
                                var mInput = input.toString()
                                if (!mInput.endsWith(".pxer"))
                                    mInput += ".pxer"

                                val fromFile = File(projects[position].path)
                                val newFile = File(projects[position].parent, mInput)

                                if (fromFile.renameTo(newFile)) {
                                    projects[position] = newFile
                                    ia.set(position, Item(newFile.name, newFile.path))
                                    fa.notifyAdapterItemChanged(position)

                                    val newIntent = Intent()
                                    newIntent.putExtra("fileNameChanged", true)

                                    setResult(Activity.RESULT_OK, newIntent)
                                }
                            }.show()
                            R.id.delete -> Tool.prompt(this@ProjectManagerActivity).title(R.string.deleteproject).content(R.string.deleteprojectwarning).positiveText(R.string.delete).onPositive { _, _ ->
                                if (projects[position].delete()) {
                                    ia.remove(position)
                                    projects.removeAt(position)

                                    if (projects.size < 1)
                                        findViewById(R.id.noProjectFound).visibility = View.VISIBLE

                                    val newIntent = Intent()
                                    newIntent.putExtra("fileNameChanged", true)

                                    setResult(Activity.RESULT_OK, newIntent)

                                    Tool.toast(this@ProjectManagerActivity, getString(R.string.projectdeleted))
                                } else
                                    Tool.toast(this@ProjectManagerActivity, getString(R.string.unabletodeleteproject))
                            }.show()
                        }
                        true
                    }
                    pm.show()
                    true
                }
            }
        }
    }

    inner class PxerFileFilter : FileFilter {
        override fun accept(pathname: File): Boolean {
            return pathname.name.endsWith(".pxer")
        }
    }

    class Item(var title: String, var path: String) : AbstractItem<ProjectManagerActivity.Item, ProjectManagerActivity.Item.ViewHolder>() {

        override fun getType(): Int {
            return 0
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_project
        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        override fun bindView(holder: ViewHolder, payloads: List<*>?) {
            super.bindView(holder, payloads)

            holder.projectTitle.text = title
            holder.projectPath.text = path
        }

        class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
            internal var projectTitle: TextView = view.findViewById(R.id.title) as TextView
            internal var projectPath: TextView = view.findViewById(R.id.path) as TextView
        }
    }

}
