<?xml version="1.0"  encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:scu="//com.scu.xmltv.XSLTExtensions"
xmlns:loc="local.values"
version="1.0">
<xsl:output method="html" version="4.0"/>
<xsl:preserve-space elements="*"/>
<!-- xsl:param name="FAVCRIT" / --> <!-- This could be loaded with a document(file) command, in which case FAVCRIT should specify the filename -->
<xsl:param name="FAVFILE" />
<xsl:param name="OUTPATH" />
<!-- xsl:variable name="FAVCRIT" select="document('U:\0 CPA_EZDRIVE80_DONOTDELETE\java\XMLTransform\test\favcrit.xml')/node()" / -->
<xsl:variable name="FAVCRIT" select="document($FAVFILE)/node()" />
<!--
Skystar channel order must be
 0 BBC 1
 1 BBC 2
 2 BBC 3
 3 BBC 4
 4 ITV1 London
 5 ITV 2
 6 ITV 3
 7 ITV 4
 8 Channel 4
 9 E4
10 More 4
11 Film 4
12 ITV 2 +1
13 ITV 3 +1
14 E4 +1
15 More4 +1
16 Film 4 +1
17 BBC HD
18 Five
19 Channel 4 +1
20 ITV4 +1
21 ITV 1 Anglia S

12 Aug 2008 Adds timer links to the favorites page. Could not get this to work with makeDBhref and makeSShref
		    creating the links - no way to pass the links in the FAV node and get them to display in the favorites
		    page. So the addtimer methods now just return a URL string which needs to be put added to a link
		    everywhere it is used. A bit ugly, since need to test to see if there is a db or ss reference before
		    creating the link.
-->
<xsl:variable name="CHNREF_RTF">
<CHANID_TO_DB_CHAN_REFS>
  	<channel id="UK_RT_92">
    	<display-name lang="en">BBC1</display-name>
    	<dbent><dbname>BBC 1 London</dbname>
    	<dbref>1:0:1:189D:7FD:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>BBC 1 London (eng)</ssname>
		<ssref>0</ssref>
  	</channel>
  	<channel id="UK_RT_105">
   	<display-name lang="en">BBC2</display-name>
  		<dbent><dbname>BBC 2 England</dbname>
		<dbref>1:0:1:189E:7FD:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>BBC 2 England (eng)</ssname>
		<ssref>1</ssref>
	</channel>
	<channel id="UK_RT_45">
   	<display-name lang="en">BBC3</display-name>
		<dbent><dbname>BBC THREE</dbname>
		<dbref>1:0:1:18AF:7FD:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>BBC THREE (eng)</ssname>
		<ssref>2</ssref>
  	</channel>
  	<channel id="UK_RT_47">
    	<display-name lang="en">BBC4</display-name>
		<dbent><dbname>BBC FOUR</dbname>
		<dbref>1:0:1:18AC:7FD:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>BBC FOUR (eng)</ssname>
		<ssref>3</ssref>
  	</channel>
  	<channel id="UK_RT_24">
    	<display-name lang="en">ITV1 Anglia</display-name>
		<dbent><dbname>ITV1 Anglia S</dbname>
		<dbref>1:0:1:27C5:7F9:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV1 Anglia S</ssname>
		<ssref>21</ssref>
  	</channel>
  	<channel id="UK_RT_185">
    	<display-name lang="en">ITV2</display-name>
		<dbent><dbname>ITV2</dbname>
		<dbref>1:0:1:2756:7FC:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV2 (eng)</ssname>
		<ssref>5</ssref>
  	</channel>
  	<channel id="UK_RT_1859">
    	<display-name lang="en">ITV3</display-name>
		<dbent><dbname>ITV3</dbname>
		<dbref>1:0:1:2814:806:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV3 (eng)</ssname>
		<ssref>6</ssref>
  	</channel>
  	<channel id="UK_RT_1961">
    	<display-name lang="en">ITV4</display-name>
		<dbent><dbname>ITV4</dbname>
		<dbref>1:0:1:2758:7FC:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV4 (eng)</ssname>
		<ssref>7</ssref>
  	</channel>
  	<channel id="UK_RT_132">
    	<display-name lang="en">Channel 4</display-name>
		<dbent><dbname>Channel 4</dbname>
		<dbref>1:0:1:23FB:7F9:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>8350</ssname>
		<ssref>8</ssref>
  	</channel>
  	<channel id="UK_RT_158">
    	<display-name lang="en">E4</display-name>
		<dbent><dbname>E4</dbname>
		<dbref>1:0:1:2071:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>E4</ssname>
		<ssref>9</ssref>
  	</channel>
  	<channel id="UK_RT_1959">
    	<display-name lang="en">More 4</display-name>
		<dbent><dbname>More4</dbname>
		<dbref>1:0:1:2094:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>More4</ssname>
		<ssref>10</ssref>
  	</channel>
  	<channel id="UK_RT_160">
    	<display-name lang="en">Film4</display-name>
		<dbent><dbname>Film4</dbname>
		<dbref>1:0:1:208F:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>Film4 (eng)</ssname>
		<ssref>11</ssref>
  	</channel>
  	<channel id="UK_RT_1990">
    	<display-name lang="en">ITV2 +1</display-name>
		<dbent><dbname>ITV2+1</dbname>
		<dbref>1:0:1:27BC:7F9:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV2+1 (eng)</ssname>
		<ssref>12</ssref>
  	</channel>
  	<!-- channel id="UK_RT_1990">
    	<display-name lang="en">ITV3+1</display-name>
		<dbent><dbname>ITV2+3</dbname>
		<dbref>1:0:1:2815:806:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV3+1 (eng)</ssname>
		<ssref>13</ssref>
	</channel -->

  	<!-- channel id="UK_RT_1990">
    	<display-name lang="en">ITV4+1</display-name>
		<dbent><dbname>ITV4+1</dbname>
		<dbref>1:0:1:271F:801:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV4+1 (eng)</ssname>
		<ssref>21</ssref>
	</channel -->

  	<channel id="UK_RT_1161">
    	<display-name lang="en">E4+1</display-name>
		<dbent><dbname>E4+1</dbname>
		<dbref>1:0:1:206C:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>E4+1</ssname>
		<ssref>14</ssref>
  	</channel>
  	<channel id="UK_RT_1972">
    	<display-name lang="en">More 4+1</display-name>
		<dbent><dbname>More4+1</dbname>
		<dbref>1:0:1:2076:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>More4+1</ssname>
		<ssref>15</ssref>
  	</channel>
  	<channel id="UK_RT_2021">
	    <display-name lang="en">Film4+1</display-name>
		<dbent><dbname>Film4 +1</dbname>
		<dbref>1:0:1:208A:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>Film4 +1 (eng)</ssname>
		<ssref>16</ssref>
  	</channel>
  	<channel id="UK_RT_1994">
    	<display-name lang="en">BBC HD</display-name>
		<ssname>BBC HD</ssname>
		<ssref>17</ssref>
  	</channel>
  	<channel id="UK_RT_26">
    	<display-name lang="en">ITV1 London</display-name>
		<dbent><dbname>ITV1 London</dbname>
		<dbref>1:0:1:274C:7FC:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>ITV1 London</ssname>
		<ssref>4</ssref>
  	</channel>
  	<channel id="UK_RT_2047">
    	<display-name lang="en">Channel 4+1</display-name>
		<dbent><dbname>Channel 4 +1</dbname>
		<dbref>1:0:1:2077:7FA:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>BBC HD</ssname>
		<ssref>19</ssref>
  	</channel>
  	<channel id="UK_RT_134">
    	<display-name lang="en">Five</display-name>
		<dbent><dbname>Five</dbname>
		<dbref>1:0:1:1E15:809:2:11A0000:0:0:0:</dbref></dbent>
		<ssname>Five</ssname>
		<ssref>18</ssref>
  	</channel>
  	<channel id="UK_RT_2062">
		<display-name lang="en">FiveR</display-name>
		<dbent><dbname>FIVER</dbname>
		<dbref>1:0:1:1E23:809:2:11A0000:0:0:0:</dbref></dbent>
  	</channel>
  	<channel id="UK_RT_2008">
    	<display-name lang="en">Five US</display-name>
		<dbent><dbname>Five US</dbname>
		<dbref>1:0:1:1E1E:809:2:11A0000:0:0:0:</dbref></dbent>
  	</channel>
  	<channel id="UK_RT_1963">
    	<display-name lang="en">Sky Three</display-name>
		<dbent><dbname>Sky Three</dbname>
		<dbref>1:0:1:13EF:7EB:2:11A0000:0:0:0:</dbref></dbent>
  	</channel>


<!-- Channel ids for MC2XML - xmltv from the MS MediaCenter listings -->
  <channel id="I1.750821.microsoft.com">
    <display-name>één</display-name>
<dbent><dbname>één</dbname>
<dbref>1:0:1:31E7:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I2.750822.microsoft.com">
    <display-name>Ketnet-Canvas</display-name>
<dbent><dbname>Ketnet/Canvas</dbname>
<dbref>1:0:1:31E8:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I3.751666.microsoft.com">
    <display-name>VTM</display-name>
<dbent><dbname>VTM</dbname>
<dbref>1:0:1:31E3:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I4.751668.microsoft.com">
    <display-name>VT4</display-name>
<dbent><dbname>VT4</dbname>
<dbref>1:0:1:31E5:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I5.751667.microsoft.com">
    <display-name>2BE</display-name>
<dbent><dbname>2BE</dbname>
<dbref>1:0:1:31E4:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I6.753795.microsoft.com">
    <display-name>Vijf TV</display-name>
<dbent><dbname>VIJFtv</dbname>
<dbref>1:0:1:31F0:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I7.751671.microsoft.com">
    <display-name>Vitaya</display-name>
<dbent><dbname>Vitaya</dbname>
<dbref>1:0:1:31E9:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I11.750832.microsoft.com">
    <display-name>Ned 1</display-name>
<dbent><dbname>NED1</dbname>
<dbref>1:0:1:FAB:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I12.750833.microsoft.com">
    <display-name>Ned 2</display-name>
<dbent><dbname>NED2</dbname>
<dbref>1:0:1:FAC:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I13.750834.microsoft.com">
    <display-name>Ned 3</display-name>
<dbent><dbname>NED3</dbname>
<dbref>1:0:1:FAD:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="I84.751945.microsoft.com">
    <display-name>ARTE</display-name>
<dbent><dbname>ARTE</dbname>
<dbref>1:0:1:233B:400:1:C00000:0:0:0:</dbref></dbent>
  </channel>


<!-- ids from the XMLTV BE grabber (TVBlad) -->
  <channel id="tv1.vrt.be">
    <display-name>een</display-name>
<dbent><dbname>één</dbname>
<dbref>1:0:1:31E7:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="canvas.vrt.be">
    <display-name>Ketnet-Canvas</display-name>
<dbent><dbname>Ketnet/Canvas</dbname>
<dbref>1:0:1:31E8:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vtm.vrt.be">
    <display-name>VTM</display-name>
<dbent><dbname>VTM</dbname>
<dbref>1:0:1:31E3:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vt4.be">
    <display-name>VT4</display-name>
<dbent><dbname>VT4</dbname>
<dbref>1:0:1:31E5:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="ka2.be">
    <display-name>2BE</display-name>
<dbent><dbname>2BE</dbname>
<dbref>1:0:1:31E4:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vijftv.be">
    <display-name>Vijf TV</display-name>
<dbent><dbname>VIJFtv</dbname>
<dbref>1:0:1:31F0:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vitaya.tv">
    <display-name>Vitaya</display-name>
<dbent><dbname>Vitaya</dbname>
<dbref>1:0:1:31E9:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="hallmarkchannelint.com">
    <display-name>Hallmark</display-name>
<dbent><dbname>Hallmark</dbname>
<dbref>1:0:1:7F3:449:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="ned1.omroep.nl">
    <display-name>Ned 1</display-name>
<dbent><dbname>NED1</dbname>
<dbref>1:0:1:FAB:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="ned2.omroep.nl">
    <display-name>Ned 2</display-name>
<dbent><dbname>NED2</dbname>
<dbref>1:0:1:FAC:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="ned3.omroep.nl">
    <display-name>Ned 3</display-name>
<dbent><dbname>NED3</dbname>
<dbref>1:0:1:FAD:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
<!-- These must be the Skynet/JXmltv ids, no lnoger used -->
  <channel id="een.7">
    <display-name>een</display-name>
	<dbent><dbname>één</dbname>
	<dbref>1:0:1:31E7:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="ketnet-canvas.9">
    <display-name>Ketnet-Canvas</display-name>
	<dbent><dbname>Ketnet/Canvas</dbname>
	<dbref>1:0:1:31E8:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vtm.54">
    <display-name>VTM</display-name>
<dbent><dbname>VTM</dbname>
<dbref>1:0:1:31E3:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vt4.36">
    <display-name>VT4</display-name>
	<dbent><dbname>VT4</dbname>
	<dbref>1:0:1:31E5:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="kanaaltwee.18">
    <display-name>2BE</display-name>
<dbent><dbname>2BE</dbname>
<dbref>1:0:1:31E4:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vijf-tv.207">
    <display-name>Vijf TV</display-name>
<dbent><dbname>VIJFtv</dbname>
<dbref>1:0:1:31F0:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="vitaya.40">
    <display-name>Vitaya</display-name>
<dbent><dbname>Vitaya</dbname>
<dbref>1:0:1:31E9:45F:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="hallmark.2068">
    <display-name>Hallmark</display-name>
<dbent><dbname>Hallmark</dbname>
<dbref>1:0:1:7F3:449:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="nederland-1.216">
    <display-name>Ned 1</display-name>
<dbent><dbname>NED1</dbname>
<dbref>1:0:1:FAB:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="nederland-2.217">
    <display-name>Ned 2</display-name>
<dbent><dbname>NED2</dbname>
<dbref>1:0:1:FAC:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>
  <channel id="nederland-3.218">
    <display-name>Ned 3</display-name>
<dbent><dbname>NED3</dbname>
<dbref>1:0:1:FAD:451:35:C00000:0:0:0:</dbref></dbent>
  </channel>


</CHANID_TO_DB_CHAN_REFS>
</xsl:variable>

<xsl:variable name="CHNREF" select="scu:convertRTFtoNode($CHNREF_RTF,'//CHANID_TO_DB_CHAN_REFS')" />
<!--
	Version 5. Day looping and channel looping are all performed by this file - with a little help from some
	Java extension functions to calculate the time ranges for each file and to actually save the files.

	The output of this stylesheet is the favorites file, with links to the generated HTML files. To achieve this
	required that a new extension function was added which converts the XML text in a variable into a nodeset.

	The FAVCRITs are loaded directly from an Xml file.

  -->

<!-- Wanted to do this by simply defining a list in the XSL and using preceding/following-sibling to
     refer to it... but I couldn't find a way to do it. So, to avoid wasting even more time, use
     the brute force approach, which at least allows for wrapping...

     Maybe the idea for the dreambox channel refs would work for this aswell (if it works for the DB channel refs!!)

     Actually why not use the refs list to define the order??
-->
<xsl:template name="nxtchan">
<xsl:param name="curchn" />

<xsl:variable name="id" select="/tv/channel[display-name=$curchn]/@id" />
<xsl:variable name="nxt" select="$CHNREF/channel[@id=$id]/following-sibling::*[1]" />
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
<xsl:variable name="id" select="/tv/channel[display-name=$curchn]/@id" />
<xsl:variable name="nxt" select="$CHNREF/channel[@id=$id]/preceding-sibling::*[1]" />
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
<link href="../stylesheet.css" rel="stylesheet" type="text/css" />
<BODY LINK="#0000ff" VLINK="#800080" BGCOLOR="#c0c0c0">
<H2>Coming to a TV near you...</H2>
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
<td class="favprog">
<A  class="favprog"><xsl:attribute name="HREF"><xsl:value-of select="DOC" />#<xsl:value-of select="IDX" /></xsl:attribute>
<b><xsl:value-of select="PROG" /></b>
<xsl:if test="EPISODE">
 (<xsl:value-of select="EPISODE" />)
</xsl:if>
</A>
<xsl:apply-templates select="DBREF" />
<xsl:apply-templates select="SSREF" />
</td>
</TR>
</xsl:template>

<xsl:template match="DBREF">
<xsl:call-template name="addDBlink"><xsl:with-param name="href" select="." /></xsl:call-template>
</xsl:template>


<xsl:template match="SSREF">
<xsl:call-template name="addSSlink"><xsl:with-param name="href" select="." /></xsl:call-template>
</xsl:template>

<xsl:key name="progidx" match="programme" use="@start"/>
<!-- usage ="#{generate-id(key('progidx',.))}" -->

<xsl:template match="tv">
	<xsl:variable name="DATERANGES" select="scu:getDateRanges()" />
	<xsl:variable name="TV" select="." />

	<!-- Loop through all progs making a file for each channel on each day -->
	<xsl:for-each select="$DATERANGES/RANGE">
		<xsl:variable name="ST" select="START" />
		<xsl:variable name="EN" select="END" />
	    <xsl:for-each select="$TV/channel">
	      <xsl:variable name="onechannel">
	      <xsl:apply-templates select=".">
	      	<xsl:with-param name="MINDATEL" select="$ST" />
	      	<xsl:with-param name="MAXDATEL" select="$EN" />
	      </xsl:apply-templates>
	      </xsl:variable>
	      <xsl:variable name="OUTDOC"><xsl:value-of disable-output-escaping="yes" select="$OUTPATH"/><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="."/><xsl:with-param name="DATEL"><xsl:value-of select="$ST" /></xsl:with-param></xsl:call-template></xsl:variable>
	      <xsl:value-of select="scu:writeToFile($onechannel, $OUTDOC)" />
	    </xsl:for-each>
	</xsl:for-each>

	<!-- Loop through all progs making a list of all the favorite programmes -->
	<xsl:variable name="favlist">
	<FAVORITES>
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

	<!-- convert the favlist contents into a node which can be processed, the return from convertRTFtoNode is a node containing the FAVORITES nodes-->
	<xsl:apply-templates select="scu:convertRTFtoNode($favlist,'//FAVORITES')"></xsl:apply-templates>
	<!-- xsl:value-of select="scu:writeToFile($favlist, 'favlist.txt')" / -->

</xsl:template>

<xsl:template match="channel">
<xsl:param name="MINDATEL" />
<xsl:param name="MAXDATEL" />
	<xsl:variable name="id" select="@id"/>
	<xsl:variable name="dbref" select="$CHNREF/channel[@id=$id]/dbent"/>
	<xsl:variable name="ssref"><xsl:value-of select="$CHNREF/channel[@id=$id]/ssref"/></xsl:variable>
	<!-- xsl:variable name="cname" select="display-name"/ -->
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
		<xsl:with-param name="ssref" select="$ssref" />
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
<xsl:param name="ssref" />
<xsl:variable name="channel" select="@channel"/>
<!-- xsl:variable name="title" select="title"/ -->
<xsl:variable name="title">
<xsl:choose>
	<xsl:when test="contains(title, 'third-party software applications available to the general public.??')">
		<xsl:value-of select="substring-after(title, 'third-party software applications available to the general public.??')" />
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="title" />
	</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<TR>
      <xsl:choose>
        <xsl:when test='(position() mod 2)=0'>
        	<xsl:attribute name="CLASS">prog0</xsl:attribute>
        	<xsl:attribute name="STYLE">background-color: #FAF0E6</xsl:attribute>
        </xsl:when>
       <xsl:otherwise>
        	<xsl:attribute name="CLASS">prog1</xsl:attribute>
        	<xsl:attribute name="STYLE">background-color: #FFE4E1</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
<TD class="time"><xsl:value-of select="scu:getTime(@start)" /><BR />
   <xsl:if test="not(scu:isMatch($title,$FAVCRIT/CRIT))">
      <A href="../crit/addcrit.php?CRIT={$title}&amp;NPAGE=critlist.php" class="fav"><img hspace="2" alt="Add to favorites" border="0" src="../heart.gif" /></A>
   </xsl:if>
   <!-- Insert link to add program to my DreamBox timer list (only useful if my DreamBox is available!!) -->
   <xsl:call-template name="addDBlink"><xsl:with-param name="href"><xsl:call-template name="makeDBhref">
   	<xsl:with-param name="prog" select="."/>
   	<xsl:with-param name="dbref" select="$dbref" />
   </xsl:call-template></xsl:with-param>
   </xsl:call-template>

   <!-- Insert link to add program to my Skystar timer list (only useful if my Skystar is available!!) -->
   <xsl:call-template name="addSSlink"><xsl:with-param name="href"><xsl:call-template name="makeSShref">
	<xsl:with-param name="prog" select="."/>
   <xsl:with-param name="ssref" select="$ssref" />
	</xsl:call-template></xsl:with-param></xsl:call-template>

</TD>
<!-- the disable-out-esc and &lt;s are there just to keep the XML island on the same line as the title in the HTML file!!! -->
<!-- <TD class="title"><xsl:text disable-output-escaping="yes">&lt;B></xsl:text><xsl:value-of select="title" /><xsl:text disable-output-escaping="yes">&lt;/B>&lt;XML>&lt;START></xsl:text><xsl:value-of select="@start" /><xsl:text disable-output-escaping="yes">&lt;/START>&lt;/XML></xsl:text></TD><xsl:text> -->
<TD class="title">
<xsl:choose>
   <xsl:when test="scu:isMatch($title,$FAVCRIT/CRIT)">
      <A NAME="{generate-id()}"><FONT COLOR="#00FF00" SIZE="+1"><B><xsl:value-of select="$title" /></B></FONT></A>
   </xsl:when>
   <xsl:otherwise>
      <B><xsl:value-of select="$title" /></B>
   </xsl:otherwise>
   </xsl:choose>
 </TD><xsl:text>
</xsl:text><TD class="desc"><xsl:apply-templates select="sub-title" /><xsl:value-of select="desc" />
<xsl:if test="length">
 <xsl:text> (</xsl:text><xsl:value-of select="length"/><xsl:value-of select="substring(length/@units,1,3)"/>
 <xsl:text>)</xsl:text>
</xsl:if>
<xsl:text>
</xsl:text></TD>
</TR>
</xsl:template>

<xsl:template match="sub-title">
<xsl:text> </xsl:text><span class="episode" STYLE="color: blue"><xsl:value-of select="." /></span>.<xsl:text> </xsl:text>
</xsl:template>

<!-- Create the a favorite list entry for the specified list of programmes (from the xmltv file) -->
<!-- PROGS must be a list of programme elements to check for favorites -->
<xsl:template name="listfavs">
<xsl:param name="ST" />
<xsl:param name="PROGS" />
<xsl:for-each select="$PROGS">
<xsl:if test="scu:isMatch(title,$FAVCRIT/CRIT)">
<xsl:variable name="id" select="current()/@channel" />
	<FAV>
	   <PROG><xsl:value-of select="title" /></PROG>
<xsl:if test="sub-title">
       <EPISODE><xsl:value-of select="sub-title" /></EPISODE>
</xsl:if>
	   <START><xsl:value-of select="@start" /></START>
	   <CHANNEL><xsl:value-of select="$CHNREF/channel[@id=$id]/display-name" /></CHANNEL>
	   <IDX><xsl:value-of select="generate-id()" /></IDX>
	   <DOC><xsl:call-template name="makoutfile"><xsl:with-param name="chan" select="//channel[@id=current()/@channel]"/><xsl:with-param name="DATEL"><xsl:value-of select="$ST" /></xsl:with-param></xsl:call-template></DOC>
	   <SSREF>
	   	<xsl:call-template name="makeSShref">
	   		<xsl:with-param name="prog" select="current()"/>
	   		<xsl:with-param name="ssref" select="$CHNREF/channel[@id=$id]/ssref" />
		   </xsl:call-template>
		</SSREF>
		<DBREF>
	   	<xsl:call-template name="makeDBhref">
		   	<xsl:with-param name="prog" select="current()"/>
		   	<xsl:with-param name="dbref" select="$CHNREF/channel[@id=$id]/dbent" />
		   </xsl:call-template>
		</DBREF>
	</FAV>
</xsl:if>
</xsl:for-each>
</xsl:template>

<xsl:template name="makeSShref">
<xsl:param name="prog"/>
<xsl:param name="ssref" />
<xsl:if test="$ssref">
<!-- Add start and end offsets -->
<xsl:variable name="ostart" select="scu:addToDate($prog/@start, 'MINUTE', -5)" />
<xsl:variable name="oend" select="scu:addToDate($prog/@stop, 'MINUTE', 15)" />
<xsl:variable name="href">
<xsl:text>http://blackey/timer_new.html?submit=New+Timer</xsl:text>
<xsl:text>&amp;aktion=timer_add</xsl:text>
<xsl:text>&amp;source=timer_add</xsl:text>
<xsl:text>&amp;active=1</xsl:text>
<xsl:text>&amp;Aufnahmeaktion=0</xsl:text>
<xsl:text>&amp;Exitaktion=0</xsl:text>
<xsl:text>&amp;DisableAV=1</xsl:text>
<xsl:text>&amp;save=Save</xsl:text>
<xsl:text>&amp;do=new</xsl:text>
<xsl:text>&amp;referer=</xsl:text><xsl:value-of select="scu:urlencode('http://blackey/timer_list.html?aktion=timer_list')" />
<xsl:text>&amp;timer_id=</xsl:text>
<xsl:text>&amp;channel=</xsl:text><xsl:value-of select="scu:urlencode($ssref)" />
<xsl:text>&amp;dor=</xsl:text><xsl:value-of select="scu:urlencode(scu:formatDate($ostart, 'dd/MM/yyyy'))" />
<xsl:text>&amp;starth=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'HH')" />
<xsl:text>&amp;startm=</xsl:text><xsl:value-of select="scu:formatDate($ostart, 'mm')" />
<xsl:text>&amp;stoph=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'HH')" />
<xsl:text>&amp;stopm=</xsl:text><xsl:value-of select="scu:formatDate($oend, 'mm')" />
<!-- Java:Sat=1 SS:Mon=0 -->
<xsl:if test="$prog/sub-title">
<xsl:variable name="dayofweek" select="scu:formatDate($ostart, 'E')" />
<xsl:text>&amp;d</xsl:text>
<xsl:choose>
<xsl:when test='$dayofweek="Mon"'>
<xsl:text>0</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Tue"'>
<xsl:text>1</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Wed"'>
<xsl:text>2</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Thu"'>
<xsl:text>3</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Fri"'>
<xsl:text>4</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Sat"'>
<xsl:text>5</xsl:text>
</xsl:when>
<xsl:when test='$dayofweek="Sun"'>
<xsl:text>6</xsl:text>
</xsl:when>
</xsl:choose>
<xsl:text>=1</xsl:text>
</xsl:if>
<xsl:text>&amp;title=</xsl:text><xsl:value-of select="scu:urlencode($prog/title)" />
<xsl:if test="$prog/sub-title">
<xsl:variable name="event">
<xsl:text> </xsl:text><xsl:value-of select="scu:formatDate($ostart, 'MM')" />-<xsl:value-of select="scu:formatDate($ostart, 'dd')" /><xsl:text> </xsl:text><xsl:value-of select="$prog/sub-title" />
</xsl:variable>
<xsl:value-of select="scu:urlencode($event)" />
</xsl:if>
</xsl:variable>
<xsl:value-of select="$href" />
</xsl:if>
</xsl:template>


<xsl:template name="makeDBhref">
<xsl:param name="prog"/>
<xsl:param name="dbref" />
<xsl:if test="$dbref">
<!-- Seems the offsets get added automatically! -->
<xsl:variable name="ostart" select="scu:addToDate($prog/@start, 'MINUTE', 0)" />
<xsl:variable name="oend" select="scu:addToDate($prog/@stop, 'MINUTE', 0)" />
<xsl:variable name="pref"><xsl:value-of select="$dbref/dbname" />|<xsl:value-of select="$dbref/dbref" /></xsl:variable>
<xsl:variable name="href">
<xsl:text>http://dreambox/addtimer?action=record</xsl:text>
<xsl:text>&amp;passOn=deleteOld</xsl:text>
<xsl:text>&amp;command=mainpage</xsl:text>
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
<xsl:text>&amp;ref=</xsl:text><xsl:value-of select="scu:urlencode($pref)" />
<xsl:text>&amp;name=</xsl:text><xsl:value-of select="scu:urlencode($prog/title)" />
<xsl:if test="$prog/sub-title">
<xsl:variable name="event">
<xsl:text> </xsl:text><xsl:value-of select="scu:formatDate($ostart, 'MM')" />-<xsl:value-of select="scu:formatDate($ostart, 'dd')" /><xsl:text> </xsl:text><xsl:value-of select="$prog/sub-title" />
</xsl:variable>
<xsl:value-of select="scu:urlencode($event)" />
</xsl:if>
<xsl:text>&amp;descr=</xsl:text>
<xsl:text>&amp;after_event=nothing</xsl:text>
<xsl:text>&amp;send=</xsl:text><xsl:value-of select="scu:urlencode('Add/Save')" />
<xsl:text>&amp;useMargin=1</xsl:text>
<xsl:text>&amp;debug=0</xsl:text>
<!-- xsl:text>&amp;save=1</xsl:text -->
</xsl:variable>
<xsl:value-of select="$href" />
</xsl:if>
</xsl:template>


<xsl:template name="addDBlink">
<xsl:param name="href"/>
<xsl:if test="normalize-space($href)">
<xsl:element name="A">
<xsl:attribute name="CLASS">dbx</xsl:attribute>
<xsl:attribute name="TARGET">dbxtimers</xsl:attribute>
<xsl:attribute name="HREF"><xsl:value-of select="$href" /></xsl:attribute>
<img hspace="2" alt="Add to Dreambox timers" border="0" src="../blueball.gif" />
</xsl:element>
</xsl:if>
</xsl:template>


<xsl:template name="addSSlink">
<xsl:param name="href"/>
<xsl:if test="normalize-space($href)">
<xsl:element name="A">
	<xsl:attribute name="CLASS">dbx</xsl:attribute>
	<xsl:attribute name="TARGET">sstimers</xsl:attribute>
	<xsl:attribute name="HREF"><xsl:value-of select="$href" /></xsl:attribute>s</xsl:element>
</xsl:if>
</xsl:template>

<!-- This is the default template which matches any text. It is a built in template
     but I've included it here to remind me how to modify the default

<xsl:template match="text()">
   <xsl:value-of select="." />
</xsl:template>
  -->

</xsl:stylesheet>