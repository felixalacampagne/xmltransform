package com.scu.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.scu.xmltv.EpisodeInfo;


class NodeUtilsTest
{

	@Test
	void testGetEpisodeInfo()
	{
		NodeUtils nu = NodeUtils.getNodeUtils();

		String episodenum = null;
		String subtitle = null;
		EpisodeInfo ei;

		ei = new EpisodeInfo(episodenum, subtitle);

		assertTrue(ei != null);
		assertTrue(ei.getEptitle() != null);
		assertTrue(ei.getEptitle().isEmpty());

		subtitle = "Title for episode";
		episodenum = "19 . 14/99 . ";
		ei = new EpisodeInfo(episodenum, subtitle);

		assertEquals("20x15 ", ei.getEpinfx());
		assertEquals(subtitle, ei.getEptitle());

		subtitle = "";
		episodenum = " 0 . 0/99 . ";
		ei = new EpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.getEpinfx());
		assertEquals("Episode 01", ei.getEptitle());

		subtitle = null;
		episodenum = " 0 . 0/99 . ";
		ei = new EpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.getEpinfx());
		assertEquals("Episode 01", ei.getEptitle());

		subtitle = "Title&for:w.t,f;<is>this?";
		episodenum = " 0 . 0/99 . ";
		ei = new EpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.getEpinfx());
		assertEquals("Title And forwtfisthis", ei.getEptitle());


	}

}
