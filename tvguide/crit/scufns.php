<?php
include_once 'logger.php';
// See README_CPA for extension loading problems
define("ROOTPATH", "file://C:/Development/website/tvguide/crit/");
define("FCXML", "favcrit.xml");
define("FCXSL", "critlist.xsl");
define("ALLOWEDTAGS", '<a><em><strong><b><i><img>');
define("STRIPATTRIB", 'javascript:|onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup|style|class|id');

function getReqValue($varname)
{
$retval = "";
if(array_key_exists($varname, $_REQUEST))
{
	$retval = $_REQUEST[$varname];
}
return $retval;
}
function getSrvVal($srvvarname)
{
$retval = "";
   if(array_key_exists($srvvarname, $_SERVER))
	{
		$retval = $_SERVER[$srvvarname];
	}

	return $retval;
}
function getLang()
{
   if(array_key_exists('lang', $_COOKIE))
   {
      $lang = $_COOKIE['lang'];
      $lang = strtolower($lang);
   }
   switch($lang)
   {
   case "nl":
      break;
   case "fr":
      break;
   case "en":
      break;
   default:
      $lang = "fr";
      break;
   }
   return $lang;
}

function  buildGuestBook()
{
   return transformGB();
}

function  buildHTMLGuestBook()
{

$entries = "";
$filename = GBDB;
if(($handle = @fopen($filename, "r")))
{
   $entries = fread($handle, filesize($filename));
   fclose($handle);
}

if($entries == "")
{
   $entries = "No entries made yet!";
}
return $entries;
}

function addChildNode($name, $value, $rootElement, $domdoc)
{
  //$encvalue = htmlentities($value, ENT_COMPAT, ISO-8859-1);
  //$childNode = $domdoc->createElement($name, $value);
  //$rootElement->appendChild($childNode);
  
  // PHP4
  //$childNode = $domdoc->create_element($name);
  //$textnode = $domdoc->create_text_node($value);
  //$childNode->append_child($textnode);
  //$rootElement->append_child($childNode);

  $childNode = $domdoc->createElement($name);
  $textnode = $domdoc->createTextNode($value);
  $childNode->appendChild($textnode);
  $rootElement->appendChild($childNode);

}

// returns a domdoc object
function loadCritXML($filename)
{
   if(($handle = @fopen($filename, "r")))
   {
      $contents = fread($handle, filesize($filename));
      fclose($handle);
   }
   if($contents == "")
   {
      $contents = '<?xml version="1.0" encoding="ISO-8859-1"?>' . "\n";
      $contents .= '<CRITS>' . "\n";
      $contents .= '</CRITS>' . "\n";
   }

   // PHP4
   //$domdoc = domxml_open_mem($contents);

   $domdoc = new DOMDocument();
   $domdoc->loadXML($contents);

   $domdoc->formatOutput = true;
	return $domdoc;	
}

// Returns:  0 Failure (probably to do with a file)
//           1 OK
//           2 Owner has tried to make too many updates
function addXMLDOMCrit($crit)
{
   // get contents of a file into a string
   //$filename = GBDB;
   $filename = FCXML;

	$domdoc = loadCritXML($filename);

   //$crits = $domdoc->get_elements_by_tagname("CRITS");
   $crits = $domdoc->getElementsByTagName("CRITS");
   
   //echo "Length of crits is: " . $crits->length;
   addChildNode("CRIT", $crit, $crits->item(0), $domdoc);

	saveCritXML($domdoc, $filename);
   return 1;

}

function delXMLDOMCrit($crit)
{
   $filename = FCXML;
   $domdoc = loadCritXML($filename);
   $xpath = new DomXPath($domdoc);
   Logger::log(Lvl::DEBUG, "scufns.delXMLDOMCrit", "Looking for CRIT = [" . $crit . "]");

   // NB. There is no way to encode a quote in a quote delimited predicate, or a double-quote in 
   // a double-quote delimited predicate. So the query text cannot contain both quotes and double-quotes.
   // To avoid a whole heap of shirt I've decided to NOT support deleting CRITs which contain double-quotes.
   // In practice I have never used a CRIT containing double-quotes, whereas there are many instances of
   // CRITs with a single quote (aka apostrophy!!), eg. Grey's Anaotomy, The Handmaid's Tale.

   $query = "/CRITS/CRIT[.=\"" . $crit . "\"]";
   echo "XPath query: " . $query;
   $nodelist = $xpath->query($query);
   var_dump($nodelist);

   if(($nodelist!= FALSE) && ($nodelist->length > 0))
   {
      Logger::log(Lvl::DEBUG, "scufns.delXMLDOMCrit", "Found " . $nodelist->length . " CRITs = [" . $crit . "]");
      for($i=0; $i < $nodelist->length; $i++)
      {
         $node = $nodelist->item($i);
         $parent = $node->parentNode;
         Logger::log(Lvl::DEBUG, "scufns.delXMLDOMCrit", "Removing matching CRIT[" . $i ."]");
         $parent->removeChild($node);
      }
      Logger::log(Lvl::DEBUG, "scufns.delXMLDOMCrit", "Saving updated crit file: " . $filename);
      saveCritXML($domdoc, $filename);
   }
   else
   {
      Logger::log(Lvl::INFO, "scufns.delXMLDOMCrit", "Failed to find CRIT = [" . $crit . "]");
   }
   return 1;
}


function saveCritXML($domdoc, $filename)
{
   // Even though formatted output is selected - the new stuff always
   // appears on the same line as everything else. So crudely put a line
   // break between each entry
   //$contents = $domdoc->dump_mem(true); // PHP4
   $contents = $domdoc->saveXML();
   if($contents)
   {
      $contents = preg_replace('/\n\n/', "", $contents);
      $contents = preg_replace('/\<\/CRIT\>\<CRIT\>/', "</CRIT>\n<CRIT>", $contents);
      $contents = preg_replace('/\<CRITS\>\<CRIT\>/', "<CRITS>\n<CRIT>", $contents);

      if($contents != "")
      {
         //echo $contents;
         if(!($handle = fopen($filename, "w")))
         {
            //echo "Failed to open $filename<br>";
            return 0;
         }
         fwrite($handle, $contents);
         fclose($handle);
      }
   }
	
}

function xml2htmlPHP4($xmldata, $xsl)
{
   $arguments = array('/_xml' => $xmldata);
   $xsltproc = xslt_create();
   xslt_set_encoding($xsltproc, 'ISO-8859-1');
   $html =
       xslt_process($xsltproc, 'arg:/_xml', "file://$xsl", NULL, $arguments);

   if (empty($html)) {
      die('XSLT processing error: '. xslt_error($xsltproc));
   }
   xslt_free($xsltproc);
   return $html;
}

function xml2html($xmldata, $xsl)
{
   $arguments = array('/_xml' => $xmldata);
   $xsltproc = new XSLTProcessor();
   $dom = new DOMDocument();
   $dom->load($xsl);
   $xsl->importStyleSheet($dom);
   
   $xmldoc = new DOMDocument();
   if($xmldoc->loadXML($xmldata))
   {
      $html = $xsl->transformToXML($xmldoc);
   }   
   $html =
       xslt_process($xsltproc, 'arg:/_xml', "file://$xsl", NULL, $arguments);

   if (empty($html)) {
      die('XSLT processing error: '. xslt_error($xsltproc));
   }

   return $html;
}

function xmlfile2html($xmlfile, $xslfile)
{
   $xsltproc = new XSLTProcessor();
   $dom = new DOMDocument();
   $dom->load($xslfile);
   $xsltproc->importStyleSheet($dom);
   
   $xmldoc = new DOMDocument();
   if($xmldoc->load($xmlfile))
   {
      $html = $xsltproc->transformToXML($xmldoc);
   }   

   if (empty($html)) {
      die('XSLT processing error');
   }
   return $html;
}
function xmlfile2htmlPHP4($xmlfile, $xslfile)
{
   $xsltproc = xslt_create();
   xslt_set_encoding($xsltproc, 'ISO-8859-1');

   $html = xslt_process($xsltproc, ROOTPATH . $xmlfile, ROOTPATH . $xslfile);

   if (empty($html)) {
      die('XSLT processing error: '. xslt_error($xsltproc));
   }
   xslt_free($xsltproc);
   return $html;
}

function transformGB_PHP5()
{
   $xsl_filename = GBXSL;
   $xml_filename = GBXML;
   $result = FALSE;
   if (!file_exists($xml_filename))
   {
      $result = "No entries have been made yet!";
      return $result;
   }

   // This needed the 'extension=php_xsl.dll' line in php.ini to be uncommented
   $xsl = new XSLTProcessor();
   $xsl->importStyleSheet(DOMDocument::load($xsl_filename));

   $xmldoc = new DOMDocument;
   if(@$xmldoc->load($xml_filename))
   {
      $result = $xsl->transformToXML($xmldoc);
   }

   if (!$result)
   {
      $result = "Sorry, the guestbook is temporarliy unavailable.<BR>\n";
      $result .= "\n" . xslt_error($xsltproc);
   }
   elseif(empty($result))
   {
      $result = "No entries have been made yet!";
   }
   else
   {
      // Could grow old trying to stop the <p>'s etc. being converted to &lt;p etc. so simply
      // do the replacement here.
      $result = str_replace('&amp;', '&', $result);
      $result = str_replace(array('&amp;', '&lt;p&gt;', '&lt;/p&gt;', '&lt;br /&gt;'), array('&', '<p>', '</p>', '<br />'), $result);
   }
   xslt_free($xsltproc);
   return $result;
}

function addXMLGBEntry($oname, $message, $catname, $lang, $ip)
{
   // get contents of a file into a string
   //$filename = GBDB;
   $filename = GBXML;
   $contents = "";
   $dateentry = date("d-m-Y");
   if(($handle = @fopen($filename, "r")))
   {
      $contents = fread($handle, filesize($filename));
      fclose($handle);
   }
   if($contents == "")
   {
      $contents = '<?xml version="1.0" encoding="UTF-8"?>' . "\n";
      $contents .= '<GBEntries>' . "\n";
      $contents .= '</GBEntries>' . "\n";
   }


   // Arbitrarily one entry per owner
   //if(preg_match("/$oname/i", $contents))
   //{
   //   return;
   //}

   $newentry = "<GBEntry>\n";
   $newentry .= "  <Owner>" . $oname . "</Owner>\n";
   if($catname != "")
   {
      $newentry .= "  <Cat>" . $catname . "</Cat>";
   }
   $newentry .= "  <Message>" . $message . "</Message>\n";
   $newentry .= "  <Date>" . $dateentry . "</Date>\n";
   $newentry .= "</GBEntry>";

   $pattern = "/(^.*\<GBEntries\>)(.*$)/s";
   $replace = "\\1\n$newentry\\2";

   $contents = preg_replace($pattern, $replace, $contents);

   if(!($handle = fopen($filename, "w")))
   {
      echo "Failed to open $filename<br>";
      return 0;
   }
   fwrite($handle, $contents);
   fclose($handle);
   return 1;
}

function addHTMLGuestBookEntry($oname, $message, $catname, $lang, $ip)
{
   // get contents of a file into a string
   $filename = GBDB;
   $contents = "";
   $dateentry = date("d-m-Y");
   if(($handle = @fopen($filename, "r")))
   {
      $contents = fread($handle, filesize($filename));
      fclose($handle);
   }

   // Arbitrarily one entry per owner
   //if(preg_match("/$oname/i", $contents))
   //{
   //   return;
   //}

   $newentry = "<TR><TD>$message<BR>";
   $newentry .= "<FONT SIZE=1>";
   $newentry .= "$oname";
   if($catname != "")
   {
      $newentry .= " ($catname)";
   }
   $newentry .= ", $dateentry</FONT>";
   $newentry .= "</TD><TR>\n";

   $contents = $newentry . $contents;

   if(!($handle = fopen($filename, "w")))
   {
      echo "Failed to open $filename<br>";
      return 0;
   }
   fwrite($handle, $contents);
   fclose($handle);
   return 1;
}

// joanna's guestbook stuff
function removeEvilTags($source)
{
   $source = strip_tags($source, ALLOWEDTAGS);
   
   // Keep getting warnings about /e and preg_replace being deprecated. This is the
   // only place I can see anything like a /e. Apparently it means execute the 
   // replacement string as PHP code with the match group expanded. So what it is
   // doing appears to be to replace certain tags with the word 'forbidden' and allow
   // others through.
   // Couldn't be bothered to try and reimplement this so will settle for replacing
   // all tags - no reason to have tags anyway, and this is code only used by
   // FALC, which will retire soon! Although maybe I should massage the values used in
   // the AccountAPI code in a similar way....
      
   //return preg_replace('/<(.*?)>/ie', "'<'.removeEvilAttributes('\\1').'>'", $source);
   return preg_replace('/(<.*?>)/i', "html-forbidden", $source);
}

//function removeEvilAttributes($tagSource)
//{
//   return stripslashes(preg_replace("/" . STRIPATTRIB . "/i", 'forbidden', $tagSource));
//}

// End joanna's guestbook stuff

function makeSafe($str)
{
   $str = stripslashes($str);
   $str = removeEvilTags($str);

   // Converts everything so it will display as written: probably means joanna's functions
   // are not required.
   $str = htmlentities($str);

   $str = wordwrap($str, 70);

   // NB Using just <p> wont result in well-formed XML except that the < and >
   // are escaped when the XML string is created. Browsers generally don't require
   // the closing </p> so it shouldn't be a problem. Blindly enclosing the whole
   // string in <p></p> does cause a problem as string which need to appear in-line
   // suddenly show up on their very own lines!!!
   $str = str_replace(array('&', "\r\n\r\n"), array('&amp;', '<p>'), $str);
   $str = str_replace(array('&amp;gt;', '&amp;lt;', "\r\n"), array('&gt;', '&lt;', '<br />'), $str);
   return $str;
}

function addCrit($crit)
{
$rc = 0;

   $crit = substr($crit, 0, 200);
   $crit = makeSafe($crit);

   $rc = addXMLDOMCrit($crit);

   return;
}

function delCrit($crit)
{
	$rc = delXMLDOMCrit($crit);
}
// This is the PHP 4 way of doing a transform. It only seems to work
// if the files are loaded into variables - attempts to use the
// filenames, which is supposed to be the way it works, resulted
// in a non-well formed error.
// Can't use this on live FalC as the extension is not loaded.
function transformGB_PHP4()
{
   $xsl_filename = GBXSL;
   $xml_filename = GBXML;
   $result = FALSE;
   if (!file_exists($xml_filename))
   {
      $result = "No entries have been made yet!";
      return $result;
   }


   $xml = file_get_contents($xml_filename) ;
   $xsl = file_get_contents($xsl_filename) ;
   $arguments = array ('/_xml' => $xml, '/_xsl' => $xsl );
   $xsltproc = xslt_create();
   $result = xslt_process($xsltproc, 'arg:/_xml', 'arg:/_xsl', NULL, $arguments);

   if (!$result)
   {
      $result = "Sorry, the guestbook is temporarliy unavailable.<BR>\n";
      $result .= "\n" . xslt_error($xsltproc);
   }
   elseif(empty($result))
   {
      $result = "No entries have been made yet!";
   }
   else
   {
      // Could grow old trying to stop the <p>'s etc. being converted to &lt;p etc. so simply
      // do the replacement here.
      $result = str_replace('&amp;', '&', $result);
      $result = str_replace(array('&amp;', '&lt;p&gt;', '&lt;/p&gt;', '&lt;br /&gt;'), array('&', '<p>', '</p>', '<br />'), $result);
   }
   xslt_free($xsltproc);
   return $result;
}

?>