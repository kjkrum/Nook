package com.chalcodes.touchpdf;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public interface Callback<T> {
	void onResult(T result);
	void onError(Throwable error);
}
