package com.chalcodes.touchpdf;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import org.vudroid.pdfdroid.codec.PdfDocument;

/**
 * Opens a {@link PdfDocument}.
 *
 * @author Kevin Krumwiede
 */
public class OpenAsync extends AsyncTask<Void, Void, PdfDocument> {
	private final String mFileName;
	private final Callback<PdfDocument> mCallback;
	private Exception mError;

	public OpenAsync(@NonNull final String fileName, @NonNull final Callback<PdfDocument> callback) {
		mFileName = fileName;
		mCallback = callback;
	}

	@Override
	protected PdfDocument doInBackground(final Void... params) {
		try {
			return PdfDocument.openDocument(mFileName, null);
		}
		catch(Exception e) {
			mError = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute(final PdfDocument document) {
		if(document != null) {
			mCallback.onResult(document);
		}
		else {
			mCallback.onError(mError);
		}
	}

	@Override
	protected void onCancelled(final PdfDocument document) {
		if(document != null) {
			document.recycle();
		}
	}
}
