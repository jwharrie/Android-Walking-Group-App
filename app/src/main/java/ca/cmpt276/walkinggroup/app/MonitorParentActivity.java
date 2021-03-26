package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Display list of "parents" (users monitoring the current user).
 */
public class MonitorParentActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 500;

    private static final String MESSAGE_KEY_READ
            = "ca.cmpt276.walkinggroup.app.MonitorParentActivity - Read-only?";
    private static final String MESSAGE_KEY_ID
            = "ca.cmpt276.walkinggroup.app.MonitorParentActivity - ID";

    private WGServerProxy proxy;
    private SharedData sharedData = SharedData.getSharedData();
    private Long userId;
    private List<User> monitoredByUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_parent);

        proxy = sharedData.getProxy();
        userId = sharedData.getUser().getId();

        Intent intent = getIntent();
        boolean readOnly = intent.getBooleanExtra(MESSAGE_KEY_READ, false);
        if (readOnly) {
            userId = intent.getLongExtra(MESSAGE_KEY_ID, userId);
            setupReadComponents();
        } else {
            setupEditComponents();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {

            case REQUEST_CODE_ADD:
                if (resultCode == RESULT_OK) {
                    // Get user from result
                    User child = FindUserActivity.getUserFromIntent(data);

                    // Call server
                    Call<List<User>> caller = proxy.addToMonitoredByUsers(userId, child);
                    ProxyBuilder.callProxy(MonitorParentActivity.this, caller,
                            returnedUsers -> updateList(returnedUsers));
                }
        }
    }

    private void updateList(List<User> users) {
        monitoredByUsers = users;
        ListView listView = findViewById(R.id.monitorParentList);
        List<String> userNames = getUserNames(monitoredByUsers);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MonitorParentActivity.this, R.layout.list_view, userNames);
        listView.setAdapter(adapter);
    }

    private List<String> getUserNames(List<User> users) {
        List<String> names = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                names.add(user.getName());
            }
        }
        return names;
    }

    private void displayEditUser(int index) {
        // Display name and E-mail address of selected user
        User userToDisplay = monitoredByUsers.get(index);
        String message = getString(R.string.txt_displayEditUserFromList,
                userToDisplay.getName(),
                userToDisplay.getEmail());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void displayReadUser(int index) {
        // Display name and detailed contact info of selected user
        User userToDisplay = monitoredByUsers.get(index);
        String message = getString(R.string.txt_displayReadUserFromList,
                userToDisplay.getName(),
                userToDisplay.getEmail(),
                userToDisplay.getAddress(),
                userToDisplay.getHomePhone(),
                userToDisplay.getCellPhone());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void removeUser(int index) {
        // Call server, remove selected user from list
        User userToRemove = monitoredByUsers.get(index);
        Call<Void> caller = proxy.removeFromMonitoredByUsers(userId, userToRemove.getId());
        ProxyBuilder.callProxy(MonitorParentActivity.this, caller, returnedNothing -> setupList());
    }

    private void setupList() {
        // Get initial list, call server
        Call<List<User>> caller = proxy.getMonitoredByUsers(userId);
        ProxyBuilder.callProxy(MonitorParentActivity.this, caller,
                returnedUsers -> updateList(returnedUsers));
    }

    private void setupEditListClick() {
        ListView listView = findViewById(R.id.monitorParentList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                displayEditUser(index);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                removeUser(index);
                return true;
            }
        });
    }

    private void setupReadListClick() {
        ListView listView = findViewById(R.id.monitorParentList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                displayReadUser(index);
            }
        });
    }

    private void setupAddButton() {
        Button btn = findViewById(R.id.btn_addUser);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = FindUserActivity.makeIntent(MonitorParentActivity.this);
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });
    }

    private void hideAddButton() {
        Button btn = findViewById(R.id.btn_addUser);
        btn.setVisibility(View.GONE);
    }


    private void setupEditComponents() {
        TextView hdr = findViewById(R.id.monitorParentHeader);
        hdr.setText(getString(R.string.txt_monitorParent));

        setupList();
        setupEditListClick();
        setupAddButton();
    }

    private void setupReadComponents() {
        TextView hdr = findViewById(R.id.monitorParentHeader);
        hdr.setText(getString(R.string.txt_monitorParentReadOnly));

        setupList();
        setupReadListClick();
        hideAddButton();
    }

    public static Intent makeEditIntent(Context context) {
        Intent intent = new Intent(context, MonitorParentActivity.class);
        intent.putExtra(MESSAGE_KEY_READ, false);
        return intent;
    }

    public static Intent makeReadIntent(Context context, Long id) {
        Intent intent = new Intent(context, MonitorParentActivity.class);
        intent.putExtra(MESSAGE_KEY_READ, true);
        intent.putExtra(MESSAGE_KEY_ID, id);
        return intent;
    }
}
