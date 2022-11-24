<?php
// TODO put the function is a separate file and include it. This will make testing
// easier to do as it will avoid the need to read posted HTML data.
// NB logger.php is missing from the test data and not present in the PHP installation. (Where is it??)
include_once 'logger.php';
$nforepodir = '../tv/nfo/';  // This is not visible to the function!!

function createnfo($nfojson, $nfodir)
{
$filename = "";
$nfoxml = "";
   // decode the json data
   //  echo "JSON: " . $nfojson . "\n"; 
   $meta = json_decode($nfojson);
   // var_dump($meta);

$nfoxml = '<?xml version="1.0" encoding="UTF-8"?><episodedetails>' . "\n";
$nfoxml = $nfoxml . '<title>' . $meta->episode->title . '</title>' . "\n";
$nfoxml = $nfoxml . '<showtitle>' . $meta->episode->show . '</showtitle>' . "\n";
$nfoxml = $nfoxml . '<season>' . $meta->episode->season . '</season>' . "\n";
$nfoxml = $nfoxml . '<episode>' . $meta->episode->number . '</episode>' . "\n";
$nfoxml = $nfoxml . '<plot>' . $meta->plot . '</plot>' . "\n";
$nfoxml = $nfoxml . '<uniqueid type="mytvshows" default="true">' . $meta->episode->uid . '</uniqueid>' . "\n";
$nfoxml = $nfoxml . '<aired>20' . $meta->episode->aired . '</aired>' . "\n";
$nfoxml = $nfoxml . '</episodedetails>' . "\n";



   $filename = $nfodir . $meta->episode->recname . ".nfo";

   if(!($handle = fopen($filename, "w")))
   {
      //echo "Failed to open $filename<br>";
      return 0;
   }
   fwrite($handle, $nfoxml);
   fclose($handle);
   Logger::log(Lvl::INFO, "createnfo.php", "NFO written to $filename: $nfoxml\n");
   echo "NFO written to $filename";
}

Logger::log(Lvl::INFO, "createnfo.php", "Entry");
// Get the JSON contents
$nfodata = file_get_contents('php://input');
Logger::log(Lvl::INFO, "createnfo.php", "POST data: $nfodata");

   createnfo($nfodata, $nforepodir);
   
   // How to return something?
   // echo "A-OK";
   
?>