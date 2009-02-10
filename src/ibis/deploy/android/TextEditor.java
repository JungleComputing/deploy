package ibis.deploy.android;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

public class TextEditor {

    private EditText mEditText;

    private CheckBox mCheckBox;

    public TextEditor(final Context context, final String value,
            final LinearLayout layout) {
        this(context, value, null, layout);
    }

    public TextEditor(final Context context, String item,
            final String defaultValue, final LinearLayout layout) {
        mEditText = (EditText) layout.findViewById(R.id.edit);
        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        if (item != null) {
            mEditText.setText(item);
            mEditText.setEnabled(true);
            mCheckBox.setChecked(true);
        } else {
            mEditText.setText(defaultValue);
            mEditText.setEnabled(false);
            mCheckBox.setChecked(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked) {
                            mEditText.setEnabled(true);
                            mCheckBox.setChecked(true);
                        } else {
                            mEditText.setText(defaultValue);
                            mEditText.setEnabled(false);
                            mCheckBox.setChecked(false);
                        }

                    }
                });

    }

    public String getItem() {
        return mEditText.getText().toString();
    }

}
