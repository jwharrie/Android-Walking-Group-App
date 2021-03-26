package ca.cmpt276.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

@SuppressWarnings("serial")
public class ChooseChildToEditActivity extends AppCompatActivity implements Serializable {

    private static final String MESSAGE_KEY_CHILD_INDEX = "CHILD CHOSEN TO EDIT";
    private static final int REQUEST_CODE_REFRESH = 456;


    private WGServerProxy proxy;
    private SharedData sharedData = SharedData.getSharedData();
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_to_edit);

        Toast.makeText(this, R.string.txt_editChildInstructions, Toast.LENGTH_LONG).show();

        userId = sharedData.getUser().getId();
        proxy = sharedData.getProxy();

        setupComponents();
    }

    private void updateList(List<User> users) {
        ListView listView = findViewById(R.id.monitorChildList);
        List<String> userNames = getUserNames(users);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ChooseChildToEditActivity.this, R.layout.list_view, userNames);
        listView.setAdapter(adapter);
    }

    private List<String> getUserNames(List<User> users) {
        List<String> names = new ArrayList<>();
        for (User user : users) {
            names.add(user.getName());
        }
        return names;
    }

    private void setupList() {
        // Get initial list, call server
        Call<List<User>> caller = proxy.getMonitorsUsers(userId);
        ProxyBuilder.callProxy(ChooseChildToEditActivity.this, caller,
                returnedUsers -> updateList(returnedUsers));
    }

    private void setupListClick() {
        ListView listView = findViewById(R.id.monitorChildList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                Intent intent = new Intent(ChooseChildToEditActivity.this
                        , EditChosenChildInfo.class);
                intent.putExtra(MESSAGE_KEY_CHILD_INDEX, index);
                startActivityForResult(intent, REQUEST_CODE_REFRESH);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_REFRESH:
                if(resultCode == Activity.RESULT_OK){
                    setupList();
                }
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ChooseChildToEditActivity.class);
    }

    private void setupComponents(){
        setupList();
        setupListClick();
    }

}
