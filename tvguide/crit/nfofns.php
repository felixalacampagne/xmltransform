<?php

include_once 'logger.php';

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
      Logger::log(Lvl::INFO, "createnfo", "Failed to write NFO to $filename\n");
      echo "FAIL";
      return 0;
   }
   fwrite($handle, $nfoxml);
   fclose($handle);
   Logger::log(Lvl::INFO, "createnfo", "NFO written to $filename: $nfoxml\n");
   echo "SUCCESS: NFO written to $filename";
}

// For a path, eg. https://hostname/dir1/dir2/filename.ext return the parent name, eg. 'dir2'
function getParentDir($url)
{
   $path = parse_url($url, PHP_URL_PATH);
   // Remove the filename
   $path = dirname($path);
   
   // get the last part which should be the tv directory
   $path = basename($path);
   
   return $path;
}

// returns the directory to write NFOs to. Assumes that a
// path relative to the directory containing the script can be used.
function getNfoDir($caller)
{
   $tvdir = getParentDir($caller);
   $nfodir = '../' . $tvdir . '/nfo/';
   return $nfodir;
}
?>