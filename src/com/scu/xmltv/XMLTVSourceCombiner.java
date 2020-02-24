package com.scu.xmltv;

import java.io.File;

import org.w3c.dom.Document;

import com.scu.utils.XMLTransform;

/**
 * Takes a 'reference' file and updates a missing field from an
 * 'alternative' source (I couldn't think of a better name!)
 * 
 * The initial use case is as follows:
 * tvgrabnl does a good job of getting descriptions of programmes etc.
 * but recently it has not been getting the episode info. The episode info
 * seems to be reliably available from the VUUltimo EPG, but the 
 * descriptions are pretty poor. The aim is to take use the tvgrabnl 
 * file as the source and fill in missing episode infos from the
 * epg file.
 * @author Chris
 *
 */
public class XMLTVSourceCombiner
{
private final File refXMLTV;
private final File altXMLTV;

private Document refDoc = null;
private Document altDoc = null;

public XMLTVSourceCombiner(String referenceXMLTV, String alternateXMLTV)
{
	refXMLTV = new File(referenceXMLTV);
	altXMLTV = new File(alternateXMLTV);
}

public void combineSource(String fieldname)
{
	if(refDoc == null)
	{
		refDoc = XMLTransform.parseXML(refXMLTV);
		altDoc = XMLTransform.parseXML(altXMLTV);
	}
	
	// Select all the /tv/programme nodes with no episode-num value
	
	// for each node find a matching programme(start and channel) and programme/title
	// 	NB start has a format like "20200220001500 +0100" so might not match if different timezone offsets are used!
	// 	title needs to be a case-insensitive match!!
	// 	If one is found add it to the 'reference' node.
	
}

public void writeUpdatedXMLTV(String filename)
{
}

}
