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

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
Shows list of permissions that are pending for the user.
 */
public class PendingPermissionsActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private List<String> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_permissions);

        mSharedData = SharedData.getSharedData();
        proxy = mSharedData.getProxy();

        grabUserFromServer();

        setUpListClick();

        setUpRefreshButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context,PendingPermissionsActivity.class);
    }

    private void grabUserFromServer() {
        Call<User> caller = proxy.getUserById(mSharedData.getUser().getId());
        ProxyBuilder.callProxy(PendingPermissionsActivity.this, caller, returnedUser->updateUser(returnedUser));
    }

    private void updateUser(User returnedUser) {
        mSharedData.setUser(returnedUser);
        setUpListItems();
    }

    private void setUpListItems() {
        listItems = new ArrayList<>();
        List<PermissionRequest> list = mSharedData.getUser().getPendingPermissionRequests();
        for (PermissionRequest permission:list) {
            String item = "Permission Request " + permission.getId() + " - " + permission.getAction();
            listItems.add(item);
        }
        createList();
    }

    private void createList() {
        ListView listView = findViewById(R.id.pending_permissions_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                PendingPermissionsActivity.this, R.layout.list_view, listItems);
        listView.setAdapter(adapter);
    }

    private void setUpListClick() {
        ListView listView = findViewById(R.id.pending_permissions_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = ViewPendingPermissionActivity.makeIntent(PendingPermissionsActivity.this,position);
                startActivity(intent);
            }
        });
    }

    private void setUpRefreshButton() {
        Button btn = findViewById(R.id.btn_refresh_pending_permissions);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabUserFromServer();
                setUpListItems();
                createList();
            }
        });
    }
}
