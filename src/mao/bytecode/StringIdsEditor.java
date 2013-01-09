package mao.bytecode;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.io.OutputStream;

import org.jf.dexlib.*;
import org.jf.dexlib.Code.*;
import org.jf.dexlib.Util.*;


public class StringIdsEditor implements Edit{
    private ArrayList<StringIdItem> stringIds;

    public void read(List<String> data,byte[] input)throws IOException{
        HashMap<StringIdItem,StringIdItem> stringIdsMap=new HashMap<StringIdItem,StringIdItem>();

        List<ClassDefItem> classes=ClassListActivity.dexFile.ClassDefsSection.getItems();
        for(ClassDefItem classItem:classes){
            ClassDataItem classData=classItem.getClassData();
            if(classData !=null){
                //
                ClassDataItem.EncodedMethod[] methods=classData.getDirectMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    filterString(method,stringIdsMap);
                }
                //virtual methods
                methods=classData.getVirtualMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    filterString(method,stringIdsMap);
                }

            }
        }

        //
        ArrayList<StringIdItem> stringIds=new ArrayList<StringIdItem>();
        for(StringIdItem stringId:stringIdsMap.keySet()){
            stringIds.add(stringId);
            data.add(Utf8Utils.escapeString(stringId.getStringValue()));
        }
        this.stringIds=stringIds;
    }

    private static void filterString(ClassDataItem.EncodedMethod method,HashMap<StringIdItem,StringIdItem> stringIdsMap){
        if(method.codeItem!=null){
            Instruction[] instructions=method.codeItem.getInstructions();
            for(Instruction instruction:instructions){
                switch(instruction.getFormat()){
                    case Format21c:
                    case Format31c:
                        switch(instruction.opcode.referenceType){
                            case string:
                                InstructionWithReference ref=(InstructionWithReference)instruction;
                                stringIdsMap.put((StringIdItem)ref.getReferencedItem(),null);
                        }

                }
            }
        }
    }

    public void write(String data,OutputStream out)throws IOException{
        ArrayList<StringIdItem> stringIds=this.stringIds;
        String[] strings=data.split("\n");
        if(strings.length!=stringIds.size())
            throw new IOException("strings length != stringIds length");
        for(int i=0,len=stringIds.size();i<len;i++){
            StringIdItem item=stringIds.get(i);
            item.setStringValue(Utf8Utils.escapeSequence(strings[i]));

        }
        ClassListActivity.isChanged=true;
    }
}
