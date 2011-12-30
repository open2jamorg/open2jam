package org.open2jam.screen2d.skin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.open2jam.entities.AnimatedEntity;
import org.open2jam.entities.Entity;
import org.open2jam.entities.LongNoteEntity;
import org.open2jam.screen2d.Actors.EGroup;
import org.open2jam.entities.MeasureEntity;
import org.open2jam.entities.NoteEntity;
import org.open2jam.parsers.Event;
import org.open2jam.screen2d.Actors.ShaderGroup;
import org.open2jam.screen2d.skin.Skin.entityID;
import org.open2jam.utils.FrameList;
import org.open2jam.utils.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author CdK
 */
public class SkinParser extends DefaultHandler {

    private enum Keyword {
	resources,
	spriteset,
	image, sprite, frame,
	font,
	styles,
	style, group, entry,
	skin,
	layer, player, notes, entity, entity_group
    }


    private ArrayDeque<Keyword> call_stack;
    private ArrayDeque<Map<String,String>> atts_stack;

    private HashMap<String, FrameList> sprite_buffer;
    private ArrayList<TextureRegion> frame_buffer;

    private EGroup group;
    private int layers, z_index = 0;
    
    private long entity = 0;
    
    private ArrayList<Actor> entities;

    private final Stage stage;

    private String skin_name;

    private Texture texture;

    private HashMap<String, Skin> result;

    public SkinParser(Stage stage)
    {
	this.stage = stage;

	call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();

	sprite_buffer = new HashMap<String, FrameList>();
	frame_buffer = new ArrayList<TextureRegion>();
	
	entities = new ArrayList<Actor>();

	result = new HashMap<String, Skin>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
    {
        HashMap<String,String> atts_map = new HashMap<String,String>(atts.getLength());
        for(int i=0;i<atts.getLength();i++)
                atts_map.put(atts.getQName(i), atts.getValue(i));

        Keyword k = getKeyword(qName);
        call_stack.push(k);
        atts_stack.push(atts_map);

        switch(k)
        {
            case skin:{
                this.skin_name = atts_map.get("name");
                result.put(skin_name, new Skin());

		float w = Float.parseFloat(atts_map.get("width"));
		float h = Float.parseFloat(atts_map.get("height"));
		stage.setViewport(w, h, true);
		
		group = createGroup("main", atts_map);
		stage.addActor(group);
            }break;
	    case image:{
		texture = new Texture(Gdx.files.internal("resources/"+atts_map.get("file")));
		//System.out.println("THE TEXTURE: "+texture.getWidth()+" "+texture.getHeight());
	    }break;
	    case player:{
		result.get(skin_name).keys = Integer.parseInt(atts_map.get("keys"));
		EGroup n = createGroup("player", atts_map);
		group.addActor(n);
		group = n;
		//System.out.println("Player created");
	    }break;
	    case notes:{
		EGroup n = createGroup("noteGroup", atts_map);
		group.addActor(n);
		group = n;
		n = new ShaderGroup("measures");
		((ShaderGroup)n).createShader(Gdx.files.internal("resources/measure_shader.vert").readString(),
			Gdx.files.internal("resources/measure_shader.frag").readString());
		n.width = group.width;
		n.height = group.height;
		group.addActor(n);
		n = new ShaderGroup("notes");
		((ShaderGroup)n).createShader(Gdx.files.internal("resources/notes_shader.vert").readString(),
			Gdx.files.internal("resources/notes_shader.frag").readString());
		n.width = group.width;
		n.height = group.height;
		group.addActor(n);
		//System.out.println("Notes created");
	    }break;
            case layer:{
		EGroup n = createGroup("layer_"+layers++, atts_map);
		group.addActor(n);
		group = n;
		//System.out.println("Layer"+layers+" created");
            }break;
	    case entity_group:{
		String t = "layer_"+layers++;
		if(atts_map.containsKey("type")) t=atts_map.get("type");
		EGroup n = createGroup(t, atts_map);
		group.addActor(n);
		group = n;
	    }
		 
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
	Keyword k = call_stack.pop();
        Map<String,String> atts = atts_stack.pop();

	switch(k)
	{
	    case entity_group:
	    {
		System.out.println(group.name);
		result.get(skin_name).getEntityMap().put(group.name, group);
	    }
	    case player:case notes:case layer:
	    {
		
		group = (EGroup) group.parent;
		z_index = 0;
	    }break;
	    case frame:
	    {
		String[] s = atts.get("xywh").split(",");

		int x = Integer.parseInt(s[0]);
		int y = Integer.parseInt(s[1]);
		int w = Integer.parseInt(s[2]);
		int h = Integer.parseInt(s[3]);

		//System.out.println("xywh: "+x+" "+y+" "+w+" "+h);
		TextureRegion tx = new TextureRegion(texture, x, y, w, h);
		//System.out.println("TX  : "+tx.getRegionX()+" "+tx.getRegionY()+" "+tx.getRegionWidth()+" "+tx.getRegionHeight());
		frame_buffer.add(tx);
	    }break;
	    case sprite:
	    {
		String id = atts.get("id");
		
		float framespeed = 0;
		boolean atBeat = false;
		boolean loop = true;
		if(atts.containsKey("framespeed"))
		{
		    String fs = atts.get("framespeed").toLowerCase();
		    if(fs.contains("beat"))
		    {
			framespeed = Float.parseFloat(fs.split("b")[0]);
			atBeat = true;
		    }
		    else
			framespeed = Float.parseFloat(fs);
		}
		if(atts.containsKey("loop")) loop = Boolean.parseBoolean("loop");

		FrameList fl = new FrameList(framespeed, loop, atBeat);
		fl.addAll(frame_buffer);
		frame_buffer.clear();

		sprite_buffer.put(id, fl);
	    }break;
	    case entity:
	    {
		Entity e;
		
		String type = "ENTITY_"+entity++;
		if(atts.containsKey("type")) type = atts.get("type");
		
		e = promoteEntity(type, atts);
		
		e.x = e.y = 0;
		e.scaleX = e.scaleY = 1;
		if(atts.containsKey("x")) e.x = Float.parseFloat(atts.get("x"));
		if(atts.containsKey("y")) e.y = Float.parseFloat(atts.get("y"));
		if(atts.containsKey("scale_x")) e.scaleX = Float.parseFloat(atts.get("scale_x"));
		if(atts.containsKey("scale_y")) e.scaleY = Float.parseFloat(atts.get("scale_y"));
		if(atts.containsKey("scale"))
		{
		    e.scaleX = Float.parseFloat(atts.get("scale"));
		    e.scaleY = Float.parseFloat(atts.get("scale"));
		}
		//System.out.println(e.name+" Z-INDEX "+z_index);
		e.z_index = z_index++;
			
		e.group_name = group.name;
		if(!type.startsWith("ENTITY_"))
		    result.get(skin_name).getEntityMap().put(type, e);
		else
		    group.addActor(e);
   
		//System.out.println("----Entity "+e.name+" created in "+group.name);
	    }break;
	}
    }

    private Entity promoteEntity(String type, Map<String,String> atts)
    {
	Entity e = null;
	type = type.toUpperCase();
	
	if(type.startsWith("ENTITY_"))
	{
	    FrameList fl = sprite_buffer.get(atts.get("sprite"));
	    
	    return new AnimatedEntity(type, fl);
	}
	
	switch(entityID.valueOf(type))
	{
	    case NOTE_1: case NOTE_2: case NOTE_3: case NOTE_4: case NOTE_5:
	    case NOTE_6: case NOTE_7: case NOTE_SC:
	    {
		FrameList sprite, head, body, tail,
			psprite, phead, pbody, ptail;

		sprite = sprite_buffer.get(atts.get("sprite"));
		head = getSprite("head", atts);
		body = getSprite("body", atts);
		tail = getSprite("tail", atts);
		psprite = getSprite("pressed_sprite", atts);
		phead = getSprite("pressed_head", atts);
		pbody = getSprite("pressed_body", atts);
		ptail = getSprite("pressed_tail", atts);

		EGroup g = new EGroup("FUUUUU");
		g.addActor(new AnimatedEntity("SPRT", sprite));
		g.addActor(new AnimatedEntity("HEAD", head));
		g.addActor(new AnimatedEntity("BODY", body));
		g.addActor(new AnimatedEntity("TAIL", tail));
		g.addActor(new AnimatedEntity("PSPRT", psprite));
		g.addActor(new AnimatedEntity("PHEAD", phead));
		g.addActor(new AnimatedEntity("PBODY", pbody));
		g.addActor(new AnimatedEntity("PTAIL", ptail));

		g.x = g.y = 0;
		g.scaleX = g.scaleY = 1;
		if(atts.containsKey("x")) g.x = Float.parseFloat(atts.get("x"));
		if(atts.containsKey("y")) g.y = Float.parseFloat(atts.get("y"));
		if(atts.containsKey("scale_x")) g.scaleX = Float.parseFloat(atts.get("scale_x"));
		if(atts.containsKey("scale_y")) g.scaleY = Float.parseFloat(atts.get("scale_y"));
		if(atts.containsKey("scale"))
		{
		    g.scaleX = Float.parseFloat(atts.get("scale"));
		    g.scaleY = Float.parseFloat(atts.get("scale"));
		}

		e = new LongNoteEntity("LONG_"+type, g, Event.Channel.valueOf(type));

		result.get(skin_name).getEntityMap().put("LONG_"+type, e);

		e = new NoteEntity(type, g, Event.Channel.valueOf(type));
	    }break;
	    case MEASURE_MARK:
	    {
		FrameList fl = sprite_buffer.get(atts.get("sprite"));

		e = new MeasureEntity(type, fl);			
	    }break;
	    case JUDGMENT_LINE:
	    case EFFECT_JUDGMENT_PERFECT: case EFFECT_JUDGMENT_COOL: case EFFECT_JUDGMENT_GOOD:
	    case EFFECT_JUDGMENT_BAD: case EFFECT_JUDGMENT_MISS:
	    case EFFECT_LONGFLARE: case EFFECT_CLICK:
	    case PRESSED_NOTE_1_LANE: case PRESSED_NOTE_2_LANE: case PRESSED_NOTE_3_LANE:
	    case PRESSED_NOTE_4_LANE: case PRESSED_NOTE_5_LANE: case PRESSED_NOTE_6_LANE:
	    case PRESSED_NOTE_7_LANE: case PRESSED_NOTE_SC_LANE:
	    case PRESSED_NOTE_1_KEY: case PRESSED_NOTE_2_KEY: case PRESSED_NOTE_3_KEY:
	    case PRESSED_NOTE_4_KEY: case PRESSED_NOTE_5_KEY: case PRESSED_NOTE_6_KEY:
	    case PRESSED_NOTE_7_KEY: case PRESSED_NOTE_SC_KEY:
	    case COUNTER_FPS:
	    case COUNTER_JUDGMENT_PERFECT: case COUNTER_JUDGMENT_COOL: case COUNTER_JUDGMENT_GOOD:
	    case COUNTER_JUDGMENT_BAD: case COUNTER_JUDGMENT_MISS:
	    case COUNTER_MAXCOMBO: case COUNTER_SCORE:
	    case COUNTER_JAM: case COUNTER_COMBO: 
	    case COUNTER_MINUTE: case COUNTER_SECOND:
	    case COMBO_TITLE: case JAM_TITLE:
	    case PILL_1: case PILL_2: case PILL_3: case PILL_4: case PILL_5:
	    case LIFE_BAR: case JAM_BAR: case TIME_BAR:
	    {
		FrameList fl = sprite_buffer.get(atts.get("sprite"));

		e = new AnimatedEntity(type, fl);		
	    }break;
	}

	return e;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
	System.out.println(e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
	System.out.println(e);
    }
    
    public Skin getResult(String skin)
    {
        if(result.containsKey(skin))
            return result.get(skin);
        else
            return null;
    }

    private EGroup createGroup(String name, HashMap<String,String> atts_map)
    {
	EGroup g = new EGroup(name);
	g.x = g.y = g.width = g.height = 0;
	if(atts_map.containsKey("x"))g.x = Float.parseFloat(atts_map.get("x"));
	if(atts_map.containsKey("y"))g.y = Float.parseFloat(atts_map.get("y"));
	if(atts_map.containsKey("width"))g.width = Float.parseFloat(atts_map.get("width"));
	if(atts_map.containsKey("height"))g.height = Float.parseFloat(atts_map.get("height"));
	if(atts_map.containsKey("scale_x")) g.scaleX = Float.parseFloat(atts_map.get("scale_x"));
	if(atts_map.containsKey("scale_y")) g.scaleY = Float.parseFloat(atts_map.get("scale_y"));
	if(atts_map.containsKey("scale"))
	{
	    g.scaleX = Float.parseFloat(atts_map.get("scale"));
	    g.scaleY = Float.parseFloat(atts_map.get("scale"));
	}
	
	//default not visible for named entities
//	if(atts_map.containsKey("type") && name.toUpperCase().equals(atts_map.get("type")))
//	    g.visible = false;
	return g;
    }

    private Keyword getKeyword(String s)
    {
        try{
            return Keyword.valueOf(s);
        }catch(IllegalArgumentException e){
            Logger.global.log(Level.WARNING, "Unknown keyword [{0}] in resources.xml.", s);
        }
        return null;
    }

    private FrameList getSprite(String s, Map<String,String> atts)
    {
	if(atts.containsKey(s)) return sprite_buffer.get(atts.get(s));
	
	return sprite_buffer.get(atts.get("sprite"));
    }
}
