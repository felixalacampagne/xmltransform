package com.scu.utils;
import java.io.*;

public class FileTools
{


public static void copyFile(String srcfile, String dstfile) throws IOException
{
    copyFile(new File(srcfile), new File(dstfile));
}

public static void copyFile(File srcfile, String dstfile) throws IOException
{
    copyFile(srcfile, new File(dstfile));
}

public static void copyFile(String srcfile, File dstfile) throws IOException
{
    copyFile(new File(srcfile), dstfile);
}




public static void copyFile(File srcfile, File dstfile) throws IOException
{
   FileOutputStream fosout = null;
   File actualdst = dstfile;
   FileInputStream fisin = new FileInputStream(srcfile);
   // If dstfile is actually a directory then need
   // to create the full name by combining the directory
   // and the source filename
   if(actualdst.isDirectory())
   {
       actualdst = new File(dstfile, srcfile.getName());
   }

   fosout = new FileOutputStream(actualdst);
   copyFile(fisin, fosout);
}

public static void copyFile(InputStream isin, OutputStream osout) throws IOException
{

byte buffer[] = new byte[1024 * 1024];
int bytesread = 0;

    for(bytesread = 1; bytesread > 0;)
    {
        bytesread = isin.read(buffer);
        if(bytesread > 0)
            osout.write(buffer, 0, bytesread);
    }
    osout.close();
    isin.close();
}

/**
 * @param aToWriteS - the string to be written to a file as
 *                    single byte characters
 * @param aFnameS - the name of the file which will be created in the
 *                  directory given by getPath(), or a full path spec,
 *                  or null to use a temporary file.
 * @return - a File object for the output file which is useful when
 *           a temporary file is used.
 */
public static File writeStringToFile(String aFnameS, String aToWriteS)
{
   return writeStringToFile(aFnameS, aToWriteS, null);
}

public static File writeStringToFile(String aFnameS, String aToWriteS, String encoding)
{
File outfile = null;
FileOutputStream fos=null;

    try
    {
        if(aFnameS == null)
        {
            outfile = File.createTempFile("cpa", ".tmp");
        }
        else
        {
            outfile = new File(aFnameS);
        }

        outfile.getParentFile().mkdirs();
        
       fos = new FileOutputStream(outfile);
       if(encoding == null)
          fos.write(aToWriteS.getBytes());
       else
          fos.write(aToWriteS.getBytes(encoding));
       fos.close();
    }
    catch(Exception ex)
    {
        ex.printStackTrace();
    }

    try { fos.close(); } catch(Exception e) {}
    return outfile;
}

public static String readStringFromFile(String fname)
{
   return readStringFromFile(fname, null);
}

public static String readStringFromFile(String fname, String charset)
{
FileInputStream fisin=null;
File datfile=null;
byte [] bytea=null;
    try
    {
       if(fname == null)
          return null;
       
        datfile = new File(fname);
        if(! datfile.exists())
        {
            System.out.println("Apparently " + datfile.getCanonicalPath() + " doesn't exist!");
        }
        else
        {
            //System.out.println("File length is " + datfile.length());
            bytea = new byte [ (int) datfile.length() ];

            fisin = new FileInputStream(datfile);
            //System.out.println("Reading file...");
            fisin.read(bytea);
            fisin.close();
            if(charset == null)
               return new String(bytea);
            else
               return new String(bytea, charset);
        }
    }
    catch(Exception e)
    {
        e.printStackTrace();
        System.out.println( "Failed to read from " + fname);
    }

    try { fisin.close(); } catch(Exception e) {}
    return new String("");
}

/*
public static String loadResourceFileAsString(String res)
{
InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(res);
String str = null;
byte [] bytea = new byte[1024];
int bytesread = 0;
StringBuffer sb = new StringBuffer();
    if(null != is)
    {
       try
       {
          for(bytesread = 1; bytesread > 0;)
          {
             bytesread = is.read(bytea);
             if(bytesread > 0)
                sb.append(new String(bytea, 0, bytesread));
          }
          is.close();
       }
       catch(Exception ex)
       {
          ex.printStackTrace();
       }
       return new String(sb.toString());

    }
    else
    {
      System.out.println("ResourceLoader:loadFile:: Resource not found: " + res);
    }

    return null;
}
*/


}
