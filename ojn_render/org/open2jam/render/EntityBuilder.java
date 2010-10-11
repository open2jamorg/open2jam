package org.open2jam.render;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import org.open2jam.entities.*;
import org.open2jam.render.SpriteID;

public class EntityBuilder
{
	private enum Keyword {
		Resources, Note, Frame
	}

	Stack<Keyword> call_stack;
	Stack<Map<String,String>> atts_stack;
	Stack<Object> buffer;

	Map<String,Entity> result;

	private static String FILE_PATH_PREFIX = "sprites"+java.io.File.separator;

	public EntityBuilder()
	{
		call_stack = new Stack<Keyword>();
		atts_stack = new Stack<Map<String,String>>();
		buffer = new Stack<Object>();
		result = new HashMap<String,Entity>();
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
			case Frame:
			int x = Integer.parseInt(atts.get("x"));
			int y = Integer.parseInt(atts.get("y"));
			int w = Integer.parseInt(atts.get("w"));
			int h = Integer.parseInt(atts.get("h"));
			java.awt.Rectangle slice = new java.awt.Rectangle(x,y,w,h);
			SpriteID s = new SpriteID(FILE_PATH_PREFIX+atts.get("file"),slice);
			buffer.push(s);
			break;

			case Note:
			int framespeed = Integer.parseInt(atts.get("framespeed"));
			String name = "note"+atts.get("number");
			SpriteID frames[] = buffer.toArray(new SpriteID[0]);
			Entity e = new AnimatedEntity(frames, 0, 0, framespeed);
			result.put(name,e);
			buffer.clear();
		}
	}

	public Map<String,Entity> getResult()
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
