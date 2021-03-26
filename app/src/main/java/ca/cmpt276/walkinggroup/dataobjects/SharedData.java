package ca.cmpt276.walkinggroup.dataobjects;

import java.util.List;

import ca.cmpt276.walkinggroup.proxy.WGServerProxy;

public class SharedData {

    private String token;
    private User user;
    private Group group;
    WGServerProxy proxy;
    String apiKey = "0E982A72-0AF4-440C-8D27-B1F37FA32E145";

    private List<User> monitoredUsers;

    static SharedData instance;

    private SharedData(){}

    public static SharedData getSharedData(){
        if (instance == null){
            instance = new SharedData();
        }
        return instance;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getToken(){
        return token;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<User> getMonitoredUsers() {
        return monitoredUsers;
    }

    public void setMonitoredUsers(List<User> monitoredUsers) {
        this.monitoredUsers = monitoredUsers;
    }

    public WGServerProxy getProxy() {
        return proxy;
    }

    public void setProxy(WGServerProxy proxy) {
        this.proxy = proxy;
    }
}
