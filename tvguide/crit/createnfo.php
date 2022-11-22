<?php
include "scufns.php";
$filename = "test.nfo";

Logger::log(Lvl::INFO, "createnfo.php", "Entry");
// Get the JSON contents
$nfodata = file_get_contents('php://input');
Logger::log(Lvl::INFO, "createnfo.php", "POST data: $nfodata");

// decode the json data
//$data = json_decode($json);
   if(!($handle = fopen($filename, "w")))
   {
      //echo "Failed to open $filename<br>";
      return 0;
   }
   fwrite($handle, $nfodata);
   fclose($handle);
   Logger::log(Lvl::INFO, "createnfo.php", "POST data: $nfodata: NFO written");
   
   // How to return something?
   echo "A-OK";
?>