<?xml version="1.0"  encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:scu="//com.scu.xmltv.XSLTExtensions"
xmlns:loc="local.values"
version="1.0">
<!--
06 Nov 2022 Modified Java code to perform episode details extraction for consistency. 
            It only supports the xmltv_ns episode numbering scheme. Support for older number schemes has been removed.
05 Nov 2022 Added NFO generation for favourites as a replacement for the DB eit files which seems
            to have stopped appearing for most programmes.
13 Oct 2018 Implemented "new series starting" page.
 -->
<xsl:output method="html" version="4.0"/>
<xsl:preserve-space elements="*"/>
<xsl:param name="FAVFILE" />
<xsl:param name="OUTPATH" />
<xsl:variable name="DRMBOXSRV">dm7025</xsl:variable>
<xsl:variable name="VUPUSRV">vuultimo</xsl:variable>
<xsl:variable name="FAVCRIT" select="document($FAVFILE)/node()" />
<xsl:variable name="NEWSERIESCRIT_RTF">
   <CRITS>
      <CRIT>.*=1x01</CRIT>
   </CRITS>
</xsl:variable>
<xsl:variable name="NEWSERIESCRIT" select="scu:convertRTFtoNode($NEWSERIESCRIT_RTF,'//CRITS')" />

<!-- New series with these categories are ignored.
     Had to use the pattern like this as the scu:isMatch extension does not accept
     a simple string pattern - something I should update, if I can find the source!!
     Could probably put each text as a separate CRIT
-->
<xsl:variable name="NEWSERIESEXCL_RTF">
   <CRITS>
      <CRIT>Popular Culture|News|Animated|Game Show|Quiz|Puppets|Cartoons|Cooking|Medicine|Health|Advertisement|Shopping|Challenge|Reality Show|Talk|History|Property|Children|Variety|Documentary</CRIT>
   </CRITS>
</xsl:variable>
<xsl:variable name="NEWSERIESEXCL" select="scu:convertRTFtoNode($NEWSERIESEXCL_RTF,'//CRITS')" />
<!-- Aim to decouple the XMLTV ids and the db and ss refs so I can just plug in the
     lists when they are generated from whatever source I find them from. More relevant for
     the db
  -->
<xsl:variable name="DBENT_RTF">
<DBENTS_REF>
<dbent><dbref>1:0:1:189D:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC One Lon</dbname></dbent>
<dbent><dbref>1:0:1:189E:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC Two</dbname></dbent>
<dbent><dbref>1:0:1:286F:800:2:11A0000:0:0:0:</dbref><dbname>BBC Three</dbname></dbent>
<dbent><dbref>1:0:1:18AC:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC Four</dbname></dbent>
<dbent><dbref>1:0:1:27A6:805:2:11A0000:0:0:0:</dbref><dbname>ITV</dbname></dbent>
<dbent><dbref>1:0:1:23FC:7F9:2:11A0000:0:0:0:</dbref><dbname>Channel 4</dbname></dbent>
<dbent><dbref>1:0:1:1E15:809:2:11A0000:0:0:0:</dbref><dbname>Channel 5</dbname></dbent>
<dbent><dbref>1:0:1:27DA:806:2:11A0000:0:0:0:</dbref><dbname>ITV2</dbname></dbent>
<dbent><dbref>1:0:1:27B3:805:2:11A0000:0:0:0:</dbref><dbname>ITV3</dbname></dbent>
<dbent><dbref>1:0:1:27B4:805:2:11A0000:0:0:0:</dbref><dbname>ITV4</dbname></dbent>
<dbent><dbref>1:0:1:2094:808:2:11A0000:0:0:0:</dbref><dbname>More4</dbname></dbent>
<dbent><dbref>1:0:1:2419:7F9:2:11A0000:0:0:0:</dbref><dbname>E4</dbname></dbent>
<dbent><dbref>1:0:1:5302:814:2:11A0000:0:0:0:</dbref><dbname>4seven</dbname></dbent>
<dbent><dbref>1:0:1:2404:7F9:2:11A0000:0:0:0:</dbref><dbname>Film4</dbname></dbent>
<dbent><dbref>1:0:1:1E1E:809:2:11A0000:0:0:0:</dbref><dbname>5 USA</dbname></dbent>
<dbent><dbref>1:0:1:1E23:809:2:11A0000:0:0:0:</dbref><dbname>5STAR</dbname></dbent>
<dbent><dbref>1:0:1:280F:806:2:11A0000:0:0:0:</dbref><dbname>ITV+1</dbname></dbent>
<dbent><dbref>1:0:1:27B5:805:2:11A0000:0:0:0:</dbref><dbname>ITV2+1</dbname></dbent>
<dbent><dbref>1:0:1:2815:806:2:11A0000:0:0:0:</dbref><dbname>ITV3+1</dbname></dbent>
<dbent><dbref>1:0:1:2805:806:2:11A0000:0:0:0:</dbref><dbname>ITV4+1</dbname></dbent>
<dbent><dbref>1:0:1:2077:808:2:11A0000:0:0:0:</dbref><dbname>Channel 4+1</dbname></dbent>
<dbent><dbref>1:0:1:240E:7F9:2:11A0000:0:0:0:</dbref><dbname>More4+1</dbname></dbent>
<dbent><dbref>1:0:1:206C:808:2:11A0000:0:0:0:</dbref><dbname>E4+1</dbname></dbent>
<dbent><dbref>1:0:1:2095:808:2:11A0000:0:0:0:</dbref><dbname>Film4+1</dbname></dbent>
<dbent><dbref>1:0:1:1E28:809:2:11A0000:0:0:0:</dbref><dbname>Channel 5+1</dbname></dbent>
<dbent><dbref>1:0:1:1E1F:809:2:11A0000:0:0:0:</dbref><dbname>5USA+1</dbname></dbent>
<dbent><dbref>1:0:1:1E24:809:2:11A0000:0:0:0:</dbref><dbname>5SELECT</dbname></dbent>
<dbent><dbref>1:0:1:1E25:809:2:11A0000:0:0:0:</dbref><dbname>5STAR+1</dbname></dbent>
<dbent><dbref>1:0:1:27F9:806:2:11A0000:0:0:0:</dbref><dbname>ITVBe</dbname></dbent>
<dbent><dbref>1:0:1:279B:805:2:11A0000:0:0:0:</dbref><dbname>ITVBe+1</dbname></dbent>
</DBENTS_REF>
</xsl:variable>
<xsl:variable name="VUUENT_RTF">
<DBENTS_REF>
<dbent><dbref>1:0:19:1B1D:802:2:11A0000:0:0:0:</dbref><dbname>BBC One HD</dbname></dbent>
<dbent><dbref>1:0:19:1B1C:802:2:11A0000:0:0:0:</dbref><dbname>BBC Two HD</dbname></dbent>
<dbent><dbref>1:0:19:1B27:802:2:11A0000:0:0:0:</dbref><dbname>BBC Three HD</dbname></dbent>
<dbent><dbref>1:0:19:5230:812:2:11A0000:0:0:0:</dbref><dbname>ITV HD</dbname></dbent>
<dbent><dbref>1:0:19:52D0:814:2:11A0000:0:0:0:</dbref><dbname>Channel 4 HD</dbname></dbent>
<dbent><dbref>1:0:19:1E46:809:2:11A0000:0:0:0:</dbref><dbname>Channel 5 HD</dbname></dbent>
<dbent><dbref>1:0:19:22E3:80D:2:11A0000:0:0:0:</dbref><dbname>BBC Four HD</dbname></dbent>
<dbent><dbref>1:0:1:27DA:806:2:11A0000:0:0:0:</dbref><dbname>ITV2</dbname></dbent>
<dbent><dbref>1:0:1:27B3:805:2:11A0000:0:0:0:</dbref><dbname>ITV3</dbname></dbent>
<dbent><dbref>1:0:1:27B4:805:2:11A0000:0:0:0:</dbref><dbname>ITV4</dbname></dbent>
<dbent><dbref>1:0:1:2419:7F9:2:11A0000:0:0:0:</dbref><dbname>E4</dbname></dbent>
<dbent><dbref>1:0:1:2094:808:2:11A0000:0:0:0:</dbref><dbname>More4</dbname></dbent>
<dbent><dbref>1:0:1:5302:814:2:11A0000:0:0:0:</dbref><dbname>4seven</dbname></dbent>
<dbent><dbref>1:0:1:2404:7F9:2:11A0000:0:0:0:</dbref><dbname>Film4</dbname></dbent>
<dbent><dbref>1:0:1:1E1E:809:2:11A0000:0:0:0:</dbref><dbname>5 USA</dbname></dbent>
<dbent><dbref>1:0:1:1E23:809:2:11A0000:0:0:0:</dbref><dbname>5STAR</dbname></dbent>
<dbent><dbref>1:0:1:1E24:809:2:11A0000:0:0:0:</dbref><dbname>5SELECT</dbname></dbent>
<dbent><dbref>1:0:1:18F6:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC RB 1</dbname></dbent>
<dbent><dbref>1:0:1:27F9:806:2:11A0000:0:0:0:</dbref><dbname>ITVBe</dbname></dbent>
<dbent><dbref>1:0:1:1146:404:1:C00000:0:0:0:</dbref><dbname>CNN Int.</dbname></dbent>
<dbent><dbref>1:0:19:5221:C99:3:EB0000:0:0:0:</dbref><dbname>een HD</dbname></dbent>
<dbent><dbref>1:0:19:5226:C99:3:EB0000:0:0:0:</dbref><dbname>Canvas HD</dbname></dbent>
<dbent><dbref>1:0:19:1B76:C88:3:EB0000:0:0:0:</dbref><dbname>VTM HD</dbname></dbent>
<dbent><dbref>1:0:19:1B91:C88:3:EB0000:0:0:0:</dbref><dbname>Play4</dbname></dbent>
<dbent><dbref>1:0:16:5286:C96:3:EB0000:0:0:0:</dbref><dbname>Play5</dbname></dbent>
<dbent><dbref>1:0:16:1B95:C88:3:EB0000:0:0:0:</dbref><dbname>Play6</dbname></dbent>
<dbent><dbref>1:0:16:5287:C96:3:EB0000:0:0:0:</dbref><dbname>Play7</dbname></dbent>
<dbent><dbref>1:0:19:1B90:C88:3:EB0000:0:0:0:</dbref><dbname>VTM 2 HD</dbname></dbent>
<dbent><dbref>1:0:16:5280:C96:3:EB0000:0:0:0:</dbref><dbname>VTM 3</dbname></dbent>
<dbent><dbref>1:0:16:5282:C96:3:EB0000:0:0:0:</dbref><dbname>VTM 4</dbname></dbent>
<dbent><dbref>1:0:19:5225:C99:3:EB0000:0:0:0:</dbref><dbname>NPO1 HD</dbname></dbent>
<dbent><dbref>1:0:19:17C0:C82:3:EB0000:0:0:0:</dbref><dbname>NPO2 HD</dbname></dbent>
<dbent><dbref>1:0:19:5230:C99:3:EB0000:0:0:0:</dbref><dbname>NPO3 HD</dbname></dbent>
<dbent><dbref>1:0:19:283E:3FB:1:C00000:0:0:0:</dbref><dbname>arte HD</dbname></dbent>
<dbent><dbref>1:0:19:51E5:C96:3:EB0000:0:0:0:</dbref><dbname>NGC HD</dbname></dbent>
<dbent><dbref>1:0:1:189D:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC One Lon</dbname></dbent>
<dbent><dbref>1:0:1:189E:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC Two</dbname></dbent>
<dbent><dbref>1:0:1:286F:800:2:11A0000:0:0:0:</dbref><dbname>BBC Three</dbname></dbent>
<dbent><dbref>1:0:1:18AC:7FD:2:11A0000:0:0:0:</dbref><dbname>BBC Four</dbname></dbent>
<dbent><dbref>1:0:1:2789:805:2:11A0000:0:0:0:</dbref><dbname>ITV</dbname></dbent>
<dbent><dbref>1:0:1:280F:806:2:11A0000:0:0:0:</dbref><dbname>ITV+1</dbname></dbent>
<dbent><dbref>1:0:1:27B5:805:2:11A0000:0:0:0:</dbref><dbname>ITV2+1</dbname></dbent>
<dbent><dbref>1:0:1:2815:806:2:11A0000:0:0:0:</dbref><dbname>ITV3+1</dbname></dbent>
<dbent><dbref>1:0:1:2805:806:2:11A0000:0:0:0:</dbref><dbname>ITV4+1</dbname></dbent>
<dbent><dbref>1:0:1:2400:7F9:2:11A0000:0:0:0:</dbref><dbname>Channel 4</dbname></dbent>
<dbent><dbref>1:0:1:2077:808:2:11A0000:0:0:0:</dbref><dbname>Channel 4+1</dbname></dbent>
<dbent><dbref>1:0:1:206C:808:2:11A0000:0:0:0:</dbref><dbname>E4+1</dbname></dbent>
<dbent><dbref>1:0:1:240E:7F9:2:11A0000:0:0:0:</dbref><dbname>More4+1</dbname></dbent>
<dbent><dbref>1:0:1:2095:808:2:11A0000:0:0:0:</dbref><dbname>Film4+1</dbname></dbent>
<dbent><dbref>1:0:1:1E15:809:2:11A0000:0:0:0:</dbref><dbname>Channel 5</dbname></dbent>
<dbent><dbref>1:0:1:1E28:809:2:11A0000:0:0:0:</dbref><dbname>Channel 5+1</dbname></dbent>
<dbent><dbref>1:0:1:1E1F:809:2:11A0000:0:0:0:</dbref><dbname>5USA+1</dbname></dbent>
<dbent><dbref>1:0:1:1E25:809:2:11A0000:0:0:0:</dbref><dbname>5STAR+1</dbname></dbent>
<dbent><dbref>1:0:19:22D9:80D:2:11A0000:0:0:0:</dbref><dbname>BBC NEWS HD</dbname></dbent>
<dbent><dbref>1:0:16:52B0:C99:3:EB0000:0:0:0:</dbref><dbname>Discovery Showcase HD virt</dbname></dbent>
<dbent><dbref>1:0:16:5276:C96:3:EB0000:0:0:0:</dbref><dbname>VTM Gold</dbname></dbent>
<dbent><dbref>1:0:19:52B0:C99:3:EB0000:0:0:0:</dbref><dbname>BBC First </dbname></dbent>
<dbent><dbref>1:0:16:17D1:C82:3:EB0000:0:0:0:</dbref><dbname>BBC Entertainment</dbname></dbent>
</DBENTS_REF>
</xsl:variable>
<xsl:variable name="CHNREF_RTF">
<CHANID_TO_DB_CHAN_REFS>
  <!-- For BE listings generated by tvgrabnlpy using my custom IDs 
       Started using this as from 12-Sep-2015 since the MS Mediacenter listings
       appear to have been turned off.       
  -->
  <channel id="XTVGRABPYeen">
      <display-name>één</display-name>
      <vuuname>een HD</vuuname>
   </channel>
  <channel id="XTVGRABPYcanvas">
      <display-name>Canvas</display-name>
      <vuuname>Canvas HD</vuuname>
   </channel>
  <channel id="XTVGRABPYvtm">
      <display-name>VTM</display-name>
      <vuuname>VTM HD</vuuname>
   </channel>
   <channel id="XTVGRABPY2be">
      <display-name>VTM 2</display-name>
      <vuuname>VTM 2 HD</vuuname>
   </channel>
   <channel id="XTVGRABPYvitaya">
   <display-name>VTM 3</display-name>
      <vuuname>VTM 3</vuuname>
   </channel>
   <channel id="XTVGRABPYacht">
      <display-name>VTM 4</display-name>
      <vuuname>VTM 4</vuuname>
   </channel>
   <channel id="XTVGRABPYvt4">
      <display-name>Play4</display-name>
      <vuuname>Play4</vuuname>
   </channel>
   <channel id="XTVGRABPYvijftv">
      <display-name>Play5</display-name>
      <vuuname>Play5</vuuname>
   </channel>
   <channel id="XTVGRABPYzes">
      <display-name>Play6</display-name>
      <vuuname>Play6</vuuname>
   </channel>
   <channel id="XTVGRABPYplay7">
      <display-name>Play7</display-name>
      <vuuname>Play7</vuuname>
   </channel>
   <channel id="XTVGRABPYned1">
      <display-name>Ned 1</display-name>
      <vuuname>NPO1 HD</vuuname>
   </channel>
   <channel id="XTVGRABPYned2">
      <display-name>Ned 2</display-name>
      <vuuname>NPO2 HD</vuuname>
   </channel>
   <channel id="XTVGRABPYned3">
      <display-name>Ned 3</display-name>
      <vuuname>NPO3 HD</vuuname>
   </channel>
   <channel id="XTVGRABPYarte">
      <display-name>ARTE</display-name>
      <vuuname>arte HD</vuuname>
   </channel>
   
<!--  Channel id used by the test data -->
<!-- channel id="XMLTVTestChannel">
   <display-name>BBC1</display-name>
   <dbname>BBC One Lon</dbname>
   <vuuname>BBC One Lon</vuuname>
</channel -->

<channel id="683.tvguide.co.uk">
   <display-name>BBC1HD</display-name>
   <vuuname>BBC One HD</vuuname>
</channel>
<channel id="387.tvguide.co.uk">
   <display-name>BBC2HD</display-name>
   <vuuname>BBC Two HD</vuuname>
</channel>
<channel id="1728.tvguide.co.uk">
   <display-name>BBC3HD</display-name>
   <vuuname>BBC Three HD</vuuname>
</channel>
<channel id="893.tvguide.co.uk">
   <display-name>BBC4HD</display-name>
   <vuuname>BBC Four HD</vuuname>
</channel>
<channel id="642.tvguide.co.uk">
   <display-name>ITV1HD</display-name>
   <vuuname>ITV HD</vuuname>
</channel>
<channel id="476.tvguide.co.uk">
   <display-name>Channel4HD</display-name>
   <vuuname>Channel 4 HD</vuuname>
</channel>
<channel id="657.tvguide.co.uk">
   <display-name>FiveHD</display-name>
   <vuuname>Channel 5 HD</vuuname>
</channel>

<channel id="74.tvguide.co.uk">
   <display-name>BBC1</display-name>
   <dbname>BBC One Lon</dbname>
   <vuuname>BBC One Lon</vuuname>
</channel>
<channel id="89.tvguide.co.uk">
   <display-name>BBC2</display-name>
   <dbname>BBC Two</dbname>
   <vuuname>BBC Two</vuuname>
</channel>
<channel id="1763.tvguide.co.uk">
   <display-name>BBC3</display-name>
   <dbname>BBC Three</dbname>
   <vuuname>BBC Three</vuuname>
</channel>
<channel id="109.tvguide.co.uk">
   <display-name>BBC4</display-name>
   <dbname>BBC Four</dbname>
   <vuuname>BBC Four</vuuname>
</channel>
<channel id="172.tvguide.co.uk">
   <display-name>ITV1</display-name>
   <dbname>ITV</dbname>
   <vuuname>ITV</vuuname>
</channel>
<channel id="121.tvguide.co.uk">
   <display-name>Channel4</display-name>
   <dbname>Channel 4</dbname>
   <vuuname>Channel 4</vuuname>
</channel>
<channel id="148.tvguide.co.uk">
   <display-name>Five</display-name>
   <dbname>Channel 5</dbname>
   <vuuname>Channel 5</vuuname>
</channel>
<channel id="180.tvguide.co.uk">
   <display-name>ITV2</display-name>
   <dbname>ITV2</dbname>
   <vuuname>ITV2</vuuname>
</channel>
<channel id="360.tvguide.co.uk">
   <display-name>ITV3</display-name>
   <dbname>ITV3</dbname>
   <vuuname>ITV3</vuuname>
</channel>
<channel id="367.tvguide.co.uk">
   <display-name>ITV4</display-name>
   <dbname>ITV4</dbname>
   <vuuname>ITV4</vuuname>
</channel>
<channel id="139.tvguide.co.u">
   <display-name>E4</display-name>
   <dbname>E4</dbname>
   <vuuname>E4</vuuname>
</channel>
<channel id="361.tvguide.co.uk">
   <display-name>More4</display-name>
   <dbname>More4</dbname>
   <vuuname>More4</vuuname>
</channel>
<channel id="145.tvguide.co.uk">
   <display-name>Film4</display-name>
   <dbname>Film4</dbname>
   <vuuname>Film4</vuuname>
</channel>
<channel id="375.tvguide.co.uk">
   <display-name>5USA</display-name>
   <dbname>5 USA</dbname>
   <vuuname>5 USA</vuuname>
</channel>
<channel id="374.tvguide.co.uk">
   <display-name>5STAR</display-name>
   <dbname>5STAR</dbname>
   <vuuname>5STAR</vuuname></channel>
<channel id="697.tvguide.co.uk">
   <display-name>ITV1+1</display-name>
   <dbname>ITV+1</dbname>
   <vuuname>ITV+1</vuuname>
</channel>
<channel id="428.tvguide.co.uk">
   <display-name>Channel4+1</display-name>
   <dbname>Channel 4+1</dbname>
   <vuuname>Channel 4+1</vuuname>
</channel>
<channel id="729.tvguide.co.uk">
   <display-name>Five+1</display-name>
   <dbname>Channel 5+1</dbname>
   <vuuname>Channel 5+1</vuuname>
</channel>
<channel id="376.tvguide.co.uk">
   <display-name>ITV2+1</display-name>
   <dbname>ITV2+1</dbname>
   <vuuname>ITV2+1</vuuname>
</channel>
<channel id="474.tvguide.co.uk">
   <display-name>ITV3+1</display-name>
   <dbname>ITV3+1</dbname>
   <vuuname>ITV3+1</vuuname>
</channel>
<channel id="530.tvguide.co.uk">
   <display-name>ITV4+1</display-name>
   <dbname>ITV4+1</dbname>
   <vuuname>ITV4+1</vuuname>
</channel>
<channel id="371.tvguide.co.uk">
   <display-name>E4+1</display-name>
   <dbname>E4+1</dbname>
   <vuuname>E4+1</vuuname>
</channel>
<channel id="429.tvguide.co.uk">
   <display-name>More4+1</display-name>
   <dbname>More4+1</dbname>
   <vuuname>More4+1</vuuname>
</channel>
<channel id="146.tvguide.co.uk">
   <display-name>Film4+1</display-name>
   <dbname>Film4+1</dbname>
   <vuuname>Film4+1</vuuname>
</channel>
<channel id="572.tvguide.co.uk">
   <display-name>5USA+1</display-name>
   <dbname>5USA+1</dbname>
   <vuuname>5USA+1</vuuname>
</channel>
<channel id="901.tvguide.co.uk">
   <display-name>5STAR+1</display-name>
   <dbname>5STAR+1</dbname>
   <vuuname>5STAR+1</vuuname>
</channel>

<!-- Maybe one day I'll figure out how to do the channel id mapping for the Perl xmltv grabber...

<channel id="TVG.BBC1">
   <display-name>BBC1</display-name>
   <dbname>BBC One Lon</dbname>
   <vuuname>BBC One Lon</vuuname>
</channel>
<channel id="TVG.BBC1.HD">
   <display-name>BBC1HD</display-name>
   <vuuname>BBC One HD</vuuname>
</channel>
<channel id="TVG.BBC2">
   <display-name>BBC2</display-name>
   <dbname>BBC Two</dbname>
   <vuuname>BBC Two</vuuname>
</channel>
<channel id="TVG.BBC2.HD">
   <display-name>BBC2HD</display-name>
   <vuuname>BBC Two HD</vuuname>
</channel>
<channel id="TVG.BBC3.HD">
   <display-name>BBC3HD</display-name>
   <vuuname>BBC Three HD</vuuname>
</channel>
<channel id="TVG.BBC3.SD">
   <display-name>BBC3</display-name>
   <dbname>BBC Three</dbname>
   <vuuname>BBC Three</vuuname>
</channel>
<channel id="TVG.BBC4">
   <display-name>BBC4</display-name>
   <dbname>BBC Four</dbname>
   <vuuname>BBC Four</vuuname>
</channel>
<channel id="TVG.BBC4.HD">
   <display-name>BBC4HD</display-name>
   <vuuname>BBC Four HD</vuuname>
</channel>
<channel id="TVG.ITV1">
   <display-name>ITV1</display-name>
   <dbname>ITV</dbname>
   <vuuname>ITV</vuuname>
</channel>
<channel id="TVG.ITV1.HD">
   <display-name>ITV1HD</display-name>
   <vuuname>ITV HD</vuuname>
</channel>
<channel id="TVG.ITV2">
   <display-name>ITV2</display-name>
   <dbname>ITV2</dbname>
   <vuuname>ITV2</vuuname>
</channel>
<channel id="TVG.ITV3">
   <display-name>ITV3</display-name>
   <dbname>ITV3</dbname>
   <vuuname>ITV3</vuuname>
</channel>
<channel id="TVG.ITV4">
   <display-name>ITV4</display-name>
   <dbname>ITV4</dbname>
   <vuuname>ITV4</vuuname>
</channel>
<channel id="TVG.ITVB">
   <display-name>ITVB</display-name>
   <dbname>ITVBe</dbname>
   <vuuname>ITVBe</vuuname>    
</channel>
<channel id="TVG.C4">
   <display-name>Channel4</display-name>
   <dbname>Channel 4</dbname>
   <vuuname>Channel 4</vuuname>
</channel>
<channel id="TVG.C4.HD">
   <display-name>Channel4HD</display-name>
   <vuuname>Channel 4 HD</vuuname>
</channel>
<channel id="TVG.E4">
   <display-name>E4</display-name>
   <dbname>E4</dbname>
   <vuuname>E4</vuuname>
</channel>
<channel id="TVG.M4">
   <display-name>More4</display-name>
   <dbname>More4</dbname>
   <vuuname>More4</vuuname>
</channel>
<channel id="TVG.F4">
   <display-name>Film4</display-name>
   <dbname>Film4</dbname>
   <vuuname>Film4</vuuname>
</channel>
<channel id="TVG.C5">
   <display-name>Five</display-name>
   <dbname>Channel 5</dbname>
   <vuuname>Channel 5</vuuname>
</channel>
<channel id="TVG.C5.HD">
   <display-name>FiveHD</display-name>
   <vuuname>Channel 5 HD</vuuname>
</channel>
<channel id="TVG.5USA">
   <display-name>5USA</display-name>
   <dbname>5 USA</dbname>
   <vuuname>5 USA</vuuname>
</channel>
<channel id="TVG.5STAR">
   <display-name>5STAR</display-name>
   <dbname>5STAR</dbname>
   <vuuname>5STAR</vuuname></channel>
<channel id="TVG.5STARalt">
   <display-name></display-name>
</channel>
<channel id="TVG.ITV1_1">
   <display-name>ITV1+1</display-name>
   <dbname>ITV+1</dbname>
   <vuuname>ITV+1</vuuname>
</channel>
<channel id="TVG.ITV2_1">
   <display-name>ITV2+1</display-name>
   <dbname>ITV2+1</dbname>
   <vuuname>ITV2+1</vuuname>
</channel>
<channel id="TVG.ITV3_1">
   <display-name>ITV3+1</display-name>
   <dbname>ITV3+1</dbname>
   <vuuname>ITV3+1</vuuname>
</channel>
<channel id="TVG.ITV4_1">
   <display-name>ITV4+1</display-name>
   <dbname>ITV4+1</dbname>
   <vuuname>ITV4+1</vuuname>
</channel>
<channel id="TVG.C4_1">
   <display-name>Channel4+1</display-name>
   <dbname>Channel 4+1</dbname>
   <vuuname>Channel 4+1</vuuname>
</channel>
<channel id="TVG.E4_1">
   <display-name>E4+1</display-name>
   <dbname>E4+1</dbname>
   <vuuname>E4+1</vuuname>
</channel>
<channel id="TVG.M4_1">
   <display-name>More4+1</display-name>
   <dbname>More4+1</dbname>
   <vuuname>More4+1</vuuname>
</channel>
<channel id="TVG.F4_1">
   <display-name>Film4+1</display-name>
   <dbname>Film4+1</dbname>
   <vuuname>Film4+1</vuuname>
</channel>
<channel id="TVG.C5_1">
   <display-name>Five+1</display-name>
   <dbname>Channel 5+1</dbname>
   <vuuname>Channel 5+1</vuuname>
</channel>
<channel id="TVG.5USA_1">
   <display-name>5USA+1</display-name>
   <dbname>5 USA+1</dbname>
   <vuuname>5 USA+1</vuuname>
</channel>
<channel id="TVG.C5STAR_1">
   <display-name>5STAR+1</display-name>
   <dbname>5STAR+1</dbname>
   <vuuname>5STAR+1</vuuname>
</channel>
<channel id="TVG.C5_24">
   <display-name>5SELECT</display-name>
   <dbname>5SELECT</dbname>
   <vuuname>5SELECT</vuuname>
</channel>
<channel id="TVG.BBCRB1">
   <display-name>BBCRB1</display-name>
   <vuuname>BBC RB 1</vuuname>
</channel>
<channel id="TVG.BBC1Lon">
   <display-name>BBC1Lon</display-name>
</channel>
<channel id="TVG.BBCRB3">
   <display-name>BBCRB3</display-name>
</channel>
<channel id="TVG.BBCRB2">
   <display-name>BBCRB2</display-name>
</channel>
-->

</CHANID_TO_DB_CHAN_REFS>
</xsl:variable>

<xsl:variable name="CHNREF" select="scu:convertRTFtoNode($CHNREF_RTF,'//CHANID_TO_DB_CHAN_REFS')" />
<xsl:variable name="DBENTS" select="scu:convertRTFtoNode($DBENT_RTF,'//DBENTS_REF')" />
<xsl:variable name="VUUENTS" select="scu:convertRTFtoNode($VUUENT_RTF,'//DBENTS_REF')" />

<!--
   Version 5. Day looping and channel looping are all performed by this file - with a little help from some
   Java extension functions to calculate the time ranges for each file and to actually save the files.

   The output of this stylesheet is the favorites file, with links to the generated HTML files. To achieve this
   required that a new extension function was added which converts the XML text in a variable into a nodeset.

   The FAVCRITs are loaded directly from an Xml file.
-->

<xsl:template name="nxtchan">
<xsl:param name="curchn" />
<!-- ID should come from CHNREF not from the listing file (ie. /tv) as the display names probably
     don't match what is used in CHNREF
 -->
<xsl:variable name="id" select="$CHNREF/channel[display-name=$curchn]/@id" />
<xsl:variable name="nxt" select="$CHNREF/channel[@id=$id]/following-sibling::channel[not(normalize-space(display-name) = '')][1]" />
<!-- CURCHAN:<xsl:value-of select="$curchn" />, ID:<xsl:value-of select="$id" /> -->
<xsl:choose>
<xsl:when test="not($nxt)">
<xsl:value-of select="$CHNREF/channel[position()=1]/display-name" />
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$nxt/display-name" />
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="prvchan">
<xsl:param name="curchn" />
<xsl:variable name="id" select="$CHNREF/channel[display-name=$curchn]/@id" />
<xsl:variable name="nxt" select="$CHNREF/channel[@id=$id]/preceding-sibling::channel[not(normalize-space(display-name) = '')][1]" />
<!-- CURCHAN:<xsl:value-of select="$curchn" />, ID:<xsl:value-of select="$id" /> -->
<xsl:choose>
<xsl:when test="not($nxt)">
<xsl:value-of select="$CHNREF/channel[position()=last()]/display-name" />
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$nxt/display-name" />
</xsl:otherwise>
</xsl:choose>
</xsl:template>



<xsl:template name="makoutfile">
<xsl:param name="chan" />
<xsl:param name="DATEL" />
<!-- Use local display name which should be filesystem safe -->
<xsl:variable name="channm" select="$CHNREF/channel[@id=$chan/@id]/display-name" />
<xsl:call-template name="makoutfilenm"><xsl:with-param name="channm" select="$channm"/><xsl:with-param name="DATEL" select="$DATEL" /></xsl:call-template>
</xsl:template>
<xsl:template name="makoutfilenm">
<xsl:param name="channm" />
<xsl:param name="DATEL" />
<xsl:value-of select="translate($channm,' ','')"/>_<xsl:value-of select="scu:formatDate($DATEL, 'EEE')" /><xsl:text>.html</xsl:text>
</xsl:template>

<xsl:template match="FAVORITES">
<HTML>
   <head>
   <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate"/>
   <meta http-equiv="Pragma" content="no-cache"/>
   <meta http-equiv="Expires" content="0"/>
   <link href="../stylesheet.css" rel="stylesheet" type="text/css" />
</head>
<BODY LINK="#0000ff" VLINK="#800080" BGCOLOR="#c0c0c0">
<H2><xsl:value-of select="DESC" /></H2>
<TABLE>
  <!-- Must specify the FAV nodes for sort to work. Just using simple apply-template
       and sorting on FAV/START didn't work!!! -->
  <xsl:apply-templates select="FAV">
      <xsl:sort select="START" order="ascending" />
  </xsl:apply-templates>
</TABLE>
</BODY>
</HTML>
</xsl:template>

<xsl:template match="FAV">
<TR>
<TD class="favstart"><xsl:value-of select="scu:formatDate(START, 'EEE, dd MMM')" /></TD>
<TD class="favstart"><xsl:value-of select="scu:formatDate(START, 'HH:mm')" /></TD>
<TD class="favchan"><xsl:value-of select="CHANNEL" /></TD>

<TD class="favprog">
<A class="favprog"><xsl:attribute name="HREF"><xsl:value-of select="DOC" />#<xsl:value-of select="IDX" /></xsl:attribute>
<xsl:value-of select="PROG" />
<xsl:if test="EPISODE">
 (<xsl:value-of select="normalize-space(EPISODE)" />)
</xsl:if>
</A>
</TD><TD class="favprog">
<xsl:apply-templates select="DBREF" />
<xsl:apply-templates select="VUREF" />
</TD>
<xsl:if test="CAT">
  <TD class="favchan"><xsl:value-of select="normalize-space(CAT)" /></TD>
</xsl:if>
</TR>
</xsl:template>

<xsl:template match="DBREF">
<xsl:call-template name="addDBlink"><xsl:with-param name="href" select="." /></xsl:call-template>
</xsl:template>

<xsl:template match="VUREF">
<xsl:call-template name="addVUlink"><xsl:with-param name="href" select="." /></xsl:call-template>
</xsl:template>

<xsl:key name="progidx" match="programme" use="@start"/>
<!-- usage ="#{generate-id(key('progidx',.))}" -->

<xsl:template match="tv"> <!-- tv is from the xmltv file -->
   <xsl:variable name="DATERANGES" select="scu:getDateRanges()" />
   <xsl:variable name="TV" select="." />

   <!-- Loop through all progs making a file for each channel on each day -->
   <xsl:for-each select="$DATERANGES/RANGE">
      <xsl:variable name="ST" select="START" />
      <xsl:variable name="EN" select="END" />
       <xsl:for-each select="$TV/channel"> 
         <!-- Should only do anything with the channel if
              if $CHNREF/channel[@id=$id]/display-name is not empty
              The test should return true when: 
               - channel[id] is absent
               - channel[id] is present, display-name is absent
               - channel[id] is present, display-name is present but empty (<display-name /> or <display-name></display-name>)
               - channel[id] is present, display-name is present but only contains whitespace, eg.
                 <display-name>  </display-name> or <display-name>
                 </display-name>. Can probably only do this by stripping spaces, newlines, tabs... unless regexs can be used?
              The use of "if" is probably the wrong way to go about this, should probably use a template, but I
              can't figure out how to do it with the mixed contexts, ie. the context here is the programmes list,
              but a template matching the channel/display-name would have the stylesheet as the context.
              Maybe it could be done passing the "tv" as a param, in addition to the date range. 
              For now will just ensure that there are no spaces in display-name if it is supposed to be treated as empty!
         -->
         <xsl:variable name="xmltvid" select="@id"/>
         <xsl:if test="not(normalize-space($CHNREF/channel[@id=$xmltvid]/display-name) = '')">
            <xsl:variable name="onechannel">
            <xsl:apply-templates select=".">
               <xsl:with-param name="MINDATEL" select="$ST" />
               <xsl:with-param name="MAXDATEL" select="$EN" />
            </xsl:apply-templates>
            </xsl:variable>
            <xsl:variable name="OUTDOC"><xsl:value-of disable-output-escaping="yes" select="$OUTPATH"/><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="."/><xsl:with-param name="DATEL"><xsl:value-of select="$ST" /></xsl:with-param></xsl:call-template></xsl:variable>
            <xsl:value-of select="scu:writeToFile($onechannel, $OUTDOC)" />
         </xsl:if>
       </xsl:for-each>
   </xsl:for-each>

   <!-- Loop through all progs making a list of all the favorite programmes providing it is on
        one of the enabled channels -->
   <xsl:variable name="favlist">
   <FAVORITES>
      <DESC>Coming to a TV near you...</DESC>
      <xsl:for-each select="$DATERANGES/RANGE">
      <xsl:variable name="ST" select="START" />
      <xsl:variable name="EN" select="END" />
         <xsl:call-template name="listfavs">
            <xsl:with-param name="ST" select="$ST" />
            <!-- Must use variables for range dates. Using just START interprets it as meaning a node called
              START in the context of the XPath, ie a child node of programme in this case. -->
            <xsl:with-param name="PROGS" select="$TV/programme[scu:isDateInRange($ST, $EN, @start)]" />
         </xsl:call-template>

      </xsl:for-each>
   </FAVORITES>
   </xsl:variable>

   <xsl:variable name="favnewseries">
   <FAVORITES>
      <DESC>New series starting soon</DESC>
      <xsl:for-each select="$DATERANGES/RANGE">
      <xsl:variable name="ST" select="START" />
      <xsl:variable name="EN" select="END" />
         <xsl:call-template name="listnewseries">
            <xsl:with-param name="ST" select="$ST" />
            <!-- Must use variables for range dates. Using just START interprets it as meaning a node called
              START in the context of the XPath, ie a child node of programme in this case. -->
            <xsl:with-param name="PROGS" select="$TV/programme[scu:isDateInRange($ST, $EN, @start)]" />
         </xsl:call-template>

      </xsl:for-each>
   </FAVORITES>
   </xsl:variable>
   
   <!-- convert the favlist contents into a node which can be processed, the return from convertRTFtoNode is a node containing the FAVORITES nodes-->
   <xsl:apply-templates select="scu:convertRTFtoNode($favlist,'//FAVORITES')"></xsl:apply-templates>
   
   <!-- Write the newseries list into variable 'newseriespage' and then save the page-->
   <!-- xsl:apply-templates select="scu:convertRTFtoNode($favnewseries,'//NEWSERIES')"></xsl:apply-templates -->
   <xsl:variable name="newseriespage">
      <xsl:apply-templates select="scu:convertRTFtoNode($favnewseries,'//FAVORITES')" />
   </xsl:variable>
    <xsl:variable name="NSPATH"><xsl:value-of disable-output-escaping="yes" select="$OUTPATH"/>\00newseries.htm</xsl:variable>
   <xsl:value-of select="scu:writeToFile($newseriespage, $NSPATH)" />
   
   <!-- Write NFO files for the favourties -->
   <xsl:call-template name="favestonfos">
      <xsl:with-param name="faves" select="scu:convertRTFtoNode($favlist,'//FAVORITES')" />
   </xsl:call-template>

</xsl:template>

<xsl:template match="channel"> <!-- this is a channel from the xmltv -->
<xsl:param name="MINDATEL" />
<xsl:param name="MAXDATEL" />
   <xsl:variable name="id" select="@id"/>

   <!-- In fact this should only be processed IF there is an entry for the channel id $CHNREF -->
   <xsl:variable name="cdbname" select="$CHNREF/channel[@id=$id]/dbname"/>
   <xsl:variable name="dbref" select="$DBENTS/dbent[dbname=$cdbname]"/>

   <xsl:variable name="cvuname" select="$CHNREF/channel[@id=$id]/vuuname"/>
   <xsl:variable name="vuref" select="$VUUENTS/dbent[dbname=$cvuname]"/>

   <xsl:variable name="cname"><xsl:value-of select="$CHNREF/channel[@id=$id]/display-name"/></xsl:variable>
   <xsl:variable name="nextday" select="scu:addToDate($MINDATEL, 'DAY', 1)" />
   <xsl:variable name="prevday" select="scu:addToDate($MINDATEL, 'DAY', -1)" />
   <!-- link to next day makes assumption that MAXDATEL will be actually be a time in the day after MINDATEL -->
   <xsl:variable name="nextp"><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="."/><xsl:with-param name="DATEL" select="$nextday" /></xsl:call-template></xsl:variable>
   <xsl:variable name="prevp"><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="."/><xsl:with-param name="DATEL" select="$prevday" /></xsl:call-template></xsl:variable>
   <xsl:variable name="prevc"><xsl:call-template name="prvchan"><xsl:with-param name="curchn" select="$cname" /></xsl:call-template></xsl:variable>
   <xsl:variable name="nextc"><xsl:call-template name="nxtchan"><xsl:with-param name="curchn" select="$cname" /></xsl:call-template></xsl:variable>
   <xsl:variable name="prevcd"><xsl:call-template name="makoutfilenm"><xsl:with-param name="channm" select="$prevc"/><xsl:with-param name="DATEL" select="$MINDATEL" /></xsl:call-template></xsl:variable>
   <xsl:variable name="nextcd"><xsl:call-template name="makoutfilenm"><xsl:with-param name="channm" select="$nextc"/><xsl:with-param name="DATEL" select="$MINDATEL" /></xsl:call-template></xsl:variable>

  <HTML>
    <HEAD>
    <link href="../stylesheet.css" rel="stylesheet" type="text/css" />
     <TITLE><xsl:value-of select="$cname" /> on <xsl:value-of select="scu:getLongDate($MINDATEL)" /></TITLE>
    </HEAD>

    <BODY BGCOLOR="#c0c0c0">
   <A HREF="{$prevp}"><xsl:value-of select="$cname" /> on <xsl:value-of select="scu:formatDate($prevday, 'EEE')" /></A> |
   <A HREF="{$prevcd}"><xsl:value-of select="$prevc" /> on <xsl:value-of select="scu:formatDate($MINDATEL, 'EEE')" /></A> |
   <A HREF="{$nextcd}"><xsl:value-of select="$nextc" /> on <xsl:value-of select="scu:formatDate($MINDATEL, 'EEE')" /></A>
   <H2>Listings for <xsl:value-of select="$cname" /> on <xsl:value-of select="scu:getLongDate($MINDATEL)" /></H2><xsl:text>
</xsl:text>

<TABLE class="tvlists">
   <xsl:apply-templates select="../programme[(@channel=$id) and (scu:isDateInRange($MINDATEL,$MAXDATEL, @start))]">
      <xsl:with-param name="OUTDOC"><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="//channel[@id=$id]"/><xsl:with-param name="DATEL" select="$MINDATEL" /></xsl:call-template></xsl:with-param>
      <xsl:with-param name="dbref" select="$dbref" />
      <xsl:with-param name="vuref" select="$vuref" />
      <xsl:sort select="@start" order="ascending" />
   </xsl:apply-templates>
</TABLE>

   <A NAME="BOTTOM"></A>
   <A HREF="{$nextp}#BOTTOM"><xsl:value-of select="$cname" /> on <xsl:value-of select="scu:formatDate($MAXDATEL, 'EEE')" /></A> |
   <A HREF="{$prevcd}#BOTTOM"><xsl:value-of select="$prevc" /> on <xsl:value-of select="scu:formatDate($MINDATEL, 'EEE')" /></A> |
   <A HREF="{$nextcd}#BOTTOM"><xsl:value-of select="$nextc" /> on <xsl:value-of select="scu:formatDate($MINDATEL, 'EEE')" /></A>

   <HR />
<xsl:value-of select="$cname" /> on <xsl:value-of select="scu:getLongDate($MINDATEL)" /><BR />
    </BODY>
  </HTML>
</xsl:template>

<xsl:template match="programme">
<xsl:param name="OUTDOC" />
<xsl:param name="dbref" />
<xsl:param name="vuref" />

<xsl:variable name="channel" select="@channel"/>
<xsl:variable name="ctitle"><xsl:call-template name="cleantitle"><xsl:with-param name="rawtitle" select="title" /></xsl:call-template></xsl:variable>
<xsl:variable name="matctitle"><xsl:value-of select="$ctitle" />=<xsl:apply-templates select="episode-num" mode="fav" /></xsl:variable>
<xsl:variable name="category"><xsl:apply-templates select="category" /></xsl:variable>
<TR>
      <xsl:choose>
        <xsl:when test='(position() mod 2)=0'>
         <xsl:attribute name="CLASS">prog0</xsl:attribute>
        </xsl:when>
       <xsl:otherwise>
         <xsl:attribute name="CLASS">prog1</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
<TD class="time"><xsl:value-of select="scu:getTime(@start)" /></TD>
<!-- the disable-out-esc and &lt;s are there just to keep the XML island on the same line as the title in the HTML file!!! -->
<TD class="title">
<xsl:choose>
   <xsl:when test="scu:isMatch($matctitle,$FAVCRIT/CRIT)">
      <A NAME="{generate-id()}" class="title_favorite"><xsl:value-of select="$ctitle" /></A>
   </xsl:when>
   <xsl:when test="scu:isMatch($matctitle,$NEWSERIESCRIT/CRIT) and not(scu:isMatch($category,$NEWSERIESEXCL/CRIT))">
      <A NAME="{generate-id()}" class="title_newseries"><xsl:value-of select="$ctitle" /></A>
   </xsl:when>
   <xsl:otherwise>
      <A NAME="{generate-id()}" class="title_normal"><xsl:value-of select="$ctitle" /></A>
   </xsl:otherwise>
</xsl:choose>
</TD><xsl:text>
</xsl:text><TD class="desc">
<xsl:variable name="eptitle" select="scu:getFullEpisodetitle( episode-num[@system='xmltv_ns'], sub-title, '. ')" />
<xsl:if test="boolean($eptitle)">
   <xsl:text> </xsl:text>
   <span class="episode"><xsl:value-of select="$eptitle" /></span>.<xsl:text> </xsl:text>
</xsl:if>
<xsl:value-of select="desc" />
<xsl:if test="length">
 <xsl:text> (</xsl:text><xsl:value-of select="length"/><xsl:value-of select="substring(length/@units,1,3)"/>
 <xsl:text>)</xsl:text>
</xsl:if>
<xsl:text>
</xsl:text></TD>
<TD class="time">
   <xsl:if test="not(scu:isMatch($matctitle,$FAVCRIT/CRIT))">
      <A href="../crit/addcrit.php?CRIT={$ctitle}&amp;NPAGE=critlist.php" class="fav"><img vspace="4" hspace="2" alt="Add to favorites" border="0" src="../heart.png" /></A>
   </xsl:if>
   <!-- Insert link to add program to DreamBox timer list (only useful if my DreamBox is available!!) -->
   <xsl:call-template name="addDBlink"><xsl:with-param name="href"><xsl:call-template name="makeVUhref">
      <xsl:with-param name="prog" select="."/>
      <xsl:with-param name="dbref" select="$dbref" />
      <xsl:with-param name="stbhost"><xsl:value-of select="$DRMBOXSRV" /></xsl:with-param>
   </xsl:call-template></xsl:with-param>
   </xsl:call-template>

   <!-- Insert link to add program to VU+ Ultimo timer list (only useful if my VU+ Ultimo is available!!) -->
   <xsl:call-template name="addVUlink"><xsl:with-param name="href"><xsl:call-template name="makeVUhref">
      <xsl:with-param name="prog" select="."/>
      <xsl:with-param name="dbref" select="$vuref" />
      <xsl:with-param name="stbhost"><xsl:value-of select="$VUPUSRV" /></xsl:with-param>
   </xsl:call-template></xsl:with-param>
   </xsl:call-template>

</TD>
</TR>
</xsl:template>

<!-- Used for the favourites match target to allow episdoe number matching-->
<xsl:template match="episode-num[@system='xmltv_ns']" mode="fav">
   <xsl:value-of select="scu:getEpisodeSxN(.)" />
</xsl:template>

<!-- Create the a favorite list entry for the specified list of programmes (from the xmltv file) -->
<!-- PROGS must be a list of programme elements to check for favorites -->
<xsl:template name="listfavs">
<xsl:param name="ST" />
<xsl:param name="PROGS" />
<xsl:for-each select="$PROGS">
   <!-- 21-May-2018 Webgrabber has started to append (?) to the titles, very annoying and cannot be disabled
        15-Dec-2011 If a favourite happens to be the first prog then the title will have the Radio Times crap
                    at the begining - should find a way to strip it from the XML, possibly during
                    the merge.
    -->
   <xsl:variable name="ctitle"><xsl:call-template name="cleantitle"><xsl:with-param name="rawtitle" select="title" /></xsl:call-template></xsl:variable>
   <xsl:variable name="matctitle"><xsl:value-of select="$ctitle" />=<xsl:apply-templates select="episode-num" mode="fav" /></xsl:variable>
   <xsl:variable name="xmltvid" select="current()/@channel" /> 
   <xsl:if test="not(normalize-space($CHNREF/channel[@id=$xmltvid]/display-name) = '')"> <!-- ignore progs whose channel is not in CHNREFs -->
      <xsl:if test="scu:isMatch($matctitle,$FAVCRIT/CRIT)">
         
         <!-- NFO needs the individual components of filename/timername. Rather than break up the template into all it's
              component parts nad risk breaking something maybe easier to parse the filename to get the info...
              Or maybe the filename template can return all of the NFO related nodes in one go
          -->
         
         <FAV>
            <PROG><xsl:value-of select="$ctitle" /></PROG>
            <EPISODE><xsl:value-of select="scu:getFullEpisodetitle( episode-num[@system='xmltv_ns'], sub-title)" /></EPISODE>
            <START><xsl:value-of select="@start" /></START>
            <CHANNEL><xsl:value-of select="$CHNREF/channel[@id=$xmltvid]/display-name" /></CHANNEL>
            <IDX><xsl:value-of select="generate-id()" /></IDX>
            <DOC><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="//channel[@id=current()/@channel]"/><xsl:with-param name="DATEL"><xsl:value-of select="$ST" /></xsl:with-param></xsl:call-template></DOC>
            <xsl:call-template name="nfoinfoforfav">
               <xsl:with-param name="prog" select="current()"/>
            </xsl:call-template>                  
            <DBREF>
               <xsl:call-template name="makeVUhref">
                  <xsl:with-param name="prog" select="current()"/>
                  <xsl:with-param name="dbref" select="$DBENTS/dbent[dbname=$CHNREF/channel[@id=$xmltvid]/dbname]" />
                  <xsl:with-param name="stbhost"><xsl:value-of select="$DRMBOXSRV" /></xsl:with-param>
               </xsl:call-template>
            </DBREF>
            <VUREF>
               <xsl:call-template name="makeVUhref">
                  <xsl:with-param name="prog" select="current()"/>
                  <xsl:with-param name="dbref" select="$VUUENTS/dbent[dbname=$CHNREF/channel[@id=$xmltvid]/vuuname]" />
                  <xsl:with-param name="stbhost"><xsl:value-of select="$VUPUSRV" /></xsl:with-param>
              </xsl:call-template>
            </VUREF>
         </FAV>
      </xsl:if>
   </xsl:if>
</xsl:for-each>
</xsl:template>


<xsl:template match="category">
  <xsl:value-of select="." /><xsl:text> </xsl:text>
</xsl:template>

<!-- Copy of listfavs but with a fixed criteria to select for "season 1 episode 1" 
     Not sure about how to specify the criteria, might have to make a dummy list with
     just the one crit - scu:isMatch does not accept a simple string as a pattern. 
     You would not believe the amount of shirt this seach throws up. Need to find a way
     to refine the list a little. Some of the items contain one or more 'category' 
     tags. This might be handy for rejected items as there are some things I'm really
     not interested in. The excluded category pattern must be defined as a CRIT list, see above.
     It might make it easier to weed out the shirt if the category is shown in the 
     new series list. So will adding a new value into the FAV break everything?? There are
     often multiple category tags so need a way to merge them into a single value. Could
     maybe highlight certain categories as being most wothy of attention!
  -->
<xsl:template name="listnewseries">
<xsl:param name="ST" />
<xsl:param name="PROGS" />
<xsl:for-each select="$PROGS">
   <xsl:variable name="ctitle"><xsl:call-template name="cleantitle"><xsl:with-param name="rawtitle" select="title" /></xsl:call-template></xsl:variable>
   <xsl:variable name="matctitle"><xsl:value-of select="$ctitle" />=<xsl:apply-templates select="episode-num" mode="fav" /></xsl:variable>
   <xsl:variable name="category"><xsl:apply-templates select="category" /></xsl:variable>
   <xsl:variable name="xmltvid" select="current()/@channel" /> 
   <xsl:if test="not(normalize-space($CHNREF/channel[@id=$xmltvid]/display-name) = '')"> <!-- ignore progs whose channel is not in CHNREFs -->
      <xsl:if test="scu:isMatch($matctitle,$NEWSERIESCRIT/CRIT) and not(scu:isMatch($category,$NEWSERIESEXCL/CRIT))">
         <FAV>
            <PROG><xsl:value-of select="$ctitle" /></PROG>
            <EPISODE><xsl:value-of select="scu:getFullEpisodetitle( episode-num[@system='xmltv_ns'], sub-title)" /></EPISODE>
            <START><xsl:value-of select="@start" /></START>
            <CHANNEL><xsl:value-of select="$CHNREF/channel[@id=$xmltvid]/display-name" /></CHANNEL>
            <IDX><xsl:value-of select="generate-id()" /></IDX>
            <DOC><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="//channel[@id=current()/@channel]"/><xsl:with-param name="DATEL"><xsl:value-of select="$ST" /></xsl:with-param></xsl:call-template></DOC>
            <CAT><xsl:value-of select="$category" /></CAT>
            <DBREF>
               <xsl:call-template name="makeVUhref">
                  <xsl:with-param name="prog" select="current()"/>
                  <xsl:with-param name="dbref" select="$DBENTS/dbent[dbname=$CHNREF/channel[@id=$xmltvid]/dbname]" />
                  <xsl:with-param name="stbhost"><xsl:value-of select="$DRMBOXSRV" /></xsl:with-param>
               </xsl:call-template>
            </DBREF>
            <VUREF>
               <xsl:call-template name="makeVUhref">
                  <xsl:with-param name="prog" select="current()"/>
                  <xsl:with-param name="dbref" select="$VUUENTS/dbent[dbname=$CHNREF/channel[@id=$xmltvid]/vuuname]" />
                  <xsl:with-param name="stbhost"><xsl:value-of select="$VUPUSRV" /></xsl:with-param>
              </xsl:call-template>
            </VUREF>
         </FAV>
      </xsl:if>
   </xsl:if>
</xsl:for-each>
</xsl:template>

<xsl:template name="makeVUhref">
<xsl:param name="prog"/>
<xsl:param name="dbref" />
<xsl:param name="stbhost" />

<xsl:if test="$dbref">
<xsl:variable name="ostart" select="scu:addToDate($prog/@start, 'MINUTE', -10)" />
<xsl:variable name="epnum" select="$prog/episode-num[@system='xmltv_ns']" />
<xsl:variable name="event"><xsl:value-of select="scu:getEventName($epnum, $prog/sub-title, $prog/title, $ostart)" /></xsl:variable>
<xsl:variable name="oend"><xsl:call-template name="stopoff"><xsl:with-param name="pnode" select="$prog" /></xsl:call-template></xsl:variable>
<xsl:variable name="pref"><xsl:value-of select="$dbref/dbref" /></xsl:variable>

<xsl:variable name="href">
<xsl:text>http://</xsl:text><xsl:value-of select="$stbhost" /><xsl:text>/static/cpa/timerlist.htm?</xsl:text>
<xsl:text>&amp;syear=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'yyyy')" />
<xsl:text>&amp;smonth=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'M')" />
<xsl:text>&amp;sday=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'dd')" />
<xsl:text>&amp;shour=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'HH')" />
<xsl:text>&amp;smin=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'mm')" />
<xsl:text>&amp;eyear=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'yyyy')" />
<xsl:text>&amp;emonth=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'M')" />
<xsl:text>&amp;eday=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'dd')" />
<xsl:text>&amp;ehour=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'HH')" />
<xsl:text>&amp;emin=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'mm')" />
<xsl:text>&amp;sref=</xsl:text><xsl:value-of select="scu:urlencode($pref)" />
<xsl:text>&amp;name=</xsl:text><xsl:value-of select="scu:urlencode($event)" />
<xsl:if test="$epnum != ''">
   <!-- repeated: Monday=1, Tuesday=2, Wednesday=4, Thursday=8, Friday=16, Saturday=32, Sunday=64 -->
   <xsl:variable name="dayofweek" select="scu:formatDate($ostart, 'E')" />
   <xsl:text>&amp;repeated=</xsl:text>
   <xsl:choose>
      <xsl:when test='$dayofweek="Mon"'>
      <xsl:text>1</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Tue"'>
      <xsl:text>2</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Wed"'>
      <xsl:text>4</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Thu"'>
      <xsl:text>8</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Fri"'>
      <xsl:text>16</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Sat"'>
      <xsl:text>32</xsl:text>
      </xsl:when>
      <xsl:when test='$dayofweek="Sun"'>
      <xsl:text>64</xsl:text>
      </xsl:when>
   </xsl:choose>
</xsl:if>
</xsl:variable> <!-- href -->
<xsl:value-of select="$href" />
</xsl:if> <!-- dbref -->
</xsl:template>

<xsl:template name="addDBlink">
<xsl:param name="href"/>
<xsl:if test="normalize-space($href)">
<xsl:element name="A">
<xsl:attribute name="CLASS">dbx</xsl:attribute>
<xsl:attribute name="TARGET">dbxtimers</xsl:attribute>
<xsl:attribute name="HREF"><xsl:value-of select="$href" /></xsl:attribute><img vspace="4" hspace="4" alt="Add to Dreambox timers" border="0" src="../blueball.png" /></xsl:element>
</xsl:if>
</xsl:template>

<xsl:template name="addVUlink">
<xsl:param name="href"/>
<xsl:if test="normalize-space($href)">
<xsl:element name="A">
<xsl:attribute name="CLASS">dbx</xsl:attribute>
<xsl:attribute name="TARGET">vuutimers</xsl:attribute>
<xsl:attribute name="HREF"><xsl:value-of select="$href" /></xsl:attribute><img vspace="4" hspace="4" alt="Add to VU+ Ultimo timers" border="0" src="../vup.png" /></xsl:element>
</xsl:if>
</xsl:template>
 

<!-- Should be called with a programme node 
 If stoptime is missing a programme length of three hours is assumed.
 Stoptime is usually only missing for the last programme of the day.
 -->
<xsl:template name="stopoff">
<xsl:param name="pnode"/>
<xsl:variable name="twohrend" select="scu:addToDate($pnode/@start, 'MINUTE', 180)" />
<xsl:choose>
   <xsl:when test="$pnode/@stop">
      <xsl:choose>
         <xsl:when test="scu:isDateInRange($pnode/@start, $twohrend,$pnode/@stop)">
            <xsl:value-of select="scu:addToDate($pnode/@stop, 'MINUTE', 20)" />
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$twohrend" />   
         </xsl:otherwise>
      </xsl:choose> 
   </xsl:when>
   <xsl:otherwise>
      <xsl:value-of select="$twohrend" />
   </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="cleantitle">
   <xsl:param name="rawtitle"/>
   <xsl:choose>
      <xsl:when test="contains($rawtitle, 'third-party software applications available to the general public.??')">
         <xsl:value-of select="substring-after($rawtitle, 'third-party software applications available to the general public.??')" />
      </xsl:when>
      <!-- Webgrabber started appending (?) to all the titles, very annoying and no way to disable -->
      <xsl:when test="contains($rawtitle, ' (?)')">
         <xsl:value-of select="substring-before($rawtitle, ' (?)')" />
      </xsl:when> 
      <xsl:otherwise>
         <xsl:value-of select="$rawtitle" />
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<xsl:template name="favestonfos">
   <xsl:param name="faves" /> <!-- The FAVOURITES list -->
   <!--  
      Write an NFO file for each of the FAV nodes in the FAVOURITES list.
   -->
   <xsl:for-each select="$faves/FAV">
      <xsl:call-template name="favtonfo"><xsl:with-param name="fave" select="." /></xsl:call-template>
   </xsl:for-each>
</xsl:template>

<xsl:template name="favtonfo">
   <xsl:param name="fave" />
   <!--  
      Writes an NFO file for a single FAV.
      NB Output automatically gets a processing instruction
   -->
   <xsl:variable name="nfo">
      <xsl:element name="episodedetails">
         <xsl:element name="title"><xsl:value-of select="$fave/EPTITLE" /></xsl:element>
         <xsl:element name="showtitle"><xsl:value-of select="$fave/PROG" /></xsl:element>
         <xsl:element name="season"><xsl:value-of select="$fave/EPSEASON" /></xsl:element>
         <xsl:element name="episode"><xsl:value-of select="$fave/EPNUM" /></xsl:element>
         <xsl:element name="plot"><xsl:value-of select="$fave/PLOT" /></xsl:element>
         <xsl:element name="uniqueid">
            <xsl:attribute name="type">mytvshows</xsl:attribute>
            <xsl:attribute name="default">true</xsl:attribute><xsl:value-of select="$fave/UID" /></xsl:element>
         <xsl:element name="aired"><xsl:value-of select="$fave/EPDATE" /></xsl:element>
      </xsl:element>
   </xsl:variable>   
   <xsl:variable name="nfoname"><xsl:value-of disable-output-escaping="yes" select="$OUTPATH"/>\nfo\<xsl:value-of select="$fave/RECNAME" />.nfo</xsl:variable>
   <xsl:value-of select="scu:writeXMLToFile($nfo, $nfoname)" />
</xsl:template>

<xsl:template name="nfoinfoforfav">
   <xsl:param name="prog"/>
   
   <xsl:variable name="epinffromjava" select="scu:getEpisodeInfo($prog)" />
   <xsl:variable name="epseason">
      <xsl:value-of select="$epinffromjava/EPSEASON"/>
   </xsl:variable>
   <xsl:variable name="epnum">
      <xsl:value-of select="$epinffromjava/EPNUM"/>
   </xsl:variable>
   <xsl:variable name="eptitle">
      <xsl:value-of select="$epinffromjava/EPTITLE"/>
   </xsl:variable>
   
   
   <EPSEASON><xsl:value-of select="$epseason"/></EPSEASON>
   <EPNUM><xsl:value-of select="$epnum"/></EPNUM>
   <EPTITLE><xsl:value-of select="$eptitle"/></EPTITLE>
   <EPDATE><xsl:value-of select="$epinffromjava/EPDATE" /></EPDATE>
   <RECNAME><xsl:value-of select="$epinffromjava/RECNAME" /></RECNAME>
   <UID><xsl:value-of select="$epinffromjava/UID" /></UID>
   <PLOT><xsl:value-of select="$prog/desc" /></PLOT>            

</xsl:template>
<!-- This is the default template which matches any text. It is a built in template
     but I've included it here to remind me how to modify the default

<xsl:template match="text()">
   <xsl:value-of select="." />
</xsl:template>
  -->

</xsl:stylesheet>
