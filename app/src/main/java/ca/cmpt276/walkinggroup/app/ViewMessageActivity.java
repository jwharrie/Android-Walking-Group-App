package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
Description: view message text, identity of user who sent it and timestamp of message.
 */
public class ViewMessageActivity extends AppCompatActivity {

    private static final String index_string = "INDEX_OF_MESSAGE";
    private SharedData mSharedData;
    private WGServerProxy proxy;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);

        mSharedData = SharedData.getSharedData();

        proxy = mSharedData.getProxy();

        grabMessage();

        setUpMessage();

        if (!message.isRead()) {
            markMessageAsRead();
        }
    }

    public static Intent makeIntent(Context context, int index) {
        Intent intent = new Intent(context, ViewMessageActivity.class);
        intent.putExtra(ViewMessageActivity.index_string, index);
        return intent;
    }

    private void grabMessage() {
        Intent intent = getIntent();
        message = mSharedData.getUser().getMessages().get(intent.getIntExtra(index_string,0));
    }

    private void setUpMessage() {
        TextView id = findViewById(R.id.txt_message_id);
        id.setText("Message #" + message.getId());

        TextView fromText = findViewById(R.id.txt_message_from);
        fromText.setText("From: " + message.getFromUser().getName() + " - " + message.getFromUser().getEmail());

        TextView timeStampText = findViewById(R.id.txt_message_timestamp);
        timeStampText.setText("" + message.getTimestamp());

        TextView textBody = findViewById(R.id.txt_message_body);
        textBody.setText(message.getText());
    }

    private void markMessageAsRead() {
        Call<Message> caller = proxy.markMessageAsRead(message.getId(), true);
        ProxyBuilder.callProxy(ViewMessageActivity.this, caller, receivedMessage -> doNothing(receivedMessage));
    }

    private void doNothing(Message message) {

    }


}
