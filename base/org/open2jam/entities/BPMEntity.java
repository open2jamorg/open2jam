package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Render;
/** this is a special type of entity.
*** it is invisible, 
*** it's only action is changing the bpm
*** when it reaches the judgment
***/
public class BPMEntity extends Entity
{
	private double bpm;
	protected Render render;

	public BPMEntity(Render r, double bpm, double y)
	{
		this.render = r;
		this.bpm = bpm;
		bounds = new Rectangle2D.Double(0,y,0,0);
	}

	public void move(long delta)
	{
		setYMove(render.getNoteSpeed());
		super.move(delta);
	}

	public void judgment()
	{
		System.out.println("BPM CHANGE "+bpm);
		render.setBPM(bpm);
		alive = false;
	}

	public void draw() {}
}