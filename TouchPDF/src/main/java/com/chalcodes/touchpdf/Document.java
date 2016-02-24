package com.chalcodes.touchpdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import org.vudroid.pdfdroid.codec.PdfDocument;
import org.vudroid.pdfdroid.codec.PdfPage;

import java.io.Closeable;

/**
 * Manages a {@link PdfDocument} and the current {@link PdfPage}.  Instances
 * must be closed to free native resources.
 *
 * @author Kevin Krumwiede
 */
public class Document implements Closeable {
	public static final int NO_PAGE = -1;
	private final PdfDocument mDoc;
	private final int mPageCount;
	private PdfPage mPage;
	private int mPageIndex = NO_PAGE;

	/**
	 * Opens a document.  Before using the document returned to the callback,
	 * you must check the page count and set a page.
	 *
	 * @param fileName the file name
	 * @param callback the callback to receive the document
	 * @return the async task
	 */
	public static OpenAsync open(@NonNull final String fileName, @NonNull final Callback<Document> callback) {
		final OpenAsync async = new OpenAsync(fileName, new Callback<PdfDocument>() {
			@Override
			public void onResult(final PdfDocument result) {
				callback.onResult(new Document(result));
			}

			@Override
			public void onError(final Throwable error) {
				callback.onError(error);
			}
		});
		async.execute();
		return async;
	}

	private Document(@NonNull final PdfDocument doc) {
		mDoc = doc;
		mPageCount = doc.getPageCount();
	}

	/**
	 * Gets the number of pages in the document.
	 *
	 * @return the page count
	 * @throws IllegalStateException if the document is closed
	 */
	public int getPageCount() {
		if(mDoc.isRecycled()) {
			throw new IllegalStateException("document is closed");
		}
		return mPageCount;
	}

	/**
	 * Gets the current page number.  Returns {@link #NO_PAGE} if {@link
	 * #setPage(int)} has not been called.
	 *
	 * @return the page number, or {@link #NO_PAGE}
	 * @throws IllegalStateException if the document is closed
	 */
	public int getPage() {
		if(mDoc.isRecycled()) {
			throw new IllegalStateException("document is closed");
		}
		return mPageIndex;
	}

	/**
	 * Sets the current page.  Page numbers are zero-based.
	 *
	 * @param page the page number
	 * @throws IllegalStateException if the document is closed
	 * @throws IndexOutOfBoundsException if pageNum &lt; 0 or pageNum &gt;=
	 * {@link #getPageCount()}
	 */
	public void setPage(final int page) {
		if(mDoc.isRecycled()) {
			throw new IllegalStateException("document is closed");
		}
		if(page < 0 || page >= mPageCount) {
			throw new IndexOutOfBoundsException("invalid page number: " + page);
		}
		if(mPageIndex != page) {
			closePage();
			mPage = mDoc.getPage(page);
			mPageIndex = page;
		}
	}

	/**
	 * Gets the native bounds of the current page.
	 *
	 * @return the page bounds
	 * @throws IllegalStateException if the document is closed or {@link
	 * #setPage(int)} has not been called
	 */
	public RectF getPageBounds() {
		if(mDoc.isRecycled()) {
			throw new IllegalStateException("document is closed");
		}
		if(mPage == null) {
			throw new IllegalStateException("no page");
		}
		return mPage.getBounds();
	}

	/**
	 * Renders the specified region of the current page.  The top left corner
	 * of the page is 0, 0 and the bottom right corner is 1, 1.  The caller is
	 * responsible for recycling the bitmap returned to the callback.
	 *
	 * @param viewport the visible region of the page
	 * @param width the desired bitmap width
	 * @param height the desired bitmap height
	 * @param callback the callback to receive the bitmap
	 * @return the async task
	 */
	public RenderAsync render(@NonNull final RectF viewport, final int width, final int height, @NonNull Callback<Bitmap> callback) {
		if(mDoc.isRecycled()) {
			throw new IllegalStateException("document is closed");
		}
		if(mPage == null) {
			throw new IllegalStateException("no page");
		}
		final RenderAsync async = new RenderAsync(mPage, viewport, width, height, callback);
		async.execute();
		return async;
	}

	@Override
	public void close() {
		closePage();
		if(!mDoc.isRecycled()) {
			mDoc.recycle();
		}
	}

	private void closePage() {
		if(mPage != null)  {
			mPage.recycle();
			mPage = null;
			mPageIndex = NO_PAGE;
		}
	}
}
