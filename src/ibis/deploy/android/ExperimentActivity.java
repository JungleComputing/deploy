package ibis.deploy.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class ExperimentActivity extends MapActivity {

    private DeployService mDeployService = null;

    private ClusterOverlay mClusterOverlay = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);

        final MapView map = (MapView) findViewById(R.id.map);
        mClusterOverlay = new ClusterOverlay();
        map.getOverlays().add(mClusterOverlay);
        map.displayZoomControls(true);
        ((LinearLayout) findViewById(R.id.layout_zoom)).addView(map
                .getZoomControls());

        map.setOnTouchListener(new View.OnTouchListener() {

            private String previousClosestCluster;

            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String closestCluster = mClusterOverlay.getClosestCluster(
                            map, new Point((int) event.getX(), (int) event
                                    .getY()));
                    if (closestCluster != null) {
                        mClusterOverlay.setSelectedCluster(closestCluster);
                        if (closestCluster.equals(previousClosestCluster)) {
                            mClusterOverlay.addResourceCount(1);
                            map.invalidate();
                        } else {
                            mClusterOverlay.setResourceCount(1);
                            map.invalidate();
                        }
                        previousClosestCluster = closestCluster;
                    }
                }
                return false;
            }

        });

        ImageButton createButton = (ImageButton) findViewById(R.id.ImageButton01);
        createButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                try {
                    if (mDeployService != null) {
                        String application = ((String) ((Spinner) findViewById(R.id.ApplicationSpinner))
                                .getSelectedItem());
                        String cluster = mClusterOverlay.getSelectedCluster();

                        int processCount = Integer
                                .parseInt(((EditText) findViewById(R.id.ProcessCount))
                                        .getText().toString());
                        int resourceCount = mClusterOverlay.getResourceCount();

                        mDeployService.addJob(application, processCount,
                                cluster, resourceCount, false);
                    } else {
                        Toast.makeText(ExperimentActivity.this,
                                "not yet bound to service", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (RemoteException e) {
                    Toast.makeText(ExperimentActivity.this, e.toString(),
                            Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        ImageButton submitButton = (ImageButton) findViewById(R.id.ImageButton02);
        submitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                try {
                    if (mDeployService != null) {
                        String application = ((String) ((Spinner) findViewById(R.id.ApplicationSpinner))
                                .getSelectedItem());
                        String cluster = mClusterOverlay.getSelectedCluster();
                        int processCount = Integer
                                .parseInt(((EditText) findViewById(R.id.ProcessCount))
                                        .getText().toString());
                        int resourceCount = mClusterOverlay.getResourceCount();

                        mDeployService.addJob(application, processCount,
                                cluster, resourceCount, true);
                    } else {
                        Toast.makeText(ExperimentActivity.this,
                                "not yet bound to service", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (RemoteException e) {
                    Toast.makeText(ExperimentActivity.this, e.toString(),
                            Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        TabHelper.init(this);

    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);
            try {
                Spinner applications = (Spinner) findViewById(R.id.ApplicationSpinner);
                // create the adapter belonging to the spinner
                final ArrayAdapter<String> applicationsAdapter = new ArrayAdapter<String>(
                        ExperimentActivity.this,
                        android.R.layout.simple_spinner_item, mDeployService
                                .getApplicationNames().toArray(new String[0]));
                applicationsAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                applications.setAdapter(applicationsAdapter);
                // update the cluster overlay and invalidate the map
                mClusterOverlay.setDeployService(mDeployService);
                findViewById(R.id.map).invalidate();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mDeployService = null;
            Toast.makeText(ExperimentActivity.this, "disconnected",
                    Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(mConnection);

    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}