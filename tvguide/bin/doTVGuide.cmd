TITLE Generating Chris TV Guide
java -Xmx512M -jar HTMLMaker.jar -xml "..\xml\mergexmltv.xmltv" -xsl "..\bin\tvguide.xsl" -xfrm "00favorites.htm" -out "..\tv" -fav "..\crit\favcrit.xml"
