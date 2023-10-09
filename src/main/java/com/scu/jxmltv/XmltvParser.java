package com.scu.jxmltv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.dontocsata.xmltv.XmlTVDataSorage;
import com.dontocsata.xmltv.XmlTv;
import com.dontocsata.xmltv.XmlTvParseException;


public class XmltvParser
{
   private static final DateTimeFormatter[] FORMATS = new DateTimeFormatter[] {
         DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"),
         DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC")),
         DateTimeFormatter.ofPattern("yyyyMMddHHmm"), DateTimeFormatter.ofPattern("yyyyMMddHH"),
         DateTimeFormatter.ofPattern("yyyyMMdd") };

   static public void parse(String filename, XmlTVDataSorage storage) throws FileNotFoundException, XmlTvParseException
   {
      File xmlTvFile = new File(filename);
      parse(xmlTvFile, storage);
   }
   static public void parse(File xmlTvFile, XmlTVDataSorage storage) throws FileNotFoundException, XmlTvParseException
   {

      InputStream inStream = new FileInputStream(xmlTvFile);
      InputStream xmlTvStream = new BufferedInputStream(inStream);

      XmlTv xmltv = new XmlTv(xmlTvStream, storage);
      xmltv.parse();
   }


   public static ZonedDateTime XMLTVToZonedDateTime(String text) {
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
