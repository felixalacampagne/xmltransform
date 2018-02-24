package com.scu.xmltv;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import javax.xml.XMLConstants;
import javax.xml.xpath.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.scu.utils.CmdArgMgr;
import com.scu.utils.FileTools;
import com.scu.utils.XMLTransform;

public class HTMLMaker
{
public final static String ARG_XMLFILE = "-xml";
public final static String ARG_XSLTFILE = "-xsl";
public final static String ARG_OUTDIR = "-out";
public final static String ARG_FAV = "-fav";

private String mFavTemplate = null;
private String mFavListFile = null;
/**
 * @param args
 */
public static void main(String[] args)
{
   CmdArgMgr cmd = new CmdArgMgr();
   String xmlfile = null;
   String xsltfile = null;
   String outfile = null;
   String inifile = null;
   String [] keys = null;
   String faveonly = null;
   XMLTransform xmlt = new XMLTransform();

      // Can't override the jar manifest start class by specifying
      // a class on the command line, so have to provide access from here!
      cmd.parseArgs(args);
      if(cmd.getArg(MergeFiles.ARG_OUTFILE) != null)
      {
         MergeFiles.main(args);
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
            outfile = val;
         else if(ARG_FAV.compareTo(keys[i]) == 0)
            inifile = val;
      }

      
      if((xmlfile==null) || (xsltfile==null) || (outfile==null) || (inifile == null))
      {
         System.out.println("Usage: HTMLMaker " + ARG_XMLFILE + "=<XMLTV file> " +
                                                      ARG_XSLTFILE + "=<XSL file> " +
                                                      ARG_OUTDIR + "=<Output directory> " +
                                                      "[" + ARG_FAV + "=<favorites file>] ");
         System.exit(1);
      }

      HTMLMaker html = new HTMLMaker();
      html.mFavListFile = inifile;
      
      html.doDaily3(xmlfile, xsltfile, outfile);


}

public void doDaily3(String xmlfile, String xsltfile, String outdir)
{
XMLTransform xmlt = new XMLTransform();
File f;

   try
   {
      f = new File(outdir, "favorites.htm");
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




/**
 * @deprecated Now performed in the XSL file
 * @param inifile
 * @return
 */
private String getFavCritXML(String inifile)
{
String []favcrit = loadFavoritesCriteria(inifile);
StringBuffer favxmlbuf = new StringBuffer();
String favxml = null;

   try
   {
      favxmlbuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
      favxmlbuf.append("<CRITS>\n");
      for(int ifc = 0; ifc < favcrit.length; ifc++)
      {
         favxmlbuf.append("<CRIT>");
         favxmlbuf.append(favcrit[ifc]); // Maybe need to XML encode this!!!!
         favxmlbuf.append("</CRIT>");
         favxmlbuf.append("\n"); // In case I need to read it!!
      }
      favxmlbuf.append("</CRITS>\n");
      favxml = favxmlbuf.toString();
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   return favxml;
}

/**
 * @deprecated Performed in XSL file
 * @param inifile
 * @return
 */
private String [] loadFavoritesCriteria(String inifile)
{
Properties ini = new Properties();
String [] favcrit = null;  

   try
   {
      ini.load(new FileInputStream(inifile));
      favcrit = ini.values().toArray(new String [0]);
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   return favcrit;
}

/**
 * @deprecated Performed in XSL file
 * @param srchdir
 */
private void findFavorites(String srchdir)
{
File [] dirlist = null;
HashMap<String,String> favorites = new HashMap<String,String>();
int id = 0;
   try
   {
      dirlist = new File(srchdir).listFiles();
      for(id = 0; id < dirlist.length; id++)
      {
         if(dirlist[id].isFile())
         {
            if(dirlist[id].getAbsolutePath().toLowerCase().endsWith(".html"))
            {
               favorites.putAll(findFaves(dirlist[id]));
            }
         }
      }
      
      // This should probably be up to the caller to do!!
      writeFavorites(favorites, new File(srchdir, "favorites.htm").getAbsolutePath());
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
}

/**
 * @deprecated Performed in XSL file
 * @param favorites
 * @param filename
 */
private void writeFavorites(HashMap<String, String> favorites, String filename)
{
String [] favs = favorites.keySet().toArray(new String [0]);
String key = null;
StringBuffer favbuf = new StringBuffer();
String op = null;

   favbuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
   favbuf.append("<FAVORITES>\n");
   Arrays.sort(favs);
   for(int ifav = 0; ifav < favs.length; ifav++)
   {
      key = favs[ifav];
      favbuf.append(favorites.get(key));
   }
   favbuf.append("</FAVORITES>\n");
   if(mFavTemplate != null)
   {
      try
      {
      // String tmpl = FileTools.readStringFromFile(mFavTemplate);
      // op = tmpl.replace("<!-- FAVORITES -->", "<!-- FAVORITES -->\n" + favbuf.toString());
         XMLTransform xmlt = new XMLTransform();
         InputStream isxsl = new FileInputStream(mFavTemplate);
         InputStream isxml = new ByteArrayInputStream(favbuf.toString().getBytes());
         OutputStream os = new FileOutputStream(filename);
         
         xmlt.transformXML(isxml, isxsl, os);
      }
      catch(Exception ex) 
      {
         ex.printStackTrace();
      }
   }
   else
   {
      op = favbuf.toString();
      FileTools.writeStringToFile(filename, op);
   }

}

/**
 * @deprecated Performed in XSL file
 * @param file
 * @return
 */
private HashMap<String,String> findFaves(File file)
{
HashMap<String,String> rs = new HashMap<String,String>();
String fav = "";
String favcrit = null;
String fileabspath = file.getAbsolutePath();
String inp = FileTools.readStringFromFile(fileabspath);
String xmldate=null;
int nxtoff = 0;
Matcher match = null;
Matcher match2 = null;
   
//    Favorites are now found during the conversion of the XMLTV file into the HTML files.
//    The favorites in the HTML files are decorated by the XSL. Ideally the XSL should generate
//    a list of the favorites which could then be transformed into the favorites file, however
//    I don't know how to do that at the moment - given the problems I has simply passing a
//    NodeList to the damn XSL I figure getting output to two different documents will be
//    impossible. The NodeList to node-set bug was reported to Sun back at 1.5.0_2 but they 
//    refused to fix it. In fact it is not possible to pass anything from Java to an XSL parameter
//    except a String, passing a Node or NodeList results in an 'Incorrect conversion to node-set' 
//    error. The error goes away when Xalan is used. Why are the Sun Java people such ass holes?
//    Why the hell don't they test to see if the object passed implements one of the supported
//    interfaces instead of looking at the actual class-name? Since it is not possible to 
//    instantiate a class with the name of the supported interfaces their implementation can never
//    work!

//    So how to get the list of favorites...
//    I'll have the XSL generate the <FAV> xml data island. Then all I need to do here
//    is search each HTML doc for the <FAV> island which should be straightforward, no problem
//    is the scan is repeated multiple times - the XSL can put all the relevant info in (except the
//    doc name which can be added here). Repeating the search wont show any new favorites anymore,
//    too bad, but will show changes in the favorites formatting....


   // A favorite will now be returned as an XML structure
   // <FAV>
   //    <PROG>
   //    <CHAN>
   //    <START>
   //    <DOC>
   //    <IDX>

   System.out.println("Searching " + fileabspath);
   nxtoff = 0;
   try
   {
      favcrit = "(?s)\\<FAV\\>.*?\\</FAV\\>"; 
      match = Pattern.compile(favcrit).matcher(inp);
      while(match.find(nxtoff))
      {
         // If it matched we know we have one group
         //messageID = xml.substring(match.start(2),match.end(2));
         int start = match.start(0);
         int end = match.end(0);
            
          
         fav = inp.substring(start, end);

         // Favs are keyed on start time
         match2 = Pattern.compile("\\<START\\>\\s*(.*)\\s*\\</START\\>").matcher(fav);
         if(match2.find())
         {
            xmldate = fav.substring(match2.start(1), match2.end(1));
         }
         
         //// Need to insert the DOC tag.
         //// This is supplied to first pass XSL as parameter so this isn't necessary!
         //match2 = Pattern.compile("</FAV\\>").matcher(fav);
         //if(match2.find())
         //{
         //   fav = fav.substring(0, match2.start(0)) +
         //         "<DOC>" + file.getName() + "</DOC>\n" +
         //         fav.substring(match2.end(0));
         //}
         rs.put(xmldate, fav);
         nxtoff = end;
      }
   }
   catch (Exception ex){ ex.printStackTrace();}

   return rs;   
   
}

/**
 * @deprecated Performed in XSL file
 * @param file
 * @param crits
 * @return
 */
private HashMap<String,String> srchNmarkFaves(File file, String [] crits)
{
HashMap<String,String> rs = new HashMap<String,String>();
String fav = "";
String fileabspath = file.getAbsolutePath();
String inp = FileTools.readStringFromFile(fileabspath);
String op = "";
String mrksig="<FONT COLOR='#00FF00' SIZE='+1'>";
String mrktrm="</FONT></U></A>";
String docdate="";
String xmldate = "";
String channel = "";
int ic = 0;
String smat=null;
String smatidx=null;
int mtchcnt = 0;
int nxtoff = 0;
Matcher match = null;
String titlecrit = "";

// Favorites are now found during the conversion of the XMLTV file into the HTML files.
// The favorites in the HTML files are decorated by the XSL. Ideally the XSL should generate
// a list of the favorites which could then be transformed into the favorites file, however
// I don't know how to do that at the moment - given the problems I has simply passing a
// NodeList to the damn XSL I figure getting output to two different documents will be
// impossible. The NodeList to node-set bug was reported to Sun back at 1.5.0_2 but they 
// refused to fix it. In fact it is not possible to pass anything from Java to an XSL parameter
// except a String, passing a Node or NodeList results in an 'Incorrect conversion to node-set' 
// error. The error goes away when Xalan is used. Why are the Sun Java people such ass holes?
// Why the hell don't they test to see if the object passed implements one of the supported
// interfaces instead of looking at the actual class-name? Since it is not possible to 
// instantiate a class with the name of the supported interfaces their implementation can never
// work!

// So how to get the list of favorites...
// I'll have the XSL generate the <FAV> xml data island. Then all I need to do here
// is search each HTML doc for the <FAV> island which should be straightforward, no problem
// is the scan is repeated multiple times - the XSL can put all the relevant info in (except the
// doc name which can be added here). Repeating the search wont show any new favorites anymore,
// too bad, but will show changes in the favorites formatting....


   // A favorite will now be returned as an XML structure
   // <FAV>
   //    <PROG>
   //    <CHAN>
   //    <START>
   //    <DOC>
   //    <IDX>

   System.out.println("Searching " + fileabspath);
   for(ic = 0; ic<crits.length; ic++)
   {
      nxtoff = 0;
      try
      {
         titlecrit = "\\<B\\>" + crits[ic] + "\\</B\\>"; 
         match = Pattern.compile(titlecrit).matcher(inp);
         while(match.find(nxtoff))
         {
            // If it matched we know we have one group
            //messageID = xml.substring(match.start(2),match.end(2));
            int start = match.start(0);
            int end = match.end(0);
            
          
            smat = inp.substring(start, end);
            smatidx = "SCU" + mtchcnt;
            
            // Need to prevent matches from being hilighted more than once, ie if doc is
            // processed more than once. Dont think this is the ideal way to do it but it
            // will do for the time being...
            if((start >= mrksig.length()) &&
                  (mrksig.compareTo(inp.substring(start-mrksig.length(),start)) == 0)  )
            {
                  // This one has been done before but carry on searching,
                  // the doc or cirteria may have been changed to result in
                  // different matches since the last search.
                  op += inp.substring(nxtoff, end);
            }
            else
            {
               op += inp.substring(nxtoff, start) + 
                  "<A NAME='" + smatidx + "'><U>" + mrksig + smat + mrktrm;
            }
            nxtoff = end;
            
            //fav = "  <A href='" + file.getName() + "#" + smatidx + "'>" +
            //      smat;
            //fav += "</A>";
            // HTML files now have snippets of XML just for the favorites search!!
            // Skip previously made mark
            if(mrktrm.compareTo(inp.substring(end, end + mrktrm.length())) == 0)
            {
               end += mrktrm.length();
            }
            xmldate = "";
            docdate = "";
            channel = "";
            // This only works if the match was in the program title column and matched the
            // complete title. I've tried to force this by adding the <B> delimiters but is
            // is still not guarenteed! And it forces the layout to enclose the titles in bold!
            // Have to allow for the XML tag being put on a new line by the transform
            Matcher match2 = Pattern.compile("[\\s\\<!--]*\\<XML\\>").matcher(inp);
            //if(inp.substring(end, end+5).compareTo("<XML>") == 0)
            if(match2.find(end))
            {
               match2 = Pattern.compile("\\<START\\>(.*)\\</START\\>").matcher(inp);
               if(match2.find(end))
               {
                  xmldate = inp.substring(match2.start(1), match2.end(1));
                  docdate = XSLTExtensions.getTime(xmldate) + " " + XSLTExtensions.getLongDate(xmldate);
               }               
               match2 = Pattern.compile("\\<CHANNEL\\>(.*)\\</CHANNEL\\>").matcher(inp);
               if(match2.find(end))
               {
                  channel = inp.substring(match2.start(1), match2.end(1));
               }
            }
            
            //if(docdate.length() == 0)
            //{
            //   docdate = file.getName();
            //}
            //else if(channel.length() == 0)
            //{
            //   channel = file.getName() + ", ";
            //}
            
            //fav += " (" + channel + docdate + ")";
            //fav += "<BR>" + "\n";
            
            fav = "<FAV>" +
                  "<PROG>" + smat + "</PROG>\n" +
                  "<CHAN>" + channel + "</CHAN>\n" +
                  "<START>" + xmldate + "</START>\n" +
                  "<DOC>" + file.getName() + "</DOC>\n" +
                  "<IDX>" + smatidx + "</IDX>\n" +
                  "</FAV>\n";
            
            
            // Need to ensure that the dates for each favorite are unique - using
            // the filename and the match index should be enough, even if an xmldate
            // was not found. Sorting wont work if the xmldate is not found... too bad!
            xmldate += file.getName();
            xmldate += "#" + smatidx;
            
            rs.put(xmldate, fav);
            
            mtchcnt++;
            
         }
         op += inp.substring(nxtoff);
      }
      catch (Exception ex){ ex.printStackTrace();}
      inp = op;
      op = "";
   }
   FileTools.writeStringToFile(fileabspath, inp);

   return rs;
}


}
