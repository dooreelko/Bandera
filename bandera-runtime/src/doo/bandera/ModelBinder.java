package doo.bandera;

import java.util.Calendar;
import java.util.Date;

import android.net.Uri;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import doo.bandera.helper.SimpleTextWatcher;

//TODO: this goes to Bandera-runtime
public class ModelBinder {
	private static final Class<DatePicker> CLASS_NAME_DATE_PICKER = DatePicker.class;
	private static final Class<EditText> CLASS_NAME_EDIT_TEXT = EditText.class;
	private static final Class<TextView> CLASS_NAME_TEXT_VIEW = TextView.class;
	private static final Class<ImageView> CLASS_NAME_IMAGE_VIEW = ImageView.class;
	private static final Class<ImageButton> CLASS_NAME_IMAGE_BUTTON = ImageButton.class;

	
	private final View[] views;
	private final ModelNormalizer normalizer;
	private Object[] preValues;
	private boolean softUpdate;
	private boolean dirty;

	public ModelBinder(ModelNormalizer normalizer, View[] views) {
		this.normalizer = normalizer;
		this.views = views;

		this.preValues = normalizer.getModelValues();
		
		bindViewsValues();
	}

	private void bindViewsValues() {
		for (int x = 0; x < views.length; x++) {
			View v = views[x];
			bindOneViewValue(v, x);
			bindOneViewEvents(v, x);
		}
	}

	protected void bindOneViewValue(View v, int pos) {
		Class<? extends View> viewClass = v.getClass();
		Object value = normalizer.getModelValues()[pos];
		
		if (viewClass == CLASS_NAME_TEXT_VIEW){
			bindTextView((TextView)v, value.toString());
		} else if (viewClass == CLASS_NAME_EDIT_TEXT){
			bindTextView((EditText)v, value.toString());
		} else if (viewClass == CLASS_NAME_DATE_PICKER){
			bindDatePicker((DatePicker)v, (Date)value, pos);
		} else if (viewClass == CLASS_NAME_IMAGE_VIEW){
			bindImageView((ImageView)v, value);
		} else if (viewClass == CLASS_NAME_IMAGE_BUTTON){
			bindImageView((ImageButton)v, value);
		}
	}

	private void bindOneViewEvents(final View v, final int pos) {
		Class<? extends View> viewClass = v.getClass();
		
		if (viewClass == CLASS_NAME_EDIT_TEXT){
			bindEditTextEvents((EditText)v, pos);
		}

		// the rest has no events to bind or already bound
	}

	protected void notifyViewChanged(final Object newValue, final int pos) {
		normalizer.setModelValue(newValue, pos);

		Object[] newValues = normalizer.getModelValues();
		preValues[pos] = newValues[pos];

		updateDirtyValues();
	}

	public void updateDirtyValues() {
		softUpdate = true;

		// One pass only - no circular dependencies
		Object[] newValues = normalizer.getModelValues();

		for (int i=0; i<newValues.length; i++) {
			if (!preValues[i].equals(newValues[i])) {
				dirty = true;
				preValues[i] = newValues[i];
				bindOneViewValue(views[i], i);
			}
		}

		softUpdate = false;
	}

	private void bindTextView(final TextView v, final String value) {
		v.setText(value);
	}
	
	private void bindImageView(ImageView v, Object value) {
		Uri uri;
		if (value instanceof Uri) {
			uri = (Uri)value;
		} else {
			uri = Uri.parse(value.toString());
		}
		
		v.setImageURI(uri);
	}

	private void bindDatePicker(final DatePicker v, final Date value, final int pos) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(value);
		
		v.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), new OnDateChangedListener() {
			
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
