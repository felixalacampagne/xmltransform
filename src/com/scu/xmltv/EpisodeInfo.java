package com.scu.xmltv;

import com.scu.utils.NodeUtils;

public class EpisodeInfo
{
   private String eptitle = "";
   private String epseason = "";
   private String epnum  = "";
   private String epinfx = "";
   
   public EpisodeInfo()
   {
   }
   
   public EpisodeInfo(String episodenum, String subtitle)
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
         epinfx = String.format("%sx%s ", epseason, epnum);
      }

      eptitle = nu.sanitizeTitle(subtitle);
      if(eptitle.isEmpty() && !epnum.isEmpty())
      {
         eptitle = "Episode " + epnum;
      }      
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
   
   
}

