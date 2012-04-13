import java.util.*;
import org.jf.dexlib.*;


public class ClassEditor{

    ClassDefItem classDef;
    ClassDataItem classData;
    MethodEditor[] directMethodEditors;
    MethodEditor[] virtualMethodEditors;
    FieldEditor[] staticFieldEditors;
    FieldEditor[] instanceFieldEditors;

     String classType;
     int accessFlags;
     String superClass;
     TypeListItem interfaces;
     List<ClassDataItem.EncodedField> staticFields;
     List<ClassDataItem.EncodedField> instanceFields;
     List<ClassDataItem.EncodedMethod> directMethods;
     List<ClassDataItem.EncodedMethod> virtualMethods;



     public ClassEditor(){
     }


    public ClassEditor(ClassDefItem classDef){
        this.classDef=classDef;
        classData=classDef.getClassData();
        classType=classDef.getClassType().getTypeDescriptor();
        accessFlags=classDef.getAccessFlags();
        TypeIdItem sup=classDef.getSuperclass();
      //  if(sup !=null)
            superClass=sup.getTypeDescriptor();
        interfaces=classDef.getInterfaces();
        staticFields=Arrays.asList(classData.getStaticFields());
        instanceFields=Arrays.asList(classData.getInstanceFields());
        initFieldEditor();
        directMethods=Arrays.asList(classData.getDirectMethods());
        virtualMethods=Arrays.asList(classData.getVirtualMethods());
        initMethodEditor();
    }

    private void initMethodEditor(){
        directMethodEditors=new MethodEditor[directMethods.size()];
        virtualMethodEditors=new MethodEditor[virtualMethods.size()];
        for(int i=0;i<directMethods.size();i++){
            directMethodEditors[i]=new MethodEditor(directMethods.get(i));
        }
        for(int i=0;i<virtualMethods.size();i++){
            virtualMethodEditors[i]=new MethodEditor(virtualMethods.get(i));
        }
    }

    private void initFieldEditor(){
        staticFieldEditors=new FieldEditor[staticFields.size()];
        instanceFieldEditors=new FieldEditor[instanceFields.size()];
        for(int i=0;i<staticFields.size();i++){
            staticFieldEditors[i]=new FieldEditor(staticFields.get(i));
        }
        for(int i=0;i<instanceFields.size();i++){
            instanceFieldEditors[i]=new FieldEditor(instanceFields.get(i));
        }
    }

    public ClassDefItem copyClassDefItem(DexFile dexFile){
        TypeIdItem classType=CodeEditor.internTypeIdItem(dexFile,this.classType);
        TypeIdItem superClass=CodeEditor.internTypeIdItem(dexFile,this.superClass);
        TypeListItem interfaces=CodeEditor.copyTypeListItem(dexFile,this.interfaces);
     List<ClassDataItem.EncodedField> staticFields=copyField(dexFile,staticFieldEditors);
     List<ClassDataItem.EncodedField> instanceFields=copyField(dexFile,instanceFieldEditors);
        List<ClassDataItem.EncodedMethod> directMethods=copyMethod(dexFile,directMethodEditors);
        List<ClassDataItem.EncodedMethod> virtualMethods=copyMethod(dexFile,virtualMethodEditors);
        ClassDataItem classData=ClassDataItem.internClassDataItem(dexFile,
                staticFields,
                instanceFields,
                directMethods,
                virtualMethods);
        return ClassDefItem.internClassDefItem(dexFile,
                classType,
                accessFlags,
                superClass,
                interfaces,
                null,
                null,
                classData,
                null);
    }


    public List<ClassDataItem.EncodedField> copyField(DexFile dexFile,FieldEditor[] editors){
        List<ClassDataItem.EncodedField> out=new ArrayList<ClassDataItem.EncodedField>(editors.length);
        for(FieldEditor editor :editors){
            out.add(editor.copyEncodedField(dexFile));
        }
        return out;
    }
    
    
    public List<ClassDataItem.EncodedMethod> copyMethod(DexFile dexFile,MethodEditor[] editors){
        List<ClassDataItem.EncodedMethod> out=new ArrayList<ClassDataItem.EncodedMethod>(editors.length);
        for(MethodEditor editor :editors){
            out.add(editor.copyEncodedMethod(dexFile));
        }
        return out;
    }


    public void print(){
       System.out.println("ClassType: "+classType); 
       System.out.println("accessFlags: "+accessFlags); 
       System.out.println("superClass: "+superClass); 
       System.out.println("interfaces: "+interfaces); 
       System.out.println("staticFields: "+staticFields); 
       System.out.println("instanceFields: "+instanceFields); 
       System.out.println("directMethods: "+directMethods); 
       System.out.println("virtualMethods: "+virtualMethods); 
    }
}
