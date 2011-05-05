package org.open2jam.render;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Map;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import org.open2jam.util.Logger;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.AnimatedEntity;
import org.open2jam.render.entities.ComboCounterEntity;
import org.open2jam.render.entities.CompositeEntity;
import org.open2jam.render.entities.EffectEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.BarEntity;
import org.open2jam.render.entities.JudgmentEntity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

public class SkinHandler extends DefaultHandler
{

    private enum Keyword {
        Resources, skin, spriteset, styles, style, sprite, frame, layer, entity
    }

    private ArrayDeque<Keyword> call_stack;
    private ArrayDeque<Map<String,String>> atts_stack;

    private ArrayList<Sprite> frame_buffer;
    private HashMap<String, SpriteList> sprite_buffer;

    private ArrayList<String> style_list;
    private HashMap<String, ArrayList<String>> styles_map;

    private Skin result;

    private int layer = -1;

    private String target_skin;
    private boolean on_skin = false;

    private double baseW = 800;
    private double baseH = 600;
    private final double targetW;
    private final double targetH;


    public SkinHandler(String skin, double width, double height)
    {
        this.target_skin = skin;
	this.targetW = width;
	this.targetH = height;
        call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();
        frame_buffer = new ArrayList<Sprite>();
        sprite_buffer = new HashMap<String, SpriteList>();

        style_list = new ArrayList<String>();
        styles_map = new HashMap<String, ArrayList<String>>();
        
        result = new Skin();
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
                if(atts_map.get("name").equals(target_skin))on_skin = true;
		if(atts_map.containsKey("width"))this.baseW = Double.parseDouble(atts_map.get("width"));
		if(atts_map.containsKey("height"))this.baseH = Double.parseDouble(atts_map.get("height"));
                result.judgment_line = Integer.parseInt(atts_map.get("judgment_line"));

		result.screen_scale_x = (float) (this.targetW/this.baseW);
		result.screen_scale_y = (float) (this.targetH/this.baseH);
                ResourceFactory.get().getGameWindow().initScales(this.baseW, this.baseH);
            }break;

            case layer:{
                this.layer = Integer.parseInt(atts_map.get("id"));
                if(this.layer > result.max_layer)result.max_layer = this.layer;
            }break;
        }
    }

    @Override
    public void endElement(String uri,String localName,String qName)
    {
        Keyword k = call_stack.pop();
        Map<String,String> atts = atts_stack.pop();

        if(!on_skin)return;
        else if(k == Keyword.skin)on_skin = false;

        switch(k)
        {
            case frame:{
                int x = Integer.parseInt(atts.get("x"));
                int y = Integer.parseInt(atts.get("y"));
                int w = Integer.parseInt(atts.get("w"));
                int h = Integer.parseInt(atts.get("h"));

                float sx = 1, sy = 1;
                if(atts.containsKey("scale_x"))sx = Float.parseFloat(atts.get("scale_x"));
                if(atts.containsKey("scale_y"))sy = Float.parseFloat(atts.get("scale_y"));
                if(atts.containsKey("scale"))sy = sx = Float.parseFloat(atts.get("scale"));

                Rectangle slice = new Rectangle(x,y,w,h);

                String FILE_PATH_PREFIX = "/resources/";
                URL url = SkinHandler.class.getResource(FILE_PATH_PREFIX +atts.get("file"));
                if(url == null)throw new RuntimeException("Cannot find resource: "+ FILE_PATH_PREFIX +atts.get("file"));

                Sprite s;
                try {
                    s = ResourceFactory.get().getSprite(url, slice);
                } catch(IOException e) {
                    Logger.global.log(Level.WARNING, "Sprite resource load error !! {0}", e);
                    break;
                }
                s.setScale(sx, sy);
                frame_buffer.add(s);
            }break;

            case sprite:{
                double framespeed = 0;
                if(atts.containsKey("framespeed"))framespeed = Double.parseDouble(atts.get("framespeed"));
                framespeed /= 1000; // spritelist need framespeed in milliseconds

                String id;
                if(atts.containsKey("id"))id = atts.get("id");
                else {
                    Logger.global.severe("bad resource file ! sprite must have an ID !");
                    break;
                }

                SpriteList sl = new SpriteList(framespeed);
                sl.addAll(frame_buffer);

                sprite_buffer.put(id, sl);

                frame_buffer.clear();
            }break;
           
            case style:{
                style_list.add(atts.get("id"));
            }break;
            case styles:{
                ArrayList<String> al = new ArrayList<String>();
                al.addAll(style_list);
                styles_map.put(atts.get("id"), al);
                style_list.clear();
            }break;
            
            case entity:{
            Entity e;

            String id = null;
            if(atts.containsKey("id"))id = atts.get("id");

            String sprites[];
            if(atts.containsKey("sprite"))sprites = atts.get("sprite").split(",");
            else {
                Logger.global.log(Level.SEVERE, "bad resource file ! entity [{0}] must have an sprite !", id);
                break;
            }

            if(sprites[0].trim().equals("")){
                Logger.global.log(Level.SEVERE, "bad resource file ! entity [{0}] must have an sprite !", id);
                break;
            }



            if(id != null && (e = promoteEntity(id, atts)) != null){
                    // ok
            }
            else if(atts.get("sprite").split(",").length > 1){
                ArrayList<Entity> list = new ArrayList<Entity>();
                for(String s : atts.get("sprite").split(",")){
                    s = s.trim();
                    list.add( new Entity(sprite_buffer.get(s),0,0));
                }
                e = new CompositeEntity(list);
            }
            else{
                String sprite = atts.get("sprite").trim();
                SpriteList sl = sprite_buffer.get(sprite);
                if(sl.size() > 1){
                    e = new AnimatedEntity(sl, 0, 0);
                }
                else e = new Entity(sl, 0, 0);
            }

            e.setLayer(this.layer);
            double x = e.getX(), y = e.getY();
            if(atts.containsKey("x"))x = Integer.parseInt(atts.get("x"));
            if(atts.containsKey("y"))y = Integer.parseInt(atts.get("y"));
            e.setPos(x, y);
            
            if(id != null){
                if(!result.getEntityMap().containsKey(id))result.getEntityMap().put(id, e);
                else{
                    Entity prime = result.getEntityMap().get(id);
                    if(prime instanceof CompositeEntity){
                        ((CompositeEntity)prime).getEntityList().add(e);
                    }else{
                        CompositeEntity ce = new CompositeEntity(prime, e);
                        result.getEntityMap().put(id, ce);
                    }
                }
            }
            else result.getEntityList().add(e);
            
            }break;
        }
    }

    private Entity promoteEntity(String id, Map<String,String> atts)
    {
        Entity e = null;
        if(id.startsWith("NOTE_")){

            String sprites[] = atts.get("sprite").split(",");

            SpriteList head = null, body = null;
            for(String s : sprites){
                s = s.trim();
                if(s.startsWith("head")){
                    head = sprite_buffer.get(s);
                }
                else if(s.startsWith("body")){
                    body = sprite_buffer.get(s);
                }
            }
            int x = 0;
            if(atts.containsKey("x"))x = Integer.parseInt(atts.get("x"));

            e = new LongNoteEntity(head, body, Event.Channel.valueOf(id), x, 0);
            e.setLayer(layer);
            result.getEntityMap().put("LONG_"+id, e);
            e = new NoteEntity(head, Event.Channel.valueOf(id), x, 0);
        }
        else if(id.equals("MEASURE_MARK")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
            e = new MeasureEntity(s, 0, 0);
        }
        else if(id.equals("JUDGMENT_LINE")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
            e = new AnimatedEntity(s, 0, 0);
        }
        else if(id.startsWith("EFFECT_JUDGMENT_")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
            e = new JudgmentEntity(s,0, 0);
        }
        // TODO: change the name of this ???
        else if(id.startsWith("EFFECT_LONGFLARE")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
            e = new AnimatedEntity(s,0,0);
        }
        else if(id.startsWith("EFFECT_")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
            e = new EffectEntity(s,0, 0);
        }
        else if(id.startsWith("PRESSED_NOTE_")){
            SpriteList sl = sprite_buffer.get(atts.get("sprite"));
            e = new AnimatedEntity(sl, 0, 0);
        }
        else if(id.equals("FPS_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
            e = new NumberEntity(list, 0, 0);
        }
	else if(id.equals("COUNTER_JUDGMENT_PERFECT")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("COUNTER_JUDGMENT_COOL")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("COUNTER_JUDGMENT_GOOD")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("COUNTER_JUDGMENT_BAD")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("COUNTER_JUDGMENT_MISS")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("MAXCOMBO_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("SCORE_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
	}
	else if(id.equals("JAM_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
            Entity title = null;
            if(list.size()>10)title = list.remove(10);
	    e = new ComboCounterEntity(list, title, 0, 0);
	}
        else if(id.equals("COMBO_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
            Entity title = null;
            if(list.size()>10)title = list.remove(10);
	    e = new ComboCounterEntity(list, title, 0, 0);
        }
        else if(id.equals("MINUTE_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
        }
        else if(id.equals("SECOND_COUNTER")){
            ArrayList<Entity> list = new ArrayList<Entity>();
            for(String s : atts.get("sprite").split(",")){
                s = s.trim();
                list.add( new AnimatedEntity(sprite_buffer.get(s),0,0));
            }
	    e = new NumberEntity(list, 0, 0);
        }
	else if(id.startsWith("PILL_")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
	    e = new AnimatedEntity(s, 0, 0);
	}
	else if(id.equals("LIFE_BAR")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
	    e = new BarEntity(s, 0, 0);
	}
	else if(id.equals("JAM_BAR")){
            SpriteList s = sprite_buffer.get(atts.get("sprite"));
	    e = new BarEntity(s, 0, 0);
	}
	else if(id.equals("TIME_BAR")){
	    //TODO
	}
        else{
            Logger.global.log(Level.WARNING, "unpromoted entity [{0}]", id);
        }
        return e;
    }

    public Skin getResult()
    {
        return result;
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

    public HashMap<String, ArrayList<String>> getStyles()
    {
        return styles_map;
    }
}
