package com.scu.docmatch;

import java.io.File;

import org.w3c.dom.Document;

import com.scu.utils.CmdArgMgr;
import com.scu.utils.XMLTransform;


public class DocMatcher
{
public final static String ARG_DOC1FILE = "-doc1";
public final static String ARG_DOC2FILE = "-doc2";
public final static String ARG_XSLTFILE = "-xsl";
public final static String ARG_OUTDIR = "-out";

private String mdoc1 = null;
private String mdoc2 = null;
private String mxslt = null;
private String mout = null;

public DocMatcher(String doc1, String doc2, String xslt)
{
   mdoc1 = doc1;
   mdoc2 = doc2;
   mxslt = xslt;
}

public void setOutputfile(String file)
{
   mout = file;
}

public String doMatch()
{
String result = null;
XMLTransform xmlt = new XMLTransform();

Document doc2 = null;

   try
   {
      // transform needs an input doc, so it can be DOC1
      doc2 = XMLTransform.parseXML(new File(mdoc2));
      xmlt.clearParameters();
      xmlt.addParameter("DOC_2", doc2);

      System.out.println("Applying matching transform."); 
      xmlt.transformXML(mdoc1, mxslt, mout);

   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   catch(Error er)
   {
      er.printStackTrace();
   } 
   


   return result;
}

public static void main(String[] args)
{
   CmdArgMgr cmd = new CmdArgMgr();
   String doc1file = null;
   String doc2file = null;
   String xsltfile = null;
   String outfile = null;

   String [] keys = null;

   cmd.parseArgs(args);
      keys = cmd.getArgNames();

      for(int i = 0; i<keys.length; i++)
      {
         String val = cmd.getArg(keys[i]);
         if(ARG_DOC1FILE.compareTo(keys[i]) == 0)
            doc1file = val;
         else if(ARG_DOC2FILE.compareTo(keys[i]) == 0)
            doc2file = val;            
         else if(ARG_XSLTFILE.compareTo(keys[i]) == 0)
            xsltfile = val;
         else if(ARG_OUTDIR.compareTo(keys[i]) == 0)
            outfile = val;
      }

      
      if((doc1file==null) || (doc2file==null) || (xsltfile==null) || (outfile==null))
      {
         System.out.println("Usage: DocMatcher " + ARG_DOC1FILE + "=<DOC1 XMLfile> "
                                                 + ARG_DOC2FILE + "=<DOC2 XML file> "
                                                 + ARG_XSLTFILE + "=<XSL file> "
                                                 + ARG_OUTDIR + "=<Output file> ");
         System.exit(1);
      }

      DocMatcher dm = new DocMatcher(doc1file, doc2file, xsltfile);
      dm.setOutputfile(outfile);
   
}
}