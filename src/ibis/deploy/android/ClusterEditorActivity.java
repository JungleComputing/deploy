package ibis.deploy.android;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

/**
 * The Learned Object Editor is an Activity where a learned object can be
 * edited. The fields that are editable are the name and the author field. This
 * Activity operates on data from the learned objects content provider. The
 * intent that starts this application should have a 'id' field in the extras
 * that refers to the id of the learned object that will be edited, a 'name'
 * field and an 'author' field.
 * 
 * @author rkemp
 */
public class ClusterEditorActivity extends MapActivity {

    private List<String> adaptors = new ArrayList<String>();

    private DeployService mDeployService = null;

    private String mClusterName;

    private TextEditor mNameEditor;

    private TextEditor mNodesEditor;

    private TextEditor mCoresEditor;

    private TextEditor mJobUriEditor;

    private SpinnerEditor mJobAdaptorEditor;

    private SpinnerArrayEditor mFileAdaptorsEditor;

    private TextEditor mUserNameEditor;

    private TextEditor mJavaPathEditor;

    private TextEditor mCacheDirectoryEditor;

    private TextEditor mJobWrapperScriptEditor;

    private SpinnerEditor mServerAdaptorEditor;

    private TextEditor mServerUriEditor;

    private MapEditor mMapEditor;

    private boolean mDefaults;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adaptors.add("SshTrilead");
        adaptors.add("Local");
        Bundle extras = getIntent().getExtras();
        mClusterName = extras.getString("name");
        mDefaults = extras.containsKey("defaults");
        setContentView(R.layout.cluster_edit);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);
            try {
                if (mDefaults) {
                    mNodesEditor = new TextEditor(ClusterEditorActivity.this,
                            "" + mDeployService.getDefaultNodes(),
                            (LinearLayout) findViewById(R.id.nodes));
                    mCoresEditor = new TextEditor(ClusterEditorActivity.this,
                            "" + mDeployService.getDefaultCores(),
                            (LinearLayout) findViewById(R.id.cores));
                    mJobUriEditor = new TextEditor(ClusterEditorActivity.this,
                            mDeployService.getDefaultJobUri(),
                            (LinearLayout) findViewById(R.id.jobUri));
                    mJobAdaptorEditor = new SpinnerEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultJobAdaptor(), adaptors,
                            (LinearLayout) findViewById(R.id.jobAdaptor));
                    mFileAdaptorsEditor = new SpinnerArrayEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultFileAdaptors(), adaptors,
                            (LinearLayout) findViewById(R.id.fileAdaptors));
                    mUserNameEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultUserName(),
                            (LinearLayout) findViewById(R.id.userName));
                    mJavaPathEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultJavaPath(),
                            (LinearLayout) findViewById(R.id.javaPath));
                    mCacheDirectoryEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultCacheDirectory(),
                            (LinearLayout) findViewById(R.id.cacheDirectory));
                    mJobWrapperScriptEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultJobWrapperScript(),
                            (LinearLayout) findViewById(R.id.jobWrapperScript));
                    mServerAdaptorEditor = new SpinnerEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultServerAdaptor(), adaptors,
                            (LinearLayout) findViewById(R.id.serverAdaptor));
                    mServerUriEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getDefaultServerUri(),
                            (LinearLayout) findViewById(R.id.serverUri));
                    mMapEditor = new MapEditor(
                            ClusterEditorActivity.this,
                            new GeoPoint(
                                    (int) (mDeployService.getDefaultLatitude() * 1000000),
                                    (int) (mDeployService.getDefaultLongitude() * 1000000)),
                            (LinearLayout) findViewById(R.id.geoPoint));
                } else {
                    mNameEditor = new TextEditor(ClusterEditorActivity.this,
                            mClusterName, mClusterName,
                            (LinearLayout) findViewById(R.id.name));
                    mNodesEditor = new TextEditor(ClusterEditorActivity.this,
                            "" + mDeployService.getNodes(mClusterName), ""
                                    + mDeployService.getDefaultNodes(),
                            (LinearLayout) findViewById(R.id.nodes));
                    mCoresEditor = new TextEditor(ClusterEditorActivity.this,
                            "" + mDeployService.getCores(mClusterName), ""
                                    + mDeployService.getDefaultCores(),
                            (LinearLayout) findViewById(R.id.cores));
                    mJobUriEditor = new TextEditor(ClusterEditorActivity.this,
                            mDeployService.getJobUri(mClusterName),
                            mDeployService.getDefaultJobUri(),
                            (LinearLayout) findViewById(R.id.jobUri));
                    mJobAdaptorEditor = new SpinnerEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getJobAdaptor(mClusterName),
                            mDeployService.getDefaultJobAdaptor(), adaptors,
                            (LinearLayout) findViewById(R.id.jobAdaptor));
                    mFileAdaptorsEditor = new SpinnerArrayEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getFileAdaptors(mClusterName),
                            mDeployService.getDefaultFileAdaptors(), adaptors,
                            (LinearLayout) findViewById(R.id.fileAdaptors));
                    mUserNameEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getUserName(mClusterName), mDeployService
                                    .getDefaultUserName(),
                            (LinearLayout) findViewById(R.id.userName));
                    mJavaPathEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getJavaPath(mClusterName), mDeployService
                                    .getDefaultJavaPath(),
                            (LinearLayout) findViewById(R.id.javaPath));
                    mCacheDirectoryEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getCacheDirectory(mClusterName),
                            mDeployService.getDefaultCacheDirectory(),
                            (LinearLayout) findViewById(R.id.cacheDirectory));
                    mJobWrapperScriptEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getJobWrapperScript(mClusterName),
                            mDeployService.getDefaultJobWrapperScript(),
                            (LinearLayout) findViewById(R.id.jobWrapperScript));
                    mServerAdaptorEditor = new SpinnerEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getServerAdaptor(mClusterName),
                            mDeployService.getDefaultServerAdaptor(), adaptors,
                            (LinearLayout) findViewById(R.id.serverAdaptor));
                    mServerUriEditor = new TextEditor(
                            ClusterEditorActivity.this, mDeployService
                                    .getServerUri(mClusterName), mDeployService
                                    .getDefaultServerUri(),
                            (LinearLayout) findViewById(R.id.serverUri));
                    mMapEditor = new MapEditor(
                            ClusterEditorActivity.this,
                            new GeoPoint(
                                    (int) (mDeployService
                                            .getLatitude(mClusterName) * 1000000),
                                    (int) (mDeployService
                                            .getLongitude(mClusterName) * 1000000)),
                            new GeoPoint(
                                    (int) (mDeployService.getDefaultLatitude() * 1000000),
                                    (int) (mDeployService.getDefaultLongitude() * 1000000)),
                            (LinearLayout) findViewById(R.id.geoPoint));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mDeployService = null;
        }

    };

    /**
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
     *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Save");
        menu.add(0, 1, 0, "Cancel");
        return true;
    }

    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        switch (item.getItemId()) {
        case 0:
            // first set everything but the name, and finally set the name.
            try {
                if (mDefaults) {
                    mDeployService.setDefaultNodes(Integer
                            .parseInt(mNodesEditor.getItem()));
                    mDeployService.setDefaultCores(Integer
                            .parseInt(mCoresEditor.getItem()));
                    mDeployService.setDefaultJobUri(mJobUriEditor.getItem());
                    mDeployService.setDefaultJobAdaptor(mJobAdaptorEditor
                            .getItem());
                    mDeployService.setDefaultFileAdaptors(mFileAdaptorsEditor
                            .getItems());
                    mDeployService
                            .setDefaultUserName(mUserNameEditor.getItem());
                    mDeployService
                            .setDefaultJavaPath(mJavaPathEditor.getItem());
                    mDeployService
                            .setDefaultCacheDirectory(mCacheDirectoryEditor
                                    .getItem());
                    mDeployService
                            .setDefaultJobWrapperScript(mJobWrapperScriptEditor
                                    .getItem());
                    mDeployService.setDefaultServerAdaptor(mServerAdaptorEditor
                            .getItem());
                    mDeployService.setDefaultServerUri(mServerUriEditor
                            .getItem());
                    mDeployService.setDefaultLatitude(mMapEditor.getItem()
                            .getLatitudeE6() / 1000000.0);
                    mDeployService.setDefaultLongitude(mMapEditor.getItem()
                            .getLongitudeE6() / 1000000.0);
                } else {
                    mDeployService.setNodes(mClusterName, Integer
                            .parseInt(mNodesEditor.getItem()));
                    mDeployService.setCores(mClusterName, Integer
                            .parseInt(mCoresEditor.getItem()));
                    mDeployService.setJobUri(mClusterName, mJobUriEditor
                            .getItem());
                    mDeployService.setJobAdaptor(mClusterName,
                            mJobAdaptorEditor.getItem());
                    mDeployService.setFileAdaptors(mClusterName,
                            mFileAdaptorsEditor.getItems());
                    mDeployService.setUserName(mClusterName, mUserNameEditor
                            .getItem());
                    mDeployService.setJavaPath(mClusterName, mJavaPathEditor
                            .getItem());
                    mDeployService.setCacheDirectory(mClusterName,
                            mCacheDirectoryEditor.getItem());
                    mDeployService.setJobWrapperScript(mClusterName,
                            mJobWrapperScriptEditor.getItem());
                    mDeployService.setServerAdaptor(mClusterName,
                            mServerAdaptorEditor.getItem());
                    mDeployService.setServerUri(mClusterName, mServerUriEditor
                            .getItem());
                    mDeployService.setClusterName(mClusterName, mNameEditor
                            .getItem());
                    mDeployService.setLatitude(mClusterName, mMapEditor
                            .getItem().getLatitudeE6() / 1000000.0);
                    mDeployService.setLongitude(mClusterName, mMapEditor
                            .getItem().getLongitudeE6() / 1000000.0);
                }
            } catch (Exception e) {
                // TODO: do something with this exception
            }

            // get all the values from the editors put them into the content
            // provider and return
        case 1:
            // only return

        }
        finish();
        return true;
    }

    /**
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}
