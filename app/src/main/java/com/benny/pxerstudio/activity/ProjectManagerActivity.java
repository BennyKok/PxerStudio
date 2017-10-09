package com.benny.pxerstudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.pxerstudio.util.AdHelper;
import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.pxerexportable.ExportingUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.NativeExpressAdView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ProjectManagerActivity extends AppCompatActivity {

    ArrayList<File> projects = new ArrayList<>();
    FastAdapter<Item> fa;
    ItemAdapter<Item> ia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);

        //Comment this line out and the if statement if you forked this repo or downloaded the code
        NativeExpressAdView adView = AdHelper.checkAndEnableAd(this);
        if (adView != null){
            final FrameLayout fl = new FrameLayout(this);
            fl.setVisibility(View.GONE);

            adView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    fl.setVisibility(View.VISIBLE);
                    super.onAdLoaded();
                }
            });

            //ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyle);
            //progressBar.setIndeterminate(true);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.gravity = Gravity.CENTER;

            //fl.addView(progressBar,lp2);
            fl.addView(adView);
            ((RelativeLayout)findViewById(R.id.content_project_manager)).addView(fl,lp);

            fl.setId(R.id.ad_view);
            ((RelativeLayout.LayoutParams)rv.getLayoutParams()).addRule(RelativeLayout.BELOW,R.id.ad_view);
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        fa = new FastAdapter<>();
        ia = new ItemAdapter<>();

        fa.withSelectable(false);
        rv.setAdapter(ia.wrap(fa));

        projects.clear();

        //Find all projects
        final File parent = new File(ExportingUtils.getProjectPath());
        if (parent.exists()){
            File[] temp = parent.listFiles(new PxerFileFilter());
            for (int i = 0; i < temp.length; i++) {
                projects.add(temp[i]);
            }
            if (projects.size() >= 1) {
                findViewById(R.id.noProjectFound).setVisibility(View.GONE);

                for (int i = 0; i < projects.size(); i++) {
                    String mName = projects.get(i).getName().substring(0, projects.get(i).getName().lastIndexOf('.'));
                    String mPath = projects.get(i).getPath();
                    ia.add(new Item(mName, mPath));
                }

                fa.withOnClickListener(new FastAdapter.OnClickListener<Item>() {
                    @Override
                    public boolean onClick(View v, IAdapter<Item> adapter, Item item, int position) {
                        Intent newIntent = new Intent();
                        newIntent.putExtra("selectedProjectPath",item.path);

                        setResult(RESULT_OK,newIntent);
                        finish();
                        return true;
                    }
                });

                fa.withOnLongClickListener(new FastAdapter.OnLongClickListener<Item>() {
                    @Override
                    public boolean onLongClick(View v, IAdapter<Item> adapter, Item item, final int position) {
                        PopupMenu pm = new PopupMenu(v.getContext(),v);
                        pm.inflate(R.menu.menu_popup_project);
                        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.rename:
                                        Tool.promptTextInput(ProjectManagerActivity.this,getString(R.string.rename)).input(null, projects.get(position).getName(), false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                String mInput = input.toString();
                                                if (!mInput.endsWith(".pxer"))
                                                    mInput += ".pxer";

                                                File fromFile = new File(projects.get(position).getPath());
                                                File newFile = new File(projects.get(position).getParent(),mInput);

                                                if (fromFile.renameTo(newFile)) {
                                                    projects.set(position,newFile);
                                                    ia.set(position, new Item(newFile.getName(),newFile.getPath()));
                                                    fa.notifyAdapterItemChanged(position);

                                                    Intent newIntent = new Intent();
                                                    newIntent.putExtra("fileNameChanged",true);

                                                    setResult(RESULT_OK,newIntent);
                                                }
                                            }
                                        }).show();
                                        break;
                                    case R.id.delete:
                                        Tool.prompt(ProjectManagerActivity.this).title(R.string.deleteproject).content(R.string.deleteprojectwarning).positiveText(R.string.delete).onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                if (projects.get(position).delete()){
                                                    ia.remove(position);
                                                    projects.remove(position);

                                                    if (projects.size() < 1)
                                                        findViewById(R.id.noProjectFound).setVisibility(View.VISIBLE);

                                                    Intent newIntent = new Intent();
                                                    newIntent.putExtra("fileNameChanged",true);

                                                    setResult(RESULT_OK,newIntent);

                                                    Tool.toast(ProjectManagerActivity.this,getString(R.string.projectdeleted));
                                                }else
                                                    Tool.toast(ProjectManagerActivity.this,getString(R.string.unabletodeleteproject));
                                            }
                                        }).show();
                                        break;
                                }
                                return true;
                            }
                        });
                        pm.show();
                        return true;
                    }
                });
            }
        }
    }

    public class PxerFileFilter implements FileFilter{
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith(".pxer")) return true;
            return false;
        }
    }

    public static class Item extends AbstractItem<ProjectManagerActivity.Item, ProjectManagerActivity.Item.ViewHolder> {

        public String title;
        public String path;

        public Item(String title,String path){
            this.title = title;
            this.path = path;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_projectitem;
        }

        @Override
        public ViewHolder getViewHolder(View v) {
            return new ViewHolder(v);
        }

        @Override
        public void bindView(ViewHolder holder, List payloads) {
            super.bindView(holder, payloads);

            holder.projectTitle.setText(title);
            holder.projectPath.setText(path);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView projectTitle;
            TextView projectPath;

            ViewHolder(View view) {
                super(view);

                projectTitle = (TextView) view.findViewById(R.id.title);
                projectPath = (TextView) view.findViewById(R.id.path);
            }
        }
    }

}
