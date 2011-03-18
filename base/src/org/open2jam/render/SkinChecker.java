package org.open2jam.render;

import java.util.Map;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class SkinChecker extends DefaultHandler
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private enum Keyword {
        Resources, skin, spriteset, styles, style, sprite, frame, layer, entity;
    }

    HashMap<String, Boolean> IDs;

    ArrayList<String> error;
    ArrayList<String> warning;
    ArrayList<String> info;

    public enum Log {
        ERROR, WARNING, INFO
    }

    ArrayDeque<Keyword> call_stack;
    ArrayDeque<Map<String,String>> atts_stack;


    ArrayList<String> style_list;
    HashMap<String, ArrayList<String>> styles_map;

    private Skin result;

    private int layer = -1;

    private static String FILE_PATH_PREFIX = "/resources/";

    protected String target_skin;
    protected int auto_draw_id = 0;

    protected int baseW = 800;
    protected int baseH = 600;
    protected int keys = 7;


    public SkinChecker()
    {
        call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();

        style_list = new ArrayList<String>();
        styles_map = new HashMap<String, ArrayList<String>>();

        error = new ArrayList<String>();
        warning = new ArrayList<String>();
        info = new ArrayList<String>();

        result = new Skin();
    }

    Locator locator;
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
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
                if(check(k, atts_map, "name", Log.ERROR, "There is no skin name!"))
                    target_skin =  atts_map.get("name");
		if(check(k, atts_map, "keys", Log.WARNING, "There is no keys attribute, using the default one "+keys))
                    this.keys = Integer.parseInt(atts_map.get("keys"));
		if(check(k, atts_map, "width", Log.WARNING, "There is no width attribute, using the default one "+baseW))
                    this.baseW = Integer.parseInt(atts_map.get("width"));
                if(check(k, atts_map, "height", Log.WARNING, "There is no height attribute, using the default one "+baseH))
                    this.baseH = Integer.parseInt(atts_map.get("height"));
                if(check(k, atts_map, "judgment_line", Log.ERROR, "There is no judgment_line attribute!"))
                    result.judgment_line = Integer.parseInt(atts_map.get("judgment_line"));
            }break;

            case layer:{
                if(check(k, atts_map, "id", Log.ERROR, "There is NO id!"))
                    this.layer = Integer.parseInt(atts_map.get("id"));
//                if(this.layer > result.max_layer)result.max_layer = this.layer;
            }break;

            case sprite:{
                check(k, atts_map, "id", Log.ERROR, "There is NO id!");
            }break;
        }
    }

    @Override
    public void endElement(String uri,String localName,String qName)
    {
        Keyword k = call_stack.pop();
        Map<String,String> atts = atts_stack.pop();

        switch(k)
        {
            case frame:{
                int x, y, w, h;
                x = y = w = h = 0;
                if(check(k, atts, "x", Log.WARNING, "There is no x attribute, using "+x))
                    x = Integer.parseInt(atts.get("x"));
                if(check(k, atts, "y", Log.WARNING, "There is no y attribute, using "+y))
                    y = Integer.parseInt(atts.get("y"));
                if(check(k, atts, "w", Log.WARNING, "There is no w attribute, using "+w))
                    w = Integer.parseInt(atts.get("w"));
                if(check(k, atts, "h", Log.WARNING, "There is no h attribute, using "+h))
                    h = Integer.parseInt(atts.get("h"));

                float sx = 1, sy = 1;
                if(check(k, atts, "scale_x", Log.INFO, "There is no scale_x attribute, using "+sx))
                    sx = Float.parseFloat(atts.get("scale_x"));
                if(check(k, atts, "scale_y", Log.INFO, "There is no scale_x attribute, using "+sy))
                    sy = Float.parseFloat(atts.get("scale_y"));
                if(check(k, atts, "scale", Log.INFO, "There is no scale_x attribute, using "+sx))
                    sy = sx = Float.parseFloat(atts.get("scale"));

                URL url = null;
                if(check(k, atts, "file", Log.ERROR, "There is NO file!"))
                    url = SkinChecker.class.getResource(FILE_PATH_PREFIX+atts.get("file"));
                if(url == null)
                    check(k, atts, "filepath", Log.ERROR, "I can't find the file "+FILE_PATH_PREFIX+atts.get("file"));
            }break;

            case sprite:{
                double framespeed = 0;
                if(check(k, atts, "framespeed", Log.INFO, "If this sprite is animated it should have a framespeed value"))
                    framespeed = Double.parseDouble(atts.get("framespeed"));

                String id = null;
                if(atts.containsKey("id"))id = atts.get("id");
                else {
                    logger.severe("bad resource file ! sprite must have an ID !");
                    break;
                }
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
            String id = null;
            if(atts.containsKey("id"))id = atts.get("id");

            String sprites[] = null;
            if(atts.containsKey("sprite"))sprites = atts.get("sprite").split(",");
            else {
                logger.log(Level.SEVERE, "bad resource file ! entity [{0}] must have an sprite !", id);
                break;
            }

            if(sprites[0].trim().equals("")){
                logger.log(Level.SEVERE, "bad resource file ! entity [{0}] must have an sprite !", id);
                break;
            }
            }break;
        }
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
            logger.log(Level.WARNING, "Unknown keyword [{0}] in resources.xml.", s);
        }
        return null;
    }

    public HashMap<String, ArrayList<String>> getStyles()
    {
        return styles_map;
    }

    private boolean check(Keyword k, HashMap<String,String> atts, String att, Log l, String msg)
    {
        if(!atts.containsKey(att))
        {
            getLog(l).add("<b>&lt;"+k.toString()+" : "+locator.getLineNumber()+"&gt;</b> <i>"+att+"</i>: \""+msg+"\"");
            return false;
        }
        else
            return true;
    }

    private boolean check(Keyword k, Map<String,String> atts, String att, Log l, String msg)
    {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.putAll(atts);
        return check(k, hm, att, l, msg);
    }

    public int getBaseW() { return baseW; }
    public int getBaseH() { return baseH; }

    public ArrayList<String> getLog(Log l)
    {
        switch(l)
        {
            case ERROR:     return error;
            case WARNING:   return warning;
            case INFO:      return info;
            default:        return null;
        }
    }
}
