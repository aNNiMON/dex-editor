package mao.bytecode;

import android.app.Activity;

import android.os.Bundle;

import android.app.AlertDialog;
import android.widget.EditText;
import android.view.KeyEvent;
import android.content.DialogInterface;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.*;
import java.util.regex.*;

import org.jf.dexlib.*;
import org.jf.dexlib.ClassDataItem.*;
import org.jf.dexlib.Util.*;
import org.jf.dexlib.Code.*;
import org.jf.dexlib.Code.Format.*;
import mao.dalvik.Parser;

public class MethodItemNewActivity extends Activity {
    private static final Pattern pattern=Pattern.compile("\\s");
    private static final Pattern pParams=Pattern.compile("\\s|\\(|\\)");
    private boolean isChanged;
    private EditText accessFlagsEdit;
    private EditText methodNameEdit;
    private EditText descriptorEdit;
    private EditText registerCountEdit;
    private ClassDefItem classDef;
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
        
        accessFlagsEdit.setText("");

        methodNameEdit.setText("newMethod");

        descriptorEdit.setText("()V");
        registerCountEdit.setText("1");
        isChanged=true;

    }
    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(isChanged){
                    FileBrowser.prompt(this,getString(R.string.prompt),getString(R.string.is_save),new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dailog,int which){
                            if(which==AlertDialog.BUTTON_POSITIVE){
                                if(save(ClassListActivity.dexFile)){
                                    setResult(R.string.add_method,getIntent());
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
    
    private boolean save(DexFile dexFile){

        String[] str=null;
        
        //access Flags
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
            CodeItem codeItem=null;
            if((accessFlags|AccessFlags.ABSTRACT.getValue())==0 
                    ||(accessFlags|AccessFlags.NATIVE.getValue())==0
                      ){
                int regCount=Integer.parseInt(registerCountEdit.getText().toString().trim());
                List<Instruction> instructions=new ArrayList<Instruction>();
                instructions.add(new Instruction10x(Opcode.RETURN_VOID));
                codeItem=CodeItem.internCodeItem(
                        dexFile,
                        regCount,
                        1,
                        0,
                        null,/*debuginfo*/
                        instructions,
                        null,
                        null
                        );

            }
            classDef.getClassData().addMethod(new EncodedMethod(method,accessFlags,codeItem));
            ClassListActivity.isChanged=true;
            isChanged=false;
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Method Error");
            return false;
        }
        return true;
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
        System.gc();
    }
 
}
