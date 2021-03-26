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
import java.util.Collections;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
Description: look at list of unread messages. Click on one to view it.
 */
public class UnreadMessagesMenuActivity extends AppCompatActivity {

    private SharedData mSharedData;
    private WGServerProxy proxy;
    private List<String> headerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unread_messages_menu);

        mSharedData = SharedData.getSharedData();

        proxy = mSharedData.getProxy();

        grabMessages();

        setUpListClick();
        setUpRefreshButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, UnreadMessagesMenuActivity.class);
    }

    private void grabMessages() {
        mSharedData.getUser().setMessages(new ArrayList<>());
        headerList = new ArrayList<>();
        Call<List<Message>> caller = proxy.getUnreadMessages(mSharedData.getUser().getId(), true);
        ProxyBuilder.callProxy(UnreadMessagesMenuActivity.this, caller, grabbedMessages -> setUpEmergencyMessages(grabbedMessages));

    }

    private void setUpEmergencyMessages(List<Message> grabbedMessages) {
        sortMessages(grabbedMessages);
        for (Message message: grabbedMessages) {
            mSharedData.getUser().getMessages().add(message);
            String header = "EMERGENCY - Message #" + message.getId() + " - " + message.getFromUser().getName() + " - " + message.getFromUser().getEmail();
            headerList.add(header);
        }
        Call<List<Message>> caller = proxy.getUnreadMessages(mSharedData.getUser().getId(), false);
        ProxyBuilder.callProxy(UnreadMessagesMenuActivity.this, caller, grabbedMessages2 -> setUpNonEmergencyMessages(grabbedMessages2));
    }

    private void setUpNonEmergencyMessages(List<Message> grabbedMessages) {
        sortMessages(grabbedMessages);
        for (Message message: grabbedMessages) {
            mSharedData.getUser().getMessages().add(message);
            String header = "Message #" + message.getId() + " - " + message.getFromUser().getName() + " - " + message.getFromUser().getEmail();
            headerList.add(header);
        }
        createList();
    }

    private void createList() {
        ListView listView = findViewById(R.id.unread_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                UnreadMessagesMenuActivity.this, R.layout.list_view, headerList);
        listView.setAdapter(adapter);
    }

    private void sortMessages(List<Message> list) {
        int toSwap;
        for (int i = 0; i < list.size(); i++) {
            toSwap = i;
            for (int j = i+1; j < list.size(); j++) {
                if (list.get(j).getId() < list.get(toSwap).getId()) {
                    toSwap = j;
                }
            }
            Collections.swap(list,i,toSwap);
        }

    }

    private void setUpListClick() {
        ListView listView = findViewById(R.id.unread_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = ViewMessageActivity.makeIntent(UnreadMessagesMenuActivity.this, position);
                startActivity(intent);
            }
        });
    }

    private void setUpRefreshButton() {
        Button btn = findViewById(R.id.btn_refresh_unread);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabMessages();
            }
        });
    }


}
