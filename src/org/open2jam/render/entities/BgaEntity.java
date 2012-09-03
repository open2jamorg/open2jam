/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.render.entities;

import com.sun.jna.Memory;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.logging.Level;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.render.Sprite;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

/**
 *
 * @author CdK
 */
public class BgaEntity extends Entity implements TimeEntity, RenderCallback {

    public boolean isVideo = false;
    public File videoFile;
    boolean isPlaying = false;
    boolean newBuffer = false;
    
    MediaPlayerFactory playerFactory;
    DirectMediaPlayer player;
    ByteBuffer videoBuffer = null;
    
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int DEPTH = 4;
    private static final int BUFFER_SIZE = WIDTH * HEIGHT * DEPTH;
    
    
    private LinkedList<Double> times;
    
    private LinkedList<Sprite> next_sprites;
    
    private double scale_w = 0, scale_h = 0;

    public BgaEntity(Sprite s, double x, double y) {
	super(s, x, y);
	scale_w = width;
	scale_h = height;
	next_sprites = new LinkedList<Sprite>();
	times = new LinkedList<Double>();
    }
    
    public BgaEntity(BgaEntity org) {
	this.sprite = org.sprite;
	this.x = org.x;
	this.y = org.y;
	this.scale_w = org.scale_w;
	this.scale_h = org.scale_h;
	this.next_sprites = org.next_sprites;
	this.times = org.times;
	this.playerFactory = org.playerFactory;
	this.player = org.player;
	this.isVideo = org.isVideo;
	this.videoFile = org.videoFile;
    }
    
    public void initVideo() {
	if(!isVideo) return;
	try {
	    playerFactory = new MediaPlayerFactory(new String[] {"--no-video-title-show", "--noaudio"});
	    player = playerFactory.newDirectMediaPlayer("RGBA", WIDTH, HEIGHT, WIDTH * DEPTH, this);
	    player.prepareMedia(videoFile.getAbsolutePath());
	} catch(Throwable t) {
	    isVideo = false;
            t.printStackTrace();
	    Logger.global.log(Level.WARNING, "VLC failed to load :(");
	}
    }
    
    public void draw() {
	if(isVideo) {
	    if(newBuffer) {
		sprite.draw(x, y, WIDTH, HEIGHT, videoBuffer);
		newBuffer = false;
	    } else {
		sprite.draw(x, y);
	    }
	} else if(sprite != null) {
	    super.draw();
	}
    }
    
    public void setSprite(Sprite s) {
	float w = (float) (scale_w/s.getWidth());
	float h = (float) (scale_h/s.getHeight());
	s.setScale(w, h);
	next_sprites.push(s);
    }
    
    @Override
    public void setTime(double t) {
	times.push(t);
    }

    @Override
    public double getTime() {
	if(times.isEmpty()) return -1;
	return times.getLast();
    }

    @Override
    public void judgment() {
	if(isVideo && !isPlaying) {
	    float w = (float) (scale_w/WIDTH);
	    float h = (float) (scale_h/HEIGHT);
	    sprite.setScale(w, h);
	    player.play();
	    isPlaying = true;
	} else {
	    if(next_sprites.isEmpty()) return;
	    if(times.isEmpty()) return;

	    times.removeLast();
	    this.sprite = next_sprites.removeLast();
	    this.width = sprite.getWidth();
	    this.height = sprite.getHeight();
	}
    }
    
    public void release() {
	if(!isVideo) return;
	player.stop();
	player.release();
	playerFactory.release();
	player = null;
	playerFactory = null;
    }
       
    @Override
    public BgaEntity copy(){
        return new BgaEntity(this);
    }

    @Override
    public void display(Memory memory) {
	if(BUFFER_SIZE <= 0) return;
	videoBuffer = memory.getByteBuffer(0, BUFFER_SIZE);
	newBuffer = true;
    }
}
