/*
 * aNNiMON 2012
 * For more info visit http://annimon.com/
 */
package mao.layoutviewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 * @author aNNiMON
 */
public class AndroidXml {
    
    public static final byte XML = 0, AXML = 1;
    private static final int AXML_FILE_SIGNATURE = 0x03000800;

    private byte xmlType;
    private ByteArrayInputStream bais;
    
    public static AndroidXml readFromStream(InputStream is) throws IOException {
        byte[] data = readData(is);
        
        AndroidXml androidXml = new AndroidXml(data);
        androidXml.readXmlType(data);
        return androidXml;
    }
    
    public static AndroidXml readFromArray(byte[] data) throws IOException {
        AndroidXml androidXml = new AndroidXml(data);
        androidXml.readXmlType(data);
        return androidXml;
    }
    
    private AndroidXml(byte[] data) {
        bais = new ByteArrayInputStream(data);
    }
    
    public String getText() throws IOException {
        if (getXmlType() == AXML) return getDecodedText();
        return getPlainText();
    }
    
    public byte getXmlType() {
        return xmlType;
    }
    
    public String getPlainText() throws IOException {
        InputStreamReader reader = new InputStreamReader(bais, Charset.defaultCharset());
        StringBuilder sb = new StringBuilder();
        
        int read;
        while ((read = reader.read()) != -1) {
            sb.append((char) read);
        }
        
        reader.close();
        return sb.toString();
    }
    
    public String getDecodedText() {
        return AXMLPrinter.decode(bais);
    }
    
    private void readXmlType(byte[] data) {
        if (data.length < 4) xmlType = XML;
        if (readSignature(data) == AXML_FILE_SIGNATURE) {
            xmlType = AXML;
        } else xmlType = XML;
    }
    
    private int readSignature(byte[] data) {
        int value = (data[0] << 24) |
                    (data[1] << 16) |
                    (data[2] << 8) |
                    (data[3]);
        return value;
    }
    
    private static byte[] readData(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        byte[] buffer = new byte[1024];
        while ( (length = is.read(buffer)) != -1 ) {
            baos.write(buffer, 0, length);
        }
        is.close();
        baos.flush();
        buffer = baos.toByteArray();
        baos.close();
        return buffer;
    }
}
