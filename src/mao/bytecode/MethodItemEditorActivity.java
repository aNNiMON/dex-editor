package mao.bytecode;

import android.app.Activity;

import android.os.Bundle;

import android.app.AlertDialog;
import android.widget.EditText;
import android.view.KeyEvent;
import android.content.DialogInterface;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.regex.*;

import org.jf.dexlib.*;
import org.jf.dexlib.Util.*;
import org.jf.dexlib.ClassDataItem.*;
import mao.dalvik.Parser;

public class MethodItemEditorActivity extends Activity {
    private static final Pattern pattern=Pattern.compile("\\s");
    private static final Pattern pParams=Pattern.compile("\\s|\\(|\\)");
    private boolean isChanged;
    private EditText accessFlagsEdit;
    private EditText methodNameEdit;
    private EditText descriptorEdit;
    private EditText registerCountEdit;
    private ClassDefItem classDef;
    private EncodedMethod method;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.method_item_editor);
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

        methodNameEdit=(EditText)findViewById(R.id.method_name_edit);
        methodNameEdit.addTextChangedListener(watch);

        descriptorEdit=(EditText)findViewById(R.id.method_descriptor_edit);
        descriptorEdit.addTextChangedListener(watch);

        registerCountEdit=(EditText)findViewById(R.id.register_count_edit);
        registerCountEdit.addTextChangedListener(watch);
        init();
 

    }

    private void init(){
        classDef=ClassListActivity.curClassDef;
        if(MethodListActivity.isDirectMethod){
            method=classDef.getClassData().getDirectMethods()[MethodListActivity.methodIndex];
        }else{
            method=classDef.getClassData().getVirtualMethods()[MethodListActivity.methodIndex];
        }

        accessFlagsEdit.setText(AccessFlags.formatAccessFlagsForMethod(method.accessFlags));

        methodNameEdit.setText(method.method.getMethodName().getStringValue());

        descriptorEdit.setText(method.method.getPrototype().getPrototypeString());
        if(method.codeItem!=null)
            registerCountEdit.setText(method.codeItem.getRegisterCount()+"");
        isChanged=false;

    }
    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(isChanged){
                    FileBrowser.prompt(this,getString(R.string.prompt),getString(R.string.is_save),new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dailog,int which){
                            if(which==AlertDialog.BUTTON_POSITIVE){
                                if(save(ClassListActivity.dexFile)){
                                finish();
                                }
                            }else if(which==AlertDialog.BUTTON_NEGATIVE){
                                ClassListActivity.isChanged=false;
                                finish();
                            }
                        }
                    });
                    return true;
                }
            }
            return super.onKeyDown(keyCode,event);
        }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearAll();
    }

    private void clearAll(){
        accessFlagsEdit=null;
        methodNameEdit=null;
        descriptorEdit=null;
        registerCountEdit=null;
        classDef=null;
        method=null;
        System.gc();
    }
    
    private boolean save(DexFile dexFile){

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
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Access Flag Error ");
            return false;
        }
        try{
            str=pParams.split(descriptorEdit.getText().toString());
            if(str[str.length-1].equals(""))
                throw new Exception("No Return Type Exception");
            TypeListItem typeList=Parser.buildTypeList(
                    dexFile,
                    str[str.length-2]
                    );
            TypeIdItem returnType=TypeIdItem.internTypeIdItem(
                    dexFile,
                    str[str.length-1]
                    );
            MethodIdItem method=MethodIdItem.internMethodIdItem(
                    dexFile,
                    classDef.getClassType(),
                    ProtoIdItem.internProtoIdItem(
                        dexFile,
                        returnType,
                        typeList),
                    StringIdItem.internStringIdItem(
                        dexFile,
                        methodNameEdit.getText().toString()
                        )
                    );
            if(MethodListActivity.isDirectMethod){
                classDef.getClassData().setDirectMethod(MethodListActivity.methodIndex,new EncodedMethod(method,accessFlags,this.method.codeItem));
            }else{
                classDef.getClassData().setVirtualMethod(MethodListActivity.methodIndex,new EncodedMethod(method,accessFlags,this.method.codeItem));
            }
            ClassListActivity.isChanged=true;
            isChanged=false;
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Method Name Or Descriptor Error");
            return false;
        }
        try{
            if(method.codeItem !=null){
                method.codeItem.registerCount=Integer.parseInt(registerCountEdit.getText().toString().trim());
            }
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Register Count Error");
        }

        return true;
    }

}
