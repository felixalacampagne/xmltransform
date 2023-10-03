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

}
