package mao.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import mao.dalvik.Parser;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.IndexedSection;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ClassListActivity extends ListActivity {

    public static String searchString="";
    public static String searchFieldClass="";
    public static String searchFieldName="";
    public static String searchFieldDescriptor="";
    public static String searchMethodClass="";
    public static String searchMethodName="";
    public static String searchMethodDescriptor="";
    private static final int SAVEFILE=1;
    private static final int SAVEDISMISS=2;
    private static final String title="/";
    private Tree tree;

    private static HashMap<String,ClassDefItem> classMap;
    private static HashMap<String,ClassDefItem> deleteclassMap;
    public static DexFile dexFile;
    public static boolean isChanged;
    public static ClassDefItem curClassDef;

    //tree dep
    private static int dep;
    private static Stack<String> path;

    private static String curFile;
    private ClassListAdapter mAdapter;
    private List<String> classList;


    private int mod;

    private static final int OPENDIR=10;
    private static final int BACK=11;
    private static final int UPDATE=12;
    private static final int INIT=13;
    private static final int TOAST=14;
    private static final int SEARCH=15;
    private static final int SEARCHDISMISS=16;


    private Handler mHandler=new Handler(){
        @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case SAVEFILE:
                        ClassListActivity.this.showDialog(SAVEFILE);
                        break;
                    case SEARCH:
                        ClassListActivity.this.showDialog(SEARCH);
                        break;
                    case SAVEDISMISS:
                        ClassListActivity.this.dismissDialog(SAVEFILE);
                        break;
                    case SEARCHDISMISS:
                        ClassListActivity.this.dismissDialog(SEARCH);
                        break;
                    case TOAST:
                        toast(msg.obj.toString());
                        break;
                }
            }
    };

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.class_list);
            init();
            mAdapter=new ClassListAdapter(this);
            mAdapter.registerDataSetObserver(new DataSetObserver(){
                @Override
                public void onInvalidated() {
                    switch(mod){
                        case OPENDIR:
                            tree.push(curFile);
                            classList=tree.list();
                            break;
                        case BACK:
                            tree.pop();
                            classList=tree.list();
                            break;
                        case UPDATE:
                            classList=tree.list();
                            break;
                        case INIT:
                            init();
                            break;
                    }
                    setTitle(title+tree.getCurPath());

                }
            });
            setListAdapter(mAdapter);
            registerForContextMenu(getListView());

            Button btn=(Button)findViewById(R.id.btn_string_pool);
            btn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    openStringPool();
                }
            });

        }
    @Override
        public void onListItemClick(ListView list,View v,int position,long id){
            curFile= (String) list.getItemAtPosition(position);
            if(tree.isDirectory(curFile)){
                mod=OPENDIR;
                mAdapter.notifyDataSetInvalidated();
                return;
            }
            curClassDef=classMap.get(tree.getCurPath()+curFile);
            Intent intent=new Intent(this,ClassItemActivity.class);
            startActivity(intent);
        }

    private void init(){
        if(classMap==null){
            classMap=new HashMap<String,ClassDefItem>();
        }else{
            classMap.clear();
        }

        HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        for(ClassDefItem classItem: dexFile.ClassDefsSection.getItems()){
            String className=classItem.getClassType().getTypeDescriptor();
            className=className.substring(1,className.length()-1);
            if(deleteclassMap!=null&&deleteclassMap.get(className)!=null){
                continue;
            }
            classMap.put(className,classItem);
        }
        tree=new Tree(classMap.keySet());

        setTitle(title+tree.getCurPath());
        classList=tree.list();
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle status){
        }

        */
    @Override
    public boolean onCreateOptionsMenu(Menu m){
        MenuInflater in=getMenuInflater();
        in.inflate(R.menu.class_list_menu,m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi){
        int id=mi.getItemId();
        switch(id){
            case R.id.save_dexfile:
                new Thread(new Runnable(){
                    public void run(){
                        mHandler.sendEmptyMessage(SAVEFILE);
                        saveDexFile();
                        mHandler.sendEmptyMessage(SAVEDISMISS);
                        
                        setResultToZipEditor();
                    }
                }).start();
                break;

            case R.id.search_string:
                searchString();
                break;
            case R.id.search_method:
                searchMethod();
                break;
            case R.id.search_field:
                searchField();
                break;
            case R.id.merger_dexfile:
                selectDexFile();
                break;

        }
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.string.rename_class, Menu.NONE, R.string.rename_class);
        menu.add(Menu.NONE, R.string.remove_class, Menu.NONE, R.string.remove_class);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode){
                case R.layout.class_list_item:
                    switch(resultCode){
                        case R.id.add_entry:
                            if(mergerDexFile(data.getStringExtra(FileBrowser.ENTRYPATH))){
                                toast(" merged");
                            }
                            break;
                    }
            }
    }
         
    @Override
        protected Dialog onCreateDialog(int id) {
            ProgressDialog dialog = new ProgressDialog(this);
            switch(id){
                case SAVEFILE:
                    dialog.setMessage(getString(R.string.saving));
                    break;
                case SEARCH:
                    dialog.setMessage(getString(R.string.searching));
                    break;
            }
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }

    public static void setCurrnetClass(String className){
        curClassDef=classMap.get(className);
    }
    

    private void searchString() {
        LayoutInflater inflate=getLayoutInflater();
        ScrollView scroll=(ScrollView)inflate.inflate(R.layout.alert_dialog_search_string,null);
        final EditText srcName = (EditText)scroll.findViewById(R.id.src_edit);
        srcName.setText(searchString);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search_string);
        alert.setView(scroll);

        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                searchString = srcName.getText().toString();
                if (searchString.length() == 0) {
                    toast(getString(R.string.search_name_empty));
                    return;
                }
                new Thread(new Runnable(){
                    public void run(){
                        mHandler.sendEmptyMessage(SEARCH);
                        List<String> classList=new ArrayList<String>();
                        searchStringInMethods(classList,searchString);
                        SearchClassesActivity.initClassList(classList);
                        mHandler.sendEmptyMessage(SEARCHDISMISS);

                        sendIntentToSearchActivity();
                    }
                }).start();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }

    
    private void searchField() {
        LayoutInflater inflate=getLayoutInflater();
        ScrollView scroll=(ScrollView)inflate.inflate(R.layout.alert_dialog_search_field,null);
        final EditText fieldClass = (EditText)scroll.findViewById(R.id.class_edit);
        final CheckBox ignoreNameAndDescriptor=(CheckBox)scroll.findViewById(R.id.ignore_name_descriptor);
        final EditText fieldName = (EditText)scroll.findViewById(R.id.name_edit);
        final CheckBox ignoreDescriptor=(CheckBox)scroll.findViewById(R.id.ignore_descriptor);
        final EditText fieldDescriptor = (EditText)scroll.findViewById(R.id.descriptor_edit);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search_field);
        alert.setView(scroll);


        fieldClass.setText(searchFieldClass);
        fieldName.setText(searchFieldName);
        fieldDescriptor.setText(searchFieldDescriptor);

        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                new Thread(new Runnable(){
                    public void run(){
                        mHandler.sendEmptyMessage(SEARCH);


                        searchFieldClass = fieldClass.getText().toString();
                        searchFieldName = fieldName.getText().toString();
                        searchFieldDescriptor = fieldDescriptor.getText().toString();
                        List<String> classList=new ArrayList<String>();
                        searchFieldInMethods(classList,searchFieldClass,searchFieldName,searchFieldDescriptor,ignoreNameAndDescriptor.isChecked(),ignoreDescriptor.isChecked());
                        SearchClassesActivity.initClassList(classList);

                        mHandler.sendEmptyMessage(SEARCHDISMISS);

                        sendIntentToSearchActivity();
                    }
                }).start();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }


    private void searchMethod() {
        LayoutInflater inflate=getLayoutInflater();
        ScrollView scroll=(ScrollView)inflate.inflate(R.layout.alert_dialog_search_method,null);
        final EditText methodClass = (EditText)scroll.findViewById(R.id.class_edit);
        final CheckBox ignoreNameAndDescriptor=(CheckBox)scroll.findViewById(R.id.ignore_name_descriptor);
        final EditText methodName = (EditText)scroll.findViewById(R.id.name_edit);
        final CheckBox ignoreDescriptor=(CheckBox)scroll.findViewById(R.id.ignore_descriptor);
        final EditText methodDescriptor = (EditText)scroll.findViewById(R.id.descriptor_edit);

        methodClass.setText(searchMethodClass);
        methodName.setText(searchMethodName);
        methodDescriptor.setText(searchMethodDescriptor);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search_method);
        alert.setView(scroll);

        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                searchMethodClass = methodClass.getText().toString();
                searchMethodName = methodName.getText().toString();
                searchMethodDescriptor = methodDescriptor.getText().toString();
                List<String> classList=new ArrayList<String>();
                searchMethodInMethods(classList,searchMethodClass,searchMethodName,searchMethodDescriptor,ignoreNameAndDescriptor.isChecked(),ignoreDescriptor.isChecked());
                SearchClassesActivity.initClassList(classList);
                sendIntentToSearchActivity();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }




    private void sendIntentToSearchActivity(){
        Intent intent=new Intent(ClassListActivity.this,SearchClassesActivity.class);
        startActivity(intent);
    }
    private void clearAll(){
        if(classMap !=null)
            classMap.clear();
        classMap=null;
        deleteclassMap=null;
        path=null;
        dexFile=null;
        curClassDef=null;
        tree=null;
        curFile=null;
        isChanged=false;
        System.gc();
    }

    private  void saveDexFile(){
        DexFile outDexFile=new DexFile();

        HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        for(Map.Entry<String,ClassDefItem> entry:classMap.entrySet()){
            if(deleteclassMap!=null&&deleteclassMap.get(entry.getKey())!=null){
                continue;
            }
            ClassDefItem classDef=entry.getValue();
            classDef.internClassDefItem(outDexFile);
        }
        outDexFile.setSortAllItems(true);
        outDexFile.place();

        //out dex byte array
        byte[] buf=new byte[outDexFile.getFileSize()];
        ByteArrayAnnotatedOutput out=new ByteArrayAnnotatedOutput(buf);
        outDexFile.writeTo(out);

        DexFile.calcSignature(buf);
        DexFile.calcChecksum(buf);
        TextEditor.data=buf;
        outDexFile=null;
        isChanged=false;
    }

    private boolean mergerDexFile(String name){
        try{
            DexFile tmp=new DexFile(name);
            DexFile dexFile=ClassListActivity.dexFile;
            IndexedSection<ClassDefItem> classes=tmp.ClassDefsSection;
            List<ClassDefItem> classDefList=classes.getItems();
            for(ClassDefItem classDef:classDefList){
                String className=classDef.getClassType().getTypeDescriptor();
                className=className.substring(1,className.length()-1);
                if(deleteclassMap!=null){
                    deleteclassMap.put(className,null);
                }
                classDef.internClassDefItem(dexFile);
            }
            mod=INIT;
            mAdapter.notifyDataSetInvalidated();
            isChanged=true;
        }catch(Exception e){
            FileBrowser.showMessage(this,"Open dexFile exception",e.getMessage());
            return false;
        }
        System.gc();
        return true;
    }

    private void openStringPool(){
        Intent intent=new Intent(this,TextEditor.class);
        intent.putExtra(TextEditor.PLUGIN,"StringIdsEditor");
        startActivity(intent);
    }

    private void replaceClassType(String src,String dst){
        for(TypeIdItem type: dexFile.TypeIdsSection.getItems()){
            String s=type.getTypeDescriptor();
            //skip start 'L'
            int pos=1;
            for(int i=0;i<s.length();i++){
                if(s.charAt(i)!='['){
                    break;
                }
                pos++;
            }
            int i=s.indexOf(src);
            if(i !=-1&&i==pos){
                s=s.replace(src,dst);
                type.setTypeDescriptor(s);
            }
        }
    }
    
    
    private void renameType(final String className) {
        final EditText newName = new EditText(this);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final boolean isDirectory=className.endsWith("/");

        if(isDirectory){
            newName.setText(className.substring(0,className.length()-1));
        }else{
            newName.setText(className);
        }
        alert.setTitle(R.string.rename);
        alert.setView(newName);
        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = newName.getText().toString();
                if (name.length() == 0
                    ||name.indexOf("/")!=-1) {
                    toast(getString(R.string.name_empty));
                    return;
                } else {
                    for (String s : classList) {
                        if (s.equals(name)) {
                            toast(String.format(getString(R.string.class_exists), name));
                            return;
                        }
                    }
                }
                name+=isDirectory?"/":"";
                String cur=tree.getCurPath();

                replaceClassType(cur+className,cur+name);
                isChanged=true;
                mod=INIT;
                mAdapter.notifyDataSetInvalidated();
            }
        });
        alert.setNegativeButton(R.string.btn_cancel, null);

        alert.show();
    }

    
    private void selectDexFile(){
        Intent intent=new Intent(this,FileBrowser.class);
        intent.putExtra(FileBrowser.SELECTEDMOD,true);
        startActivityForResult(intent,R.layout.class_list_item);
    }

    


    private static void searchStringInMethods(List<String> list,String src){

        HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        for(Map.Entry<String,ClassDefItem> entry:classMap.entrySet()){
            if(deleteclassMap!=null&&deleteclassMap.get(entry.getKey())!=null){
                continue;
            }
            ClassDefItem classItem=entry.getValue();
            boolean isSearch=false;
            ClassDataItem classData=classItem.getClassData();
            if(classData !=null){
                //
                ClassDataItem.EncodedMethod[] methods=classData.getDirectMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchStringInMethod(method,src)){
                        String name=classItem.getClassType().getTypeDescriptor();
                        list.add(name.substring(1,name.length()-1));
                        isSearch=true;
                        break;
                    }
                }
                if(isSearch){
                    continue;
                }
                //virtual methods
                methods=classData.getVirtualMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchStringInMethod(method,src)){
                        String name=classItem.getClassType().getTypeDescriptor();
                        list.add(name.substring(1,name.length()-1));
                        break ;
                    }
                }
            }
        }
    }


    private static void searchFieldInMethods(List<String> list,String classType,String name,String descriptor,boolean ignoreNameAndDescriptor,boolean ignoreDescriptor){

        HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        for(Map.Entry<String,ClassDefItem> entry:classMap.entrySet()){
            if(deleteclassMap!=null&&deleteclassMap.get(entry.getKey())!=null){
                continue;
            }
            ClassDefItem classItem=entry.getValue();
            boolean isSearch=false;
            ClassDataItem classData=classItem.getClassData();
            if(classData !=null){
                //
                ClassDataItem.EncodedMethod[] methods=classData.getDirectMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchFieldInMethod(method,classType,name,descriptor,ignoreNameAndDescriptor,ignoreDescriptor)){
                        String string=classItem.getClassType().getTypeDescriptor();
                        list.add(string.substring(1,string.length()-1));
                        isSearch=true;
                        break;
                    }
                }
                if(isSearch){
                    continue;
                }
                //virtual methods
                methods=classData.getVirtualMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchFieldInMethod(method,classType,name,descriptor,ignoreNameAndDescriptor,ignoreDescriptor)){
                        String string=classItem.getClassType().getTypeDescriptor();
                        list.add(string.substring(1,string.length()-1));
                        break ;
                    }
                }
            }
        }
    }


    private static void searchMethodInMethods(List<String> list,String classType,String name,String descriptor,boolean ignoreNameAndDescriptor,boolean ignoreDescriptor){

        HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        for(Map.Entry<String,ClassDefItem> entry:classMap.entrySet()){
            if(deleteclassMap!=null&&deleteclassMap.get(entry.getKey())!=null){
                continue;
            }
            ClassDefItem classItem=entry.getValue();
            boolean isSearch=false;
            ClassDataItem classData=classItem.getClassData();
            if(classData !=null){
                //
                ClassDataItem.EncodedMethod[] methods=classData.getDirectMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchMethodInMethod(method,classType,name,descriptor,ignoreNameAndDescriptor,ignoreDescriptor)){
                        String string=classItem.getClassType().getTypeDescriptor();
                        list.add(string.substring(1,string.length()-1));
                        isSearch=true;
                        break;
                    }
                }
                if(isSearch){
                    continue;
                }
                //virtual methods
                methods=classData.getVirtualMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    if(Parser.searchMethodInMethod(method,classType,name,descriptor,ignoreNameAndDescriptor,ignoreDescriptor)){
                        String string=classItem.getClassType().getTypeDescriptor();
                        list.add(string.substring(1,string.length()-1));
                        break ;
                    }
                }
            }
        }
    }




    private void showDialogIfChanged(){
        if(isChanged){
            FileBrowser.prompt(this,getString(R.string.prompt),getString(R.string.is_save),new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dailog,int which){
                    if(which==AlertDialog.BUTTON_POSITIVE){
                        new Thread(new Runnable(){
                            public void run(){
                                mHandler.sendEmptyMessage(SAVEFILE);
                                saveDexFile();
                                mHandler.sendEmptyMessage(SAVEDISMISS);
                                setResultToZipEditor();
                            }
                        }).start();

                    }
                    else if(which==AlertDialog.BUTTON_NEGATIVE){
                        finish();
                    }
                }
            });
        }else{
            finish();
        }
    }

    private void setResultToZipEditor(){
        Intent intent=getIntent();
        setResult(R.layout.text_editor,intent);
        finish();
    }

        @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(e.toString(),"Bad menuInfo");
            return false;
        }
        switch(item.getItemId()){
            case R.string.rename_class:
                {
                    String className=classList.get(info.position);
                    renameType(className);
                }
                break;
            case R.string.remove_class:
                final String name=classList.get(info.position);
                FileBrowser.prompt(this,getString(R.string.is_remove),name,new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int which){
                            if(which==AlertDialog.BUTTON_POSITIVE){
                                if(tree.isDirectory(name)){
                                    removeClassesDir(name);
                                }else{
                                    removeClasses(name);
                                }
                            }
                        }
                    });

                break;
        }
        return true;
    }

    private void removeClassesDir(String name){
        if(deleteclassMap==null){
            deleteclassMap=new HashMap<String,ClassDefItem>();
        }

        HashMap<String,ClassDefItem> deleteclassMap=ClassListActivity.deleteclassMap;

        String cur=tree.getCurPath()+name;
        for(String key:classMap.keySet()){
            if(key.indexOf(cur) == 0){
                deleteclassMap.put(key,classMap.get(key));
            }
        }
        isChanged=true;
        mod=INIT;
        mAdapter.notifyDataSetInvalidated();
    }
    
    private void removeClasses(String name){
        if(deleteclassMap==null){
            deleteclassMap=new HashMap<String,ClassDefItem>();
        }

        String cur=tree.getCurPath()+name;
        deleteclassMap.put(cur,classMap.get(cur));
        isChanged=true;
        mod=INIT;
        mAdapter.notifyDataSetInvalidated();
    }



    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
        public void onDestroy(){
            super.onDestroy();
            clearAll();
        }


    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(!getTitle().equals(title)){
                    mod=BACK;
                    mAdapter.notifyDataSetInvalidated();
                    return true;
                }else{
                    showDialogIfChanged();
                    return true;
                }
            }
            return super.onKeyDown(keyCode,event);
        }

    private class ClassListAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;
        LinearLayout container;
        public ClassListAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return classList.size();
        }

        public Object getItem(int position) {
            return classList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }



        public View getView(int position, View convertView, ViewGroup parent) {
            String file=classList.get(position);

            if(convertView==null){
                container = (LinearLayout) mInflater.inflate(R.layout.class_list_item, null);
            }else{
                container=(LinearLayout)convertView;
            }
            ImageView icon = (ImageView) container.findViewById(R.id.list_item_icon);
            if (tree.isDirectory(file)) {
                icon.setImageResource(R.drawable.folder);
            } else {
                icon.setImageResource(R.drawable.clazz);
            }
            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(file);
            return container;
        }
    }
    

    private static class Tree{
        private List<Map<String,String>> node;
        private Comparator<String> sortByType=new Comparator<String>(){
            public int compare(String a,String b){
                if(isDirectory(a) && !isDirectory(b)){
                    return -1;
                }
                if(!isDirectory(a) &&isDirectory(b)){
                    return 1;
                }
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        };

        private Tree(Set<String> names){
            if(path==null){
                path=new Stack<String>();
                dep=0;
            }
            HashMap<String,ClassDefItem> classMap=ClassListActivity.classMap;
            node=new ArrayList<Map<String,String>>();
            for(String name :names){
                String[] token=name.split("/");
                String tmp="";
                for(int i=0,len=token.length;i<len;i++){
                    String value=token[i];
                    if(i>=node.size()){
                        Map<String,String> map=new HashMap<String,String>();
                        if(classMap.containsKey(tmp+value)
                                &&i+1 == len){
                            map.put(tmp+value,tmp);
                        }else{
                            map.put(tmp+value+"/",tmp);
                        }
                        node.add(map);
                        tmp+=value+"/";
                    }else{
                        Map<String,String> map=node.get(i);
                        if(classMap.containsKey(tmp+value)
                                &&i+1 == len){
                            map.put(tmp+value,tmp);
                        }else{
                            map.put(tmp+value+"/",tmp);
                        }
                        tmp+=value+"/";
                    }
                }
            }
        }

        private List<String> list(String parent){
            Map<String,String> map=null;
            List<String> str=new ArrayList<String>();
            while(dep>=0&&node.size()>0){
                map=node.get(dep);
                if(map != null){
                    break;
                }
                pop();
            }
            if(map ==null){
                return str;
            }
            for(String key :map.keySet()){
                if(parent.equals(map.get(key))){
                    int index;
                    if(key.endsWith("/")){
                        index=key.lastIndexOf("/",key.length()-2);
                    }else{
                        index=key.lastIndexOf("/");
                    }
                    if(index != -1)
                        key=key.substring(index+1);
                    str.add(key);
            //        Log.e("tree",key);
                }
            }
            Collections.sort(str,sortByType);

            return str;
        }

        private List<String> list(){
            return list(getCurPath());
        }
        private void push(String name){
            dep++;
            path.push(name);
        }
        private String pop(){
            if(dep>0){
                dep--;
                return path.pop();
            }
            return null;
        }
        public String getCurPath(){
            return join(path,"/");
        }
        private boolean isDirectory(String name){
            return name.endsWith("/");
        }
        
        private String join(Stack<String> stack,String d){
            StringBuilder sb=new StringBuilder("");
            for(String s: stack){
                sb.append(s);
            }
            return sb.toString();
        }


    }

}
