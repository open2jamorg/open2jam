package org.open2jam.render;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.net.URL;

public class SpriteBuilder
{
	private enum Keyword {
		Resources, spritelist, sprite
	}

	Stack<Keyword> call_stack;
	Stack<Map<String,String>> atts_stack;

	Stack<Sprite> buffer;
	HashMap<String,SpriteList> result;

	private static String FILE_PATH_PREFIX = "/resources/";

	public SpriteBuilder()
	{
		call_stack = new Stack<Keyword>();
		atts_stack = new Stack<Map<String,String>>();
		buffer = new Stack<Sprite>();
		result = new HashMap<String,SpriteList>();
	}
	
	public void parseStart(String s, Map<String,String> atts)
	{
		Keyword k = getKeyword(s);
		call_stack.push(k);
		atts_stack.push(atts);
	}

	public void parseEnd()
	{
		Keyword k = call_stack.pop();
		Map<String,String> atts = atts_stack.pop();
		switch(k)
		{
			case sprite:
			int x = Integer.parseInt(atts.get("x"));
			int y = Integer.parseInt(atts.get("y"));
			int w = Integer.parseInt(atts.get("w"));
			int h = Integer.parseInt(atts.get("h"));
			java.awt.Rectangle slice = new java.awt.Rectangle(x,y,w,h);

			URL url = SpriteBuilder.class.getResource(FILE_PATH_PREFIX+atts.get("file"));
			if (url == null)throw new RuntimeException("Cannot find resource: "+FILE_PATH_PREFIX+atts.get("file"));

			SpriteID s = new SpriteID(url,slice);
			buffer.push(ResourceFactory.get().getSprite(s));
			break;

			case spritelist:
			double framespeed = Integer.parseInt(atts.get("framespeed"));
			framespeed /= 1000; // spritelist need framespeed in milliseconds
			String id = atts.get("id");
			SpriteList sl = new SpriteList(framespeed);
			sl.addAll(buffer);
			buffer.clear();
			result.put(id,sl);
		}
	}

	public HashMap<String,SpriteList> getResult()
	{
		return result;
	}

	private Keyword getKeyword(String s)
	{
		try{
			return Keyword.valueOf(s);
		}catch(IllegalArgumentException e){
			javax.swing.JOptionPane.showMessageDialog(null, "Unknown keyword ["+s+"] in resources.xml.", "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		return null;
	}
}
