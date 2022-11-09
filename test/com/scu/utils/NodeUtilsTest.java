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

      assertEquals("20x15", ei.getEpinfx());
      assertEquals(subtitle, ei.getEptitle());

      subtitle = "";
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeInfo(episodenum, subtitle);

      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Episode 01", ei.getEptitle());

      subtitle = null;
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeInfo(episodenum, subtitle);

      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Episode 01", ei.getEptitle());

      subtitle = "Title&for:w.t,f;<is>this?";
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeInfo(episodenum, subtitle);

      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Title And forwtfisthis", ei.getEptitle());

      subtitle = "Title&for:w.t,f;<is>this?";
      episodenum = "3 . 114 . ";
      ei = new EpisodeInfo(episodenum, subtitle);

      assertEquals("4x115", ei.getEpinfx());
      assertEquals("Title And forwtfisthis", ei.getEptitle());

   }

   @Test
   void missingEpisodeNumberParts()
   {
      String episodenum = null;
      EpisodeInfo ei;

      episodenum = " 1. 15/99 . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("2x16", ei.getEpinfx());

      episodenum = "2.23/99.";
      ei = new EpisodeInfo(episodenum);
      assertEquals("3x24", ei.getEpinfx());

      episodenum = " . 15/99 . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("x16", ei.getEpinfx());

      episodenum = " 2021. /99 . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("2022", ei.getEpinfx());

      episodenum = " . /99 . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = " .  . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = "..";
      ei = new EpisodeInfo(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = "46 .  . ";
      ei = new EpisodeInfo(episodenum);
      assertEquals("47", ei.getEpinfx());

   }
}
