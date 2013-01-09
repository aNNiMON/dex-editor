package mao.bytecode;

import android.app.Activity;
import android.app.AlertDialog;

import android.os.Bundle;
import android.content.DialogInterface;
import android.widget.EditText;
import android.view.KeyEvent;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.regex.*;
import java.util.*;

import org.jf.dexlib.*;
import org.jf.dexlib.Util.*;

public class ClassInfoEditorActivity extends Activity {
    private static final Pattern pattern=Pattern.compile("\\s");
    private EditText accessFlagsEdit;
    private EditText superclassEdit;
    private EditText interfacesEdit;
    private EditText sourceFileEdit;
    private boolean isChanged;
    private ClassDefItem classDef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_info_editor);
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


        accessFlagsEdit=(EditText)findViewById(R.id.access_flags_edit);
        accessFlagsEdit.addTextChangedListener(watch);

        superclassEdit=(EditText)findViewById(R.id.super_class_edit);
        superclassEdit.addTextChangedListener(watch);

        interfacesEdit=(EditText)findViewById(R.id.interface_edit);
        interfacesEdit.addTextChangedListener(watch);

        sourceFileEdit=(EditText)findViewById(R.id.source_file_edit);
        sourceFileEdit.addTextChangedListener(watch);
        init();
    }

    private void init(){
        classDef=ClassListActivity.curClassDef;

        accessFlagsEdit.setText(AccessFlags.formatAccessFlagsForClass(classDef.getAccessFlags()));

        String superClassName=classDef.getSuperclass().getTypeDescriptor();
        superclassEdit.setText(superClassName);
        //interfaces
        String interfaces=classDef.getInterfaces() !=null?classDef.getInterfaces().getTypeListString(" "):"";
        interfacesEdit.setText(interfaces);


        String source=classDef.getSourceFile()!=null?classDef.getSourceFile().getStringValue():"";
        sourceFileEdit.setText(source);
        isChanged=false;
    }
    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(isChanged){
                    FileBrowser.prompt(this,getString(R.string.prompt),getString(R.string.is_save),new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dailog,int which){
                            if(which==AlertDialog.BUTTON_POSITIVE){
                                save(ClassListActivity.dexFile);
                                finish();
                            }else if(which==AlertDialog.BUTTON_NEGATIVE){
                                finish();
                            }
                        }
                    });
                    return true;
                }
            }
            return super.onKeyDown(keyCode,event);
        }

    private void save(DexFile dexFile){
        //Access Flags
        String[] str=null;
        int accessFlags=0;
        try{
            String ac=accessFlagsEdit.getText().toString();
            if(ac !=null&&!ac.equals("")){
                str=pattern.split(accessFlagsEdit.getText().toString());
                if(str !=null){
                    for(String s:str){
                        AccessFlags accessFlag=AccessFlags.getAccessFlag(s);
                        accessFlags|=accessFlag.getValue();
                    }
                }
            }
            classDef.accessFlags=accessFlags;
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Access Flag Error ");
        }

        //super class
        classDef.superType=TypeIdItem.internTypeIdItem(
                dexFile,
                superclassEdit.getText().toString()
                );

        //interfaces
        ArrayList<TypeIdItem> types=new ArrayList<TypeIdItem>();
        String in=interfacesEdit.getText().toString();
        if(in !=null&&!in.equals("")){
            str=pattern.split(in);
            if(str != null){
                for(String s:str){
                    if(s.equals(""))
                        continue;
                    types.add(
                            TypeIdItem.internTypeIdItem(
                                dexFile,
                                s
                                )
                            );
                }
            }
        }
        TypeListItem typeList=null;
        if(types.size()>0){
            typeList=TypeListItem.internTypeListItem(
                    dexFile,
                    types
                    );
        }
        classDef.implementedInterfaces=typeList;
        String sourceFile=sourceFileEdit.getText().toString().trim();
        if(!sourceFile.equals("")){
            classDef.sourceFile=StringIdItem.internStringIdItem(
                    dexFile,
                    sourceFile
                    );
        }else{
            classDef.sourceFile=null;
        }
        ClassListActivity.isChanged=true;
        isChanged=false;

    }

    private void clearAll(){
        classDef=null;
        accessFlagsEdit=null;
        superclassEdit=null;
        interfacesEdit=null;
        sourceFileEdit=null;
        System.gc();
    }
    @Override
        public void onDestroy(){
            super.onDestroy();
            clearAll();
        }
}
