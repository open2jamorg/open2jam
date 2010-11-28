package org.open2jam.render;

import java.awt.Rectangle;
import java.util.Map;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import org.open2jam.util.Logger;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.AnimatedEntity;
import org.open2jam.render.entities.CompositeEntity;
import org.open2jam.render.entities.EffectEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;

public class ResourceBuilder
{
    private enum Keyword {
        Resources, skin, layer, entity, sprite, frame, judgment, type;
    }

    ArrayDeque<Keyword> call_stack;
    ArrayDeque<Map<String,String>> atts_stack;

    ArrayList<Sprite> frame_buffer;
    HashMap<String, Entity> sprite_buffer;

    HashMap<String, Double> judgment_type_map;

    private Skin result;

    private int layer = -1;

    private static String FILE_PATH_PREFIX = "/resources/";

    protected Render render;
    protected String target_skin;
    protected boolean on_skin = false;
    protected int auto_draw_id = 0;

    public ResourceBuilder(Render r, String skin)
    {
        this.render = r;
        this.target_skin = skin;
        call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();
        frame_buffer = new ArrayList<Sprite>();
        sprite_buffer = new HashMap<String, Entity>();
        judgment_type_map = new HashMap<String,Double>();
        result = new Skin();
    }
    
    public void parseStart(String s, Map<String,String> atts)
    {
        Keyword k = getKeyword(s);
        call_stack.push(k);
        atts_stack.push(atts);

        switch(k)
        {
            case skin:{
                if(atts.get("name").equals(target_skin))on_skin = true;
            }break;

            case layer:{
                this.layer = Integer.parseInt(atts.get("id"));
                if(this.layer > result.max_layer)result.max_layer = this.layer;
            }break;
        }
    }

    public void parseEnd()
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

            Rectangle slice = new Rectangle(x,y,w,h);

            URL url = ResourceBuilder.class.getResource(FILE_PATH_PREFIX+atts.get("file"));
            if(url == null)throw new RuntimeException("Cannot find resource: "+FILE_PATH_PREFIX+atts.get("file"));

            Sprite s = ResourceFactory.get().getSprite(url, slice);
            s.setScale(sx, sy);
            frame_buffer.add(s);
            }
            break;

            case sprite:{
            int x = atts.containsKey("x") ? Integer.parseInt(atts.get("x")) : 0;
            int y = atts.containsKey("y") ? Integer.parseInt(atts.get("y")) : 0;
            double framespeed = 0;
            if(atts.containsKey("framespeed"))framespeed = Double.parseDouble(atts.get("framespeed"));
            framespeed /= 1000; // spritelist need framespeed in milliseconds
            try{
                String id = null;
                if(atts.containsKey("id"))id = atts.get("id");
                else {
                    id = "AUTODRAW_SPRITE_"+auto_draw_id;
                    auto_draw_id++;
                }

                SpriteList sl = new SpriteList(framespeed);
                sl.addAll(frame_buffer);

                Entity e = null;
                if(sl.size() == 1)e = new Entity(sl, Event.Channel.NONE, x, y);
                else e = new AnimatedEntity(sl, Event.Channel.NONE, x, y);
                
                sprite_buffer.put(id, e);
            }catch(Exception e){ Logger.log(e); }
            frame_buffer.clear();
            }
            break;

            case entity:{
            Entity e = null;

            String id = null;
            if(atts.containsKey("id"))id = atts.get("id");

            if(id != null && (e = promoteEntity(id, atts)) != null){
                    // ok
            }
            else if(sprite_buffer.size() > 1){
                e = new CompositeEntity(sprite_buffer.values());
            }
            else{
                e = sprite_buffer.values().iterator().next();
            }

            e.setLayer(this.layer);
            
            if(id != null)result.addNamed(id, e);
            else result.add(e);
            
            sprite_buffer.clear();
            }break;

            case type:{
                String name = atts.get("id");
                Double hit = Double.parseDouble(atts.get("hit"));
                judgment_type_map.put(name, hit);
            }break;

            case judgment:{
                Integer start = Integer.parseInt(atts.get("start"));
                Integer size = Integer.parseInt(atts.get("size"));

                result.judgment.start = start;
                result.judgment.size = size;

                // TODO: the types
            }break;
        }
    }

    private Entity promoteEntity(String id, Map<String,String> atts)
    {
        Entity e = null;
        if(id.startsWith("NOTE_")){
            Entity head = sprite_buffer.remove("HEAD");
            Entity body = sprite_buffer.remove("BODY");

            e = new LongNoteEntity(render, head.getFrames(), body.getFrames(), Event.Channel.valueOf(id), head.getX(), head.getY());
            e.setLayer(layer);
            result.addNamed("LONG_"+id, e);
            e = new NoteEntity(render, head.getFrames(), Event.Channel.valueOf(id), head.getX(), head.getY());
        }
        else if(id.equals("MEASURE_MARK")){
            Entity sprite = sprite_buffer.values().iterator().next();
            e = new MeasureEntity(render, sprite.getFrames(), Event.Channel.NONE, sprite.getX(), sprite.getY());
        }
        else if(id.startsWith("EFFECT_")){
            Entity t = sprite_buffer.values().iterator().next();
            e = new EffectEntity(t.getFrames(), t.getChannel(), t.getX(), t.getY());
        }
        else if(id.startsWith("PRESSED_NOTE_")){
            e = new CompositeEntity(sprite_buffer.values());
        }
        else{

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
            Logger.die(new Exception("Unknown keyword ["+s+"] in resources.xml."));
        }
        return null;
    }
}
