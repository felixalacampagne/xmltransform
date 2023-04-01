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

		zdt1 = zdt2.plusMinutes(5-off);
		System.out.println("2 5mins adjusted up: " + zdt1);
		
		zdt1 = zdt2.minusMinutes(off);
		System.out.println("2 5mins adjusted down: " + zdt1);
		
		xmltvdt = XMLTVutils.getXmltvFromZDate(zdt1);
		System.out.println("2 5mins adjusted down XMLTV: " + xmltvdt);
	}

}
