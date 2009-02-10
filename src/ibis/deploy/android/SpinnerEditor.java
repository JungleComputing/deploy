package ibis.deploy.android;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class SpinnerEditor {

    private Spinner mSpinner;

    private CheckBox mCheckBox;

    public SpinnerEditor(final Context context, final String value,
            final List<String> options, final LinearLayout layout) {
        this(context, value, null, options, layout);
    }

    public SpinnerEditor(final Context context, String item,
            final String defaultValue, final List<String> options,
            final LinearLayout layout) {
        mSpinner = (Spinner) layout.findViewById(R.id.edit);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, options);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);

        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        if (item != null) {
            mSpinner.setSelection(options.indexOf(item));
            mSpinner.setEnabled(true);
            mCheckBox.setChecked(true);
        } else {
            mSpinner.setSelection(Math.max(0, options.indexOf(defaultValue)));
            mSpinner.setEnabled(false);
            mCheckBox.setChecked(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked) {
                            mSpinner.setEnabled(true);
                            mCheckBox.setChecked(true);
                        } else {
                            mSpinner.setSelection(Math.max(0, options
                                    .indexOf(defaultValue)));
                            mSpinner.setEnabled(false);
                            mCheckBox.setChecked(false);
                        }
                    }
                });

    }

    public String getItem() {
        return mSpinner.getSelectedItem().toString();
    }

}
