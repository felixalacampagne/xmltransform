package com.scu.xmltv;

import java.io.File;
import javax.xml.transform.TransformerException;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.scu.utils.CmdArgMgr;
import com.scu.utils.NodeUtils;


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
public final static String ARG_OUTFILE = "-combine";
private static final String ARG_REF = "-ref";
private static final String ARG_ALT = "-alt";
private static final String ARG_RESULT = "-res";	

private final File refXMLTV;
private final File altXMLTV;
private final NodeUtils nu = NodeUtils.getNodeUtils();
private Document refDoc = null;
private Document altDoc = null;
java.util.logging.Logger log = java.util.logging.Logger.getLogger(this.getClass().getName());

public XMLTVSourceCombiner(String referenceXMLTV, String alternateXMLTV)
{
	refXMLTV = new File(referenceXMLTV);
	altXMLTV = new File(alternateXMLTV);
}

public void combineSource(String fieldname)
{
	if(refDoc == null)
	{
		refDoc = nu.parseXML(refXMLTV);
		altDoc = nu.parseXML(altXMLTV);
	}
	
	// Select all the /tv/programme nodes with no episode-num value
	
	// for each node find a matching programme(start and channel) and programme/title
	// 	NB start has a format like "20200220001500 +0100" so might not match if different timezone offsets are used!
	// 	title needs to be a case-insensitive match!!
	// 	If one is found add it to the 'reference' node.
	
	
	NodeList progs = null;
	progs = nu.getNodesByPath(refDoc, "/tv/programme[not(" + fieldname + ")]");
	for(int i = 0; i <  progs.getLength(); i++)
	{
		Node refProg = progs.item(i);
		String title = nu.getNodeValue(refProg, "title");
		
		// Locate a matching node in altDoc with a fieldname
		String starttime = nu.getAttributeValue(refProg, "start");
		String chanid = nu.getAttributeValue(refProg, "channel");
		
		
		NodeList altprogs = nu.getNodesByPath(altDoc, "/tv/programme[@start='" + starttime + "' and @channel='" + chanid + "']");
		
		if(altprogs == null || (altprogs.getLength() == 0))
		{
			log.fine("combineSource: No program found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
			continue;
		}
		else if(altprogs.getLength() > 1)
		{
			log.info("combineSource: Multiple programs found in alt source for: start=" + starttime + " and channel= " + chanid + " (title=" +  title + ")");
		}
		
		Node altProg = altprogs.item(0);
		Node altFld = null;
		try
		{
			altFld = nu.getNodeByPath(altProg, fieldname);
			if((altFld == null))
			{
				log.fine("combineSource: Alt source has no field '" + fieldname + "' for '" + title + "'");
				continue;
			}
		}
		catch(TransformerException tex)
		{
			log.info("combineSource: exception finding field:" + fieldname + ": " + tex);
			continue;
		}

		Node newNode = altFld.cloneNode(true);  // Create a duplicate node
	   refDoc.adoptNode(newNode);              // Transfer ownership of the new node into the destination document
	   refProg.insertBefore(newNode, refProg.getLastChild()); // Place the node in the document. Fingers crossed putting it at the end is OK!!
	   log.info("combineSource: updated '" + title + "' (" + starttime + " " + chanid + "): " + newNode.getTextContent());

	}
}

public void writeUpdatedXMLTV(String filename) throws Exception
{
	nu.outputNode(this.refDoc, new File(filename));
}


public static void main(String[] args) throws Exception
{
CmdArgMgr cmd = new CmdArgMgr();
String ref = null;
String alt = null;
String result = null;
String [] keys = null;
	
	cmd.parseArgs(args);
	keys = cmd.getArgNames();
	
	
	for(int i = 0; i<keys.length; i++)
	{
	   String val = cmd.getArg(keys[i]);
	   if(ARG_REF.compareTo(keys[i]) == 0)
	      ref = val;
	   else if(ARG_ALT.compareTo(keys[i]) == 0)
	      alt = val;
	   else if(ARG_RESULT.compareTo(keys[i]) == 0)
	      result = val;
	}
	
	
	if((ref==null) || (alt==null) || (result==null))
	{
	   System.out.println("Usage: HTMLMaker " + ARG_REF + "=<reference file> " +
	   		ARG_ALT + "=<alternative file> " +
	   		ARG_RESULT + "=<result> ");
	   System.exit(1);
	}
	
	
	
	XMLTVSourceCombiner sc = new XMLTVSourceCombiner(ref, alt);
	sc.combineSource("sub-title");
	sc.combineSource("episode-num");
	sc.writeUpdatedXMLTV(result);
}
}
