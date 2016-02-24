package com.chalcodes.touchpdf;

import android.support.annotation.NonNull;

/**
 * Constants identify and serve as factories for {@link ReadingStrategy}
 * implementations.
 *
 * @author Kevin Krumwiede
 */
public enum ReadingStrategyFactory {
	FULL_PAGE {
		@Override
		public ReadingStrategy newInstance(@NonNull final Document document) {
			return new FullPageStrategy(document);
		}
	},
	TWO_COLUMN {
		@Override
		public ReadingStrategy newInstance(@NonNull final Document document) {
			return new TwoColumnStrategy(document);
		}
	};

	abstract public ReadingStrategy newInstance(@NonNull final Document document);

	public static ReadingStrategyFactory getDefault() {
		return FULL_PAGE;
	}
}
