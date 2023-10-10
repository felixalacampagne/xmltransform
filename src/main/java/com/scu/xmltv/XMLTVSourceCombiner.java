package com.scu.xmltv;

import java.io.File;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

import com.dontocsata.xmltv.model.XmlTvProgram;
import com.dontocsata.xmltv.model.XmlTvProgramId;
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
private static final Pattern sEpPatternBare = Pattern.compile(" *S(\\d{1,2}) *Ep(\\d{1,2}) *$"); // S1 Ep9

private static final Pattern bbcPatternA = Pattern.compile("^(\\d{1,2})/(\\d{1,2})\\. ");
private static final Pattern bbcPatternB = Pattern.compile("^\\.\\.\\.\\S.*?\\. (\\d{1,2})/(\\d{1,2})\\. ");
private static final Pattern subtitPattern = Pattern.compile("^(?:\\.\\.\\.\\S.*?: )?(\\S.*?): "); // ignore case?
private static final Pattern newpfxes = Pattern.compile("(\\.\\.\\.\\S.*?: )?(?:Brand new series *[:-] *|Brand new: |New: )");

private final ZoneId zoneId = ZoneId.systemDefault(); // Use the system default time zone
private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
private final String durationFormat = "mm:ss";

private final StopWatch swfindalt = StopWatch.create();
private final StopWatch swcopyfields = StopWatch.create();
private final StopWatch swadjustTimes = StopWatch.create();
private final StopWatch swcleanProg = StopWatch.create();
private final StopWatch swextract = StopWatch.create();

private final Map<String, Map<ZonedDateTime, Node>> refDayChanIndex = new HashMap<>();
private final Map<String, Map<ZonedDateTime, Node>> altDayChanIndex = new HashMap<>();

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
//      try
//      {
//         XmltvParser.parse(altXMLTV, altStore);
//      }
//      catch (FileNotFoundException | XmlTvParseException e)
//      {
//         // This means that there will be no combining with the alt file,
//         // but at least a guide will continue to be produced
//         log.warn("initDocs: failed to parse alt file: {}", altXMLTV.getAbsolutePath(), e);
//      }

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


   NodeList progs = nu.getNodesByPath(altDoc, "/tv/programme");
   buildDayChannelIndex(progs, altDayChanIndex);

   progs = nu.getNodesByPath(refDoc, "/tv/programme");
   buildDayChannelIndex(progs, refDayChanIndex);
   int progcnt = progs.getLength();
   StopWatch sw = StopWatch.createStarted();

   int lastpcDone = 0;
   long lastSplit = 0;

   for(int i = 0; i <  progcnt; i++)
   {
      Node refProg = progs.item(i);

      String progid = nu.getNodeValue(refProg, "title") + ":"
                    + nu.getAttributeValue(refProg, "start") + ":"
                    + nu.getAttributeValue(refProg, "channel");
      log.debug("combineSource: processing {}", progid);


      boolean useJxmltv = false;
      if(! useJxmltv )
      {
         findAltNode(refProg, progid).ifPresentOrElse(
               altn -> {
                  copyFields(refProg, altn, fieldnames, progid);
                  adjustTimes(refProg, altn, progid);
                  },
               () -> {log.debug("combineSource: NO alternative found for {}", progid); }
               );
      }
      else
      {
         findAltProgram(refProg, progid).ifPresentOrElse(
               altp -> {
                  copyFields(refProg, altp, fieldnames, progid);
                  adjustTimes(refProg, altp, progid);
                  },
               () -> {log.debug("combineSource: NO alternative found for {}", progid); }
               );
      }

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

         if(log.isDebugEnabled())
         {
            String tfindalt = resetStopWatch(this.swfindalt);
            String tcopyf = resetStopWatch(this.swcopyfields);
            String tadjust = resetStopWatch(this.swadjustTimes);
            String tclean = resetStopWatch(swcleanProg);
            String textract = resetStopWatch(swextract);

            log.debug("combineSource: Progress:{}% split:{} cleanProg:{} extractInfo:{} adjustTImes: {} copyfields:{} findalt:{}", pcDone,
                  formattedSplit, tclean, textract, tadjust, tcopyf, tfindalt);
         }
         else
         {
            log.info("combineSource: Progress:{}({}%) Elapsed: split:{} total:{} Est.End:{} Duration:{}",
                  String.format("%04d",i), pcDone, formattedSplit,
                  DurationFormatUtils.formatDuration(split, durationFormat), formattedDateTime, formattedEstTotalElapsed);
         }
         lastpcDone = pcDone; // avoid multiple output for same %age
      }
   }
}

protected void cleanProg(Node refProg)
{
   resumeStopWatch(swcleanProg);
   String [] fields = new String[] { "desc", "title" };

   for(String fieldname : fields)
   {
      Optional<Node> optrefFld = nu.getChildByName(refProg, fieldname); // findNodeByPath(refProg, fieldname);
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
   suspendStopWatch(swcleanProg);
}

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



private void extractMissingEpisodeInfo(Node refProg, String progid)
{
   // Aim of function is to extract missing "episode-num" and "sub-title" data from the show description.
   // This mainly applies to the UK show info from the Ultimo EPG data which is not handled by the python
   // grabber - it seems to work OK for the TVV shows. I have noticed that there is sometimes some episode
   // info buried in the description so the idea is to use it if there is none already present in the
   // refProg - I'm assuming that anything useful in altn has already been copied into refProg.
   resumeStopWatch(this.swextract);
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
      else if ( (m = sEpPatternBare.matcher(desc)).find())
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
         getEpisodenum(season, ep, eptot).ifPresent(e -> addEpisodeNumToProgram(refProg, e, progid));
      }
   }

   String subtitle = nu.getNodeValue(refProg, "sub-title");
   if( Utils.safeIsEmpty(subtitle))
   {
      Matcher m = subtitPattern.matcher(desc);
      if(m.find())
      {
         subtitle = m.group(1);
         addSubtitleToProgramNode(refProg, subtitle);
         log.debug("extractMissingEpisodeInfo: added field sub-title to {}: {}", progid, subtitle);
      }
   }
   suspendStopWatch(this.swextract);
}

private Optional<String> getEpisodenum(Integer season, Integer episodenumber, Integer totalEpisodesInSeason)
{
   String epnum = null;
   // 0 . 2/99
   // actually it is
   // S/TS . E/TE . P/TP
   // where S = season, E = episode, P = part, T = total
   // the S,E,P values are 0 based BUT the totals are 1 based
   // so first ever episode in two parts is of the only season or 10 episodes is
   // 0/1 . 0/10 . 0/2
   // I've only seen S, E, and TE so that's all I'll support for now
   // S is optional
   // If S and E are absent then return null
   StringBuffer sb = new StringBuffer();
   if(season != null)
   {
      sb.append(season - 1);
   }

   if(episodenumber != null)
   {
      sb.append(" . ").append(episodenumber - 1);
      if(totalEpisodesInSeason != null)
      {
         sb.append("/").append(totalEpisodesInSeason);
      }
      sb.append(" . ");
   }

   if(sb.length() > 0)
   {
      epnum = sb.toString();
   }
   return Optional.ofNullable(epnum);
}

private Optional<String> getEpisodenum(XmlTvProgram prog)
{
   Optional<String> ropt = Optional.empty();
   // 0 . 2/99
   // actually it is
   // S/TS . E/TE . P/TP
   // where S = season, E = episode, P = part, T = total
   // the S,E,P values are 0 based BUT the totals are 1 based
   // so first ever episode in two parts is of the only season or 10 episodes is
   // 0/1 . 0/10 . 0/2
   // I've only seen S, E, and TE so that's all I'll support for now
   // S is optional
   // If S and E are absent then return null
   XmlTvProgramId prgid = prog.getXmlTvProgramId();
   if(prgid != null)
   {
      ropt = getEpisodenum(prgid.getSeason(), prgid.getEpisode(), prgid.getNumberOfEpisodes());
   }
   return ropt;
}

private void addSubtitleToProgramNode(Node refProg, String subtitle)
{
   Node newNode = refDoc.createElement("sub-title");
   nu.setAttributeValue(newNode, "lang", "en");
   newNode.appendChild(refDoc.createTextNode(subtitle));

   refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
   refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!

}

private void addEpisodeNumToProgram(Node refProg, String xmltvEpnum, String progid)
{
   Node newNode = refDoc.createElement("episode-num");  // new episode-num node
   nu.setAttributeValue(newNode, "system", "xmltv_ns");
   newNode.appendChild(refDoc.createTextNode(xmltvEpnum));

   refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
   refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
   log.debug("extractMissingEpisodeInfo: added field episode-num to {}: {}", progid, xmltvEpnum);

}

private void resumeStopWatch(StopWatch sw)
{
   if(sw.isStopped())
   {
      sw.start();
   }
   else if(sw.isSuspended())
   {
      sw.resume();
   }
}

private void suspendStopWatch(StopWatch sw)
{
   if(!sw.isSuspended())
   {
      sw.suspend();
   }
}

// stops and resets StopWatch.
// Returns the formatted time on the StopWatch before the reset
private String resetStopWatch(StopWatch sw)
{
   if(!sw.isStopped())
   {
      sw.stop();
   }
   String stopTime = formatTime(sw);
   sw.reset();
   return stopTime;
}

private String formatTime(StopWatch sw)
{
   String t = DurationFormatUtils.formatDuration(sw.getTime(), durationFormat);
   return t;
}


private List<Node> getTitleForDay(String day, String chanid, String title, Map<String, Map<ZonedDateTime, Node>> dayChanIndex)
{
   Map<ZonedDateTime, Node> refNodes = getDayChannel(day, chanid, dayChanIndex);
   List<Node> ntitles = refNodes.values().stream()
                    .filter(n -> title.equalsIgnoreCase(nu.getNodeValue(n, "title")))
                    .sorted((n1, n2) -> nu.getAttributeValue(n1, "start").compareTo(nu.getAttributeValue(n2, "start")))
                    .toList();
   return ntitles;
}

@Deprecated
private Optional<XmlTvProgram> findAltProgram(Node refProg, String progid)
{
   resumeStopWatch(swfindalt);

   XmlTvProgram xprog = null;
   String title = nu.getNodeValue(refProg, "title");
   String starttime = nu.getAttributeValue(refProg, "start");
   String day = starttime.substring(0,8);
   String chanid = nu.getAttributeValue(refProg, "channel");
   ZonedDateTime zdt = XmltvParser.XMLTVToZonedDateTime(starttime);
   List<XmlTvProgram> progs;
   progs = this.altStore.getProgrammesForDayChannel(day, chanid);

   // TODO: need to maximise the chances of the titles matching
   // either just look for an exact time match and assume it must be the same program
   // or normalize the titles to reduce the chance of mismatch
   // This doesn't work when there is an exact match for the time but the titles
   // in the two guides are not the same, eg. 'Magnum, PI' vs 'Magnum P.I'
   // Spotted this when comparing the 'slow' and 'fast' versions. epnum info was missing from the
   // 'fast' version. The 'slow' version matches the different titles because it does an explicit
   // check for a program with the exact time, regardless of the title.
   //
   Optional<XmlTvProgram> progtime = progs.stream()
         .filter(p -> p.getStart().isEqual(zdt))
         .findAny();

   if(progtime.isPresent())
   {
      // Assume that exact match for time MUST be the right programm
      xprog = progtime.get();
   }
   else
   {
      // this is where it gets really ugly!!!
      List<XmlTvProgram> xtitles = getXTitleForDay(day, chanid, title);
      List<Node> ntitles = getTitleForDay(day, chanid, title, refDayChanIndex);
      // Occurrence matching only valid if both ref and alt contain same number of occurrences
      if(ntitles.size() == xtitles.size())
      {
         if(xtitles.size() == 1)
         {
            xprog = xtitles.get(0);
         }
         else if(xtitles.size() > 1)
         {
            // Find occurrence of refProg
            int refoccur = -1;
            int i=0;
            for(Node n : ntitles)
            {
               String s = nu.getAttributeValue(n, "start"); //nu.getNodeValue(n, "start");
               if(starttime.equals(s) )
               {
                  refoccur = i;
                  break;
               }
               i++;
            }

            if(refoccur < 0)
            {
               // This should not happen!!
               log.warn("findAltProg: Failed to find reference occurrence for {}", progid);
            }
            else
            {
               xprog = xtitles.get(refoccur);
            }
         }
      }
      else
      {
         // This usually occurs because the data available for the last day of the range is not
         // always complete
         log.debug("findAltProg: found different no. of occurrences of '{}' for {} {}: ref:{} alt:{}",
               title, chanid, day, ntitles.size(), xtitles.size());
      }
   }
   suspendStopWatch(swfindalt);
   return Optional.ofNullable(xprog);
}


private List<XmlTvProgram> getXTitleForDay(String day, String chanid, String title)
{
   List<XmlTvProgram> progs;
   progs = this.altStore.getProgrammesForDayChannel(day, chanid);

   List<XmlTvProgram> xtitles =  progs.stream()
            .filter(p -> p.getTitle().equalsIgnoreCase(title))
            .sorted((t1, t2) -> (t1.getStart().compareTo(t2.getStart())))
            .toList();
   return xtitles;
}

// Processing is faster by using jxmltv but still slows down as more records are processed
// I'm guessing that this is because it takes longer and longer to search for the Nodes for a given day/channel
// So the idea here is to pre-build the day/channel list of programmes and to index the list on the start string

private void buildDayChannelIndex(NodeList progs, Map<String, Map<ZonedDateTime, Node>> dayChanIndex)
{
   int progcnt = progs.getLength();
   StopWatch sw = StopWatch.createStarted();
   for(int i = 0; i <  progcnt; i++)
   {
      Node refProg = progs.item(i);
      String starttime = nu.getAttributeValue(refProg, "start");
      String chanid = nu.getAttributeValue(refProg, "channel");

      String startday = starttime.substring(0,8);
      ZonedDateTime zdtstart = XmltvParser.XMLTVToZonedDateTime(starttime);
      Map<ZonedDateTime, Node> daychanmap = getDayChannel(startday, chanid, dayChanIndex);
      daychanmap.put(zdtstart, refProg);
   }

   log.info("buildDayChannelIndex: build index. Time Elapsed: {}",  DurationFormatUtils.formatDuration(sw.getTime(), durationFormat));
}

private Map<ZonedDateTime, Node> getDayChannel(String startday, String chanid, Map<String, Map<ZonedDateTime, Node>> dayChanIndex)
{
   String key = startday + ":" + chanid;
   Map<ZonedDateTime, Node> refprogs = dayChanIndex.get(key);
   if(refprogs == null)
   {
      refprogs = new HashMap<>();
      dayChanIndex.put(key, refprogs);
   }
   return refprogs;
}


@Deprecated
private void adjustTimes(Node refProg, XmlTvProgram altProg, String progid)
{
   resumeStopWatch(swadjustTimes);
   // Compare the times for ref and alt. To maximise the chance of recording the entire program
   // should take the earliest starttime and the latest endtime.
   String refstart = nu.getAttributeValue(refProg, "start");
   String refend = nu.getAttributeValue(refProg, "stop");

   // Can't rely on the timezone offsets being the same so convert to Date for
   // comparing but use the original string if necessary to change the time.
   // Should round the times to 5mins (down for start, up for end) for compatibility
   // with the timer editor.
   ZonedDateTime refdt = XMLTVutils.getZDateFromXmltv(refstart);
   ZonedDateTime altdt =altProg.getStart(); // XMLTVutils.getZDateFromXmltv(altstart);
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
   altdt = altProg.getStop(); // XMLTVutils.getZDateFromXmltv(altend);

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
   suspendStopWatch(swadjustTimes);
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

@Deprecated
private void copyFields(Node refProg, XmlTvProgram altProg, String[] fieldnames, String progid)
{
   // Not so easy to use an array of field names against a Java object.
   // For now a simple if equals list will have to do. Using reflection is a bit over
   // the top and kind of slow given that this is a performance enhancement!
   // "episode-num", "sub-title", "desc"
   resumeStopWatch(this.swcopyfields);
   for(String fieldname : fieldnames)
   {
      Optional<Node> optrefFld;
//      optrefFld = nu.findNodeByPath(refProg, fieldname);
      optrefFld = nu.getChildByName(refProg, fieldname);
      String fieldval = null;
      if("episode-num".equals(fieldname))
      {
         if( ! optrefFld.isPresent())
         {
            getEpisodenum(altProg).ifPresent(e -> addEpisodeNumToProgram(refProg, e, progid));
         }
      }
      else if("sub-title".equals(fieldname))
      {
         if( ! optrefFld.isPresent())
         {
            fieldval = altProg.getSubTitle();
            if(fieldval != null)
            {
               addSubtitleToProgramNode(refProg, fieldval);
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
   suspendStopWatch(this.swcopyfields);
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
   // By chance I found an XMLTV to Java object model parser which might make creating something which can be searched faster than
   // the XML DOM
   // https://github.com/raydouglass/xmltv-to-mxf/blob/master/pom.xml.
   //
   // Well the conversion to a java model took a while but did result in an improvement in speed however the
   // split timing still showed it getting slower and slower.
   // By using the 'accumulating' stop watches I saw that it was the copyFields method which seemed to be responsible
   // for this slow down. This was very surprising given that it operates on a single node each time so should be
   // consistent in timings. As all it is doing is getting a child Node with a given name from a parent Node I wrote
   // a method which iterated through the children looking for one with a matching name. Obviously wont work if it
   // is not a child node which is required but the xmltv programs have all the relevant details at child level.
   // Use of this new way of getting the child nodes produced a very dramatic speed improvement - from 6mins to 6secs!
   // So the overall speed improvement is from 26mins to 6secs. Not bad!!Saving the planet one tvguide at a time!
   //
   // Not sure whether I'm going to keep the java objects - the code is a bit buggy, ie. unusable without the
   // changes I made. I'm thinking that reverting to the use of xml nodes throughout might still be as fast with the
   // copyFields change in place and reducing the dependency on an external library, which is not really intended to
   // be a library, would be a good thing.
   //
   // The ref nodes have now been indexed by day/channel. The same could be done for the alt node.
   // findAltNode would need to be modified to use the caches and the node versions of copyfields, adjustTimes
   // updated to use the new getChild method. That;s something for a rainy day though, for now I'm using the
   // java objects version
   sc.writeUpdatedXMLTV(result);
}


private Optional<Node> findAltNode(Node refProg, String progid)
{
   Node altProg = null;
   String title = nu.getNodeValue(refProg, "title");
   String starttime = nu.getAttributeValue(refProg, "start");
   String chanid = nu.getAttributeValue(refProg, "channel");
   boolean fuzzyMatch = true;

   String day = starttime.substring(0,8);
   ZonedDateTime zdtStart = XmltvParser.XMLTVToZonedDateTime(starttime);
   Map<ZonedDateTime, Node> altprogs = getDayChannel(day, chanid, altDayChanIndex);

   altProg = altprogs.get(zdtStart);

   if((altProg == null) && fuzzyMatch)
   {
      List<Node> alttitles = getTitleForDay(day, chanid, title, altDayChanIndex);
      List<Node> reftitles = getTitleForDay(day, chanid, title, refDayChanIndex);

      // Occurrence matching only valid if both ref and alt contain same number of occurrences
      if(reftitles.size() == alttitles.size())
      {
         if(alttitles.size() == 1)
         {
            altProg = alttitles.get(0);
         }
         else if(alttitles.size() > 1)
         {
            // Find occurrence of refProg
            int refoccur = -1;
            int i=0;
            for(Node n : reftitles)
            {
               String s = nu.getAttributeValue(n, "start");
               if(starttime.equals(s) ) // Safe to compare strings as looking for the entry which provided starttime
               {
                  refoccur = i;
                  break;
               }
               i++;
            }

            if(refoccur < 0)
            {
               // This should not happen!!
               log.warn("findAltProg: Failed to find reference occurrence for {}", progid);
            }
            else
            {
               altProg = alttitles.get(refoccur);
            }
         }
      }
      else
      {
         // This usually occurs because the data available for the last day of the range is not
         // always complete
         log.debug("findAltProg: found different no. of occurrences of '{}' for {} {}: ref:{} alt:{}",
               title, chanid, day, reftitles.size(), alttitles.size());
      }

   }
   return Optional.ofNullable(altProg);
}

//@Deprecated
private void adjustTimes(Node refProg, Node altProg, String progid)
{
   resumeStopWatch(swadjustTimes);
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
   suspendStopWatch(swadjustTimes);
}


private void copyFields(Node refProg, Node altProg, String[] fieldnames, String progid)
{
   resumeStopWatch(this.swcopyfields);
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
   suspendStopWatch(this.swcopyfields);
}

//10-Sep-2023 Kludge to workaround EPG missing BBC1 programme info.
//It appears that BBC1 programme info is no longer available. This makes it
//difficult to add BBC1 progs on the Dreambox, which only has SD channels, and
//annoying on the Ultimo which has both HD and SD but HD is more likely to cause a
//conflict with existing timers.
//For now the solution will copy the programmes for one channel into another channel.
//Must be done after the filter since the chances are dest channel will be absent from one or both the inputs.
//Need to add a <channel> for the dest channel since there probably wont already be one.
//Remove this. Issue is moot as BBC SD has become unwatchable due to the red stopping banner
//and this was not used anyway as it was easier to map a dummy BBC1HD channel to BBC1SD in the grabbers.
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
//   tvNode.appendChild(newNode); This inserts after the programme block, which might mess something up
   // Ideally new node should go at end of channel block or after the src channel but this is easier!
   tvNode.insertBefore(newNode, srcChanNode);

}
}

}
