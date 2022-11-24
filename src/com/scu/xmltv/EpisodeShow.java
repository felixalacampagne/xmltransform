package com.scu.xmltv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scu.utils.NodeUtils;

@JsonPropertyOrder({"show", "season", "number", "title"})
public class EpisodeShow extends EpisodeTitle
{
@JsonProperty("show")
String cleanshow = "";

@JsonProperty("aired")
String showDate = "";

@JsonProperty("recname")
String eventName = "";

String uid = "";


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
      uid = nu.calcDigest(eventName);
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

   public String getUid()
   {
      return uid;
   }

}
