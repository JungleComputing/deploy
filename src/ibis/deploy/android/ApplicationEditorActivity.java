package ibis.deploy.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

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
public class ApplicationEditorActivity extends Activity {

    private DeployService mDeployService = null;

    private String mApplicationName;

    private TextEditor mNameEditor;

    private TextEditor mMainClassEditor;

    private ArrayEditor mArgumentsEditor;

    private ArrayPropertyEditor mSystemPropertiesEditor;

    private ArrayEditor mJvmOptionsEditor;

    private ArrayEditor mLibrariesEditor;

    private ArrayEditor mInputFilesEditor;

    private ArrayEditor mOutputFilesEditor;

    private boolean mDefaults;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        mApplicationName = extras.getString("name");
        mDefaults = extras.containsKey("defaults");
        setContentView(R.layout.application_edit);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);
        // findViewById(R.id.main).setOnCreateContextMenuListener(this);

        // registerForContextMenu(findViewById(R.id.main));
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);

            try {
                if (mDefaults) {
                    mNameEditor = new TextEditor(
                            ApplicationEditorActivity.this, "defaults",
                            "defaults", (LinearLayout) findViewById(R.id.name));
                    mMainClassEditor = new TextEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultMainClass(),
                            (LinearLayout) findViewById(R.id.mainClass));
                    mArgumentsEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultArguments(),
                            (LinearLayout) findViewById(R.id.arguments));
                    mSystemPropertiesEditor = new ArrayPropertyEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultSystemPropertyKeys(),
                            mDeployService.getDefaultSystemPropertyValues(),
                            (LinearLayout) findViewById(R.id.systemProperties));
                    mJvmOptionsEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultJvmOptions(),
                            (LinearLayout) findViewById(R.id.jvmOptions));
                    mLibrariesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultLibraries(),
                            (LinearLayout) findViewById(R.id.libraries));
                    mInputFilesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultInputFiles(),
                            (LinearLayout) findViewById(R.id.inputFiles));
                    mOutputFilesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getDefaultOutputFiles(),
                            (LinearLayout) findViewById(R.id.outputFiles));
                } else {
                    mNameEditor = new TextEditor(
                            ApplicationEditorActivity.this, mApplicationName,
                            mApplicationName,
                            (LinearLayout) findViewById(R.id.name));
                    mMainClassEditor = new TextEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getMainClass(mApplicationName),
                            mDeployService.getDefaultMainClass(),
                            (LinearLayout) findViewById(R.id.mainClass));
                    mArgumentsEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getArguments(mApplicationName),
                            mDeployService.getDefaultArguments(),
                            (LinearLayout) findViewById(R.id.arguments));
                    mSystemPropertiesEditor = new ArrayPropertyEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getSystemPropertyKeys(mApplicationName),
                            mDeployService
                                    .getSystemPropertyValues(mApplicationName),
                            mDeployService.getDefaultSystemPropertyKeys(),
                            mDeployService.getDefaultSystemPropertyValues(),
                            (LinearLayout) findViewById(R.id.systemProperties));
                    mJvmOptionsEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getJvmOptions(mApplicationName),
                            mDeployService.getDefaultJvmOptions(),
                            (LinearLayout) findViewById(R.id.jvmOptions));
                    mLibrariesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getLibraries(mApplicationName),
                            mDeployService.getDefaultLibraries(),
                            (LinearLayout) findViewById(R.id.libraries));
                    mInputFilesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getInputFiles(mApplicationName),
                            mDeployService.getDefaultInputFiles(),
                            (LinearLayout) findViewById(R.id.inputFiles));
                    mOutputFilesEditor = new ArrayEditor(
                            ApplicationEditorActivity.this, mDeployService
                                    .getOutputFiles(mApplicationName),
                            mDeployService.getDefaultOutputFiles(),
                            (LinearLayout) findViewById(R.id.outputFiles));
                }
            } catch (RemoteException e) {
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
                    mDeployService.setDefaultMainClass(mMainClassEditor
                            .getItem());
                    mDeployService.setDefaultArguments(mArgumentsEditor
                            .getItems());
                    mDeployService.setDefaultSystemProperties(
                            mSystemPropertiesEditor.getKeys(),
                            mSystemPropertiesEditor.getValues());
                    mDeployService.setDefaultJvmOptions(mJvmOptionsEditor
                            .getItems());
                    mDeployService.setDefaultLibraries(mLibrariesEditor
                            .getItems());
                    mDeployService.setDefaultInputFiles(mInputFilesEditor
                            .getItems());
                    mDeployService.setDefaultOutputFiles(mOutputFilesEditor
                            .getItems());
                } else {
                    mDeployService.setMainClass(mApplicationName,
                            mMainClassEditor.getItem());
                    mDeployService.setArguments(mApplicationName,
                            mArgumentsEditor.getItems());
                    mDeployService.setSystemProperties(mApplicationName,
                            mSystemPropertiesEditor.getKeys(),
                            mSystemPropertiesEditor.getValues());
                    mDeployService.setJvmOptions(mApplicationName,
                            mJvmOptionsEditor.getItems());
                    mDeployService.setLibraries(mApplicationName,
                            mLibrariesEditor.getItems());
                    mDeployService.setInputFiles(mApplicationName,
                            mInputFilesEditor.getItems());
                    mDeployService.setOutputFiles(mApplicationName,
                            mOutputFilesEditor.getItems());
                    // now set the name
                    mDeployService.setApplicationName(mApplicationName,
                            mNameEditor.getItem());
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

}
