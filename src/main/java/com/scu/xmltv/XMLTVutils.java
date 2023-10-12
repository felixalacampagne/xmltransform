package com.scu.xmltv;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLTVutils
{
private static Logger log = LoggerFactory.getLogger(XMLTVutils.class);

// This is intended to match something like:
// Short episode title: rest of description bla bla bla blab alblalbladelbllblblba
// Of course, as soon as I implemented this there were a rash of descriptions like:
// Pure description with no subtitle. Somewhere, over the rainbow; they decided to plant a : just for the hell of it.bla bla bla blab alblalbladelbl
// which results in ridiculously long episode sub-titles. No simple way to avoid this.
// It seems that \S matches all punctuation so try to ignore ":" which come after other punctuation.
// Might need to limit the number of characters before the ':' since sub-titles are usually quite short
// (yes there will be exceptions but long texts are not displayed well in the media player UI)
// Could also only look for a subtitle if there is epnum info.
private static final Pattern subtitPattern = Pattern.compile("^(?:\\.\\.\\.\\S.*?: )?([\\w ]*?): ");

   /**
    * SimpleDateFormat is too stupid to realise that not all of a date has been passed in,
    * ie 20060728 instead of 20060728000000 +0000.
    * So this method adjusts the pattern to fit the number of fields which have been supplied.
    * The string must contain an integral number of fields or an exception will still result
    * @param xmltvdatetime
    * @return
    */
   public static String getXMLTVDateFormat(String xmltvdatetime)
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

   public static Date getDateFromXmltv(String xmltvdatetime)
   {
      SimpleDateFormat sdf = new SimpleDateFormat();
      Date dt = null;
      try
      {
         sdf.applyPattern(getXMLTVDateFormat(xmltvdatetime));
         dt = sdf.parse(xmltvdatetime);
      }
      catch(Exception ex) {}

      return dt;
   }


//   private static final DateTimeFormatter[] FORMATS = new DateTimeFormatter[] {
//         DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"),
//         DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC")),
//         DateTimeFormatter.ofPattern("yyyyMMddHHmm"),
//         DateTimeFormatter.ofPattern("yyyyMMddHH"),
//         DateTimeFormatter.ofPattern("yyyyMMdd") };
//   public static ZonedDateTime XMLTVToZonedDateTime(String text) {
//      for (DateTimeFormatter dtf : FORMATS) {
//         try {
//            return ZonedDateTime.parse(text, dtf);
//         } catch (DateTimeParseException e) {
//            // no-op
//         }
//      }
//      throw new IllegalArgumentException("Unparseable: " + text);
//   }


	public static ZonedDateTime getZDateFromXmltv(String xmltvdatetime)
	{
		ZonedDateTime zdt;
		DateTimeFormatter format = DateTimeFormatter.ofPattern(getXMLTVDateFormat(xmltvdatetime));

		// The default timezone for values without a timezone should be UTC. Need to figure out
		// how to do that, eg. does setting the withZone to UTC override a timezone in the pattern
		// DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC")).
		// See above XMLTVToZonedDateTime for an alternative solution....
		zdt = ZonedDateTime.parse(xmltvdatetime, format);
		return zdt;
	}

	public static String getXmltvFromZDate(ZonedDateTime zdt)
	{
		DateTimeFormatter format = DateTimeFormatter.ofPattern(getXMLTVDateFormat(""));
		String xmltv = format.format(zdt);
		return xmltv;
	}

	public static ZonedDateTime getQuantizedDate(ZonedDateTime zdt, int quantum, int direction)
	{
		int mins = zdt.getMinute();


		int off = mins % quantum;
		if(off == 0)
			return zdt;

		ZonedDateTime adj;
		if(direction < 0)
		{
			adj = zdt.minusMinutes(off);
		}
		else
		{
			adj = zdt.plusMinutes(quantum-off);
		}

		return adj;
	}

	public static Optional<String> getSubTitleFromDesc(String desc) {
	   String subtitle = null;
	   Matcher m = subtitPattern.matcher(desc);
	   if(m.find())
	   {
	      subtitle = m.group(1);
	      log.debug("getSubTitleFromDesc: found sub-title '{}' in '{}'", subtitle, desc);
	   }
	   return Optional.ofNullable(subtitle);
	}

}
