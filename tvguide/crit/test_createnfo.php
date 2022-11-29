<?php
// TODO put the function is a separate file and include it. This will make testing
// easier to do as it will avoid the need to read posted HTML data.
// NB logger.php is missing from the test data and not present in the PHP installation. (Where is it??)

include_once 'nfofns.php';
$nforepodir = './';  // This is not visible to the function!!

Logger::log(Lvl::DEBUG, "createnfo.php", "Entry");
// Get the JSON contents
$nfodata = '{"episode":{"show":"Critical Incident","season":"3","number":"12","title":"Episode 12","uid":"09637a9ac5699c7149811ee070fef76f","aired":"22-11-24","recname":"Critical Incident 22-11-24 3x12 Episode 12"}, "plot":"A Warwickshire police officer who used his medical training to save the lives of two stab victims and a mountain bike paramedic violently attacked in Manchester city centre."}';
$url = 'https://hostname/dir1/dir2/tv-nfo/bbc1_mon.html';

   $nforepodir = getNfoDir($url);
   echo $nforepodir;

   // createnfo($nfodata, $nforepodir);

   
?>