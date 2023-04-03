package com.scu.xmltv;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.scu.utils.CmdArgMgr;
import com.scu.utils.NodeUtils;


/**
 * Takes a 'reference' file and updates a missing field from an
 * 'alternative' source (I couldn't think of a better name!)
 *
 * The initial use case is as follows:
 * tvgrabnl does a good job of getting descriptions of programmes etc.
 * but recently it has not been getting the episode info. The episode info
 * seems to be reliably available from the VUUltimo EPG, but the
 * descriptions are pretty poor. The aim is to take use the tvgrabnl
 * file as the source and fill in missing episode infos from the
 * epg file.
 *
   Many moons later...
   The TVguide and EPG times are not the same. This seems to be especially the case for programmes early
   in the day. Sometimes the difference is minutes but occasionally it is more than an hour. The large
   discrepencies appear to be relate to the designation of 'No broadcast' periods. I think in some cases
   the 'NB' period is merged into the start time of the next program. The result is that many serial programmes
   are ending up with no episode info which then requires a lot of manual actions to recover. 
   The tvguide source was used as the reference but to avoid this issue it is now the epg which is used as
   reference to ensure the episode info is present - the problem with this is that the epg is not reliable, 
   sometimes it disappears, especially after a reboot.
   
   Currently the starttime is used as-is to match the program in the reference and alternative. In practice
   it should probably not be used at all, would be better to select based on the order of the programme, ie. first 
   occurrence of 'program' in the reference should match with the first occurrence in the alternative, maybe with a
   sanity check on the times. This should work in the case of the midday episode, eg. Grey's Anatomy, which is the repeat of
   the previous days evening episode and there is a different episode for the current evening.
   
   Above seems to work OK however the difference in time between the EPG and the tvguide is quite significant, more than
   30mins for programmes early in the day. Maybe the times should also be merged to maximise the chance of
   actually recording the whole programme. This would require taking the earliest startime and the latest endtime.
   Don't know what effect this will have when multiple fields are updated using multiple passes like at present. 
   Maybe a list of potential missing fields could be provided so a single search can be used.
   
 * @author Chris
 *
 */
public class XMLTVSourceCombiner
{
public final static String ARG_OUTFILE = "-combine";
private static final String ARG_REF = "-ref";
private static final String ARG_ALT = "-alt";
private static final String ARG_RESULT = "-res";

private final File refXMLTV;
private final File altXMLTV;
private final NodeUtils nu = NodeUtils.getNodeUtils();
private Document refDoc = null;
private Document altDoc = null;
Logger log = LoggerFactory.getLogger(this.getClass().getName());

public XMLTVSourceCombiner(String referenceXMLTV, String alternateXMLTV)
{
   refXMLTV = new File(referenceXMLTV);
   altXMLTV = new File(alternateXMLTV);
}

public void combineSource(String... fieldnames)
{
   if(refDoc == null)
   {
      refDoc = nu.parseXML(refXMLTV);
      altDoc = nu.parseXML(altXMLTV);
   }

   NodeList progs = nu.getNodesByPath(refDoc, "/tv/programme");
   for(int i = 0; i <  progs.getLength(); i++)
   {
      Node refProg = progs.item(i);
  
      String progid = nu.getNodeValue(refProg, "title") + ":" 
                    + nu.getAttributeValue(refProg, "start") + ":" 
      		        + nu.getAttributeValue(refProg, "channel"); 
      log.info("combineSource: processing {}", progid);
      
//      // The 'modern' way to do it - but no way to log a message if nothing found - 
//      // so much for continuous improvement...
//      this.findAltNode(refProg, progid).ifPresent(altn ->
//	      {
//	      	copyFields(refProg, altn, fieldnames, progid);
//	      	adjustTimes(refProg, altn, progid);
//	      });
      
      // Kludge to use modern way and do what I want to do. It is confusing using
      // map to map the object to itself and then having the end result of findAltNode being
      // null defeats the purpose of using Optional plus it is certainly not more readable
      // than the 'old' way of doing things (ifPresent.ifNotPresent would have been OK)
      // but Hey! That's Progress for you. Apparently Java 11 supports ifNotPresent, seems
      // the Java committee took a leaf out of the Apple iPhone playbook and only provided the
      // blindingly obvious 3 or 4 versions later (think copy/paste function).
      this.findAltNode(refProg, progid)
      .map( altn -> {
      	copyFields(refProg, altn, fieldnames, progid);
      	adjustTimes(refProg, altn, progid);
      	return Optional.of(altn); } )
      .orElseGet( () -> {
      	log.info("combineSource: NO alternative found for {}", progid);	
      	return null; } );
   }
}

private Optional<Node> findAltNode(Node refProg, String progid)
{
	Node altProg = null;
	String title = nu.getNodeValue(refProg, "title");
   String starttime = nu.getAttributeValue(refProg, "start");
   String chanid = nu.getAttributeValue(refProg, "channel");

   NodeList altprogs = nu.getNodesByPath(altDoc, "/tv/programme[@start='" + starttime + "' and @channel='" + chanid + "']");
   
   if(altprogs == null || (altprogs.getLength() == 0))
   {
      log.debug("findAltNode: alternative does not contain exact match for {}", progid);
      // The starttime in alt should be for the same day as the ref item.
      // A show can be broadcast multiple times during the day, eg. Grey's Anatomy
      // is shown at midday and in the evening, Minx is shown as multiple 
      // episodes one after the other. The Minx case prevents a large date discrepancy
      // from being accepted (Minx is ca.25mins!)
      //
      // Determine the occurrence of the program in the reference then find same occurrence in alternative.
      // The procedure will be something like:
      //   select using starttime,chanid. 
      //     single match - use it directly
      //     no match 
      //        select from 'ref' for chanid, 'programme',starttime[day] 
      //           select from alt with criteria chanid, 'programme',starttime[day] - should give same number
      //              different count - goto next ref node
      //           determine occurrence number of ref node (1 if there is only one occurrence!)
      //           locate occurrence number of alt node      
      int refoccur = -1;
      String startday = starttime.substring(0,8);
      String safeTitle = title; // there is no safe title - XPath just doesn't support searching for single quote! title.replace("'", "?");
      String occurCrit = "/tv/programme[" 
            + "starts-with(@start, '" + startday + "') and " 
	         + "@channel='" + chanid + "' and "
	         + "title=\"" + safeTitle + "\""
            + "]";
      NodeList refprogsoccurs = nu.getNodesByPath(refDoc, occurCrit);

      for(int occurs = 0; occurs <  refprogsoccurs.getLength(); occurs++)
      {
      	Node refOccur = refprogsoccurs.item(occurs);
         String occurStarttime = nu.getAttributeValue(refOccur, "start");         	
      	if(occurStarttime.equals(starttime))
      	{
      		log.debug("findAltNode: reference {} is occurrence {}", progid, occurs+1);
      		refoccur = occurs;
      		break;
      	}
      }
      
      // In theory at least one must match since refProg is from the search list however special characters
      // in the title could fork up the search. Single quote is allowed for and I can't think of a reason for
      // the title containing a double-quote but who know what else title makers can come up with to break
      // the very fragile XPath search.
      if(refoccur < 0)
      {
      	log.info("findAltNode: Failed to find reference occurrence for {} with predicate [{}]", progid, occurCrit);
      }
      else
      {
	      NodeList altprogsoccurs = nu.getNodesByPath(altDoc, occurCrit);
	      log.debug("findAltNode: alternative occurrences of {}: {}", progid, altprogsoccurs.getLength());
	      if(refoccur >=0 && refoccur < altprogsoccurs.getLength())
	      {
	      	altProg = altprogsoccurs.item(refoccur);
	      }
      }
   }
   else if(altprogs.getLength() > 0)
   {
      log.info("findAltNode: alternative programs with same start time for {}: {}", progid, altprogs.getLength());
      altProg = altprogs.item(0);
   }

//   // Must log here as Optional does not have ifNotPresent capability so 
//   // caller cannot log a message when Node is absent and do processing when present
//   if( altProg == null)
//   {
//   	log.info("findAltNode: NO alternative found for {}", progid);
//   }
   
   return Optional.ofNullable(altProg);
}

private void adjustTimes(Node refProg, Node altProg, String progid)
{
   // Compare the times for ref and alt. To maximise the chance of recording the entire program
   // should take the earliest starttime and the latest endtime.
   // How to change attribute values?
   // How to compare the dates?
   String refstart = nu.getAttributeValue(refProg, "start");
   String altstart = nu.getAttributeValue(altProg, "start");
   String altend = nu.getAttributeValue(altProg, "stop");
   String refend = nu.getAttributeValue(refProg, "stop");
   
   // Can't rely on the timezone offsets being the same so convert to Date for
   // comparing but use the original string if necessary to change the time.
   // Should round the times to 5mins (down for start, up for end) for compatibility
   // with the timer editor. Would then need to be able to convert the Date back
   // to timezone aware string. The 5min adjustment is done when the web pages are
   // generated from the merged XML so it's not so important to do it here.
   ZonedDateTime refdt = XMLTVutils.getZDateFromXmltv(refstart);
   ZonedDateTime altdt = XMLTVutils.getZDateFromXmltv(altstart);
   String zxmltvdt = null;
   
   // Get the earliest start time then quantize it down. Update the reference if necessary.
   if(altdt.isAfter(refdt))
   { 
   	altdt = refdt;
   }
   altdt = XMLTVutils.getQuantizedDate(altdt, 5, -1);
   
   if(!altdt.equals(refdt) )
   {
   	zxmltvdt = XMLTVutils.getXmltvFromZDate(altdt);
   	nu.setAttributeValue(refProg, "start", zxmltvdt);
   	log.info("adjustTimes: changed start for {} from {} to {}", progid, refstart, zxmltvdt);
   }
  
   refdt = XMLTVutils.getZDateFromXmltv(refend);
   altdt = XMLTVutils.getZDateFromXmltv(altend);
   
   // Get the latest stop time then quantize it up. Update the reference if necessary.
   if( altdt.isBefore(refdt))
   {
      	altdt = refdt;
   }
   altdt = XMLTVutils.getQuantizedDate(altdt, 5, 1);
 
   if(!altdt.equals(refdt) )
   {
   	zxmltvdt = XMLTVutils.getXmltvFromZDate(altdt);
   	nu.setAttributeValue(refProg, "stop", zxmltvdt);
   	log.info("adjustTimes: changed stop for {} from {} to {}", progid, refend, zxmltvdt);
   }	
}

private void copyFields(Node refProg, Node altProg, String[] fieldnames, String progid)
{
	for(String fieldname : fieldnames)
	{
		Optional<Node> optrefFld = nu.findNodeByPath(refProg, fieldname);
		if( ! optrefFld.isPresent())
		{
	      Node altFld = null;
	      try
	      {
	         altFld = nu.getNodeByPath(altProg, fieldname);
	         if((altFld == null))
	         {
	            log.info("copyFields: alternative for {} has no field {}", progid, fieldname);
	            continue;
	         }
	      }
	      catch(TransformerException tex)
	      {
	         log.info("copyFields: exception for {} finding field: {}", progid, fieldname, tex);
	         continue;
	      }
	
	      Node newNode = altFld.cloneNode(true);  // Create a duplicate node
	      refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
	      refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
	      log.info("copyFields: added field {} to {}: {}", fieldname, progid, newNode.getTextContent());
		}
		else
		{
			log.debug("copyFields: reference {} already contains field {}", progid, fieldname);
		}
	}
}

public void writeUpdatedXMLTV(String filename) throws Exception
{
   nu.outputNode(this.refDoc, new File(filename));
}


public static void main(String[] args) throws Exception
{
CmdArgMgr cmd = new CmdArgMgr();
String ref = null;
String alt = null;
String result = null;
String [] keys = null;

   cmd.parseArgs(args);
   keys = cmd.getArgNames();


   for(int i = 0; i<keys.length; i++)
   {
      String val = cmd.getArg(keys[i]);
      if(ARG_REF.compareTo(keys[i]) == 0)
         ref = val;
      else if(ARG_ALT.compareTo(keys[i]) == 0)
         alt = val;
      else if(ARG_RESULT.compareTo(keys[i]) == 0)
         result = val;
   }


   if((ref==null) || (alt==null) || (result==null))
   {
      System.out.println("Usage: HTMLMaker " + ARG_REF + "=<reference file> " +
            ARG_ALT + "=<alternative file> " +
            ARG_RESULT + "=<result> ");
      System.exit(1);
   }



   XMLTVSourceCombiner sc = new XMLTVSourceCombiner(ref, alt);
   sc.combineSource("episode-num", "sub-title");
  // sc.combineSource("episode-num");
   sc.writeUpdatedXMLTV(result);
}
}
