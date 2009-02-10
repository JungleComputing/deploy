package ibis.deploy.android;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;

public class TabHelper {

    public static void init(final Activity activity) {
        ImageButton experimentButton = (ImageButton) activity
                .findViewById(R.id.Experiment);
        if (!(activity instanceof ExperimentActivity)) {
            experimentButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    activity.setResult(DeployConstants.EXPERIMENTS);
                    activity.finish();
                }
            });
        }

        if (!(activity instanceof JobTableActivity)) {
            ImageButton jobsButton = (ImageButton) activity
                    .findViewById(R.id.Jobs);
            jobsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    activity.setResult(DeployConstants.JOB_TABLE);
                    activity.finish();
                }
            });
        }

        if (!(activity instanceof HubVizActivity)) {
            ImageButton networkButton = (ImageButton) activity
                    .findViewById(R.id.Network);
            networkButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    activity.setResult(DeployConstants.NETWORK);
                    activity.finish();
                }
            });
        }

        if (!(activity instanceof ApplicationEditorActivity)) {
            ImageButton applicationEditorButton = (ImageButton) activity
                    .findViewById(R.id.Applications);
            applicationEditorButton
                    .setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            activity
                                    .setResult(DeployConstants.APPLICATION_EDITOR);
                            activity.finish();
                        }
                    });
        }

        if (!(activity instanceof ClusterEditorActivity)) {
            ImageButton clusterEditorButton = (ImageButton) activity
                    .findViewById(R.id.Grid);
            clusterEditorButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    activity.setResult(DeployConstants.CLUSTER_EDITOR);
                    activity.finish();
                }
            });
        }
    }

}
