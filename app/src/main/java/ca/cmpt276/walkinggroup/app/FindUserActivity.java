package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class FindUserActivity extends AppCompatActivity {

    private static final String MESSAGE_KEY_ID
            = "ca.cmpt276.walkinggroup.app.FindUserActivity - ID";
    private static final String MESSAGE_KEY_NAME
            = "ca.cmpt276.walkinggroup.app.FindUserActivity - NAME";
    private static final String MESSAGE_KEY_EMAIL
            = "ca.cmpt276.walkinggroup.app.FindUserActivity - EMAIL";

    private WGServerProxy proxy;

    private SharedData sharedData = SharedData.getSharedData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        proxy = sharedData.getProxy();
        setupButtons();
    }

    private void setupFindByIdButton() {
        Button btn = findViewById(R.id.btn_inputId);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pull ID from user input
                EditText input = findViewById(R.id.inputId);
                Long inputId = Long.valueOf(Long.parseLong(input.getText().toString()));

                // Make call
                Call<User> caller = proxy.getUserById(Long.valueOf(inputId));
                ProxyBuilder.callProxy(FindUserActivity.this, caller,
                        returnedUser -> passUserAsResult(returnedUser));
            }
        });
    }

    private void setupFindByEmailButton() {
        Button btn = findViewById(R.id.btn_inputEmail);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pull E-mail address from user input
                EditText input = findViewById(R.id.inputEmail);
                String inputEmail = input.getText().toString();

                // Make call
                Call<User> caller = proxy.getUserByEmail(inputEmail);
                ProxyBuilder.callProxy(FindUserActivity.this, caller,
                        returnedUser -> passUserAsResult(returnedUser));
            }
        });
    }

    private void passUserAsResult(User user) {
        Intent intent = new Intent();
        intent.putExtra(MESSAGE_KEY_ID, user.getId());
        intent.putExtra(MESSAGE_KEY_NAME, user.getName());
        intent.putExtra(MESSAGE_KEY_EMAIL, user.getEmail());
        setResult(RESULT_OK, intent);
        finish();
    }

    public static User getUserFromIntent(Intent data) {
        User user = new User();
        user.setId(data.getLongExtra(MESSAGE_KEY_ID, 0));
        user.setName(data.getStringExtra(MESSAGE_KEY_NAME));
        user.setEmail(data.getStringExtra(MESSAGE_KEY_EMAIL));
        return user;
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, FindUserActivity.class);
    }

    private void setupButtons(){
        setupFindByIdButton();
        setupFindByEmailButton();
    }
}
