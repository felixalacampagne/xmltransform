package com.scu.xmltv;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;


import org.apache.xalan.extensions.XSLProcessorContext;

import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.w3c.dom.Node;


public class ExtensionElements
{



public static String urlencode(XSLProcessorContext context, ElemExtensionCall elem)
throws java.net.MalformedURLException,
       java.io.FileNotFoundException,
       java.io.IOException,
       javax.xml.transform.TransformerException
{
TransformerImpl transf = context.getTransformer();
String encurl = null;
StringBuffer sb = new StringBuffer();
SerializationHandler origSerializationHandler = null;
   try
   {
      // So if the node is simple text then no problems. If I wanted to put
      // actions inside the element then they would not be expanded.
      // executeChildTemplates might cause the expansions, but I've no idea
      // how to get at the result of the expansion, encode it and swallow the
      // original expansion.
      
      // Have the feeling the output has to be redirected into another sort
      // of ContentHandler, and then the original must be restored after.
      
      
      OutputProperties format = transf.getOutputFormat();
      ByteArrayOutputStream ostream = new ByteArrayOutputStream();
      origSerializationHandler = transf.getSerializationHandler();
      SerializationHandler flistener = transf.createSerializationHandler(new StreamResult(ostream), format);           
      flistener.startDocument();
//      transf.executeChildTemplates(transf.getCurrentElement(), true);
      transf.executeChildTemplates(elem, context.getContextNode(), context.getMode(), flistener);      
      flistener.endDocument();
      // I'm guessing the result of the child transforms is now in the bstream
      // So it needs to be encoded and the result returned to the parent templates....
      encurl = URLEncoder.encode(new String(ostream.toByteArray(), "UTF-8"),"UTF-8");
      

      // Tidy up - might need to go in a finally
      transf.setSerializationHandler(origSerializationHandler);
      ostream.close();
      
      /*if(elem.getFirstChild() != null)
      {
         encurl = elem.getFirstChild().getTextContent();
         if(encurl != null)
         {
            encurl = URLEncoder.encode(encurl,"UTF-8");
         }
      }
      */
   }
   catch(Exception ex)
   {
      ex.printStackTrace();
   }
   return encurl;

}

}

