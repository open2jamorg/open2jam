/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.screen2d.Actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.open2jam.GameOptions.VisibilityMod;

/**
 *
 * @author CdK
 */
public class ShaderGroup extends EGroup {
    
    /** The start of the visible part in gl viewport **/
    private static final float VIEWPORT_START = 0.6f;
    /** The end of the visible part in gl viewport **/
    private static final float VIEWPORT_END = -1f;    
    
    ShaderProgram shader = null;
    
    VisibilityMod visibility = VisibilityMod.None;
    Vector2 hidden_part, sudden_part;

    public ShaderGroup(String name) {
	super(name);
	hidden_part = sudden_part = new Vector2(VIEWPORT_START, VIEWPORT_END);
    }

    public void createShader (String vertex, String frag) {
	ShaderProgram.pedantic = false;    
	
	shader = new ShaderProgram(vertex, frag);
	    
	if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
    }
    
    public void setVisibility(VisibilityMod v)
    {
	visibility = v;
    }
    
    public void setVisibilityPoints(float p1, float p2, float p3, float p4)
    {
	p1 = MathUtils.clamp(p1, -1, 1);
	p2 = MathUtils.clamp(p2, -1, 1);
	p3 = MathUtils.clamp(p3, -1, 1);
	p4 = MathUtils.clamp(p4, -1, 1);
	
	hidden_part = new Vector2(p1, p2);
	sudden_part = new Vector2(p3, p4);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
	if (debug && debugTexture != null && parent != null)
		batch.draw(debugTexture, x, y, originX, originY, width == 0 ? 200 : width, height == 0 ? 200 : height, scaleX, scaleY,
			rotation, 0, 0, debugTexture.getWidth(), debugTexture.getHeight(), false, false);
	
	if (transform) applyTransform(batch);
	batch.end();
	batch.setShader(shader);
	batch.begin();
        shader.setUniformi("u_visibility", visibility.ordinal());
	shader.setUniformf("u_v_points", hidden_part.x, hidden_part.y, sudden_part.x, sudden_part.y);
	shader.setUniformf("u_cut_points", VIEWPORT_START, VIEWPORT_END);
	drawChildren(batch, parentAlpha);
	batch.end();
	batch.setShader(null);
	batch.begin();
	if (transform) resetTransform(batch);
    }
    
}
