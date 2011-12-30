/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.screen2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import org.open2jam.GameOptions;
import org.open2jam.parsers.Chart;

/**
 *
 * @author CdK
 */
public class Open2jamGame extends Game {

    Chart chart;
    GameOptions opt;
    
    public Open2jamGame(Chart c, GameOptions opt) {
	this.chart = c;
	this.opt = opt;
    }
    
    @Override
    public void create() {
	setScreen(new TestScreen(this, chart, opt));
    }

    @Override
    public void dispose() {
	getScreen().dispose();
	super.dispose();
	Gdx.gl = null;
	System.gc();
    }
    
}
