package mao.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ZipExtract {

    public static void unzipAll(ZipFile zipFile,File outPath)throws Exception{
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            extractEntry(zipFile,entry,outPath);
        }
    }

    public static void extractEntry(ZipFile zipFile,ZipEntry entry,File outPath)throws Exception{
        File dest=new File(outPath,entry.getName());
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        if (dest.exists()) {
            dest.delete();
            dest.createNewFile();
        }
        
        FileOutputStream outStream=new FileOutputStream(dest);
        IOUtils.copy(zipFile.getInputStream(entry),outStream);
        outStream.close();

    }

    public static void extractEntryForByteArray(byte[] input,String entryName,File outPath)throws Exception{
        File dest=new File(outPath,entryName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        if (dest.exists()) {
            dest.delete();
            dest.createNewFile();
        }
        
        FileOutputStream outStream=new FileOutputStream(dest);
        outStream.write(input);
        outStream.close();

    }


}
