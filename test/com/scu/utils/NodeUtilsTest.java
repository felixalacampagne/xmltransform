package com.scu.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.scu.xmltv.EpisodeNumber;
import com.scu.xmltv.EpisodeTitle;
import com.scu.xmltv.XSLTExtensions;

class NodeUtilsTest
{

   @Test
   void testGetEpisodeTitle()
   {
      String episodenum = null;
      String subtitle = null;
      EpisodeTitle ei;

      ei = new EpisodeTitle(episodenum, subtitle);
      assertTrue(ei != null);
      assertTrue(ei.getEptitle() != null);
      assertTrue(ei.getEptitle().isEmpty());

      subtitle = "Title for episode";
      episodenum = "19 . 14/99 . ";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("20x15", ei.getEpinfx());
      assertEquals(subtitle, ei.getEptitle());

      subtitle = "";
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Episode 01", ei.getEptitle());

      subtitle = null;
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Episode 01", ei.getEptitle());

      subtitle = "Title&for:w.t,f;<is>this?";
      episodenum = " 0 . 0/99 . ";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("1x01", ei.getEpinfx());
      assertEquals("Title And forwtfisthis", ei.getEptitle());

      subtitle = "Title&for:w.t,f;<is>this?";
      episodenum = "3 . 114 . ";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("4x115", ei.getEpinfx());
      assertEquals("Title And forwtfisthis", ei.getEptitle());

      subtitle = "Title for episode";
      episodenum = null;
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("", ei.getEpinfx());
      assertEquals("Title for episode", ei.getEptitle());

      subtitle = "Title for episode";
      episodenum = "";
      ei = new EpisodeTitle(episodenum, subtitle);
      assertEquals("", ei.getEpinfx());
      assertEquals("Title for episode", ei.getEptitle());

   }

   @Test
   void missingEpisodeNumberParts()
   {
      String episodenum = null;
      EpisodeNumber ei;

      episodenum = " 1. 15/99 . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("2x16", ei.getEpinfx());

      episodenum = "2.23/99.";
      ei = new EpisodeNumber(episodenum);
      assertEquals("3x24", ei.getEpinfx());

      episodenum = " . 15/99 . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("x16", ei.getEpinfx());

      episodenum = " 2021. /99 . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("2022", ei.getEpinfx());

      episodenum = " . /99 . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = " .  . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = "..";
      ei = new EpisodeNumber(episodenum);
      assertEquals("x00", ei.getEpinfx());

      episodenum = "46 .  . ";
      ei = new EpisodeNumber(episodenum);
      assertEquals("47", ei.getEpinfx());

   }

   @Test
   void xsltExtensionsTest()
   {
      // Should really go in it own class...
      String json;
      json = XSLTExtensions.getEpisodeInfoAsJson("Show Name", "20221115060000 +0000", " 1. 15/99 . ", "Episode Title");
      System.out.println("JSON=" + json);
      assertEquals("{\"show\":\"Show Name\",\"season\":\"2\",\"number\":\"16\",\"title\":\"Episode Title\",\"uid\":\"350e9c44650d42a4f089f7214030af94\",\"aired\":\"22-11-15\",\"recname\":\"Show Name 22-11-15 2x16 Episode Title\"}", json);

      json = XSLTExtensions.getEpisodeInfoAsJson("Star Wars", "20221115060000 +0000", "", "");
      System.out.println("JSON=" + json);
      assertEquals("{\"show\":\"Star Wars\",\"season\":\"\",\"number\":\"\",\"title\":\"\",\"uid\":\"607cf407c1ec1582849e374324e76d48\",\"aired\":\"\",\"recname\":\"Star Wars\"}", json);

   }
}
