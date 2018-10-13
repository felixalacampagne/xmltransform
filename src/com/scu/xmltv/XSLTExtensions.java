package com.scu.xmltv;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import com.scu.utils.FileTools;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class XSLTExtensions
{

   /**
    * Test whether a value matches one of a list
    * of regular expressions. 
    * 
    * The idea is for regexps to contain the list of TV programme favorites. Matching values
    * can then be decorated in a suitable manner. This would avoid requiring the decoration
    * to be done in code which is currently the case. It would be nice to return a list of 
    * matching program nodes but only way I think of doing that is to do a second pass....
    * 
    * Not sure what the format of regexps will need to be - probably something like
    * <CRIT>regexp1</CRIT>
    * <CRIT>regexp2</CRIT>
    * 
    * The favorites will probably be need to supplied to the stylesheet as a
    * parameter to keep the entry of favorites simple. Still need
    * @param value
    * @param favs
    * @return
    */
   public static boolean isMatch(String value, NodeList regexps)
   {
   Matcher match = null;
   Node node = null;
      if(regexps == null)
         return false;
      try
      {
         //System.out.println("isMatch: Checking " + regexps.getLength() + " reg.exps.");
         
         
         //while((node = regexps.nextNode()) != null)
         for(int ir = 0; ir < regexps.getLength(); ir++)
         {
            node = (Node) regexps.item(ir);
            String crit = null;
            try
            {
               crit = node.getTextContent();
            }
            catch(ArrayIndexOutOfBoundsException aiex)
            {
               // With default Java XMl implementation and Java Web Services 2.0 pack getTextContent 
               // gives an ArrayIndexOutOfBoundsException.
               // It only seems to be a problem when a NodeList is supplied as a parameter,
               // the isMatchOneNode works as expected.
               //
               // No idea know why...
               // Seems it might be a bug in the Xalan implementation used by JE 1.5, which is
               // probably why I started using a local copy of xalan and serializer. 
            }
            
            
            if(crit == null)
            {
               // More XSL weirdness. If the CRITs are created in code and supplied to a
               // parameter in the XSL then the value is in the TextContent of the CRIT node.
               // If the xml is read from an xml file using the XSL document() function
               // then the value is in the child node of the CRIT node!!
               Node cn = null;
               try
               {
                  cn = node.getFirstChild();
               }
               catch(ArrayIndexOutOfBoundsException aiex)
               {
                  System.err.println("" + aiex.getClass().getName() + ": An incompatible XML parser is being used - Xalan 2.7 or later is required. "
                        +"\n Please ensure there are no other xalan.jar files in the classpath,"
                        +"\n especially in the endorsed directory of the Java installation.");
                  return false;
               }
               if(cn != null)
               {
                  crit = cn.getTextContent();
               }
            }

            if(crit != null)
            {
               match = Pattern.compile(crit).matcher(value);
               if(match.find())
               {
                  return true;
               }
            }
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      return false;
   }

   // Attempt to work around the ArrayIndexOutOfBoundsException seen with the default Java 1.5
   // XSLT processor...
   public static boolean isMatchAlt(String value, Node crits)
   {
      
      if(crits == null)
         return false;
      try
      {
         NodeList critnl = null;
         //critnl = crits.getChildNodes();
         return isMatch(value, critnl);
      }
      catch(ArrayIndexOutOfBoundsException aiex)
      {
         System.err.println("" + aiex.getClass().getName() + ": An incompatible XML parser is being used - the Xalan parser is required. "
               +"\n Please ensure there are no other xalan.jar files in the classpath,"
               +"\n especially in the endorsed directory of the Java installation.");         
      }
      
      return false;
   }
   
   
   public static String currentDate(String format)
   {
   Date d = new Date();
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
   
      try
      {
         sdf.applyPattern(format);
         rs = sdf.format(d);
      }
      catch(Exception ex) {}
      
      return rs;
  
   }
   
   public static String formatDate(String xmltvdatetime, String format)
   {
      String rs = "";
      SimpleDateFormat sdf = new SimpleDateFormat();
      Date dt = null;
         try
         {
            // "20060726101500 +0200"
            // The +0200 indicates that the time is 2 hours ahead of GMT, not that is should be adjusted to 2 hours ahead!!
            sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
            dt = sdf.parse(xmltvdatetime);
            sdf.applyPattern(format);
            rs = sdf.format(dt);
         }
         catch(Exception ex) {}
         
         return rs;

   }
   
   public static String getTime(String xmltvdatetime)
   {
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
   Date dt = null;
      try
      {
         // "20060726101500 +0200"
         // The +0200 indicates that the time is 2 hours ahead of GMT, not that is should be adjusted to 2 hours ahead!!
         sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
         dt = sdf.parse(xmltvdatetime);
         sdf.applyPattern("HH:mm");
         rs = sdf.format(dt);
      }
      catch(Exception ex) {}
      
      return rs;
   
   }

   public static String getLongDate(String xmltvdatetime)
   {
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
   Date dt = null;
   String fullpattern = getXMLTVDateFormat(xmltvdatetime);
      try
      {
         // "20060726101500 +0200"
         // The +0200 indicates that the time is 2 hours ahead of GMT, not that is should be adjusted to 2 hours ahead!!
         // SimpleDateFormat is too stupid to ignore missing elements when the input date is shorter than
         // the pattern!!!
         sdf.applyPattern(fullpattern);
         dt = sdf.parse(xmltvdatetime);
         sdf.applyPattern("EEE, d MMM yyyy");
         rs = sdf.format(dt);
      }
      catch(Exception ex) 
      {
         ex.printStackTrace();
      }
      
      return rs;
   
   }   

   public static String getShortDate(String xmltvdatetime)
   {
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
   Date dt = null;
      try
      {
         // "20060726101500 +0200"
         // The +0200 indicates that the time is 2 hours ahead of GMT, not that is should be adjusted to 2 hours ahead!!
         sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
         dt = sdf.parse(xmltvdatetime);
         sdf.applyPattern("dd/MM/yyyy");
         rs = sdf.format(dt);
      }
      catch(Exception ex) {}
      
      return rs;
   
   }   
   
   /**
    * SimpleDateFormat is too stupid to realise that not all of a date has been passed in,
    * ie 20060728 instead of 20060728000000 +0000.
    * So this method adjusts the pattern to fit the number of fields which have been supplied.
    * The string must contain an integral number of fields or an exception will still result 
    * @param xmltvdatetime
    * @return
    */
   private static String getXMLTVDateFormat(String xmltvdatetime)
   {
   String pattern="";
   
      switch(xmltvdatetime.length())
      {
      case 4:
         pattern = "yyyy";
         break;
      case 6:
         pattern = "yyyyMM";
         break;
      case 8:
         pattern = "yyyyMMdd";
         break;
      case 10:
         pattern = "yyyyMMddHH";
         break;
      case 12:
         pattern = "yyyyMMddHHmm";
         break;
      case 14:
         pattern = "yyyyMMddHHmmss";
         break;
      case 18:  // Be version uses "200704071755 +0200"
         pattern = "yyyyMMddHHmm Z";
         break;
      default:
         pattern = "yyyyMMddHHmmss Z";
         break;
      }
      return pattern;
   }
      
   public static boolean isDateInRange(String mindate, String maxdate, String xmltvdatetime)
   {
   boolean rb = false;
   String tmp = null;
      // Not sure how this is going to be used. For now assume that min/maxdate are
      // probably of form yyyyMMddHH. So all need to do is a string comparison of the
      // first n characters of the target date.
      tmp = xmltvdatetime.substring(0,mindate.length());
      if(mindate.compareTo(tmp)<=0)
      {
         tmp = xmltvdatetime.substring(0,maxdate.length());
         if(maxdate.compareTo(tmp)>=0)
         {
            rb = true;
         }
      }
      return rb;
   }
   public static String nodeToXmltext(Node node)
   {
   StreamResult sr = null;
   StringWriter sw = null;
   String xml = "";
   
   
      try
      {
         sw = new StringWriter();
         sr = new StreamResult(sw);
         TransformerFactory.newInstance().newTransformer().transform(new DOMSource(node), sr);
         xml = sw.toString();
      }
      catch(Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return xml;
   }
   
   public static String writeToFile(Node value, String file)
   {
   String xml =  null;

      xml = nodeToXmltext(value);
      FileTools.writeStringToFile(file, xml);
      return "";
   }
   
   /**
    * 
    * @param xmltvdate One of the supported XMLTV based dates (see getXMLTVDateFormat)
    * @param field     One of 'DAY', 'MONTH', 'YEAR', 'HOUR', 'MINUTE'
    * @param value     A numeric value to be added to the date (can be negative!)
    * @return
    */
   public static String addToDate(String xmltvdate, String field, int ivalue)
   {
   Date dt = null;
   String stmp = null;
   SimpleDateFormat sdf = new SimpleDateFormat();
   GregorianCalendar cal = new GregorianCalendar();
   int fieldcode = -1;
   //int ivalue = 0;
      try
      {
         stmp = getXMLTVDateFormat(xmltvdate);
         sdf.applyPattern(stmp);
         dt = sdf.parse(xmltvdate);
         cal.setTime(dt);
         
         // No easy way to map field from XSL into Calendar field code (could
         // have used a Map but this is just as good!
         if("DAY".compareToIgnoreCase(field) == 0)
            fieldcode = Calendar.DAY_OF_MONTH;
         else if("MONTH".compareToIgnoreCase(field) == 0)
            fieldcode = Calendar.MONTH;
         else if("YEAR".compareToIgnoreCase(field) == 0)
            fieldcode = Calendar.YEAR;
         else if("HOUR".compareToIgnoreCase(field) == 0)
            fieldcode = Calendar.HOUR;
         else if("MINUTE".compareToIgnoreCase(field) == 0)
            fieldcode = Calendar.MINUTE;
         
         //ivalue = Integer.parseInt(value);
         
         cal.add(fieldcode, ivalue);
         sdf.applyPattern("yyyyMMddHHmm");
         stmp = sdf.format(cal.getTime());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
   
      return stmp;
   }
   
   /**
    * test function to return the min/max dates, formatted as XMLTV dates for the seven
    * days starting from today. This is an attempt to use the XSLT document() function
    * to perform the date loop in the XSLT rather than in the HTMLMaker Java.
    * 
    * If this works then it could also be used to feed the favorite criteria into
    * the xslt - they would need to be stored in XML format.
    * 
    * Once the whole week is processed with a single pass through the XSLT is might
    * be possible to generate the favorites page as part of that pass, if the faves can
    * be added to a seperate output document as they are found.
    * 
    * Don't really understand the description for the document() function so for now
    * this returns a string of XML of the form:
    * <RANGES>
    *    <RANGE> 0-n
    *       <START>YYYYMMDDHHMM</START>
    *       <END>YYYYMMDDHHMM</END>
    *    <RANGE>
    * </RANGES>
    * 
    * Returning a string containing an xml fragment didn't work... now trying to return a Node created
    * in same way is the favorite criteria node is. 
    * So the Node thing worked - without the document() function being required. So, still don't know how the
    * document function is supposed to work!!
    * @return
    */
   public static Node getDateRanges()
   {
   Date dt = new Date();
   SimpleDateFormat sdf = new SimpleDateFormat();
   GregorianCalendar cal = new GregorianCalendar();
   String mindate = null;
   String maxdate = null;
   StringBuffer result = new StringBuffer();
   int di = 0;


         try
         {
            result.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
            result.append("<RANGES>");
            for(di=0; di<7; di++)
            {
               result.append("<RANGE>");
               cal.setTime(dt);
               // Can't use the Date functions to just set these the once as the useful
               // Date functions have been deprecated... of course
               cal.set(Calendar.HOUR_OF_DAY, 6);
               cal.set(Calendar.MINUTE, 0);
               cal.set(Calendar.SECOND, 0);
               cal.add(Calendar.DAY_OF_MONTH, di);
               
               sdf.applyPattern("yyyyMMddHH");
               mindate = sdf.format(cal.getTime());

               cal.add(Calendar.DAY_OF_MONTH, 1);
               maxdate = sdf.format(cal.getTime());
               result.append("<START>");
               result.append(mindate + "00");
               result.append("</START>");
                  
               result.append("<END>");
               result.append(maxdate + "00");
               result.append("</END>");
               result.append("</RANGE>");
            }
            result.append("</RANGES>");
         }
         catch(Exception ex)
         {
            ex.printStackTrace();
         }

         return xmltextToNode(result.toString(), "//RANGES");
   }
   
   /**
    * Converts xml formatted text into a node which can be
    * assigned to a variable in a stylesheet and queried with XPath,
    * apply-templates to etc.
    * 
    * Not really useful for calling directly as it appears that XSL will
    * convert such an XML formatted string into something which only 
    * contains the values, no tags etc. Use convertRTFtoNode from
    * within a stylesheet.
    * @param xml
    * @param xpath
    * @return
    */
   public static Node xmltextToNode(String xml, String xpath)
   {
      Node nlfav = null;
      try
      {
         InputSource is = null;
         XPath xp = XPathFactory.newInstance().newXPath();
         
         is = new InputSource(  new StringReader(xml));
//               new ByteArrayInputStream(xml.getBytes()));
         nlfav = (Node) xp.evaluate(xpath, is, XPathConstants.NODE);      
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      
      return nlfav;
   }
   
   /**
    *  Converts XML added into an XSL variable into a node which can then be processes by the XSL.
    *  Well, that's the idea. 
    *  
    *  rtf - the value of the variable containing the XML
    *  xpath - the nodes to select from the XML.
    */
   public static Node convertRTFtoNode(Node rtf, String xpath)
   {
   Node nlfav = null;
   StreamResult sr = null;
   StringWriter sw = null;
   
      try
      {
         sw = new StringWriter();
         sr = new StreamResult(sw);
         TransformerFactory.newInstance().newTransformer().transform(new DOMSource(rtf), sr);

         nlfav = xmltextToNode(sw.toString(), xpath);
      
      }
      catch(Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return nlfav;
   }
   
   public static String replaceStr(String body, String target, String replacement)
   {
      String newbody = body;
      
      try
      {
         newbody = body.replace(target, replacement);
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      
      return newbody;
   }
   
   public static String urlencode(String url)
   {
   String encurl = url;
      try
      {
         // JDK docs say this will convert a space into a "+". Never seen this before
         // so might still have to manually convert spaces into %20s first!!
         // The docs use the HTML spec as the justification for converting the space
         // to a plus, but I can't see where this is stated. In fact I did find something
         // stating that a space should be encoded as %20, but maybe it is different for
         // values used in the 'query' part of the URI rather than the 'path' part. The specs
         // do not go into much detail about how to handle the 'query' part.
         // So I'll convert the +s into %20s for now
         encurl = URLEncoder.encode(url,"UTF-8");
         
         // Seems the pluses are OK with the DreamBox
         //encurl = encurl.replace("+", "%20");
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      return encurl;
   }

}
