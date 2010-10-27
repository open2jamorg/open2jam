package org.open2jam.render;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.tools.TimeProvider;

public class InterfaceController implements ScreenController
{
	/** nifty instance. */
	private Nifty nifty;

	public void newNifty()
	{
		nifty = new Nifty(
		new LwjglRenderDevice(),
		new NullSoundDevice(),
		new LwjglInputSystem(),
		new TimeProvider());
		nifty.fromXml("interface.xml","start");
	}

	public final void bind(final Nifty newNifty, final Screen newScreen) {
		this.nifty = newNifty;
	}

	/**
	* on start screen interactive.
	*/
	public final void onStartScreen() {
	}

	/**
	* on end screen.
	*/
	public final void onEndScreen() {
	}

	public void render()
	{
		nifty.render(true);
	}
}