package org.open2jam.render.entities;

import java.awt.geom.Rectangle2D;
import org.lwjgl.opengl.GL11;

public class JudgmentEntity extends Entity
{
	public JudgmentEntity(double x, double y, double width, double height)
	{
		bounds = new Rectangle2D.Double(x,y,width,height);
	}

	public void draw()
	{
		GL11.glPushMatrix();
		GL11.glColor3f(1,1,1);
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		{
			GL11.glVertex2d(bounds.x, bounds.y);
			GL11.glVertex2d(bounds.x+bounds.width, bounds.y);
			GL11.glVertex2d(bounds.x, bounds.y+bounds.height);
			GL11.glVertex2d(bounds.x+bounds.width, bounds.y+bounds.height);
		}
		GL11.glEnd();
		GL11.glPopMatrix();
	}
}