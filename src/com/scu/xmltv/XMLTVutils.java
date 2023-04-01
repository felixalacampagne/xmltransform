package com.scu.xmltv;

import static com.scu.xmltv.XMLTVutils.getXMLTVDateFormat;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class XMLTVutils
{
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

	public static ZonedDateTime getZDateFromXmltv(String xmltvdatetime)
	{
		ZonedDateTime zdt;
		DateTimeFormatter format = DateTimeFormatter.ofPattern(getXMLTVDateFormat(xmltvdatetime));
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
}
