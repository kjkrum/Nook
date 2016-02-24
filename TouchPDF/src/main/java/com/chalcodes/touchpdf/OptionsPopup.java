package com.chalcodes.touchpdf;

import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class OptionsPopup extends PopupWindow {
	private RadioGroup mStrategyGroup;

	public OptionsPopup(final MainActivity activity, ReadingStrategyFactory currentFactory) {
		super(activity);
		final ViewGroup root = (ViewGroup) activity.findViewById(R.id.root);
		final View popupLayout = activity.getLayoutInflater().inflate(R.layout.popup_options, root, false);
		setContentView(popupLayout);
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);

		final RadioGroup strategyGroup = (RadioGroup) popupLayout.findViewById(R.id.strategy_group);
		strategyGroup.check(mResIds.get(currentFactory));

		strategyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final RadioGroup group, final int checkedId) {
				activity.setReadingStrategy(mReadingStrategies.get(checkedId));
				dismiss();
			}
		});
	}

	private static final Map<ReadingStrategyFactory, Integer> mResIds
			= new EnumMap<>(ReadingStrategyFactory.class);
	private static final Map<Integer, ReadingStrategyFactory> mReadingStrategies;

	static {
		// TODO add new strategy factories here
		mResIds.put(ReadingStrategyFactory.FULL_PAGE, R.id.full_page);
		mResIds.put(ReadingStrategyFactory.TWO_COLUMN, R.id.two_column);

		mReadingStrategies = new HashMap<>(mResIds.size());
		for(Map.Entry<ReadingStrategyFactory, Integer> entry : mResIds.entrySet()) {
			if(mReadingStrategies.put(entry.getValue(), entry.getKey()) != null) {
				throw new RuntimeException("duplicate resource ID mapping");
			};
		}
	}
}
