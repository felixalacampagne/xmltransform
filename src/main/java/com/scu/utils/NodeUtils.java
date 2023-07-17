package com.scu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class NodeUtils
{
public final static String XML_DEFAULT_ENCODING = "UTF-8";
Logger log = LoggerFactory.getLogger(this.getClass());
private final static NodeUtils singleton = new NodeUtils();

public static NodeUtils getNodeUtils()
{
   return singleton;
}

private NodeUtils()
{
}

/**
 * Parses the XML string into an XML node. This is far more complex than
 * it should be. For reasons beyond me there is no straight forward method
 * for parsing a block of xml text into a node so a completely new
 * document has to be created and the root node of the new doc imported into the
 * existing doc.
 */
public Node parseXMLString(String xml, Document importDoc)
{
   try
   {
      Document doc;
      Element root = null;
      Node newnode = null;
      doc = parseXML(xml);
      // normalize text representation, ie. squash the spaces!
      doc.getDocumentElement().normalize ();
      root = doc.getDocumentElement();

      newnode = importDoc.importNode(root, true);
      return newnode;
   }
   catch (Throwable t)
   {
        t.printStackTrace ();
   }

   return null;
}

public Document parseXML(File xmlfile)
{
Document doc = null;

   try
   {
      // Should not assume that the file is UTF-8 encoded
      //in = new BufferedReader(new InputStreamReader(, "UTF-8"));
      doc = parseXML(new InputSource(new FileInputStream(xmlfile)));
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   return doc;
}

public Document parseXML(String xmlstring)
{
   return parseXML(new InputSource(new StringReader(xmlstring)));
}

public Document parseXML(InputSource xmlstream)
{
   Document doc= null;
   try
   {
   DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();


   docBuilderFactory.setNamespaceAware(true);
   DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

   // This causes the files specified in <DOCTYPE statements to
   // be ignored - which is a good thing. Don't know what the
   // downside is though!!
   docBuilder.setEntityResolver(new IgnoreEntityResolver());

   doc = docBuilder.parse(xmlstream);
   doc.normalize();
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   return doc;
}

public String getNodeValue(Node annode, String anodename)
{
NodeList nl = null;
String anodevalue = null;

   if((annode == null) || ((nl = annode.getChildNodes()) == null))
         return anodevalue;
   try
   {
      for (int k = 0; k < nl.getLength(); k++)
      {
         if (nl.item(k).getNodeName().compareTo(anodename) == 0)
         {
            anodevalue = nl.item(k).getChildNodes().item(0).getNodeValue();
         }
      }
   }
   catch (Exception ex)
   {
      log.info("getNodeValue: Failed to extract value of {} from {}", anodename, annode.getLocalName(), ex);
   }
   return anodevalue;
}

// Get the value of a simple node as returned by getNodeByPath eg. the text between the start tag and the end tag
public String getNodeValue(Node annode)
{
NodeList nl = null;
String anodevalue = null;

   if((annode == null) || ((nl = annode.getChildNodes()) == null))
         return anodevalue;
   try
   {
      anodevalue = nl.item(0).getNodeValue();
   }
   catch (Exception ex)
   {
      log.warn( "getNodeValue: Failed to extract value from {}", annode.getLocalName(), ex);
   }
   return anodevalue;
}

// Returns null if the xpath is not valid.
public NodeList getNodesByPath(Node doc, String xpath)
{
NodeList nl = null;

   try
   {
      nl = XPathAPI.selectNodeList(doc, xpath);
   }
   catch(Exception ex)
   {
      log.warn("getNodesByPath: Error getting {}", xpath, ex);
   }
   return nl;
}

// Returns the node as a string without the processing instruction
public String nodeToString( Node n ) throws Exception
{
String strRet = null;

   // Prepare a large enough String Buffer to hold the XML file
   StringWriter strwriter = new StringWriter(110000);
   outputNode(n, strwriter);

   strRet = strwriter.toString();

   if (strRet.indexOf("<?") != -1)
   {
      int iPos = strRet.indexOf("?>");
      if (iPos > -1)
      {
         strRet = strRet.substring(iPos + 2);
      }
  }

  return strRet;
}

public void outputNode( Node n, File f) throws Exception
{
   // The output mechanism appears to be responsible for the actual character encoding
   // so FileWriter is no good as it assumes the default encoding (which is not UTF8)
   FileOutputStream fos = new FileOutputStream(f);
   Writer w = new OutputStreamWriter(fos, XML_DEFAULT_ENCODING);
   outputNode(n, w);
   fos.close();
}

public void outputNode( Node n, Writer w) throws Exception
{
   // Prepare the DOM document for writing
   Source source = new DOMSource(n);

   // Prepare the output stream
   Result result = new StreamResult(w);

   // Write the DOM document to the file
   // Get Transformer
   TransformerFactory xfact = TransformerFactory.newInstance();
   //xfact.setAttribute("indent", "yes");

   Transformer xformer = xfact.newTransformer();

   // This doesn't appear to work correctly - nothing is actually indented
   xformer.setOutputProperty(OutputKeys.INDENT, "yes");

   // This either doesn't work or is only intended to define what goes into the processing
   // instruction. The output is not actually UTF8 encoded even though the PI says it should be.
   // I guess it is upto the Writer to perform the real encoding.
   xformer.setOutputProperty(OutputKeys.ENCODING, XML_DEFAULT_ENCODING);
   xformer.setOutputProperty(OutputKeys.METHOD, "xml");

   // Write to a file
   xformer.transform(source, result);
   w.close();
   return;
}

public Node getNodeByPath(Node doc, String xpath) throws TransformerException
{
NodeList nl = null;
Node n = null;
   nl = XPathAPI.selectNodeList(doc, xpath);
   if((nl != null) && (nl.getLength() > 0))
   {
      n = nl.item(0);
   }
   return n;
}

public Optional<Node> findNodeByPath(Node doc, String xpath)
{
	Node n = null;
	
	try
	{
		n = getNodeByPath(doc,xpath);
	}
	catch (TransformerException e)
	{
		log.debug("No node for xpath: {}" , xpath, e);
	}
	return Optional.ofNullable(n);
}

public String getAttributeValue(Node n, String attrname)
{
NamedNodeMap attrs = n.getAttributes();
Node attr = attrs.getNamedItem(attrname);
String value = null;
   if(attr != null)
   {
      value = attr.getNodeValue();
   }
   return value;
}

public void setAttributeValue(Node n, String attrname, String value)
{
NamedNodeMap attrs = n.getAttributes();
Node attr = attrs.getNamedItem(attrname);

   if(attr != null)
   {
   	attr.setNodeValue(value);
   }
   return;
}

// Methods below are not really Node utilities but are specific to
// XSLTExtensions and the processing of XMLTV files.

// Highly specialised function for use with XMLTV episode numbers to safely
// convert a string to a +ve integer and to return -9999 if the string
// is null or cannot be parsed to a number. -9999 is chosen so the caller can
// determine that the value is missing even after incrementing to get the
// real value
public int stringToInt(String str)
{
   int result = -9999;
   try
   {
      if(str != null)
      {
         result = Integer.parseInt(str.trim());
      }
   }
   catch(Exception ex)
   {
      // its a 'safe' method, ignore all exceptions
   }
   return result;
}

public String bytesToHex(byte[] bs)
{
   if((bs == null) || (bs.length < 1))
   {
      return null;
   }
   StringBuffer sb = new StringBuffer();
   for (byte b : bs)
   {
      sb.append(String.format("%02x", b & 0xff));
   }
   return sb.toString();
}

public String calcDigest(String value)
{
   String digest="";
   MessageDigest md;
   try
   {
      md = MessageDigest.getInstance("MD5");
      md.reset();
      byte[] mdbytes = md.digest(value.getBytes("UTF-8"));
      digest = bytesToHex(mdbytes);
   }
   catch (Exception e)
   {
      e.printStackTrace();
   }
   return digest;
}

public String sanitizeTitle(String title)
{
String clean = "";

   if(title != null)
   {
      clean = title.replace("(New Series)", "").replace("&", " And ").replaceAll("[\"'?/\\*&:;!$%<>,.|@#]", "");;
   }
   return clean;
}

}
