# xmltransform

Generate static HTML TV listing pages from an XMLTV source file using XSLT. The static pages are populated with links to set timers on my OpenPli based
satellite boxes (confgured with a personalised version of the web interface). A summary page of 'favourite' programmes is generated based on a list of 
regular expression. A similar page listing new series starting is also generated. The Java code provides additional helper methods in the form of XSLT extensions
to assist in the parsing and formatting. These XSLT Extensions require the use of the Xalan parser, I think.

The generation of static HTML pages rather than dynamic creation from a database of entries or directly from the XMLTV was chosen for 
performance reasons - the code has been in use since around 2010 with minimal changes mainly to accomodate changing TV listing grabbers and changes to the XMLTV
format.

Recently creation of NFO files for the programmes listed in the favourites page has been introduced since programmes recorded on one of the satellite
boxes no longer have an accompanying EIT file. Since this box dates from 2008 and is no longer supported by OpenPli there is no chance that this issue will
be fixed so an alternate solution was required for gathering the programme description.

# GitHub Patches

Finally figured out how to get 'github.dev' to import changes from a diff file created on my local system with no internet access. Needless to say the documentation for this is none existant and apparently no one else in the whole world actually does this judging by the lack of any useful responses to my Google queries. Anyway, here is the command used to create the diff file which I just used to successfully import the pom changes for building both the webserver and command line runnable jars:

git diff --no-prefix > diff.diff

This was done BEFORE the changes were committed to the local repo. To make a diff relative to 'main' once the changes have been committed to a new branch is something like:

git diff --no-prefix main new-branch > diff.diff

or something similar. The first version is likely to be most useful for me.

Test
