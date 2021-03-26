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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
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

public class JoinGroupActivity extends AppCompatActivity {

    private final static String EXTRA_GROUPID = "ca.cmpt276.walkinggroup.app.groupId_to_join";

    private long groupId;

    private WGServerProxy proxy;
    private SharedData sharedData = SharedData.getSharedData();

    private Group selectedGroup;
    private User selectedGroupLeader;
    private List<User> groupMembers;
    private List<User> monitoredGroupMembers;
    private List<String> monitoredGroupMembersNames;
    private List<User> otherGroupMembers;
    private List<String> otherGroupMembersNames;

    private long userId;
    private User user;
    private long leaderId;
    private List<User> monitorsUsers;
    private List<User> monitorsUsersInGroup;
    private UserState userState;
    private int selectedUserToRemove;

    private enum UserState {
        MEMBER, LEADER, NOTMEMBER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        proxy = sharedData.getProxy();
        user = sharedData.getUser();
        userState = UserState.NOTMEMBER;
        groupMembers = new ArrayList<>();
        monitorsUsers = new ArrayList<>();
        monitorsUsersInGroup = new ArrayList<>();

        extractDataFromIntent();
        setupWalkSwitch();
        getGroupInfoFromTheServer();
    }
    //-----------------------------------------------------------


    //-----------------------------------------------------------
    private void getGroupInfoFromTheServer() {
        Call<Group> caller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedGroup -> response(returnedGroup));
    }

    private void response(Group returnedGroup) {
        selectedGroup = returnedGroup;
        Call<List<User>> caller = proxy.getGroupMembers(groupId);
        ProxyBuilder.callProxy(this, caller, returnedGroupUsers -> responseGetUsers(returnedGroupUsers));

    }

    private void responseGetUsers(List<User> returnedGroupUsers) {
        groupMembers = new ArrayList<>();
        groupMembers = returnedGroupUsers;
        Log.i("ret", returnedGroupUsers + "");

        Call<User> caller = proxy.getUserById(selectedGroup.getLeader().getId());
        ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedLeader -> responseToLeader(returnedLeader));
    }

    private void responseToLeader(User leader) {
        selectedGroupLeader = leader;
        getMonitoredUsersFromServer();
    }

    //-----------------------------------------------------------
    //Setup list with monitored users.
    private void getMonitoredUsersFromServer() {
        // Get initial list, call server
        Call<List<User>> caller = proxy.getMonitorsUsers(userId);
        ProxyBuilder.callProxy(JoinGroupActivity.this, caller,
                returnedUsers -> responseToMonitorsUsers(returnedUsers));
    }

    private void responseToMonitorsUsers(List<User> returnedUsers) {
        monitorsUsers = returnedUsers;
        setupListView();
    }

    // On response show the monitored users in a list view.
    private void setupListView() {
        setUserInfoTextView();
        leaderId = selectedGroup.getLeader().getId();

        Group monitoredGroup = new Group();
        for (User user : monitorsUsers) {
            monitoredGroup.addMember(user);
        }
        monitoredGroupMembers = new ArrayList<>();
        monitoredGroupMembersNames = new ArrayList<>();
        otherGroupMembers = new ArrayList<>();
        otherGroupMembersNames = new ArrayList<>();

        if (userId != leaderId) {
            if (monitoredGroup.containsMember(leaderId)) {
                monitoredGroupMembers.add(selectedGroupLeader);
                monitoredGroupMembersNames.add(selectedGroupLeader.getName() + " (leader)");
            } else {
                otherGroupMembers.add(selectedGroupLeader);
                otherGroupMembersNames.add(selectedGroupLeader.getName() + " (leader)");
            }
        }

        for (User user : monitorsUsers) {
            if (selectedGroup.containsMember(user.getId())) {
                monitoredGroupMembers.add(user);
                monitoredGroupMembersNames.add(user.getName());
            }
        }

        for (User user : groupMembers) {
            if (!monitoredGroup.containsMember(user.getId())) {
                otherGroupMembersNames.add(user.getName());
                otherGroupMembers.add(user);
            }
        }

        ListView listView = findViewById(R.id.JoinGroupActivityChildList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                JoinGroupActivity.this, R.layout.list_view, monitoredGroupMembersNames);

        listView.setAdapter(adapter);

        // Other members make visible and setup.
        TextView textViewOthersHeading = findViewById(R.id.joinGroupActivityOtherMembersTitle);
        textViewOthersHeading.setVisibility(View.VISIBLE);

        if (otherGroupMembersNames != null)
        {
            ListView listViewOthers = findViewById(R.id.JoinGroupActivityOtherMembersList);
            ArrayAdapter<String> adapterOthers = new ArrayAdapter<>(
                    JoinGroupActivity.this, R.layout.list_view, otherGroupMembersNames);

            listViewOthers.setAdapter(adapterOthers);
        }

        setupAddUserBtn();
        setupJoinLeaveButton();
        setupMonitoredListClick();
        setupOthersListClick();
    }



    private void setUserInfoTextView() {
        TextView textView = findViewById(R.id.joinGroupActivityUserInfo);
        Button joinLeaveBtn = findViewById(R.id.joinActivityJoinLeaveButton);

        long selectedGroupLeaderId = selectedGroup.getLeader().getId();
        if (userId == selectedGroupLeaderId) {
            textView.setText(R.string.joinActivityLeaderHeading);
            joinLeaveBtn.setText(R.string.deleteBtn);
            userState = UserState.LEADER;
        } else {
            if (GroupList.getInstance().getGroupById(groupId).containsMember(userId)) {
                textView.setText(R.string.joinActivityMemberHeading);
                joinLeaveBtn.setText(R.string.leaveBtn);
                userState = UserState.MEMBER;

            } else {
                textView.setText(R.string.JoinActivitynotAMemberHeading);
                joinLeaveBtn.setText(R.string.joinBtn);
                userState = UserState.NOTMEMBER;
            }
        }
    }

    private void setupAddUserBtn() {
        Button addBtn = findViewById(R.id.joinActivityJoinAddMembersButton);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToAdd = AddUsersActivity.makeIntentWithData(JoinGroupActivity.this, groupId);
                startActivity(intentToAdd);
            }
        });
    }

    private void setupJoinLeaveButton() {
        Button joinLeaveBtn = findViewById(R.id.joinActivityJoinLeaveButton);
        joinLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userState == UserState.MEMBER) {
                    GroupList.getInstance().getGroupById(groupId).removeMemberById(userId);
                    selectedGroup.removeMemberById(userId);
                    Call<Void> caller = proxy.removeGroupMember(groupId, userId);
                    ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedNothing -> removeSelfResponse(returnedNothing));
                } else if (userState == UserState.NOTMEMBER) {
                    User user = new User();
                    user.setId(sharedData.getUser().getId());
                    GroupList.getInstance().getGroupById(groupId).addMember(user);
                    selectedGroup.addMember(user);

                    Call<List<User>> caller = proxy.addGroupMember(groupId, user);
                    ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedMembers -> addMemberResponse(returnedMembers));
                } else {
                    Call<Void> caller = proxy.deleteGroup(groupId);
                    ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedNothing -> deleteGroupResponse(returnedNothing));
                }
            }
        });
    }

    private void removeSelfResponse(Void returnedNothing) {
        Toast.makeText(this, R.string.success_removing, Toast.LENGTH_SHORT).show();
        sharedData.getUser().removeUserFromGroup(groupId);
        selectedGroup.removeMemberById(userId);
        GroupList.getInstance().getGroupById(selectedGroup.getId()).removeMemberById(userId);

        TextView textView = findViewById(R.id.joinGroupActivityUserInfo);
        textView.setText(R.string.JoinActivitynotAMemberHeading);
        Button joinLeaveBtn = findViewById(R.id.joinActivityJoinLeaveButton);
        joinLeaveBtn.setText(getString(R.string.joinBtn));

        userState = UserState.NOTMEMBER;
    }

    private void addMemberResponse(List<User> returnedMembers) {
        Toast.makeText(this, R.string.success_joined, Toast.LENGTH_SHORT).show();
        sharedData.getUser().addUserToGroup(selectedGroup);
        selectedGroup.addMember(sharedData.getUser());
        GroupList.getInstance().getGroupById(selectedGroup.getId()).addMember(sharedData.getUser());

        TextView textView = findViewById(R.id.joinGroupActivityUserInfo);
        textView.setText(R.string.joinActivityMemberHeading);
        Button joinLeaveBtn = findViewById(R.id.joinActivityJoinLeaveButton);
        joinLeaveBtn.setText(getString(R.string.leaveBtn));

        userState = UserState.MEMBER;
    }

    private void deleteGroupResponse(Void returnedNothing) {
        Toast.makeText(this, selectedGroup.getGroupDescription() + getString(R.string.group_deleted), Toast.LENGTH_SHORT).show();
        finish();
    }
    //-----------------------------------------------------------

    //-----------------------------------------------------------
    // press to get user info and long press to remove the user.
    private void setupMonitoredListClick() {
        ListView listView = findViewById(R.id.JoinGroupActivityChildList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                displayUserMonitored(index);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                removeUserMonitored(index);
                return true;
            }
        });
    }

    private void setupOthersListClick() {
        ListView listView = findViewById(R.id.JoinGroupActivityOtherMembersList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                displayUserOthers(index);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                if (userId == leaderId) {
                    removeUserOthers(index);
                    return true;
                }else{
                    Toast.makeText(JoinGroupActivity.this, "Only leader could remove members from a group.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }

    // Display name and E-mail address of selected user called on clicking on the user name in list view.
    private void displayUserMonitored(int index) {
        User userToDisplay = monitoredGroupMembers.get(index);
        String userInfo = userToDisplay.getName() + ", " + userToDisplay.getEmail();
        String message = userInfo + ",  " + getString(R.string.txt_joinGroupFromList);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void displayUserOthers(int index) {
        User userToDisplay = otherGroupMembers.get(index);
        String userInfo = userToDisplay.getName() + ", " + userToDisplay.getEmail();
        String message;
        if(userId == leaderId){
            message = userInfo + ",  " + getString(R.string.txt_joinGroupFromList);
        }else{
            message = userInfo + ",  " + getString(R.string.txt_joinGroupFromListNotRemoveable);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // remove the user from the group and the listView on long pressing the user on the list.
    private void removeUserMonitored(int index) {
        // Call server, remove selected user from list
        User userToRemove = monitoredGroupMembers.get(index);
        selectedUserToRemove = index;
        Call<Void> caller = proxy.removeGroupMember(groupId, userToRemove.getId());
        ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedNothing -> removeUserResponseMonitored(returnedNothing));
    }

    private void removeUserOthers(int index) {
        // Call server, remove selected user from list
        User userToRemove = otherGroupMembers.get(index);
        selectedUserToRemove = index;
        Call<Void> caller = proxy.removeGroupMember(groupId, userToRemove.getId());
        ProxyBuilder.callProxy(JoinGroupActivity.this, caller, returnedNothing -> removeUserResponseOthers(returnedNothing));
    }

    private void removeUserResponseMonitored(Void returnedNothing) {
        String removedUserName = monitoredGroupMembers.get(selectedUserToRemove).getName();
        monitoredGroupMembers.remove(selectedUserToRemove);
        monitoredGroupMembersNames.remove(selectedUserToRemove);

        ListView listView = findViewById(R.id.JoinGroupActivityChildList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                JoinGroupActivity.this, R.layout.list_view, monitoredGroupMembersNames);
        listView.setAdapter(adapter);
        Toast.makeText(this, getString(R.string.success_removed) + " " +removedUserName, Toast.LENGTH_SHORT).show();
    }

    private void removeUserResponseOthers(Void returnedNothing) {
        String removedUserName = otherGroupMembers.get(selectedUserToRemove).getName();
        otherGroupMembers.remove(selectedUserToRemove);
        otherGroupMembersNames.remove(selectedUserToRemove);

        ListView listView = findViewById(R.id.JoinGroupActivityOtherMembersList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                JoinGroupActivity.this, R.layout.list_view, otherGroupMembersNames);
        listView.setAdapter(adapter);
        Toast.makeText(this, getString(R.string.success_removed) + " " + removedUserName, Toast.LENGTH_SHORT).show();
    }
    //-----------------------------------------------------------

    private void setupWalkSwitch() {
        Switch walkSwitch = findViewById(R.id.joinActivityWalkSwitch);

        if(user.getWalkWithGroupId() == groupId){
                walkSwitch.setChecked(true);
        }
        else{
            walkSwitch.setChecked(false);
        }

        walkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedData.getUser().setWalkWithGroupId(groupId);
                }
                else{
                    sharedData.getUser().setWalkWithGroupId(0);
                }
            }
        });
    }

    public static Intent makeIntentWithData(Context context, Long groupId) {
        Intent intent = new Intent(context, JoinGroupActivity.class);
        intent.putExtra(EXTRA_GROUPID, groupId);

        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        groupId = intent.getLongExtra(EXTRA_GROUPID, 0);
        userId = SharedData.getSharedData().getUser().getId();
        selectedGroup = GroupList.getInstance().getGroupById(groupId);
    }

    @Override
    protected void onResume() {
        getGroupInfoFromTheServer();
        super.onResume();
    }
}
