package com.scu.xmltv;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scu.utils.NodeUtils;
import com.scu.utils.Utils;

@JsonPropertyOrder({"show", "season", "number", "title"})
public class EpisodeShow extends EpisodeTitle
{
@JsonProperty("show")
String cleanshow = "";

@JsonProperty("aired")
String showDate = "";

@JsonProperty("recname")
String eventName = "";

String uid = "";

   public EpisodeShow(String show, String start, String episodenum, String subtitle)
   {
      super(episodenum, subtitle);

      NodeUtils nu = NodeUtils.getNodeUtils();
      Date date = XMLTVutils.getDateFromXmltv(start);
      cleanshow = nu.sanitizeTitle(show);
      showDate = XSLTExtensions.formatDate(date, "yy-MM-dd");
      eventName = cleanshow;

      // Decided that anything that is missing an episode title and episode number will
      // be handled as a Film. The automated file handling relies on filenames (eventname)
      // with a consistent format and the bare show name causes problems, not least
      // being the creation of many directories containing just one item.
      // The other non-series category is 'Documentary' but there is no way to
      // automatically distinguish between the two types and it's rare that I
      // want to process documentaries for later viewing.
      //
      // Not sure about the fake episode number for the Films.
      // Don't want to persist a counter - this is used from a webserver and from the command line
      // and I couldbn't be bothered to figure out 'safe' shared file locations.
      // I thought <year>x<dayofyear> would be suitable but now realise
      // that will result in the films being in sub-lists based on the year
      // they were recorded, which is pretty pointless. Maybe I can just use the
      // number of years since now as a suffix to the dayofyear, 9 years before more
      // than 4 digits are required, and then just wrap around.
      // Actually better to just use the number of days since a reference date,
      // 4 digits gives 9999 days = 27 years!!

      boolean film = (Utils.safeIsEmpty(episodenum) && Utils.safeIsEmpty(subtitle));
      if(!getEpfulltitle().isEmpty())
      {
         eventName = String.format("%s %s %s", cleanshow, showDate, getEpfulltitle());
      }
      else if(film)
      {
         eventName = String.format("Film %s 1x%s %s", showDate, getFilmNumber(date), cleanshow);
      }
      uid = nu.calcDigest(eventName);
   }

   public String getShowDate()
   {
      return showDate;
   }

   public String getEventName()
   {
      return eventName;
   }

   public String getCleanshow()
   {
      return cleanshow;
   }

   public String getUid()
   {
      return uid;
   }

   private String getFilmNumber(Date date)
   {
      LocalDate filmdate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      LocalDate refdate = LocalDate.of(2022,06,10);
      long diff = ChronoUnit.DAYS.between(refdate, filmdate) % 10000;
      return String.format("%04d", diff);
   }
}
