package com.benny.pxerstudio.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.benny.pxerstudio.R
import com.benny.pxerstudio.databinding.ActivityProjectManagerBinding
import com.benny.pxerstudio.databinding.ItemProjectBinding
import com.benny.pxerstudio.pxerexportable.ExportingUtils
import com.benny.pxerstudio.util.Tool
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.select.getSelectExtension
import java.io.File
import java.io.FileFilter
import java.util.*

class ProjectManagerActivity : AppCompatActivity() {

    private var projects = ArrayList<File>()
    private lateinit var fa: FastAdapter<Item>
    private lateinit var ia: ItemAdapter<Item>
    private lateinit var binding: ActivityProjectManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.projectManagerToolbar)

        //Comment this line out and the if statement if you forked this repo or downloaded the code
/*
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

            //ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
            //progressBar.setIndeterminate(true);

            val lp = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)

            val lp2 = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp2.gravity = Gravity.CENTER

            //fl.addView(progressBar,lp2);
            fl.addView(adView)
            (findViewById<RelativeLayout>(R.id.content_project_manager)).addView(fl, lp)

            fl.id = R.id.ad_view
            (rv.layoutParams as RelativeLayout.LayoutParams).addRule(
                RelativeLayout.BELOW,
                R.id.ad_view
            )
        }
*/

        binding.projectManagerCM.cMRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ia = ItemAdapter()
        fa = FastAdapter.with(ia)

        fa.getSelectExtension().isSelectable = false
        binding.projectManagerCM.cMRecyclerView.adapter = fa

        projects.clear()

        //Find all projects
        val parent = File(ExportingUtils.getProjectPath(this))
        if (parent.exists()) {
            val temp = parent.listFiles(PxerFileFilter())
            for (i in temp!!.indices) {
                projects.add(temp[i])
            }
            if (projects.size >= 1) {
                binding.projectManagerCM.cMNoProjectFound.visibility = View.GONE

                for (i in projects.indices) {
                    val mName = projects[i].name.substring(0, projects[i].name.lastIndexOf('.'))
                    val mPath = projects[i].path
                    ia.add(Item(mName, mPath))
                }

                fa.onClickListener = { _, _, item, _ ->
                    val newIntent = Intent()
                    newIntent.putExtra("selectedProjectPath", item.path)

                    setResult(RESULT_OK, newIntent)
                    finish()
                    true
                }

                fa.onLongClickListener = { v, _, _, position ->
                    val pm = PopupMenu(v.context, v)
                    pm.inflate(R.menu.menu_popup_project)
                    pm.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_popup_project_rename ->
                                MaterialDialog(this).show {
                                    title = getString(R.string.rename)
                                    input(hint = projects[position].name) { _, text ->
                                        var mInput = "$text"
                                        if (!mInput.endsWith(".pxer"))
                                            mInput += ".pxer"

                                        val fromFile = File(projects[position].path)
                                        val newFile = File(projects[position].parent, mInput)

                                        if (fromFile.renameTo(newFile)) {
                                            projects[position] = newFile
                                            ia[position] = Item(newFile.name, newFile.path)
                                            fa.notifyAdapterItemChanged(position)

                                            val newIntent = Intent()
                                            newIntent.putExtra("fileNameChanged", true)

                                            setResult(RESULT_OK, newIntent)
                                        }
                                    }
                                    positiveButton(R.string.ok)
                                }

                            R.id.menu_popup_project_delete -> Tool.prompt(this@ProjectManagerActivity)
                                .title(R.string.deleteproject)
                                .message(R.string.deleteprojectwarning)
                                .positiveButton(R.string.delete).positiveButton {
                                    if (projects[position].delete()) {
                                        ia.remove(position)
                                        projects.removeAt(position)

                                        if (projects.size < 1)
                                            binding.projectManagerCM.cMNoProjectFound.visibility =
                                                View.VISIBLE

                                        val newIntent = Intent()
                                        newIntent.putExtra("fileNameChanged", true)

                                        setResult(RESULT_OK, newIntent)

                                        Tool.toast(
                                            this@ProjectManagerActivity,
                                            getString(R.string.projectdeleted)
                                        )
                                    } else
                                        Tool.toast(
                                            this@ProjectManagerActivity,
                                            getString(R.string.unabletodeleteproject)
                                        )
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

    class Item(var title: String, var path: String) : AbstractItem<Item.ViewHolder>() {

        override val type: Int
            get() = 0

        override val layoutRes: Int
            get() = R.layout.item_project

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        class ViewHolder internal constructor(view: View) : FastAdapter.ViewHolder<Item>(view) {
            private var itemProjectBinding = ItemProjectBinding.bind(view)

            override fun bindView(item: Item, payloads: List<Any>) {
                itemProjectBinding.itemProjectTitle.text = item.title
                itemProjectBinding.itemProjectPath.text = item.path
            }

            override fun unbindView(item: Item) {
            }
        }
    }
}
