package mao.bytecode;


import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.CheckBox;

import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Typeface;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jf.dexlib.*;
import org.jf.dexlib.ClassDataItem.*;
import org.jf.util.IndentingWriter;

import mao.bytecode.opcodes.OpcodeInfoActivity;
import mao.dalvik.Parser;

public class CodeEditorActivity extends Activity {

    private EditText codeEdit;
    private ScrollView scroll;
    private ClassDefItem classDef;
    private EncodedMethod method;
    private Parser parser;
    private boolean isChanged;
    private boolean noCode;

    private SharedPreferences mPreferences;
    private TextSettings mSettings;

    private static String searchString="";
    private static String replaceString="";

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.code_editor);

            TextWatcher watch=new TextWatcher(){
                public void beforeTextChanged(CharSequence c,int start,int count,int after){
                }
                public void onTextChanged(CharSequence c,int start,int count,int after){
                }
                public void afterTextChanged(Editable edit){
                    if(!isChanged){
                        isChanged=true;
                    }
                }
            };

            codeEdit=(EditText)findViewById(R.id.code_edit);
            codeEdit.addTextChangedListener(watch);
            scroll=(ScrollView)findViewById(R.id.scroll);
            mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mSettings=new TextSettings(mPreferences);

            init();

        }

    private void init(){
        classDef=ClassListActivity.curClassDef;
        if(MethodListActivity.isDirectMethod){
            method=classDef.getClassData().getDirectMethods()[MethodListActivity.methodIndex];
        }else{
            method=classDef.getClassData().getVirtualMethods()[MethodListActivity.methodIndex];
        }
        if(method.codeItem !=null){
            parser=new Parser(method.codeItem);
            try{
                StringBuilder sb=new StringBuilder(4*1024);
                parser.dump(new IndentingWriter(sb));
                codeEdit.setText(sb.toString());
            }catch(Exception e){
                noCode=true;
            }
        }else{
            noCode=true;
        }
        isChanged=false;
    }

 
    private void updatePrefs(){
        mSettings.readPrefs(mPreferences);

        //line wrap 
		codeEdit.setHorizontallyScrolling(!mSettings.mLineWrap);

        //font type
        String font=mSettings.mFontType;

        if (font.equals("Serif"))
            codeEdit.setTypeface(Typeface.SERIF);
        else if (font.equals("Sans Serif"))
            codeEdit.setTypeface(Typeface.SANS_SERIF);
        else  
            codeEdit.setTypeface(Typeface.MONOSPACE);


        codeEdit.setTextSize(mSettings.mFontSize);
        codeEdit.setTextColor(mSettings.mFontColor);


        codeEdit.setBackgroundColor(mSettings.mBgColor);
        scroll.setBackgroundColor(mSettings.mBgColor);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if (!noCode){
            menu.add(0, R.string.save_code, 0, R.string.save_code);
        }

        menu.add(0, R.string.search, 0, R.string.search);
        menu.add(0, R.string.opcode_info, 0, R.string.opcode_info);
        menu.add(0, R.string.method_editor, 0, R.string.method_editor);
        menu.add(0, R.string.preferences, 0, R.string.preferences);
        menu.add(0, R.string.exit, 0, R.string.exit);
        return true;
    }


    private boolean saveCode(){
        try{
            parser.parse(ClassListActivity.dexFile,codeEdit.getText().toString());
            ClassListActivity.isChanged=true;
        }catch(Exception e){
            FileBrowser.showMessage(this,"Code Error",e.getMessage());
            return false;
        }
        toast("save successful");
        return true;
    }


    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDialogIfChanged(){
        FileBrowser.prompt(this,getString(R.string.prompt),getString(R.string.is_save),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dailog,int which){
                if(which==AlertDialog.BUTTON_POSITIVE){
                    if(saveCode()){
                        resultToMethodList();
                    }
                }else if(which==AlertDialog.BUTTON_NEGATIVE){
                    resultToMethodList();
                }
            }
        });
    }

    private void resultToMethodList(){
        Intent intent=getIntent();
        setResult(R.layout.code_editor,intent);
        finish();
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem mi){
            switch(mi.getItemId()){
                case R.string.method_editor:
                {
                    Intent intent=new Intent(this,MethodItemEditorActivity.class);
                    startActivity(intent);
                    break;
                }
                
                case R.string.opcode_info:
                {
                    Intent intent = new Intent(this, OpcodeInfoActivity.class);
                    startActivity(intent);
                    break;
                }
                
                case R.string.save_code:
                    isChanged=!saveCode();
                    break;
                case R.string.exit:
                    if(noCode){
                        resultToMethodList();
                        return true;
                    }

                    if(isChanged){
                        showDialogIfChanged();
                    }else{
                        resultToMethodList();
                    }

                    break;
                case R.string.preferences:
                    {
                        Intent intent=new Intent(this,TextPreferences.class);
                        startActivity(intent);
                        break;
                    }
                case R.string.search:
                    searchString();
            }
            return true;
        }
    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(!noCode&&isChanged){
                    showDialogIfChanged();
                    return true;
                }
                resultToMethodList();
            }
            return super.onKeyDown(keyCode,event);
        }

    private void searchString() {
        LayoutInflater inflate=getLayoutInflater();
        ScrollView scroll=(ScrollView)inflate.inflate(R.layout.alert_dialog_search_or_replace,null);
        final CheckBox from_start=(CheckBox)scroll.findViewById(R.id.from_start);
        final EditText srcName = (EditText)scroll.findViewById(R.id.src_edit);
        final CheckBox isReplace=(CheckBox)scroll.findViewById(R.id.replace);
        final EditText dstName = (EditText)scroll.findViewById(R.id.replace_edit);
        srcName.setText(searchString);
        dstName.setText(replaceString);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search_string);
        alert.setView(scroll);

        alert.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                searchString = srcName.getText().toString();
                replaceString = dstName.getText().toString();
                if (searchString.length() == 0) {
                    toast(getString(R.string.search_name_empty));
                    return;
                }
                int index=from_start.isChecked()?0:codeEdit.getSelectionStart();
                if(isReplace.isChecked()){
                    if(!replace(searchString,replaceString,index)){
                        toast(String.format(getString(R.string.search_not_found),searchString));
                    }
                    return;
                }
                if(!searchString(searchString,index)){
                    toast(String.format(getString(R.string.search_not_found),searchString));
                }

            }
        });
        alert.setNegativeButton(R.string.btn_cancel,null);

        alert.show();
    }

    private boolean searchString(String src,int index){
        CharSequence seq=codeEdit.getText();
        index=seq.toString().indexOf(src,index+1);
        if(index !=-1){
            codeEdit.setSelection(index,index+src.length());
            return true;
        }
        return false;
    }

    private boolean replace(String src,String dst,int index){
        Editable editable=codeEdit.getEditableText();
        String s=codeEdit.getText().toString();
        if((index=s.indexOf(src,index+1)) !=-1){
            editable.replace(index,index+src.length(),dst);
            codeEdit.setSelection(index,index+dst.length());
            return true;
        }
        return false;
    }




    @Override
    public void onResume(){
        super.onResume();
        updatePrefs();
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        clearAll();
    }
    private void clearAll(){
        classDef=null;
        method=null;
        parser=null;
        codeEdit=null;
        scroll=null;
        System.gc();
    }


}
