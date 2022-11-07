package com.scu.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.scu.utils.NodeUtils.EpisodeInfo;

class NodeUtilsTest
{

	@Test
	void testGetEpisodeInfo()
	{
		NodeUtils nu = NodeUtils.getNodeUtils();

		String episodenum = null;
		String subtitle = null;
		EpisodeInfo ei;

		ei = nu.getEpisodeInfo(episodenum, subtitle);

		assertTrue(ei != null);
		assertTrue(ei.eptitle != null);
		assertTrue(ei.eptitle.isEmpty());

		subtitle = "Title for episode";
		episodenum = "19 . 14/99 . ";
		ei = nu.getEpisodeInfo(episodenum, subtitle);

		assertEquals("20x15 ", ei.epinfx);
		assertEquals(subtitle, ei.eptitle);

		subtitle = "";
		episodenum = " 0 . 0/99 . ";
		ei = nu.getEpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.epinfx);
		assertEquals("Episode 01", ei.eptitle);

		subtitle = null;
		episodenum = " 0 . 0/99 . ";
		ei = nu.getEpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.epinfx);
		assertEquals("Episode 01", ei.eptitle);

		subtitle = "Title&for:w.t,f;<is>this?";
		episodenum = " 0 . 0/99 . ";
		ei = nu.getEpisodeInfo(episodenum, subtitle);

		assertEquals("1x01 ", ei.epinfx);
		assertEquals("Title And forwtfisthis", ei.eptitle);


	}

}
