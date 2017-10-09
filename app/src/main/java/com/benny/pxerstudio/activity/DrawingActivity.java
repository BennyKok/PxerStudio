package com.benny.pxerstudio.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.benny.pxerstudio.R;
import com.benny.pxerstudio.colorpicker.ColorPicker;
import com.benny.pxerstudio.colorpicker.SatValView;
import com.benny.pxerstudio.pxerexportable.AtlasExportable;
import com.benny.pxerstudio.pxerexportable.FolderExportable;
import com.benny.pxerstudio.pxerexportable.GifExportable;
import com.benny.pxerstudio.pxerexportable.PngExportable;
import com.benny.pxerstudio.shape.EraserShape;
import com.benny.pxerstudio.shape.LineShape;
import com.benny.pxerstudio.shape.RectShape;
import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.widget.BorderFab;
import com.benny.pxerstudio.widget.FastBitmapView;
import com.benny.pxerstudio.widget.PxerView;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DrawingActivity extends AppCompatActivity implements FileChooserDialog.FileCallback, ItemTouchCallback {

    public static final String UNTITLED = "Untitled";
    public static final RectShape rectShapeFactory = new RectShape();
    public static final LineShape lineShapeFactory = new LineShape();
    public static final EraserShape eraserShapeFactory = new EraserShape();
    public static String currentProjectPath;
    public static BorderFab fabColor;
    public static ColorPicker cp;
    public boolean isEdited = false;
    public FastAdapter<LayerThumbItem> fa;
    public ItemAdapter<LayerThumbItem> ia;
    private PxerView pxerView;
    private RecyclerView layersRv;
    private View layerView;
    private TextView titleTextVIew;
    private Toolbar toolbar;
    private boolean onlyShowSelected;

    public void setTitle(String subtitle, boolean edited) {
        if (subtitle == null)
            subtitle = UNTITLED;
        titleTextVIew.setText(Html.fromHtml("PxerStudio<br><small><small>" + subtitle + (edited ? "*" : "") + "</small></small>"));
        isEdited = edited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
        titleTextVIew.setText(Html.fromHtml("PxerStudio<br><small><small>" + pxerView.getProjectName() + (edited ? "*" : "") + "</small></small>"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleTextVIew = (TextView) findViewById(R.id.titleTextView);
        pxerView = (PxerView) findViewById(R.id.pxerView);
        layerView = findViewById(R.id.layerview);

        setTitle(UNTITLED, false);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        titleTextVIew.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        SharedPreferences pxerPref = getSharedPreferences("pxerPref", MODE_PRIVATE);
        pxerView.setSelectedColor(pxerPref.getInt("lastUsedColor", Color.YELLOW));

        setUpLayersView();
        setupControl();

        currentProjectPath = pxerPref.getString("lastOpenedProject", null);
        if (currentProjectPath != null) {
            File file = new File(currentProjectPath);
            if (file.exists()) {
                pxerView.loadProject(file);
                setTitle(Tool.stripExtension(file.getName()), false);
            }
        }
        if (fa.getItemCount() == 0) {
            ia.add(new LayerThumbItem());
            fa.select(0);
        }
        System.gc();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onPostCreate(savedInstanceState);
    }

    private void setupControl() {
        final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.fabs);
        findViewById(R.id.fab_eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_eraser_24dp);
                pxerView.setMode(PxerView.Mode.ShapeTool);
                pxerView.setShapeTool(eraserShapeFactory);
            }
        });
        findViewById(R.id.fab_fill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_fill_24dp);
                pxerView.setMode(PxerView.Mode.Fill);
            }
        });
        findViewById(R.id.fab_dropper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_colorize_24dp);
                pxerView.setMode(PxerView.Mode.Dropper);
            }
        });
        findViewById(R.id.fab_pen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_mode_edit_24dp);
                pxerView.setMode(PxerView.Mode.Normal);
            }
        });
        findViewById(R.id.fab_rect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_square_24dp);
                pxerView.setMode(PxerView.Mode.ShapeTool);
                pxerView.setShapeTool(rectShapeFactory);
            }
        });
        findViewById(R.id.fab_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabMenu.getMenuIconView().setImageResource(R.drawable.ic_line_24dp);
                pxerView.setMode(PxerView.Mode.ShapeTool);
                pxerView.setShapeTool(lineShapeFactory);
            }
        });
        fabColor = (BorderFab) findViewById(R.id.fab_color);
        fabColor.setColor(pxerView.getSelectedColor());
        fabColor.setColorNormal(pxerView.getSelectedColor());
        fabColor.setColorPressed(pxerView.getSelectedColor());
        cp = new ColorPicker(this, pxerView.getSelectedColor(), new SatValView.OnColorChangeListener() {
            @Override
            public void onColorChanged(int newColor) {
                pxerView.setSelectedColor(newColor);
                fabColor.setColor(newColor);
            }
        });
        fabColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                cp.show(view);
            }
        });
        findViewById(R.id.fab_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pxerView.undo();
            }
        });
        findViewById(R.id.fab_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pxerView.redo();
            }
        });
    }

    private void setUpLayersView() {
        layersRv = (RecyclerView) findViewById(R.id.layersRv);
        View layersBtn = findViewById(R.id.layersBtn);

        layersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pxerView.addLayer();
                ia.add(Math.max(pxerView.getCurrentLayer(), 0), new LayerThumbItem());
                fa.deselect();
                fa.select(pxerView.getCurrentLayer());
                layersRv.invalidate();
            }
        });

        fa = new FastAdapter<>();
        ia = new ItemAdapter<>();

        layersRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        layersRv.setAdapter(ia.wrap(fa));

        layersRv.setItemAnimator(new DefaultItemAnimator());

        SimpleDragCallback touchCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(layersRv);

        fa.withSelectable(true);
        fa.withMultiSelect(false);
        fa.withAllowDeselection(false);

        fa.withOnLongClickListener(new FastAdapter.OnLongClickListener<LayerThumbItem>() {
            @Override
            public boolean onLongClick(View v, IAdapter<LayerThumbItem> adapter, LayerThumbItem item, int position) {
                fa.deselect();
                fa.select(position);
                return false;
            }
        });
        fa.withOnClickListener(new FastAdapter.OnClickListener<LayerThumbItem>() {
            @Override
            public boolean onClick(View v, IAdapter<LayerThumbItem> adapter, LayerThumbItem item, final int position) {
                if (onlyShowSelected) {
                    PxerView.PxerLayer layer = pxerView.getPxerLayers().get(pxerView.getCurrentLayer());
                    layer.visible = false;
                    pxerView.invalidate();

                    fa.notifyAdapterItemChanged(pxerView.getCurrentLayer());
                }
                pxerView.setCurrentLayer(position);
                if (onlyShowSelected) {
                    PxerView.PxerLayer layer = pxerView.getPxerLayers().get(pxerView.getCurrentLayer());
                    layer.visible = true;
                    pxerView.invalidate();

                    fa.notifyAdapterItemChanged(pxerView.getCurrentLayer());
                }
                item.pressed();
                if (item.isPressSecondTime()) {
                    PopupMenu popupMenu = new PopupMenu(DrawingActivity.this, v);
                    popupMenu.inflate(R.menu.menu_popup_layer);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            DrawingActivity.this.onOptionsItemSelected(item);
                            return false;
                        }
                    });
                    popupMenu.show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        if (!isEdited)
            setEdited(true);

        pxerView.moveLayer(oldPosition, newPosition);

        if (oldPosition < newPosition) {
            for (int i = oldPosition + 1; i <= newPosition; i++) {
                Collections.swap(ia.getAdapterItems(), i, i - 1);
                fa.notifyAdapterItemMoved(i, i - 1);
            }
        } else {
            for (int i = oldPosition - 1; i >= newPosition; i--) {
                Collections.swap(ia.getAdapterItems(), i, i + 1);
                fa.notifyAdapterItemMoved(i, i + 1);
            }
        }

        return true;
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {
        pxerView.setCurrentLayer(newPosition);
    }

    public void onLayerUpdate() {
        ia.clear();
        for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
            ia.add(new LayerThumbItem());
        }
        fa.select(0);
        ia.getAdapterItem(0).pressed();
    }

    public void onLayerRefresh() {
        if (layersRv != null)
            layersRv.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_drawing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.onlyshowselectedlayer:
                onlyShowSelected = true;
                pxerView.visibilityAllLayer(false);

                PxerView.PxerLayer layer2 = pxerView.getPxerLayers().get(pxerView.getCurrentLayer());
                layer2.visible = true;
                pxerView.invalidate();

                fa.notifyAdapterDataSetChanged();
                break;
            case R.id.export:
                new PngExportable().runExport(this, pxerView);
                break;
            case R.id.exportgif:
                new GifExportable().runExport(this, pxerView);
                break;
            case R.id.exportfolder:
                new FolderExportable().runExport(this, pxerView);
                break;
            case R.id.exportatlas:
                new AtlasExportable().runExport(this, pxerView);
                break;
            case R.id.save:
                pxerView.save(true);
                break;
            case R.id.projectm:
                pxerView.save(false);
                startActivityForResult(new Intent(this, ProjectManagerActivity.class), 01223);
                break;
            case R.id.open:
                new FileChooserDialog.Builder(this)
                        .initialPath(Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Project"))
                        .extensionsFilter(".pxer")
                        .goUpLabel(".../")
                        .show();
                break;
            case R.id.newp:
                createNewProject();
                break;
            case R.id.resetvp:
                pxerView.resetViewPort();
                break;
            case R.id.hidealllayers:
                if (onlyShowSelected) break;
                pxerView.visibilityAllLayer(false);
                fa.notifyAdapterDataSetChanged();
                break;
            case R.id.showalllayers:
                onlyShowSelected = false;
                pxerView.visibilityAllLayer(true);
                fa.notifyAdapterDataSetChanged();
                break;
            case R.id.gridonoff:
                if (pxerView.isShowGrid())
                    item.setIcon(R.drawable.ic_grid_on_24dp);
                else
                    item.setIcon(R.drawable.ic_grid_off_24dp);
                pxerView.setShowGrid(!pxerView.isShowGrid());
                break;
            case R.id.layers:
                layerView.setPivotX(layerView.getWidth() / 2);
                layerView.setPivotY(0);
                if (layerView.getVisibility() == View.VISIBLE) {
                    layerView.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).alpha(0).scaleX(0.85f).scaleY(0.85f).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            layerView.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    layerView.setVisibility(View.VISIBLE);
                    layerView.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1).scaleY(1).alpha(1);
                }
                break;
            case R.id.deletelayer:
                if (pxerView.getPxerLayers().size() <= 1) break;
                Tool.prompt(this).title(R.string.deletelayer).content(R.string.deletelayerwarning).positiveText(R.string.delete).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!isEdited)
                            setEdited(true);

                        ia.remove(pxerView.getCurrentLayer());
                        pxerView.removeCurrentLayer();

                        fa.deselect();
                        fa.select(pxerView.getCurrentLayer());
                        fa.notifyAdapterDataSetChanged();
                    }
                }).show();
                break;
            case R.id.copypastelayer:
                pxerView.copyAndPasteCurrentLayer();
                ia.add(Math.max(pxerView.getCurrentLayer(), 0), new LayerThumbItem());
                fa.deselect();
                fa.select(pxerView.getCurrentLayer());
                layersRv.invalidate();
                break;
            case R.id.mergealllayer:
                if (pxerView.getPxerLayers().size() <= 1) break;
                Tool.prompt(this).title(R.string.mergealllayers).content(R.string.mergealllayerswarning).positiveText(R.string.merge).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!isEdited)
                            setEdited(true);

                        pxerView.mergeAllLayers();
                        ia.clear();
                        ia.add(new LayerThumbItem());
                        fa.deselect();
                        fa.select(0);
                    }
                }).show();
                break;
            case R.id.about:
                startActivity(new Intent(DrawingActivity.this, AboutActivity.class));
                break;
            case R.id.tvisibility:
                if (onlyShowSelected) break;
                PxerView.PxerLayer layer = pxerView.getPxerLayers().get(pxerView.getCurrentLayer());
                layer.visible = !layer.visible;
                pxerView.invalidate();
                fa.notifyAdapterItemChanged(pxerView.getCurrentLayer());
                break;
            case R.id.clearlayer:
                Tool.prompt(this)
                        .title(R.string.clearcurrentlayer)
                        .content(R.string.clearcurrentlayerwarning)
                        .positiveText(R.string.clear)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                pxerView.clearCurrentLayer();
                            }
                        }).show();
                break;
            case R.id.mergedown:
                if (pxerView.getCurrentLayer() == pxerView.getPxerLayers().size() - 1) break;
                Tool.prompt(this)
                        .title(R.string.mergedownlayer)
                        .content(R.string.mergedownlayerwarning)
                        .positiveText(R.string.merge)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                pxerView.mergeDownLayer();
                                ia.remove(pxerView.getCurrentLayer() + 1);
                                fa.select(pxerView.getCurrentLayer());
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 01223 && data != null) {
            String path = data.getStringExtra("selectedProjectPath");
            if (path != null && !path.isEmpty()) {
                currentProjectPath = path;
                File file = new File(path);
                if (file.exists()) {
                    pxerView.loadProject(file);
                    setTitle(Tool.stripExtension(file.getName()), false);
                }
            } else if (data.getBooleanExtra("fileNameChanged", false)) {
                currentProjectPath = "";
                pxerView.setProjectName("");
                recreate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createNewProject() {
        ConstraintLayout l = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_activity_drawing_newproject, null);
        final EditText editText = (EditText) l.findViewById(R.id.et1);
        final SeekBar seekBar = (SeekBar) l.findViewById(R.id.sb);
        final TextView textView = (TextView) l.findViewById(R.id.tv2);
        final SeekBar seekBar2 = (SeekBar) l.findViewById(R.id.sb2);
        final TextView textView2 = (TextView) l.findViewById(R.id.tv3);
        seekBar.setMax(127);
        seekBar.setProgress(39);
        textView.setText("Width : " + 40);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setText("Width : " + String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar2.setMax(127);
        seekBar2.setProgress(39);
        textView2.setText("Height : " + 40);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView2.setText("Height : " + String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .customView(l, false)
                .title(R.string.newproject)
                .positiveText(R.string.create)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (editText.getText().toString().isEmpty()) return;
                        setTitle(editText.getText().toString(), true);
                        pxerView.createBlankProject(editText.getText().toString(), seekBar.getProgress() + 1, seekBar2.getProgress() + 1);
                    }
                })
                .show();
        pxerView.save(false);
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        pxerView.loadProject(file);
        setTitle(Tool.stripExtension(file.getName()), false);
        currentProjectPath = file.getPath();
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }

    @Override
    protected void onStop() {
        saveState();
        super.onStop();
    }

    private void saveState() {
        SharedPreferences pxerPref = getSharedPreferences("pxerPref", MODE_PRIVATE);
        pxerPref.edit()
                .putString("lastOpenedProject", currentProjectPath)
                .putInt("lastUsedColor", pxerView.getSelectedColor())
                .apply();
        if (pxerView.getProjectName() != null && !pxerView.getProjectName().isEmpty())
            pxerView.save(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cp.onConfigChanges();
    }

    public class LayerThumbItem extends AbstractItem<LayerThumbItem, LayerThumbItem.ViewHolder> {

        public int pressedTime = 0;

        public void pressed() {
            pressedTime++;
            pressedTime = Math.min(2, pressedTime);
        }

        public boolean isPressSecondTime() {
            if (pressedTime == 2) {
                return true;
            } else return false;
        }

        @Override
        public int getType() {
            return R.id.layerthumbitem;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_layerthumbitem;
        }

        @Override
        public void bindView(ViewHolder viewHolder, List payloads) {
            super.bindView(viewHolder, payloads);
            viewHolder.iv.setSelected(isSelected());

            PxerView.PxerLayer layer = pxerView.getPxerLayers().get(viewHolder.getLayoutPosition());
            viewHolder.iv.setVisible(layer.visible);
            viewHolder.iv.setBitmap(layer.bitmap);
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public LayerThumbItem withSetSelected(boolean selected) {
            if (!selected)
                pressedTime = 0;
            return super.withSetSelected(selected);
        }

        @Override
        public ViewHolder getViewHolder(View v) {
            return new ViewHolder(v);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            FastBitmapView iv;

            ViewHolder(View view) {
                super(view);
                iv = (FastBitmapView) view;
            }
        }
    }
}
