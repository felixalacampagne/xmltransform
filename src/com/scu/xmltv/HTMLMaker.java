package com.scu.xmltv;
import java.io.File;
import com.scu.utils.CmdArgMgr;
import com.scu.utils.XMLTransform;

public class HTMLMaker
{
public final static String ARG_XMLFILE = "-xml";
public final static String ARG_XSLTFILE = "-xsl";
public final static String ARG_OUTDIR = "-out";
public final static String ARG_FAV = "-fav";
public final static String ARG_DEST = "-xfrm";


private String mFavListFile = null;
public String getFavListFile()
{
	return mFavListFile;
}
public void setFavListFile(String favListFile)
{
	mFavListFile = favListFile;
}
/**
 * @param args
 */
public static void main(String[] args)
{
   CmdArgMgr cmd = new CmdArgMgr();
   String xmlfile = null;
   String xsltfile = null;
   String outdir = null;
   String tfrmfile = null; // Will contain the actual output of the transform, ie. the list of favourites for tvguide.xsl
   String inifile = null;
   String [] keys = null;

      // Can't override the jar manifest start class by specifying
      // a class on the command line, so have to provide access from here!
      cmd.parseArgs(args);
      if(cmd.getArg(MergeFiles.ARG_OUTFILE) != null)
      {
         MergeFiles.main(args);
         System.exit(0);
      }
      else if(cmd.getArg(XMLTVSourceCombiner.ARG_OUTFILE) != null)
      {
      	try
			{
				XMLTVSourceCombiner.main(args);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
         System.exit(0);
      }
      keys = cmd.getArgNames();

      
      for(int i = 0; i<keys.length; i++)
      {
         String val = cmd.getArg(keys[i]);
         if(ARG_XMLFILE.compareTo(keys[i]) == 0)
            xmlfile = val;
         else if(ARG_XSLTFILE.compareTo(keys[i]) == 0)
            xsltfile = val;
         else if(ARG_OUTDIR.compareTo(keys[i]) == 0)
            outdir = val;
         else if(ARG_FAV.compareTo(keys[i]) == 0)
            inifile = val;
         else if(ARG_DEST.compareTo(keys[i]) == 0)
            tfrmfile = val;
      }

      
      if((xmlfile==null) || (xsltfile==null) || (outdir==null) || (inifile == null))
      {
         System.out.println("Usage: HTMLMaker " + ARG_XMLFILE + "=<XMLTV file> " +
                                                      ARG_XSLTFILE + "=<XSL file> " +
                                                      ARG_OUTDIR + "=<Output directory> " +
                                                      ARG_FAV + "=<favorites file> " +
                                                      "[" + ARG_DEST + "=<transform output file>]");
         System.exit(1);
      }

      HTMLMaker html = new HTMLMaker();
      html.setFavListFile(inifile);
      
      html.doTransform(xmlfile, xsltfile, outdir, tfrmfile);


}
public void doTransform(String xmlfile, String xsltfile, String outdir)
{
   doTransform(xmlfile, xsltfile, outdir, null);
}

public void doTransform(String xmlfile, String xsltfile, String outdir, String tfrmfile)
{
XMLTransform xmlt = new XMLTransform();
File f;
   if(tfrmfile == null)
   {
      tfrmfile = "favorites.htm";
   }
   try
   {
      f = new File(outdir, tfrmfile);
      xmlt.clearParameters();
      xmlt.addParameter("OUTPATH", outdir + File.separator);
      xmlt.addParameter("FAVFILE", mFavListFile);

      System.out.println("Generating listings."); 
      xmlt.transformXML(xmlfile, xsltfile, f.getAbsolutePath());

   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   catch(Error er)
   {
      er.printStackTrace();
   }   


}






}
