<?php
include "scufns.php";





$crit = getReqValue('CRIT');
$npage = getReqValue('NPAGE');

# Might need to find a way to prevent multiple messages per IP from being added
# to prevent malicious entries from being made
# $ip = "unknownIP";


$rc = 0;


if(($crit!="") && ($npage!=""))
{
   $rc = addCrit($crit);
}
//echo "Return from addGuestBookEntry = $rc";
	header("Location: http://" . $_SERVER['HTTP_HOST']
                  . dirname($_SERVER['PHP_SELF'])
                  . "/" . $npage);
exit;

?>