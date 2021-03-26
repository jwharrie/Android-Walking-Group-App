package ca.cmpt276.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that your group can change the format of in (almost) any way you like
 * to encode the rewards that this user has earned.
 *
 * This class gets serialized/deserialized as part of a User object. Server stores it as
 * a JSON string, so it has no direct knowledge of what it contains.
 * (Rewards may not be used during first project iteration or two)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarnedRewards {
    private List<File> possibleBadges = new ArrayList<>();
    private List<Integer> possibleBadgeNumbers = new ArrayList<>();
    private Integer selectedBadgeNumber = 0;

    // Needed for JSON deserialization
    public EarnedRewards() {
    }

    public List<File> getPossibleBadges() {
        return possibleBadges;
    }

    public void setPossibleBadges(List<File> possibleBadges) {
        this.possibleBadges = possibleBadges;
    }

    public List<Integer> getPossibleBadgeNumbers() {
        return possibleBadgeNumbers;
    }

    public void setPossibleBadgeNumbers(List<Integer> possibleBadgeNumbers) {
        this.possibleBadgeNumbers = possibleBadgeNumbers;
    }

    public int getSelectedBadgeNumber() {
        return selectedBadgeNumber;
    }

    public void setSelectedBadgeNumber(int selectedBadgeNumber) {
        this.selectedBadgeNumber = selectedBadgeNumber;
    }

    @Override
    public String toString() {
        return "EarnedRewards{" +
                "possibleBadges=" + possibleBadges +
                "possibleBadgeNumbers=" + possibleBadgeNumbers +
                ", selectedBadgeNumber=" + selectedBadgeNumber +
                '}';
    }
}