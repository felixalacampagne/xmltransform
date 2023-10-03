package com.scu.jxmltv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.dontocsata.xmltv.XmlTVDataSorage;
import com.dontocsata.xmltv.XmlTv;
import com.dontocsata.xmltv.XmlTvParseException;


public class XmltvParser
{

   static public void parse(String filename, XmlTVDataSorage storage) throws FileNotFoundException, XmlTvParseException
   {
      File xmlTvFile = new File(filename);
      InputStream inStream = new FileInputStream(xmlTvFile);
      InputStream xmlTvStream = new BufferedInputStream(inStream);
      
      XmlTv xmltv = new XmlTv(xmlTvStream, storage);  
      xmltv.parse();
   }
}
