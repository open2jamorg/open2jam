/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.screen2d.Actors;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import java.util.ArrayList;
import java.util.List;
import org.open2jam.entities.Entity;

/**
 * Add some helpers to the Group class
 * @author CdK
 */
public class EGroup extends Group {
    
    public EGroup(String name) {
	super(name);
    }  
    
    public void setOrigin(float x, float y)
    {
	originX = x;
	originY = y;
    }
    
    public void centerOrigin()
    {
	originX = width / 2.0f;
	originY = height / 2.0f;
    }  
    
    public List<Entity> getEntities()
    {
	ArrayList<Entity> l = new ArrayList<Entity>();
	
	for(Actor a : this.getActors())
	    l.add((Entity)a);
	
	return l;
    }    
}
