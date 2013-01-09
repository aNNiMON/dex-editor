package mao.res;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.io.ByteArrayOutputStream;

import mao.util.LEDataInputStream;
import mao.util.LEDataOutputStream;

import org.jf.dexlib.Util.Utf8Utils;

public class StringBlock
{
    private int[] m_stringOffsets;
    private byte[] m_strings;
    private int[] m_styleOffsets;
    private int[] m_styles;
    private boolean m_isUTF8;
    private int styleOffsetCount;
    private int stylesOffset;
    private int stringsOffset;
    private int flags;
    private int chunkSize;


    private static final CharsetDecoder UTF16LE_DECODER = Charset.forName("UTF-16LE").newDecoder();
    private static final CharsetDecoder UTF8_DECODER = Charset.forName("UTF-8").newDecoder();
    
    private static final int CHUNK_STRINGBLOCK=1835009;
    private static final int IS_UTF8=0x100;

    public static StringBlock read(LEDataInputStream reader)
        throws IOException
    {
        reader.skipCheckInt(CHUNK_STRINGBLOCK);
        StringBlock block = new StringBlock();

        int chunkSize =block.chunkSize=reader.readInt();
                System.out.println("chunkSize "+chunkSize);

        int stringCount = reader.readInt();
              System.out.println("stringCount "+stringCount);

        int styleOffsetCount =block.styleOffsetCount= reader.readInt();
            System.out.println("styleOffsetCount "+styleOffsetCount);

        int flags = block.flags = reader.readInt();

        int stringsOffset =block.stringsOffset= reader.readInt();
          System.out.println("stringsOffset "+stringsOffset);

        int stylesOffset =block.stylesOffset= reader.readInt();
        System.out.println("stylesOffset "+stylesOffset);

        block.m_isUTF8 = ((flags & IS_UTF8) != 0);
        block.m_stringOffsets = reader.readIntArray(stringCount);
        if (styleOffsetCount != 0) {
            block.m_styleOffsets = reader.readIntArray(styleOffsetCount);
        }

        int size = (stylesOffset == 0 ? chunkSize : stylesOffset) - stringsOffset;
        if (size % 4 != 0) {
            throw new IOException("String data size is not multiple of 4 (" + size + ").");
        }
        block.m_strings = new byte[size];
        reader.readFully(block.m_strings);

        //        System.out.println("m_strings_size "+size);

        if (stylesOffset != 0) {
            size = chunkSize - stylesOffset;
            if (size % 4 != 0) {
                throw new IOException("Style data size is not multiple of 4 (" + size + ").");
            }
            block.m_styles = reader.readIntArray(size / 4);
            System.out.println("m_styles_size "+size);
        }
        System.out.println();

        return block;
    }

    public void getStrings(List<String> list){
        int size=getSize();
        for(int i=0;i<size;i++)
            list.add(Utf8Utils.escapeString(getString(i)));
    }


    public void write(List<String> list,LEDataOutputStream out)throws IOException{


        ByteArrayOutputStream outBuf=new ByteArrayOutputStream();
        LEDataOutputStream led=new LEDataOutputStream(outBuf);
        // stringCount
        int size=list.size();

        //m_stringOffsets
        int[] offset=new int[size];
        int len=0;

        //m_strings
        ByteArrayOutputStream bOut=new ByteArrayOutputStream();
        LEDataOutputStream mStrings=new LEDataOutputStream(bOut);
        for(int i=0;i<size;i++){
            offset[i]=len;
            String var=Utf8Utils.escapeSequence(list.get(i));
            char[] charbuf=var.toCharArray();
            mStrings.writeShort((short)charbuf.length);
            mStrings.writeCharArray(charbuf);
            mStrings.writeShort((short)0);
            len+=charbuf.length*2+4;
        }

        int m_strings_size=bOut.size();
        int size_mod=m_strings_size%4;//m_strings_size%4
        //padding 0
        if(size_mod !=0){
            for(int i=0;i<4-size_mod;i++){
                bOut.write(0);
            }
            m_strings_size+=4-size_mod;
        }
        byte[] m_strings=bOut.toByteArray();



        System.out.println("string chunk size: "+chunkSize);

        led.writeInt(size);
        led.writeInt(styleOffsetCount);
        led.writeInt(flags);

        led.writeInt(stringsOffset);
        led.writeInt(stylesOffset);

        led.writeIntArray(offset);
        if(styleOffsetCount!=0){
            System.out.println("write stylesOffset");
            led.writeIntArray(m_styleOffsets);
        }

        led.writeFully(m_strings);

        if(m_styles!=null){
            System.out.println("write m_styles");
            led.writeIntArray(m_styles);
        }
        out.writeInt(CHUNK_STRINGBLOCK);

        byte[] b=outBuf.toByteArray();
        out.writeInt(b.length+8);
        out.writeFully(b);
    }


    public int getChunkSize(){
        return chunkSize;
    }

    private String getString(int index)
    {
        if ((index < 0) || (this.m_stringOffsets == null) || (index >= this.m_stringOffsets.length))
        {
            return null;
        }
        int offset = this.m_stringOffsets[index];
        int length;
        if (!this.m_isUTF8) {
            length = getShort(this.m_strings, offset) * 2;
            offset += 2;
        } else {
            offset += getVarint(this.m_strings, offset)[1];
            int[] varint = getVarint(this.m_strings, offset);
            offset += varint[1];
            length = varint[0];
        }
        return decodeString(offset, length);
    }


    public int getSize(){
        return m_stringOffsets!=null?m_stringOffsets.length:0;
    }

    private String decodeString(int offset, int length) {
        try {
            return (this.m_isUTF8 ? UTF8_DECODER : UTF16LE_DECODER).decode(ByteBuffer.wrap(this.m_strings, offset, length)).toString();
        }
        catch (CharacterCodingException ex) {
        }return null;
    }

    private static final int getShort(byte[] array, int offset)
    {
        return (array[offset + 1] & 0xFF) << 8 | array[offset] & 0xFF;
    }

    private static final int[] getVarint(byte[] array, int offset)
    {
        int val = array[offset];
        boolean more = (val & 0x80) != 0;
        val &= 127;

        if (!more) {
            return new int[] { val, 1 };
        }
        return new int[] { val << 8 | array[(offset + 1)] & 0xFF, 2 };
    }
}
