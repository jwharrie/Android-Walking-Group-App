package ca.cmpt276.walkinggroup.dataobjects;


import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * User class to store the data the server expects and returns.
 * (Incomplete: Needs support for monitoring and groups).
 */

// All model classes coming from server must have this next line.
// It ensures deserialization does not fail if server sends you some fields you are not expecting.
// This is needed for the server to be able to change without breaking your app!
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends IdItemBase implements Comparable<User> {
    // ---------------------------------------
    //    Fields
    // ---------------------------------------
    private String name;
    private String email;
    private Integer birthYear;
    private Integer birthMonth;
    private String address;
    private String cellPhone;
    private String homePhone;
    private String grade;
    private String teacherName;
    private String emergencyContactInfo;
    private String password;
    private Long walkWithGroupId = 0L;

    // Monitoring
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private List<User> monitoredByUsers;
    private List<User> monitorsUsers;

    // Group Membership / Leading
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private List<Group> memberOfGroups;
    private List<Group> leadsGroups;

    // GPS Location
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private GpsLocation lastGpsLocation;

    // Messages
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private List<Message> messages;

    // Gamification Support
    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private Integer currentPoints = 0;
    private Integer totalPointsEarned = 0;
    // rewards will be serialized to be the customJson
    private EarnedRewards rewards;

    // Permissions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private List<PermissionRequest> pendingPermissionRequests;

    // ----------------------------------------------
    // Setters / Getters
    // ----------------------------------------------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getEmergencyContactInfo() {
        return emergencyContactInfo;
    }

    public void setEmergencyContactInfo(String emergencyContactInfo) {
        this.emergencyContactInfo = emergencyContactInfo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<User> getMonitoredByUsers() {
        return monitoredByUsers;
    }

    public void setMonitoredByUsers(List<User> monitoredByUsers) {
        this.monitoredByUsers = monitoredByUsers;
    }

    public List<User> getMonitorsUsers() {
        return monitorsUsers;
    }

    public void setMonitorsUsers(List<User> monitorsUsers) {
        this.monitorsUsers = monitorsUsers;
    }

    public List<Group> getMemberOfGroups() {
        return memberOfGroups;
    }

    public void setMemberOfGroups(List<Group> memberOfGroups) {
        this.memberOfGroups = memberOfGroups;
    }

    public void addUserToGroup(Group group){
        memberOfGroups.add(group);
    }

    public void removeUserFromGroup(long groupId){
        for (Group group: memberOfGroups){
            if(groupId == group.getId()){
                memberOfGroups.remove(group);
                return;
            }
        }
    }



    public List<Group> getLeadsGroups() {
        return leadsGroups;
    }

    public void setLeadsGroups(List<Group> leadsGroups) {
        this.leadsGroups = leadsGroups;
    }
    public void addLeadsGroups(Group group) {
        this.leadsGroups.add(group);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Integer getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(Integer currentPoints) {
        this.currentPoints = currentPoints;
    }

    public Integer getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void addPoints(int points){
        totalPointsEarned += points;
        currentPoints += points;
    }

    public void setTotalPointsEarned(Integer totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public GpsLocation getLastGpsLocation() {
        return lastGpsLocation;
    }

    public void setLastGpsLocation(GpsLocation gpsLocation) {
        this.lastGpsLocation = gpsLocation;
    }


    // Setter will be called when deserializing User's JSON object; we'll automatically
    // expand it into the custom object.
    public void setCustomJson(String jsonString) {
        if (jsonString == null || jsonString.length() == 0) {
            rewards = null;
            Log.w("USER", "De-serializing string is null for User's custom Json rewards; ignoring.");
        } else {
            Log.w("USER", "De-serializing string: " + jsonString);
            try {
                rewards = new ObjectMapper().readValue(
                        jsonString,
                        EarnedRewards.class);
                Log.w("USER", "De-serialized embedded rewards object: " + rewards);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Having a getter will make this function be called to set the value of the
    // customJson field of the JSON data being sent to server.
    public String getCustomJson() {
        // Convert custom object to a JSON string:
        String customAsJson = null;
        try {
            customAsJson = new ObjectMapper().writeValueAsString(rewards);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return customAsJson;
    }

    public EarnedRewards getRewards() {
        return rewards;
    }
    public void setRewards(EarnedRewards rewards) {
        this.rewards = rewards;
    }



    public List<PermissionRequest> getPendingPermissionRequests() {
        return pendingPermissionRequests;
    }

    public void setPendingPermissionRequests(List<PermissionRequest> pendingPermissionRequests) {
        this.pendingPermissionRequests = pendingPermissionRequests;
    }

    @JsonIgnore
    public long getWalkWithGroupId() {
        return walkWithGroupId;
    }

    @JsonProperty
    public void setWalkWithGroupId(long walkWithGroupId) {
        this.walkWithGroupId = walkWithGroupId;
    }

    @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", birthYear=" + birthYear +
                    ", birthMonth=" + birthMonth +
                    ", address='" + address + '\'' +
                    ", cellPhone='" + cellPhone + '\'' +
                    ", homePhone='" + homePhone + '\'' +
                    ", grade='" + grade + '\'' +
                    ", teacherName='" + teacherName + '\'' +
                    ", emergencyContactInfo='" + emergencyContactInfo + '\'' +
                    ", password='" + password + '\'' +
                    ", monitoredByUsers=" + monitoredByUsers +
                    ", monitorsUsers=" + monitorsUsers +
                    ", memberOfGroups=" + memberOfGroups +
                    ", leadsGroups=" + leadsGroups +
                    ", lastGpsLocation=" + lastGpsLocation +
                    ", messages=" + messages +
                    ", currentPoints=" + currentPoints +
                    ", totalPointsEarned=" + totalPointsEarned +
                    ", rewards=" + rewards +
                    ", pendingPermissionRequests=" + pendingPermissionRequests +
                    ", id=" + id +
                    ", hasFullData=" + hasFullData +
                    ", href='" + href + '\'' +
                    '}';
        }

    @Override
    public int compareTo(@NonNull User other) {
        int rank = Integer.compare(totalPointsEarned, other.getTotalPointsEarned());
        if (rank != 0) {
            return rank;
        }
        else {
            return name.compareTo(other.getName());
        }
    }
}
