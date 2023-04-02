package com.scu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XMLTransform
{
   public final static Logger LOG = Logger.getLogger(XMLTransform.class.getName());
public final static String ARG_XMLFILE = "-xml";
public final static String ARG_XSLTFILE = "-xsl";
public final static String ARG_OUTFILE = "-out";



   protected InputStream msrcfile = null;
   protected InputStream mxslfile = null;
   protected OutputStream moutfile = null;
   protected HashMap<String, Object> mParams = null;
   protected Exception mLastTxErr = null;

   /**
    * @param args
    */
   public static void main(String[] args)
   {
   CmdArgMgr cmd = new CmdArgMgr();
   String xmlfile = null;
   String xsltfile = null;
   String outfile = null;
   String [] keys = null;
   XMLTransform xmlt = new XMLTransform();

      cmd.parseArgs(args);
      keys = cmd.getArgNames();

      for(int i = 0; i<keys.length; i++)
      {
         String val = cmd.getArg(keys[i]);
         if(ARG_XMLFILE.compareTo(keys[i]) == 0)
            xmlfile = val;
         else if(ARG_XSLTFILE.compareTo(keys[i]) == 0)
            xsltfile = val;
         else if(ARG_OUTFILE.compareTo(keys[i]) == 0)
            outfile = val;
         else
         {
            // Treat the argument as a parameter for the xslt
            // cmdline args start with a - which we don't want
            xmlt.addParameter(keys[i].substring(1), val);
         }
      }

      if((xmlfile==null) || (xsltfile==null) || (outfile==null))
      {
         System.out.println("Usage: xmltransform " + ARG_XMLFILE + "=<XML file> " +
                                                      ARG_XSLTFILE + "=<XSL file> " +
                                                      ARG_OUTFILE + "=<Output file>" +
                                                      "[paramname=paramval]");
         System.exit(1);
      }

      try
      {

         LOG.log(Level.ALL, " Transforming {0} with {1} into {2}", new Object[] {xmlfile, xsltfile, outfile});
         xmlt.transformXML(xmlfile, xsltfile, outfile);
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      catch(Error er)
      {
            er.printStackTrace();
      }
      LOG.log(Level.ALL, "Done!");
   }


   public XMLTransform()
   {
      //System.err.println("XMLProcessor: default constructor");
      mParams = new HashMap<String,Object>();
   }

   public XMLTransform(String srcxml, String xslurl)
   {
      this(srcxml, xslurl, new File(new File(srcxml).getParentFile(), "output.xml").toString());
   }

   public XMLTransform(String srcxml, String xslurl, String output)
   {
      this();
      try
      {
         msrcfile = new FileInputStream(srcxml);
         mxslfile = new FileInputStream(xslurl);
         moutfile = new FileOutputStream(output);
      }
      catch(Exception ex) { ex.printStackTrace(); }
   }

   public void setXMLstream(InputStream srcxml)
   {
      msrcfile = srcxml;
   }

   public void setXSLstream(InputStream srcxsl)
   {
      mxslfile = srcxsl;
   }

   public void setOutputstream(OutputStream output)
   {
      moutfile = output;
   }

   public void addParameter(String name, Object value)
   {
      deleteParameter(name);
      mParams.put(name, value);
   }


   public void addParameter(String name, String value)
   {
      deleteParameter(name);
      mParams.put(name, value);
   }

   public void clearParameters()
   {
      mParams.clear();
   }

   public void deleteParameter(String name)
   {
      try
      {
         mParams.remove(name);
      }
      catch(Exception ex) {};
   }

   public void transformXML() throws Exception
   {
      transformXML(msrcfile, mxslfile, moutfile);
   }
   public void transformXML(String srcfile, String xslfile, String outfile)
      throws Exception
   {
   InputStream isx = null;
   InputStream ist = null;
   OutputStream of = null;

      try
      {
         isx = new FileInputStream(srcfile);
         ist = new FileInputStream(xslfile);
         if(outfile != null)
         {
            of = new FileOutputStream(outfile);
         }
         transformXML(isx, ist, of);
      }
      finally
      {
         Utils.safeClose(isx);
         Utils.safeClose(ist);
         Utils.safeClose(of);
      }
   }

   public void transformXML(InputStream srcfile, InputStream xslfile, OutputStream outfile)
      throws Exception
   {
   //System.err.println("XMLProcessor.transformXML: entry");
   Document doc;
   DOMSource xmlsrc = null;
   StreamSource xslsrc = null;
   StreamResult outrst = null;
   TransformerFactory tf = null;
   Transformer tx = null;
   String skey = null;

      // Construct the DOM from the XML text file
      try
      {
         //System.err.println("XMLProcessor.transformXML: get doc builder factory");

         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         docBuilderFactory.setNamespaceAware(true);
         //System.err.println("XMLProcessor.transformXML: get new doc builder");
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

         // With Xalan parser it might be possible to prevent the
         // DOCTYPE dtd not found errors by setting this feature on the docbldfact to false
         // http://apache.org/xml/features/nonvalidating/load-external-dtd
         // The value is true by default. Don't know if this works for the default Java parser.
         docBuilder.setEntityResolver(new IgnoreEntityResolver());

         //System.err.println("XMLProcessor.transformXML: parse the source XML");
         doc = docBuilder.parse(srcfile);
         doc.normalize();

         //// normalize text representation - whatever that means - squash the spaces apparently!
         //doc.getDocumentElement().normalize();

         // This is not the root. The document itself is the root, at least
         // as far as the XSLT match="/" is concerned.
         //System.err.println("XMLProcessor.transformXML: find the root");
         //root = (Element) doc.getDocumentElement();
      }
      catch (Exception err)
      {
         mLastTxErr = err;
         throw err;
      }

   //       xmlsrc = new DOMSource(root);
      xmlsrc = new DOMSource(doc);
      xslsrc = new StreamSource(xslfile);
      if(outfile == null)
      {
         outrst = new StreamResult(new FileOutputStream(File.createTempFile("scu","xmlt")));
      }
      else
      {
         outrst = new StreamResult(outfile);
      }

      // Try to force use of Xalan even when JWSDP2 is installed in the endorsed folder.
      System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");

      //System.err.println("XMLProcessor.transformXML: get Transformer factory");

      tf = TransformerFactory.newInstance();

      try
      {
         //System.err.println("XMLProcessor.transformXML: load XSL into the transformer");

         tx = tf.newTransformer(xslsrc);

         //System.err.println("XMLProcessor.transformXML: add parameters to the transformer");
         for(Iterator<String> it = mParams.keySet().iterator(); it.hasNext();)
         {
            skey = it.next();
            tx.setParameter(skey, mParams.get(skey));
         }

         //System.err.println("XMLProcessor.transformXML: do the transformation");
         tx.transform(xmlsrc, outrst);
      }
      catch (Exception err)
      {
         mLastTxErr = err;
         throw err;
      }

      //System.err.println("XMLProcessor.transformXML: exit");
   }

   public static String toXMLString(Document doc)
   {
   StringWriter sw = new StringWriter();
   StreamResult outrst = null;

      outrst = new StreamResult(sw);
      domToXML(doc, outrst);


      return sw.toString();
   }

   public static void domToXML(Document doc, StreamResult sr)
   {
   DOMSource xmlsrc = null;
   TransformerFactory tf = null;
   Transformer tx = null;

      xmlsrc = new DOMSource(doc);

      tf = TransformerFactory.newInstance();

      try
      {
         //System.err.println("XMLProcessor.transformXML: load XSL into the transformer");
         tx = tf.newTransformer();
         tx.transform(xmlsrc, sr);
      }
      catch (Exception err)
      {
         err.printStackTrace();
      }

      return;
   }



   public String getLastErrorDescription()
   {
   String desc = "";
      if(mLastTxErr == null)
      {
         return "";
      }
      else if(TransformerException.class.equals(mLastTxErr.getClass()) == true)
      {
         TransformerException tcex = (TransformerException) mLastTxErr;
         SourceLocator loc = tcex.getLocator();
         desc = "Transform failure: " +  tcex.getCause() + "\nFile: " + mxslfile ;
         if(loc != null)
         {
            desc += "\nLine " + loc.getLineNumber() + " col " + loc.getColumnNumber();
         }
         else
         {
            desc += "\nNo location information available.";
         }
      }
      else if(SAXParseException.class.equals(mLastTxErr.getClass()) == true)
      {
         SAXParseException spex = (SAXParseException) mLastTxErr;
         desc = "** Parsing error" + ", line " + spex.getLineNumber ()
                                   + ", uri " + spex.getSystemId ()
                                   + "\n   " + spex.getMessage ();
      }
      else
      {
         desc = "Exception: " + mLastTxErr.getMessage();
      }
      return desc;
   }

   public static String XMLEncode(String rawtxt)
   {
   StringBuffer sb = new StringBuffer(rawtxt);
   int i = 0;
      while( i >= 0)
      {
         i = sb.toString().indexOf("&", i);
         if(i >= 0)
         {
            // Must be a better way than this but the StringBuffer has the
            // replace and the String has the indexOf function!!!
            sb.replace(i, i+1, "&amp;");
            i++;
         }
      }
      return sb.toString();
   }

}

// Should be in a class of it's own really
class IgnoreEntityResolver implements EntityResolver
{

// Not really sure what is meant by an entity BUT the Jxmltv output for the
// Be channels contains a <!DOCTYPE entry with a file reference. The parser
// tries to load this, even with validating and namespaceaware set to false,
// which gives a FileNotFoundException (which is annoying!!)
// This method is called with the name of the file in the systemId parameter.
// Returning null doesn't stop the exception. Returning an InputSource
// pointing to an empty string DOES stop the exception and allows the parsing to
// be performed. No idea what the side effects are... yet!!
public InputSource resolveEntity (String publicId, String systemId)
{
    return new InputSource(new StringReader(""));
}
}