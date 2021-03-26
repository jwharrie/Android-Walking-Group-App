package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountMonitoringActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_monitoring);

        setUpParentButton();
        setUpChildButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, AccountMonitoringActivity.class);
    }

    private void setUpChildButton() {
        Button btn = findViewById(R.id.btn_monitorChild);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MonitorChildActivity.makeEditIntent(AccountMonitoringActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpParentButton() {
        Button btn = findViewById(R.id.btn_monitorParent);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MonitorParentActivity.makeEditIntent(AccountMonitoringActivity.this);
                startActivity(intent);
            }
        });
    }
}
