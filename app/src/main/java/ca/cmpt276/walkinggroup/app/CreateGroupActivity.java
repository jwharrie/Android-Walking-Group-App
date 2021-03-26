package ca.cmpt276.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String EXTRA_GROUP_NAME = "ca.cmpt276.walkinggroup.app.group_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        setupButtons();
    }

    private void setupCreateBtn() {
        Button createBtn = findViewById(R.id.create_group_create_btn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedData sharedData = SharedData.getSharedData();

                EditText groupNameEditText = findViewById(R.id.create_group_group_name);
                String groupName = groupNameEditText.getText().toString();

                Group group = new Group();
                group.setGroupDescription(groupName);

                User leader = new User();
                leader.setId(sharedData.getUser().getId());
                leader.setHref(sharedData.getUser().getHref());
                group.setLeader(leader);
                sharedData.setGroup(group);
                Log.i("Group: ", group.toString());

                setResult(Activity.RESULT_OK, getIntent());
                finish();
            }
        });
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, CreateGroupActivity.class);
    }

    private void setupButtons(){
        setupCreateBtn();
    }

}
