package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;

public class ShowPermissionRequestDetails extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private PermissionRequest mPermission;

    public static final String INDEX_STRING = "index2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_permission_request_details);

        mSharedData = SharedData.getSharedData();
        proxy = mSharedData.getProxy();
        grabPermissions();
        setupTextView();


    }

    public static Intent makeIntent(Context context, int i) {
        Intent intent = new Intent(context, ShowPermissionRequestDetails.class);
        intent.putExtra(ViewPendingPermissionActivity.INDEX_STRING,i);
        return intent;
    }

    private void grabPermissions() {
        Intent intent = getIntent();
        mPermission = mSharedData.getUser().getPendingPermissionRequests().get(intent.getIntExtra(ViewPendingPermissionActivity.INDEX_STRING,0));
    }

    private void setupTextView() {
        TextView action = findViewById(R.id.txt_action);
        action.setText("Permission Type: \n" + mPermission.getAction());

        TextView status = findViewById(R.id.txt_status);
        status.setText("Permission Status: \n" + mPermission.getStatus());

        TextView message = findViewById(R.id.txt_message);
        message.setText("Permission Details: \n" + mPermission.getMessage());
    }
}
