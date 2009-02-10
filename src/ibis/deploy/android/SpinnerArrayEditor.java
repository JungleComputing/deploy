package ibis.deploy.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class SpinnerArrayEditor {

    private List<Spinner> mSpinners = new ArrayList<Spinner>();

    private CheckBox mCheckBox;

    public SpinnerArrayEditor(final Context context, final List<String> items,
            final List<String> options, final LinearLayout layout) {
        this(context, items, null, options, layout);
    }

    public SpinnerArrayEditor(final Context context, List<String> items,
            final List<String> defaultItems, final List<String> options,
            final LinearLayout layout) {

        final LinearLayout container = (LinearLayout) layout
                .findViewById(R.id.container);
        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        final Button addArgument = (Button) container.findViewById(R.id.add);
        addArgument.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                final LinearLayout argumentsRow = new LinearLayout(context);

                final Spinner spinner = new Spinner(context);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        context, android.R.layout.simple_spinner_item, options);
                adapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setAdapter(adapter);
                mSpinners.add(spinner);
                Button button = new Button(context);
                button.setText(" - ");
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(argumentsRow);
                        mSpinners.remove(spinner);
                    }

                });
                argumentsRow.addView(button);
                argumentsRow.addView(spinner);
                container.addView(argumentsRow);

            }
        });

        // initialize the editor with the existing items
        if (items != null) {
            init(context, items, options, container, true);
            mCheckBox.setChecked(true);
            addArgument.setEnabled(true);
        } else {
            init(context, defaultItems, options, container, false);
            mCheckBox.setChecked(false);
            addArgument.setEnabled(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked) {
                            init(context, defaultItems, options, container,
                                    true);
                            addArgument.setEnabled(true);
                            mCheckBox.setChecked(true);
                        } else {
                            init(context, defaultItems, options, container,
                                    false);
                            addArgument.setEnabled(false);
                            mCheckBox.setChecked(false);
                        }

                    }
                });

    }

    private void init(Context context, List<String> items,
            final List<String> options, final LinearLayout container,
            boolean enabled) {
        for (Spinner spinner : mSpinners) {
            ((LinearLayout) spinner.getParent()).removeAllViews();
        }
        mSpinners.clear();
        if (items != null) {
            for (String item : items) {
                final LinearLayout itemRow = new LinearLayout(context);

                final Spinner spinner = new Spinner(context);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        context, android.R.layout.simple_spinner_item, options);
                adapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setAdapter(adapter);
                mSpinners.add(spinner);
                spinner.setEnabled(enabled);
                spinner.setSelection(options.indexOf(item));

                Button button = new Button(context);
                button.setEnabled(enabled);
                button.setText(" - ");
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(itemRow);
                        mSpinners.remove(spinner);
                    }

                });
                itemRow.addView(button);
                itemRow.addView(spinner);
                container.addView(itemRow);
            }
        }
    }

    public List<String> getItems() {
        List<String> result = new ArrayList<String>();
        for (Spinner spinner : mSpinners) {
            result.add(spinner.getSelectedItem().toString());
        }
        return result;
    }

}
