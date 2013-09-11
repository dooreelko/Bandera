package doo.bandera;

import java.text.DateFormat;
import java.util.Date;

import android.net.Uri;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SimpleViewUpdater implements ICanUpdateViews {
	private DateFormat dateFormatter = DateFormat.getDateInstance();	

	@Override
	public void updateTextView(final TextView v, String stringValue) {
		v.setText(stringValue);
	}

	@Override
	public void updateTextView(final TextView v, final Date dateValue) {
		String stringValue;
		if (dateFormatter != null) {
			stringValue = dateFormatter.format(dateValue);
		}
		else {
			stringValue = dateValue.toString();
		}
		
		v.setText(stringValue);
	}
	
	@Override
	public void updateImageView(final ImageView v, Uri uri) {
		v.setImageURI(uri);
	}
	
	@Override
	public void updateProgressBar(final ProgressBar v, final int intValue) {
		v.setMax(100);
		v.setProgress(intValue);
	}

	@Override
	public void updateDatePicker(final DatePicker v, final int year, final int month, final int dayOfMonth) {
		v.updateDate(year, month, dayOfMonth);
	}

}
