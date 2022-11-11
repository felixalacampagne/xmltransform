package com.scu.xmltv;

import com.scu.utils.NodeUtils;

public class EpisodeNumber
{
   private String epseason = "";
   private String epnum  = "";
   private String epinfx = "";
   

   public EpisodeNumber(String episodenum)
   {
   	parseEpisodeNumber(episodenum);
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

	protected void parseEpisodeNumber(String episodenum)
   {
      NodeUtils nu = NodeUtils.getNodeUtils();
      int iepnum = -1;
      int iepseason = -1;
      
   	if(!((episodenum==null) || episodenum.isEmpty()))
      {
         String [] parts = episodenum.split("[\\./]");

         if((parts != null))
         {
            if(parts.length > 0)
            {
               iepseason = nu.stringToInt(parts[0]) + 1;
            }
            if(parts.length > 1)
            {
               iepnum =    nu.stringToInt(parts[1]) + 1;
            }

            // Historically template "extract_xmltvns" would put 'Ep.' when no
            // valid season was found and "00" if no valid episode number was found
            // based on the assumption that some sort of episode number must be intended
            // by the field.
            // It will be a bit different now.
            //   S    - season, number not present
            //    xNN - no season, number present
            //   SxNN - season, number present
            //    x00 - no season, number not present
            String sep = "";
            if(iepseason > 0)
            {
               epseason = String.valueOf(iepseason);
            }

            if(iepnum > 0)
            {
               epnum = String.format("%02d", iepnum);
            }

            if((iepnum < 0) && (iepseason < 0))
            {
               epnum = "00";
            }

            if(!epnum.isEmpty())
            {
               sep = "x";
            }

            epinfx = String.format("%s%s%s", epseason, sep, epnum);
         }
      }   	
   }
   

}
