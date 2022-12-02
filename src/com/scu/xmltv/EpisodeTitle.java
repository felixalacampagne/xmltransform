package com.scu.xmltv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scu.utils.NodeUtils;

public class EpisodeTitle extends EpisodeNumber
{
   @JsonProperty("title")
   private String eptitle = "";

   @JsonIgnore
   private String epfulltitle = "";

   public EpisodeTitle(String episodenum, String subtitle)
   {
      this(episodenum, subtitle, " ");
   }

   public EpisodeTitle(String episodenum, String subtitle, String separator)
   {
      super(episodenum);
      NodeUtils nu = NodeUtils.getNodeUtils();

      eptitle = nu.sanitizeTitle(subtitle);
      if(eptitle.isEmpty() && !getEpnum().isEmpty())
      {
         eptitle = "Episode " + getEpnum();
      }

      if(getEpinfx().isEmpty())
      {
         separator = "";
      }
      epfulltitle = getEpinfx() + separator + eptitle;
   }

   public String getEptitle()
   {
      return eptitle;
   }

   public String getEpfulltitle()
   {
      return epfulltitle;
   }
}
