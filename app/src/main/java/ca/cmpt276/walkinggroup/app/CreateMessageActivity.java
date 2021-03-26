package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
Description: allows user to create and send a message either as a child or as a group leader.
    Child messages get sent to all parents and the group leader
    Leader messages get sent to all group members and their parents
 */
public class CreateMessageActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        mSharedData = SharedData.getSharedData();
        proxy = mSharedData.getProxy();
        mCheckBox = findViewById(R.id.checkBox_emergency);

        setUpSendAsChildButton();
        setUpSendAsLeaderButton();
        if (mSharedData.getUser().getLeadsGroups().size() > 0) {
            mCheckBox.setVisibility(View.VISIBLE);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, CreateMessageActivity.class);
    }

    private void setUpSendAsChildButton() {
        Button button = findViewById(R.id.btn_message_child_send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.edit_new_message);
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(CreateMessageActivity.this, "The message is empty. Cannot send.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Message message = CreateMessageActivity.createMessage(false,editText.getText().toString(),mSharedData.getUser());
                    Call<List<Message>> caller = proxy.newMessageToParentsOf(mSharedData.getUser().getId(), message);
                    ProxyBuilder.callProxy(CreateMessageActivity.this, caller, returnedList -> closeActivity(returnedList));
                }
            }
        });
    }

    private void setUpSendAsLeaderButton() {
        Button button = findViewById(R.id.btn_message_leader_send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.edit_new_message);
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(CreateMessageActivity.this, "The message is empty. Cannot send.", Toast.LENGTH_SHORT).show();
                }
                else if (mSharedData.getUser().getLeadsGroups().size() == 0 ) {
                    Toast.makeText(CreateMessageActivity.this, "You do not lead any groups. Cannot send message.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Message message = CreateMessageActivity.createMessage(mCheckBox.isChecked(), editText.getText().toString(),mSharedData.getUser());
                    Call<List<Message>> caller = proxy.newMessageToGroup(mSharedData.getUser().getLeadsGroups().get(0).getId(), message);
                    ProxyBuilder.callProxy(CreateMessageActivity.this, caller, returnedList -> closeActivity(returnedList));
                }

            }
        });
    }

    public static Message createMessage(boolean isEmergency, String text, User fromUser) {
        Message message = new Message();
        message.setEmergency(isEmergency);
        message.setText(text);
        message.setFromUser(fromUser);
        return message;
    }

    private void closeActivity(List<Message> returnedList) {
        Toast.makeText(CreateMessageActivity.this, "Message sent.",Toast.LENGTH_LONG).show();
        finish();
    }
}
