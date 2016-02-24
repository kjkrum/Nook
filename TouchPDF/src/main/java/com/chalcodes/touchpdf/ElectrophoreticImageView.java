package com.chalcodes.touchpdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An {@link ImageView} that clears itself to black, then white, before
 * drawing its content.  This helps avoid ghosting on electrophoretic (e-ink)
 * displays.
 *
 * @author Kevin Krumwiede
 */
public class ElectrophoreticImageView extends ImageView {

	private int mDrawStep = 0;

	@Override
	protected void onDraw(@NonNull final Canvas canvas) {
		switch(mDrawStep) {
			case 0:
				canvas.drawColor(Color.BLACK);
				++mDrawStep;
				invalidate();
				break;
			case 1:
				canvas.drawColor(Color.WHITE);
				++mDrawStep;
				invalidate();
				break;
			case 2:
				super.onDraw(canvas);
				mDrawStep = 0;
				break;
		}
	}

	public ElectrophoreticImageView(final Context context) {
		super(context);
	}

	public ElectrophoreticImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ElectrophoreticImageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
