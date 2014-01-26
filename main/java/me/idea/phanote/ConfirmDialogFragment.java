package me.idea.phanote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class ConfirmDialogFragment extends DialogFragment {
	private ConfirmDialogListener mListener;
	private int resId;
	
	public interface ConfirmDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	public ConfirmDialogFragment(ListFragment fragment) {
		mListener = (ConfirmDialogListener) fragment;
		resId = R.string.delete_note;
	}
	
	public ConfirmDialogFragment(Activity activity, int id) {
		mListener = (ConfirmDialogListener) activity;
		resId = id;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(resId);
		
		builder.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogPositiveClick(ConfirmDialogFragment.this);
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogNegativeClick(ConfirmDialogFragment.this);
			}
		});
		
		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
}