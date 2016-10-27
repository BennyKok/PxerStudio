package com.benny.pxerstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.pxerstudio.pxerexportable.ExportingUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ProjectManagerActivity extends AppCompatActivity {

    ArrayList<File> projects = new ArrayList<>();
    FastItemAdapter<Item> fa = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        fa = new FastItemAdapter<>();
        fa.withSelectable(false);
        rv.setAdapter(fa);

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
                    fa.add(new Item(mName, mPath));
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
                                                    fa.set(position, new Item(newFile.getName(),newFile.getPath()));
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
                                                    fa.remove(position);
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

        private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

        protected class ItemFactory implements ViewHolderFactory<ViewHolder> {
            public ViewHolder create(View v) {
                return new ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends ViewHolder> getFactory() {
            return FACTORY;
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
