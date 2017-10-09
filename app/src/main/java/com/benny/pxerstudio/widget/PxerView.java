package com.benny.pxerstudio.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.pxerstudio.activity.DrawingActivity;
import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.shape.BaseShape;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by BennyKok on 10/3/2016.
 */
public class PxerView extends View implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    public static final String PXER_EXTENTION_NAME = ".pxer";
    private final static Long pressDelay = 60L;
    private ArrayList<PxerLayer> pxerLayers = new ArrayList<>();

    //Drawing property
    private Paint pxerPaint;
    private int selectedColor = Color.YELLOW;
    private Mode mode = Mode.Normal;
    private BaseShape shapeTool;
    private int currentLayer = 0;
    private boolean showGrid;
    //Picture property
    private String projectName;
    private Paint borderPaint;
    private Rect[][] rects;
    private int picWidth;
    private int picHeight;
    private float pxerSize;
    private RectF picBoundary;
    private Rect picRect = new Rect();
    private Path grid = new Path();
    private Bitmap bgbitmap;
    private Canvas previewCanvas = new Canvas();
    private Bitmap preview;
    //Control property
    private Point[] points;
    private int downY, downX;
    private boolean downInPic;
    private Matrix drawMatrix = new Matrix();
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private float mScaleFactor = 1.f;
    private Long prePressedTime = -1L;
    //History property
    private ArrayList<ArrayList<PxerHistory>> history = new ArrayList<>();
    private ArrayList<ArrayList<PxerHistory>> redohistory = new ArrayList<>();
    private ArrayList<Integer> historyIndex = new ArrayList<>();
    private ArrayList<Pxer> currentHistory = new ArrayList<>();
    //Callback
    private OnDropperCallBack dropperCallBack;

    public PxerView(Context context) {
        super(context);

        init();
    }

    public PxerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public static ArrayList<Pxer> cloneList(List<Pxer> list) {
        ArrayList<Pxer> clone = new ArrayList<Pxer>(list.size());
        for (Pxer item : list) clone.add(item.clone());
        return clone;
    }

    public void setDropperCallBack(OnDropperCallBack dropperCallBack) {
        this.dropperCallBack = dropperCallBack;
    }

    public ArrayList<PxerLayer> getPxerLayers() {
        return pxerLayers;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public BaseShape getShapeTool() {
        return shapeTool;
    }

    public void setShapeTool(BaseShape shapeTool) {
        this.shapeTool = shapeTool;
    }

    public int getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(int currentLayer) {
        this.currentLayer = currentLayer;
        invalidate();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        invalidate();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getPicHeight() {
        return picHeight;
    }

    public int getPicWidth() {
        return picWidth;
    }

    public Canvas getPreviewCanvas() {
        return previewCanvas;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public ArrayList<Pxer> getCurrentHistory() {
        return currentHistory;
    }

    public void copyAndPasteCurrentLayer() {
        Bitmap bitmap = pxerLayers.get(currentLayer).bitmap.copy(Bitmap.Config.ARGB_8888, true);
        pxerLayers.add(Math.max(getCurrentLayer(), 0), new PxerLayer(bitmap));

        history.add(Math.max(getCurrentLayer(), 0), new ArrayList<PxerHistory>());
        redohistory.add(Math.max(getCurrentLayer(), 0), new ArrayList<PxerHistory>());
        historyIndex.add(Math.max(getCurrentLayer(), 0), 0);
    }

    public void addLayer() {
        Bitmap bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        pxerLayers.add(Math.max(getCurrentLayer(), 0), new PxerLayer(bitmap));

        history.add(Math.max(getCurrentLayer(), 0), new ArrayList<PxerHistory>());
        redohistory.add(Math.max(getCurrentLayer(), 0), new ArrayList<PxerHistory>());
        historyIndex.add(Math.max(getCurrentLayer(), 0), 0);
    }

    public void removeCurrentLayer() {
        getPxerLayers().remove(getCurrentLayer());

        history.remove(getCurrentLayer());
        redohistory.remove(getCurrentLayer());
        historyIndex.remove(getCurrentLayer());

        setCurrentLayer(Math.max(0, getCurrentLayer() - 1));
        invalidate();
    }

    public void moveLayer(int from, int to) {
        Collections.swap(getPxerLayers(), from, to);

        Collections.swap(history, from, to);
        Collections.swap(redohistory, from, to);
        Collections.swap(historyIndex, from, to);
        invalidate();
    }

    public void clearCurrentLayer() {
        getPxerLayers().get(currentLayer).bitmap.eraseColor(Color.TRANSPARENT);
    }

    public void mergeDownLayer() {
        getPreview().eraseColor(Color.TRANSPARENT);
        getPreviewCanvas().setBitmap(getPreview());

        getPreviewCanvas().drawBitmap(pxerLayers.get(getCurrentLayer() + 1).bitmap, 0, 0, null);
        getPreviewCanvas().drawBitmap(pxerLayers.get(getCurrentLayer()).bitmap, 0, 0, null);

        pxerLayers.remove(getCurrentLayer() + 1);
        history.remove(getCurrentLayer() + 1);
        redohistory.remove(getCurrentLayer() + 1);
        historyIndex.remove(getCurrentLayer() + 1);

        pxerLayers.set(getCurrentLayer(), new PxerLayer(Bitmap.createBitmap(getPreview())));
        history.set(getCurrentLayer(), new ArrayList<PxerHistory>());
        redohistory.set(getCurrentLayer(), new ArrayList<PxerHistory>());
        historyIndex.set(getCurrentLayer(), 0);

        invalidate();
    }

    public void visibilityAllLayer(boolean visible) {
        for (int i = 0; i < pxerLayers.size(); i++) {
            pxerLayers.get(i).visible = visible;
        }
        invalidate();
    }

    public void mergeAllLayers() {
        getPreview().eraseColor(Color.TRANSPARENT);
        getPreviewCanvas().setBitmap(getPreview());
        for (int i = 0; i < pxerLayers.size(); i++) {
            getPreviewCanvas().drawBitmap(pxerLayers.get(pxerLayers.size() - i - 1).bitmap, 0, 0, null);
        }
        pxerLayers.clear();
        history.clear();
        redohistory.clear();
        historyIndex.clear();

        pxerLayers.add(new PxerLayer(Bitmap.createBitmap(getPreview())));
        history.add(new ArrayList<PxerHistory>());
        redohistory.add(new ArrayList<PxerHistory>());
        historyIndex.add(0);

        setCurrentLayer(0);

        invalidate();
    }

    public void createBlankProject(String name, int picWidth, int picHeight) {
        this.projectName = name;
        this.picWidth = picWidth;
        this.picHeight = picHeight;

        points = new Point[picWidth * picHeight];
        for (int i = 0; i < picWidth; i++) {
            for (int j = 0; j < picHeight; j++) {
                points[i * picHeight + j] = new Point(i, j);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        pxerLayers.clear();
        pxerLayers.add(new PxerLayer(bitmap));
        onLayerUpdate();

        mScaleFactor = 1.f;
        drawMatrix.reset();
        initPxerInfo();

        history.clear();
        redohistory.clear();
        historyIndex.clear();

        history.add(new ArrayList<PxerHistory>());
        redohistory.add(new ArrayList<PxerHistory>());
        historyIndex.add(0);

        setCurrentLayer(0);

        reCalBackground();

        Tool.freeMemory();
    }

    public boolean loadProject(File file) {
        Gson gson = new Gson();

        ArrayList<PxableLayer> out = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(new File(file.getPath()))));
            reader.beginArray();
            while (reader.hasNext()) {
                PxerView.PxableLayer layer = gson.fromJson(reader, PxerView.PxableLayer.class);
                out.add(layer);
            }
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();

            Tool.prompt(getContext()).content(R.string.error_while_loading_project).title(R.string.something_went_wrong).negativeText("").positiveColor(Color.GRAY).positiveText(R.string.cancel).show();
            return false;
        }

        this.picWidth = out.get(0).width;
        this.picHeight = out.get(0).height;

        points = new Point[picWidth * picHeight];
        for (int i = 0; i < picWidth; i++) {
            for (int j = 0; j < picHeight; j++) {
                points[i * picHeight + j] = new Point(i, j);
            }
        }

        history.clear();
        redohistory.clear();
        historyIndex.clear();


        pxerLayers.clear();
        for (int i = 0; i < out.size(); i++) {
            Bitmap bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

            history.add(new ArrayList<PxerHistory>());
            redohistory.add(new ArrayList<PxerHistory>());
            historyIndex.add(0);

            PxerLayer layer = new PxerLayer(bitmap);
            layer.visible = out.get(i).visible;
            pxerLayers.add(layer);
            for (int x = 0; x < out.get(i).pxers.size(); x++) {
                Pxer p = out.get(i).pxers.get(x);
                pxerLayers.get(i).bitmap.setPixel(p.x, p.y, p.color);
            }
        }
        onLayerUpdate();
        this.projectName = Tool.stripExtension(file.getName());

        mScaleFactor = 1.f;
        drawMatrix.reset();
        initPxerInfo();

        setCurrentLayer(0);

        reCalBackground();
        invalidate();

        Tool.freeMemory();
        return true;
    }

    public void undo() {
        if (historyIndex.get(currentLayer) <= 0) {
            Tool.toast(getContext(), "No more undo");
            return;
        }

        historyIndex.set(currentLayer, historyIndex.get(currentLayer) - 1);
        for (int i = 0; i < history.get(currentLayer).get(historyIndex.get(currentLayer)).pxers.size(); i++) {
            Pxer pxer = history.get(currentLayer).get(historyIndex.get(currentLayer)).pxers.get(i);
            currentHistory.add(new Pxer(pxer.x, pxer.y, pxerLayers.get(currentLayer).bitmap.getPixel(pxer.x, pxer.y)));

            Pxer coord = history.get(currentLayer).get(historyIndex.get(currentLayer)).pxers.get(i);
            pxerLayers.get(currentLayer).bitmap.setPixel(coord.x, coord.y, coord.color);
        }
        redohistory.get(currentLayer).add(new PxerHistory(cloneList(currentHistory)));
        currentHistory.clear();

        history.get(currentLayer).remove(history.get(currentLayer).size() - 1);
        invalidate();
    }

    public void redo() {
        if (redohistory.get(currentLayer).size() <= 0) {
            Tool.toast(getContext(), "No more redo");
            return;
        }

        for (int i = 0; i < redohistory.get(currentLayer).get(redohistory.get(currentLayer).size() - 1).pxers.size(); i++) {
            Pxer pxer = redohistory.get(currentLayer).get(redohistory.get(currentLayer).size() - 1).pxers.get(i);
            currentHistory.add(new Pxer(pxer.x, pxer.y, pxerLayers.get(currentLayer).bitmap.getPixel(pxer.x, pxer.y)));

            pxer = redohistory.get(currentLayer).get(redohistory.get(currentLayer).size() - 1).pxers.get(i);
            pxerLayers.get(currentLayer).bitmap.setPixel(pxer.x, pxer.y, pxer.color);
        }
        historyIndex.set(currentLayer, historyIndex.get(currentLayer) + 1);

        history.get(currentLayer).add(new PxerHistory(cloneList(currentHistory)));
        currentHistory.clear();

        redohistory.get(currentLayer).remove(redohistory.get(currentLayer).size() - 1);
        invalidate();
    }

    public boolean save(boolean force) {
        if (projectName == null || projectName.isEmpty()) {
            if (force)
                new MaterialDialog.Builder(getContext())
                        .titleGravity(GravityEnum.CENTER)
                        .typeface(Tool.myType, Tool.myType)
                        .inputRange(0, 20)
                        .title(R.string.save_project)
                        .input(getContext().getString(R.string.name), null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                            }
                        })
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .positiveText(R.string.save)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                projectName = dialog.getInputEditText().getText().toString();
                                if (getContext() instanceof DrawingActivity)
                                    ((DrawingActivity) getContext()).setTitle(projectName, false);
                                save(true);
                            }
                        })
                        .show();
            return false;
        } else {
            ((DrawingActivity) getContext()).setEdited(false);
            Gson gson = new Gson();
            ArrayList<PxableLayer> out = new ArrayList<>();
            for (int i = 0; i < pxerLayers.size(); i++) {
                PxableLayer pxableLayer = new PxableLayer();
                pxableLayer.height = picHeight;
                pxableLayer.width = picWidth;
                pxableLayer.visible = pxerLayers.get(i).visible;
                out.add(pxableLayer);
                for (int x = 0; x < pxerLayers.get(i).bitmap.getWidth(); x++) {
                    for (int y = 0; y < pxerLayers.get(i).bitmap.getHeight(); y++) {
                        int pc = pxerLayers.get(i).bitmap.getPixel(x, y);
                        if (pc != Color.TRANSPARENT) {
                            out.get(i).pxers.add(new Pxer(x, y, pc));
                        }
                    }
                }
            }
            DrawingActivity.Companion.setCurrentProjectPath(Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Project/").concat(projectName + ".pxer"));
            if (getContext() instanceof DrawingActivity)
                ((DrawingActivity) getContext()).setTitle(projectName, false);
            Tool.saveProject(projectName + PXER_EXTENTION_NAME, gson.toJson(out));
            return true;
        }
    }

    public void resetViewPort() {
        scaleAtFirst();
    }

    private void init() {
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mGestureDetector = new GestureDetector(getContext(), this);

        setWillNotDraw(false);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        borderPaint.setColor(Color.DKGRAY);

        pxerPaint = new Paint();
        pxerPaint.setAntiAlias(true);

        picBoundary = new RectF(0, 0, 0, 0);

        //Create a 40 x 40 project
        this.picWidth = 40;
        this.picHeight = 40;

        points = new Point[picWidth * picHeight];
        for (int i = 0; i < picWidth; i++) {
            for (int j = 0; j < picHeight; j++) {
                points[i * picHeight + j] = new Point(i, j);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        pxerLayers.clear();
        pxerLayers.add(new PxerLayer(bitmap));

        history.add(new ArrayList<PxerHistory>());
        redohistory.add(new ArrayList<PxerHistory>());
        historyIndex.add(0);

        reCalBackground();
        resetViewPort();

        //Avoid unknown flicking issue if the user scale the canvas immediately
        long downTime = SystemClock.uptimeMillis(), eventTime = downTime + 100;
        float x = 0.0f, y = 0.0f;
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState);
        mGestureDetector.onTouchEvent(motionEvent);
    }

    public void reCalBackground() {
        preview = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        bgbitmap = Bitmap.createBitmap(picWidth * 2, picHeight * 2, Bitmap.Config.ARGB_8888);
        bgbitmap.eraseColor(ColorUtils.setAlphaComponent(Color.WHITE, 200));

        for (int i = 0; i < picWidth; i++) {
            for (int j = 0; j < picHeight * 2; j++) {
                if (j % 2 != 0)
                    bgbitmap.setPixel(i * 2 + 1, j, Color.argb(200, 220, 220, 220));
                else
                    bgbitmap.setPixel(i * 2, j, Color.argb(200, 220, 220, 220));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            downInPic = false;

            if (getMode() == Mode.ShapeTool)
                getShapeTool().onDrawEnd(this);

            if (getMode() != Mode.Fill && getMode() != Mode.Dropper && getMode() != Mode.ShapeTool)
                finishAddHistory();
        }

        if (event.getPointerCount() > 1) {
            prePressedTime = -1L;
            mGestureDetector.onTouchEvent(event);
            return true;
        }
        //Get the position
        final float mX = event.getX();
        final float mY = event.getY();
        final float[] raw = new float[9];
        drawMatrix.getValues(raw);
        final float scaledWidth = picBoundary.width() * mScaleFactor;
        final float scaledHeight = picBoundary.height() * mScaleFactor;
        picRect.set((int) (raw[Matrix.MTRANS_X])
                , (int) (raw[Matrix.MTRANS_Y])
                , (int) (raw[Matrix.MTRANS_X] + scaledWidth)
                , (int) (raw[Matrix.MTRANS_Y] + scaledHeight)
        );
        if (!picRect.contains((int) mX, (int) mY)) {
            return true;
        }
        final int x = (int) (((mX - picRect.left) / (scaledWidth)) * picWidth);
        final int y = (int) (((mY - picRect.top) / (scaledHeight)) * picHeight);
        //We got x and y

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (prePressedTime != -1L && System.currentTimeMillis() - prePressedTime <= pressDelay)
                return true;
            if (prePressedTime == -1L) return true;
        }

        if (!isValid(x, y)) return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downY = y;
            downX = x;
            downInPic = true;
            prePressedTime = System.currentTimeMillis();
        }

        if (!((DrawingActivity) getContext()).isEdited())
            ((DrawingActivity) getContext()).setEdited(true);
        if (getMode() == Mode.ShapeTool && downX != -1 && event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_DOWN) {
            if (!getShapeTool().hasEnded())
                getShapeTool().onDraw(this, downX, downY, x, y);
            return true;
        }

        Pxer pxer;
        Bitmap bitmapToDraw = pxerLayers.get(currentLayer).bitmap;
        if (event.getAction() != MotionEvent.ACTION_UP) {
            pxer = new Pxer(x, y, bitmapToDraw.getPixel(x, y));
            if (!currentHistory.contains(pxer))
                currentHistory.add(pxer);
        }
        switch (getMode()) {
            case Normal:
                if (event.getAction() == MotionEvent.ACTION_UP)
                    break;
                bitmapToDraw.setPixel(x, y, ColorUtils.compositeColors(selectedColor, bitmapToDraw.getPixel(x, y)));
                break;
            case Dropper:
                if (event.getAction() == MotionEvent.ACTION_UP)
                    break;
                if (x == downX && downY == y) {
                    for (int i = 0; i < pxerLayers.size(); i++) {
                        int pixel = pxerLayers.get(i).bitmap.getPixel(x, y);
                        if (pixel != Color.TRANSPARENT) {
                            setSelectedColor(pxerLayers.get(i).bitmap.getPixel(x, y));
                            if (dropperCallBack != null){
                                dropperCallBack.onColorDropped(selectedColor);
                            }
                            break;
                        }
                        if (i == pxerLayers.size() - 1) {
                            if (dropperCallBack != null){
                                dropperCallBack.onColorDropped(Color.TRANSPARENT);
                            }
                        }
                    }
                }
                break;
            case Fill:
                //The fill tool is brought to us with aid by some open source project online :( I forgot the name
                if (event.getAction() == MotionEvent.ACTION_UP && x == downX && downY == y) {
                    Tool.freeMemory();

                    int targetColor = bitmapToDraw.getPixel(x, y);
                    Queue<Point> toExplore = new LinkedList<>();
                    HashSet<Point> explored = new HashSet<>();
                    toExplore.add(new Point(x, y));
                    while (!toExplore.isEmpty()) {
                        Point p = toExplore.remove();
                        //Color it
                        currentHistory.add(new Pxer(p.x, p.y, targetColor));
                        bitmapToDraw.setPixel(p.x, p.y, ColorUtils.compositeColors(selectedColor, bitmapToDraw.getPixel(p.x, p.y)));
                        //
                        Point cp;
                        if (isValid(p.x, p.y - 1)) {
                            cp = points[p.x * picHeight + p.y - 1];
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor)
                                    toExplore.add(cp);
                                explored.add(cp);
                            }
                        }

                        if (isValid(p.x, p.y + 1)) {
                            cp = points[p.x * picHeight + p.y + 1];
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor)
                                    toExplore.add(cp);
                                explored.add(cp);
                            }
                        }

                        if (isValid(p.x - 1, p.y)) {
                            cp = points[(p.x - 1) * picHeight + p.y];
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor)
                                    toExplore.add(cp);
                                explored.add(cp);
                            }
                        }

                        if (isValid(p.x + 1, p.y)) {
                            cp = points[(p.x + 1) * picHeight + p.y];
                            if (!explored.contains(cp)) {
                                if (bitmapToDraw.getPixel(cp.x, cp.y) == targetColor)
                                    toExplore.add(cp);
                                explored.add(cp);
                            }
                        }
                    }
                    finishAddHistory();
                }
                break;
        }
        invalidate();
        return true;
    }

    public void finishAddHistory() {
        if (!(currentHistory.size() <= 0)) {
            redohistory.get(currentLayer).clear();
            historyIndex.set(currentLayer, historyIndex.get(currentLayer) + 1);
            history.get(currentLayer).add(new PxerHistory(cloneList(currentHistory)));
            currentHistory.clear();
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x <= (picWidth - 1) && y >= 0 && y <= (picHeight - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.DKGRAY);
        canvas.save();
        canvas.concat(drawMatrix);
        canvas.drawBitmap(bgbitmap, null, picBoundary, pxerPaint);
        for (int i = pxerLayers.size() - 1; i > -1; i--) {
            if (pxerLayers.get(i).visible)
                canvas.drawBitmap(pxerLayers.get(i).bitmap, null, picBoundary, pxerPaint);
        }
        if (showGrid)
            canvas.drawPath(grid, borderPaint);
        canvas.restore();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        initPxerInfo();
    }

    private void initPxerInfo() {
        int length = Math.min(getHeight(), getWidth());
        pxerSize = length / 40;
        picBoundary.set(0, 0, pxerSize * picWidth, pxerSize * picHeight);
        scaleAtFirst();

        grid.reset();
        for (int x = 0; x < picWidth + 1; x++) {
            float posx = (picBoundary.left) + pxerSize * x;
            grid.moveTo(posx, picBoundary.top);
            grid.lineTo(posx, picBoundary.bottom);
        }
        for (int y = 0; y < picHeight + 1; y++) {
            float posy = (picBoundary.top) + pxerSize * y;
            grid.moveTo(picBoundary.left, posy);
            grid.lineTo(picBoundary.right, posy);
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        drawMatrix.postTranslate(-v, -v1);
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float scale = scaleGestureDetector.getScaleFactor();

        mScaleFactor *= scale;
        Matrix transformationMatrix = new Matrix();
        float focusX = scaleGestureDetector.getFocusX();
        float focusY = scaleGestureDetector.getFocusY();

        transformationMatrix.postTranslate(-focusX, -focusY);
        transformationMatrix.postScale(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getScaleFactor());

        transformationMatrix.postTranslate(focusX, focusY);
        drawMatrix.postConcat(transformationMatrix);

        invalidate();
        return true;
    }

    private void scaleAtFirst() {
        //int width = Math.max(getWidth(),getHeight()),height = Math.min(getWidth(),getHeight());
        mScaleFactor = 1.f;
        drawMatrix.reset();

        float scale = 0.98f;

        mScaleFactor = scale;
        Matrix transformationMatrix = new Matrix();
        transformationMatrix.postTranslate((getWidth() - picBoundary.width()) / 2, (getHeight() - picBoundary.height()) / 3);

        float focusX = getWidth() / 2;
        float focusY = getHeight() / 2;

        transformationMatrix.postTranslate(-focusX, -focusY);
        transformationMatrix.postScale(scale, scale);

        transformationMatrix.postTranslate(focusX, focusY);
        drawMatrix.postConcat(transformationMatrix);

        invalidate();
    }

    public void onLayerUpdate() {
        ((DrawingActivity) getContext()).onLayerUpdate();
    }

    @Override
    public void invalidate() {
        ((DrawingActivity) getContext()).onLayerRefresh();
        super.invalidate();
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    public enum Mode {
        Normal, Eraser, Fill, Dropper, ShapeTool
    }

    public interface OnDropperCallBack {
        void onColorDropped(int newColor);
    }

    public static class PxerLayer {
        public Bitmap bitmap;
        public boolean visible = true;

        public PxerLayer() {
        }

        public PxerLayer(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public static class PxableLayer {
        public int width, height;
        public boolean visible;
        public ArrayList<Pxer> pxers = new ArrayList<>();

        public PxableLayer() {
        }
    }

    public static class Pxer {
        public int x, y, color;

        public Pxer(int x, int y, int c) {
            this.x = x;
            this.y = y;
            this.color = c;
        }

        @Override
        protected Pxer clone() {
            return new Pxer(x, y, color);
        }

        @Override
        public boolean equals(Object obj) {
            return ((Pxer) obj).x == this.x && ((Pxer) obj).y == this.y;
        }
    }

    public static class PxerHistory {
        public ArrayList<Pxer> pxers;

        public PxerHistory(ArrayList<Pxer> pxers) {
            this.pxers = pxers;
        }

    }
}
