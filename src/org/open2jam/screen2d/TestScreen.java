package org.open2jam.screen2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.open2jam.GameOptions;
import org.open2jam.screen2d.Actors.EGroup;
import org.open2jam.gameplays.O2JamPlay;
import org.open2jam.parsers.Chart;
import org.open2jam.screen2d.Actors.ShaderGroup;
import org.open2jam.screen2d.skin.Skin;
import org.open2jam.screen2d.skin.SkinParser;
import org.open2jam.utils.Logger;
import org.open2jam.utils.SystemTimer;

/**
 *
 * @author CdK
 */
public class TestScreen extends BasicScreen {
    
    private static final FileHandle resources_xml = Gdx.files.internal("resources/skin.xml");
    
    Skin skin;
    
    EGroup player;
    EGroup notes;
    
    FPSLogger fps;
    
    O2JamPlay gp;
    
    public TestScreen(Game game, Chart chart, GameOptions opt) {
	super(game, chart, opt);
    }

    @Override
    public void show() {

	SkinParser sp = new SkinParser(stage);
	try {
	    SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.read(), sp);
	    skin = sp.getResult("o2jam");
        } catch (ParserConfigurationException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (org.xml.sax.SAXException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (java.io.IOException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        }
	fps = new FPSLogger();
	//System.out.println(stage.graphToString());
	
	notes = (EGroup) stage.findActor("notes");
	notes.centerOrigin();
	
	player = (EGroup) stage.findActor("player");
	player.centerOrigin();
//	player.scaleX = player.scaleY = 0.6f;
//	player.y -=300;
//	player.rotation = 45;
	
	for(String s : skin.getEntityMap().keySet())
	{
	    if(skin.getEntityMap().get(s).name.toLowerCase().startsWith("layer")) continue;
	    skin.getEntityMap().get(s).visible = false;
	}

	gp = new O2JamPlay(stage, skin, chart, opt, SystemTimer.getTime());
	gp.init();
    }

    @Override
    public void render(float delta) {
	stage.draw();

	fps.log();
	SystemTimer.updateTimes(delta);
	gp.update(SystemTimer.getTime()-gp.startTime());
	
	stage.act(delta);

//	player.rotation += delta*30;
//	cls();


    }
}
