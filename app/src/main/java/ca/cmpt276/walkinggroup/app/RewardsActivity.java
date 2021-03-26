package ca.cmpt276.walkinggroup.app;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class RewardsActivity extends AppCompatActivity {

    private static final int BUTTON_CONSTANT = 100;
    private static final int TEXT_CONSTANT = 1000;

    private SharedData sharedData = SharedData.getSharedData();
    private Integer points;
    private EarnedRewards rewards;

    private TypedArray unlockedBadges;
    private TypedArray lockedBadges;

    private int badgeCount;
    private int[] badgeCosts;
    private String[] badgeNames;
    private Status[] badgeStatus;

    private enum Status {
        LOCKED, UNLOCKED, ACTIVE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        User user = sharedData.getUser();
        points = user.getTotalPointsEarned();
        rewards = user.getRewards();

        setupComponents();
    }

    // Generate a menu from the values stored in res/values/rewards.xml.
    private void setupRewardsMenu() {
        TableLayout table = findViewById(R.id.rewardList);
        for (int i = 0; i < badgeCount; i++) {
            TableRow row = new TableRow(this);

            Button btn = new Button(this);
            btn.setId(i + BUTTON_CONSTANT);
            int badgeNumber = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBadge(badgeNumber);
                }
            });
            row.addView(btn);

            TextView txt = new TextView(this);
            txt.setId(i + TEXT_CONSTANT);
            row.addView(txt);

            table.addView(row);
        }
        updateRows();
    }

    private void setBadge(int number) {
        if (rewards.getPossibleBadgeNumbers().contains(number)) {
            rewards.setSelectedBadgeNumber(number);
        }
        updateBadgeList();
        updateRows();
    }

    private void updateRows() {
        for (int i = 0; i < badgeCount; i++) {
            Button btn = findViewById(i + BUTTON_CONSTANT);
            TextView txt = findViewById(i + TEXT_CONSTANT);
            String menuText = badgeNames[i] + "\n";
            switch (badgeStatus[i]) {
                case LOCKED:
                    btn.setBackground(lockedBadges.getDrawable(i));
                    btn.setEnabled(false);
                    menuText = menuText + getString(R.string.avatar_menu_locked, badgeCosts[i]);
                    break;
                case UNLOCKED:
                    btn.setBackground(unlockedBadges.getDrawable(i));
                    btn.setEnabled(true);
                    menuText = menuText + getString(R.string.avatar_menu_unlocked);
                    break;
                case ACTIVE:
                    btn.setBackground(unlockedBadges.getDrawable(i));
                    btn.setEnabled(false);
                    menuText = menuText + getString(R.string.avatar_menu_active);
                    break;
            }
            txt.setText(menuText);
        }
    }

    private void setupBackButton() {
        Button btn = findViewById(R.id.btn_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void updateBadgeList() {
        List<Integer> possibleBadges = new ArrayList<>();
        for (int i = 0; i < badgeCount; i++) {
            if (points < badgeCosts[i]) {
                badgeStatus[i] = Status.LOCKED;
            } else {
                possibleBadges.add(i);
                if (rewards.getSelectedBadgeNumber() == i) {
                    badgeStatus[i] = Status.ACTIVE;
                } else {
                    badgeStatus[i] = Status.UNLOCKED;
                }
            }
        }
        rewards.setPossibleBadgeNumbers(possibleBadges);
    }

    private void setupBadgeList() {
        Resources res = getResources();
        unlockedBadges = res.obtainTypedArray(R.array.unlocked_badges);
        lockedBadges = res.obtainTypedArray(R.array.locked_badges);
        badgeCosts = res.getIntArray(R.array.badge_costs);
        badgeNames = res.getStringArray(R.array.badge_names);
        badgeCount = badgeCosts.length;
        badgeStatus = new Status[badgeCount];
        updateBadgeList();
    }

    private void setupComponents() {
        TextView hdr = findViewById(R.id.rewardHeader);
        hdr.setText(getString(R.string.txt_viewRewards, points));

        setupBadgeList();
        setupRewardsMenu();
        setupBackButton();
    }
}
