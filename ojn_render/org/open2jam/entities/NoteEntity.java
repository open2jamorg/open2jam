package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;
import org.open2jam.parser.Chart;

/** a NoteEntity is a animated entity which moves down.
*** it moves according to a Chart */
public class NoteEntity extends AnimatedEntity
{

	public NoteEntity(SpriteID refs[], double framespeed)
	{
		super(refs,framespeed);
	}

	
}