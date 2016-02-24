package com.chalcodes.touchpdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Stretches the full page to fit the screen.
 *
 * @author Kevin Krumwiede
 */
public class FullPageStrategy implements ReadingStrategy {
	private final Document mDocument;
	private final RectF mViewport = new RectF(0f, 0f, 1f, 1f);

	public FullPageStrategy(@NonNull final Document document) {
		mDocument = document;
	}

	@Override
	public RenderAsync render(final int width, final int height, @NonNull final Callback<Bitmap> callback) {
		return mDocument.render(mViewport, width, height, callback);
	}

	@Override
	public boolean pgUp() {
		final int page = mDocument.getPage();
		if(page > 0) {
			mDocument.setPage(page - 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean pgDn() {
		final int page = mDocument.getPage();
		if(page < mDocument.getPageCount() - 1) {
			mDocument.setPage(page + 1);
			return true;
		}
		return false;
	}
}
