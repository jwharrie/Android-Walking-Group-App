package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ViewPendingPermissionActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private PermissionRequest mPermission;

    public static final String INDEX_STRING = "index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pending_permission);

        mSharedData = SharedData.getSharedData();
        proxy = mSharedData.getProxy();

        grabPendingPermission();
        setUpPermissionInfo();

        setUpApproveButton();
        setUpDenyButton();

    }

    public static Intent makeIntent(Context context, int i) {
        Intent intent = new Intent(context, ViewPendingPermissionActivity.class);
        intent.putExtra(ViewPendingPermissionActivity.INDEX_STRING,i);
        return intent;
    }

    private void grabPendingPermission() {
        Intent intent = getIntent();
        mPermission = mSharedData.getUser().getPendingPermissionRequests().get(intent.getIntExtra(ViewPendingPermissionActivity.INDEX_STRING,0));
    }

    private void setUpPermissionInfo() {
        TextView action = findViewById(R.id.txt_pending_action);
        action.setText("Action: " + mPermission.getAction());

        TextView status = findViewById(R.id.txt_pending_status);
        status.setText("Permission status: " + mPermission.getStatus());

        TextView message = findViewById(R.id.txt_pending_message);
        message.setText("" + mPermission.getMessage());
    }



    private void setUpApproveButton() {
        Button btn = findViewById(R.id.btn_accept);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(mPermission.getId(), WGServerProxy.PermissionStatus.APPROVED);
                ProxyBuilder.callProxy(ViewPendingPermissionActivity.this, caller, returnNothing -> closeAfterApprove(returnNothing));
            }
        });
    }

    private void closeAfterApprove(PermissionRequest request) {
        Toast.makeText(ViewPendingPermissionActivity.this, "Permission approved.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setUpDenyButton() {
        Button btn = findViewById(R.id.btn_deny);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(mPermission.getId(), WGServerProxy.PermissionStatus.DENIED);
                ProxyBuilder.callProxy(ViewPendingPermissionActivity.this, caller, returnNothing -> closeAfterDeny(returnNothing));
            }
        });
    }

    private void closeAfterDeny(PermissionRequest request) {
        Toast.makeText(ViewPendingPermissionActivity.this, "Permission denied.", Toast.LENGTH_LONG).show();
        finish();
    }


}
