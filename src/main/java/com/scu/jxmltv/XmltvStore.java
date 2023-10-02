package com.scu.jxmltv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dontocsata.xmltv.XmlTVDataSorage;
import com.dontocsata.xmltv.model.XmlTvChannel;
import com.dontocsata.xmltv.model.XmlTvProgram;

public class XmltvStore implements XmlTVDataSorage
{
   private List<XmlTvChannel> channels = new ArrayList<>();
   private List<XmlTvProgram> programmes = new ArrayList<>();
   
   @Override
   public void save(XmlTvChannel channel)
   {
      channels.add(channel);
      
   }

   // TODO Remove this from interface?
   @Override
   public XmlTvChannel getChannel(String id)
   {
      return null;
   }

   @Override
   public Collection<XmlTvChannel> getChannels()
   {
      return channels;
   }

   @Override
   public void save(XmlTvProgram program)
   {
     programmes.add(program);
   }

   @Override
   public Collection<XmlTvProgram> getXmlTvPrograms()
   {
      // TODO Auto-generated method stub
      return programmes;
   }

}
