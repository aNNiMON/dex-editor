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

public class FieldItemEditorActivity extends Activity {
    private static final Pattern pattern=Pattern.compile("\\s");
    private boolean isChanged;
    private EditText accessFlagsEdit;
    private EditText fieldNameEdit;
    private EditText descriptorEdit;
    private EncodedField field;
    private ClassDefItem classDef;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.field_item_editor);

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

        fieldNameEdit=(EditText)findViewById(R.id.field_name_edit);
        fieldNameEdit.addTextChangedListener(watch);

        descriptorEdit=(EditText)findViewById(R.id.field_descriptor_edit);
        descriptorEdit.addTextChangedListener(watch);
        init();

    }

    private void init(){
        classDef=ClassListActivity.curClassDef;
        if(FieldListActivity.isStaticField){
            field=classDef.getClassData().getStaticFields()[FieldListActivity.fieldIndex];
        }else{
            field=classDef.getClassData().getInstanceFields()[FieldListActivity.fieldIndex];
        }
     
        accessFlagsEdit.setText(AccessFlags.formatAccessFlagsForField(field.accessFlags));

        fieldNameEdit.setText(field.field.getFieldName().getStringValue());

        descriptorEdit.setText(field.field.getFieldType().getTypeDescriptor());
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
                                    setResult(R.layout.field_item_editor);
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
            FieldIdItem field=FieldIdItem.internFieldIdItem(
                    dexFile,
                    classDef.getClassType(),
                    TypeIdItem.internTypeIdItem(
                        dexFile,
                        descriptorEdit.getText().toString()
                        ),
                    StringIdItem.internStringIdItem(
                        dexFile,
                        fieldNameEdit.getText().toString()
                        )
                    );
            if(FieldListActivity.isStaticField){
                classDef.getClassData().setStaticField(FieldListActivity.fieldIndex,new EncodedField(field,accessFlags));
            }else{
                classDef.getClassData().setInstanceField(FieldListActivity.fieldIndex,new EncodedField(field,accessFlags));
            }
            ClassListActivity.isChanged=true;
            isChanged=false;
        }catch(Exception e){
            FileBrowser.showMessage(this,"","Field Name or Descriptor Error");
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
        classDef=null;
        accessFlagsEdit=null;
        fieldNameEdit=null;
        descriptorEdit=null;
        System.gc();
    }
}
