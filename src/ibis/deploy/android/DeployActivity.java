package ibis.deploy.android;

import ibis.deploy.State;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class DeployActivity extends MapActivity {

    private DeployService mDeployService = null;

    private ClusterOverlay mClusterOverlay = null;

    private String mClosestCluster = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);

        final Button initButton = (Button) findViewById(R.id.InitButton);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
        final LinearLayout main = (LinearLayout) findViewById(R.id.main);
        main.removeView(progressBar);
        initButton.setEnabled(false);

        initButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // remove the init button, change it to a progress bar
                main.removeView(initButton);
                main.addView(progressBar);
                // initialize the deploy service

                try {
                    mDeployService.initialize(mClosestCluster, mCallback);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        });

        mClusterOverlay = new ClusterOverlay();
        final TextView text = (TextView) findViewById(R.id.text);
        final MapView map = (MapView) findViewById(R.id.map);
        map.getController().setCenter(new GeoPoint(52333485, 4864776));
        map.setSatellite(true);
        map.getOverlays().add(mClusterOverlay);
        map.displayZoomControls(true);
        ((LinearLayout) findViewById(R.id.layout_zoom)).addView(map
                .getZoomControls());

        map.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mClosestCluster = mClusterOverlay.getClosestCluster(map,
                            new Point((int) event.getX(), (int) event.getY()));
                    if (mClosestCluster != null) {
                        text.setText(mClosestCluster);
                        text.invalidate();
                        mClusterOverlay.setSelectedCluster(mClosestCluster);
                        initButton.setEnabled(true);
                    }
                }
                return false;
            }

        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {

        case DeployConstants.EXPERIMENTS:
            System.out.println("experiments");
            startActivityForResult(new Intent(DeployActivity.this,
                    ExperimentActivity.class), 0);
            break;
        case DeployConstants.JOB_TABLE:
            System.out.println("job table");
            startActivityForResult(new Intent(DeployActivity.this,
                    JobTableActivity.class), 0);
            break;
        case DeployConstants.NETWORK:
            System.out.println("network");
            startActivityForResult(new Intent(DeployActivity.this,
                    HubVizActivity.class), 0);
            break;
        case DeployConstants.APPLICATION_EDITOR:
            System.out.println("application editor");
            startActivityForResult(new Intent(DeployActivity.this,
                    ApplicationListActivity.class), 0);
            break;
        case DeployConstants.CLUSTER_EDITOR:
            System.out.println("cluster editor");
            startActivityForResult(new Intent(DeployActivity.this,
                    ClusterListActivity.class), 0);
            break;
        case Activity.RESULT_CANCELED:
            System.out.println("canceled");
            unbindService(mConnection);
            break;
        default:
            System.out.println("default");
            finish();
        }

    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);

            // update the cluster overlay and invalidate the map
            mClusterOverlay.setDeployService(mDeployService);
            findViewById(R.id.map).invalidate();

        }

        public void onServiceDisconnected(ComponentName className) {
            mDeployService = null;
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // unbindService(mConnection);
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

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    /**
     * This implementation is used to receive callbacks from the remote service.
     */
    private DeployServiceCallBack mCallback = new DeployServiceCallBack.Stub() {
        /**
         * This is called by the remote service regularly to tell us about new
         * values. Note that IPC calls are dispatched through a thread pool
         * running in each process, so the code executing here will NOT be
         * running in our main thread like most other things -- so, to update
         * the UI, we need to use a Handler to hop over there.
         */
        public void valueChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }
    };

    private static final int BUMP_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ProgressBar p = (ProgressBar) findViewById(R.id.progress_horizontal);
            if (msg.arg1 > State.DEPLOYED.ordinal()) {
                p.incrementProgressBy(-1);
            } else {
                p.incrementProgressBy(1);
            }
            TextView status = (TextView) findViewById(R.id.text2);
            status.setText(ibis.deploy.State.values()[msg.arg1].toString());

            System.err.println(ibis.deploy.State.values()[msg.arg1].toString()
                    + " (" + System.currentTimeMillis() + ")");
            if (msg.arg1 == State.DEPLOYED.ordinal()) {
                startActivityForResult(new Intent(DeployActivity.this,
                        ExperimentActivity.class), 0);
                p.setMax(2);
                p.setProgress(2);
            }
            if (msg.arg1 == State.DONE.ordinal()) {
                finish();
            }
        }

    };

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}