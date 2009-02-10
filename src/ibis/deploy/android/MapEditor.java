package ibis.deploy.android;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MapEditor {

    private MapView mMapView;

    private CheckBox mCheckBox;

    public MapEditor(final Context context, final GeoPoint point,
            final LinearLayout layout) {
        this(context, point, null, layout);
    }

    public MapEditor(final Context context, final GeoPoint point,
            final GeoPoint defaultPoint, final LinearLayout layout) {
        mMapView = (MapView) layout.findViewById(R.id.edit);
        mCheckBox = (CheckBox) layout.findViewById(R.id.checkbox);

        if (point != null) {

            mMapView.getController().setCenter(point);
            mMapView.setEnabled(true);
            mCheckBox.setChecked(true);
        } else {
            mMapView.getController().setCenter(defaultPoint);
            mMapView.setEnabled(false);
            mCheckBox.setChecked(false);
        }

        mCheckBox
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked) {
                            mMapView.setEnabled(true);
                            mCheckBox.setChecked(true);
                        } else {
                            mMapView.getController().setCenter(defaultPoint);
                            mMapView.setEnabled(false);
                            mCheckBox.setChecked(false);
                        }

                    }
                });

    }

    public GeoPoint getItem() {
        return mMapView.getMapCenter();
    }

}
