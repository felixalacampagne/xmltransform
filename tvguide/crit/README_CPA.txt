Problems for PHP5

   php.ini must have a full path set for the variable "extension_dir" when using apache.
   'extension=php_xsl.dll' line in php.ini must be uncommented to use XSLTProcessor
   
   Apache 2.4 related: Access to all folders requires a user name by default. Therefore it must
   be disabled for every (root) folder. The entry which forces passwords is "Require all denied". The
   entry which cancels the requirement for passwords is "Require all granted"


Problems encountered getting PHP4 XML extensions working on Win7 Ult system.

Fatal error: Call to undefined function: domxml_open_mem() in 

	Occurs when clicking the Favourite icon in the tv listings.
	The "extension_dir" and "extension=php_domxml.dll" settings are correct in PHP.ini
	PATH is already updated with php install directory.
	(Same configuration works on WinXP system)
	
	Copy ICONV.DLL from the dlls directory to the root of the php installation (eg. C:\PHP4)
		For some unknown reason PHP does not look in the dlls directory. In fact the WinXP installation
		had iconv.dll in the system32 directory so I must have had a similar problem when setting up php
		on the WinXP system.
		
Fatal error: Call to undefined function: xslt_create() in 

	Occurs when the link to the Favourites page is clicked or when the heart icon is clicked
	after the domxml_open_mem problem is fixed.
	
	Use DEPENDS.EXE to figure out which other .dlls are needed for extensions/php_xslt.dll (SABLOT.DLL and EXPAT.DLL).
	Chances are the required files are in the dlls directory. Copy them into the root of the php installation and 
	restart the server. Should work OK now.		 
	
