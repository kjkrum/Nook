package com.chalcodes.touchpdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class TwoColumnStrategy implements ReadingStrategy {
	private final Document mDocument;

	public TwoColumnStrategy(@NonNull final Document document) {
		mDocument = document;
	}

	private static final float VIEWPORT_WIDTH = 0.5f;
	private static final float COLUMN_OVERLAP = 0.025f;

	@Override
	public RenderAsync render(final int width, final int height, @NonNull final Callback<Bitmap> callback) {
		mViewportAspect = (float) width / (float) height;
		final RectF viewport = new RectF();
		viewport.left = VIEWPORT_WIDTH * mColumn;
		// trim the margins and overlap slightly in the center
		if(mColumn == 0) {
			viewport.left += COLUMN_OVERLAP;
		}
		else {
			viewport.left -= COLUMN_OVERLAP;
		}
		viewport.right = viewport.left + VIEWPORT_WIDTH;

		viewport.top = mTopPosition;
		viewport.bottom = viewport.top + computeViewportHeight();
		if(viewport.bottom > 1f) {
			viewport.top -= viewport.bottom - 1f;
			viewport.bottom -= 1f;
		}
		return mDocument.render(viewport, width, height, callback);
	}

	/** 0-1 */
	private float mTopPosition = 0f;
	/** 0-1 */
	private int mColumn = 0;
	/** Width to height */
	private float mViewportAspect = 0f;

	/* pgUp and pgDn can't do anything until mViewportAspect has been set by render */

	private static final float STRIDE = 0.75f;

	@Override
	synchronized public boolean pgUp() {
		if(mViewportAspect == 0f) {
			return false;
		}
		final float viewportHeight = computeViewportHeight();
		if(isEqual(mTopPosition, 0f)) {
			// at top of page
			if(mColumn == 0) {
				// go to bottom of column 1 on previous page
				final int page = mDocument.getPage();
				if(page > 0) {
					mDocument.setPage(page - 1);
					mColumn = 1;
					mTopPosition = 1f - viewportHeight;
					return true;
				}
				else {
					return false;
				}
			}
			else {
				// go to bottom of column 0 on this page
				mColumn = 0;
				mTopPosition = 1f - viewportHeight;
				return true;
			}
		}
		else {
			// move viewport up
			mTopPosition -= viewportHeight * STRIDE;
			if(mTopPosition < 0f) {
				mTopPosition = 0f;
			}
			return true;
		}
	}

	@Override
	synchronized public boolean pgDn() {
		if(mViewportAspect == 0f) {
			return false;
		}
		final float viewportHeight = computeViewportHeight();
		if(isEqual(mTopPosition + viewportHeight, 1f)) {
			// at bottom of page
			if(mColumn == 1) {
				// go to top of column 0 on next page
				final int page = mDocument.getPage();
				if(page < mDocument.getPageCount() - 1) {
					mDocument.setPage(page + 1);
					mColumn = 0;
					mTopPosition = 0f;
					return true;
				}
				else {
					return false;
				}
			}
			else {
				// go to top of column 1 on this page
				mColumn = 1;
				mTopPosition = 0f;
				return true;
			}
		}
		else {
			// move viewport down
			mTopPosition += viewportHeight * STRIDE;
			if(mTopPosition + viewportHeight > 1f) {
				mTopPosition = 1f - viewportHeight;
			}
			return true;
		}
	}

	private float computeViewportHeight() {
		/* This algorithm maintains the proportions of the document.  For most
		 * documents, this results in pressing PgDn once more in each column
		 * to expose a single line of text or even just the empty bottom
		 * margin.  I decided that a one-third reduction in button presses is
		 * worth introducing a slight distortion. */
//		final RectF pageBounds = mDocument.getPageBounds();
//		final float pageAspect = pageBounds.width() / pageBounds.height();
//		return VIEWPORT_WIDTH / mViewportAspect * pageAspect;

		return VIEWPORT_WIDTH / mViewportAspect;
	}

	private static final float EPSILON = 0.03f;

	private boolean isEqual(final float a, final float b) {
		return Math.abs(a - b) < EPSILON;
	}
}
