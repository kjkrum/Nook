package com.chalcodes.touchpdf;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Displays an error message.  Finishes the activity when the "OK" button is
 * pressed.
 *
 * @author Kevin Krumwiede
 */
public class ErrorDialog extends DialogFragment {
	private static final String ARG_MESSAGE = "message";
	private static final String ARG_FATAL = "fatal";

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		setCancelable(false);
		final Bundle args = getArguments();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if(args != null && args.getBoolean(ARG_FATAL)) {
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					getActivity().finish();
				}
			});
		}
		else {
			builder.setNeutralButton(android.R.string.ok, null);
		}
		if(args != null && args.containsKey(ARG_MESSAGE)) {
			builder.setMessage(args.getInt(ARG_MESSAGE));
		}
		return builder.create();
	}

	@Override
	public void onPause() {
		super.onPause();
		dismiss();
	}

	public static void show(@NonNull final AppCompatActivity activity, final int messageId, boolean fatal) {
		final Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, messageId);
		args.putBoolean(ARG_FATAL, fatal);
		DialogFragment dialog = new ErrorDialog();
		dialog.setArguments(args);
		dialog.show(activity.getSupportFragmentManager(), null);
	}
}
