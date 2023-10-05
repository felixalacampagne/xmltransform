package com.scu.xmltv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dontocsata.xmltv.XmlTvParseException;
import com.dontocsata.xmltv.model.XmlTvProgram;
import com.scu.jxmltv.XmltvParser;
import com.scu.jxmltv.XmltvStore;
import com.scu.utils.CmdArgMgr;
import com.scu.utils.NodeUtils;
import com.scu.utils.Utils;


/**
 * Takes a 'reference' file and updates a missing field from an
 * 'alternative' source (I couldn't think of a better name!)
 *
 * The initial use case is as follows:
 * tvgrabnl does a good job of getting descriptions of programmes etc.
 * but recently it has not been getting the episode info. The episode info
 * seems to be reliably available from the VUUltimo EPG, but the
 * descriptions are pretty poor. The aim is to use the tvgrabnl
 * file as the source and fill in missing episode infos from the
 * epg file.
 *
   Many moons later...
   The TVguide and EPG times are not the same. This seems to be especially the case for programmes early
   in the day. Sometimes the difference is minutes but occasionally it is more than an hour. The large
   discrepencies appear to be related to the designation of 'No broadcast' periods. I think in some cases
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

   08 Sep 2023 The GB guide has stopped working so now forced to rely on the Ultimo EPG data. The EPG data does not
   contain the episode info for GB channels. Given that I'm not sure how the EPG xmltv extractor works (it is written
   in python) an attempt is made here to extract missing episode info from the description.
   Using the same xmltv file for both BE and GB progs results in duplications when the BE EPG data is combined with
   guide data and then merged with the original EPG data used as the GB guide (if you see what I mean). Still want
   to keep the combining of EPG and guide info so now the combine filters programmes which do not have a channel in
   both the ref and alt sources - this means a combined GB guide with only GB progs and a combined BE guide with only
   BE progs can be produced from the original EPG containing all progs - this should avoid the duplicate programs, I hope!

   18 Sep 2023 Alt guide is sometimes missing required channels which results in them being excluded even though they
   are present in the ref guide. Changed the filter to simply use a regex expression for the channel ids to keep. Thus
   when combining EPG and GB guide the regex matches the GB guide ids, and matches the BE guide ids when combining the
   EPG and BE guides. Much easier to use providing the IDs are readily distinguishable.

   25 Sep 2023 GB tvguide is working again so would like to switch back to using it as the ref since it is generally
   more reliable than the EPG. The EPG contains more detailed descriptions. Neither contains explicit sub-title info.
   Therefore need to use the EPG (alt) desc even though one is present in the GB (ref) guide. Need to look for
   implicit sub-title in the (EPG) desc.

 * @author Chris
 *
 */
public class XMLTVSourceCombiner
{
public final static String ARG_OUTFILE = "-combine";
private static final String ARG_REF = "-ref";
private static final String ARG_ALT = "-alt";
private static final String ARG_RESULT = "-res";
private static final String ARG_FILTER = "-filter";

private final File refXMLTV;
private final File altXMLTV;
private final NodeUtils nu = NodeUtils.getNodeUtils();
private final Pattern regexFilter;

private Document refDoc = null;
private Document altDoc = null;

private XmltvStore altStore = new XmltvStore();

static Logger log = LoggerFactory.getLogger(XMLTVSourceCombiner.class);

private static final Pattern sEpPattern = Pattern.compile("\\( *S(\\d{1,2}) *Ep(\\d{1,2}) *\\)"); // (S1 Ep9)
private static final Pattern bbcPatternA = Pattern.compile("^(\\d{1,2})/(\\d{1,2})\\. ");
private static final Pattern bbcPatternB = Pattern.compile("^\\.\\.\\.\\S.*?\\. (\\d{1,2})/(\\d{1,2})\\. ");
private static final Pattern subtitPattern = Pattern.compile("^(?:\\.\\.\\.\\S.*?: )?(\\S.*?): "); // ignore case?
private static final Pattern newpfxes = Pattern.compile("(\\.\\.\\.\\S.*?: )?(?:Brand new series *[:-] *|Brand new: |New: )");

private final ZoneId zoneId = ZoneId.systemDefault(); // Use the system default time zone
private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

public XMLTVSourceCombiner(String referenceXMLTV, String alternateXMLTV)
{
   refXMLTV = new File(referenceXMLTV);
   altXMLTV = new File(alternateXMLTV);
   regexFilter = null;
}

public XMLTVSourceCombiner(String referenceXMLTV, String alternateXMLTV, String regexFilter)
{
   // Can't init files in a shared method because they must be initd in the constructor!
   refXMLTV = new File(referenceXMLTV);
   altXMLTV = new File(alternateXMLTV);
   this.regexFilter = regexFilter!=null ? Pattern.compile(regexFilter) : null;
}

protected void initDocs()
{
   if(refDoc == null)
   {
      refDoc = nu.parseXML(refXMLTV);
      altDoc = nu.parseXML(altXMLTV);
      try
      {
         XmltvParser.parse(altXMLTV, altStore);
      }
      catch (FileNotFoundException | XmlTvParseException e)
      {
         // This means that there will be no combining with the alt file,
         // but at least a guide will continue to be produced
         log.warn("initDocs: failed to parse alt file: {}", altXMLTV.getAbsolutePath(), e);
      }

   }
}

public void combineSource(String... fieldnames)
{
   initDocs();

   try
   {
      filterProgrammes();
   }
   catch (TransformerException e)
   {
      // Continue anyway as it's better to have a listing with too much than no listing at all
      log.warn("combineSource: filter failed: {}", e.toString());
   }

   NodeList progs = nu.getNodesByPath(refDoc, "/tv/programme");
   int progcnt = progs.getLength();
   StopWatch sw = StopWatch.createStarted();
   String durationFormat = "mm:ss";
   int lastpcDone = 0;
   long lastSplit = 0;
   for(int i = 0; i <  progcnt; i++)
   {
      Node refProg = progs.item(i);

      String progid = nu.getNodeValue(refProg, "title") + ":"
                    + nu.getAttributeValue(refProg, "start") + ":"
                    + nu.getAttributeValue(refProg, "channel");
      log.debug("combineSource: processing {}", progid);

      findAltNode(refProg, progid).ifPresentOrElse(
            altn -> {
               copyFields(refProg, altn, fieldnames, progid);
               adjustTimes(refProg, altn, progid);
               },
            () -> {log.debug("combineSource: NO alternative found for {}", progid); }
            );
      cleanProg(refProg);
      extractMissingEpisodeInfo(refProg, progid);

      int pcDone = ((i *100 ) / progcnt);
      if((pcDone != lastpcDone) && (pcDone % 5) == 0)
      {
      	long split = sw.getTime();
      	long estTotalElapsed = split * progcnt / i;  // gives an estimate of the total time to process all records
      	String formattedEstTotalElapsed = DurationFormatUtils.formatDuration(estTotalElapsed, durationFormat);

      	// Want time to do the 5% as it appears to get longer as the processing get closer to the end
      	String formattedSplit = DurationFormatUtils.formatPeriod(lastSplit, split, durationFormat);
      	lastSplit = split;

      	Instant instant = Instant.ofEpochMilli(sw.getStartTime() + estTotalElapsed);
      	LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
      	String formattedDateTime = localDateTime.format(formatter);
      	log.info("combineSource: Progress:{}({}%) Elapsed: split:{} total:{} Est.End:{} Duration:{}",
      			String.format("%04d",i), pcDone, formattedSplit,
      			DurationFormatUtils.formatDuration(split, durationFormat), formattedDateTime, formattedEstTotalElapsed);
      	lastpcDone = pcDone; // avoid multiple output for same %age
      }
   }
}

// Clean fields of all programmes in refDoc - not used, decided to do it per prog before extracting missing info
protected void cleanFields()
{
   initDocs();
   NodeList progs = nu.getNodesByPath(refDoc, "/tv/programme");

   for(int i = 0; i <  progs.getLength(); i++)
   {
      cleanProg(progs.item(i));
   }
}

protected void cleanProg(Node refProg)
{
   String [] fields = new String[] { "desc", "title" };

   for(String fieldname : fields)
   {
      Optional<Node> optrefFld = nu.findNodeByPath(refProg, fieldname);
      if(optrefFld.isPresent())
      {
         Node refFld = optrefFld.get();
         String origTxt = Utils.safeString(refFld.getTextContent());
         Matcher m = newpfxes.matcher(origTxt);
         String updTxt = m.replaceAll("$1");  // origTxt.replaceAll("^Brand new series - ", "").replaceAll("^New: ", "");
         if(!origTxt.equals(updTxt))  // Is it worth doing this check?
         {
            refFld.setTextContent(updTxt);
         }
      }
   }
}

// 'protected' so it can be tested.
protected void filterProgrammes() throws TransformerException
{
   initDocs();  // so it can be tested
   Optional<Pattern> regex = getRegexFilter();
   NodeList refchans = nu.getNodesByPath(refDoc, "/tv/channel");
   List<String> refchanids = new ArrayList<>();

   for(int idx=0 ; idx<refchans.getLength() ; idx++)
   {
      Node channel = refchans.item(idx);
      String chanId = nu.getAttributeValue(channel, "id");
      refchanids.add(chanId);
   }

   if(!regex.isPresent())
   {
      return;
   }
   
   final Pattern pat =  regex.get();
   refchanids = refchanids.stream().filter(rc -> pat.matcher(rc).matches() ).collect(Collectors.toList());
   

   // Now remove programmes from ref doc for channel ids not in the filtered list
   NodeList progs = nu.getNodesByPath(refDoc, "/tv/programme");
   Node tvNode = nu.getNodeByPath(refDoc, "/tv");
   for(int i = 0; i <  progs.getLength(); i++)
   {
      Node refProg = progs.item(i);
      String progchan = nu.getAttributeValue(refProg, "channel");
      if( ! refchanids.contains(progchan))
      {
         log.debug("filterProgrammes: removing program: {}:{}", nu.getAttributeValue(refProg, "channel"),  nu.getNodeValue(refProg, "title"));
         tvNode.removeChild(refProg);
      }
   }

   // refDoc still contains the extra channels which should not appear int he channels list of the output document
   for(int i = 0; i <  refchans.getLength(); i++)
   {
      Node channel = refchans.item(i);
      String chanid = nu.getAttributeValue(channel, "id");
      if( ! refchanids.contains(chanid))
      {
         tvNode.removeChild(channel);
      }
   }

}


// 10-Sep-2023 Kludge to workaround EPG missing BBC1 programme info.
// It appears that BBC1 programme info is no longer available. This makes it
// difficult to add BBC1 progs on the Dreambox, which only has SD channels, and
// annoying on the Ultimo which has both HD and SD but HD is more likely to cause a
// conflict with existing timers.
// For now the solution will copy the programmes for one channel into another channel.
// Must be done after the filter since the chances are dest channel will be absent from one or both the inputs.
// Need to add a <channel> for the dest channel since there probably wont already be one.
// Remove this. Issue is moot as BBC SD has become unwatchable due to the red stopping banner
// and this was not used anyway as it was easier to map a dummy BBC1HD channel to BBC1SD in the grabbers.
@Deprecated
protected void shadowChannel(String srcChannel, String destChannel, String destDisplayName) throws TransformerException
{
   initDocs();
   NodeList progs = nu.getNodesByPath(refDoc, "/tv/programme[@channel='" + srcChannel + "']");
   Node tvNode = nu.getNodeByPath(refDoc, "/tv");

   if(progs.getLength() < 1)
   {
      log.info("shadowChannel: no programmes found for channel '{}'", srcChannel);
      return;
   }

   for(int i = 0; i <  progs.getLength(); i++)
   {
      Node prog = progs.item(i);
      Node newNode = prog.cloneNode(true);
      nu.setAttributeValue(newNode, "channel", destChannel);
      refDoc.adoptNode(newNode);
      tvNode.appendChild(newNode);
   }

   Node destChanNode = nu.getNodeByPath(refDoc, "/tv/channel[@id='" + destChannel + "']");
   if(destChanNode == null)
   {
      // Probably easier to copy the source node and update the bits which are known.
      Node srcChanNode = nu.getNodeByPath(refDoc, "/tv/channel[@id='" + srcChannel + "']");
      if(srcChanNode == null)
      {
         log.error("shadowChannel: channel '{}' not found - this should not happen!", srcChannel);
         return;
      }
      Node newNode = srcChanNode.cloneNode(true);
      nu.setAttributeValue(newNode, "id", destChannel);
      Node disp = nu.getNodeByPath(newNode, "display-name"); // maybe need the text child node
      disp.setTextContent(destDisplayName);

      refDoc.adoptNode(newNode);
//      tvNode.appendChild(newNode); This inserts after the programme block, which might mess something up
      // Ideally new node should go at end of channel block or after the src channel but this is easier!
      tvNode.insertBefore(newNode, srcChanNode);

   }
}

private void extractMissingEpisodeInfo(Node refProg, String progid)
{
   // Aim of function is to extract missing "episode-num" and "sub-title" data from the show description.
   // This mainly applies to the UK show info from the Ultimo EPG data which is not handled by the python
   // grabber - it seems to work OK for the TVV shows. I have noticed that there is sometimes some episode
   // info buried in the description so the idea is to use it if there is none already present in the
   // refProg - I'm assuming that anything useful in altn has already been copied into refProg.

   String desc = nu.getNodeValue(refProg, "desc");
   if( Utils.safeIsEmpty(desc)) {
      return;
   }

   String epnum = nu.getNodeValue(refProg, "episode-num");
   if( Utils.safeIsEmpty(epnum))
   {
      int season = -1;
      int ep = -1;
      int eptot = 99;

      Matcher m = sEpPattern.matcher(desc);
      if(m.find())
      {
         season = nu.stringToInt(m.group(1));
         ep = nu.stringToInt(m.group(2));
      }
      else if( (m=bbcPatternA.matcher(desc)).find() )
      {
         season = 1;
         ep = nu.stringToInt(m.group(1));
         eptot = nu.stringToInt(m.group(2));
      }
      else if( (m=bbcPatternB.matcher(desc)).find() )
      {
         season = 1;
         ep = nu.stringToInt(m.group(1));
         eptot = nu.stringToInt(m.group(2));
      }

      if((season > 0) && (ep > 0))
      {
         // S2 Ep13
         // <episode-num system="xmltv_ns">1 . 12/99 . </episode-num>
         // should eptot be -1? Don't use it so don't really care...
         epnum = String.format("%d . %d/%d . ", season - 1, ep - 1, eptot);

         Node newNode = refDoc.createElement("episode-num");  // new episode-num node
         nu.setAttributeValue(newNode, "system", "xmltv_ns");
         newNode.appendChild(refDoc.createTextNode(epnum));

         refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
         refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
         log.debug("extractMissingEpisodeInfo: added field episode-num to {}: {}", progid, newNode.getTextContent());
      }
   }

   String subtitle = nu.getNodeValue(refProg, "sub-title");
   if( Utils.safeIsEmpty(subtitle))
   {
      Matcher m = subtitPattern.matcher(desc);
      if(m.find())
      {
         subtitle = m.group(1);
         Node newNode = refDoc.createElement("sub-title");
         nu.setAttributeValue(newNode, "lang", "en");
         newNode.appendChild(refDoc.createTextNode(subtitle));

         refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
         refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
         log.debug("extractMissingEpisodeInfo: added field sub-title to {}: {}", progid, newNode.getTextContent());
      }
   }
}

private Optional<XmlTvProgram> findAltProgram(Node refProg, String progid)
{
   XmlTvProgram xprog = null;
   String title = nu.getNodeValue(refProg, "title");
   String starttime = nu.getAttributeValue(refProg, "start");
   String day = starttime.substring(0,8);
   String chanid = nu.getAttributeValue(refProg, "channel");

   List<XmlTvProgram> progs;
   progs = this.altStore.getProgrammesForDayChannel(day, chanid);

   // If there is only one that that's the one to return
   // If more than one check if there is a match for the start time
   // If no match then do the 'fuzzy' match which requires determining the occurrence of the
   // program in ref, which is a slow process.
   List<XmlTvProgram> titles = progs.stream()
         .filter(p -> p.getTitle().equalsIgnoreCase(title))
         .sorted((t1, t2) -> (t1.getStart().compareTo(t2.getStart())))
         .toList();
   if(titles.size() == 1)
   {
      xprog = titles.get(0);
   }
   else if(titles.size() > 1)
   {
      // first look for exact matching time
      ZonedDateTime zdt = XmltvParser.XMLTVToZonedDateTime(starttime);

      // Assume there can only be one exact match for a real guide
      Optional<XmlTvProgram> optexacttime = titles.stream()
         .filter(p -> p.getStart().equals(zdt))
         .findAny();

      if(optexacttime.isPresent())
      {
         xprog = optexacttime.get();
      }
      else
      {
         // this is where it gets really ugly!!!
         // Need to determine the occurrence no. of the node and then pick the corresponding occurrence from the alt list.
         // Orig version searched the entire list for matches for the day/channel/title
         // Going to try to use some caching to reduce the list of nodes to search through.
         // Can't use XPath to search a nodelist but on the plus side don't need to worry about making the title safe

         int refoccur = -1;
         String startday = starttime.substring(0,8);
         NodeList refNodes = getNodesForDayChannel(startday, chanid);
         List<Node> ntitles = new ArrayList<>();

         for(int occurs = 0; occurs <  refNodes.getLength(); occurs++)
         {
            Node refOccur = refNodes.item(occurs);
            if(title.equalsIgnoreCase(nu.getNodeValue(refOccur, "title")))
            {
               ntitles.add(refOccur);
            }
         }
         Collections.sort(ntitles, (n1, n2) -> nu.getNodeValue(n1, "start").compareTo(nu.getNodeValue(n2, "start")) );

         // Find occurrence of refProg
         int i=0;
         for(Node n : ntitles)
         {

            if(starttime.equals(nu.getNodeValue(n, "start")) )
            {
               refoccur = i;
               break;
            }
            i++;
         }

         if(refoccur < 0)
         {
            log.debug("findAltProg: Failed to find reference occurrence for {}", progid);
         }
         else if(titles.size() > refoccur)
         {
            xprog = titles.get(refoccur);
         }
         else
         {
            log.debug("findAltProg: no alt occurrence {} for {}", refoccur, progid);
         }
      }
   }
   return Optional.ofNullable(xprog);
}

private Map<String, NodeList> nodesDayChan = new HashMap<>();
private NodeList getNodesForDayChannel(String startday, String chanid)
{
   String key = startday + ":" + chanid;
   NodeList refprogs = nodesDayChan.get(key);
   if(refprogs == null)
   {
      String occurCrit = "/tv/programme["
         + "starts-with(@start, '" + startday + "') and "
         + "@channel='" + chanid + "'"
         + "]";
      refprogs = nu.getNodesByPath(refDoc, occurCrit);
      nodesDayChan.put(key, refprogs);
   }

   return refprogs;
}

private Optional<Node> findAltNode(Node refProg, String progid)
{
	Node altProg = null;
	String title = nu.getNodeValue(refProg, "title");
   String starttime = nu.getAttributeValue(refProg, "start");
   String chanid = nu.getAttributeValue(refProg, "channel");
   boolean fuzzyMatch = true;
   // Could cache the programmes for chanid since they are usually processed in channel order
   // Could maybe cache on chanid and day
   // Could keep a map of the lists so it's not important that the progs are processed in order.
   // Don't know how to do an xpath using nodelist as target though which would mean having to manually
   // search through the list which is potentially slow.
   // The cache is a list of lists.
   // The top list has key channel/day and the value is a list with key programme/starttime
   //   - should it be starttime/programme to ensure the order of multiple episodes is preserved.
   // This might not work for multi-episodes which occur either side of midnight
   //   - this probably doesn't matter unless there is a time discrepancy which the episode is today
   //     in one guide and tomorrow in the other
   NodeList altprogs = nu.getNodesByPath(altDoc, "/tv/programme[@start='" + starttime + "' and @channel='" + chanid + "']");

   if(fuzzyMatch)
   {
	   if(altprogs == null || (altprogs.getLength() == 0))
	   {
	   	// Cache might be more useful here
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

	      // there is no safe title - XPath just doesn't support searching for single quote!
	      String safeTitle = title.replace("\"", "?");
	      String occurCrit = "/tv/programme["
	            + "starts-with(@start, '" + startday + "') and "
		         + "@channel='" + chanid + "' and "
		         + "title=\"" + safeTitle + "\""
	            + "]";

	      try
	      {
	         // TODO: Use safeGetNodeByPath
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
	      }
	      catch(Exception ex)
	      {
	         log.warn("findAltNode: search criteria:{} exception:{}", occurCrit, ex.toString());
	      }

	      if(refoccur < 0)
	      {
	      	log.debug("findAltNode: Failed to find reference occurrence for {} with predicate [{}]", progid, occurCrit);
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
	      log.debug("findAltNode: alternative programs with same start time for {}: {}", progid, altprogs.getLength());
	      altProg = altprogs.item(0);
	   }
   }
   return Optional.ofNullable(altProg);
}

private void adjustTimes(Node refProg, Node altProg, String progid)
{
   // Compare the times for ref and alt. To maximise the chance of recording the entire program
   // should take the earliest starttime and the latest endtime.
   String refstart = nu.getAttributeValue(refProg, "start");
   String altstart = nu.getAttributeValue(altProg, "start");
   String altend = nu.getAttributeValue(altProg, "stop");
   String refend = nu.getAttributeValue(refProg, "stop");

   // Can't rely on the timezone offsets being the same so convert to Date for
   // comparing but use the original string if necessary to change the time.
   // Should round the times to 5mins (down for start, up for end) for compatibility
   // with the timer editor.
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
   	log.debug("adjustTimes: changed start for {} from {} to {}", progid, refstart, zxmltvdt);
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
   	log.debug("adjustTimes: changed stop for {} from {} to {}", progid, refend, zxmltvdt);
   }
}

private Optional<Node> safeGetNodeByPath(Node altProg, String fieldname)
{
Node node = null;
   try
   {
      node = nu.getNodeByPath(altProg, fieldname);
   }
   catch(TransformerException tex)
   {
      log.info("safeGetNodeByPath: exception finding field '{}': ", fieldname, tex.toString());
   }
   return Optional.ofNullable(node);
}

private void copyFields(Node refProg, Node altProg, String[] fieldnames, String progid)
{
	for(String fieldname : fieldnames)
	{
      Optional<Node> optAltFld = safeGetNodeByPath(altProg, fieldname);
      if(! optAltFld.isPresent() )
      {
         log.debug("copyFields: alternative for {} has no field {}", progid, fieldname);
         continue;
      }
      Node altFld = optAltFld.get();

		Optional<Node> optrefFld = nu.findNodeByPath(refProg, fieldname);
		if( ! optrefFld.isPresent())
		{
         Node newNode = altFld.cloneNode(true);  // Create a duplicate node
         refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
         refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
	      log.debug("copyFields: added field {} to {}: {}", fieldname, progid, newNode.getTextContent());
		}
		else if(optrefFld.isPresent() && "desc".equals(fieldname))
      {
		   // Special handling for 'desc' as the field might be present in ref but contain more info in the alt
         Node refFld = optrefFld.get();
         String refDesc = Utils.safeString(refFld.getTextContent());
         String altDesc = Utils.safeString(altFld.getTextContent());
         if(altDesc.length() > refDesc.length())
         {
            refFld.setTextContent(altDesc);
         }
      }
		else
		{
			log.debug("copyFields: reference {} already contains field {}", progid, fieldname);
		}
	}
}


private Optional<String> getEpisodenum(XmlTvProgram prog)
{
   String epnum = null;
   // TODO format as an xmltv epdisode-num
   
   return Optional.ofNullable(epnum);
}

private void copyFields(Node refProg, XmlTvProgram altProg, String[] fieldnames, String progid)
{
   // Not so easy to use an array of fields names against a Java object
   // For now a simple if equals list will have to do. Using reflection is a bit over
   // the top and kind of slow given that this is a performace enhancement!
   // "episode-num", "sub-title", "desc"
   
   for(String fieldname : fieldnames)
   {
      Optional<Node> optrefFld = nu.findNodeByPath(refProg, fieldname);
      String fieldval = null;
      if("episode-num".equals(fieldname))
      {
         if( ! optrefFld.isPresent())
         {
            Optional<String> o = getEpisodenum(altProg);
            if(o.isPresent())
            {
               fieldval = o.get();
               // TODO Add new Node
            }
            
         }
      }
      else if("sub-title".equals(fieldname))
      {
         if( ! optrefFld.isPresent())
         {
            fieldval = altProg.getSubTitle();
            if(fieldval != null)
            {
               // TODO Add new Node
            }
         }
      }
      else if("desc".equals(fieldname))
      {
         String refDesc = "";
         // Special handling for 'desc' as the field might be present in ref but contain more info in the alt
         if(optrefFld.isPresent())
         {
            Node refFld = optrefFld.get();
            refDesc = Utils.safeString(refFld.getTextContent());
            String altDesc = Utils.safeString(altProg.getDescription());
            if(altDesc.length() > refDesc.length())
            {
               refFld.setTextContent(altDesc);
            }
         }
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

public void writeUpdatedXMLTV(Writer writer) throws Exception
{
   nu.outputNode(this.refDoc, writer);
}


public Optional<Pattern> getRegexFilter()
{
   return Optional.ofNullable(regexFilter);
}

public static void main(String[] args) throws Exception
{
CmdArgMgr cmd = new CmdArgMgr();
String ref = null;
String alt = null;
String result = null;
String filter = null;
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
      else if(ARG_FILTER.compareTo(keys[i]) == 0)
         filter = val;
   }


   if((ref==null) || (alt==null) || (result==null))
   {
      System.out.println("Usage: HTMLMaker " + ARG_REF + "=<reference file> " +
            ARG_ALT + "=<alternative file> " +
            ARG_RESULT + "=<result> ");
      System.exit(1);
   }



   XMLTVSourceCombiner sc = new XMLTVSourceCombiner(ref, alt, filter);
   log.info("main: combine starting");
   StopWatch sw = StopWatch.createStarted();
   sc.combineSource("episode-num", "sub-title", "desc");
   sw.stop();
   log.info("main: combine done: Time Elapsed: {}", sw.formatTime());
   // For
   // findAltNode enabled:  speedy/eclipse debug/normal size guide/no cache   Time Elapsed: 00:26:59.684
   // findAltNode disabled: speedy/eclipse debug/normal size guide/no cache   Time Elapsed: 00:06:08.759
   // findAltNode nofuzzy:  speedy/eclipse debug/normal size guide/no cache   Time Elapsed: 00:11:56.247
   // findAltNode nofuzzy/reduced log:  speedy/eclipse debug/normal size guide/no cache   Time Elapsed: 00:11:53.861
   //                                                                                     Time Elapsed: 00:11:44.409
   // findAltNode fuzzy/reduced log:  speedy/eclipse debug/normal size guide/no cache   Time Elapsed: 00:26:00.307

   // Timings show that it takes longer and longer to find the alternative program, probably because the
   // XPath is doing a linear search from the first item.
   // Anything involving caching is going to create a NodeList which cannot be searched with XPath. Getting the
   // values out of the nodes in the nodelist is very cumbersome so the thought is to parse the XMLTV into a
   // Java object model so the fields are easily accessible for searching. Could write my own which would probably
   // use jaxb however I don't want to have to create the schema from scratch.
   // By chnace I found an XMLTV to Java object model parser which might make creating something which can be searched faster than
   // the XML DOM
   // https://github.com/raydouglass/xmltv-to-mxf/blob/master/pom.xml.
   sc.writeUpdatedXMLTV(result);
}

}
