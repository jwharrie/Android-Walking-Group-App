package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class PermissionHistoryActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private List<String> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_history);

        mSharedData = SharedData.getSharedData();
        proxy = mSharedData.getProxy();

        getPermissions();
        clickForDetails();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, PermissionHistoryActivity.class);
    }

    private void getPermissions() {
        Call<List<PermissionRequest>> caller = proxy.getPermissions();
        ProxyBuilder.callProxy(this, caller, returnedList -> setPermissions(returnedList));
    }

    private void setPermissions(List<PermissionRequest> returnedList) {
        mSharedData.getUser().setPendingPermissionRequests(new ArrayList<>());
        List<PermissionRequest> temp_list = new ArrayList<>();
        for (PermissionRequest permission:returnedList) {
            if (    permission.getStatus() == WGServerProxy.PermissionStatus.APPROVED ||
                    permission.getStatus() == WGServerProxy.PermissionStatus.DENIED) {

                temp_list.add(permission);
            }
        }
        for (PermissionRequest permission:temp_list) {
            Set<PermissionRequest.Authorizor> auth_set = permission.getAuthorizors();
            for (PermissionRequest.Authorizor authorizor:auth_set) {
                Set<User> user_set = authorizor.getUsers();
                for (User user:user_set) {
                    if (user.getId().equals(mSharedData.getUser().getId()) && permission.getStatus()
                            == WGServerProxy.PermissionStatus.APPROVED
                            || user.getId().equals(mSharedData.getUser().getId())
                            && permission.getStatus() == WGServerProxy.PermissionStatus.DENIED) {
                        mSharedData.getUser().getPendingPermissionRequests().add(permission);
                    }
                }
            }
        }
        populateList();
    }

    private void clickForDetails() {
        ListView listView = findViewById(R.id.permissionList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = ShowPermissionRequestDetails.makeIntent(PermissionHistoryActivity.this ,position);
                startActivity(intent);
            }
        });
    }

    private void populateList() {
        listItems = new ArrayList<>();
        List<PermissionRequest> list = mSharedData.getUser().getPendingPermissionRequests();
        for (PermissionRequest permission:list) {
            String item = "Permission Request " + permission.getId() + " - " + permission.getAction();
            listItems.add(item);
        }
        setupList();
    }

    private void setupList() {
        ListView listView = findViewById(R.id.permissionList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.list_view, listItems);
        listView.setAdapter(adapter);
    }
}
