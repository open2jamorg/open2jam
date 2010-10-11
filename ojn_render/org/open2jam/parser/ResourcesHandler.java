package org.open2jam.parser;

import java.util.HashMap;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

import org.open2jam.render.SpriteID;
import org.open2jam.render.EntityBuilder;

public class ResourcesHandler extends DefaultHandler
{
	/** an object for locating the origin of SAX document events */
	private Locator locator;

	/** our work is to just defer the nodes to this */
	private EntityBuilder ef;

	public ResourcesHandler(EntityBuilder ef)
	{
		this.ef = ef;
	}

	//Receive notification of the beginning of an element.
	public void startElement(String uri, String localName, String qName, Attributes atts)
	{
		HashMap<String,String> atts_map = new HashMap<String,String>(atts.getLength());
		for(int i=0;i<atts.getLength();i++)
			atts_map.put(atts.getQName(i), atts.getValue(i));
		ef.parseStart(qName,atts_map);
	}

	public void endElement(String uri,String localName,String qName)
	{
		ef.parseEnd();
	}
}

