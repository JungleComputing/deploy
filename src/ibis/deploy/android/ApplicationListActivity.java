package ibis.deploy.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Displays a list of learned objects. Will display learned objects from the
 * {@link Uri} provided in the intent if there is one, otherwise defaults to
 * displaying the contents of the {@link LearnedObjectsProvider}
 */
public class ApplicationListActivity extends ListActivity {

    private DeployService mDeployService = null;

    private ArrayAdapter<String> mAdapter;

    // a single click on an item in the list will result in the edit action
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // move the cursor to the position where the user clicked, retrieve the
        // id, the name and the author in order to construct an intent for the
        // edit activity

        Intent editIntent = new Intent(ApplicationListActivity.this,
                ApplicationEditorActivity.class);
        editIntent
                .putExtra("name", (String) getListAdapter().getItem(position));
        startActivityForResult(editIntent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");
        setContentView(R.layout.object_list);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        registerForContextMenu(getListView());
        TabHelper.init(this);

    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);

            try {
                mAdapter = new ArrayAdapter<String>(
                        ApplicationListActivity.this,
                        R.layout.objects_list_item, mDeployService
                                .getApplicationNames());
                setListAdapter(mAdapter);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
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
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, 0, 0, "Delete");
        // menu.add(0, EDIT_ITEM, 0, R.string.edit_learned_object);
        // menu.add(0, SHARE_ITEM, 0, R.string.share_learned_object);
    }

    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        String selectedItem = mAdapter.getItem(menuInfo.position);
        mAdapter.remove(selectedItem);
        try {
            mDeployService.deleteApplication(selectedItem);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // int id = mCursor.getInt(mCursor.getColumnIndex(LearnedObject._ID));
        //
        // switch (item.getItemId()) {
        // case DELETE_ITEM:
        // // delete the current item
        // getContentResolver().delete(
        // Uri.withAppendedPath(getIntent().getData(), "" + id), null,
        // null);
        // break;
        // case EDIT_ITEM:
        // // construct an edit intent for the current item
        // Intent editIntent = new Intent(ApplicationListActivity.this,
        // LearnedObjectEditor.class).setData(getIntent().getData());
        // editIntent.putExtra("id", id);
        // editIntent.putExtra("name", mCursor.getString(mCursor
        // .getColumnIndex(LearnedObject.OBJECT_NAME)));
        // editIntent.putExtra("author", mCursor.getString(mCursor
        // .getColumnIndex(LearnedObject.AUTHOR)));
        // startActivityForResult(editIntent, ACTIVITY_EDIT);
        // break;
        // }
        //
        // mCursor.requery();
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent resultIntent) {
        // super.onActivityResult(requestCode, resultCode, resultIntent);
        // if (resultIntent == null) {
        // return;
        // }
        // switch (requestCode) {
        // case ACTIVITY_EDIT:
        // // upon a finished edit activity, update the content provider
        // ContentValues values = new ContentValues();
        // Bundle extras = resultIntent.getExtras();
        // values.put(LearnedObject.OBJECT_NAME, extras.getString("name"));
        // values.put(LearnedObject.AUTHOR, extras.getString("author"));
        // int id = extras.getInt("id");
        // getContentResolver().update(
        // Uri.withAppendedPath(getIntent().getData(), "" + id),
        // values, null, null);
        // break;
        // }
    }

    /**
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
     *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "New Application");
        menu.add(0, 1, 0, "Edit Defaults");
        return true;
    }

    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialog = factory.inflate(R.layout.dialog, null);
        final Button button = (Button) dialog.findViewById(R.id.button);
        final EditText editText = (EditText) dialog.findViewById(R.id.edit);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = editText.getText().toString();
                try {
                    mDeployService.createNewApplication(name);
                    mAdapter.add(name);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dismissDialog(0);

            }
        });
        return new AlertDialog.Builder(this).setTitle("Enter Application Name")
                .setView(dialog).create();
    }

    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        switch (item.getItemId()) {
        case 0:
            showDialog(0);
            break;
        case 1:
            Intent editIntent = new Intent(ApplicationListActivity.this,
                    ApplicationEditorActivity.class);
            editIntent.putExtra("name", "defaults");
            editIntent.putExtra("defaults", "true");
            startActivityForResult(editIntent, 0);
            break;
        }
        return true;
    }

}
