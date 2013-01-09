package mao.util;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public final class LEDataOutputStream{

    private DataOutputStream dos;

    public LEDataOutputStream(OutputStream out){
        dos=new DataOutputStream(out);
    }

    public final void writeShort(short s)throws IOException{

        dos.writeByte(s&0xFF);
        dos.writeByte((s>>>8)&0xFF);
    }

    private final void writeChar(char c)throws IOException{

        dos.writeByte(c&0xFF);
        dos.writeByte((c>>>8)&0xFF);
    }
    public final void writeCharArray(char[] c)throws IOException{

        for(int i=0;i<c.length;i++){
            writeChar(c[i]);
        }
    }

    public final void writeFully(byte[] b)throws IOException{
        dos.write(b,0,b.length);
    }

    public final void writeInt(int i)throws IOException{
        dos.writeByte(i&0xFF);
        dos.writeByte((i>>>8)&0xFF);
        dos.writeByte((i>>>16)&0xFF);
        dos.writeByte((i>>>24)&0xFF);
    }

    private final void writeIntArray(int[] buf,int s,int end)throws IOException{
        for(int i=s;s<end;s++)
            writeInt(buf[s]);
    }

    public final void writeIntArray(int[] buf)throws IOException{
        writeIntArray(buf,0,buf.length);
    }

}
