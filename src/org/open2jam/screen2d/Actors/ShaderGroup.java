/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.screen2d.Actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author CdK
 */
public class ShaderGroup extends EGroup {
    
    ShaderProgram shader = null;
    
    Map<String, Integer> uniformi;

    public ShaderGroup(String name) {
	super(name);
	
	uniformi = new HashMap<String, Integer>();
    }

    public void createShader (String vertex, String frag) {
	ShaderProgram.pedantic = false;    
	
	shader = new ShaderProgram(vertex, frag);
	    
	if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
    }
    
    public void setUniformi(String name, int value)
    {
	uniformi.put(name, value);
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
	for(Entry e : uniformi.entrySet())
	    shader.setUniformi((String)e.getKey(), (Integer)e.getValue());
	drawChildren(batch, parentAlpha);
	batch.end();
	batch.setShader(null);
	batch.begin();
	if (transform) resetTransform(batch);
    }
    
}
