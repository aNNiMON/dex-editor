
package mao.res;

import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;

import mao.bytecode.Edit;


public class AXmlEditor implements Edit {
    private AXmlDecoder axml;

    public void read(final List<String> data, byte[] input)throws IOException{
        axml=AXmlDecoder.read(new ByteArrayInputStream(input));
        axml.mTableStrings.getStrings(data);
    }

    public void write(String data,OutputStream out)throws IOException{
        String[] strings=data.split("\n");
        List<String> list=new ArrayList<String>(strings.length);
        for(String str: strings){
            list.add(str);
        }
        axml.write(list,out);
    }

}
