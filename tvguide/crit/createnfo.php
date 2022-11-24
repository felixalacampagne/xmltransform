<?php
include_once 'logger.php';
include_once 'nfofns.php';
include_once 'scufns.php';

Logger::log(Lvl::DEBUG, "createnfo.php", "Entry");
// Get the JSON contents
$nfodata = file_get_contents('php://input');
Logger::log(Lvl::DEBUG, "createnfo.php", "POST data: $nfodata");

   $caller = getSrvVal('HTTP_REFERER');

   $nforepodir = getNfoDir($caller);  
   createnfo($nfodata, $nforepodir);

   
?>