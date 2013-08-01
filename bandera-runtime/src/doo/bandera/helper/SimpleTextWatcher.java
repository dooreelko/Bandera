package doo.bandera.helper;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {	}

	@Override
	public void afterTextChanged(Editable s) {	}

}
