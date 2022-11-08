package com.scu.xmltv;

import com.scu.utils.NodeUtils;

public class EpisodeInfo
{
   private String eptitle = "";
   private String epseason = "";
   private String epnum  = "";
   private String epinfx = "";
   private String epfulltitle = "";

   public EpisodeInfo()
   {
   }

   public EpisodeInfo(String episodenum, String subtitle)
   {
      this(episodenum, subtitle, " ");
   }

   public EpisodeInfo(String episodenum, String subtitle, String separator)
   {
      NodeUtils nu = NodeUtils.getNodeUtils();
      int iepnum = -1;
      int iepseason = -1;

      if(episodenum!=null)
      {
         String [] parts = episodenum.split("[\\./]");
         // If there aren't at least two parts then it is not a valid episodenum
         if((parts != null) && (parts.length > 1))
         {
            iepseason = nu.stringToInt(parts[0]) + 1;
            iepnum =    nu.stringToInt(parts[1]) + 1;
         }
      }

      if(iepnum > 0)
      {
         epnum = String.format("%02d", iepnum);
         if(iepseason < 0)
         {
            iepseason = 0;
         }
         epseason = String.valueOf(iepseason);
         epinfx = String.format("%sx%s", epseason, epnum);
      }

      eptitle = nu.sanitizeTitle(subtitle);
      if(eptitle.isEmpty() && !epnum.isEmpty())
      {
         eptitle = "Episode " + epnum;
      }

      if(epinfx.isEmpty())
      {
         separator = "";
      }
      epfulltitle = epinfx + separator + eptitle;
   }

   public String getEptitle()
   {
      return eptitle;
   }

   public String getEpseason()
   {
      return epseason;
   }

   public String getEpnum()
   {
      return epnum;
   }

   public String getEpinfx()
   {
      return epinfx;
   }

   public String getEpfulltitle()
   {
      return epfulltitle;
   }


}

