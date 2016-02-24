package org.vudroid.pdfdroid.codec;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import java.nio.ByteBuffer;

public class PdfPage {
	private long pageHandle;
	private long docHandle;

	static PdfPage createPage(long dochandle, int pageno) {
		return new PdfPage(open(dochandle, pageno), dochandle);
	}

	private PdfPage(long pageHandle, long docHandle) {
		this.pageHandle = pageHandle;
		this.docHandle = docHandle;
	}

	public RectF getBounds() {
		return getMediaBox();
	}

	synchronized public Bitmap renderBitmap(int width, int height, RectF zoomBounds) {
		Matrix matrix = new Matrix();
		final RectF pageBounds = getMediaBox();
		matrix.postScale(width / pageBounds.width(), -height / pageBounds.height());
		matrix.postTranslate(0, height);
		matrix.postTranslate(-zoomBounds.left * width, -zoomBounds.top * height);
		matrix.postScale(1 / zoomBounds.width(), 1 / zoomBounds.height());

		// experimental - see if this affects text clarity
		final float[] values = new float[9];
		matrix.getValues(values);
		for(int i = 0; i < values.length; ++i) {
			final int rounded = Math.round(values[i]);
			if(Math.abs(values[i] - rounded) < 0.03) {
				values[i] = rounded;
			}
		}
		matrix.setValues(values);

		return render(new Rect(0, 0, width, height), matrix);
	}

	private RectF getMediaBox() {
		float[] box = new float[4];
		getMediaBox(pageHandle, box);
		return new RectF(box[0], box[1], box[2], box[3]);
	}

	private Bitmap render(Rect viewbox, Matrix matrix) {
		int[] mRect = new int[4];
		mRect[0] = viewbox.left;
		mRect[1] = viewbox.top;
		mRect[2] = viewbox.right;
		mRect[3] = viewbox.bottom;

		float[] matrixSource = new float[9];
		float[] matrixArray = new float[6];
		matrix.getValues(matrixSource);
		matrixArray[0] = matrixSource[0];
		matrixArray[1] = matrixSource[3];
		matrixArray[2] = matrixSource[1];
		matrixArray[3] = matrixSource[4];
		matrixArray[4] = matrixSource[2];
		matrixArray[5] = matrixSource[5];

		int width = viewbox.width();
		int height = viewbox.height();
		int[] bufferarray = new int[width * height];
		nativeCreateView(docHandle, pageHandle, mRect, matrixArray, bufferarray);
		return Bitmap.createBitmap(bufferarray, width, height, Bitmap.Config.RGB_565);
		/*ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 2);
        render(docHandle, docHandle, mRect, matrixArray, buffer, ByteBuffer.allocateDirect(width * height * 8));
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;*/
	}

	synchronized public void recycle() {
		if(pageHandle != 0) {
			free(pageHandle);
			pageHandle = 0;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		recycle();
		super.finalize();
	}

	private static native void getMediaBox(long handle, float[] mediabox);

	private static native void free(long handle);

	private static native long open(long dochandle, int pageno);

	private static native void render(long dochandle, long pagehandle,
									  int[] viewboxarray, float[] matrixarray,
									  ByteBuffer byteBuffer, ByteBuffer tempBuffer);

	private native void nativeCreateView(long dochandle, long pagehandle,
										 int[] viewboxarray, float[] matrixarray,
										 int[] bufferarray);

}
