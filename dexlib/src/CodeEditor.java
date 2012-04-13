
import org.jf.dexlib.*;
import org.jf.dexlib.Code.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CodeEditor{
    CodeItem code;
    int registerCount;
    int inWords;
    int outWords;
    DebugInfoItem debugInfo;
    List<Instruction> instructions;
    List<CodeItem.TryItem> tries;
    
    List<CodeItem.EncodedCatchHandler> encodedCatchHandlers;
    
    public CodeEditor(CodeItem code){
        this.code=code;
        registerCount=code.getRegisterCount();
        inWords=code.inWords;
        outWords=code.outWords;
        instructions = Arrays.asList(code.getInstructions());
        CodeItem.TryItem[] tryItem=code.getTries();
        if(tryItem != null)
            tries=Arrays.asList(tryItem);
    }


    public CodeItem copyCodeItem(DexFile dexFile){

        List<CodeItem.EncodedCatchHandler> encodedCatchHandlers=new ArrayList<CodeItem.EncodedCatchHandler>();
        return CodeItem.internCodeItem(
                dexFile,
                registerCount,
                inWords,
                outWords,
                debugInfo,
                copyInstruction(dexFile),
                copyTryItems(dexFile,tries,encodedCatchHandlers),
                encodedCatchHandlers
                );
    }
    

    public CodeItem internCodeItem(DexFile dexFile){

        return CodeItem.internCodeItem(
                dexFile,
                registerCount,
                inWords,
                outWords,
                debugInfo,
                instructions,
                tries,
                encodedCatchHandlers
                );
    }



    public List<Instruction> copyInstruction(DexFile dexFile){
        List<Instruction> instructions=this.instructions;
        List<Instruction> out=new ArrayList<Instruction>(instructions.size());
        for(Instruction instruction : instructions){
            if(instruction instanceof InstructionWithReference){
                copyReferencedInstruction(dexFile,instruction,out);
            }else{
                out.add(instruction);
            }
            //System.out.println("instructions: "+out);
        }
        return out;
    }

    private static void copyReferencedInstruction(DexFile dexFile,Instruction instruction, List<Instruction> instructions) {
        InstructionWithReference ref=(InstructionWithReference) instruction;
        switch (instruction.opcode.referenceType) {
            case field:
                FieldIdItem field=(FieldIdItem) ref.getReferencedItem();
                field=copyFieldIdItem(dexFile,field);
                ref.setReferencedItem(field);
                instructions.add(instruction);
                return;
            case method:
                MethodIdItem method=(MethodIdItem) ref.getReferencedItem();
                method=copyMethodIdItem(dexFile,method);
                ref.setReferencedItem(method);
                instructions.add(instruction);
                return;
            case type:
                TypeIdItem type=(TypeIdItem) ref.getReferencedItem();
                type=copyTypeIdItem(dexFile,type);
                ref.setReferencedItem(type);
                instructions.add(instruction);
                return;
            case string:
                StringIdItem string=(StringIdItem)ref.getReferencedItem();
                string=StringIdItem.internStringIdItem(dexFile,string.getStringValue());
                ref.setReferencedItem(string);
                instructions.add(instruction);
        }
    }


    public static TypeIdItem internTypeIdItem(DexFile dexFile,String name){
        StringIdItem typeDescriptor=StringIdItem.internStringIdItem(dexFile,name);
        return TypeIdItem.internTypeIdItem(dexFile,typeDescriptor);
    }


    public static CodeItem.EncodedTypeAddrPair copyEncodedTypeAddrPair(DexFile dexFile,CodeItem.EncodedTypeAddrPair handler){
        TypeIdItem exceptionType=copyTypeIdItem(dexFile,handler.exceptionType);
        return new CodeItem.EncodedTypeAddrPair(
                exceptionType,
                handler.getHandlerAddress()
                );
    }
    
    
    public static List<CodeItem.TryItem> copyTryItems(DexFile dexFile,List<CodeItem.TryItem> tryItems,List<CodeItem.EncodedCatchHandler> handlers){
        if(tryItems == null)
            return null;

        List<CodeItem.TryItem> out=new ArrayList<CodeItem.TryItem>(tryItems.size());
        for(CodeItem.TryItem tryItem : tryItems){
            out.add(copyTryItem(dexFile,tryItem,handlers));
        }
        return out;
    }
/* 
    public static List<CodeItem.EncodedCatchHandler> copyEncodedCatchHandlers(DexFile dexFile,List<CodeItem.EncodedCatchHandler> encodedCatchHandlers){
        if(encodedCatchHandlers == null)
            return null;

        List<CodeItem.EncodedCatchHandler> out=new ArrayList<CodeItem.EncodedCatchHandler>(encodedCatchHandlers.size());
        for(CodeItem.EncodedCatchHandler encodedCatchHandler : encodedCatchHandlers){
            out.add(copyEncodedCatchHandler(dexFile,encodedCatchHandler));
        }
System.out.println("encodedCatchHandler: "+out);
        return out;
    }
*/

    
    public static CodeItem.TryItem copyTryItem(DexFile dexFile,CodeItem.TryItem tryItem,List<CodeItem.EncodedCatchHandler> handlers){

                CodeItem.EncodedCatchHandler handler=copyEncodedCatchHandler(dexFile,tryItem.encodedCatchHandler);
                handlers.add(handler);

        return new CodeItem.TryItem(
                tryItem.getStartCodeAddress(),
                tryItem.getTryLength(),
                handler
                );
    }


    public static CodeItem.TryItem internTryItem(DexFile dexFile,int startCodeAddress,int tryLength,CodeItem.EncodedCatchHandler handler,List<CodeItem.EncodedCatchHandler> handlers){

                handlers.add(handler);

        return new CodeItem.TryItem(
                startCodeAddress,
                tryLength,
                handler
                );
    }


    public static CodeItem.EncodedTypeAddrPair internEncodedTypeAddrPair(DexFile dexFile,String exceptionType,int handlerAddress){
        return new CodeItem.EncodedTypeAddrPair(
                internTypeIdItem(dexFile,exceptionType),
                handlerAddress
                );
    }


    public static CodeItem.EncodedCatchHandler copyEncodedCatchHandler(DexFile dexFile,CodeItem.EncodedCatchHandler handler){
        if(handler.handlers ==null)
            return null;

        CodeItem.EncodedTypeAddrPair[] out=new CodeItem.EncodedTypeAddrPair[handler.handlers.length];
        for(int i=0;i<handler.handlers.length;i++){
            out[i]=copyEncodedTypeAddrPair(dexFile,handler.handlers[i]);
        }
        return new CodeItem.EncodedCatchHandler(
                out,
                handler.getCatchAllHandlerAddress()
                );
    }

    
    public static CodeItem.EncodedCatchHandler internEncodedCatchHandler(DexFile dexFile,CodeItem.EncodedTypeAddrPair[] handlers,int catchAllAddress){
        if(handlers ==null)
            return null;

        return new CodeItem.EncodedCatchHandler(
                handlers,
                catchAllAddress
                );
    }



    public static TypeIdItem copyTypeIdItem(DexFile dexFile,TypeIdItem typeDescriptor){
        return TypeIdItem.internTypeIdItem(
                dexFile,
                StringIdItem.internStringIdItem(dexFile,typeDescriptor.getTypeDescriptor())
                );
    }


    public static TypeListItem copyTypeListItem(DexFile dexFile,TypeListItem typeList){
        if(typeList == null)
            return null;


        List<TypeIdItem> types=typeList.getTypes();
        List<TypeIdItem> outs=new ArrayList<TypeIdItem>(types.size());
        for(TypeIdItem item : types){
            outs.add(copyTypeIdItem(dexFile,item));
        }
        return TypeListItem.internTypeListItem(dexFile,outs);
    }
    
    public static TypeListItem internTypeListItem(DexFile dexFile,String[] typeList){
        if(typeList ==null)
            return null;

        List<TypeIdItem> outs=new ArrayList<TypeIdItem>(typeList.length);
        for(String  item : typeList){
            outs.add(internTypeIdItem(dexFile,item));
        }
        return TypeListItem.internTypeListItem(dexFile,outs);
    }

    public static FieldIdItem copyFieldIdItem(DexFile dexFile,FieldIdItem field){
        if(field ==null)
            return null;

        TypeIdItem classType=copyTypeIdItem(dexFile,field.getContainingClass());
        TypeIdItem fieldType=copyTypeIdItem(dexFile,field.getFieldType());
        StringIdItem fieldName=StringIdItem.internStringIdItem(dexFile,field.getFieldName().getStringValue());
        return FieldIdItem.internFieldIdItem(
                dexFile,
                classType,
                fieldType,
                fieldName);
    }
    
    public static FieldIdItem internFieldIdItem(DexFile dexFile,String classType,String fieldType,String fieldName){

        return FieldIdItem.internFieldIdItem(
                dexFile,
                internTypeIdItem(dexFile,classType),
                internTypeIdItem(dexFile,fieldType),
                StringIdItem.internStringIdItem(dexFile,fieldName)
                );
    }
    
    public static MethodIdItem internMethodIdItem(DexFile dexFile,String classType,String returnType,String[] parameters,String methodName){

        ProtoIdItem methodPrototype=ProtoIdItem.internProtoIdItem(
                dexFile,
                internTypeIdItem(dexFile,returnType),
                internTypeListItem(dexFile,parameters)
                );
        return MethodIdItem.internMethodIdItem(
                dexFile,
                internTypeIdItem(dexFile,classType),
                methodPrototype,
                StringIdItem.internStringIdItem(dexFile,methodName)
                );
    }




    public static MethodIdItem copyMethodIdItem(DexFile dexFile,MethodIdItem method){
        if(method == null)
            return null;

        TypeIdItem classType=copyTypeIdItem(dexFile,method.getContainingClass());
        ProtoIdItem methodPrototype=method.getPrototype();
        TypeIdItem returnType=copyTypeIdItem(dexFile,methodPrototype.getReturnType());
        TypeListItem parameters=copyTypeListItem(dexFile,methodPrototype.getParameters());
        methodPrototype=ProtoIdItem.internProtoIdItem(dexFile,returnType,parameters);
        StringIdItem methodName=StringIdItem.internStringIdItem(dexFile,method.getMethodName().getStringValue());
        return MethodIdItem.internMethodIdItem(
                dexFile,
                classType,
                methodPrototype,
                methodName
                );
    }


    public void print(){
        for(Instruction instruction :instructions){
            System.out.println(instruction.opcode.name);
        }
    }

}
