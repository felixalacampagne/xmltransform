package com.scu.xmltv;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import org.w3c.dom.Document;

import com.scu.utils.CmdArgMgr;

import com.scu.utils.XMLTransform;

// Stupid class to merge multiple XMLTV files into one file for
// processing by the HTML generator.
// This is a temporary solution (likely to become a permanent one) while
// I think of an elegant way to handle multiple XMLTV sources and the
// one single favourites file....
public class MergeFiles
{
public final static String ARG_OUTFILE = "-merge";
private List<String> mFiles = new ArrayList<String>();
private String mOutFile = null;

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
/*
   // Might be better to parse the first doc, get the TV node and then add
   // the programme and channel entries from the subsequent docs
   //
   // Then again, maybe not! No way to add a list of nodes except one at a time.
   // Might be possible to simply append a new child TV nodes, would require
   // the XSL to use XPaths to select from all possible TV nodes, which is probably the
   // default anyway.
    * Would have to make a new doc with a new root node. Then insert the tv nodes from the
    * docs to be merged into the new doc and save it.
   // 
   Document doc1 = XMLTransform.parseXML(new File(mFiles.get(0)));
   NodeList nl = null;
   nl = doc1.getElementsByTagName("channel");
   Node nlc = nl.item(nl.getLength()-1);
   nl = doc1.getElementsByTagName("programme");
   Node nlp = nl.item(nl.getLength()-1);
   
   for(int i = 1; i < mFiles.size(); i++)
   {
      doc = XMLTransform.parseXML(new File(mFiles.get(i)));
      nl = doc.getElementsByTagName("channel");
      for(int j = nl.getLength(); j < 0; j--)
      {
         Node nimp = doc1.importNode(nl.item(j), true);
         doc1.insertBefore(nimp, nlc);
      }
      
      nl = doc.getElementsByTagName("programme");
      for(int j = nl.getLength(); j < 0; j--)
      {
         Node nimp = doc1.importNode(nl.item(j), true);
         doc1.insertBefore(nimp, nlp);
      }      
   }
   try
   {
      XMLTransform.domToXML(doc1, new StreamResult(new FileOutputStream( new File(mOutFile))));
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
 */ 
   
   
   for(int i = 0; i < mFiles.size(); i++)
   {
      try
      {
         doc = XMLTransform.parseXML(new File(mFiles.get(i)));
         inxmltv = XMLTransform.toXMLString(doc);
         chns = inxmltv.indexOf("<channel ");
         progs = inxmltv.indexOf("<programme ");
         tve = inxmltv.indexOf("</tv");
         sbchans.append(inxmltv.substring(chns, progs));
         sbprogs.append(inxmltv.substring(progs, tve));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         System.err.println("Failed to process " + mFiles.get(i));
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
   }
   
   
}

public static void main(String[] args)
{
   CmdArgMgr cmd = new CmdArgMgr();
   String outfile = null;
   String inifile = null;
   String [] keys = null;
   String faveonly = null;
   XMLTransform xmlt = new XMLTransform();

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
