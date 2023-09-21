package com.scu.utils;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.scu.xmltv.EpisodeNumber;
import com.scu.xmltv.EpisodeShow;
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

      subtitle = "Title#&for:w.t,f;<is>this?";
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
   void filmTitles()
   {
      String recname = null;
      EpisodeShow es;

      es = new EpisodeShow("Wonderful Movie", "20221224060000 +0000", "", "");
      recname = es.getEventName();
      // assertEquals("Film 22-12-24 1x0197 Wonderful Movie", recname);
      assertEquals("Wonderful Movie 22-12-24 1x99 Episode 99", recname);
 
      es = new EpisodeShow("What A Wonderful Year", "20230610090000 +0000", null, "");
      recname = es.getEventName();
//      assertEquals("Film 23-06-10 1x0365 What A Wonderful Year", recname);
      assertEquals("What A Wonderful Year 23-06-10 1x99 Episode 99", recname);


      es = new EpisodeShow("A Decade In The Life", "20320610090000 +0000", "", null);
      recname = es.getEventName();
//      assertEquals("Film 32-06-10 1x3653 A Decade In The Life", recname);
      assertEquals("A Decade In The Life 32-06-10 1x99 Episode 99", recname);
   }

   @Test
   void xsltExtensionsTest()
   {
      String json;
      json = XSLTExtensions.getEpisodeInfoAsJson("Show Name", "20221115060000 +0000", " 1. 15/99 . ", "Episode Title");
      System.out.println("JSON=" + json);
      assertEquals("{\"show\":\"Show Name\",\"season\":\"2\",\"number\":\"16\",\"title\":\"Episode Title\",\"uid\":\"350e9c44650d42a4f089f7214030af94\",\"aired\":\"22-11-15\",\"recname\":\"Show Name 22-11-15 2x16 Episode Title\"}", json);

      json = XSLTExtensions.getEpisodeInfoAsJson("Star Wars", "20221115060000 +0000", "", "");
      System.out.println("JSON=" + json);
//      assertEquals("{\"show\":\"Star Wars\",\"season\":\"\",\"number\":\"\",\"title\":\"\",\"uid\":\"f1d32adeacbf1870b876db01db65ea64\",\"aired\":\"22-11-15\",\"recname\":\"Film 22-11-15 1x0158 Star Wars\"}", json);
      assertEquals("{\"show\":\"Star Wars\",\"season\":\"\",\"number\":\"\",\"title\":\"\",\"uid\":\"bbc3cdeefddc9767ea40621dd15f754f\",\"aired\":\"22-11-15\",\"recname\":\"Star Wars 22-11-15 1x99 Episode 99\"}", json);

   }
}
