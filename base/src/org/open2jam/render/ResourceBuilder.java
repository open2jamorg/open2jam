package org.open2jam.render;

import java.util.Map;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import org.open2jam.Logger;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.AnimatedEntity;
import org.open2jam.render.entities.EffectEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.FlareEffectEntity;
import org.open2jam.render.entities.LaneEntity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;

public class ResourceBuilder
{
    private enum Keyword {
        Resources, entity, spritelist, sprite
    }

    ArrayDeque<Keyword> call_stack;
    ArrayDeque<Map<String,String>> atts_stack;

    ArrayList<Sprite> sprite_buffer;
    HashMap<String, SpriteList> spritelist_buffer;
    HashMap<String,Entity> result;

    private static String FILE_PATH_PREFIX = "/resources/";

    protected Render render;

    public ResourceBuilder(Render r)
    {
        this.render = r;
        call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();
        sprite_buffer = new ArrayList<Sprite>();
        spritelist_buffer = new HashMap<String, SpriteList>();
        result = new HashMap<String,Entity>();
    }
    
    public void parseStart(String s, Map<String,String> atts)
    {
        Keyword k = getKeyword(s);
        call_stack.push(k);
        atts_stack.push(atts);
    }

    public void parseEnd()
    {
        Keyword k = call_stack.pop();
        Map<String,String> atts = atts_stack.pop();
        switch(k)
        {
            case sprite:{
            int x = Integer.parseInt(atts.get("x"));
            int y = Integer.parseInt(atts.get("y"));
            int w = Integer.parseInt(atts.get("w"));
            int h = Integer.parseInt(atts.get("h"));
            java.awt.Rectangle slice = new java.awt.Rectangle(x,y,w,h);

            URL url = ResourceBuilder.class.getResource(FILE_PATH_PREFIX+atts.get("file"));
            if (url == null)throw new RuntimeException("Cannot find resource: "+FILE_PATH_PREFIX+atts.get("file"));

            sprite_buffer.add(ResourceFactory.get().getSprite(url, slice));
            }
            break;

            case spritelist:{
            double framespeed = Double.parseDouble(atts.get("framespeed"));
            framespeed /= 1000; // spritelist need framespeed in milliseconds
            try{
                String id = "default";
                if(atts.containsKey("id"))id = atts.get("id");
                SpriteList sl = new SpriteList(framespeed);
                sl.addAll(sprite_buffer);
                spritelist_buffer.put(id, sl);
            }catch(Exception e){ Logger.log(e); }
            sprite_buffer.clear();
            }
            break;

            case entity:{
            try{
                String id = atts.get("id");
                createEntity(id, atts);
            }catch(Exception e){ Logger.log(e); }
            spritelist_buffer.clear();
            }break;
        }
    }


    private void createEntity(String id, Map<String,String> atts) {
        if(id.equals("NOTE_PANEL")){
                int x = Integer.parseInt(atts.get("x"));
                int y = Integer.parseInt(atts.get("y"));
                int y2 = Integer.parseInt(atts.get("y2"));
            SpriteList sl = spritelist_buffer.get("default");
            LaneEntity e = new LaneEntity(sl.get(0), Event.Channel.NONE, x, y, y2);
            result.put(id, e);
        }
        else
        if(id.startsWith("NOTE_")){
            SpriteList head = spritelist_buffer.get("HEAD");
            SpriteList body = spritelist_buffer.get("BODY");
            Event.Channel ch = Event.Channel.valueOf(id);
            NoteEntity n = new NoteEntity(render,head, ch, 0, 0);
            LongNoteEntity ln = new LongNoteEntity(render,head, body, ch, 0, 0);
            result.put(id, n);
            result.put("LONG_"+id, ln);
        }
        else
        if(id.equals("MEASURE_MARK")){
            SpriteList sl = spritelist_buffer.get("default");
            MeasureEntity m = new MeasureEntity(render,sl, Event.Channel.NONE, 0, 0);
            result.put(id, m);
        }
        else
        if(id.equals("JUDGMENT_LINE")){
            int x = Integer.parseInt(atts.get("x"));
            int y = Integer.parseInt(atts.get("y"));
            SpriteList sl = spritelist_buffer.get("default");
            AnimatedEntity e = new AnimatedEntity(sl, Event.Channel.NONE, x, y);
            result.put(id, e);
        }
        else
        if(id.equals("KEYBOARD_PANEL")){
            int x = Integer.parseInt(atts.get("x"));
            int y = Integer.parseInt(atts.get("y"));
            SpriteList sl = spritelist_buffer.get("default");
            Entity e = new Entity(sl.get(0), Event.Channel.NONE, x, y);
            result.put(id, e);
        }
        else
        if(id.startsWith("PRESSED_NOTE")){
            int x = Integer.parseInt(atts.get("x"));
            int y = Integer.parseInt(atts.get("y"));
            SpriteList sl = spritelist_buffer.get("default");
            Entity e = new FlareEffectEntity(sl, Event.Channel.NONE, x, y);
            result.put(id, e);
        }
        else
        if(id.equals("EFFECT_LONGFLARE")){
            SpriteList s = spritelist_buffer.get("default");
            FlareEffectEntity e = new FlareEffectEntity(s, Event.Channel.NONE, 0, 0);
            result.put(id, e);
        }
        else
        if(id.startsWith("EFFECT_")){
            SpriteList s = spritelist_buffer.get("default");
            EffectEntity e = new EffectEntity(s, Event.Channel.NONE, 0, 0);
            e.setScale(0.7f, 0.7f);
            result.put(id, e);
        }
    }

    public HashMap<String,Entity> getResult()
    {
        return result;
    }

    private Keyword getKeyword(String s)
    {
        try{
            return Keyword.valueOf(s);
        }catch(IllegalArgumentException e){
            Logger.die(new Exception("Unknown keyword ["+s+"] in resources.xml."));
        }
        return null;
    }
}
