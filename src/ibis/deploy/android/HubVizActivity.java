package ibis.deploy.android;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.util.MalformedAddressException;
import ibis.smartsockets.viz.android.SmartsocketsViz;

import java.net.UnknownHostException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.LinearLayout;

public class HubVizActivity extends Activity {
    private DeployService mDeployService = null;

    private SmartsocketsViz mViz = null;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartsockets);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);

        TabHelper.init(this);
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);
            try {
                LinearLayout layout = (LinearLayout) findViewById(R.id.HubViz);
                mViz = new SmartsocketsViz(DirectSocketAddress
                        .getByAddress(mDeployService.getRootHubAddress()),
                        HubVizActivity.this, false);
                layout.addView(mViz, 0, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.FILL_PARENT));
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MalformedAddressException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mDeployService = null;
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mViz.done();
        // System.out.println("stopping viz done!");
        // mViz.waitUntilFinished();
        // System.out.println("finished!");
        unbindService(mConnection);
        super.onDestroy();
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

}
