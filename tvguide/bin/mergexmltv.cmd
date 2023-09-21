TITLE Merge TV Guides
rem java -Xmx512M -jar HTMLMaker.jar -merge "..\xml\mergexmltv.xmltv" "..\xml\tvguide.xmltv" "..\xml\xmltv_be.xml"

rem TVGuide extends further into the future but misses the episode info. EPG usually has the episode info.
rem TVGuide is the best reference providing the episode info can be pulled from the EPG but this has
rem been failing lately (Mar 2023) due to mismatching times. 
rem The combine algorithm has now (Apr 2023) been updated to find the programs in the epg data
rem based on day and occurrence if there is no exact start time match.
rem If the result is still series without episodes then go back to using the epg as reference.
rem 26 Aug 2023 UK listings suddenly stopped appearing - right before forking holiday. So go back to using epg
rem as reference until I get back from holiday and can find time to investigate. Hmmm looks like tvguide is dead - 
rem there are no programmes after tomorrow 29/8/2023!!
rem Damn! The epg is only for BE progs at the moment!
java -Xmx512M -jar HTMLMaker.jar -combine -ref "..\xml\xmltv_tvg_be.xml" -alt "..\xml\xmltv_epg.xml" -res "..\xml\xmltv_be.xml"
rem java -Xmx512M -jar HTMLMaker.jar -combine -alt "..\xml\xmltv_tvg_be.xml" -ref "..\xml\xmltv_epg.xml" -res "..\xml\xmltv_be.xml"

rem Kludge to try to get the episode info for the current day form tvguide but use the epg for the remaining days
java -Xmx512M -jar HTMLMaker.jar -combine -alt "..\xml\xmltv_gb.xml" -ref "..\xml\xmltv_epg.xml" -res "..\xml\xmltv_gb_m.xml"

rem Second run of HTMLMaker overwrites the combine log which makes troubleshooting difficult. Until I get
rem round to fixing it in the code this will have to do
copy /Y HTMLMaker.log HTMLMaker_combine.log 

rem 31 Auf 2023 UK tvguide is broken. Added the UK channels to the Ultimo TVV bouquet so the progs appear
rem in xmltv_epg.xml. Lets try replacing xmltv_gb with xmltv_epg even though it also contains the BE channels from
rem java -Xmx512M -jar HTMLMaker.jar -merge "..\xml\mergexmltv.xmltv" "..\xml\xmltv_gb.xml" "..\xml\xmltv_be.xml"
java -Xmx512M -jar HTMLMaker.jar -merge "..\xml\mergexmltv.xmltv" "..\xml\xmltv_gb_m.xml" "..\xml\xmltv_be.xml"
