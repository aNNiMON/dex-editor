/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mao.bytecode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipFile;
import mao.util.FileUtil;
import mao.util.FileUtils;
import mao.util.ZipExtract;
import org.jf.dexlib.DexFile;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class FileBrowser extends ListActivity {

    private final static String EMPTY = "";
    public final static String ENTRYPATH = "ZipEntry";
    public final static String SELECTEDMOD = "selected_mod";
    private final static String TAG = "FileBrowser";

    private Stack<Integer> pos=new Stack<Integer>();

    private List<File> mFileList;
    private FileListAdapter mAdapter;
    private boolean mSelectMod = false;
    private String mQuery = EMPTY;
    private File mCurrentDir;
    private File mCurrent;

    private int position; 

    private static boolean mCut;
    private static File mClipboard;
    private Dialog mPermissionDialog;
    private Handler mHandler=new Handler(){
        @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case SHOWPROGRESS:
                        FileBrowser.this.showDialog(0);
                        break;
                    case DISMISSPROGRESS:
                        mAdapter.notifyDataSetInvalidated();
                        FileBrowser.this.dismissDialog(0);
                        break;
                    case TOAST:
                        toast(msg.obj.toString());
                        break;
                    case SHOWMESSAGE:
                        showMessage(FileBrowser.this,"",msg.obj.toString());
                        break;
                }
            }
    };

    private Comparator<File> sortByType=new Comparator<File>(){
        public int compare(File file1,File file2){
            boolean a=file1.isDirectory();
            boolean b=file2.isDirectory();
            if(a && !b){
                return -1;
            }else if(!a && b){
                return 1;
            }else if(a && b){
                return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
            }else{
                return file1.getName().compareTo(file2.getName());
            }
        }

    };


    private static final int SHOWPROGRESS = 1; 
    private static final int DISMISSPROGRESS = 2; 
    private static final int TOAST = 3; 
    private static final int ERROR = 4; 
    private static final int SHOWMESSAGE = 5; 
    // Linux stat constants
    private static final int S_IFMT = 0170000; /* type of file */
    private static final int S_IFLNK = 0120000; /* symbolic link */
    private static final int S_IFREG = 0100000; /* regular */
    private static final int S_IFBLK = 0060000; /* block special */
    private static final int S_IFDIR = 0040000; /* directory */
    private static final int S_IFCHR = 0020000; /* character special */
    private static final int S_IFIFO = 0010000; /* this is a FIFO */
    private static final int S_ISUID = 0004000; /* set user id on execution */
    private static final int S_ISGID = 0002000; /* set group id on execution */

/*
    static{
        Log.e("maxheap",""+Runtime.getRuntime().maxMemory());
    }
*/
    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            handleIntent(getIntent());
            if(mCurrentDir ==null){
                mCurrentDir = new File("/sdcard");
            }
            mAdapter = new FileListAdapter(getApplication());
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onInvalidated() {
                    updateAndFilterFileList(EMPTY);
                }
            });
            registerForContextMenu(getListView());
            updateAndFilterFileList(mQuery);
            setListAdapter(mAdapter);
            if(mPermissionDialog ==null){
                mPermissionDialog = new Dialog(this);
                mPermissionDialog.setContentView(R.layout.permissions);
                mPermissionDialog.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setPermissions();
                    }
                });
                mPermissionDialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mPermissionDialog.hide();
                    }
                });
            }
            setSelection(position);

        }


    private void updateAndFilterFileList(final String query) {
        File[] files = mCurrentDir.listFiles();
        if(files != null){
            setTitle(mCurrentDir.getPath());

            List<File> work= new Vector<File>(files.length);
            for (File file : files) {
                if (query == null || query.equals(EMPTY)) {
                    work.add(file);
                } else if (file.getName().toLowerCase().contains(query.toLowerCase())) {
                    work.add(file);
                }
            }
            Collections.sort(work,sortByType);

            mFileList = work;

            File parent = mCurrentDir.getParentFile();
            if (parent != null) {
                mFileList.add(0, new File(mCurrentDir.getParent()) {
                    @Override
                    public boolean isDirectory() {
                        return true;
                    }

                @Override
                    public String getName() {
                        return "..";
                    }
                });
            }
        }
    }

    private void handleIntent(Intent intent) {
        mSelectMod = intent.getBooleanExtra(SELECTEDMOD,false);
    }
    /*
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mBusy = false;

                int first = view.getFirstVisiblePosition();
                int count = view.getChildCount();
                for (int i=0; i<count; i++) {
                    TextView t = (TextView)view.getChildAt(i);
                    if (t.getTag() != null) {
                        t.setText(mStrings[first + i]);
                        t.setTag(null);
                    }
                }

                mStatus.setText("Idle");
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                mStatus.setText("Touch scroll");
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                mStatus.setText("Fling");
                break;
        }
    }
*/



    private void resultFileToZipEditor(File file){
        Intent intent=getIntent();
        intent.putExtra(ENTRYPATH,file.getAbsolutePath());
        setResult(R.id.add_entry,intent);
        finish();
    }
    
    
    private void openApk(File file){
        Intent intent=new Intent(this,ZipEditor.class);
        ZipEditor.zipFileName=file.getAbsolutePath();
        startActivityForResult(intent,R.layout.list_item_details);
    }

    private void editArsc(final File file){
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                try{
                    TextEditor.data=FileUtil.readFile(file);
                    Intent intent=new Intent(FileBrowser.this,TextEditor.class);
                    intent.putExtra(TextEditor.PLUGIN,"ARSCEditor");
                    startActivityForResult(intent,R.layout.list_item_details);
                }catch(Exception e){
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="Open Arsc exception "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }
    private void editText(final File file){
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                try{
                    TextEditor.data=FileUtil.readFile(file);
                    Intent intent=new Intent(FileBrowser.this,TextEditor.class);
                    intent.putExtra(TextEditor.PLUGIN,"TextEditor");
                    startActivityForResult(intent,R.layout.list_item_details);
                }catch(Exception e){
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="Open Text exception "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }
    
    private void editAxml(final File file){
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                try{
                    TextEditor.data=FileUtil.readFile(file);
                    Intent intent=new Intent(FileBrowser.this,TextEditor.class);
                    intent.putExtra(TextEditor.PLUGIN,"AXmlEditor");
                    startActivityForResult(intent,R.layout.list_item_details);
                }catch(Exception e){
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="Open Axml exception "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }

    private void openDexFile(final File file){
        new Thread(new Runnable(){
            public void run(){
                try{
                    mHandler.sendEmptyMessage(SHOWPROGRESS);
                    ClassListActivity.dexFile=new DexFile(file);
                    Intent intent=new Intent(FileBrowser.this,ClassListActivity.class);
                    startActivityForResult(intent,R.layout.list_item_details);
                }catch(Exception e){
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="Open dexFile exception "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }

    private void openImageFile(final File file) {
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                
                try {
                    Intent intent = new Intent(FileBrowser.this, ImageViewer.class);
                    intent.putExtra(ImageViewer.FILE_PATH_EXTRA, file.getAbsolutePath());
                    startActivityForResult(intent, R.layout.list_item_details);
                } catch(Exception e) {
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="Open image exception "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }



    @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            //super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode){
                case R.layout.list_item_details:
                    switch(resultCode){
                        case R.layout.text_editor:
                            renameAndWrite();
                            break;
                        case R.layout.zip_list_item:
                            mAdapter.notifyDataSetInvalidated();
                            toast(ZipEditor.zipFileName);
                            break;
                    }
                    break;
            }
        }

    /*
    @Override
    public void onConfigurationChanged(Configuration conf){
        super.onConfigurationChanged(conf);
    }*/

    private  void renameAndWrite(){
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                FileOutputStream out=null;
                try{
                    FileUtils.rename(mCurrent,mCurrent.getName()+".bak");
                    out=new FileOutputStream(mCurrent.getAbsolutePath());
                    out.write(TextEditor.data);

                }catch(IOException io){
                }finally{
                    try{
                        if(out!=null)out.close();
                    }catch(IOException e){}
                    TextEditor.data=null;
                    System.gc();
                }
                Message msg=new Message();
                msg.what=TOAST;
                msg.obj=mCurrent.getName()+getString(R.string.saved);
                mHandler.sendMessage(msg);
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();

    }

    private  boolean isZip(File file){
        String name=file.getName().toLowerCase();
        if(name.endsWith(".apk")){
            return true;
        }
        if(name.endsWith(".zip")
                ||name.endsWith(".jar")){
            return true;
                }
        return false;
    }

    private boolean isImageType(String file) {
        final String[] extentions = {".png", ".jpg", ".jpeg", ".bmp", ".gif"};
        
        String name = file.toLowerCase(Locale.ENGLISH);
        for (String ext : extentions) {
            if (name.endsWith(ext)) return true;
        }
        
        return false;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if(mSelectMod) {
            return;
        }
        
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.options);
        
        File file = null;
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            file = (File) getListView().getItemAtPosition(info.position);
            if(!file.isDirectory())
                menu.add(Menu.NONE, R.string.view, Menu.NONE, R.string.view);
        } catch (ClassCastException e) {
            Log.e(TAG,"Bad menuInfo"+ e);
        }
        
        if (file.getName().equals("..")) return;
        
        menu.add(Menu.NONE, R.string.delete, Menu.NONE, R.string.delete);
        menu.add(Menu.NONE, R.string.rename, Menu.NONE, R.string.rename);
        if (isZip(file)) {
            menu.add(Menu.NONE, R.string.signed, Menu.NONE, R.string.signed);
            menu.add(Menu.NONE, R.string.extract_all, Menu.NONE, R.string.extract_all);
        }
        menu.add(Menu.NONE, R.string.copy, Menu.NONE, R.string.copy);
        menu.add(Menu.NONE, R.string.cut, Menu.NONE, R.string.cut);
        menu.add(Menu.NONE, R.string.paste, Menu.NONE, R.string.paste);
        menu.add(Menu.NONE, R.string.permission, Menu.NONE, R.string.permission);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG,"Bad menuInfo"+ e);
            return false;
        }
        mCurrent = (File) getListView().getItemAtPosition(info.position);
        position=info.position;

        switch(item.getItemId()){
            case R.string.delete:
                delete(mCurrent);
                return true;
            case R.string.view:
                viewCurrent();
                return true;
            case R.string.extract_all:
                extractAll(mCurrent);
                return true;
            case R.string.signed:
                signedFile(mCurrent);
                return true;
            case R.string.rename:
                rename(mCurrent);
                return true;
            case R.string.copy:
                addCopy(mCurrent);
                return false;
            case R.string.cut:
                addCut(mCurrent);
                return false;
            case R.string.paste:
                pasteFile();
                return false;
            case R.string.permission:
                showPermissions();
                return false;
        }
        return false;
    }

    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mFileList != null && mFileList.size() > 0) {
                    File first = mFileList.get(0);
                    if (first.getName().equals("..")&&first.getParentFile() !=null) {
                        mCurrentDir = first;

                        mAdapter.notifyDataSetInvalidated();
                        if(!pos.empty())
                            setSelection(pos.pop());
                        return true;
                    }
                }
                if(mCurrentDir!=null&&mCurrentDir.getParentFile()!=null){
                    mCurrentDir=mCurrentDir.getParentFile();

                    mAdapter.notifyDataSetInvalidated();
                    if(!pos.empty())
                        setSelection(pos.pop());
                    return true;
                }
                if(mCurrentDir !=null&&mCurrentDir.getParent()==null){
                    finish();
                    if(!mSelectMod)
                        System.exit(0);
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("dir_path", mCurrentDir.getAbsolutePath());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String dir = savedInstanceState.getString("dir_path");
        mCurrentDir = new File(dir);
        mAdapter.notifyDataSetInvalidated();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
        protected void onResume() {
            super.onResume();
            mAdapter.notifyDataSetChanged();
        }

    @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
            menu.add(Menu.NONE, R.string.add_folder, Menu.NONE, R.string.add_folder);

            if (mClipboard != null) {
                menu.add(Menu.NONE, R.string.paste, Menu.NONE, R.string.paste);
            }

            menu.add(Menu.NONE, R.string.about, Menu.NONE, R.string.about);
            if(!mSelectMod){
                menu.add(Menu.NONE, R.string.exit, Menu.NONE, R.string.exit);
            }
            return true;
        }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    private void clearAll(){
        mCurrent=null;
        mClipboard=null;
        mCurrentDir=null;
        mCut=false;
        pos=null;
        System.gc();
    }


    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int itemId = item.getItemId();
            switch(itemId){
                case R.string.add_folder:
                    newFolder();
                    break;
                case R.string.paste:
                    pasteFile();
                    break;
                case R.string.about:
                    showAbout();
                    break;
                case R.string.exit:
                    finish();
                    clearAll();
                    System.exit(0);
                    break;
            }
            return true;
        }


    private void signedFile(final File file){
        new Thread(new Runnable(){
            public void run(){
                mHandler.sendEmptyMessage(SHOWPROGRESS);
                try{
                    String out=file.getAbsolutePath();
                    int i=out.lastIndexOf(".");
                    if(i != -1){
                        out=out.substring(0,i)+".signed"+out.substring(i);
                    }
                    apksigner.Main.sign(file,out);
                    Message msg=new Message();
                    msg.what=TOAST;
                    msg.obj=out+getString(R.string.signed);
                    mHandler.sendMessage(msg);
                }catch(Exception e){
                    Message msg=new Message();
                    msg.what=SHOWMESSAGE;
                    msg.obj="signed error: "+e.getMessage();
                    mHandler.sendMessage(msg);
                }
                mHandler.sendEmptyMessage(DISMISSPROGRESS);
            }
        }).start();
    }


    private void extractAll(final File file){
        String absName=file.getAbsolutePath();
        int i=absName.indexOf('.');
        if(i != -1)
            absName=absName.substring(0,i);
        absName+="_upack";
        
        final EditText srcName=new EditText(this);
        srcName.setText(absName);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.extract_path);
        alert.setView(srcName);
        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String src = srcName.getText().toString();
                if (src.length() == 0) {
                    toast(getString(R.string.extract_path_empty));
                    return;
                }
                new Thread(new Runnable(){
                    public void run(){
                        mHandler.sendEmptyMessage(SHOWPROGRESS);
                        try{
                            ZipExtract.unzipAll(new ZipFile(file),new File(srcName.getText().toString()));
                        }catch(Exception e){
                        }
                        mHandler.sendEmptyMessage(DISMISSPROGRESS);
                    }
                }).start();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }

    private void dialogMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(mCurrent.getName());
        builder.setItems(R.array.dialog_menu, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        viewCurrent();
                        break;
                    case 1:
                        editText(mCurrent);
                        break;
                    case 2:
                        delete(mCurrent);
                        break;
                    case 3:
                        rename(mCurrent);
                        break;
                    case 4:
                        addCopy(mCurrent);
                        break;
                    case 5:
                        addCut(mCurrent);
                        break;
                    case 6:
                        showPermissions();
                }
            }
        });
        builder.show();
    }

    private void setPermBit(int perms, int bit, int id) {
        CheckBox ck = (CheckBox) mPermissionDialog.findViewById(id);
        ck.setChecked(((perms >> bit) & 1) == 1);
    }

    private int getPermBit(int bit, int id) {
        CheckBox ck = (CheckBox) mPermissionDialog.findViewById(id);
        int ret = (ck.isChecked()) ? (1 << bit) : 0;
        return ret;
    }

    /**
     * Show and edit file permissions
     */
    private void showPermissions() {
        mPermissionDialog.setTitle(mCurrent.getName());
        try {
            int perms = FileUtils.getPermissions(mCurrent);
            setPermBit(perms, 8, R.id.ckOwnRead);
            setPermBit(perms, 7, R.id.ckOwnWrite);
            setPermBit(perms, 6, R.id.ckOwnExec);
            setPermBit(perms, 5, R.id.ckGrpRead);
            setPermBit(perms, 4, R.id.ckGrpWrite);
            setPermBit(perms, 3, R.id.ckGrpExec);
            setPermBit(perms, 2, R.id.ckOthRead);
            setPermBit(perms, 1, R.id.ckOthWrite);
            setPermBit(perms, 0, R.id.ckOthExec);
            /*
               TextView v = (TextView) mPermissionDialog.findViewById(R.id.permInfo);
               Date date = new Date(mCurrent.lastModified());
               v.setText(mCurrent.getParent() + "\nSize=" + mCurrent.length() + "\nModified=" + date);
               */
            mPermissionDialog.show();
        } catch (Exception e) {
            showMessage(this,"Permission Exception",e.getMessage());
        }
    }

    /**
     * Perform permission setting
     */
    private void setPermissions() {
        mPermissionDialog.hide();
        int perms =
            getPermBit(8, R.id.ckOwnRead) | getPermBit(7, R.id.ckOwnWrite)
            | getPermBit(6, R.id.ckOwnExec) | getPermBit(5, R.id.ckGrpRead)
            | getPermBit(4, R.id.ckGrpWrite) | getPermBit(3, R.id.ckGrpExec)
            | getPermBit(2, R.id.ckOthRead) | getPermBit(1, R.id.ckOthWrite)
            | getPermBit(0, R.id.ckOthExec);

        try {
            FileUtils.chmod(mCurrent, perms);
            toast(Integer.toString(perms, 8));
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            showMessage(this,"Set Permission Exception",e.getMessage());
        }
    }

    private void viewCurrent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(mCurrent);
        String mime = URLConnection.guessContentTypeFromName(uri.toString());

        if (mime != null) {
            if("text/x-java".equals(mime)||"text/xml".equals(mime)){
                intent.setDataAndType(uri, "text/plain");
            }else{
                intent.setDataAndType(uri, mime);
            }
        }else{
            intent.setDataAndType(uri, "*/*");
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            showMessage(this,"Intent Exception",e.getMessage());
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void addCopy(File file) {
        mClipboard = file;
        toast(getString(R.string.copy_to) + file.getName());
        mCut = false;
    }

    private void addCut(File file) {
        mClipboard = file;
        toast(getString(R.string.cut_to) + file.getName());
        mCut = true;
    }

    private void pasteFile() {
        String message = "";
        if (mClipboard == null) {
            showMessage(this,getString(R.string.copy_exception),getString(R.string.copy_nothing));
            return;
        }
        final File destination = new File(mCurrentDir, mClipboard.getName());
        if (destination.exists()) {
            message = String.format(getString(R.string.copy_message),destination.getName());
        }
        if (message != "") {
            prompt(this,getString(R.string.over_write),message, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        performPasteFile(mClipboard, destination);
                    }
                }
            });
        } else {
            performPasteFile(mClipboard, destination);
        }

    }

    private void performPasteFile(final File source, final File destination) {
        if (source.isDirectory()) {
            showMessage(this,getString(R.string.copy_exception),getString(R.string.copy_exist));
        } else {
            new Thread(new Runnable(){
                public void run(){
                    mHandler.sendEmptyMessage(SHOWPROGRESS);
                    try{
                        copyFile(source, destination);
                        if (mCut) {
                            source.delete();
                        }
                    } catch (Exception e) {
                    }
                    mClipboard = null;
                    Message msg=new Message();
                    msg.what=TOAST;
                    msg.obj=destination.getName()+getString(R.string.copied);
                    mHandler.sendMessage(msg);

                    mHandler.sendEmptyMessage(DISMISSPROGRESS);

                }
            }).start();
        }
    }

    /**
     * Copy file from source to destination.
     * 
     * @param source
     * @param destination
     */

    private static void copyFile(File source, File destination) throws Exception {
        byte[] buf = new byte[1024];
        InputStream input = new BufferedInputStream(new FileInputStream(source));
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        int len;
        while ((len = input.read(buf)) > 0) {
            output.write(buf, 0, len);
        }
        output.flush();
        output.close();
        int perms = FileUtils.getPermissions(source) & 0777;
        FileUtils.chmod(destination, perms);
        destination.setLastModified(source.lastModified());
    }

    @Override
        protected void onListItemClick(ListView list, View view, int position, long id) {
            final File file = (File) list.getItemAtPosition(position);
            this.position=position;
            String name=file.getName();
            mCurrent = file;
            if (file.isDirectory()) {
                mCurrentDir = file;

                pos.push(list.getFirstVisiblePosition());
                mAdapter.notifyDataSetInvalidated();
                return;
            }
            if(mSelectMod){

                mSelectMod=false;

                resultFileToZipEditor(file);
                return;
            }
            if(isZip(file)) {
                openApk(file);
            } else if(name.endsWith(".arsc")) {
                editArsc(file);
            } else if(name.endsWith(".xml")) {
                editAxml(file);
            } else if (name.endsWith(".txt")
                    || name.endsWith(".java")
                    || name.endsWith(".py")) {
                editText(file);
            } else if(name.endsWith(".dex")) {
                openDexFile(file);
            } else if(isImageType(file.getName())){
                openImageFile(file);
            } else {
                dialogMenu();
            } 
        }


    @Override
        protected Dialog onCreateDialog(int id) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.wait));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }



    public static void showMessage(Context context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.btn_ok, null);
        builder.show();
    }

    
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.android);
        String title=getString(R.string.app_name);
        try{
            PackageManager pm=getPackageManager();
            PackageInfo pi=pm.getPackageInfo(getPackageName(),0);
            if(pi.versionName !=null){
                title+=" v"+pi.versionName;
            }
        }catch(Exception e){}
        builder.setTitle(title);
        builder.setMessage(getString(R.string.about_content));
        builder.setNeutralButton(R.string.btn_ok, null);
        builder.show();
    }

    public static void prompt(Context context,String title,String message, DialogInterface.OnClickListener btnlisten) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, btnlisten);
        builder.setNegativeButton(R.string.btn_cancel, btnlisten);
        builder.show();
    }

    private void delete(final File file) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.delete);
        alert.setMessage( String.format(getString(R.string.is_delete),file.getName()) );
        alert.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new Thread(new Runnable(){
                    public void run(){
                        mHandler.sendEmptyMessage(SHOWPROGRESS);
                        FileUtils.delete(file);
                        mFileList.remove(file);
                        Message msg=new Message();
                        msg.what=TOAST;
                        msg.obj=file.getName()+getString(R.string.deleted);
                        mHandler.sendMessage(msg);

                        mHandler.sendEmptyMessage(DISMISSPROGRESS);
                    }
                }).start();
            }
        });
        alert.setNegativeButton(R.string.btn_no, null);
        alert.show();
    }

    private void newFolder() {
        final EditText folderName = new EditText(this);
        folderName.setHint(R.string.folder_name);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_folder);
        alert.setView(folderName);
        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = folderName.getText().toString();
                if (name.length() == 0) {
                    toast(getString(R.string.directory_empty));
                    return;
                } else {
                    for (File f : mFileList) {
                        if (f.getName().equals(name)) {
                            toast(String.format(getString(R.string.directory_exists, name)));
                            return;
                        }
                    }
                }
                File dir = new File(mCurrentDir, name);
                if (!dir.mkdirs()) {
                    toast(String.format(getString(R.string.directory_cannot_create), name));
                }else{
                    toast(String.format(getString(R.string.directory_created), name));
                }
                mAdapter.notifyDataSetInvalidated();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }

    private void rename(final File file) {
        final EditText newName = new EditText(this);
        newName.setText(file.getName());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.rename);
        alert.setView(newName);
        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = newName.getText().toString();
                if (name.length() == 0) {
                    toast(getString(R.string.name_empty));
                    return;
                } else {
                    for (File f : mFileList) {
                        if (f.getName().equals(name)) {
                            toast(String.format(getString(R.string.file_exists), name));
                            return;
                        }
                    }
                }
                if (!FileUtils.rename(file, name)) {
                    toast(String.format(getString(R.string.cannot_rename), file.getPath()));
                }
                mAdapter.notifyDataSetInvalidated();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null );

        alert.show();
    }


    /*
       public Drawable getApkDrawable(File apkFile){

       String apkPath=apkFile.getAbsolutePath();

       PackageParser packageParser =new PackageParser(apkPath);


       DisplayMetrics metrics =new DisplayMetrics();
       metrics.setToDefaults();

       PackageParser.Package mPkgInfo = packageParser.parsePackage(
       apkFile,
       apkPath,
       metrics,
       0
       );


       Resources pRes = getResources();

       AssetManager assmgr =    new AssetManager();
       assmgr.addAssetPath(apkPath);

       Resources res =    new Resources(
       assmgr,
       pRes.getDisplayMetrics(),
       pRes.getConfiguration()
       );

       if (info.icon !=0){
       return  res.getDrawable(info.icon);
       }
       return null;



       }
       */

    private Drawable showApkIcon(String apkPath) {
        String PATH_PackageParser ="android.content.pm.PackageParser";
        String PATH_AssetManager ="android.content.res.AssetManager";
        try{
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs =new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs =new Object[1];
            valueArgs[0] = apkPath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            DisplayMetrics metrics =new DisplayMetrics();
            metrics.setToDefaults();

            typeArgs =new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",typeArgs);

            valueArgs =new Object[4];
            valueArgs[0] =new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] =0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

            Class assetMagCls = Class.forName(PATH_AssetManager);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[])null);
            Object assetMag = assetMagCt.newInstance((Object[])null);
            typeArgs =new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",typeArgs);

            valueArgs =new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = getResources();
            typeArgs =new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs =new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            if(info.icon !=0) {
                return res.getDrawable(info.icon);
            }
        }catch(Exception e){}
        return getResources().getDrawable(R.drawable.android);
    }



    private class FileListAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;
        private SimpleDateFormat format=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        AsyncImageLoader  asyn=new AsyncImageLoader();
        /*
           private final Handler mHandler=new Handler(){

           public void handleMessage(Message msg){
           icon.setImageDrawable((Drawable)msg.obj);
           }
           };
           */

        public FileListAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return getFileList().size();
        }

        public Object getItem(int position) {
            return getFileList().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        private String permRwx(int perm) {
            String result;
            result =
                ((perm & 04) != 0 ? "r" : "-") + ((perm & 02) != 0 ? "w" : "-")
                + ((perm & 1) != 0 ? "x" : "-");
            return result;
        }

        private String permFileType(int perm) {
            String result = "?";
            switch (perm & S_IFMT) {
                case S_IFLNK:
                    result = "l";
                    break; /* symbolic link */
                case S_IFREG:
                    result = "-";
                    break; /* regular */
                case S_IFBLK:
                    result = "b";
                    break; /* block special */
                case S_IFDIR:
                    result = "d";
                    break; /* directory */
                case S_IFCHR:
                    result = "c";
                    break; /* character special */
                case S_IFIFO:
                    result = "p";
                    break; /* this is a FIFO */
            }
            return result;
        }

        public String permString(int perms) {
            String result;
            result = permFileType(perms) + permRwx(perms >> 6) + permRwx(perms >> 3) + permRwx(perms);
            return result;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final File file = getFileList().get(position);
            String name=file.getName().toLowerCase();

            RelativeLayout container;
            if (convertView == null) {
                container = (RelativeLayout) mInflater.inflate(R.layout.list_item_details, null);
            } else {
                container = (RelativeLayout) convertView;
            }

            final ImageView icon = (ImageView) container.findViewById(R.id.icon);
            if (file.isDirectory()) {
                icon.setImageResource(R.drawable.folder);
            }else if(name.endsWith(".apk")){
                /*
                Drawable drawable=drawableMap.get(file);
                if(drawable==null){
                    drawable=showApkIcon(file.getAbsolutePath());
                    drawableMap.put(file,drawable);
                }
                icon.setImageDrawable(drawable);
                */
                Drawable drawable=asyn.loadDrawable(file.getAbsolutePath(),icon,new ImageCallback(){
                    public void imageLoaded(Drawable drawable,ImageView imageView){
                        icon.setImageDrawable(drawable);
                    }
                });
                icon.setImageDrawable(drawable);
            }else if(isImageType(name)){

                icon.setImageResource(R.drawable.image);
            }else if(name.endsWith(".zip")
                    ||name.endsWith(".jar")){

                icon.setImageResource(R.drawable.zip);
            } else {
                icon.setImageResource(R.drawable.file);
            }

            TextView text = (TextView) container.findViewById(R.id.text);
            TextView perm = (TextView) container.findViewById(R.id.permissions);
            TextView time = (TextView) container.findViewById(R.id.times);
            TextView size = (TextView) container.findViewById(R.id.size);

            text.setText(file.getName());
            String perms;
            try {
                perms = permString(FileUtils.getPermissions(file));

            } catch (Exception e) {
                perms = "????";
            }
            perm.setText(perms);

            Date date=new Date(file.lastModified());
            time.setText(format.format(date));


            if(file.isDirectory()){
                size.setText("");
            }else{
                size.setText(convertBytesLength(file.length()));
            }
            /*
               if (mShowOwner) {
               String owner = "";
               try {
               FileUtils.FileStatus fs = FileUtils.getFileStatus(file);
               if (fs.uid != 0) {
               owner = mPackageManager.getNameForUid(fs.uid);
               }
               } catch (Exception e) {
               owner = "?";
               }
               line += " " + owner;
               }
               */

            return container;
        }

        private String convertBytesLength(long len){
            if(len<1024){
                return len+"B";
            }
            if(len<1024*1024){
                return String.format("%.2f%s",(len/1024.0),"K");
            }
            if(len<1024*1024*1024)
                return String.format("%.2f%s",(len/(1024*1024.0)),"M");
            return String.format("%.2f%s",(len/(1024*1024*1024.0)),"G");
        }


        protected List<File> getFileList() {
            return mFileList;
        }
    }

    private class AsyncImageLoader {
        private HashMap<String, SoftReference<Drawable>> imageCache;

        public AsyncImageLoader() {
            imageCache=new HashMap<String, SoftReference<Drawable>>();
        }
        private Drawable loadDrawable(final String imageUrl,final ImageView imageView,final ImageCallback imageCallback){
            if(imageCache.containsKey(imageUrl)) {
                SoftReference<Drawable> softReference=imageCache.get(imageUrl);
                Drawable drawable=softReference.get();
                if(drawable!=null) {
                    return drawable;
                }
            }

            final Handler handler=new Handler() {
                public void handleMessage(Message message) {
                    imageCallback.imageLoaded((Drawable) message.obj, imageView);
                }
            };


            new Thread() {
                public void run() {
                    Drawable drawable=showApkIcon(imageUrl);
                    imageCache.put(imageUrl,new SoftReference<Drawable>(drawable));
                    Message message=handler.obtainMessage(0, drawable);
                    handler.sendMessage(message);
                }
            }.start();
            return getResources().getDrawable(R.drawable.android);
        }
    }

    private static interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable,ImageView imageView);
    }

}
