package com.scu.jxmltv;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import com.dontocsata.xmltv.XmlTvParseException;

class XmltvParserTest
{

   @Test
   void testParse() throws FileNotFoundException, XmlTvParseException
   {
      String filename = "C:\\Development\\workspace\\xmltransform\\tvguide\\xml\\xmltv_gb.xml";
      XmltvStore store = new XmltvStore();
      
      XmltvParser.parse(filename, store);
      
      assertTrue(store.getChannels().size() > 0, "There should be some channels");
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
   }
}
