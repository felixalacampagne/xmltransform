package com.scu.jxmltv;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dontocsata.xmltv.XmlTVDataSorage;
import com.dontocsata.xmltv.model.XmlTvChannel;
import com.dontocsata.xmltv.model.XmlTvProgram;
import com.scu.xmltv.XMLTVSourceCombiner;

public class XmltvStore implements XmlTVDataSorage
{
   static Logger log = LoggerFactory.getLogger(XmltvStore.class);

   class DayKey
   {
      final String key;
      DayKey(String day, String channel)
      {
         key = day + channel;
      }

      DayKey(ZonedDateTime day, String channel)
      {
         this(String.format("%04d%02d%02d", day.getYear(), day.getMonthValue(), day.getDayOfMonth()), channel);
      }

      DayKey(XmlTvProgram prog)
      {
         this(prog.getStart(), prog.getChannelId());
      }

      public String getKey()
      {
         return key;
      }

      //
      @Override
      public boolean equals(Object o)
      {
         if (this == o)
         {
            return true;
         }

         if((o instanceof DayKey) && (o != null))
            return key.equals( ((DayKey)o).getKey());

         return false;
      }

      @Override
      public int hashCode()
      {
         return key.hashCode();
      }
   }

   private List<XmlTvChannel> channels = new ArrayList<>();
   private List<XmlTvProgram> programmes = new ArrayList<>();
   private Map<DayKey, List<XmlTvProgram>> daymap =  new HashMap<>();

   private static Map.Entry<DayKey, List<XmlTvProgram>> lastdaymap = null;
   
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

     DayKey dk = new DayKey(program);

     List<XmlTvProgram> dayprogs = getDayChannel(dk);
     dayprogs.add(program);
   }

   protected List<XmlTvProgram> getDayChannel(DayKey dk)
   {
      if((lastdaymap != null) && lastdaymap.getKey().equals(dk))
      { 
         return lastdaymap.getValue();
      }
      
      List<XmlTvProgram> dayprogs = daymap.get(dk);
      if(dayprogs == null)
      {
         dayprogs = new ArrayList<>();
         daymap.put(dk, dayprogs);
      }
      lastdaymap = Map.entry(dk, dayprogs);
      return dayprogs;
   }

   @Override
   public Collection<XmlTvProgram> getXmlTvPrograms()
   {
      return programmes;
   }

   // Format for 'day' is 'YYYYMMDD'
   public List<XmlTvProgram> getProgrammesForDayChannel(String day, String channel)
   {
      DayKey dk = new DayKey(day, channel);
      return getDayChannel(dk);
   }
}
