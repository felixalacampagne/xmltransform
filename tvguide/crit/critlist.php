<?php
include "scufns.php";

//
//     NOTE!!!!!!!!!!
//     PHP.INI file has to go into the Windows directory for Apache to load it!
//
?>

<html>
<head>
   <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<!-- English -->   
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="../stylesheet.css?1810" rel="stylesheet" type="text/css">
<title>Chris' Favorite TV Programs Search List</title>
</head>
   <body>
<FORM action="addcrit.php" method="get">
	<table>
   	<tr><td>New crit:</td><td><INPUT class="input" size="60" TYPE="text" VALUE="" NAME="CRIT"></td>
    	<td></td><td><INPUT TYPE="submit" VALUE="Add" NAME="SEND"></td></tr>
   </table>
   <INPUT TYPE="hidden" VALUE="critlist.php" NAME="NPAGE">
</FORM>
<P>
   <table>
      <tr>
         <td><input id="btnGenerate" type="button" value="Re-Generate TV pages" onclick="doStart();" /></td>
         <td><div class="genprog" id="progress"></div></td>
   </tr>
  </table>
<h2>The list of favourite criteria</h2>

<?php
   $filename = FCXML;
   $contents = "";

   if(($handle = @fopen($filename, "r")))
   {
      $contents = fread($handle, filesize($filename));
      fclose($handle);
   }

	// phpinfo();
   $html = xmlfile2html(FCXML, FCXSL);
   echo $html;
   
?>
<script>
var progress = document.getElementById('progress');
var btngen = document.getElementById('btnGenerate');

// Unbelievable - the bloody .get calls are cached but it doesn't seem to be the case for .post
function doPoll(){

    $.post("/pagebuilderrest/ap/api/status" ,  "",
       function(data) 
       {
         if(data == "STOPPED")
         {
            var date = new Date().toTimeString().substr(0, 8)
             var content = document.createTextNode("Pages generated at " + date);
             progress.innerText = "";
             progress.appendChild(content);
             //btngen.style.display = "inline";
             btngen.disabled = false;
         }
         else
         {
            var content = document.createTextNode("*");
            progress.appendChild(content);
            setTimeout(doPoll, 1000);
         }
       });
}
function doStart()
{
	//alert("start: post Starting pagebuilder");

    $.post("/pagebuilderrest/ap/api/start" ,  "",
       function(data) 
       {
         if(data == "STOPPED")
         {
        	    // alert("start: received " + data);
        	    // Can't find a way to set the div content to the textnode - can only append it to existing content
             // var content = document.createTextNode("Pagebuilder did not start!");
             // progress.appendChild(content);
             progress.innerText = "Pagebuilder failed to start:" + data;
         }
         else
         {
            //btngen.style.display = "none";
            btngen.disabled = true;
        	   //alert("start: Starting poll");
        	   // No way to set the text of an element to a text node, can only append
            progress.innerText = "";
        	   var content = document.createTextNode("Generating pages: ");
        	   progress.appendChild(content);
        	   
            setTimeout(doPoll, 1000);
         }

       });
}



//document.getElementById("clickMe").onclick = doStart;

</script> 
   </body>
</html>