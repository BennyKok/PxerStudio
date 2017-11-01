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
import android.widget.ImageView
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

    companion object {
        val UNTITLED = "Untitled"
        val rectShapeFactory = RectShape()
        val lineShapeFactory = LineShape()
        val eraserShapeFactory = EraserShape()
        var currentProjectPath: String? = null
    }

    var isEdited = false
        set(value) {
            field = value
            title_text_view.text = Html.fromHtml("PxerStudio<br><small><small>" + pxerView.projectName + (if (value) "*" else "") + "</small></small>")
        }

    private lateinit var layerAdapter: FastAdapter<LayerThumbItem>
    private lateinit var layerItemAdapter: ItemAdapter<LayerThumbItem>

    private lateinit var toolsAdapter: FastAdapter<ToolItem>
    private lateinit var toolsItemAdapter: ItemAdapter<ToolItem>

    private lateinit var cp: ColorPicker

    private var onlyShowSelected: Boolean = false

    fun setTitle(subtitle: String?, edited: Boolean) {
        title_text_view.text = Html.fromHtml("PxerStudio<br><small><small>" + if (subtitle.isNullOrEmpty()) UNTITLED else subtitle + (if (edited) "*" else "") + "</small></small>")
        isEdited = edited
    }

    private lateinit var previousMode: PxerView.Mode

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
        if (!currentProjectPath.isNullOrEmpty()) {
            val file = File(currentProjectPath!!)
            if (file.exists()) {
                pxerView.loadProject(file)
                setTitle(Tool.stripExtension(file.name), false)
            }
        }
        if (layerAdapter.itemCount == 0) {
            layerItemAdapter.add(LayerThumbItem())
            layerAdapter.select(0)
            layerItemAdapter.getAdapterItem(0).pressed()
        }
        System.gc()
    }

    override fun onColorDropped(newColor: Int) {
        fab_color.setColor(newColor)
        cp.setColor(newColor)

        fab_dropper.callOnClick()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPostCreate(savedInstanceState)
    }

    fun onProjectTitleClicked (view: View){
        openProjectManager()
    }

    fun onToggleToolsPanel(view: View) {
        if (tools_view.visibility == View.INVISIBLE) {
            tools_view.visibility = View.VISIBLE
            tools_view.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).translationX(0f)
        } else {
            tools_view.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).translationX((+tools_view.width).toFloat()).withEndAction({
                tools_view.visibility = View.INVISIBLE
            })
        }
    }

    private fun setupControl() {
        tools_view.post({
            tools_view.translationX = (tools_view.width).toFloat()
        })

        toolsAdapter = FastAdapter()
        toolsItemAdapter = ItemAdapter()

        tools_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        tools_recycler.adapter = toolsItemAdapter.wrap(toolsAdapter)

        tools_recycler.itemAnimator = DefaultItemAnimator()

        with(toolsAdapter) {
            withMultiSelect(false)
            withSelectable(true)
            withAllowDeselection(false)
        }

        with(toolsItemAdapter) {
            add(ToolItem(R.drawable.ic_square_24dp).withOnItemClickListener { _, _, item, _ ->
                pxerView.mode = PxerView.Mode.ShapeTool
                pxerView.shapeTool = rectShapeFactory

                tools_fab.setImageResource(item.icon)
                notifyDataSetChanged()
                true
            })
            add(ToolItem(R.drawable.ic_line_24dp).withOnItemClickListener { _, _, item, _ ->
                pxerView.mode = PxerView.Mode.ShapeTool
                pxerView.shapeTool = lineShapeFactory

                tools_fab.setImageResource(item.icon)
                notifyDataSetChanged()
                true
            })
            add(ToolItem(R.drawable.ic_fill_24dp).withOnItemClickListener { _, _, item, _ ->
                pxerView.mode = PxerView.Mode.Fill

                tools_fab.setImageResource(item.icon)
                notifyDataSetChanged()
                true
            })
            add(ToolItem(R.drawable.ic_eraser_24dp).withOnItemClickListener { _, _, item, _ ->
                pxerView.mode = PxerView.Mode.ShapeTool
                pxerView.shapeTool = eraserShapeFactory

                tools_fab.setImageResource(item.icon)
                notifyDataSetChanged()
                true
            })
            add(ToolItem(R.drawable.ic_mode_edit_24dp).withOnItemClickListener { _, _, item, _ ->
                pxerView.mode = PxerView.Mode.Normal

                tools_fab.setImageResource(item.icon)
                notifyDataSetChanged()
                true
            })
        }
        toolsItemAdapter.adapterItems.reverse()
        toolsAdapter.select(0)

        fab_color.setColor(pxerView.selectedColor)
        fab_color.colorNormal = pxerView.selectedColor
        fab_color.colorPressed = pxerView.selectedColor
        cp = ColorPicker(this, pxerView.selectedColor, SatValView.OnColorChangeListener { newColor ->
            pxerView.selectedColor = newColor
            fab_color.setColor(newColor)
        })
        fab_color.setOnClickListener { view -> cp.show(view) }
        fab_undo.setOnClickListener { pxerView.undo() }
        fab_redo.setOnClickListener { pxerView.redo() }
        fab_dropper.setOnClickListener {
            if (pxerView.mode == PxerView.Mode.Dropper){
                fab_undo.show(true)
                fab_redo.show(true)

                tools_fab.show(true)

                pxerView.mode = previousMode

                fab_dropper.setImageResource(R.drawable.ic_colorize_24dp)
            }else{
                fab_undo.hide(true)
                fab_redo.hide(true)

                tools_fab.hide(true)
                if (tools_view.visibility == View.VISIBLE)
                    tools_fab.callOnClick()

                previousMode = pxerView.mode
                pxerView.mode = PxerView.Mode.Dropper

                fab_dropper.setImageResource(R.drawable.ic_close_24dp)
            }
        }
    }

    private fun setUpLayersView() {
        val layersBtn = findViewById(R.id.layers_add)

        layersBtn.setOnClickListener {
            pxerView.addLayer()
            layerItemAdapter.add(Math.max(pxerView.currentLayer, 0), LayerThumbItem())
            layerAdapter.deselect()
            layerAdapter.select(pxerView.currentLayer)
            layerItemAdapter.getAdapterItem(pxerView.currentLayer).pressed()
            layers_recycler.invalidate()
        }

        layerAdapter = FastAdapter()
        layerItemAdapter = ItemAdapter()

        layers_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layers_recycler.adapter = layerItemAdapter.wrap(layerAdapter)

        layers_recycler.itemAnimator = DefaultItemAnimator()
        layers_recycler.itemAnimator.changeDuration = 0
        layers_recycler.itemAnimator.addDuration = 0
        layers_recycler.itemAnimator.removeDuration = 0

        val touchCallback = SimpleDragCallback(this)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(layers_recycler)

        with(layerAdapter) {
            withSelectable(true)
            withMultiSelect(false)
            withAllowDeselection(false)
        }

        layerAdapter.withOnLongClickListener { _, _, item, position ->
            layerAdapter.deselect()
            layerAdapter.select(position)

            item.pressed()
            false
        }
        layerAdapter.withOnClickListener { v, _, item, position ->
            if (onlyShowSelected) {
                val layer = pxerView.pxerLayers[pxerView.currentLayer]
                layer.visible = false
                pxerView.invalidate()

                layerAdapter.notifyAdapterItemChanged(pxerView.currentLayer)
            }
            pxerView.currentLayer = position
            if (onlyShowSelected) {
                val layer = pxerView.pxerLayers[pxerView.currentLayer]
                layer.visible = true
                pxerView.invalidate()

                layerAdapter.notifyAdapterItemChanged(pxerView.currentLayer)
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

        pxerView.moveLayer(oldPosition, newPosition)

        if (oldPosition < newPosition) {
            for (i in oldPosition + 1..newPosition) {
                Collections.swap(layerItemAdapter.adapterItems, i, i - 1)
                layerAdapter.notifyAdapterItemMoved(i, i - 1)
            }
        } else {
            for (i in oldPosition - 1 downTo newPosition) {
                Collections.swap(layerItemAdapter.adapterItems, i, i + 1)
                layerAdapter.notifyAdapterItemMoved(i, i + 1)
            }
        }

        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        pxerView.currentLayer = newPosition
    }

    fun onLayerUpdate() {
        layerItemAdapter.clear()
        for (i in 0 until pxerView.pxerLayers.size) {
            layerItemAdapter.add(LayerThumbItem())
        }
        layerAdapter.select(0)
        layerItemAdapter.getAdapterItem(0).pressed()
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
                pxerView.visibilityAllLayer(false)

                val layer2 = pxerView.pxerLayers[pxerView.currentLayer]
                layer2.visible = true
                pxerView.invalidate()

                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.export -> PngExportable().runExport(this, pxerView)
            R.id.exportgif -> GifExportable().runExport(this, pxerView)
            R.id.exportfolder -> FolderExportable().runExport(this, pxerView)
            R.id.exportatlas -> AtlasExportable().runExport(this, pxerView)
            R.id.save -> pxerView.save(true)
            R.id.projectm -> openProjectManager()
            R.id.open -> FileChooserDialog.Builder(this)
                    .initialPath(Environment.getExternalStorageDirectory().path + "/PxerStudio/Project")
                    .extensionsFilter(PxerView.PXER_EXTENSION_NAME)
                    .goUpLabel(".../")
                    .show()
            R.id.newp -> createNewProject()
            R.id.resetvp -> pxerView.resetViewPort()
            R.id.hidealllayers -> run {
                if (onlyShowSelected) return@run
                pxerView.visibilityAllLayer(false)
                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.showalllayers -> {
                onlyShowSelected = false
                pxerView.visibilityAllLayer(true)
                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.gridonoff -> {
                if (pxerView.isShowGrid)
                    item.setIcon(R.drawable.ic_grid_on_24dp)
                else
                    item.setIcon(R.drawable.ic_grid_off_24dp)
                pxerView.isShowGrid = !pxerView.isShowGrid
            }
            R.id.layers -> {
                layer_view.pivotX = (layer_view!!.width / 2).toFloat()
                layer_view.pivotY = 0f
                if (layer_view.visibility == View.VISIBLE) {
                    layer_view.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).alpha(0f).scaleX(0.85f).scaleY(0.85f).withEndAction {
                        layer_view.visibility = View.INVISIBLE
                    }
                } else {
                    layer_view.visibility = View.VISIBLE
                    layer_view.animate().setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).alpha(1f)
                }
//                layer_view.alpha = 1f
//                layer_view.scaleX = 1f
//                layer_view.scaleY = 1f

//                if (layer_view.visibility == View.VISIBLE)
//                    layer_view!!.visibility = View.INVISIBLE
//                else
//                    layer_view.visibility = View.VISIBLE
            }
            R.id.deletelayer -> run {
                if (pxerView.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.deletelayer).content(R.string.deletelayerwarning).positiveText(R.string.delete).onPositive { _, _ ->
                    if (!isEdited)
                        isEdited = true

                    layerItemAdapter.remove(pxerView.currentLayer)
                    pxerView.removeCurrentLayer()

                    layerAdapter.deselect()
                    layerAdapter.select(pxerView.currentLayer)
                    layerItemAdapter.getAdapterItem(pxerView.currentLayer).pressed()
                    layerAdapter.notifyAdapterDataSetChanged()
                }.show()
            }
            R.id.copypastelayer -> {
                pxerView.copyAndPasteCurrentLayer()
                layerItemAdapter.add(Math.max(pxerView.currentLayer, 0), LayerThumbItem())
                layerAdapter.deselect()
                layerAdapter.select(pxerView.currentLayer)
                layerItemAdapter.getAdapterItem(pxerView.currentLayer).pressed()
                layers_recycler.invalidate()
            }
            R.id.mergealllayer -> run {
                if (pxerView.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.mergealllayers).content(R.string.mergealllayerswarning).positiveText(R.string.merge).onPositive { _, _ ->
                    if (!isEdited)
                        isEdited = true

                    pxerView.mergeAllLayers()
                    layerItemAdapter.clear()
                    layerItemAdapter.add(LayerThumbItem())
                    layerAdapter.deselect()
                    layerAdapter.select(0)
                    layerItemAdapter.getAdapterItem(0).pressed()
                }.show()
            }
            R.id.about -> startActivity(Intent(this@DrawingActivity, AboutActivity::class.java))
            R.id.tvisibility -> run {
                if (onlyShowSelected) return@run
                val layer = pxerView.pxerLayers[pxerView.currentLayer]
                layer.visible = !layer.visible
                pxerView.invalidate()
                layerAdapter.notifyAdapterItemChanged(pxerView.currentLayer)
            }
            R.id.clearlayer -> Tool.prompt(this)
                    .title(R.string.clearcurrentlayer)
                    .content(R.string.clearcurrentlayerwarning)
                    .positiveText(R.string.clear)
                    .onPositive { _, _ -> pxerView.clearCurrentLayer() }.show()
            R.id.mergedown -> run {
                if (pxerView.currentLayer == pxerView.pxerLayers.size - 1) return@run
                Tool.prompt(this)
                        .title(R.string.mergedownlayer)
                        .content(R.string.mergedownlayerwarning)
                        .positiveText(R.string.merge)
                        .onPositive { _, _ ->
                            pxerView.mergeDownLayer()
                            layerItemAdapter.remove(pxerView.currentLayer + 1)
                            layerAdapter.select(pxerView.currentLayer)
                            layerItemAdapter.getAdapterItem(pxerView.currentLayer).pressed()
                        }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openProjectManager() {
        pxerView.save(false)
        startActivityForResult(Intent(this, ProjectManagerActivity::class.java), 659)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 659 && data != null) {
            val path = data.getStringExtra("selectedProjectPath")
            if (path != null && !path.isEmpty()) {
                currentProjectPath = path
                val file = File(path)
                if (file.exists()) {
                    pxerView.loadProject(file)
                    setTitle(Tool.stripExtension(file.name), false)
                }
            } else if (data.getBooleanExtra("fileNameChanged", false)) {
                currentProjectPath = ""
                pxerView.projectName = ""
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
                    pxerView.createBlankProject(editText.text.toString(), seekBar.progress + 1, seekBar2.progress + 1)
                })
                .show()
        pxerView.save(false)
    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        pxerView.loadProject(file)
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
                .putInt("lastUsedColor", pxerView.selectedColor)
                .apply()
        if (!pxerView.projectName.isNullOrEmpty() || pxerView.projectName != UNTITLED)
            pxerView.save(false)
        else
            pxerView.save(true)
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
            return R.id.item_layer_thumb
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_layer_thumb
        }

        override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
            super.bindView(viewHolder, payloads)
            viewHolder.iv.isSelected = isSelected

            val layer = pxerView.pxerLayers[viewHolder.layoutPosition]
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

    private inner class ToolItem(var icon: Int) : AbstractItem<ToolItem, ToolItem.ViewHolder>() {

        override fun getType(): Int {
            return R.id.item_tool
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_tool
        }

        override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
            super.bindView(viewHolder, payloads)

            if (isSelected)
                viewHolder.iv.alpha = 1f
            else
                viewHolder.iv.alpha = 0.3f

            viewHolder.iv.setImageResource(icon)
        }

        override fun isSelectable(): Boolean {
            return true
        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var iv: ImageView = view as ImageView
        }
    }
}
