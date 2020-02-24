package com.scu.xmltv;

import java.io.File;
import javax.xml.transform.TransformerException;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	
	
	NodeList progs = null;
	progs = XMLTransform.getNodesByPath(refDoc, "/tv/programme[not(" + fieldname + ")]");
	for(int i = 0; i <  progs.getLength(); i++)
	{
		Node refProg = progs.item(i);
		String title = XMLTransform.getNodeValue(refProg, "title");
		
		// Locate a matching node in altDoc with a fieldname
		String starttime = XMLTransform.getAttributeValue(refProg, "start");
		String chanid = XMLTransform.getAttributeValue(refProg, "channel");
		
		
		NodeList altprogs = XMLTransform.getNodesByPath(altDoc, "/tv/programme[@start='" + starttime + "' and @channel='" + chanid + "']");
		
		if(altprogs == null || (altprogs.getLength() == 0))
		{
			System.out.println("No program found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
			continue;
		}
		else if(altprogs.getLength() > 1)
		{
			System.out.println("Multiple programs found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
		}
		
		Node altProg = altprogs.item(0);
		Node altFld = null;
		try
		{
			altFld = XMLTransform.getNodeByPath(altProg, fieldname);
			if((altFld == null))
			{
				System.out.println("Alt source has no field '" + fieldname + "' for '" + title + "'");
				continue;
			}
		}
		catch(TransformerException tex)
		{
			System.err.println("combineSource: exception finding field:" + fieldname + ": " + tex);
			continue;
		}

		Node newNode = altFld.cloneNode(true);  // Create a duplicate node
	   refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
	   refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!

	}
}

public void writeUpdatedXMLTV(String filename) throws Exception
{
	XMLTransform.outputNode(this.refDoc, new File(filename));
}

}
