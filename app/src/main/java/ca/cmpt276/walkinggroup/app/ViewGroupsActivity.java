package ca.cmpt276.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.GroupList;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ViewGroupsActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private SharedData sharedData = SharedData.getSharedData();
    private GroupList groupList = GroupList.getInstance();
    private User user;

    private List<Group> leaderGroups;
    private List<Group> memberGroups;
    private List<Group> monitorGroups;

    private List<String> groupDescriptions;
    private List<Long> groupIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);

        proxy = sharedData.getProxy();
        user = sharedData.getUser();
        fetchGroupsFromTheServer();
    }

    private void fetchGroupsFromTheServer() {
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(ViewGroupsActivity.this, caller,
                returnedGroups -> setupListsAndComponents(returnedGroups));
    }

    private List<String> getGroupDescriptions(List<Group> groups) {
        List<String> descriptions = new ArrayList<>();
        if (groups != null) {
            for (Group group : groups) {
                descriptions.add(group.getGroupDescription());
            }
        }
        return descriptions;
    }

    private List<Long> getGroupIds(List<Group> groups) {
        List<Long> ids = new ArrayList<>();
        if (groups != null) {
            for (Group group : groups) {
                ids.add(group.getGroupId());
            }
        }
        return ids;
    }

    private void getLists() {
        leaderGroups = new ArrayList<>();
        for (Long id : getGroupIds(user.getLeadsGroups())) {
            leaderGroups.add(groupList.getGroupById(id));
        }

        memberGroups = new ArrayList<>();
        for (Long id : getGroupIds(user.getMemberOfGroups())) {
            memberGroups.add(groupList.getGroupById(id));
        }

        Call<List<User>> caller = proxy.getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(ViewGroupsActivity.this, caller,
                returnedUsers -> monitorGroups = getMonitorGroups(returnedUsers));
    }

    private List<Group> getMonitorGroups(List<User> monitoredUsers) {
        List<Group> groups = new ArrayList<>();
        if (monitoredUsers != null) {
            List<Long> monitorGroupIds = new ArrayList<>();
            for (User monitoredUser : monitoredUsers) {
                for (Long id : getGroupIds(monitoredUser.getMemberOfGroups())) {
                    if (!monitorGroupIds.contains(id)) {
                        monitorGroupIds.add(id);
                    }
                }
            }
            for (Long id : monitorGroupIds) {
                groups.add(groupList.getGroupById(id));
            }
        }
        return groups;
    }

    private void setupList() {
        ListView listView = findViewById(R.id.groupsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ViewGroupsActivity.this, R.layout.list_view, groupDescriptions);
        listView.setAdapter(adapter);
    }

    private void setupListClick() {
        ListView listView = findViewById(R.id.groupsList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                Intent intent = ViewGroupMembersActivity.makeIntent(
                        ViewGroupsActivity.this,
                        groupIds.get(index), groupDescriptions.get(index));
                startActivity(intent);
            }
        });
    }

    private void setupLeaderButton() {
        Button btn = findViewById(R.id.btn_leader);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView hdr = findViewById(R.id.groupsHeader);
                hdr.setText(R.string.txt_viewGroupsAsLeader);

                groupDescriptions = getGroupDescriptions(leaderGroups);
                groupIds = getGroupIds(leaderGroups);

                setupList();
                setupListClick();
            }
        });
    }

    private void setupMemberButton() {
        Button btn = findViewById(R.id.btn_member);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView hdr = findViewById(R.id.groupsHeader);
                hdr.setText(R.string.txt_viewGroupsAsMember);

                groupDescriptions = getGroupDescriptions(memberGroups);
                groupIds = getGroupIds(memberGroups);

                setupList();
                setupListClick();
            }
        });
    }

    private void setupMonitorButton() {
        Button btn = findViewById(R.id.btn_monitor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView hdr = findViewById(R.id.groupsHeader);
                hdr.setText(R.string.txt_viewGroupsAsMonitor);

                groupDescriptions = getGroupDescriptions(monitorGroups);
                groupIds = getGroupIds(monitorGroups);

                setupList();
                setupListClick();
            }
        });
    }

    private void setupListsAndComponents(List<Group> returnedGroups) {
        groupList.setGroups(returnedGroups);
        getLists();
        setupLeaderButton();
        setupMemberButton();
        setupMonitorButton();
    }
}
