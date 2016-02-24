package com.chalcodes.touchpdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import org.vudroid.pdfdroid.codec.PdfPage;

/**
 * Renders a page.
 *
 * @author Kevin Krumwiede
 */
public class RenderAsync extends AsyncTask<Void, Void, Bitmap> {
	private final PdfPage mPage;
	private final RectF mViewport;
	private final int mWidth;
	private final int mHeight;
	private final Callback<Bitmap> mCallback;
	private Exception mError;

	public RenderAsync(@NonNull final PdfPage page, @NonNull final RectF viewport,
					   final int width, final int height, @NonNull final Callback<Bitmap> callback) {
		mPage = page;
		mViewport = new RectF(viewport);
		mWidth = width;
		mHeight = height;
		mCallback = callback;
	}

	@Override
	protected Bitmap doInBackground(final Void... params) {
		try {
			return mPage.renderBitmap(mWidth, mHeight, mViewport);
		}
		catch(Exception e) {
			mError = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute(final Bitmap bitmap) {
		if(bitmap != null) {
			mCallback.onResult(bitmap);
		}
		else {
			mCallback.onError(mError);
		}
	}

	@Override
	protected void onCancelled(final Bitmap bitmap) {
		if(bitmap != null) {
			bitmap.recycle();
		}
	}
}
