package ibis.deploy.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ArrayEditor {

    private List<EditText> mEditTexts = new ArrayList<EditText>();

    private CheckBox mCheckBox;

    public ArrayEditor(final Context context, final List<String> items,
            final LinearLayout layout) {
        this(context, items, null, layout);
    }

    public ArrayEditor(final Context context, List<String> items,
            final List<String> defaultItems, final LinearLayout layout) {

        final LinearLayout container = (LinearLayout) layout
                .findViewById(R.id.container);
        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        final Button addArgument = (Button) container.findViewById(R.id.add);
        addArgument.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                final LinearLayout argumentsRow = new LinearLayout(context);
                final EditText text = new EditText(context);
                mEditTexts.add(text);
                Button button = new Button(context);
                button.setText(" - ");
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(argumentsRow);
                        mEditTexts.remove(text);
                    }

                });
                argumentsRow.addView(button);
                argumentsRow.addView(text);
                container.addView(argumentsRow);

            }
        });

        // initialize the editor with the existing items
        if (items != null) {
            init(context, items, container, true);
            mCheckBox.setChecked(true);
            addArgument.setEnabled(true);
        } else {
            init(context, defaultItems, container, false);
            mCheckBox.setChecked(false);
            addArgument.setEnabled(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked) {
                            init(context, defaultItems, container, true);
                            addArgument.setEnabled(true);
                            mCheckBox.setChecked(true);
                        } else {
                            init(context, defaultItems, container, false);
                            addArgument.setEnabled(false);
                            mCheckBox.setChecked(false);
                        }

                    }
                });

    }

    private void init(Context context, List<String> items,
            final LinearLayout container, boolean enabled) {
        for (EditText editText : mEditTexts) {
            ((LinearLayout) editText.getParent()).removeAllViews();
        }
        mEditTexts.clear();
        if (items != null) {
            for (String item : items) {
                final LinearLayout itemRow = new LinearLayout(context);
                final EditText text = new EditText(context);
                text.setText(item);
                text.setEnabled(enabled);
                mEditTexts.add(text);
                Button button = new Button(context);
                button.setEnabled(enabled);
                button.setText(" - ");
                button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        container.removeView(itemRow);
                        mEditTexts.remove(text);
                    }

                });
                itemRow.addView(button);
                itemRow.addView(text);
                container.addView(itemRow);
            }
        }
    }

    public List<String> getItems() {
        List<String> result = new ArrayList<String>();
        for (EditText editText : mEditTexts) {
            result.add(editText.getText().toString());
        }
        return result;
    }

}
