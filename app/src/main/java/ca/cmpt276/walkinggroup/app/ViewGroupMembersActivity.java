package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ViewGroupMembersActivity extends AppCompatActivity {

    private static final String MESSAGE_KEY_ID
            = "ca.cmpt276.walkinggroup.app.ViewGroupMembersActivity - ID";
    private static final String MESSAGE_KEY_DESCRIPTION
            = "ca.cmpt276.walkinggroup.app.ViewGroupMembersActivity - description";

    private SharedData sharedData = SharedData.getSharedData();
    private List<String> memberNames;
    private List<Long> memberIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_members);

        WGServerProxy proxy = sharedData.getProxy();

        Long groupId = getIntent().getLongExtra(MESSAGE_KEY_ID, 0);
        Call<List<User>> caller = proxy.getGroupMembers(groupId);
        ProxyBuilder.callProxy(ViewGroupMembersActivity.this, caller,
                returnedUsers -> setGroupAndComponents(returnedUsers));
    }

    private void setGroupAndComponents(List<User> users) {
        memberNames = getUserNames(users);
        memberIds = getUserIds(users);
        setupComponents();
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

    private List<Long> getUserIds(List<User> users) {
        List<Long> ids = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                ids.add(user.getId());
            }
        }
        return ids;
    }

    private void setupList() {
        ListView listView = findViewById(R.id.groupMembersList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ViewGroupMembersActivity.this, R.layout.list_view, memberNames);
        listView.setAdapter(adapter);
    }

    private void setupListClick() {
        ListView listView = findViewById(R.id.groupMembersList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                Intent intent = MonitorParentActivity.makeReadIntent(
                        ViewGroupMembersActivity.this, memberIds.get(index));
                startActivity(intent);
            }
        });
    }

    private void setupComponents() {
        TextView hdr = findViewById(R.id.groupName);
        hdr.setText(getIntent().getStringExtra(MESSAGE_KEY_DESCRIPTION));

        setupList();
        setupListClick();
    }

    public static Intent makeIntent(Context context, Long groupId, String groupDescription) {
        Intent intent = new Intent(context, ViewGroupMembersActivity.class);
        intent.putExtra(MESSAGE_KEY_ID, groupId);
        intent.putExtra(MESSAGE_KEY_DESCRIPTION, groupDescription);
        return intent;
    }
}