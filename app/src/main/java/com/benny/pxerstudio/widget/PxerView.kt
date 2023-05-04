@file:Suppress("PrintStackTrace")

package com.benny.pxerstudio.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Environment
import android.os.SystemClock
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.benny.pxerstudio.R
import com.benny.pxerstudio.activity.DrawingActivity
import com.benny.pxerstudio.activity.DrawingActivity.Companion.UNTITLED
import com.benny.pxerstudio.activity.DrawingActivity.Companion.currentProjectPath
import com.benny.pxerstudio.shape.BaseShape
import com.benny.pxerstudio.util.PreviewSaver
import com.benny.pxerstudio.util.displayToast
import com.benny.pxerstudio.util.freeMemory
import com.benny.pxerstudio.util.prompt
import com.benny.pxerstudio.util.saveProject
import com.benny.pxerstudio.util.stripExtension
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by BennyKok on 10/3/2016.
 */
class PxerView : View, OnScaleGestureListener, GestureDetector.OnGestureListener {
    val pxerLayers = ArrayList<PxerLayer?>()

    // Drawing property
    private var pxerPaint: Paint? = null
    var selectedColor = Color.YELLOW
    var mode = Mode.Normal
    var shapeTool: BaseShape? = null
    var currentLayer = 0
        set(value) {
            field = value
            invalidate()
        }
    var showGrid = false
        set(value) {
            field = value
            invalidate()
        }

    private var isUnrecordedChanges = false

    // Picture property
    var projectName: String? = UNTITLED
    private var borderPaint: Paint? = null

    // private val rects: Array<Array<Rect>>? = null
    var picWidth = 0
        private set
    var picHeight = 0
        private set
    private var pxerSize = 0f
    private var picBoundary: RectF? = null
    private val picRect = Rect()
    private val grid = Path()
    private var bgbitmap: Bitmap? = null
    val previewCanvas = Canvas()
    var preview: Bitmap? = null
        private set

    // Control property
    private var points: Array<Point?>? = null
    private var downY = 0
    private var downX = 0
    private var downInPic = false
    private val drawMatrix = Matrix()
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var mScaleFactor = 1f
    private var prePressedTime = -1L

    // History property
    private val history = ArrayList<ArrayList<PxerHistory>?>()
    private val redohistory = ArrayList<ArrayList<PxerHistory>?>()
    private val historyIndex = ArrayList<Int?>()
    val currentHistory = ArrayList<Pxer>()

    // Callback
    private var dropperCallBack: OnDropperCallBack? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun setDropperCallBack(dropperCallBack: OnDropperCallBack?) {
        this.dropperCallBack = dropperCallBack
    }

    fun copyAndPasteCurrentLayer() {
        val bitmap = pxerLayers[currentLayer]!!.bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        pxerLayers.add(max(currentLayer, 0), PxerLayer(bitmap))
        history.add(max(currentLayer, 0), ArrayList())
        redohistory.add(max(currentLayer, 0), ArrayList())
        historyIndex.add(max(currentLayer, 0), 0)
    }

    fun addLayer() {
        val bitmap = createBitmap(picWidth, picHeight)
        bitmap.eraseColor(Color.TRANSPARENT)
        pxerLayers.add(max(currentLayer, 0), PxerLayer(bitmap))
        history.add(max(currentLayer, 0), ArrayList())
        redohistory.add(max(currentLayer, 0), ArrayList())
        historyIndex.add(max(currentLayer, 0), 0)
    }

    fun removeCurrentLayer() {
        pxerLayers.removeAt(currentLayer)
        history.removeAt(currentLayer)
        redohistory.removeAt(currentLayer)
        historyIndex.removeAt(currentLayer)
        currentLayer = (max(0, currentLayer - 1))
        invalidate()
    }

    fun moveLayer(from: Int, to: Int) {
        Collections.swap(pxerLayers, from, to)
        Collections.swap(history, from, to)
        Collections.swap(redohistory, from, to)
        Collections.swap(historyIndex, from, to)
        invalidate()
    }

    fun clearCurrentLayer() {
        pxerLayers[currentLayer]!!.bitmap!!.eraseColor(Color.TRANSPARENT)
    }

    fun mergeDownLayer() {
        preview!!.eraseColor(Color.TRANSPARENT)
        previewCanvas.setBitmap(preview)
        previewCanvas.drawBitmap(pxerLayers[currentLayer + 1]!!.bitmap!!, 0f, 0f, null)
        previewCanvas.drawBitmap(pxerLayers[currentLayer]!!.bitmap!!, 0f, 0f, null)
        pxerLayers.removeAt(currentLayer + 1)
        history.removeAt(currentLayer + 1)
        redohistory.removeAt(currentLayer + 1)
        historyIndex.removeAt(currentLayer + 1)
        pxerLayers[currentLayer] = PxerLayer(Bitmap.createBitmap(preview!!))
        history[currentLayer] = ArrayList()
        redohistory[currentLayer] = ArrayList()
        historyIndex[currentLayer] = 0
        invalidate()
    }

    fun visibilityAllLayer(visible: Boolean) {
        for (i in pxerLayers.indices) {
            pxerLayers[i]!!.visible = visible
        }
        invalidate()
    }

    fun mergeAllLayers() {
        preview!!.eraseColor(Color.TRANSPARENT)
        previewCanvas.setBitmap(preview)
        for (i in pxerLayers.indices) {
            previewCanvas.drawBitmap(pxerLayers[pxerLayers.size - i - 1]!!.bitmap!!, 0f, 0f, null)
        }
        pxerLayers.clear()
        history.clear()
        redohistory.clear()
        historyIndex.clear()
        pxerLayers.add(PxerLayer(Bitmap.createBitmap(preview!!)))
        history.add(ArrayList())
        redohistory.add(ArrayList())
        historyIndex.add(0)
        currentLayer = (0)
        invalidate()
    }

    fun createBlankProject(name: String?, picWidth: Int, picHeight: Int) {
        projectName = name
        this.picWidth = picWidth
        this.picHeight = picHeight
        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points!![i * picHeight + j] = Point(i, j)
            }
        }
        val bitmap = createBitmap(picWidth, picHeight)
        bitmap.eraseColor(Color.TRANSPARENT)
        pxerLayers.clear()
        pxerLayers.add(PxerLayer(bitmap))
        onLayerUpdate()
        mScaleFactor = 1f
        drawMatrix.reset()
        initPxerInfo()
        history.clear()
        redohistory.clear()
        historyIndex.clear()
        history.add(ArrayList())
        redohistory.add(ArrayList())
        historyIndex.add(0)
        currentLayer = (0)
        reCalBackground()
        freeMemory()
    }

    fun loadProject(file: File): Boolean {
        val gson = Gson()
        val out = ArrayList<PxableLayer>()
        try {
            val reader = JsonReader(InputStreamReader(FileInputStream(File(file.path))))
            reader.beginArray()
            while (reader.hasNext()) {
                val layer = gson.fromJson<PxableLayer>(reader, PxableLayer::class.java)
                out.add(layer)
            }
            reader.endArray()
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            context.prompt()
                .message(R.string.error_loading_project, null, null)
                .title(R.string.error_something_went_wrong, null)
                .negativeButton(null, null, null)
                // .positiveColor(Color.GRAY)
                .positiveButton(android.R.string.ok, null, null).show()
            return false
        }
        picWidth = out[0].width
        picHeight = out[0].height
        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points!![i * picHeight + j] = Point(i, j)
            }
        }
        history.clear()
        redohistory.clear()
        historyIndex.clear()
        pxerLayers.clear()
        for (i in out.indices) {
            val bitmap = createBitmap(picWidth, picHeight)
            history.add(ArrayList())
            redohistory.add(ArrayList())
            historyIndex.add(0)
            val layer = PxerLayer(bitmap)
            layer.visible = out[i].visible
            pxerLayers.add(layer)
            for (x in out[i].pxers.indices) {
                val p = out[i].pxers[x]
                pxerLayers[i]!!.bitmap!!.setPixel(p.x, p.y, p.color)
            }
        }
        onLayerUpdate()
        projectName = file.name.stripExtension()
        mScaleFactor = 1f
        drawMatrix.reset()
        initPxerInfo()
        currentLayer = (0)
        reCalBackground()
        invalidate()
        freeMemory()
        return true
    }

    fun undo() {
        if (historyIndex[currentLayer]!! <= 0) {
            context.displayToast(R.string.no_more_undo)
            return
        }
        historyIndex[currentLayer] = historyIndex[currentLayer]!! - 1
        for (i in history[currentLayer]!![historyIndex[currentLayer]!!].pxers.indices) {
            val pxer = history[currentLayer]!![historyIndex[currentLayer]!!].pxers[i]
            currentHistory.add(
                Pxer(
                    pxer.x,
                    pxer.y,
                    pxerLayers[currentLayer]!!.bitmap!!.getPixel(pxer.x, pxer.y),
                ),
            )
            val coord = history[currentLayer]!![historyIndex[currentLayer]!!].pxers[i]
            pxerLayers[currentLayer]!!.bitmap!!.setPixel(coord.x, coord.y, coord.color)
        }
        redohistory[currentLayer]!!.add(PxerHistory(cloneList(currentHistory)))
        currentHistory.clear()
        history[currentLayer]!!.removeAt(history[currentLayer]!!.size - 1)
        invalidate()
    }

    fun redo() {
        if (redohistory[currentLayer]!!.size <= 0) {
            context.displayToast(R.string.no_more_redo)
            return
        }
        for (i in redohistory[currentLayer]!![redohistory[currentLayer]!!.size - 1].pxers.indices) {
            var pxer = redohistory[currentLayer]!![redohistory[currentLayer]!!.size - 1].pxers[i]
            currentHistory.add(
                Pxer(
                    pxer.x,
                    pxer.y,
                    pxerLayers[currentLayer]!!.bitmap!!.getPixel(pxer.x, pxer.y),
                ),
            )
            pxer = redohistory[currentLayer]!![redohistory[currentLayer]!!.size - 1].pxers[i]
            pxerLayers[currentLayer]!!.bitmap!!.setPixel(pxer.x, pxer.y, pxer.color)
        }
        historyIndex[currentLayer] = historyIndex[currentLayer]!! + 1
        history[currentLayer]!!.add(PxerHistory(cloneList(currentHistory)))
        currentHistory.clear()
        redohistory[currentLayer]!!.removeAt(redohistory[currentLayer]!!.size - 1)
        invalidate()
    }

    fun save(force: Boolean): Boolean {
        return if (projectName == null || projectName!!.isEmpty()) {
            if (force) {
                MaterialDialog(context)
//                .titleGravity(GravityEnum.CENTER)
//                .inputRange(0, 20)
                    .title(R.string.save_project)
                    .input(context.getString(R.string.name), inputType = InputType.TYPE_CLASS_TEXT) { _, text ->
                        projectName = "$text"
                        if (context is DrawingActivity) {
                            (context as DrawingActivity).setTitle(
                                projectName,
                                false,
                            )
                        }
                        save(true)
                    }
                    .positiveButton(R.string.save)
                    .show()
            }
            false
        } else {
            (context as DrawingActivity).isEdited = false
            val gson = Gson()
            val out = ArrayList<PxableLayer>()
            for (i in pxerLayers.indices) {
                val pxableLayer = PxableLayer()
                pxableLayer.height = picHeight
                pxableLayer.width = picWidth
                pxableLayer.visible = pxerLayers[i]!!.visible
                out.add(pxableLayer)
                for (x in 0 until pxerLayers[i]!!.bitmap!!.width) {
                    for (y in 0 until pxerLayers[i]!!.bitmap!!.height) {
                        val pc = pxerLayers[i]!!.bitmap!!.getPixel(x, y)
                        if (pc != Color.TRANSPARENT) {
                            out[i].pxers.add(Pxer(x, y, pc))
                        }
                    }
                }
            }
            currentProjectPath =
                Environment.getExternalStorageDirectory().path + "/PxerStudio/Project/" + projectName + ".pxer"
            if (context is DrawingActivity) {
                (context as DrawingActivity).setTitle(
                    projectName,
                    false,
                )
            }
            context.saveProject(projectName + PXER_EXTENSION_NAME, gson.toJson(out))
            PreviewSaver.saveTo(
                File(context.getExternalFilesDir("/")!!.path + "/PxerStudio/Project", projectName + ".png"),
                picWidth,
                picHeight,
                this,
            )
            true
        }
    }

    fun resetViewPort() {
        scaleAtFirst()
    }

    private fun init() {
        mScaleDetector = ScaleGestureDetector(context, this)
        mGestureDetector = GestureDetector(context, this)
        setWillNotDraw(false)
        borderPaint = Paint()
        borderPaint!!.isAntiAlias = true
        borderPaint!!.style = Paint.Style.STROKE
        borderPaint!!.strokeWidth = 1f
        borderPaint!!.color = Color.DKGRAY
        pxerPaint = Paint()
        pxerPaint!!.isAntiAlias = true
        picBoundary = RectF(0f, 0f, 0f, 0f)

        // Create a 40 x 40 project
        picWidth = 40
        picHeight = 40
        points = arrayOfNulls(picWidth * picHeight)
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight) {
                points!![i * picHeight + j] = Point(i, j)
            }
        }
        val bitmap = createBitmap(picWidth, picHeight)
        bitmap.eraseColor(Color.TRANSPARENT)
        pxerLayers.clear()
        pxerLayers.add(PxerLayer(bitmap))
        history.add(ArrayList())
        redohistory.add(ArrayList())
        historyIndex.add(0)
        reCalBackground()
        resetViewPort()

        // Avoid unknown flicking issue if the user scale the canvas immediately
        val downTime = SystemClock.uptimeMillis()
        val eventTime = downTime + 100
        val x = 0.0f
        val y = 0.0f
        val metaState = 0
        val motionEvent =
            MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState)
        mGestureDetector!!.onTouchEvent(motionEvent)
    }

    private fun reCalBackground() {
        preview = createBitmap(picWidth, picHeight)
        bgbitmap = createBitmap(picWidth * 2, picHeight * 2)
        bgbitmap!!.eraseColor(ColorUtils.setAlphaComponent(Color.WHITE, 200))
        for (i in 0 until picWidth) {
            for (j in 0 until picHeight * 2) {
                if (j % 2 != 0) {
                    bgbitmap!!.setPixel(
                        i * 2 + 1,
                        j,
                        Color.argb(200, 220, 220, 220),
                    )
                } else {
                    bgbitmap!!.setPixel(i * 2, j, Color.argb(200, 220, 220, 220))
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) mGestureDetector!!.onTouchEvent(event)
        mScaleDetector!!.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            downInPic = false
            if (mode == Mode.ShapeTool) shapeTool!!.onDrawEnd(this)
            if (mode != Mode.Fill && mode != Mode.Dropper && mode != Mode.ShapeTool) {
                finishAddHistory()
            }
        }
        if (event.pointerCount > 1) {
            prePressedTime = -1L
            mGestureDetector!!.onTouchEvent(event)
            return true
        }
        // Get the position
        val mX = event.x
        val mY = event.y
        val raw = FloatArray(9)
        drawMatrix.getValues(raw)
        val scaledWidth = picBoundary!!.width() * mScaleFactor
        val scaledHeight = picBoundary!!.height() * mScaleFactor
        picRect[
            raw[Matrix.MTRANS_X].toInt(),
            raw[Matrix.MTRANS_Y].toInt(),
            (raw[Matrix.MTRANS_X] + scaledWidth).toInt(),
        ] =
            (raw[Matrix.MTRANS_Y] + scaledHeight).toInt()
        if (!picRect.contains(mX.toInt(), mY.toInt())) {
            return true
        }
        val x = ((mX - picRect.left) / scaledWidth * picWidth).toInt()
        val y = ((mY - picRect.top) / scaledHeight * picHeight).toInt()
        // We got x and y
        if (event.action == MotionEvent.ACTION_MOVE) {
            if (prePressedTime != -1L && System.currentTimeMillis() - prePressedTime <= pressDelay) return true
            if (prePressedTime == -1L) return true
        }
        if (!isValid(x, y)) return true
        if (event.action == MotionEvent.ACTION_DOWN) {
            downY = y
            downX = x
            downInPic = true
            prePressedTime = System.currentTimeMillis()
        }
        if (mode == Mode.ShapeTool &&
            downX != -1 &&
            event.action != MotionEvent.ACTION_UP &&
            event.action != MotionEvent.ACTION_DOWN
        ) {
            if (!shapeTool!!.hasEnded()) shapeTool!!.onDraw(this, downX, downY, x, y)
            return true
        }
        val pxer: Pxer
        val bitmapToDraw = pxerLayers[currentLayer]!!.bitmap
        if (event.action != MotionEvent.ACTION_UP) {
            pxer = Pxer(x, y, bitmapToDraw!!.getPixel(x, y))
            if (!currentHistory.contains(pxer)) currentHistory.add(pxer)
        }
        Log.v("shit", "$mode")
        when (mode) {
            Mode.Normal -> run {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) return@run
                bitmapToDraw!!.setPixel(
                    x,
                    y,
                    ColorUtils.compositeColors(selectedColor, bitmapToDraw.getPixel(x, y)),
                )
                Log.v("shit", "$mode")
                setUnrecordedChanges(true)
            }
            Mode.Dropper -> run {
                if (event.action == MotionEvent.ACTION_DOWN) return@run
                if (x == downX && downY == y) {
                    var i = 0
                    while (i < pxerLayers.size) {
                        val pixel = pxerLayers[i]!!.bitmap!!.getPixel(x, y)
                        if (pixel != Color.TRANSPARENT) {
                            selectedColor = pxerLayers[i]!!.bitmap!!.getPixel(x, y)
                            if (dropperCallBack != null) {
                                dropperCallBack!!.onColorDropped(selectedColor)
                            }
                            break
                        }
                        if (i == pxerLayers.size - 1) {
                            if (dropperCallBack != null) {
                                dropperCallBack!!.onColorDropped(Color.TRANSPARENT)
                            }
                        }
                        i++
                    }
                }
            }
            Mode.Fill -> run {
                // The fill tool is brought to us with aid by some open source project online :( I forgot the name
                if (event.action == MotionEvent.ACTION_UP && x == downX && downY == y) {
                    freeMemory()
                    val targetColor = bitmapToDraw!!.getPixel(x, y)
                    val toExplore: Queue<Point?> = LinkedList()
                    val explored = HashSet<Point?>()
                    toExplore.add(Point(x, y))
                    while (!toExplore.isEmpty()) {
                        val p = toExplore.remove()
                        // Color it
                        currentHistory.add(Pxer(p!!.x, p.y, targetColor))
                        bitmapToDraw.setPixel(
                            p.x,
                            p.y,
                            ColorUtils.compositeColors(
                                selectedColor,
                                bitmapToDraw.getPixel(p.x, p.y),
                            ),
                        )
                        //
                        var cp: Point?
                        if (isValid(p.x, p.y - 1)) {
                            cp = points!![p.x * picHeight + p.y - 1]
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(
                                        cp!!.x,
                                        cp.y,
                                    ) == targetColor
                                ) {
                                    toExplore.add(cp)
                                }
                                explored.add(cp)
                            }
                        }
                        if (isValid(p.x, p.y + 1)) {
                            cp = points!![p.x * picHeight + p.y + 1]
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(
                                        cp!!.x,
                                        cp.y,
                                    ) == targetColor
                                ) {
                                    toExplore.add(cp)
                                }
                                explored.add(cp)
                            }
                        }
                        if (isValid(p.x - 1, p.y)) {
                            cp = points!![(p.x - 1) * picHeight + p.y]
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(
                                        cp!!.x,
                                        cp.y,
                                    ) == targetColor
                                ) {
                                    toExplore.add(cp)
                                }
                                explored.add(cp)
                            }
                        }
                        if (isValid(p.x + 1, p.y)) {
                            cp = points!![(p.x + 1) * picHeight + p.y]
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(
                                        cp!!.x,
                                        cp.y,
                                    ) == targetColor
                                ) {
                                    toExplore.add(cp)
                                }
                                explored.add(cp)
                            }
                        }
                    }
                    setUnrecordedChanges(true)
                    finishAddHistory()
                }
            }
            Mode.Eraser -> run {
            }
            Mode.ShapeTool -> run {
            }
        }
        invalidate()
        return true
    }

    fun finishAddHistory() {
        if (currentHistory.size > 0 && isUnrecordedChanges) {
            isUnrecordedChanges = false
            redohistory[currentLayer]!!.clear()
            historyIndex[currentLayer] = historyIndex[currentLayer]!! + 1
            history[currentLayer]!!.add(PxerHistory(cloneList(currentHistory)))
            currentHistory.clear()
        }
    }

    private fun isValid(x: Int, y: Int): Boolean {
        return x >= 0 && x <= picWidth - 1 && y >= 0 && y <= picHeight - 1
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.DKGRAY)
        canvas.save()
        canvas.concat(drawMatrix)
        canvas.drawBitmap(bgbitmap!!, null, picBoundary!!, null)
        for (i in pxerLayers.size - 1 downTo -1 + 1) {
            if (pxerLayers[i]!!.visible) {
                canvas.drawBitmap(
                    pxerLayers[i]!!.bitmap!!,
                    null,
                    picBoundary!!,
                    null,
                )
            }
        }
        if (showGrid) canvas.drawPath(grid, borderPaint!!)
        canvas.restore()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        initPxerInfo()
    }

    private fun initPxerInfo() {
        val length = min(height, width)
        pxerSize = (length / 40).toFloat()
        picBoundary!![0f, 0f, pxerSize * picWidth] = pxerSize * picHeight
        scaleAtFirst()
        grid.reset()
        for (x in 0 until picWidth + 1) {
            val posx = picBoundary!!.left + pxerSize * x
            grid.moveTo(posx, picBoundary!!.top)
            grid.lineTo(posx, picBoundary!!.bottom)
        }
        for (y in 0 until picHeight + 1) {
            val posy = picBoundary!!.top + pxerSize * y
            grid.moveTo(picBoundary!!.left, posy)
            grid.lineTo(picBoundary!!.right, posy)
        }
    }

    override fun onDown(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent) {}

    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        motionEvent: MotionEvent,
        motionEvent1: MotionEvent,
        v: Float,
        v1: Float,
    ): Boolean {
        drawMatrix.postTranslate(-v, -v1)
        invalidate()
        return true
    }

    override fun onLongPress(motionEvent: MotionEvent) {}

    override fun onFling(
        motionEvent: MotionEvent,
        motionEvent1: MotionEvent,
        v: Float,
        v1: Float,
    ): Boolean {
        return false
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val scale = scaleGestureDetector.scaleFactor
        mScaleFactor *= scale
        val transformationMatrix = Matrix()
        val focusX = scaleGestureDetector.focusX
        val focusY = scaleGestureDetector.focusY
        transformationMatrix.postTranslate(-focusX, -focusY)
        transformationMatrix.postScale(
            scaleGestureDetector.scaleFactor,
            scaleGestureDetector.scaleFactor,
        )
        transformationMatrix.postTranslate(focusX, focusY)
        drawMatrix.postConcat(transformationMatrix)
        invalidate()
        return true
    }

    private fun scaleAtFirst() {
        // int width = Math.max(getWidth(),getHeight()),height = Math.min(getWidth(),getHeight());
        mScaleFactor = 1f
        drawMatrix.reset()
        val scale = 0.98f
        mScaleFactor = scale
        val transformationMatrix = Matrix()
        transformationMatrix.postTranslate(
            (width - picBoundary!!.width()) / 2,
            (height - picBoundary!!.height()) / 3,
        )
        val focusX = (width / 2).toFloat()
        val focusY = (height / 2).toFloat()
        transformationMatrix.postTranslate(-focusX, -focusY)
        transformationMatrix.postScale(scale, scale)
        transformationMatrix.postTranslate(focusX, focusY)
        drawMatrix.postConcat(transformationMatrix)
        invalidate()
    }

    private fun onLayerUpdate() {
        (context as DrawingActivity).onLayerUpdate()
    }

    override fun invalidate() {
        (context as DrawingActivity).onLayerRefresh()
        super.invalidate()
    }

    override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {}

    fun setUnrecordedChanges(unrecordedChanges: Boolean) {
        isUnrecordedChanges = unrecordedChanges
        if (!(context as DrawingActivity).isEdited) {
            (context as DrawingActivity).isEdited =
                isUnrecordedChanges
        }
    }

    enum class Mode {
        Normal, Eraser, Fill, Dropper, ShapeTool
    }

    interface OnDropperCallBack {
        fun onColorDropped(newColor: Int)
    }

    class PxerLayer(@JvmField var bitmap: Bitmap?) {
        @JvmField
        var visible = true
    }

    class PxableLayer {
        var width = 0
        var height = 0
        var visible = false
        var pxers = ArrayList<Pxer>()
    }

    data class Pxer(var x: Int, var y: Int, var color: Int) {
//        override fun clone(): Pxer {
//            return Pxer(x, y, color)
//        }

        override fun equals(other: Any?): Boolean {
            return (other as Pxer?)!!.x == x && other!!.y == y
        }
    }

    class PxerHistory(var pxers: ArrayList<Pxer>)
    companion object {
        const val PXER_EXTENSION_NAME = ".pxer"
        private const val pressDelay = 60L
        fun cloneList(list: List<Pxer>): ArrayList<Pxer> {
            val clone = ArrayList<Pxer>(list.size)
            list.mapTo(clone) { it.copy() }
            return clone
        }
    }
}
