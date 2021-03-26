package ca.cmpt276.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Demonstrates how to place calls to the server via the proxy.
 * Basic design:
 * - When you press the button, it executes the call. The 'response()' function then
 * gets the callback when the call is done.
 * - Errors get logged and have a toast. Real applications should do something more with the errors.
 * (Likely need to change the code in the ProxyBuilder class.
 * - When logging in, it calls back the onReceiveToken() function which we must then
 * create a new proxy (via ProxyBuider) and let the old one be destroyed because we must
 * set the token when its created.
 * <p>
 * Some changes may be required to your 'build.gradle (Module: app)" file
 * Compare the gradle file of this application and yours.
 */
public class StartupMenuActivity extends AppCompatActivity {
    private static final String TAG = "ServerTest";

    private String userEmail;
    private String userToken;
    private User userInfo;

    private static final int REQUEST_CODE_ADD = 666;
    private static final int REQUEST_CODE_LOGIN = 999;

    private static final String USER_EMAIL_STRING_KEY = "userEmail";
    private static final String USER_TOKEN_STRING_KEY = "userToken";

    private SharedData sharedData = SharedData.getSharedData();

    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_menu);

        grabLoggedInUserData();

        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), userToken);
        sharedData.setProxy(proxy);

        Log.i("message", "Grabbed email: " + userEmail + ". Grabbed token: " + userToken);
        sharedData.setToken(userToken);

        ifUserIsLoggedIn();
        setupButtons();
    }


    private void ifUserIsLoggedIn() {
        if (userToken != null) {
            StartupMenuActivity.saveLoggedInUserData(StartupMenuActivity.this, userEmail, userToken);
            sharedData.setProxy(ProxyBuilder.getProxy(getString(R.string.apikey), userToken));
            Log.i("message", "Saved e¬mail: " + userEmail + ". Saved token:" + userToken);
            setUser();
            Intent intent = MainMenuActivity.makeIntent(StartupMenuActivity.this);
            startActivity(intent);
        }
    }

    private void setUser() {
        Call<User> caller = proxy.getUserByEmail(userEmail);
        ProxyBuilder.callProxy(StartupMenuActivity.this, caller, returnedUser -> setUserResponse(returnedUser));
    }

    private void setUserResponse(User returnedUser) {
        sharedData.setUser(returnedUser);
        Log.i("message", "Set user name: " + sharedData.getUser().getName() + ". Set user email: " + sharedData.getUser().getEmail());
    }

    private void setupNewUserButton() {
        Button btn = findViewById(R.id.btnNewUser);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();
                Intent intent = NewUserActivity.makeIntent(StartupMenuActivity.this, user);
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });
    }

    // Login to get a Token
    private void setupLoginButton() {
        Button btn = findViewById(R.id.btnLogin);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();
                Intent intent = LoginActivity.makeIntent(StartupMenuActivity.this, user);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD:
                if (resultCode == Activity.RESULT_OK) {
                    //Get the msg
                    userInfo = NewUserActivity.getUserFromIntent(data);
                    Log.i(TAG, userInfo.toString() + "\n" + userInfo.getPassword());

                    // Make call
                    Call<User> caller = proxy.createUser(userInfo);
                    ProxyBuilder.callProxy(StartupMenuActivity.this, caller, returnedUser -> response(returnedUser));

                } else {
                    Log.i("MyApp", "Create New User Cancelled");
                }
                break;

            case REQUEST_CODE_LOGIN:
                if (resultCode == Activity.RESULT_OK) {
                    //Get the msg
                    User userInfo = LoginActivity.getUserFromIntent(data);

                    // Make call
                    Call<Void> caller = proxy.login(userInfo);
                    ProxyBuilder.callProxy(StartupMenuActivity.this, caller, returnedNothing -> response(returnedNothing));

                    // Register for token received:
                    ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token, userInfo));

                } else {
                    Log.i("MyApp", "Login Cancelled");
                }
                break;
        }
    }

    private void response(User user) {
        notifyUserViaLogAndToast("User " + user.getName() + " (" + user.getEmail() + ") " + "has been created.");

        proxy = ProxyBuilder.getProxy(getString(R.string.apikey));
        Call<Void> caller = proxy.login(userInfo);
        ProxyBuilder.callProxy(StartupMenuActivity.this, caller, returnedNothing -> response(returnedNothing));

        // Register for token received:
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token, userInfo));
        ifUserIsLoggedIn();
    }

    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token, User user) {
        // Replace the current proxy with one that uses the token!
        userToken = token;
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + userToken);
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), userToken);
        Log.i("message", "Connected to server");
        sharedData.setToken(userToken);
        sharedData.setProxy(proxy);
        sharedData.setUser(user);
        userEmail = user.getEmail();
        Log.i("message", "User email is now: " + userEmail);

        ifUserIsLoggedIn();
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void grabLoggedInUserData() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString(USER_EMAIL_STRING_KEY, null);
        userToken = prefs.getString(USER_TOKEN_STRING_KEY, null);
    }

    public static void saveLoggedInUserData(Context context, String email, String token) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_EMAIL_STRING_KEY, email);
        editor.putString(USER_TOKEN_STRING_KEY, token);
        editor.apply();
    }

    private void setupButtons() {
        setupNewUserButton();
        setupLoginButton();
    }
}
