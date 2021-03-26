package ca.cmpt276.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Store information about the walking groups.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase{
    private String groupDescription;
    private List<Double> routeLatArray;
    private List<Double> routeLngArray;
    private User leader;
    private List<User> memberUsers;

    public Group(){
        memberUsers = new ArrayList<>();
    }

    public void setGroupId(long id) {
        setId(id);
    }

    public void setGroupDescription(String description) {
        this.groupDescription = description;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public void setMemberUsers(List<User> members) {
        this.memberUsers = members;
    }

    public void addMember(User user){
        memberUsers.add(user);
    }

    public void removeMemberById(long userId){
        for (User member: memberUsers){
            if (member.getId() == userId){
                memberUsers.remove(member);
                return;
            }
        }
    }

    public void setRouteLatArray(List<Double> routeLat) {
        this.routeLatArray = routeLat;
    }

    public void setRouteLngArray(List<Double> routeLng) {
        this.routeLngArray = routeLng;
    }

    @JsonIgnore
    public long getGroupId() {
        return getId();
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public User getLeader() {
        return leader;
    }

    @JsonIgnore
    public Boolean containsMember(long userId){
        for (User user: memberUsers){
            if (user.getId() == userId){
                return true;
            }
        }

        return false;
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    public List<Double> getRouteLatArray() {
        return routeLatArray;
    }

    public List<Double> getRouteLngArray() {
        return routeLngArray;
    }

    @Override
    public String toString(){
        return "Group{" +
                "id=" + getId() +
                ", name='" + groupDescription + '\'' +
                ", leader=" + leader +
                ", href='" + getHref() + '\'' +
                ", members='" + memberUsers + '\'' +

                '}';
    }
}
