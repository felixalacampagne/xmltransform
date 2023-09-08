package com.scu.xmltv;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      
      StringWriter strwrite = new StringWriter();
      srccmb.writeUpdatedXMLTV(strwrite);
      
      log.info("testCombineSource: result:\n{}", strwrite.toString());
   }

}
