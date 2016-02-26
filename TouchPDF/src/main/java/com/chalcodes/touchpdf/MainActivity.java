package com.chalcodes.touchpdf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

/**
 * PDF viewer activity.
 *
 * @author Kevin Krumwiede
 */
public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String SCHEME_FILE = "file";
	private Handler mHandler;
	private ImageView mImageView;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(isFileIntent(getIntent())) {
			setContentView(R.layout.activity_main);
			mHandler = new Handler(Looper.getMainLooper());
			mImageView = (ImageView) findViewById(R.id.imageView);
			mImageView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(final View v) {
					showOptionsPopup();
					return true;
				}
			});
			openDocument(getIntent());
		}
		else {
			ErrorDialog.show(this, R.string.error_bogus_intent, true);
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		if(isFileIntent(intent)) {
			if(!intent.getData().equals(getIntent().getData())) {
				closeDocument();
				setIntent(intent);
				openDocument(intent);
			}
		}
		else {
			ErrorDialog.show(this, R.string.error_bogus_intent, false);
		}
	}

	private static boolean isFileIntent(final Intent intent) {
		return intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) &&
				intent.getData() != null && SCHEME_FILE.equals(intent.getData().getScheme());
	}

	private Document mDocument;
	private ReadingStrategyFactory mReadingStrategyFactory;
	private ReadingStrategy mReadingStrategy;
	private OpenAsync mOpenAsync;
	private RenderAsync mRenderAsync;
	private DialogFragment mWaitDialog;

	private void openDocument(@NonNull final Intent intent) {
		mWaitDialog = WaitDialog.show(MainActivity.this, R.string.opening);
		final String file = intent.getData().getPath();
		Log.d(TAG, "opening " + file);
		mOpenAsync = Document.open(file, new Callback<Document>() {
			@Override
			public void onResult(final Document result) {
				dismissWaitDialog();
				mOpenAsync = null;
				prepareDocument(result);
			}

			@Override
			public void onError(final Throwable error) {
				dismissWaitDialog();
				mOpenAsync = null;
				ErrorDialog.show(MainActivity.this, R.string.error_opening, true);
				if(error != null) {
					Log.w(TAG, "file error", error);
				}
			}
		});
	}

	private void prepareDocument(@NonNull final Document document) {
		if(document.getPageCount() > 0) {
			document.setPage(0);
			mDocument = document;
			/* If this isn't posted, sometimes the image view hasn't been
			 * measured when it tries to render.  This happens consistently
			 * with certain files and not with others. */
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO this could be a persistent setting
					setReadingStrategy(ReadingStrategyFactory.getDefault());
				}
			});
		}
		else {
			ErrorDialog.show(this, R.string.error_empty_document, true);
		}
	}

	private void closeDocument() {
		if(mOpenAsync != null) {
			mOpenAsync.cancel(true);
			mOpenAsync = null;
			dismissWaitDialog();
		}
		if(mDocument != null) {
			mDocument.close();
			mDocument = null;
		}
		cancelRender();
		mReadingStrategyFactory = null;
		mReadingStrategy = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDocument();
		recycleImageViewBitmap();
	}

	void setReadingStrategy(final ReadingStrategyFactory factory) {
		if(mReadingStrategyFactory != factory) {
			mReadingStrategy = factory.newInstance(mDocument);
			mReadingStrategyFactory = factory;
			render();
		}
	}

	private final Runnable mShowRenderingDialogTask = new Runnable() {
		@Override
		public void run() {
			mWaitDialog = WaitDialog.show(MainActivity.this, R.string.rendering);
		}
	};

	private static final long SHOW_RENDER_DIALOG_DELAY = 1000L;

	private void render() {
		cancelRender();
		mHandler.postDelayed(mShowRenderingDialogTask, SHOW_RENDER_DIALOG_DELAY);
		mRenderAsync = mReadingStrategy.render(mImageView.getWidth(), mImageView.getHeight(), new Callback<Bitmap>() {
			@Override
			public void onResult(final Bitmap result) {
				mRenderAsync = null;
				mHandler.removeCallbacks(mShowRenderingDialogTask);
				dismissWaitDialog();
				recycleImageViewBitmap();
				mImageView.setImageBitmap(result);
			}

			@Override
			public void onError(final Throwable error) {
				mRenderAsync = null;
				mHandler.removeCallbacks(mShowRenderingDialogTask);
				dismissWaitDialog();
				ErrorDialog.show(MainActivity.this, R.string.error_rendering, false);
				if(error != null) {
					Log.d(TAG, "rendering error", error);
				}
			}
		});
	}

	private void cancelRender() {
		if(mRenderAsync != null) {
			mRenderAsync.cancel(true);
			mRenderAsync = null;
			mHandler.removeCallbacks(mShowRenderingDialogTask);
			dismissWaitDialog();
		}
	}

	private void showOptionsPopup() {
		final PopupWindow popup = new OptionsPopup(this, mReadingStrategyFactory);
		popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mImageView.invalidate();
			}
		});
		popup.showAtLocation(findViewById(R.id.root), Gravity.BOTTOM, 0, 0);
	}

	private void dismissWaitDialog() {
		if(mWaitDialog != null) {
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}

	private void recycleImageViewBitmap() {
		final Drawable drawable = mImageView.getDrawable();
		if(drawable instanceof BitmapDrawable) {
			((BitmapDrawable) drawable).getBitmap().recycle();
		}
	}

	// these start at KeyEvent.LAST_KEYCODE + 1
	private static final int KEYCODE_TOP_LEFT = 92;
	private static final int KEYCODE_BOTTOM_LEFT = 93;
	private static final int KEYCODE_TOP_RIGHT = 94;
	private static final int KEYCODE_BOTTOM_RIGHT = 95;

	@Override
	public boolean onKeyDown(final int keyCode, @NonNull final KeyEvent event) {
		// TODO make key actions configurable
		switch(keyCode) {
			case KEYCODE_TOP_LEFT:
				// TODO show page select dialog
				return true;
			case KEYCODE_BOTTOM_LEFT:
				finish();
				return true;
			case KEYCODE_TOP_RIGHT:
				if(mReadingStrategy.pgUp()) {
					render();
				}
				return true;
			case KEYCODE_BOTTOM_RIGHT:
				if(mReadingStrategy.pgDn()) {
					render();
				}
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

//	@Override
//	public boolean onKeyDown(final int keyCode, @NonNull final KeyEvent event) {
//		if(mKeyActions.containsKey(keyCode)) {
//			event.startTracking();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//
//	@Override
//	public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
//		return mKeyActions.containsKey(keyCode) || super.onKeyLongPress(keyCode, event);
//	}
//
//	@Override
//	public boolean onKeyUp(final int keyCode, @NonNull final KeyEvent event) {
//		if(mKeyActions.containsKey(keyCode)) {
//			if((event.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) != 0) {
//				Log.d("onKeyUp", "keyCode: " + keyCode + " (long press)");
//			}
//			else {
//				Log.d("onKeyUp", "keyCode: " + keyCode + " (short press)");
//			}
//			mPageView.zoomTo(mPageView.getZoomMode() + 1f);
//			mPageView.invalidate();
//			return true;
//		}
//		return super.onKeyUp(keyCode, event);
//	}
}
