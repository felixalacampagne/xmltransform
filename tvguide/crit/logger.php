<?php
abstract class Lvl
{
   const DEBUG   = 1; 
   const INFO    = 2;
   const WARNING = 3;
   const ERROR   = 4;
   const FATAL   = 5;

}

class Logger
{
private static $logger = null; # = new Logger();

private $logname = "C:\\Development\\Apache24\\logs\\tvguide.log";//"C:\\Development\\workspace_test\\script\\accountAPI\\acc2003.mdb";
private $fh = null;

public static function log($level, $src, $msg)
{
   
   if(self::$logger == null)
   {
      self::$logger = new Logger();
   }
   
   # Tried using date but it causes Warning messages about some nonesense I don't care about
   # and which will screw up the API HTTP response
   # $date = new DateTime();
   # $op = $date->format("ymdHis") . " " . $level . ": " . $msg . "\n";
   # Forking DateTime does the same shirt!
   self::$logger->logMsg($level, $src, $msg);

}

public function __construct()
{
   # This seems to magically get rid of the assinine warnings which otherwise pollute the output.
   date_default_timezone_set(@date_default_timezone_get());
   $this->init();
}

public function  __destruct ()
{
   if($this->fh != null)
   {
      fclose($this->fh);  
      $this->fh = null;
   }
}

public function init()
{
   if($this->fh == null)
   {
      $this->fh = fopen($this->logname, "a");
      $this->logMsg(Lvl::DEBUG, "Logger.init", "Log file opened: " . $this->logname);
   }
}
private function logMsg($lvl, $src, $msg)
{
   $this->writeToLog($this->formatMsg($lvl, $src, $msg));
}

private function formatMsg($lvl, $src, $msg)
{
   # Tried using date but it causes Warning messages about some nonesense I don't care about
   # and which will screw up the API HTTP response
   # $date = new DateTime();
   # $op = $date->format("ymdHis") . " " . $level . ": " . $msg . "\n";
   # Forking DateTime does the same shirt!
$level = "";
   switch($lvl)
   {
      case Lvl::DEBUG:    $level = "DEBUG"   ; break;
      case Lvl::INFO;     $level = "INFO"    ; break;
      case Lvl::WARNING;  $level = "WARNING" ; break;
      case Lvl::ERROR;    $level = "ERROR"   ; break;
      case Lvl::FATAL;    $level = "FATAL"   ; break;
   }
   
   $op = date("y-m-d H:i:s") . " " . sprintf("%-7s", $level) . " (" . $src . ") " . $msg . "\n";
   return $op;
}

private function writeToLog($msg)
{
   fwrite($this->fh, $msg);
}

}
?>