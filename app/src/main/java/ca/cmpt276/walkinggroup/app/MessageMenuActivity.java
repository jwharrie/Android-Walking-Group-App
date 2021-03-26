package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
Description: menu for messages
    Can access unread messages list and read messages list.
    Can create a new message.
    Can see number of emergency messages and total unread messages (emergency and non-emergency messages)
 */
public class MessageMenuActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_menu);

        mSharedData = SharedData.getSharedData();

        proxy = mSharedData.getProxy();

        checkForMessages();

        setUpViewUnreadMessagesButton();
        setUpViewReadMessagesButton();
        setUpSendNewMessageButton();
        setUpRefreshButton();
        setUpPanicButton();

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MessageMenuActivity.class);
    }

    private void checkForMessages() {
        Call<List<Message>> caller = proxy.getUnreadMessages(mSharedData.getUser().getId(), true);
        ProxyBuilder.callProxy(MessageMenuActivity.this, caller, returnedList -> showNumberOfUnreadEmergencyMessages(returnedList));
    }


    private void showNumberOfUnreadEmergencyMessages(List<Message> grabbedUnreadMessages) {
        TextView text = findViewById(R.id.txt_number_emergency_messages);
        int number = grabbedUnreadMessages.size();
        text.setText("Emergency messages to read: " + number);

        Call<List<Message>> caller = proxy.getUnreadMessages(mSharedData.getUser().getId(), false);
        ProxyBuilder.callProxy(MessageMenuActivity.this, caller, returnedList -> showNumberOfTotalUnreadMessages(returnedList, number));
    }

    private void showNumberOfTotalUnreadMessages(List<Message> grabbedUnreadMessages, int numberEmergency) {
        TextView text = findViewById(R.id.txt_total_number_unread_messages);
        int total = grabbedUnreadMessages.size() + numberEmergency;
        text.setText("Total number of unread messages: " + total);
    }

    private void setUpViewUnreadMessagesButton() {
        Button btn = findViewById(R.id.btn_view_unread_messages);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UnreadMessagesMenuActivity.makeIntent(MessageMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpViewReadMessagesButton() {
        Button btn = findViewById(R.id.btn_view_read_messages);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ReadMessagesMenuActivity.makeIntent(MessageMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpSendNewMessageButton() {
        Button btn = findViewById(R.id.btn_send_new_message);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CreateMessageActivity.makeIntent(MessageMenuActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setUpRefreshButton() {
        Button btn = findViewById(R.id.btn_refresh_messages);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForMessages();
            }
        });
    }

    private void setUpPanicButton() {
        Button btn = findViewById(R.id.btn_PANIC);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "PANIC BUTTON PRESSED BY " + mSharedData.getUser().getName() + ".";
                Message message = CreateMessageActivity.createMessage(true,text,mSharedData.getUser());
                Call<List<Message>> caller = proxy.newMessageToParentsOf(mSharedData.getUser().getId(), message);
                ProxyBuilder.callProxy(MessageMenuActivity.this, caller, returnedList -> makePanicToast(returnedList));
            }
        });
    }

    private void makePanicToast(List<Message> returnedList) {
        Toast.makeText(MessageMenuActivity.this, "Emergency message sent.", Toast.LENGTH_LONG).show();
    }
}
