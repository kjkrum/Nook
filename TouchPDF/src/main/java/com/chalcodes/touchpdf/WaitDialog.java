package com.chalcodes.touchpdf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class WaitDialog extends DialogFragment {
	private static final String ARG_MESSAGE = "message";

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		setCancelable(false);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_MESSAGE)) {
			builder.setMessage(getContext().getString(args.getInt(ARG_MESSAGE)));
		}
		return builder.create();
	}

	@Override
	public void onPause() {
		super.onPause();
		dismiss();
	}

	public static DialogFragment show(@NonNull final AppCompatActivity activity, @NonNull final int messageResId) {
		final Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, messageResId);
		DialogFragment dialog = new WaitDialog();
		dialog.setArguments(args);
		dialog.show(activity.getSupportFragmentManager(), null);
		return dialog;
	}
}
