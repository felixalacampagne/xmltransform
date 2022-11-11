package com.scu.xmltv;

import com.scu.utils.NodeUtils;

public class EpisodeShow extends EpisodeTitle
{
String showDate = "";
String eventName = "";
String cleanshow = "";

	public EpisodeShow(String show, String start, String episodenum, String subtitle)
	{
		super(episodenum, subtitle);
		
      NodeUtils nu = NodeUtils.getNodeUtils();
      cleanshow = nu.sanitizeTitle(show);

      if(!getEpfulltitle().isEmpty())
      {
      	showDate = XSLTExtensions.formatDate(start, "yy-MM-dd");
      	eventName = String.format("%s %s %s", cleanshow, showDate, getEpfulltitle());
      }
      else
      {
      	eventName = cleanshow;
      }
	}
	
	public String getShowDate()
	{
		return showDate;
	}
	
	public String getEventName()
	{
		return eventName;
	}
	
	public String getCleanshow()
	{
		return cleanshow;
	}

}
