package doo.bandera;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import doo.bandera.ModelNormalizer.ViewState;
import doo.bandera.helper.SimpleTextWatcher;

//TODO: this goes to Bandera-runtime
public class ModelBinder {
	private static final Class<DatePicker> CLASS_NAME_DATE_PICKER = DatePicker.class;
	private static final Class<EditText> CLASS_NAME_EDIT_TEXT = EditText.class;
	private static final Class<TextView> CLASS_NAME_TEXT_VIEW = TextView.class;
	private static final Class<ImageView> CLASS_NAME_IMAGE_VIEW = ImageView.class;
	private static final Class<ImageButton> CLASS_NAME_IMAGE_BUTTON = ImageButton.class;
	private static final Class<ProgressBar> CLASS_NAME_PROGRESS_BAR = ProgressBar.class;

	private final View[] views;
	private final ModelNormalizer normalizer;
	private Object[] preValues;
	private boolean softUpdate;
	private boolean dirty;

	private ICanUpdateViews updater;

	public ModelBinder(Context ctx, ModelNormalizer normalizer, View[] views) {
		this(ctx, normalizer, views, null);
	}
	
	public ModelBinder(Context ctx, ModelNormalizer normalizer, View[] views, ICanUpdateViews viewUpdater) {
		this.normalizer = normalizer;
		this.views = views;

		this.preValues = normalizer.getModelValues();
		
		updater = viewUpdater == null ? new SimpleViewUpdater() : viewUpdater;
		
		bindViews();
		updateViewStates();
	}

	private void bindViews() {
		for (int x = 0; x < views.length; x++) {
			View v = views[x];
			bindOneView(v, x);
		}
	}

	protected void bindOneView(View v, int pos) {
		Class<? extends View> viewClass = v.getClass();
		Object value = normalizer.getModelValues()[pos];
		
		if (viewClass == CLASS_NAME_TEXT_VIEW){
			bindTextView((TextView)v, value);
		} else if (viewClass == CLASS_NAME_EDIT_TEXT){
			bindTextView((EditText)v, value);
			bindEditTextEvents((EditText)v, pos);
		} else if (viewClass == CLASS_NAME_DATE_PICKER){
			bindDatePicker((DatePicker)v, (Date)value, pos);
		} else if (viewClass == CLASS_NAME_IMAGE_VIEW){
			bindImageView((ImageView)v, value);
		} else if (viewClass == CLASS_NAME_IMAGE_BUTTON){
			bindImageView((ImageButton)v, value);
		} else if (viewClass == CLASS_NAME_PROGRESS_BAR){
			bindProgressBar((ProgressBar)v, value);
		}
	}

	protected void notifyViewChanged(final Object newValue, final int pos) {
		dirty = true;
		normalizer.setModelValue(newValue, pos);

		Object[] newValues = normalizer.getModelValues();
		preValues[pos] = newValues[pos];

		updateDirtyValues();
	}

	/**
	 * Call this to notify the binder that you've changed the model and it should reflect that
	 */
	public void updateDirtyValues() {
		softUpdate = true;

		// One pass only - no circular dependencies
		Object[] newValues = normalizer.getModelValues();

		for (int i=0; i<newValues.length; i++) {
			if (newValues[i] != null && !newValues[i].equals(preValues[i])) {
				dirty = true;
				preValues[i] = newValues[i];
				bindOneView(views[i], i);
			}
		}

		softUpdate = false;
		updateViewStates();
	}

	private void updateViewStates() {
		ViewState[] viewStates = normalizer.getViewStates();
		for (int x = 0; x < views.length; x++) {
			View v = views[x];
			
			switch (viewStates[x])
			{
				case NotSet:
					break;
				case Invisible:
					v.setVisibility(View.INVISIBLE);
					break;
				case Gone:
					v.setVisibility(View.GONE);
					break;
				case Normal:
					v.setEnabled(true);
					v.setVisibility(View.VISIBLE);
					break;
				case ReadOnly:
					v.setEnabled(false);
					break;
			}
		}		
	}

	private void bindTextView(final TextView v, final Object value) {
		if (value == null) {
			return;
		}
		
		if (value instanceof Date) {
			updater.updateTextView(v, (Date)value);
		} else {
			updater.updateTextView(v, value.toString());
		}
	}

	private void bindImageView(final ImageView v, final Object value) {
		if (value == null) {
			return;
		}
		Uri uri;
		if (value instanceof Uri) {
			uri = (Uri)value;
		} else {
			uri = Uri.parse(value.toString());
		}

		updater.updateImageView(v, uri);
	}

	private void bindProgressBar(final ProgressBar v, final Object value) {
		// polymorphism they said, it'll be fun they said
		int intValue = 0;
		if (value instanceof Long) {
			intValue = ((Long)value).intValue();
		}
		else {
			intValue = (Integer) value;
		}

		updater.updateProgressBar(v, intValue);
	}

	private void bindDatePicker(final DatePicker v, final Date value, final int pos) {
		final Calendar cal = Calendar.getInstance();
		if (value != null) {
			cal.setTime(value);
		}
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int dayOfMonth = cal.get(Calendar.DATE);
		updater.updateDatePicker(v, year, month, dayOfMonth);
		
		v.init(year, month, dayOfMonth, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				if (softUpdate) {
					return;
				}
				
				Calendar cal = Calendar.getInstance();
				cal.set(year, monthOfYear, dayOfMonth);

				notifyViewChanged(new Date(cal.getTimeInMillis()), pos);
			}
		});
	}


	private void bindEditTextEvents(final EditText v, final int pos) {
		//TODO: for now let's hope this will be called only once per EditText, mkay?
		//EditText doesn't provide something like removeAllTextChangedListeners
		v.addTextChangedListener(new SimpleTextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (softUpdate) {
					return;
				}
				notifyViewChanged(s, pos);
			}
		});
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
