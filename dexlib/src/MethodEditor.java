import org.jf.dexlib.*;

public class MethodEditor{
    ClassDataItem.EncodedMethod method;
    CodeItem codeItem;
    CodeEditor codeEditor;
    int accessFlags;
    String classType;
    String methodName;
    ProtoIdItem methodPrototype;
    public MethodEditor(ClassDataItem.EncodedMethod method){
        this.method=method;
        codeItem=method.codeItem;
        if(codeItem != null)
            codeEditor=new CodeEditor(codeItem);
        classType=method.method.getContainingClass().getTypeDescriptor();
        methodName=method.method.getMethodName().getStringValue();
        methodPrototype=method.method.getPrototype();
        accessFlags=method.accessFlags;
    }
    
    public ClassDataItem.EncodedMethod copyEncodedMethod(DexFile dexFile){

        MethodIdItem method=CodeEditor.copyMethodIdItem(dexFile,this.method.method);
        CodeItem codeItem=null;
        if(codeEditor != null)
            codeItem=codeEditor.copyCodeItem(dexFile);

        return new ClassDataItem.EncodedMethod(method,
                accessFlags,
                codeItem);
 
    }

    public ClassDataItem.EncodedMethod internEncodedMethod(DexFile dexFile){

        MethodIdItem method=CodeEditor.copyMethodIdItem(dexFile,this.method.method);
        CodeItem codeItem=null;
        if(codeEditor != null)
            codeItem=codeEditor.internCodeItem(dexFile);

        return new ClassDataItem.EncodedMethod(method,
                accessFlags,
                codeItem);
 
    }




    public void print(){
        System.out.println("accessFlags: "+accessFlags);
        System.out.println("classType: "+classType);
        System.out.println("methodName: "+methodName);
        System.out.println("protoType: "+methodPrototype);
        System.out.println("codeItem: "+codeItem);
        codeEditor.print();
    }
}
