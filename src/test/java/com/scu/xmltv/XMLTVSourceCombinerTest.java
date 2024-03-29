package com.scu.xmltv;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// NB. These are manual tests intended to see if the functionality of the XML apis etc.
// is actually doing what I think it is supposed to and to assist
// in getting it to do what I need it to. The results are intended for manual inspection
class XMLTVSourceCombinerTest
{
   Logger log = LoggerFactory.getLogger(this.getClass().getName());


   @Test
   void testCombineSource() throws Exception
   {
      // getFile returns the fullpathname of the file prefixed with '/' which appears to be
      // ignored when opening the file.
      String ref = this.getClass().getClassLoader().getResource("xmltv_epg.xml").getFile();
      String alt = this.getClass().getClassLoader().getResource("xmltv_gb.xml").getFile();
      XMLTVSourceCombiner srccmb = new XMLTVSourceCombiner(ref, alt);
      srccmb.combineSource("episode-num");

      srccmb.writeUpdatedXMLTV("TestResult-combine.xml");

      StringWriter strwrite = new StringWriter();
      srccmb.writeUpdatedXMLTV(strwrite);
      log.info("testCombineSource: result:\n{}", strwrite.toString());
   }

   @Test
   void testCombineSourceDesc() throws Exception
   {
      // getFile returns the fullpathname of the file prefixed with '/' which appears to be
      // ignored when opening the file.
      String ref = this.getClass().getClassLoader().getResource("xmltv_epg.xml").getFile();
      String alt = this.getClass().getClassLoader().getResource("xmltv_gb.xml").getFile();
      XMLTVSourceCombiner srccmb = new XMLTVSourceCombiner(ref, alt);
      srccmb.combineSource("desc");

      srccmb.writeUpdatedXMLTV("TestResult-combine.xml");

      StringWriter strwrite = new StringWriter();
      srccmb.writeUpdatedXMLTV(strwrite);
      log.info("testCombineSource: result:\n{}", strwrite.toString());
   }

   @Test
   void testFilter() throws Exception
   {
      // getFile returns the fullpathname of the file prefixed with '/' which appears to be
      // ignored when opening the file.
      String ref = this.getClass().getClassLoader().getResource("xmltv_epg-filter.xml").getFile();
      String alt = this.getClass().getClassLoader().getResource("xmltv_gb-filter.xml").getFile();
      XMLTVSourceCombiner srccmb = new XMLTVSourceCombiner(ref, alt, "XTVGRABPY.*");  // "\\d+\\.tvguide\\.co\\.uk");
      srccmb.filterProgrammes();

      srccmb.writeUpdatedXMLTV("TestResult-filter.xml");

      StringWriter strwrite = new StringWriter();
      srccmb.writeUpdatedXMLTV(strwrite);
      log.info("testFilter: result:\n{}", strwrite.toString());
   }
}
