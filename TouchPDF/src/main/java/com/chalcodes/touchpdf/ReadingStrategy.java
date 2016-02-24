package com.chalcodes.touchpdf;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Controls the size and position of the visible region of the document.
 *
 * @author Kevin Krumwiede
 */
public interface ReadingStrategy {
	/**
	 * Renders the document viewport.  The caller is responsible for recycling
	 * the bitmap received in the callback.
	 *
	 * @param width the desired bitmap width
	 * @param height the desired bitmap height
	 * @param callback the render callback
	 */
	RenderAsync render(int width, int height, @NonNull Callback<Bitmap> callback);

	/**
	 * Moves the document viewport up.  Called in the main thread.
	 *
	 * @return true if the document viewport changed; otherwise false
	 */
	boolean pgUp();

	/**
	 * Moves the document viewport down.  Called in the main thread.
	 *
	 * @return true if the document viewport changed; otherwise false
	 */
	boolean pgDn();
}
