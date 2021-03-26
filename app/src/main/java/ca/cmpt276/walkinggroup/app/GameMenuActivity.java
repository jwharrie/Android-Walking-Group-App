package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        setupLeaderBoardButton();

        setupViewRewardsButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, GameMenuActivity.class);
    }

    private void setupViewRewardsButton() {
        Button btn = findViewById(R.id.btn_viewRewards);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameMenuActivity.this, RewardsActivity.class);
                startActivity(intent);

            }
        });
    }

    private void setupLeaderBoardButton() {
        Button leaderBoardBtn = findViewById(R.id.btn_leaderboard);
        leaderBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = LeaderboardActivity.makeIntent(GameMenuActivity.this);
                startActivity(intent);
            }
        });
    }
}
