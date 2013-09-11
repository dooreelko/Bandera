package doo.bandera;

import java.util.Date;

import android.net.Uri;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public interface ICanUpdateViews {

	public abstract void updateTextView(final TextView v, String stringValue);

	public abstract void updateTextView(final TextView v, final Date dateValue);

	public abstract void updateImageView(final ImageView v, Uri uri);

	public abstract void updateProgressBar(final ProgressBar v, final int intValue);

	public abstract void updateDatePicker(final DatePicker v, final int year, final int month, final int dayOfMonth);

}