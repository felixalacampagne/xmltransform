<?php
include "scufns.php";

//
//     NOTE!!!!!!!!!!
//     PHP.INI file has to go into the Windows directory for Apache to load it!
//
?>

<html>
<head>
<!-- English -->   
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="../stylesheet.css" rel="stylesheet" type="text/css">
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

   <h2>The list</h2>

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
   </body>
</html>