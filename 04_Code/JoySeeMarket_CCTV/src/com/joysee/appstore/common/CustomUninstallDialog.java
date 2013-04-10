package com.joysee.appstore.common;

import com.joysee.appstore.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class CustomUninstallDialog extends Dialog {

	public CustomUninstallDialog(Context context, int theme) {
		super(context, theme);
	}
	public CustomUninstallDialog(Context context) {
		super(context);
	}

	/**
	 * 
	 * Helper class for creating a custom dialog
	 */

	public static class Builder {
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private DialogInterface.OnClickListener
		positiveButtonClickListener,
		negativeButtonClickListener;
		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * 
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * 
		 * @return
		 */
		private Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * 
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * 
		 * @return
		 */

		private Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomUninstallDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomUninstallDialog dialog = new CustomUninstallDialog(context,R.style.CustomUninstallDialog);
			View layout = inflater.inflate(R.layout.uninstall_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			((TextView) layout.findViewById(R.id.title)).setText(title);
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.yes)).setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.yes)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
						}
					});

				}
			} else {
				layout.findViewById(R.id.yes).setVisibility(View.GONE);

			}
			// set the cancel button
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.no)).setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.no)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
						}
					});

				}
			} else {
				layout.findViewById(R.id.no).setVisibility(
				View.GONE);
			}

			// set the content message
			if (message != null) {

//				((TextView) layout.findViewById(
//
//				R.id.message)).setText(message);

			} else if (contentView != null) {/*

				// if no message set

				// add the contentView to the dialog body

				((LinearLayout) layout.findViewById(R.id.content))

				.removeAllViews();

				((LinearLayout) layout.findViewById(R.id.content))

				.addView(contentView,

				new LayoutParams(

				LayoutParams.WRAP_CONTENT,

				LayoutParams.WRAP_CONTENT));

			*/}

			dialog.setContentView(layout);
			return dialog;
		}
	}
}
