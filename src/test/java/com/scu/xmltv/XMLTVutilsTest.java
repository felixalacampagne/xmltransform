package com.scu.xmltv;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class XMLTVutilsTest
{

	@Test
	void testgetZDateFromXmltv()
	{
		ZonedDateTime zdt1;
		ZonedDateTime zdt2;
		String xmltvdt;
		
		xmltvdt = "20230327093000 +0200";
		
		zdt1 = XMLTVutils.getZDateFromXmltv(xmltvdt);
		System.out.println("XMLTV:" + xmltvdt + " ZDT1:" + zdt1);
		
		
		xmltvdt = "20230327093400 +0200";
		zdt2 = XMLTVutils.getZDateFromXmltv(xmltvdt);
		System.out.println("XMLTV:" + xmltvdt + " ZDT2:" + zdt2);
		
		if(zdt1.compareTo(zdt2) < 0)
		{
			System.out.println("1 is before 2");
		}
		
		int mins = zdt2.getMinute(); 
		System.out.println("2 minutes: " + mins);
		
		int off = mins % 5;

		
		// This will need to be 0 aware!!
		zdt1 = zdt2.plusMinutes(5-off);
		System.out.println("2 5mins adjusted up: " + zdt1);
		
		zdt1 = zdt2.minusMinutes(off);
		System.out.println("2 5mins adjusted down: " + zdt1);
		
		xmltvdt = XMLTVutils.getXmltvFromZDate(zdt1);
		System.out.println("2 5mins adjusted down XMLTV: " + xmltvdt);
		assertEquals("20230327093000 +0200", xmltvdt.toString());
      		
		off = 0;
		zdt1 = zdt2.plusMinutes(off);
		System.out.println("2 5mins adjusted up 0 off: " + zdt1);
		
		
		zdt1 = zdt2.minusMinutes(off);
		System.out.println("2 5mins adjusted down 0 off: " + zdt1);
		
		// DST change is 02:00->03:00 on 26 Mar 2023. Deducting 20 mins from
		// 03:10(+02:00) should give 01:50(+01:00) but instead give 02:50(+02:00).
		// Not sure how this would be handled by enigma but both times should
		// give the same GMT unix time... in theory!
      xmltvdt = "20230326031000 +0200";
      zdt2 = XMLTVutils.getZDateFromXmltv(xmltvdt);
      System.out.println("XMLTV:" + xmltvdt + " ZDT2:" + zdt2);
      off = 20;
      zdt1 = zdt2.minusMinutes(off);
      System.out.println("20 mins back over DST change: " + zdt1);
     	
      long unx1 = zdt1.toEpochSecond();
      
      xmltvdt = "20230326015000 +0100";
      zdt2 = XMLTVutils.getZDateFromXmltv(xmltvdt); 
      long unx2 = zdt2.toEpochSecond();
      assertEquals(unx1, unx2, "Check '015000 +0100' gives same Unix time as '031000 +0200' - 20mins");
	}

}
