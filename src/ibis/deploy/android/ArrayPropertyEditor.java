package ibis.deploy.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArrayPropertyEditor {

    private List<EditText> mKeys = new ArrayList<EditText>();

    private List<EditText> mValues = new ArrayList<EditText>();

    private CheckBox mCheckBox;

    public ArrayPropertyEditor(final Context context, final List<String> keys,
            final List<String> values, final LinearLayout layout) {
        this(context, keys, values, null, null, layout);
    }

    public ArrayPropertyEditor(final Context context, List<String> keys,
            List<String> values, final List<String> defaultKeys,
            final List<String> defaultValues, final LinearLayout layout) {

        final LinearLayout container = (LinearLayout) layout
                .findViewById(R.id.container);
        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        final Button addArgument = (Button) container.findViewById(R.id.add);
        addArgument.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                final LinearLayout argumentsRow = new LinearLayout(context);
                final EditText key = new EditText(context);
                final TextView text = new TextView(context);
                final EditText value = new EditText(context);
                mKeys.add(key);
                mValues.add(value);
                text.setText(" = ");
                Button button = new Button(context);
                button.setText(" - ");
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(argumentsRow);
                        mKeys.remove(key);
                        mValues.remove(value);
                    }

                });
                argumentsRow.addView(button);
                argumentsRow.addView(key);
                argumentsRow.addView(text);
                argumentsRow.addView(value);

                container.addView(argumentsRow);

            }
        });

        // initialize the editor with the existing items
        if (keys != null) {
            init(context, keys, values, container, true);
            mCheckBox.setChecked(true);
            addArgument.setEnabled(true);
        } else {
            init(context, defaultKeys, defaultValues, container, false);
            mCheckBox.setChecked(false);
            addArgument.setEnabled(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        init(context, defaultKeys, defaultValues, container,
                                checked);
                        addArgument.setEnabled(checked);
                        mCheckBox.setChecked(checked);

                    }
                });

    }

    private void init(Context context, List<String> keys, List<String> values,
            final LinearLayout container, boolean enabled) {
        for (EditText editText : mKeys) {
            ((LinearLayout) editText.getParent()).removeAllViews();
        }
        mKeys.clear();
        mValues.clear();

        if (keys != null) {
            for (int i = 0; i < keys.size(); i++) {
                final LinearLayout argumentsRow = new LinearLayout(context);
                final EditText key = new EditText(context);
                final TextView text = new TextView(context);
                final EditText value = new EditText(context);
                mKeys.add(key);
                mValues.add(value);
                key.setText(keys.get(i));
                key.setEnabled(enabled);
                text.setText(" = ");
                text.setEnabled(enabled);
                value.setText(values.get(i));
                value.setEnabled(enabled);
                Button button = new Button(context);
                button.setText(" - ");
                button.setEnabled(enabled);
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(argumentsRow);
                        mKeys.remove(key);
                        mValues.remove(value);
                    }

                });
                argumentsRow.addView(button);
                argumentsRow.addView(key);
                argumentsRow.addView(text);
                argumentsRow.addView(value);

                container.addView(argumentsRow);

            }
        }
    }

    public List<String> getKeys() {
        List<String> result = new ArrayList<String>();
        for (EditText editText : mKeys) {
            result.add(editText.getText().toString());
        }
        return result;
    }

    public List<String> getValues() {
        List<String> result = new ArrayList<String>();
        for (EditText editText : mValues) {
            result.add(editText.getText().toString());
        }
        return result;
    }

}
