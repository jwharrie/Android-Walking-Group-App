package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        setUpEditUserButton();
        setUpEditChildButton();
        setUpMonitorButton();
        setUpViewGroupsButton();

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, AccountManagementActivity.class);
    }

    private void setUpEditUserButton() {
        Button btn = findViewById(R.id.accntntMangmnt_btn_editUser);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EditUserInformation.makeIntent(AccountManagementActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpEditChildButton() {
        Button btn = findViewById(R.id.btn_editChild);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ChooseChildToEditActivity.makeIntent(AccountManagementActivity.this);
                startActivityForResult(intent, RESULT_OK);
                //startActivity(intent);
            }
        });
    }

    private void setUpMonitorButton() {
        Button btn = findViewById(R.id.btn_monitor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AccountMonitoringActivity.makeIntent(AccountManagementActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpViewGroupsButton() {
        Button btn = findViewById(R.id.btn_viewGroups);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountManagementActivity.this, ViewGroupsActivity.class);
                startActivity(intent);
            }
        });
    }
}
