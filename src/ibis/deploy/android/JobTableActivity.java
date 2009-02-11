package ibis.deploy.android;

import ibis.deploy.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class JobTableActivity extends Activity {

    private DeployService mDeployService = null;

    private String mJobName;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobtable2);
        bindService(new Intent(DeployService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);
        TabHelper.init(this);

    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        dialog.setTitle("Details of " + mJobName);
        try {
            ((TextView) dialog.findViewById(R.id.application))
                    .setText(mDeployService.getApplication(mJobName));
            ((TextView) dialog.findViewById(R.id.cluster))
                    .setText(mDeployService.getCluster(mJobName));
            ((TextView) dialog.findViewById(R.id.resourceCount)).setText(""
                    + mDeployService.getResourceCount(mJobName));
            ((TextView) dialog.findViewById(R.id.processCount)).setText(""
                    + mDeployService.getProcessCount(mJobName));
            ((TextView) dialog.findViewById(R.id.pool)).setText(mDeployService
                    .getPool(mJobName));
            ((TextView) dialog.findViewById(R.id.stdout))
                    .setText(mDeployService.getStdout(mJobName));
            ((TextView) dialog.findViewById(R.id.stderr))
                    .setText(mDeployService.getStderr(mJobName));

        } catch (Exception e) {
        }

    }

    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialog = factory.inflate(R.layout.jobdetails, null);

        return new AlertDialog.Builder(this).setTitle("Details of " + mJobName)
                .setView(dialog).create();
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDeployService = DeployService.Stub.asInterface(service);
            try {
                ((ListView) findViewById(R.id.list)).setAdapter(new JobAdapter(
                        JobTableActivity.this, R.layout.job_list_item,
                        mDeployService));
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
        super.onDestroy();
        try {
            mDeployService.removeListener();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    /**
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
     *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Start All");
        menu.add(0, 1, 0, "Stop All");
        return true;
    }

    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        switch (item.getItemId()) {
        case 0:
            try {
                for (String jobName : mDeployService.getJobNames()) {
                    mDeployService.start(jobName);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case 1:
            try {
                for (String jobName : mDeployService.getJobNames()) {
                    mDeployService.stop(jobName);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        }
        return true;
    }

    /**
     * This implementation is used to receive callbacks from the remote service.
     */

    public class JobAdapter extends BaseAdapter {

        private List<DataSetObserver> mObservers = new ArrayList<DataSetObserver>();

        private Context mContext;

        private int mLayout;

        private DeployService mDeployService;

        private List<String> mJobNames = new ArrayList<String>();

        private Map<String, TextView> hubStates = new HashMap<String, TextView>();

        private Map<String, TextView> jobStates = new HashMap<String, TextView>();

        private Map<String, ImageButton> mStartButtons = new HashMap<String, ImageButton>();

        public JobAdapter(Context context, int layout,
                DeployService deployService) throws RemoteException {
            mContext = context;
            mLayout = layout;
            mDeployService = deployService;
            mJobNames = mDeployService.getJobNames();
            mDeployService.addListener(mListener);
        }

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int arg0) {
            return true;
        }

        public int getCount() {
            return mJobNames.size();
        }

        public Object getItem(int position) {
            return mJobNames.get(position);
        }

        public long getItemId(int position) {
            return mJobNames.get(position).hashCode();
        }

        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView == null || convertView instanceof LinearLayout) {
                convertView = (LinearLayout) View.inflate(mContext, mLayout,
                        null);
            }
            final ImageButton startButton = (ImageButton) convertView
                    .findViewById(R.id.startButton);
            final ImageButton detailsButton = (ImageButton) convertView
                    .findViewById(R.id.detailsButton);
            final TextView jobNameText = (TextView) convertView
                    .findViewById(R.id.jobNameText);
            final TextView jobStateText = (TextView) convertView
                    .findViewById(R.id.jobStateText);
            final TextView hubStateText = (TextView) convertView
                    .findViewById(R.id.hubStateText);

            final String jobName = mJobNames.get(position);
            hubStates.put(jobName, hubStateText);
            jobStates.put(jobName, jobStateText);
            mStartButtons.put(jobName, startButton);
            try {
                if (!mDeployService.isStartable(jobName)) {
                    startButton
                            .setImageResource(R.drawable.media_playback_stop);
                } else {
                    startButton
                            .setImageResource(R.drawable.media_playback_start);
                }
            } catch (Exception e) {
            }
            startButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    try {
                        if (!mDeployService.isStartable(jobName)) {
                            startButton
                                    .setImageResource(R.drawable.media_playback_start);
                            try {
                                mDeployService.stop(jobName);
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            startButton
                                    .setImageResource(R.drawable.media_playback_stop);
                            try {
                                mDeployService.start(jobName);
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            });

            detailsButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    mJobName = jobName;
                    showDialog(0);
                }
            });

            jobNameText.setText(jobName);
            try {
                jobStateText.setText(mDeployService.getJobState(jobName));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                hubStateText.setText(mDeployService.getHubState(jobName));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return convertView;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean hasStableIds() {
            return false;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            mObservers.add(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mObservers.remove(observer);
        }

        private Handler mHubHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hubStates.get(msg.obj).setText(
                        State.values()[msg.what].toString());
            }

        };

        private Handler mJobHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                jobStates.get(msg.obj).setText(
                        State.values()[msg.what].toString());
                // check whether we've to flip the start/stop button.
                if (msg.what == State.DONE.ordinal()) {
                    mStartButtons.get(msg.obj).setImageResource(
                            R.drawable.media_playback_start);
                }
            }

        };

        private JobUpdateCallBack mListener = new JobUpdateCallBack.Stub() {

            public void updateHubState(String jobName, int state)
                    throws RemoteException {
                mHubHandler.sendMessage(mHubHandler.obtainMessage(state,
                        jobName));
            }

            public void updateJobState(String jobName, int state)
                    throws RemoteException {
                mJobHandler.sendMessage(mJobHandler.obtainMessage(state,
                        jobName));

            }

        };

    }

}