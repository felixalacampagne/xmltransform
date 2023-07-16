package com.scu.xmltv;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.scu.utils.CmdArgMgr;
import com.scu.utils.NodeUtils;
import com.scu.utils.XMLTransform;

// Stupid class to merge multiple XMLTV files into one file for
// processing by the HTML generator.
// This is a temporary solution (likely to become a permanent one) while
// I think of an elegant way to handle multiple XMLTV sources and the
// one single favourites file....
public class MergeFiles
{
Logger log = LoggerFactory.getLogger(this.getClass());	
public final static String ARG_OUTFILE = "-merge";
private List<String> mFiles = new ArrayList<String>();
private String mOutFile = null;
final NodeUtils nu = NodeUtils.getNodeUtils();

public MergeFiles(String outfile)
{
   mOutFile = outfile;
}

public void add(String infile)
{
   mFiles.add(infile);
}

// Quick n' Dirty, all done in memory for now!
// Damn, need to parse the files cause there are mixed
// encodings!

public void merge()
{
StringBuffer sbchans = new StringBuffer();
StringBuffer sbprogs = new StringBuffer();
String inxmltv = null;
Document doc = null;
int chns = 0;
int progs = 0;
int tve = 0;

   for(int i = 0; i < mFiles.size(); i++)
   {
      try
      {
         File mergeFile = new File(mFiles.get(i));
         if(mergeFile.length() > 0L)
         {
            doc = nu.parseXML(mergeFile);
            inxmltv = XMLTransform.toXMLString(doc);
            chns = inxmltv.indexOf("<channel ");
            progs = inxmltv.indexOf("<programme ");
            tve = inxmltv.indexOf("</tv");
            
            // zero length check is not sufficient to avoid 
            // StringIndexOutOfBoundsException: String index out of range: -1
            if((chns >=0) && (progs >= 0))
            {
               sbchans.append(inxmltv.substring(chns, progs));
            }
            else
            {
               log.info("merge: channel block missing from " 
                 + mergeFile.getAbsolutePath() + ": chns=" + chns + " progs=" + progs);
            }
            if((progs>=0) && (tve >= 0))
            {
               sbprogs.append(inxmltv.substring(progs, tve));
            }
            else
            {
            	log.info("merge: programme blocks missing from " 
                     + mergeFile.getAbsolutePath() + ": progs=" + progs + " tve=" + tve);
               
            }
         }
         else
         {
         	log.info("merge: File is zero length (or missing): " + mergeFile.getAbsolutePath());
         }
      }
      catch(Exception ex)
      {
         log.warn("merge: Failed to process " + mFiles.get(i), ex);
      }
   }

   try
   {
      File fout = new File(mOutFile);
      FileOutputStream fos = new FileOutputStream(fout);
      fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
      fos.write("<tv>\n".getBytes("UTF-8"));
      fos.write(sbchans.toString().getBytes("UTF-8"));
      fos.write(sbprogs.toString().getBytes("UTF-8"));
      fos.write("\n</tv>\n".getBytes("UTF-8"));
      fos.close();
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
      log.warn("merge: Failed to write to " + mOutFile, ex);
   }


}

public static void main(String[] args)
{
   CmdArgMgr cmd = new CmdArgMgr();
   String outfile = null;
   String [] keys = null;
   new XMLTransform();

      cmd.parseArgs(args);
      keys = cmd.getArgNames();
      outfile = cmd.getArg(ARG_OUTFILE);

   MergeFiles mf = new MergeFiles(outfile);

   for(int i = 0; i < keys.length; i++)
   {
      if(!ARG_OUTFILE.equals(keys[i]))
      {
         mf.add(cmd.getArg(keys[i]));
      }
   }
   mf.merge();
}
}
