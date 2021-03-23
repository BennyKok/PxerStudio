package com.benny.pxerstudio.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.fileChooser
import com.benny.pxerstudio.R
import com.benny.pxerstudio.colorpicker.ColorPicker
import com.benny.pxerstudio.colorpicker.SatValView
import com.benny.pxerstudio.databinding.ActivityDrawingBinding
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
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.select.getSelectExtension
import java.io.File
import java.util.*

class DrawingActivity : AppCompatActivity(), ItemTouchCallback, PxerView.OnDropperCallBack {

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
            binding!!.titleTextView.text =
                Html.fromHtml(
                    "PxerStudio<br><small><small>"
                            + binding!!.pxerView.projectName
                            + (if (value) "*" else "") + "</small></small>"
                )
        }

    private lateinit var layerAdapter: FastAdapter<LayerThumbItem>
    private lateinit var layerItemAdapter: ItemAdapter<LayerThumbItem>

    private lateinit var toolsAdapter: FastAdapter<ToolItem>
    private lateinit var toolsItemAdapter: ItemAdapter<ToolItem>

    private lateinit var cp: ColorPicker

    private var onlyShowSelected: Boolean = false

    fun setTitle(subtitle: String?, edited: Boolean) {
        binding!!.titleTextView.text =
            Html.fromHtml(
                "PxerStudio<br><small><small>" +
                        if (subtitle.isNullOrEmpty()) UNTITLED
                        else subtitle + (if (edited) "*" else "") + "</small></small>"
            )
        isEdited = edited
    }

    private lateinit var previousMode: PxerView.Mode

    private var binding: ActivityDrawingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setTitle(UNTITLED, false)
        binding!!.toolbar.title = ""
        setSupportActionBar(binding!!.toolbar)
        binding!!.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        binding!!.pxerView.selectedColor = pxerPref.getInt("lastUsedColor", Color.YELLOW)
        binding!!.pxerView.setDropperCallBack(this)

        setUpLayersView()
        setupControl()

        currentProjectPath = pxerPref.getString("lastOpenedProject", null)
        print(currentProjectPath)
        if (!currentProjectPath.isNullOrEmpty()) {
            val file = File(currentProjectPath!!)
            if (file.exists()) {
                binding!!.pxerView.loadProject(file)
                setTitle(Tool.stripExtension(file.name), false)
            }
        }
        if (layerAdapter.itemCount == 0) {
            layerItemAdapter.add(LayerThumbItem())
            layerAdapter.getSelectExtension().select(0)
            layerItemAdapter.getAdapterItem(0).pressed()
        }
        System.gc()
    }

    override fun onColorDropped(newColor: Int) {
        binding!!.fabColor.setColor(newColor)
        cp.setColor(newColor)

        binding!!.fabDropper.callOnClick()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPostCreate(savedInstanceState)
    }

    fun onProjectTitleClicked(view: View) {
        openProjectManager()
    }

    fun onToggleToolsPanel(view: View) {
        if (binding!!.toolsView.visibility == View.INVISIBLE) {
            binding!!.toolsView.visibility = View.VISIBLE
            binding!!.toolsView
                .animate()
                .setDuration(100)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .translationX(0f)
        } else {
            binding!!.toolsView
                .animate()
                .setDuration(100)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .translationX((+binding!!.toolsView.width).toFloat())
                .withEndAction {
                    binding!!.toolsView.visibility = View.INVISIBLE
                }
        }
    }

    private fun setupControl() {
        binding!!.toolsView.post {
            binding!!.toolsView.translationX = (binding!!.toolsView.width).toFloat()
        }

        toolsItemAdapter = ItemAdapter()
        toolsAdapter = FastAdapter.with(toolsItemAdapter)

        binding!!.toolsRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding!!.toolsRecycler.adapter = toolsAdapter

        binding!!.toolsRecycler.itemAnimator = DefaultItemAnimator()

        val selectExtension = toolsAdapter.getSelectExtension()
        selectExtension.isSelectable = true
        selectExtension.multiSelect = false
        selectExtension.allowDeselection = true

        toolsAdapter.onClickListener = { view, adapter, item, position ->
            binding!!.toolsFab.setImageResource(item.icon)
            when (item.icon) {
                R.drawable.ic_square_24dp -> {
                    binding!!.pxerView.mode = PxerView.Mode.ShapeTool
                    binding!!.pxerView.shapeTool = rectShapeFactory
                }
                R.drawable.ic_line_24dp -> {
                    binding!!.pxerView.mode = PxerView.Mode.ShapeTool
                    binding!!.pxerView.shapeTool = lineShapeFactory
                }
                R.drawable.ic_fill_24dp -> {
                    binding!!.pxerView.mode = PxerView.Mode.Fill
                }
                R.drawable.ic_eraser_24dp -> {
                    binding!!.pxerView.mode = PxerView.Mode.ShapeTool
                    binding!!.pxerView.shapeTool = eraserShapeFactory
                }
                R.drawable.ic_mode_edit_24dp -> {
                    binding!!.pxerView.mode = PxerView.Mode.Normal
                }
            }
            false
        }
        with(toolsItemAdapter) {
            add(ToolItem(R.drawable.ic_square_24dp))
            add(ToolItem(R.drawable.ic_line_24dp))
            add(ToolItem(R.drawable.ic_fill_24dp))
            add(ToolItem(R.drawable.ic_eraser_24dp))
            add(ToolItem(R.drawable.ic_mode_edit_24dp))
        }
        toolsItemAdapter.adapterItems.reverse()
        toolsAdapter.getSelectExtension().select(0)

        binding!!.fabColor.setColor(binding!!.pxerView.selectedColor)
        binding!!.fabColor.colorNormal = binding!!.pxerView.selectedColor
        binding!!.fabColor.colorPressed = binding!!.pxerView.selectedColor
        cp = ColorPicker(
            this,
            binding!!.pxerView.selectedColor,
            SatValView.OnColorChangeListener { newColor ->
                binding!!.pxerView.selectedColor = newColor
                binding!!.fabColor.setColor(newColor)
            })
        binding!!.fabColor.setOnClickListener { view -> cp.show(view) }
        binding!!.fabUndo.setOnClickListener { binding!!.pxerView.undo() }
        binding!!.fabRedo.setOnClickListener { binding!!.pxerView.redo() }
        binding!!.fabDropper.setOnClickListener {
            if (binding!!.pxerView.mode == PxerView.Mode.Dropper) {
                binding!!.fabUndo.show(true)
                binding!!.fabRedo.show(true)

                binding!!.toolsFab.show(true)

                binding!!.pxerView.mode = previousMode

                binding!!.fabDropper.setImageResource(R.drawable.ic_colorize_24dp)
            } else {
                binding!!.fabUndo.hide(true)
                binding!!.fabRedo.hide(true)

                binding!!.toolsFab.hide(true)
                if (binding!!.toolsView.visibility == View.VISIBLE)
                    binding!!.toolsFab.callOnClick()

                previousMode = binding!!.pxerView.mode
                binding!!.pxerView.mode = PxerView.Mode.Dropper

                binding!!.fabDropper.setImageResource(R.drawable.ic_close_24dp)
            }
        }
    }

    private fun setUpLayersView() {
        val layersBtn = findViewById<CardView>(R.id.layers_add)

        layersBtn.setOnClickListener {
            binding!!.pxerView.addLayer()
            layerItemAdapter.add(Math.max(binding!!.pxerView.currentLayer, 0), LayerThumbItem())
            layerAdapter.getSelectExtension().deselect()
            layerAdapter.getSelectExtension().select(binding!!.pxerView.currentLayer)
            layerItemAdapter.getAdapterItem(binding!!.pxerView.currentLayer).pressed()
            binding!!.layersRecycler.invalidate()
        }

        layerItemAdapter = ItemAdapter()
        layerAdapter = FastAdapter.with(layerItemAdapter)

        binding!!.layersRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding!!.layersRecycler.adapter = layerAdapter

        binding!!.layersRecycler.itemAnimator = DefaultItemAnimator()
        (binding!!.layersRecycler.itemAnimator as DefaultItemAnimator).changeDuration = 0
        (binding!!.layersRecycler.itemAnimator as DefaultItemAnimator).addDuration = 0
        (binding!!.layersRecycler.itemAnimator as DefaultItemAnimator).removeDuration = 0

        val touchCallback = SimpleDragCallback(this)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding!!.layersRecycler)

        val selectExtension = layerAdapter.getSelectExtension()
        selectExtension.isSelectable = true
        selectExtension.multiSelect = false
        selectExtension.allowDeselection = false

        layerAdapter.onLongClickListener = { _, _, item, position ->
            selectExtension.deselect()
            selectExtension.select(position)

            item.pressed()
            false
        }
        layerAdapter.onClickListener = { v, _, item, position ->
            for (_item in layerItemAdapter.adapterItems)
                if (!_item.isSelected)
                    _item.pressedTime = 0

            if (onlyShowSelected) {
                val layer = binding!!.pxerView.pxerLayers[binding!!.pxerView.currentLayer]
                layer!!.visible = false
                binding!!.pxerView.invalidate()

                layerAdapter.notifyAdapterItemChanged(binding!!.pxerView.currentLayer)
            }
            binding!!.pxerView.currentLayer = position
            if (onlyShowSelected) {
                val layer = binding!!.pxerView.pxerLayers[binding!!.pxerView.currentLayer]
                layer!!.visible = true
                binding!!.pxerView.invalidate()

                layerAdapter.notifyAdapterItemChanged(binding!!.pxerView.currentLayer)
            }
            item.pressed()
            if (item.isPressSecondTime) {
                val popupMenu = PopupMenu(this@DrawingActivity, v!!)
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

        binding!!.pxerView.moveLayer(oldPosition, newPosition)

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
        binding!!.pxerView.currentLayer = newPosition
    }

    fun onLayerUpdate() {
        layerItemAdapter.clear()
        for (i in 0 until binding!!.pxerView.pxerLayers.size) {
            layerItemAdapter.add(LayerThumbItem())
        }
        layerAdapter.getSelectExtension().select(0)
        layerItemAdapter.getAdapterItem(0).pressed()
    }

    fun onLayerRefresh() {
        if (binding != null)
            binding!!.layersRecycler.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_drawing, menu)
        return super.onCreateOptionsMenu(menu)
    }

    val myFilter: FileFilter =
        { it.isDirectory || it.name.endsWith(PxerView.PXER_EXTENSION_NAME, true) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.onlyshowselectedlayer -> {
                onlyShowSelected = true
                binding!!.pxerView.visibilityAllLayer(false)

                val layer2 = binding!!.pxerView.pxerLayers[binding!!.pxerView.currentLayer]
                layer2!!.visible = true
                binding!!.pxerView.invalidate()

                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.export -> PngExportable().runExport(this, binding!!.pxerView)
            R.id.exportgif -> GifExportable().runExport(this, binding!!.pxerView)
            R.id.exportfolder -> FolderExportable().runExport(this, binding!!.pxerView)
            R.id.exportatlas -> AtlasExportable().runExport(this, binding!!.pxerView)
            R.id.save -> binding!!.pxerView.save(true)
            R.id.projectm -> openProjectManager()
            R.id.open ->
                MaterialDialog(this).show {
                    fileChooser(
                        filter = myFilter,
                        initialDirectory = File(
                            context.getExternalFilesDir("/")!!,
                            "/PxerStudio/Project"
                        ),
                        context = context
                    ) { dialog, file ->
                        binding!!.pxerView.loadProject(file)
                        setTitle(Tool.stripExtension(file.name), false)
                        currentProjectPath = file.path
                    }
                }
            R.id.newp -> createNewProject()
            R.id.resetvp -> binding!!.pxerView.resetViewPort()
            R.id.hidealllayers -> run {
                if (onlyShowSelected) return@run
                binding!!.pxerView.visibilityAllLayer(false)
                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.showalllayers -> {
                onlyShowSelected = false
                binding!!.pxerView.visibilityAllLayer(true)
                layerAdapter.notifyAdapterDataSetChanged()
            }
            R.id.gridonoff -> {
                if (binding!!.pxerView.showGrid)
                    item.setIcon(R.drawable.ic_grid_on_24dp)
                else
                    item.setIcon(R.drawable.ic_grid_off_24dp)
                binding!!.pxerView.showGrid = !binding!!.pxerView.showGrid
            }
            R.id.layers -> {
                binding!!.layerView.pivotX = (binding!!.layerView!!.width / 2).toFloat()
                binding!!.layerView.pivotY = 0f
                if (binding!!.layerView.visibility == View.VISIBLE) {
                    binding!!.layerView
                        .animate()
                        .setDuration(100)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .alpha(0f)
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .withEndAction {
                            binding!!.layerView.visibility = View.INVISIBLE
                        }
                } else {
                    binding!!.layerView.visibility = View.VISIBLE
                    binding!!.layerView
                        .animate()
                        .setDuration(100)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
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
                if (binding!!.pxerView.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.deletelayer).message(R.string.deletelayerwarning)
                    .positiveButton(R.string.delete).positiveButton {
                        if (!isEdited)
                            isEdited = true

                        layerItemAdapter.remove(binding!!.pxerView.currentLayer)
                        binding!!.pxerView.removeCurrentLayer()

                        layerAdapter.getSelectExtension().deselect()
                        layerAdapter.getSelectExtension().select(binding!!.pxerView.currentLayer)
                        layerItemAdapter.getAdapterItem(binding!!.pxerView.currentLayer).pressed()
                        layerAdapter.notifyAdapterDataSetChanged()
                    }.show()
            }
            R.id.copypastelayer -> {
                binding!!.pxerView.copyAndPasteCurrentLayer()
                layerItemAdapter.add(Math.max(binding!!.pxerView.currentLayer, 0), LayerThumbItem())
                layerAdapter.getSelectExtension().deselect()
                layerAdapter.getSelectExtension().select(binding!!.pxerView.currentLayer)
                layerItemAdapter.getAdapterItem(binding!!.pxerView.currentLayer).pressed()
                binding!!.layersRecycler.invalidate()
            }
            R.id.mergealllayer -> run {
                if (binding!!.pxerView.pxerLayers.size <= 1) return@run
                Tool.prompt(this).title(R.string.mergealllayers)
                    .message(R.string.mergealllayerswarning).positiveButton(R.string.merge)
                    .positiveButton {
                        if (!isEdited)
                            isEdited = true

                        binding!!.pxerView.mergeAllLayers()
                        layerItemAdapter.clear()
                        layerItemAdapter.add(LayerThumbItem())
                        layerAdapter.getSelectExtension().deselect()
                        layerAdapter.getSelectExtension().select(0)
                        layerItemAdapter.getAdapterItem(0).pressed()
                    }.show()
            }
            R.id.about -> startActivity(Intent(this@DrawingActivity, AboutActivity::class.java))
            R.id.tvisibility -> run {
                if (onlyShowSelected) return@run
                val layer = binding!!.pxerView.pxerLayers[binding!!.pxerView.currentLayer]
                layer!!.visible = !layer!!.visible
                binding!!.pxerView.invalidate()
                layerAdapter.notifyAdapterItemChanged(binding!!.pxerView.currentLayer)
            }
            R.id.clearlayer -> Tool.prompt(this)
                .title(R.string.clearcurrentlayer)
                .message(R.string.clearcurrentlayerwarning)
                .positiveButton(R.string.clear)
                .positiveButton { binding!!.pxerView.clearCurrentLayer() }.show()
            R.id.mergedown -> run {
                if (binding!!.pxerView.currentLayer == binding!!.pxerView.pxerLayers.size - 1) return@run
                Tool.prompt(this)
                    .title(R.string.mergedownlayer)
                    .message(R.string.mergedownlayerwarning)
                    .positiveButton(R.string.merge)
                    .positiveButton {
                        binding!!.pxerView.mergeDownLayer()
                        layerItemAdapter.remove(binding!!.pxerView.currentLayer + 1)
                        layerAdapter.getSelectExtension().select(binding!!.pxerView.currentLayer)
                        layerItemAdapter.getAdapterItem(binding!!.pxerView.currentLayer).pressed()
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openProjectManager() {
        binding!!.pxerView.save(false)
        startActivityForResult(Intent(this, ProjectManagerActivity::class.java), 659)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == 659 && data != null) {
            val path = data.getStringExtra("selectedProjectPath")
            if (path != null && !path.isEmpty()) {
                currentProjectPath = path
                val file = File(path)
                if (file.exists()) {
                    binding!!.pxerView.loadProject(file)
                    setTitle(Tool.stripExtension(file.name), false)
                }
            } else if (data.getBooleanExtra("fileNameChanged", false)) {
                currentProjectPath = ""
                binding!!.pxerView.projectName = ""
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNewProject() {
        val l = layoutInflater.inflate(
            R.layout.dialog_activity_drawing_newproject,
            null
        ) as ConstraintLayout
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

        MaterialDialog(this)
//            .typeface(Tool.myType, Tool.myType)
            .customView(view = l)
            .title(R.string.newproject)
            .positiveButton(R.string.create)
            .negativeButton(R.string.cancel)
            .positiveButton {
                if (!editText.text.toString().isEmpty()) {
                    setTitle(editText.text.toString(), true)
                    binding!!.pxerView.createBlankProject(
                        editText.text.toString(),
                        seekBar.progress + 1,
                        seekBar2.progress + 1
                    )
                }
            }
            .show()
        binding!!.pxerView.save(false)
    }


    override fun onStop() {
        saveState()
        super.onStop()
    }

    private fun saveState() {
        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        pxerPref.edit()
            .putString("lastOpenedProject", currentProjectPath)
            .putInt("lastUsedColor", binding!!.pxerView.selectedColor)
            .apply()
        if (!binding!!.pxerView.projectName.isNullOrEmpty() || binding!!.pxerView.projectName != UNTITLED)
            binding!!.pxerView.save(false)
        else
            binding!!.pxerView.save(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cp.onConfigChanges()
    }

    private inner class LayerThumbItem : AbstractItem<LayerThumbItem.ViewHolder>() {
        var pressedTime = 0

        val isPressSecondTime: Boolean
            get() = pressedTime == 2

        fun pressed() {
            pressedTime++
            pressedTime = Math.min(2, pressedTime)
        }

        override val type: Int
            get() = R.id.item_layer_thumb

        override val layoutRes: Int
            get() = R.layout.item_layer_thumb


//        override fun withSetSelected(selected: Boolean): LayerThumbItem {
//            if (!selected)
//                pressedTime = 0
//            return super.withSetSelected(selected)
//        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        inner class ViewHolder(view: View) : FastAdapter.ViewHolder<LayerThumbItem>(view) {
            var iv: FastBitmapView = view as FastBitmapView

            override fun bindView(item: LayerThumbItem, payloads: List<Any>) {
                iv.isSelected = item.isSelected

                val layer = binding!!.pxerView.pxerLayers[layoutPosition]
                iv.setVisible(layer!!.visible)
                iv.bitmap = layer!!.bitmap
            }

            override fun unbindView(item: LayerThumbItem) {

            }
        }
    }

    private inner class ToolItem(var icon: Int) : AbstractItem<ToolItem.ViewHolder>() {

        override val type: Int
            get() = R.id.item_tool

        override val layoutRes: Int
            get() = R.layout.item_tool

//        override fun isSelectable(): Boolean {
//            return true
//        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        inner class ViewHolder(view: View) : FastAdapter.ViewHolder<ToolItem>(view) {
            var iv: ImageView = view as ImageView

            override fun bindView(item: ToolItem, payloads: List<Any>) {
                if (isSelected)
                    iv.alpha = 1f
                else
                    iv.alpha = 0.3f

                iv.setImageResource(item.icon)
            }

            override fun unbindView(item: ToolItem) {

            }
        }
    }
}
