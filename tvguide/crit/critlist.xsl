<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 
version="1.0">
<xsl:output method="html" version="4.0"/>
<xsl:preserve-space elements="*"/>

<xsl:template match="/">
	<TABLE>
	<xsl:apply-templates select="//CRIT">
		<xsl:sort select="translate(text(),'^*.\\$[]','')" order="ascending" /> 
		<!-- Need custom sort to ignore regexp characters 
		Aahha, no we don't since we can do it with translate!!
		-->
		
	</xsl:apply-templates>
	</TABLE>
</xsl:template>

<xsl:template match="CRIT">
<xsl:variable name="crit" select="." />
<!-- This doesn't work, special chars in CRIT need to be urlencoded -->
<TR><TD class="fav"><xsl:value-of select="." /></TD><TD class="fav"><A class="fav" HREF="delCrit.php?CRIT={$crit}&amp;NPAGE=critlist.php">del</A></TD> <!-- Eventually a delete would be useful! -->
</TR>
</xsl:template>



<!-- This is the default template which matches any text. It is a built in template
     but I've included it here to remind me how to modify the default

<xsl:template match="text()">
   <xsl:value-of select="." />
</xsl:template>
  -->

</xsl:stylesheet>