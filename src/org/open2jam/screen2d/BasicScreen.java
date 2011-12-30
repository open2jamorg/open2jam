package org.open2jam.screen2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import org.open2jam.GameOptions;
import org.open2jam.parsers.Chart;

/**
 *
 * @author CdK
 */
public class BasicScreen implements Screen {

    Game game;
    Stage stage;
    
    Chart chart;
    GameOptions opt;
    
    public BasicScreen(Game game, Chart chart, GameOptions opt) {
	this.game = game;
	this.chart = chart;
	this.opt = opt;

	stage = new Stage(0, 0, true);
    }


    @Override
    public void show() {}
    
    @Override
    public void render(float delta) {
	stage.act(delta);
    }
    
    /**
     * Clear screen
     */
    public void cls() {
//	Gdx.gl.glClearColor(1, 1, 1, 1);
	Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void hide() {
	stage.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
	stage.dispose();
    }

}
