package com.benny.pxerstudio.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.benny.pxerstudio.R
import com.benny.pxerstudio.colorpicker.ColorPicker
import com.benny.pxerstudio.colorpicker.SatValView
import com.benny.pxerstudio.pxerexportable.AtlasExportable
import com.benny.pxerstudio.pxerexportable.FolderExportable
import com.benny.pxerstudio.pxerexportable.GifExportable
import com.benny.pxerstudio.pxerexportable.PngExportable
import com.benny.pxerstudio.shape.EraserShape
import com.benny.pxerstudio.shape.LineShape
import com.benny.pxerstudio.shape.RectShape
import com.benny.pxerstudio.util.Tool
import com.benny.pxerstudio.widget.FastBitmapView
import com.benny.pxerstudio.widget.PxerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.*

class DrawingActivity : AppCompatActivity(), FileChooserDialog.FileCallback, ItemTouchCallback, PxerView.OnDropperCallBack {

    var isEdited = false
        set(value) {
            field = value
            title_text_view.text = Html.fromHtml("PxerStudio<br><small><small>" + pxerView!!.projectName + (if (value) "*" else "") + "</small></small>")
        }

    private lateinit var fa: FastAdapter<LayerThumbItem>
    private lateinit var ia: ItemAdapter<LayerThumbItem>

    lateinit var cp: ColorPicker

    private var onlyShowSelected: Boolean = false

    fun setTitle(subtitle: String?, edited: Boolean) {
        var m_subtitle = subtitle
        if (m_subtitle == null)
            m_subtitle = UNTITLED
        title_text_view.text = Html.fromHtml("PxerStudio<br><small><small>" + m_subtitle + (if (edited) "*" else "") + "</small></small>")
        isEdited = edited
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        setTitle(UNTITLED, false)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        title_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        pxerView.selectedColor = pxerPref.getInt("lastUsedColor", Color.YELLOW)
        pxerView.setDropperCallBack(this)

        setUpLayersView()
        setupControl()

        currentProjectPath = pxerPref.getString("lastOpenedProject", null)
        if (currentProjectPath != null) {
            val file = File(currentProjectPath!!)
            if (file.exists()) {
                pxerView.loadProject(file)
                setTitle(Tool.stripExtension(file.name), false)
            }
        }
        if (fa.itemCount == 0) {
            ia.add(LayerThumbItem())
            fa.select(0)
        }
        System.gc()
    }

    override fun onColorDropped(newColor: Int) {
        fab_color.setColor(newColor)
        cp.setColor(newColor)

        TODO("Test this out")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPostCreate(savedInstanceState)
    }

    fun onToggleToolsPanel(view: View) {

    }

    private fun setupControl() {
//        val fabMenu = findViewById(R.id.fabs) as FloatingActionMenu
//        findViewById(R.id.fab_eraser).setOnClickListener {
//            fabMenu.close(true)
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_eraser_24dp)
//            pxerView!!.mode = PxerView.Mode.ShapeTool
//            pxerView!!.shapeTool = eraserShapeFactory
//        }
//        findViewById(R.id.fab_fill).setOnClickListener {
//            fabMenu.close(true)
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_fill_24dp)
//            pxerView!!.mode = PxerView.Mode.Fill
//        }
//        findViewById(R.id.fab_dropper).setOnClickListener {
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_colorize_24dp)
//            pxerView!!.mode = PxerView.Mode.Dropper
//        }
//        findViewById(R.id.fab_pen).setOnClickListener {
//            fabMenu.close(true)
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_mode_edit_24dp)
//            pxerView!!.mode = PxerView.Mode.Normal
//        }
//        findViewById(R.id.fab_rect).setOnClickListener {
//            fabMenu.close(true)
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_square_24dp)
//            pxerView!!.mode = PxerView.Mode.ShapeTool
//            pxerView!!.shapeTool = rectShapeFactory
//        }
//        findViewById(R.id.fab_line).setOnClickListener {
//            fabMenu.close(true)
//            fabMenu.menuIconView.setImageResource(R.drawable.ic_line_24dp)
//            pxerView!!.mode = PxerView.Mode.ShapeTool
//            pxerView!!.shapeTool = lineShapeFactory
//        }


        fab_color.setColor(pxerView.selectedColor)
        fab_color.colorNormal = pxerView.selectedColor
        fab_color.colorPressed = pxerView.selectedColor
        cp = ColorPicker(this, pxerView.selectedColor, SatValView.OnColorChangeListener { newColor ->
            pxerView.selectedColor = newColor
            fab_color.setColor(newColor)
        })
        fab_color.setOnClickListener { view -> cp.show(view) }
        findViewById(R.id.fab_undo).setOnClickListener { pxerView.undo() }
        findViewById(R.id.fab_redo).setOnClickListener { pxerView.redo() }
    }

    private fun setUpLayersView() {
        val layersBtn = findViewById(R.id.layers_add)

        layersBtn.setOnClickListener {
            pxerView!!.addLayer()
            ia.add(Math.max(pxerView!!.currentLayer, 0), LayerThumbItem())
            fa.deselect()
            fa.select(pxerView!!.currentLayer)
            layers_recycler.invalidate()
        }

        fa = FastAdapter()
        ia = ItemAdapter()

        layers_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layers_recycler.adapter = ia.wrap(fa)

        layers_recycler.itemAnimator = DefaultItemAnimator()

        val touchCallback = SimpleDragCallback(this)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(layers_recycler)

        fa.withSelectable(true)
        fa.withMultiSelect(false)
        fa.withAllowDeselection(false)

        fa.withOnLongClickListener { _, _, _, position ->
            fa.deselect()
            fa.select(position)
            false
        }
        fa.withOnClickListener { v, _, item, position ->
            if (onlyShowSelected) {
                val layer = pxerView!!.pxerLayers[pxerView!!.currentLayer]
                layer.visible = false
                pxerView!!.invalidate()

                fa.notifyAdapterItemChanged(pxerView!!.currentLayer)
            }
            pxerView!!.currentLayer = position
            if (onlyShowSelected) {
                val layer = pxerView!!.pxerLayers[pxerView!!.currentLayer]
                layer.visible = true
                pxerView!!.invalidate()

                fa.notifyAdapterItemChanged(pxerView!!.currentLayer)
            }
            item.pressed()
            if (item.isPressSecondTime) {
                val popupMenu = PopupMenu(this@DrawingActivity, v)
                popupMenu.inflate(R.menu.menu_popup_layer)
                popupMenu.setOnMenuItemClickListener { clickedItem ->
                    this@DrawingActivity.onOptionsItemSelected(clickedItem)
                    false
                }
                popupMenu.show()
            }
            true
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        if (!isEdited)
            isEdited = true

        pxerView!!.moveLayer(oldPosition, newPosition)

        if (oldPosition < newPosition) {
            for (i in oldPosition + 1..newPosition) {
                Collections.swap(ia.adapterItems, i, i - 1)
                fa.notifyAdapterItemMoved(i, i - 1)
            }
        } else {
            for (i in oldPosition - 1 downTo newPosition) {
                Collections.swap(ia.adapterItems, i, i + 1)
                fa.notifyAdapterItemMoved(i, i + 1)
            }
        }

        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        pxerView!!.currentLayer = newPosition
    }

    fun onLayerUpdate() {
        ia.clear()
        for (i in 0 until pxerView!!.pxerLayers.size) {
            ia.add(LayerThumbItem())
        }
        fa.select(0)
        ia.getAdapterItem(0).pressed()
    }

    fun onLayerRefresh() {
        if (layers_recycler != null)
            layers_recycler!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_drawing, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.onlyshowselectedlayer -> {
                onlyShowSelected = true
                pxerView!!.visibilityAllLayer(false)

                val layer2 = pxerView!!.pxerLayers[pxerView!!.currentLayer]
                layer2.visible = true
                pxerView!!.invalidate()

                fa.notifyAdapterDataSetChanged()
            }
            R.id.export -> PngExportable().runExport(this, pxerView)
            R.id.exportgif -> GifExportable().runExport(this, pxerView)
            R.id.exportfolder -> FolderExportable().runExport(this, pxerView)
            R.id.exportatlas -> AtlasExportable().runExport(this, pxerView)
            R.id.save -> pxerView!!.save(true)
            R.id.projectm -> {
                pxerView!!.save(false)
                startActivityForResult(Intent(this, ProjectManagerActivity::class.java), 659)
            }
            R.id.open -> FileChooserDialog.Builder(this)
                    .initialPath(Environment.getExternalStorageDirectory().path + "/PxerStudio/Project")
                    .extensionsFilter(PxerView.PXER_EXTENTION_NAME)
                    .goUpLabel(".../")
                    .show()
            R.id.newp -> createNewProject()
            R.id.resetvp -> pxerView!!.resetViewPort()
            R.id.hidealllayers -> run {
                if (onlyShowSelected) return@run
                pxerView!!.visibilityAllLayer(false)
                fa.notifyAdapterDataSetChanged()
            }
            R.id.showalllayers -> {
                onlyShowSelected = false
                pxerView!!.visibilityAllLayer(true)
                fa.notifyAdapterDataSetChanged()
            }
            R.id.gridonoff -> {
                if (pxerView!!.isShowGrid)
                    item.setIcon(R.drawable.ic_grid_on_24dp)
                else
                    item.setIcon(R.drawable.ic_grid_off_24dp)
                pxerView!!.isShowGrid = !pxerView!!.isShowGrid
            }
            R.id.layers -> {
                layer_view!!.pivotX = (layer_view!!.width / 2).toFloat()
                layer_view!!.pivotY = 0f
                if (layer_view!!.visibility == View.VISIBLE) {
                    layer_view!!.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).alpha(0f).scaleX(0.85f).scaleY(0.85f).withEndAction { layer_view!!.visibility = View.INVISIBLE }
                } else {
                    layer_view!!.visibility = View.VISIBLE
                    layer_view!!.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).alpha(1f)
                }
            }
            R.id.deletelayer -> run {
                if (pxerView!!.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.deletelayer).content(R.string.deletelayerwarning).positiveText(R.string.delete).onPositive { _, _ ->
                    if (!isEdited)
                        isEdited = true

                    ia.remove(pxerView!!.currentLayer)
                    pxerView!!.removeCurrentLayer()

                    fa.deselect()
                    fa.select(pxerView!!.currentLayer)
                    fa.notifyAdapterDataSetChanged()
                }.show()
            }
            R.id.copypastelayer -> {
                pxerView!!.copyAndPasteCurrentLayer()
                ia.add(Math.max(pxerView!!.currentLayer, 0), LayerThumbItem())
                fa.deselect()
                fa.select(pxerView!!.currentLayer)
                layers_recycler.invalidate()
            }
            R.id.mergealllayer -> run {
                if (pxerView!!.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.mergealllayers).content(R.string.mergealllayerswarning).positiveText(R.string.merge).onPositive { _, _ ->
                    if (!isEdited)
                        isEdited = true

                    pxerView!!.mergeAllLayers()
                    ia.clear()
                    ia.add(LayerThumbItem())
                    fa.deselect()
                    fa.select(0)
                }.show()
            }
            R.id.about -> startActivity(Intent(this@DrawingActivity, AboutActivity::class.java))
            R.id.tvisibility -> run {
                if (onlyShowSelected) return@run
                val layer = pxerView!!.pxerLayers[pxerView!!.currentLayer]
                layer.visible = !layer.visible
                pxerView!!.invalidate()
                fa.notifyAdapterItemChanged(pxerView!!.currentLayer)
            }
            R.id.clearlayer -> Tool.prompt(this)
                    .title(R.string.clearcurrentlayer)
                    .content(R.string.clearcurrentlayerwarning)
                    .positiveText(R.string.clear)
                    .onPositive { _, _ -> pxerView!!.clearCurrentLayer() }.show()
            R.id.mergedown -> run {
                if (pxerView!!.currentLayer == pxerView!!.pxerLayers.size - 1) return@run
                Tool.prompt(this)
                        .title(R.string.mergedownlayer)
                        .content(R.string.mergedownlayerwarning)
                        .positiveText(R.string.merge)
                        .onPositive { _, _ ->
                            pxerView!!.mergeDownLayer()
                            ia.remove(pxerView!!.currentLayer + 1)
                            fa.select(pxerView!!.currentLayer)
                        }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 659 && data != null) {
            val path = data.getStringExtra("selectedProjectPath")
            if (path != null && !path.isEmpty()) {
                currentProjectPath = path
                val file = File(path)
                if (file.exists()) {
                    pxerView!!.loadProject(file)
                    setTitle(Tool.stripExtension(file.name), false)
                }
            } else if (data.getBooleanExtra("fileNameChanged", false)) {
                currentProjectPath = ""
                pxerView!!.projectName = ""
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNewProject() {
        val l = layoutInflater.inflate(R.layout.dialog_activity_drawing_newproject, null) as ConstraintLayout
        val editText = l.findViewById(R.id.et1) as EditText
        val seekBar = l.findViewById(R.id.sb) as SeekBar
        val textView = l.findViewById(R.id.tv2) as TextView
        val seekBar2 = l.findViewById(R.id.sb2) as SeekBar
        val textView2 = l.findViewById(R.id.tv3) as TextView
        seekBar.max = 127
        seekBar.progress = 39
        textView.text = "Width : " + 40
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.text = "Width : " + (i + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBar2.max = 127
        seekBar2.progress = 39
        textView2.text = "Height : " + 40
        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView2.text = "Height : " + (i + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .customView(l, false)
                .title(R.string.newproject)
                .positiveText(R.string.create)
                .negativeText(R.string.cancel)
                .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                    if (editText.text.toString().isEmpty()) return@SingleButtonCallback
                    setTitle(editText.text.toString(), true)
                    pxerView!!.createBlankProject(editText.text.toString(), seekBar.progress + 1, seekBar2.progress + 1)
                })
                .show()
        pxerView!!.save(false)
    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        pxerView!!.loadProject(file)
        setTitle(Tool.stripExtension(file.name), false)
        currentProjectPath = file.path
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {

    }

    override fun onStop() {
        saveState()
        super.onStop()
    }

    private fun saveState() {
        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        pxerPref.edit()
                .putString("lastOpenedProject", currentProjectPath)
                .putInt("lastUsedColor", pxerView!!.selectedColor)
                .apply()
        if (pxerView!!.projectName != null && !pxerView!!.projectName.isEmpty())
            pxerView!!.save(false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cp.onConfigChanges()
    }

    private inner class LayerThumbItem : AbstractItem<LayerThumbItem, LayerThumbItem.ViewHolder>() {
        var pressedTime = 0

        val isPressSecondTime: Boolean
            get() = pressedTime == 2

        fun pressed() {
            pressedTime++
            pressedTime = Math.min(2, pressedTime)
        }

        override fun getType(): Int {
            return R.id.layerthumbitem
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_layerthumbitem
        }

        override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
            super.bindView(viewHolder, payloads)
            viewHolder.iv.isSelected = isSelected

            val layer = pxerView!!.pxerLayers[viewHolder.layoutPosition]
            viewHolder.iv.setVisible(layer.visible)
            viewHolder.iv.bitmap = layer.bitmap
        }

        override fun isSelectable(): Boolean {
            return true
        }

        override fun withSetSelected(selected: Boolean): LayerThumbItem {
            if (!selected)
                pressedTime = 0
            return super.withSetSelected(selected)
        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var iv: FastBitmapView = view as FastBitmapView
        }
    }

    companion object {
        val UNTITLED = "Untitled"
        val rectShapeFactory = RectShape()
        val lineShapeFactory = LineShape()
        val eraserShapeFactory = EraserShape()
        var currentProjectPath: String? = null
    }
}
