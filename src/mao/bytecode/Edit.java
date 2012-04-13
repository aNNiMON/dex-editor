
package mao.bytecode;

import java.util.List;
import java.io.IOException;
import java.io.OutputStream;

public interface Edit{
    public void read(List<String> data,byte[] input)throws IOException;
    public void write(String data,OutputStream output)throws IOException;
}
