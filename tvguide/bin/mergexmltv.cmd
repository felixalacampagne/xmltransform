java -Xmx512M -jar HTMLMaker.jar -combine -alt "..\xml\xmltv_tvg_be.xml" -ref "..\xml\xmltv_epg.xml" -res "..\xml\xmltv_be.xml"
java -Xmx512M -jar HTMLMaker.jar -merge "..\xml\mergexmltv.xmltv" "..\xml\xmltv_gb.xml" "..\xml\xmltv_be.xml"
