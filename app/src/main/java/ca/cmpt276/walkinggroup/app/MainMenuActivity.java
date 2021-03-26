package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "ServerTest";
    private WGServerProxy proxy;
    private SharedData sharedData;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        sharedData = SharedData.getSharedData();
        proxy = sharedData.getProxy();
        Toast.makeText(MainMenuActivity.this, "Successfully logged in.", Toast.LENGTH_LONG).show();

        setupButtons();

        checkMessages();

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MainMenuActivity.class);
    }

    private void setupGoToMapsBtn() {
        Button btn = findViewById(R.id.btn_goToMaps);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedData.getProxy() != null) {
                    Intent intent = MapsActivity.makeIntent(MainMenuActivity.this);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainMenuActivity.this, "Please login first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setupParentsDashboard() {
        Button btn = findViewById(R.id.btn_parentsDashboardMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = parentMapActivity.makeIntent(MainMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupLogoutButton(){
        Button btn = findViewById(R.id.btn_logout);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedData.getToken() != null) {
                    proxy = ProxyBuilder.getProxy(getString(R.string.apikey), null);
                    StartupMenuActivity.saveLoggedInUserData(MainMenuActivity.this, null, null);
                    sharedData.setToken(null);

                    Toast.makeText(MainMenuActivity.this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                    finish();

                }else{
                    Toast.makeText(MainMenuActivity.this, "Please Login First", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void setupMessagesButton() {
        Button btn = findViewById(R.id.btn_messages);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MessageMenuActivity.makeIntent(MainMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupButtons(){
        setupGoToMapsBtn();
        setupParentsDashboard();
        setupLogoutButton();
        setupMessagesButton();
        setUpGameMenuButton();
        setUpPermissionsMenuButton();
        setUpAccountManagementButton();
    }

    private void checkMessages() {
        handler = new Handler();
        int delay = 60000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sharedData.getToken() != null) {
                    checkForMessages();
                }

                handler.postDelayed(this,delay);
            }
        }, delay);
    }

    private void checkForMessages() {
        Call<List<Message>> caller = proxy.getUnreadMessages(sharedData.getUser().getId(), true);
        ProxyBuilder.callProxy(MainMenuActivity.this, caller, returnedList -> showNumberOfUnreadEmergencyMessages(returnedList));
    }


    private void showNumberOfUnreadEmergencyMessages(List<Message> grabbedUnreadMessages) {
        int number = grabbedUnreadMessages.size();
        if (number > 0) {
            Toast.makeText(MainMenuActivity.this, "EMERGENCY MESSAGES TO READ: " + number, Toast.LENGTH_LONG).show();
        }
        Call<List<Message>> caller = proxy.getUnreadMessages(sharedData.getUser().getId(), false);
        ProxyBuilder.callProxy(MainMenuActivity.this, caller, returnedList -> showNumberOfTotalUnreadMessages(returnedList, number));
    }

    private void showNumberOfTotalUnreadMessages(List<Message> grabbedUnreadMessages, int numberEmergency) {
        int total = grabbedUnreadMessages.size() + numberEmergency;
        Toast.makeText(MainMenuActivity.this, "Total number of unread messages: " + total, Toast.LENGTH_LONG).show();
        Log.i(TAG, "Total number of unread messages:" + total);
    }

    private void setUpPermissionsMenuButton() {
        Button btn = findViewById(R.id.btn_permissions_menu);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PermissionsMenuActivity.makeIntent(MainMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpGameMenuButton() {
        Button button = findViewById(R.id.btn_game_menu);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = GameMenuActivity.makeIntent(MainMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpAccountManagementButton() {
        Button button = findViewById(R.id.btn_account_management);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AccountManagementActivity.makeIntent(MainMenuActivity.this);
                startActivity(intent);
            }
        });
    }
}
