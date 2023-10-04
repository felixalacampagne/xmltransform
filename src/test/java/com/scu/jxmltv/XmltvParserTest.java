package com.scu.jxmltv;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dontocsata.xmltv.XmlTvParseException;
import com.dontocsata.xmltv.model.XmlTvChannel;
import com.dontocsata.xmltv.model.XmlTvProgram;

class XmltvParserTest
{

   @Test
   void testParse() throws FileNotFoundException, XmlTvParseException
   {
      String filename = "C:\\Development\\workspace\\xmltransform\\tvguide\\xml\\xmltv_gb.xml";
      XmltvStore store = new XmltvStore();
      
      XmltvParser.parse(filename, store);
      
      assertTrue(store.getChannels().size() > 0, "There should be some channels");
      
      
      List<XmlTvProgram> progs;
      progs = store.getProgrammesForDayChannel("20230929", "TVG.C4.HD");
      assertNotNull(progs);
   }

   @Test
   void testStoreIndexing()
   {
      // test the creation of the store indexes
      XmltvStore store = new XmltvStore();

      store.save(createProg("CHN1", "PROG_001", "20230610060000 +0200"));
      store.save(createProg("CHN1", "PROG_002", "20230610070000 +0200"));
      store.save(createProg("CHN1", "PROG_003", "20230610080000 +0200"));
      store.save(createProg("CHN2", "PROG_001", "20230610070000 +0200"));
      store.save(createProg("CHN2", "PROG_002", "20230610080000 +0200"));
      store.save(createProg("CHN2", "PROG_003", "20230610090000 +0200"));
      store.save(createProg("CHN2", "PROG_004", "20230610100000 +0200"));
      store.save(createProg("CHN1", "PROG_011", "20230710020000 +0200"));
      store.save(createProg("CHN1", "PROG_012", "20230710030000 +0200"));
      store.save(createProg("CHN1", "PROG_013", "20230710040000 +0200"));
      store.save(createProg("CHN2", "PROG_011", "20230710050000 +0200"));
      store.save(createProg("CHN2", "PROG_012", "20230710060000 +0200"));
      store.save(createProg("CHN3", "PROG_011", "20230710070000 +0200"));

      List<XmlTvProgram> progs;

      progs = store.getProgrammesForDayChannel("20230610", "CHN2");
      assertTrue(4 == progs.size());
      
      progs = store.getProgrammesForDayChannel("20230610", "CHN2");
   }

   private XmlTvProgram createProg(String channel, String programme, String start)
   {
   
      XmlTvProgram prog = new XmlTvProgram();
      prog.setChannelId(channel);
      prog.setTitle(programme);
      prog.setStart(parseZonedDateTime(start));
      return prog;
   }
   
   // RIpped from xmltv-to-mxf
   private static final DateTimeFormatter[] FORMATS = new DateTimeFormatter[] {
         DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"),
         DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC")),
         DateTimeFormatter.ofPattern("yyyyMMddHHmm"), DateTimeFormatter.ofPattern("yyyyMMddHH"),
         DateTimeFormatter.ofPattern("yyyyMMdd") };
   
   private ZonedDateTime parseZonedDateTime(String text) {
      for (DateTimeFormatter dtf : FORMATS) {
         try {
            return ZonedDateTime.parse(text, dtf);
         } catch (DateTimeParseException e) {
            // no-op
         }
      }
      throw new IllegalArgumentException("Unparseable: " + text);
   }   
}
