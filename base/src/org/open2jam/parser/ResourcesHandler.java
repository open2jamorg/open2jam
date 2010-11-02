package org.open2jam.parser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import java.util.HashMap;

import org.open2jam.render.SpriteBuilder;

public class ResourcesHandler extends DefaultHandler
{

	/** our work is to just defer the nodes to this */
	private SpriteBuilder ef;

	public ResourcesHandler(SpriteBuilder ef)
	{
		this.ef = ef;
	}

	//Receive notification of the beginning of an element.
    @Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
	{
		HashMap<String,String> atts_map = new HashMap<String,String>(atts.getLength());
		for(int i=0;i<atts.getLength();i++)
			atts_map.put(atts.getQName(i), atts.getValue(i));
		ef.parseStart(qName,atts_map);
	}

    @Override
	public void endElement(String uri,String localName,String qName)
	{
		ef.parseEnd();
	}
}

