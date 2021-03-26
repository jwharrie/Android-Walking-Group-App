package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class LeaderboardActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User currentUser;

    private List<User> usersByRank;
    private List<String> usersInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        proxy = SharedData.getSharedData().getProxy();
        currentUser = SharedData.getSharedData().getUser();

        usersByRank = new ArrayList<>();
        usersInfo = new ArrayList<>();

        getUsersFromServer();
    }

    private void getUsersFromServer() {
        Call<List<User>> caller = proxy.getUsers();
        ProxyBuilder.callProxy(LeaderboardActivity.this, caller, returnedUsers -> responseToUsers(returnedUsers));
    }

    private void responseToUsers(List<User> returnedUsers){
        usersByRank = returnedUsers;
        Collections.sort(usersByRank);
        Collections.reverse(usersByRank);
        if(usersByRank.size() > 100){
            usersByRank.subList(0, 99);
        }

        populateList();
    }

    private void populateList() {
        ListView leaderList = findViewById(R.id.leaderboard_ListView);
        leaderList.setVisibility(View.VISIBLE);
        LeaderboardAdapter adapter = new LeaderboardAdapter(LeaderboardActivity.this, R.layout.leaderboard_list_view, usersByRank);
        leaderList.setAdapter(adapter);

    }

    public static Intent makeIntent(Context context){
        return new Intent(context, LeaderboardActivity.class);
    }
}
