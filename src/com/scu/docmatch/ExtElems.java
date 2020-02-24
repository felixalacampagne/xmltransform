package com.scu.docmatch;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.stream.StreamResult;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;

public class ExtElems
{

public String elmtval(XSLProcessorContext context, ElemExtensionCall elem)
   throws java.io.FileNotFoundException,
          java.io.IOException,
          javax.xml.transform.TransformerException
{
TransformerImpl transf = context.getTransformer();
String result = null;
SerializationHandler origSerializationHandler = null;
 
   try
   {

   OutputProperties format = transf.getOutputFormat();
   boolean oldstanda = format.getXmlStandalone();
   // TODO: I Think I need to change this to a 'text' output format instead
   // of XML to avoid having the XML processing instruction added.
   format.setXmlStandalone(false);
   ByteArrayOutputStream ostream = new ByteArrayOutputStream();
   origSerializationHandler = transf.getSerializationHandler();
   SerializationHandler flistener = transf.createSerializationHandler(new StreamResult(ostream), format);           

   flistener.startDocument();
   transf.executeChildTemplates(elem, context.getContextNode(), context.getMode(), flistener);      
   flistener.endDocument();

    

   result = new String(ostream.toByteArray(), "UTF-8");

   // Tidy up - might need to go in a finally
   format.setXmlStandalone(oldstanda);
   transf.setSerializationHandler(origSerializationHandler);
   ostream.close();

   }
   catch(Exception ex)
   {
   ex.printStackTrace();
   }
   return result;
}

}
