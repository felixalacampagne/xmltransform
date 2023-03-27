package com.scu.xmltv;

import java.io.File;

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
   are ending up with no episode info which is requires a lot of manual actions to recover. 
   The tvguide source was used as the reference but to avoid this issue it is now the epg which is used as
   reference to ensure the episode info is present - the problem with this is that the epg is not reliable, 
   sometimes it disappears, especially after a reboot.
   
   Currently the starttime is used as-is to match the program in the reference and alternative. In practice
   it should probably not be used at all, would be better to select based on the order of the programme, ie. first 
   occurrence of 'program' in the reference should match with the first occurrence in the alternative, maybe with a
   sanity check on the times. This should work in the case of the midday episode, eg. Grey Anatomy, which is the repeat of
   the previous days evening episode and there is a different episode for the current evening.
   
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

public void combineSource(String fieldname)
{
   if(refDoc == null)
   {
      refDoc = nu.parseXML(refXMLTV);
      altDoc = nu.parseXML(altXMLTV);
   }

   // Select all the /tv/programme nodes with no episode-num value

   // for each node find a matching programme(start and channel) and programme/title
   //    NB start has a format like "20200220001500 +0100" so might not match if different timezone offsets are used!
   //    title needs to be a case-insensitive match!!
   //    If one is found add it to the 'reference' node.


   NodeList progs = null;
   progs = nu.getNodesByPath(refDoc, "/tv/programme[not(" + fieldname + ")]");
   for(int i = 0; i <  progs.getLength(); i++)
   {
      Node refProg = progs.item(i);
      String title = nu.getNodeValue(refProg, "title");

      // Locate a matching node in altDoc with a fieldname
      String starttime = nu.getAttributeValue(refProg, "start");
      String chanid = nu.getAttributeValue(refProg, "channel");


            // The starttime in alt should be for the same day as the ref item.
      // A show can be broadcast multiple times during the day, eg. Grey's Anatomy
      // is shown at midday and in the evening, Minx is shown as multiple 
      // episode one after the other. The Minx case prevents a large date discrepancy
      // from being accepted (Minx is ca.25mins!)
      
      // Need to figure out a way to determine the occurrence of the current program in
      // the day for both ref and alt.
      //
      // Can't think of a simple way to do it.
      // The procedure will be something like:
      //   select using starttime,chanid. 
      //     single match use it directly
      //     no match 
      //        select from 'ref' for chanid, 'programme',starttime[day] 
      //           select from alt with criteria chanid, 'programme',starttime[day] - should give same number
      //           different count - goto next ref node
      //              determine occurrence number of ref node (1 if there is only one occurrence!)
      //              locate occurrence number of alt node
      //              sanity check starttime
      //              use alt details.
      NodeList altprogs = nu.getNodesByPath(altDoc, "/tv/programme[@start='" + starttime + "' and @channel='" + chanid + "']");
      Node altProg = null;
      if(altprogs == null || (altprogs.getLength() == 0))
      {
         log.debug("combineSource: No program found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
         
         int refoccur = -1;
         String startday = starttime.substring(0,8);
         String occurCrit = "/tv/programme[" 
               + "starts-with(@start, '" + startday + "') and " 
		         + " @channel='" + chanid + "' and "
		         + " title='" + title + "'"
               + "]";
         NodeList refprogsoccurs = nu.getNodesByPath(refDoc, occurCrit);
         
         for(int occurs = 0; i <  refprogsoccurs.getLength(); i++)
         {
         	Node refOccur = refprogsoccurs.item(occurs);
            String occurStarttime = nu.getAttributeValue(refOccur, "start");         	
         	if(occurStarttime.equals(starttime))
         	{
         		log.debug("combineSource: match found for start=" + startday  + " and channel= " + chanid  + " occurence=" + occurs);
         		refoccur = occurs;
         		break;
         	}
         }
         // at least one must match since starttime comes from the same list

         NodeList altprogsoccurs = nu.getNodesByPath(altDoc, occurCrit);
         log.debug("combineSource: refoccur={} altprogsoccurs length={}", refoccur, altprogsoccurs.getLength());
         if(refoccur >=0 && refoccur < altprogsoccurs.getLength())
         {
         	altProg = altprogsoccurs.item(refoccur);
         }
         
      }
      else if(altprogs.getLength() > 1)
      {
         log.info("combineSource: Multiple programs found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
         altProg = altprogs.item(0);
      }

      if(altProg == null)
      {
      	continue;
      }
      
      Node altFld = null;
      try
      {
         altFld = nu.getNodeByPath(altProg, fieldname);
         if((altFld == null))
         {
            log.debug("combineSource: Alt source has no field '" + fieldname + "' for '" + title + "'");
            continue;
         }
      }
      catch(TransformerException tex)
      {
         log.info("combineSource: exception finding field:" + fieldname + ": " + tex);
         continue;
      }

      Node newNode = altFld.cloneNode(true);  // Create a duplicate node
      refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
      refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
      log.info("combineSource: updated '" + title + "' (" + starttime + " " + chanid + "): " + newNode.getTextContent());

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
   sc.combineSource("sub-title");
   sc.combineSource("episode-num");
   sc.writeUpdatedXMLTV(result);
}
}
