package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PermissionsMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_menu);

        setUpPermissionHistoryButton();

        setUpPendingPermissionsButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, PermissionsMenuActivity.class);
    }

    private void setUpPendingPermissionsButton() {
        Button btn = findViewById(R.id.btn_pending_permissions);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PendingPermissionsActivity.makeIntent(PermissionsMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpPermissionHistoryButton(){
        Button btn = findViewById(R.id.btn_permissionHistory);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = PermissionHistoryActivity.makeIntent(PermissionsMenuActivity.this);
                    startActivity(intent);
            }
        });
    }
}
