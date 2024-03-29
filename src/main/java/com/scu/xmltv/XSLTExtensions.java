package com.scu.xmltv;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scu.utils.FileTools;
import com.scu.utils.NodeUtils;

import static com.scu.xmltv.XMLTVutils.getXMLTVDateFormat;

public class XSLTExtensions
{
	// All methods are static so fields also need to be static
static ObjectMapper mapper = new ObjectMapper();
static Logger log = LoggerFactory.getLogger(XSLTExtensions.class);
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
      return isMatch(value, regexps, true);
   }

   public static boolean isMatch(String value, NodeList regexps, boolean igncase)
   {
   Node node = null;

      if(regexps == null)
         return false;

      try
      {
         for(int ir = 0; ir < regexps.getLength(); ir++)
         {
            node = regexps.item(ir);
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

            if(isMatch(value, crit, igncase))
            {
               return true;
            }
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      return false;
   }

   public static boolean isMatch(String value, String regexp)
   {
      return isMatch(value, regexp, true);
   }

   public static boolean isMatch(String value, String regexp, boolean igncase)
   {
      Matcher match = null;
      int flags = 0;
      if(igncase)
      {
         flags = Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
      }
      if(regexp != null)
      {
         match = Pattern.compile(regexp, flags).matcher(value);
         boolean found = match.find();
         log.trace("isMatch: value:{} expression:{} match:{}", value, regexp, found);
         if(found)
         {
            return true;
         }
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
      return formatDate(XMLTVutils.getDateFromXmltv(xmltvdatetime), format);
   }


   public static String formatDate(Date date, String format)
   {
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
      try
      {
         sdf.applyPattern(format);
         rs = sdf.format(date);
      }
      catch(Exception ex) {}

      return rs;
   }


//   // This is for use by Java code, it is not really as an XSLTExtension
//   public static Date getDateFromXmltv(String xmltvdatetime)
//   {
//      SimpleDateFormat sdf = new SimpleDateFormat();
//      Date dt = null;
//      try
//      {
//         sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
//         dt = sdf.parse(xmltvdatetime);
//      }
//      catch(Exception ex) {}
//
//      return dt;
//   }

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

   public static String getUnixDate(String xmltvdatetime)
   {
   String rs = "";
   SimpleDateFormat sdf = new SimpleDateFormat();
   Date dt = null;
      try
      {
         sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
         dt = sdf.parse(xmltvdatetime);
         // A Date is the number of seconds since the 'epoch' with no timezone offsets applied, ie. UTC/GMT.
         // This is the Unix way of representing a datetime. Will use this to try and avoid timezone
         // issues when setting programmes from a device using a different timezone to the set-top box, eg.
         // when using iPhone in UK and STB in Be.
         rs = "" + dt.getTime();
      }
      catch(Exception ex) {}

      return rs;
   }

   // This is to allow the timerlist page to issue a warning that the device
   // timezone is not the same as the one used for the EPG times. Only required
   // because I couldn't find a way in 'standard' jaavascript to force the unix Date
   // values to be displayed using a summertime aware timezone.
   // This is only an issue when using iPhone which adjusts the local timezone
   // according to the location and javascript converts Dates using the local
   // device timezone.
   //
   // WARNING: This doesn't really do what is required as it gives the timezone offset
   // in effect when the EPG pages are generated but the device will be calculating
   // the offset when the link is clicked and the page is viewed so the two calculations
   // could be made either side of a daylight saving transition! It should only give
   // false positives though so is good enough provided the value is not used
   // for any program time calculations.
   public static String getTimeZoneOffset()
   {
   	// NB. All the relevant methods of Date have been deprecated so should use Calendar!
   	Calendar nowlocal = Calendar.getInstance();
   	int off = nowlocal.getTimeZone().getOffset(nowlocal.getTimeInMillis()) / 1000 / 60;
   	return String.valueOf(off);
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


   // This is used for writing the HTML listing pages so it probably
   // shouldn't be messed around with too much to get it to do things specific to XML
   public static String nodeToXmltext(Node node)
   {
   StreamResult sr = null;
   StringWriter sw = null;
   String xml = "";

      try
      {
         sw = new StringWriter();
         sr = new StreamResult(sw);
         Transformer tfmr = TransformerFactory.newInstance().newTransformer();
         tfmr.transform(new DOMSource(node), sr);
         xml = sw.toString();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return xml;
   }

   // Writes the serialized Node to a file as Text, ie. no indentation etc.
   public static String writeToFile(Node value, String file)
   {
   String xml =  null;

      xml = nodeToXmltext(value);
      FileTools.writeStringToFile(file, xml);
      return "";
   }

   public static String nodeToXml(Node node)
   {
   StreamResult sr = null;
   StringWriter sw = null;
   String xml = "";


      try
      {
         sw = new StringWriter();
         sr = new StreamResult(sw);
         Transformer tfmr = TransformerFactory.newInstance().newTransformer();
         tfmr.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
         tfmr.setOutputProperty(OutputKeys.INDENT, "yes"); // This doesn't give indenting
         tfmr.setOutputProperty(OutputKeys.ENCODING, "utf8");
         
         // This gives unrecognised property exception, with or without xalan: prefix.
         // Google revealed the weird URL format to me which doesn't give an error.
         // The indentation only appears if the output type is set to XML.
         tfmr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
         tfmr.transform(new DOMSource(node), sr);
         xml = sw.toString();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return xml;
   }

   // Writes the serialized Node to a file as XML, ie. attempts to
   // pretty print the output, etc.
   public static String writeXMLToFile(Node value, String file)
   {
   String xml =  null;

      xml = nodeToXml(value);
      FileTools.writeStringToFile(file, xml, "utf-8");
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
         nlfav = (Node) xp.evaluate(xpath, is, XPathConstants.NODE);
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         System.out.println("xmltextToNode: xml=" + xml);
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


   /**
    * Gathers all the info needed to create the NFO files into one block.
    * Originally it was passed a PROGRAMME node and it did the extraction but this
    * turned out to be quite slow. Extracting the values in the stylesheet and passing
    * them to the method seems to work better.
    *
    * Getting the returned XML block to be included in the FAV block took a great deal of
    * experimentation. Eventually arrived at a solution which works by converting the XML string into
    * a Node. The children of the Node are then added to the FAV using the 'copy-of' function
    * instead of 'value-of'. This is not ideal as the conversion to a Node is quite a heavy
    * process but at least now it is not necessary to copy the value from the returned object
    * into a new node with the same name in the XSLT, ie. fields can be added here without the
    * need to change the stylesheet besides where the fields are to be used.
    *
    * Well bugger me - I thought I'd give it one last go to return a Node instead of strings and
    * the risk that disable-output-escaping stops working - and it works. The trick is perhaps the
    * use of a DocumentFragment. The problem is that a DocumentFragment can only created by a Document
    * and it's not obvious where that comes from. My solution is currently to pass in a Node from the
    * XSLT, the '.' inside a template appears to be good enough, and it's OwnerDocument is used
    * to create the DocumentFragment. The episode info inserted into the DocumentFragment has Element child nodes
    * with the value set via setTextContent and lo! they appear in the output of dumpNode just like the
    * other elements in the FAV!
    * @param show    - the show name
    * @param start   - the start time in XMLTV format
    * @param episodenum - the episode number in xmltv format or empty string
    * @param subtitle   - the episdoe title or empty string
    * @return
    */

   public static Node getEpisodeInfo(Node any, String show, String start, String episodenum, String subtitle)
   {
      Document doc = any.getOwnerDocument();
      DocumentFragment topNode = any.getOwnerDocument().createDocumentFragment();
      EpisodeShow ei = new EpisodeShow(show, start, episodenum, subtitle);

      Node node;

      node = doc.createElement("EPSHOW");
      node.setTextContent(ei.getCleanshow());
      topNode.appendChild(node);

      node = doc.createElement("EPSEASON");
      node.setTextContent(ei.getEpseason());
      topNode.appendChild(node);

      node = doc.createElement("EPNUM");
      node.setTextContent(ei.getEpnum());
      topNode.appendChild(node);

      node = doc.createElement("EPTITLE");
      node.setTextContent(ei.getEptitle());
      topNode.appendChild(node);

      node = doc.createElement("EPDATE");
      node.setTextContent("20" + ei.getShowDate()); // Y2.1K wont be my problem!
      topNode.appendChild(node);

      node = doc.createElement("EPFMTX");
      node.setTextContent(ei.getEpinfx());
      topNode.appendChild(node);

      node = doc.createElement("RECNAME");
      node.setTextContent(ei.getEventName());
      topNode.appendChild(node);

      node = doc.createElement("UID");
      node.setTextContent(ei.getUid());
      topNode.appendChild(node);

      return topNode;
   }

   public static String getEpisodeInfo(String show, String start, String episodenum, String subtitle)
   {
      StringBuffer xml = new StringBuffer();
      NodeUtils nu = NodeUtils.getNodeUtils();
      EpisodeShow ei = null;

      ei = new EpisodeShow(show, start, episodenum, subtitle);
      String recname = ei.getEventName();
      String sdate = start;
      String epshow = ei.getCleanshow();
      xml.append("<EPSHOW>").append(epshow).append("</EPSHOW>");
      xml.append("<EPSEASON>").append(ei.getEpseason()).append("</EPSEASON>");
      xml.append("<EPNUM>").append(ei.getEpnum()).append("</EPNUM>");
      xml.append("<EPTITLE>").append(ei.getEptitle()).append("</EPTITLE>");
      xml.append("<EPDATE>").append(formatDate(sdate, "yyyy-MM-dd")).append("</EPDATE>");
      xml.append("<EPFMTX>").append(ei.getEpinfx()).append("</EPFMTX>");
      xml.append("<RECNAME>").append(recname).append("</RECNAME>");
      xml.append("<UID>").append(nu.calcDigest(recname)).append("</UID>");

      // Returning an XML string and using
      // <xsl:value-of disable-output-escaping="yes"> seems to work although the output from dumpNode
      // looks weird which suggests it might be a more luck than by design! But I'll keep it for now!!
      // NB. Might need to xmlencode the field content which actually only applies to the description
      // since the other fields are sanitized to make them filename compatible... and the description
      // is not actually handled here!! Anyway, this is what the Perl functions does
      // $tr =~ s/&/&amp;/g;
      // $str =~ s/\>/&gt;/g;
      // $str =~ s/\</&lt;/g;
      // $str =~ s/\"/&quot;/g;
      // $str =~ s/\'/&apos;/g;
      return xml.toString();
   }

   public static String getEpisodeInfoAsJson(String show, String start, String episodenum, String subtitle)
   {
      String json = "";
      EpisodeShow ei = null;

      ei = new EpisodeShow(show, start, episodenum, subtitle);
      try
      {
         json = mapper.writeValueAsString(ei);
      }
      catch (JsonProcessingException e)
      {
         log.info("Failed to generate JSON for EpisodeShow: " + e.getMessage());
         e.printStackTrace();
      }
      return json;
   }

   // If the disable-output-escaping stops working or is unreliable then revert to use
   // a node via this method.
   public Node getEpisodeInfoAsNode(String show, String start, String episodenum, String subtitle)
   {
   StringBuffer xml = new StringBuffer();
      xml.append("<EPINFO>");
      xml.append(getEpisodeInfo(show, start, episodenum, subtitle));
      xml.append("</EPINFO>");
      return xmltextToNode(xml.toString(), "//EPINFO");
   }

   // Returns the episode title with the SxE prefix
   public static String getFullEpisodetitle(String episodenum, String subtitle)
   {
      return getFullEpisodetitle(episodenum, subtitle, " ");
   }

   public static String getFullEpisodetitle(String episodenum, String subtitle, String separator)
   {
      EpisodeTitle ei = new EpisodeTitle(episodenum, subtitle, separator);
      return ei.getEpfulltitle();
   }

   /**
    *
    * @param episodenum episode number in xmltv_ns format
    * @param subtitle the title of the episode
    * @param show the name of the show
    * @param start the start time in xmltv format
    * @return
    */
   public static String getEventName(String episodenum, String subtitle, String show, String start)
   {
      EpisodeShow eps = new EpisodeShow(show, start, episodenum, subtitle);
      return eps.getEventName();
   }

   public static String getEpisodeSxN(String episodenum)
   {
      EpisodeNumber ei = new EpisodeNumber(episodenum);
      return ei.getEpinfx();
   }

   public static String dumpNode(Node node)
   {
      String result = "";
      result = dumpNode(node, "");
      log.info("\n" + result.toString());
      return result;
   }

   public static String dumpNode(Node node, String indent)
   {
      StringBuffer result = new StringBuffer();

      result.append(indent).append(node.getNodeName());
      if(node.hasChildNodes())
      {
         result.append("  Children: "). append(node.getChildNodes().getLength()).append("\n");
      }
      else
      {
         result.append("  Value: ").append(node.getNodeValue()).append("\n");
      }
      for(int childno=0 ; childno <node.getChildNodes().getLength(); childno++)
      {
         Node child = node.getChildNodes().item(childno);
         result.append( dumpNode(child, indent + "   ") );
      }

      return result.toString();
   }



}
