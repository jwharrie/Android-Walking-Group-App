package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.GroupList;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class AddUsersActivity extends AppCompatActivity {

    private final static String EXTRA_GROUPIDKEY = "ca.cmpt276.walkingGroup.app.groupId.key";
    private WGServerProxy proxy;
    private int indexRemoved;
    private SharedData sharedData = SharedData.getSharedData();
    private long userId;
    private long groupId;
    private List<User> monitorsUsers;
    private List<User> groupMembers;
    private List<User> notMonitoredGroupMembers;
    private List<String> notMonitoredGroupMembersNames;
    private Group selectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);

        proxy = sharedData.getProxy();

        notMonitoredGroupMembers = new ArrayList<>();
        notMonitoredGroupMembersNames = new ArrayList<>();
        extractDataFromIntent();
        setupDoneButton();
        getGroupInfoFromTheServer();
    }

    private void getGroupInfoFromTheServer() {
        Call<Group> caller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(AddUsersActivity.this, caller, returnedGroup -> response(returnedGroup));
    }

    private void response(Group returnedGroup) {
        selectedGroup = returnedGroup;
        Log.i("selected group" , "" + selectedGroup);
        groupMembers = selectedGroup.getMemberUsers();
        Log.i("returned Group: ", returnedGroup.toString());
        getMonitoredUsers();

    }
    private void getMonitoredUsers() {
        // Get initial list, call server
        Call<List<User>> caller = proxy.getMonitorsUsers(userId);
        ProxyBuilder.callProxy(AddUsersActivity.this, caller,
                returnedUsers -> responseToReturnedMonitoredUsers(returnedUsers));
    }

    private void responseToReturnedMonitoredUsers(List<User> users){
        monitorsUsers = users;
        for (User user : users) {
            if (!selectedGroup.containsMember(user.getId())) {
                notMonitoredGroupMembers.add(user);
                notMonitoredGroupMembersNames.add(user.getName());
                Log.i("Monitored User", user.getName());
            }
        }
        updateList();
    }

    private void updateList() {
        ListView listView = findViewById(R.id.addUserList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                AddUsersActivity.this, R.layout.list_view, notMonitoredGroupMembersNames);
        listView.setAdapter(adapter);
        setupListClick();
    }

    private void setupListClick() {
        ListView listView = findViewById(R.id.addUserList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                displayUser(index);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                addUserToGroup(index);
                return true;
            }
        });
    }

    private void displayUser(int index) {
        // Display name and E-mail address of selected user
        User userToDisplay = monitorsUsers.get(index);
        String userInfo = userToDisplay.getName() + ", " + userToDisplay.getEmail();
        String message = userInfo + getString(R.string.txt_addUserClicked).toString();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void addUserToGroup(int index) {
        // Call server, add selected user from list
        indexRemoved = index;
        User userToAdd = new User();
        userToAdd.setId(notMonitoredGroupMembers.get(index).getId());
        Call<List<User>> caller = proxy.addGroupMember(groupId, userToAdd);
        ProxyBuilder.callProxy(AddUsersActivity.this, caller, returnedUsers -> addedUserResponse(returnedUsers));
    }

    private void addedUserResponse(List<User> returnedUsers){
        notMonitoredGroupMembers.remove(indexRemoved);
        notMonitoredGroupMembersNames.remove(indexRemoved);
        updateList();
    }

    private void setupDoneButton() {
        Button btn = findViewById(R.id.addUserDoneBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public static Intent makeIntentWithData(Context context, long groupIdForIntent) {
        Intent intent = new Intent(context, AddUsersActivity.class);
        intent.putExtra(EXTRA_GROUPIDKEY, groupIdForIntent);

        return intent;
    }

    private void extractDataFromIntent(){
        Intent intent = getIntent();
        groupId = intent.getLongExtra(EXTRA_GROUPIDKEY, 0);

        userId = SharedData.getSharedData().getUser().getId();
        selectedGroup = GroupList.getInstance().getGroupById(groupId);
        Log.i("groupId ", groupId + "");
    }
}
