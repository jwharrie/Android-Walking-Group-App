package ca.cmpt276.walkinggroup.dataobjects;

import java.util.List;

public class GroupList {


    List<Group> groups;

    private static GroupList instance;

    private GroupList() {
        groups = null;
    }

    public static GroupList getInstance() {
        if (instance == null) {
            instance = new GroupList();
        }
        return instance;

    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Group getGroupById(long groupId){
        for(Group group: groups){
            if(group.getGroupId() == groupId){
                return group;
            }
        }
        return new Group();
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void replaceGroup(Group newGroup){
        long groupId = newGroup.getId();
        for(Group group: groups){
            if(group.getGroupId() == groupId){
                groups.remove(group);
                groups.add(newGroup);
                return;
            }
        }
        groups.add(newGroup);
    }
}
