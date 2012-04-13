import org.jf.dexlib.*;
import org.jf.dexlib.Util.*;
import org.jf.util.IndentingWriter;
import org.jf.util.LiteralTools;
import org.jf.dexlib.EncodedValue.*;
import org.jf.dexlib.Code.*;
import org.jf.dexlib.Code.Format.*;
import java.util.*;
import java.util.regex.*;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.*;

public class Test{
    public static final String decimal_literal="[1-9]([0-9])*";
    public static final String hex_literal="([+,-])?0[x,X]([0-9,a-f,A-F])+";
    public static final String octal_literal="0([0-7])*";
    public static final String floating_literal="([0-9])+\\.([0-9])+([f,F,d,D])?";
    public static void main(String[] args)throws Exception{

        //buildParamterList("[Ljava/lang/String;[[IJ");
        /*
           parse(Opcode.MOVE,"move v1 v2",in);
           parse(Opcode.GOTO,"goto -0xf",in);
           parse(Opcode.CONST_4,"const/4 v0 7",in);
           parse(Opcode.GOTO_16,"goto/16 0xffffa",in);
           parse(Opcode.CONST_HIGH16,"const/high16 v0 -0xfff",in);
           System.out.println(in);
           */
        DexFile dex=new DexFile(FileUtils.readFile("/mnt/sdcard/demo/file/power.dex"));
        System.out.println(parseField(dex," iput-object v0, p0, Lcom/lzexe/AndroidResEdit/MyFileManager;->c:Ljava/util/List;"));
        System.out.println(parseMethod(dex," invoke-direct {v0, p1}, Ljava/io/File;-><init>()V"));
        String s="";
        /*List<Instruction> instructionsx=t(dex,s);
        for(Instruction instruction: instructionsx){
            Parser.dump(new IndentingWriter(new PrintWriter(System.out)),instruction);
        }
        */
        DexFile dex2=new DexFile();
        //dex2.setInplace(true);

        
        IndexedSection<ClassDefItem> classes=dex.ClassDefsSection;
        List<StringIdItem> stringids=new ArrayList<StringIdItem>();
        int i=0;
        for(ClassDefItem cl: classes.getItems()){

System.out.println(cl);
            ClassDataItem da=cl.getClassData();
            if(da !=null){
                System.out.println(da);
                ClassDataItem.EncodedMethod[] methods=cl.getClassData().getDirectMethods();
                for(ClassDataItem.EncodedMethod method :methods){
                    /*
                   Instruction[] instructions=method.codeItem.getInstructions();
                   for(Instruction instruction: instructions){
                       switch(instruction.getFormat()){
                           case Format21c:
                           case Format31c:
                               switch(instruction.opcode.referenceType){
                                   case string:
                                       InstructionWithReference ref=(InstructionWithReference)instruction;
                                       stringids.add((StringIdItem)ref.getReferencedItem());

                                       StringIdItem item=stringids.get(stringids.size()-1);
                                       item.setStringValue("hello");
                               }

                       }
                   }
                   */
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    CodeItem code=method.codeItem;
                    System.out.println(code);

                    Parser parser=new Parser(code);

                    StringBuilder sb=new StringBuilder();
                    parser.dump(new IndentingWriter(sb));
                    String ins=sb.toString();
                    System.out.println(ins);


                    //  ins=ins.replace("nop","");
                  //      ins=ins.replace("const-string v2 \"ja\"","const-string v2 \"1\"\nconst-string v2 \"hello world\"");

                    parser.parse(dex2,ins);
                    //     code.fixInstructions(true,false);
                    //                System.out.println(instructionsx);
                }
                
                for(ClassDataItem.EncodedMethod method :cl.getClassData().getVirtualMethods()){
                    /* 
                    Instruction[] instructions=method.codeItem.getInstructions();
                   for(Instruction instruction: instructions){
                       switch(instruction.getFormat()){
                           case Format21c:
                           case Format31c:
                               switch(instruction.opcode.referenceType){
                                   case string:
                                       InstructionWithReference ref=(InstructionWithReference)instruction;
                                       stringids.add((StringIdItem)ref.getReferencedItem());
                               }

                       }
                   }
                   */

                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    CodeItem code=method.codeItem;
                    System.out.println(code);

                    Parser parser=new Parser(code);

                    StringBuilder sb=new StringBuilder();
                    parser.dump(new IndentingWriter(sb));
                    String ins=sb.toString();

                    //ins=ins.replace("nop","");
                    // ins=ins.replace("const-string v2 \"ja\"","const-string v2 \"1\"\nconst-string v2 \"hello world\"");
                    System.out.println(ins);

                    parser.parse(dex2,ins);
                    //code.fixInstructions(true,false);
                    //               System.out.println(ins);
                }
            }

            cl.internClassDefItem(dex2);
           /* 
            ClassEditor editor=new ClassEditor(cl);
            editor.print();
            editor.copyClassDefItem(dex2);
            
               for(ClassDataItem.EncodedField field: editor.staticFields){
               FieldEditor fieldeditor=new FieldEditor(field);
               fieldeditor.print();
               }
               for(ClassDataItem.EncodedField field: editor.instanceFields){
               FieldEditor fieldeditor=new FieldEditor(field);
               fieldeditor.print();
               }
               for(ClassDataItem.EncodedMethod method : editor.directMethods){
                   MethodEditor methodEditor=new MethodEditor(method);
                   ByteArrayOutputStream baos=new ByteArrayOutputStream();
                   List<Instruction> instructions=methodEditor.codeEditor.instructions;
                   for(Instruction instruction: instructions){
                       //    System.out.println(instruction.opcode.name);
                       Parser.dump(new IndentingWriter(new PrintWriter(baos)),instruction);
                   }
                   String ins=new String(baos.toByteArray());
                   System.out.println(ins);

                   List<Instruction> instructionsx=t(dex3,ins);
                   methodEditor.codeEditor.instructions=instructionsx;
                   System.out.println("instructions "+methodEditor.codeEditor.instructions.equals(instructionsx));
        //           

                   methodEditor.print();
                   methodEditor.copyEncodedMethod(dex2);
               }
               for(ClassDataItem.EncodedMethod method : editor.virtualMethods){
                   ByteArrayOutputStream baos=new ByteArrayOutputStream();
                   MethodEditor methodEditor=new MethodEditor(method); 
                   List<Instruction> instructions=methodEditor.codeEditor.instructions;
                   for(Instruction instruction: instructions){
                       //    System.out.println(instruction.opcode.name);
                       Parser.dump(new IndentingWriter(new PrintWriter(baos)),instruction);
                   }
                   String ins=new String(baos.toByteArray());
                   System.out.println(ins);

                   List<Instruction> instructionsx=t(dex,ins);
                   methodEditor.codeEditor.instructions=instructionsx;


                   methodEditor.print();
                   methodEditor.copyEncodedMethod(dex2);
               }
         */      

        }
        for(StringIdItem  item:stringids){
            System.out.println(item);
        }
        /*
        for(StringIdItem item: dex2.StringIdsSection.getItems()){
            System.out.println(item);
        }
        for(MethodIdItem item: dex2.MethodIdsSection.getItems()){
            System.out.println(item);
        }
        */


        /*
           System.out.println(cl);
           TypeIdItem classType=cl.getClassType();
           ClassDataItem da=cl.getClassData();
           ClassDataItem.EncodedMethod[] sm=da.getDirectMethods();
           ClassDataItem.EncodedMethod[] vm=da.getVirtualMethods();
           List<ClassDataItem.EncodedMethod> stm=Arrays.asList(sm);
           List<ClassDataItem.EncodedMethod> vim=Arrays.asList(vm);
           List<ClassDataItem.EncodedField> fields=new ArrayList<ClassDataItem.EncodedField>();
           ClassDataItem.EncodedField[] staticfields=da.getStaticFields();
           ClassDataItem.EncodedField[] virfields=da.getInstanceFields();
           System.out.println(""+staticfields.length);
           System.out.println(""+virfields.length);
           fields.add(new ClassDataItem.EncodedField(FieldIdItem.internFieldIdItem(dex,classType,TypeIdItem.internTypeIdItem(dex,"I"),StringIdItem.internStringIdItem(dex,"test")),1));
        //ClassDataItem dan=ClassDataItem.internClassDataItem(dex,null,null,stm,vim);
        //  ClassDefItem cln=ClassDefItem.internClassDefItem(dex,classType,1,cl.getSuperclass(),null,cl.getSourceFile(),null,dan,null);
        //System.out.println(""+(cln.equals(cl)));

        //System.out.println(""+(da.equals(dan)));

        ClassDefItem.internClassDefItem(dex,TypeIdItem.internTypeIdItem(dex,"MyTest"),1,TypeIdItem.internTypeIdItem(dex,"Ljava/lang/Object;"),null,null,null,null,null);

        /*
        Section sec=dex.getSectionForType(ItemType.TYPE_STRING_ID_ITEM);
        List<StringIdItem> str=sec.getItems();
        //    str.add(StringIdItem.internStringIdItem(dex,"maokaijaka"));
        int c=0;
        for(StringIdItem item: str){

        System.out.println(c+++" "+item.getStringValue());
        if(item.getStringValue().equals("/")){
        //item.setStringValue("/sdcard");
        System.out.println(item.getStringValue());
        }

        }

*/
        dex2.setSortAllItems(true);
        dex2.place();
        System.out.println("dexfile size : "+dex2.getFileSize());

        byte[] buf=new byte[dex2.getFileSize()];
        ByteArrayAnnotatedOutput out=new ByteArrayAnnotatedOutput(buf);
        dex2.writeTo(out);
        byte[] buf2=out.toByteArray();
        System.out.println(""+Arrays.equals(buf,buf2));
        System.out.println(""+buf2.length);
        DexFile.calcSignature(buf2);
        DexFile.calcChecksum(buf2);
        FileOutputStream w=new FileOutputStream("/mnt/sdcard/classes.dex");
        w.write(buf2);
        w.close();
        System.out.println(""+parseInt(" 0x16"));
        System.out.println(Parser.escapeSequence("\\u6211\\n"));
        System.out.println(""+Opcode.getOpcodeByName("pswitch_data"));
    }

    public static void p(Instruction instruction){
        switch(instruction.opcode.format){
            case Format10x:
                if(instruction.opcode==Opcode.NOP){
                    if(instruction instanceof PackedSwitchDataPseudoInstruction){
                        PackedSwitchDataPseudoInstruction pswitch=(PackedSwitchDataPseudoInstruction)instruction;
                        System.out.print("packed-switch ");
                        int first=pswitch.getFirstKey();
                        System.out.println(""+first);
                        for(int key:pswitch.getTargets()){
                            System.out.println("pswitch_"+key);
                        }

                    }
                }else{
                    System.out.println(instruction.opcode.name+" ");
                }
                break;
            case Format10t:
                Instruction10t ins10x=(Instruction10t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.println(ins10x.getTargetAddressOffset()+"");
                break;
            case Format11x:
                Instruction11x ins11x=(Instruction11x)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.println("v"+ins11x.getRegisterA());
                break;
            case Format11n:
                Instruction11n ins11n=(Instruction11n)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins11n.getRegisterA());
                System.out.println(" "+ins11n.getLiteral());
                break;
            case Format12x:
                Instruction12x ins12x=(Instruction12x)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins12x.getRegisterA());
                System.out.println(" v"+ins12x.getRegisterB());
                break;
            case Format20t:
                Instruction20t ins20t=(Instruction20t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.println(ins20t.getTargetAddressOffset()+"");
                break;
            case Format21t:
                Instruction21t ins21t=(Instruction21t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins21t.getRegisterA()+" ");
                System.out.println(ins21t.getTargetAddressOffset()+"");
                break;
            case Format21c:
                Instruction21c ins21c=(Instruction21c)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins21c.getRegisterA()+" ");
                System.out.println(ins21c.getReferencedItem()+"");
                break;
            case Format21h:
                Instruction21h ins21h=(Instruction21h)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins21h.getRegisterA()+" ");
                System.out.println(ins21h.getLiteral()+"");
                break;
            case Format21s:
                Instruction21s ins21s=(Instruction21s)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins21s.getRegisterA()+" ");
                System.out.println(ins21s.getLiteral()+"");
                break;
            case Format22x:
                Instruction22x ins22x=(Instruction22x)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins22x.getRegisterA()+" ");
                System.out.println(" v"+ins22x.getRegisterB()+"");
                break;
            case Format22t:
                Instruction22t ins22t=(Instruction22t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins22t.getRegisterA()+" ");
                System.out.print(" v"+ins22t.getRegisterB()+" ");
                System.out.println(ins22t.getTargetAddressOffset()+"");
                break;
            case Format22b:
                Instruction22b ins22b=(Instruction22b)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins22b.getRegisterA()+" ");
                System.out.print(" v"+ins22b.getRegisterB()+" ");
                System.out.println(ins22b.getLiteral()+"");
                break;
            case Format22c:
                Instruction22c ins22c=(Instruction22c)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins22c.getRegisterA()+" ");
                System.out.print(" v"+ins22c.getRegisterB());
                System.out.println(" "+ins22c.getReferencedItem());
                break;
            case Format22s:
                Instruction22s ins22s=(Instruction22s)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins22s.getRegisterA()+" ");
                System.out.print(" v"+ins22s.getRegisterB());
                System.out.println(" "+ins22s.getLiteral());
                break;
            case Format23x:
                Instruction23x ins23x=(Instruction23x)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins23x.getRegisterA()+" ");
                System.out.print(" v"+ins23x.getRegisterB()+"");
                System.out.println(" v"+ins23x.getRegisterC()+"");
                break;
            case Format30t:
                Instruction30t ins30t=(Instruction30t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.println(ins30t.getTargetAddressOffset()+"");
                break;
            case Format31t:
                Instruction31t ins31t=(Instruction31t)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.println(ins31t.getTargetAddressOffset()+"");
                break;
            case Format31c:
                Instruction31c ins31c=(Instruction31c)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins31c.getRegisterA()+" ");
                System.out.println(" "+ins31c.getReferencedItem());
                break;
            case Format31i:
                Instruction31i ins31i=(Instruction31i)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins31i.getRegisterA()+" ");
                System.out.println(" "+ins31i.getLiteral());
                break;
            case Format32x:
                Instruction32x ins32x=(Instruction32x)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins32x.getRegisterA()+" ");
                System.out.println(" "+ins32x.getRegisterB());
                break;
            case Format35c:
                Instruction35c ins35c=(Instruction35c)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print(" {");
                int count=ins35c.getRegCount();
                for(int i=0;i<count;i++){
                    switch(i){
                        case 0:
                            System.out.print("v"+ins35c.getRegisterD()+" ");
                            break;
                        case 1:
                            System.out.print("v"+ins35c.getRegisterE()+" ");
                            break;
                        case 2:
                            System.out.print("v"+ins35c.getRegisterF()+" ");
                            break;
                        case 3:
                            System.out.print("v"+ins35c.getRegisterG()+" ");
                            break;
                        case 4:
                            System.out.print("v"+ins35c.getRegisterA()+" ");
                            break;
                    }
                }
                System.out.print(" }");
                System.out.println(" "+ins35c.getReferencedItem());
                break;
            case Format35s:
                Instruction35s ins35s=(Instruction35s)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print(" {");
                int c=ins35s.getRegCount();
                for(int i=0;i<c;i++){
                    switch(i){
                        case 0:
                            System.out.print("v"+ins35s.getRegisterD()+" ");
                            break;
                        case 1:
                            System.out.print("v"+ins35s.getRegisterE()+" ");
                            break;
                        case 2:
                            System.out.print("v"+ins35s.getRegisterF()+" ");
                            break;
                        case 3:
                            System.out.print("v"+ins35s.getRegisterG()+" ");
                            break;
                        case 4:
                            System.out.print("v"+ins35s.getRegisterA()+" ");
                            break;
                    }
                }
                System.out.print(" }");
                System.out.println(" "+ins35s.getReferencedItem());
                break;
            case Format3rc:
                Instruction3rc ins3rc=(Instruction3rc)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print(" {");
                int rc=ins3rc.getRegCount();
                for(int i=0;i<rc;i++){
                    System.out.print("v"+(ins3rc.getStartRegister()+i)+" ");
                }
                System.out.print(" }");
                System.out.println(" "+ins3rc.getReferencedItem());
                break;
            case Format51l:
                Instruction51l ins51l=(Instruction51l)instruction;
                System.out.print(instruction.opcode.name+" ");
                System.out.print("v"+ins51l.getRegisterA()+" ");
                System.out.println(" "+ins51l.getLiteral());
                break;



  

        }
    }

/*
    public void myp(){

        switch(Opcode.getOpcodeByName("").format){
            case Format10t:
                writeOpcode(writer);
                writer.write(' ');
                writeTargetLabel(writer);
                return true;
            case Format10x:
                writeOpcode(writer);
                return true;
            case Format11n:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeLiteral(writer);
                return true;
            case Format11x:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                return true;
            case Format12x:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                return true;
            case Format20t:
            case Format30t:
                writeOpcode(writer);
                writer.write(' ');
                writeTargetLabel(writer);
                return true;
            case Format21c:
            case Format31c:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeReference(writer);
                return true;
            case Format21h:
            case Format21s:
            case Format31i:
            case Format51l:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeLiteral(writer);
                return true;
            case Format21t:
            case Format31t:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeTargetLabel(writer);
                return true;
            case Format22b:
            case Format22s:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                writer.write(", ");
                writeLiteral(writer);
                return true;
            case Format22c:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                writer.write(", ");
                writeReference(writer);
                return true;
            case Format22cs:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                writer.write(", ");
                writeFieldOffset(writer);
                return true;
            case Format22t:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                writer.write(", ");
                writeTargetLabel(writer);
                return true;
            case Format22x:
            case Format32x:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                return true;
            case Format23x:
                writeOpcode(writer);
                writer.write(' ');
                writeFirstRegister(writer);
                writer.write(", ");
                writeSecondRegister(writer);
                writer.write(", ");
                writeThirdRegister(writer);
                return true;
            case Format35c:
            case Format35s:
                writeOpcode(writer);
                writer.write(' ');
                writeInvokeRegisters(writer);
                writer.write(", ");
                writeReference(writer);
                return true;
            case Format35ms:
                writeOpcode(writer);
                writer.write(' ');
                writeInvokeRegisters(writer);
                writer.write(", ");
                writeVtableIndex(writer);
                return true;
            case Format3rc:
                writeOpcode(writer);
                writer.write(' ');
                writeInvokeRangeRegisters(writer);
                writer.write(", ");
                writeReference(writer);
                return true;
            case Format3rms:
                writeOpcode(writer);
                writer.write(' ');
                writeInvokeRangeRegisters(writer);
                writer.write(", ");
                writeVtableIndex(writer);
                return true;
        }
    }
    */

    public static List<Instruction> t(DexFile dexFile,String s)throws Exception{
        String[] str=s.split("\n");
        List<Instruction> instructions=new ArrayList<Instruction>(str.length);
        for(String content: str){
            content=content.trim();
            if(content==null
                    ||content.equals("")){
                continue;
                    }
            int i=content.indexOf(' ');
            String opcodeName="";
            if(i!=-1)
                opcodeName=content.substring(0,i);
            else
                opcodeName=content.trim();
            Opcode opcode=Opcode.getOpcodeByName(opcodeName);
            System.out.println(opcode);
            System.out.println(opcode.format);
            parse(dexFile,opcode,content,instructions);
        }
        return instructions;
    }


    public static void parse(DexFile dexFile,Opcode opcode,String str,List<Instruction> instructions)throws Exception{
        switch(opcode.format){
            case Format10x:
                {
                    instructions.add(new Instruction10x(opcode));
                    break;
                }
            case Format10t:
                {
                    Instruction10t ins=new Instruction10t(opcode,(byte)parseInt(str));

              //      System.out.println(""+ins.getTargetAddressOffset()); 
                    instructions.add(ins);
                    break;
                }
            case Format11n:
                {
                    byte regA=(byte)parseSingleRegister(str);
                    byte litB=(byte)parseInt(str);
                    Instruction11n ins=new Instruction11n(opcode,regA,litB);
                    instructions.add(ins);
                  //  System.out.println(""+ins.getLiteral()); 

                    break;
                }
            case Format11x:
                {
                    short regA=(short)parseSingleRegister(str);
                    instructions.add(new Instruction11x(opcode,regA));
                    break;
                }
            case Format12x:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    byte regA=(byte)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    byte regB=(byte)Integer.parseInt(m.group().substring(1));
                    instructions.add(new Instruction12x(opcode,regA,regB));
                    break;
                }
            case Format20t:
                {
                    short offset=(short)(parseInt(str)&0xFFFF);
                    Instruction20t ins=new Instruction20t(opcode,offset);

                //    System.out.println(""+ins.getTargetAddressOffset()); 
                    instructions.add(ins);
                    break;
                }
            case Format21c:
                {
                    short regA=(short)parseSingleRegister(str);
                    Item item=null;
                    switch(opcode.referenceType){
                        case field:
                            item=parseField(dexFile,str);
                            break;
                        case string:
                            item=parseString(dexFile,str);
                            break;
                        case type:
                            item=parseType(dexFile,str);
                            break;
                    }
                    instructions.add(new Instruction21c(opcode,regA,item));
                break;
                }
            case Format21h:
                {
                    short regA=(short)parseSingleRegister(str);
                    short litB=(short)(parseInt(str)&0xFFFF);
                    Instruction21h ins=new Instruction21h(opcode,regA,litB);
                    instructions.add(ins);
              //      System.out.println(""+ins.getLiteral()); 
                    break;
                }
            case Format21s:
                {
                    short regA=(short)parseSingleRegister(str);
                    short litB=(short)(parseInt(str)&0xFFFF);
                    Instruction21s ins=new Instruction21s(opcode,regA,litB);
                    instructions.add(ins);
            //        System.out.println(""+ins.getLiteral()); 
                    break;
                }
            case Format21t:
                {
                    short regA=(short)parseSingleRegister(str);
                    short offset=(short)(parseInt(str)&0xFFFF);
                    Instruction21t ins=new Instruction21t(opcode,regA,offset);
                    instructions.add(ins);
          //          System.out.println(""+ins.getTargetAddressOffset()); 
                    break;
                }
            case Format22b:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    short regA=(short)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    short regB=(short)Integer.parseInt(m.group().substring(1));
                    byte litC=(byte)(parseInt(str)&0xFF);
                    instructions.add(new Instruction22b(opcode,regA,regB,litC));
                    break;
                }
            case Format22s:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    byte regA=(byte)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    byte regB=(byte)Integer.parseInt(m.group().substring(1));
                    short litC=(short)(parseInt(str)&0xFFFF);
                    instructions.add(new Instruction22s(opcode,regA,regB,litC));
                    break;
                }
            case Format22t:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    byte regA=(byte)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    byte regB=(byte)Integer.parseInt(m.group().substring(1));
                    short offset=(short)(parseInt(str)&0xFFFF);
                    instructions.add(new Instruction22t(opcode,regA,regB,offset));
                    break;
                }
            case Format22c:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    byte regA=(byte)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    byte regB=(byte)Integer.parseInt(m.group().substring(1));

                    Item item=null;
                    switch(opcode.referenceType){
                        case field:
                            item=parseField(dexFile,str);
                            break;
                        case type:
                            item=parseType(dexFile,str);
                            break;
                    }
                    instructions.add(new Instruction22c(opcode,regA,regB,item));
                break;
                }
            case Format22x:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    short regA=(short)(Integer.parseInt(m.group().substring(1))&0xFF);
                    if(!m.find());
                    int regB=Integer.parseInt(m.group().substring(1))&0xFFFF;
                    instructions.add(new Instruction22x(opcode,regA,regB));
                    break;
                }
            case Format23x:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    short regA=(short)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    short regB=(short)Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    short regC=(short)Integer.parseInt(m.group().substring(1));
                    instructions.add(new Instruction23x(opcode,regA,regB,regC));
                    break;
                }
            case Format30t:
                {
                    Instruction30t ins=new Instruction30t(opcode,parseInt(str));

                    instructions.add(ins);
                    break;
                }
            case Format31c:
                {
                    short regA=(short)parseSingleRegister(str);
                    Item item=parseString(dexFile,str);
                    instructions.add(new Instruction31c(opcode,regA,item));
                    break;
                }
            case Format31i:
                {
                    byte regA=(byte)parseSingleRegister(str);
                    int litB=parseInt(str);
                    Instruction31i ins=new Instruction31i(opcode,regA,litB);
                    instructions.add(ins);
                    break;
                }
            case Format31t:
                {
                    short regA=(short)parseSingleRegister(str);
                    int offset=parseInt(str);
                    Instruction31t ins=new Instruction31t(opcode,regA,offset);
                    instructions.add(ins);
         //           System.out.println(""+ins.getTargetAddressOffset()); 
                    break;
                }
            case Format32x:
                {
                    Pattern p=Pattern.compile("v\\d+");
                    Matcher m=p.matcher(str);
                    if(!m.find());
                    int regA=Integer.parseInt(m.group().substring(1));
                    if(!m.find());
                    int  regB=Integer.parseInt(m.group().substring(1));
                    instructions.add(new Instruction32x(opcode,regA,regB));
                    break;
                }
            case Format35c:
                {
                    int[] regCount=new int[1];
                    byte[] regs=parseFiveRegister(str,regCount);
                    Item item=null;
                    switch(opcode.referenceType){
                        case method:
                            item=parseMethod(dexFile,str);
                            break;
                        case type:
                            item=parseType(dexFile,str);
                            break;
                    }
                    instructions.add(new Instruction35c(opcode,regCount[0],regs[0],regs[1],regs[2],regs[3],regs[4],item));
                    break;
                }
            case Format35s:
                break;
            case Format3rc:
                {
                    int[] regs=parseRangeRegister(str);

                    Item item=null;
                    switch(opcode.referenceType){
                        case method:
                            item=parseMethod(dexFile,str);
                            break;
                        case type:
                            item=parseType(dexFile,str);
                            break;
                    }
                    instructions.add(new Instruction3rc(opcode,(short)regs[0]/*register count*/,regs[1]/*start register*/,item));
                    break;
                }
            case Format51l:
                {
                    short regA=(short)parseSingleRegister(str);
                    long litB=parseLong(str);
                    Instruction51l ins=new Instruction51l(opcode,regA,litB);
                    instructions.add(ins);
        //            System.out.println(""+ins.getLiteral()); 
                    break;
                }



        }
    }


    private static byte[] parseFiveRegister(String s,int[] len){
        byte[] regs=new byte[5];
        Arrays.fill(regs,(byte)0);
        Pattern p=Pattern.compile("v\\d+");
        Matcher m=p.matcher(s);
        int i=0;
        while(m.find()&&i<5){
            regs[i++]=(byte)Integer.parseInt(m.group().substring(1));
        }
        len[0]=i;
        return regs;
    }

    
    private static int[] parseRangeRegister(String s){
        int[] regs=new int[2];//regs[0] reg count regs[1] start reg
        Pattern p=Pattern.compile("v\\d+");
        Matcher m=p.matcher(s);
        if(!m.find());
        regs[1]=Integer.parseInt(m.group().substring(1));
        int i=0;
        while(m.find()){
            i=Integer.parseInt(m.group().substring(1));
        }
        regs[0]=i-regs[1]+1;//reg count
        return regs;
    }



    private static FieldIdItem parseField(DexFile dexFile,String s){

        Pattern p=Pattern.compile("\\s|:|->");
        String[] strs=p.split(s);
       // for(String a:strs)
         //   System.out.println(a.trim());
        int i=strs.length-1;
        if(i<2)
            throw new RuntimeException("FieldIdItem error: "+s);

        String classType=strs[i-2];
        String name=strs[i-1];
        String type=strs[i];
        //                System.out.println(classType+"  "+name+"   "+type);
        return FieldIdItem.internFieldIdItem(
                dexFile,
                TypeIdItem.internTypeIdItem(
                    dexFile,
                    classType),
                TypeIdItem.internTypeIdItem(
                    dexFile,
                    type),
                StringIdItem.internStringIdItem(
                    dexFile,
                    name)
                );
    }


    private static MethodIdItem parseMethod(DexFile dexFile,String s){

        Pattern p=Pattern.compile("\\s|\\(|\\)|->");
        String[] strs=p.split(s);
        int i=strs.length-1;
        if(i<3)
            throw new RuntimeException("MethodIdItem error: "+s);
        String classType=strs[i-3];
        String name=strs[i-2];
        TypeListItem params=buildParamterList(dexFile,strs[i-1]);
        String returnType=strs[i];
        //    System.out.println(classType+"   "+name+"   "+strs[i-1]+"   "+returnType);
        ProtoIdItem proto=ProtoIdItem.internProtoIdItem(
                dexFile,
                TypeIdItem.internTypeIdItem(
                    dexFile,
                    returnType),
                params
                );
        return MethodIdItem.internMethodIdItem(
                dexFile,
                TypeIdItem.internTypeIdItem(
                    dexFile,
                    classType),
                proto,
                StringIdItem.internStringIdItem(
                    dexFile,
                    name)
                );
    }


    private static TypeIdItem parseType(DexFile dexFile,String s){
        s=s.trim();
        int i=s.lastIndexOf(" ");
        String type =s.substring(i+1);
        return TypeIdItem.internTypeIdItem(
                dexFile,
                type
                );
    }

    private static StringIdItem parseString(DexFile dexFile,String s){
        int i=s.indexOf("\"");
        int j=s.lastIndexOf("\"");
        return StringIdItem.internStringIdItem(
                dexFile,
                s.substring(i+1,j)
                );
    }


    private static int parseInt(String s){
        Pattern p=Pattern.compile("\\s"+hex_literal+"|\\s([+,-])?\\d+");
        Matcher m=p.matcher(s);
        if(!m.find());
        return LiteralTools.parseInt(m.group().trim());

    }

    private static long parseLong(String s){
        Pattern p=Pattern.compile("\\s"+hex_literal+"([l,L])?|\\s([+,-])?\\d+([l,L])?");
        Matcher m=p.matcher(s);
        if(!m.find());
        return LiteralTools.parseLong(m.group().trim());

    }

    private static int parseSingleRegister(String s){
        Pattern p=Pattern.compile("v\\d+");
        Matcher m=p.matcher(s);
        
        if(!m.find())
            ;
        /*
        while(m.find()){
            System.out.println(m.group());
        }
        */
        return Integer.parseInt(m.group().substring(1));
    }

    private static TypeListItem buildParamterList(DexFile dexFile,String str){
        List<TypeIdItem> typeList=new ArrayList<TypeIdItem>();
        int typeStartIndex=0;
        while(typeStartIndex<str.length()){
            switch(str.charAt(typeStartIndex)){
                case 'Z':
                case 'B':
                case 'S':
                case 'C':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                    typeList.add(TypeIdItem.internTypeIdItem(
                                dexFile,
                                str.substring(typeStartIndex,++typeStartIndex)
                                )
                            );
                    break;
                case 'L':
                    int i=typeStartIndex;
                    while(str.charAt(++typeStartIndex) != ';');
                    typeList.add(TypeIdItem.internTypeIdItem(
                                dexFile,
                                str.substring(i,++typeStartIndex)
                                )
                            );
                    break;
                case '[':
                    int j=typeStartIndex;
                    while(str.charAt(++typeStartIndex) == '[');
                    if(str.charAt(typeStartIndex++) == 'L'){
                        while(str.charAt(typeStartIndex++) != ';');
                    }
                    typeList.add(TypeIdItem.internTypeIdItem(
                                dexFile,
                                str.substring(j,typeStartIndex)
                                )
                            );
                    break;
                default:
                    throw new RuntimeException("Invalid param list");
            }
        }
        if(typeList.size() ==0)
            return null;

        return TypeListItem.internTypeListItem(
                dexFile,
                typeList
                );
    }


    
}
