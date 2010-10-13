package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;
import org.open2jam.parser.Chart;


public class LongNoteEntity extends NoteEntity
{
	protected Sprite body_frames[];

	public LongNoteEntity(SpriteID head_refs[], SpriteID body_refs[], double framespeed)
	{
		super(head_refs,framespeed);
		this.body_frames = ResourceFactory.get().getSprites(body_refs);
	}
}