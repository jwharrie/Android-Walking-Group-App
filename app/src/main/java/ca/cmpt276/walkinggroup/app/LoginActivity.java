package ca.cmpt276.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ca.cmpt276.walkinggroup.dataobjects.User;

public class LoginActivity extends AppCompatActivity {

    private static final String MESSAGE_KEY_PASSWORD
            = "ca.cmpt276.walkinggroup.app.LoginActivity - PASSWORD";
    private static final String MESSAGE_KEY_EMAIL
            = "ca.cmpt276.walkinggroup.app.LoginActivity - EMAIL";

    private String userEmail = "abrach@sfu.ca";
    private String userPassword = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupLoginButton();
    }

    private void setupLoginButton() {
        final Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText email = findViewById(R.id.etxt_email);
                setUserEmail(email.getText().toString());

                EditText password = findViewById(R.id.etxt_password);
                setUserPassword(password.getText().toString());

                Intent intent = new Intent();
                intent.putExtra(MESSAGE_KEY_EMAIL, getUserEmail());
                intent.putExtra(MESSAGE_KEY_PASSWORD, getUserPassword());
                setResult(Activity.RESULT_OK, intent);

                finish();
            }
        });
    }

    private String getUserEmail() {
        return userEmail;
    }

    private void setUserEmail(String userEmail){
        try{
            if (userEmail != null && userEmail.length() > 0) {
                this.userEmail = userEmail;
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Empty Email");
        }
    }

    private String getUserPassword() {
        return userPassword;
    }

    private void setUserPassword(String userPassword) {
        try{
            if (userPassword != null && userPassword.length() > 0) {
                this.userPassword = userPassword;
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Empty Name");
        }
    }

    public static Intent makeIntent(Context context, User user) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(MESSAGE_KEY_EMAIL,user.getEmail());
        intent.putExtra(MESSAGE_KEY_PASSWORD,user.getPassword());
        return intent;
    }

    public static User getUserFromIntent(Intent data){
        String userEmail;
        String userPassword;

        userEmail = data.getStringExtra(MESSAGE_KEY_EMAIL);
        userPassword = data.getStringExtra(MESSAGE_KEY_PASSWORD);

        User theUser = new User();
        theUser.setEmail(userEmail);
        theUser.setPassword(userPassword);
        return theUser;
    }

}
