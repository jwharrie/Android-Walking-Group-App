package ca.cmpt276.walkinggroup.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.GroupList;
import ca.cmpt276.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class parentMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "ParentMapActivity";

    private final float DEFAULT_ZOOM = 15f;
    private final float ORIGIN_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
    private final float DESTINATION_MARKER_COLOR = BitmapDescriptorFactory.HUE_GREEN;
    private final float LEADER_COLOR = BitmapDescriptorFactory.HUE_MAGENTA;
    private final float CHILD_COLOR = BitmapDescriptorFactory.HUE_BLUE;
    private final long TIMER_TO_REFRESH = 30000;


    private GoogleMap mMap;
    private WGServerProxy proxy;
    private SharedData sharedData;
    private User currentUser;
    private GroupList groupList;

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;

    private ArrayList<Group> groupsOfChildren;
    private ArrayList<User> children;
    private ArrayList<User> leadersOfGroups;
    private List<User> monitoredChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_map);

        sharedData = SharedData.getSharedData();
        currentUser = sharedData.getUser();
        groupList = GroupList.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(parentMapActivity.this);

        getLocationPermission();
        proxy = sharedData.getProxy();

        Log.i("CURRENT", currentUser.toString());
        leadersOfGroups = new ArrayList<>();
        groupsOfChildren = new ArrayList<>();
        children = new ArrayList<>();
        monitoredChildren = new ArrayList<>();

        fetchGroupsFromTheServer();
//        getMonitoredUsers();
//        setupRefreshBtn();
        setupAutoRefresh();
        setupViewUnreadMessagesBtn();
        checkForMessages();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Get the location permissions from the user if they are not already granted.
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            openMap();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    openMap();
                }
            }
        }
    }

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    private void openMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //  Move the camera to current device location.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(parentMapActivity.this));
        }
    }

    // Get Device location when map is ready and move the camera to current location.
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    Log.i("LOCATION: ", location + "");
                                    LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, DEFAULT_ZOOM));
                                } else {
                                    Log.i("LOCATION: ", "Current Location is null.");
                                }
                            }
                        });
            }
        } catch (SecurityException e) {

        }
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Fetch existing groups from the server and show them on the map using markers.
    // Called when the map is ready and on pressing the refresh button.
    private void fetchGroupsFromTheServer() {
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(parentMapActivity.this, caller, returnedGroups -> response(returnedGroups));
    }

    // Respond to the returned groups list from the server.
    private void response(List<Group> returnedGroups) {
        groupList.setGroups(returnedGroups);
        getMonitoredUsers();
    }
    //----------------------------------------------------------------------------------------------------------------------

    private void getMonitoredUsers() {
        Call<List<User>> callToGetChildren = proxy.getMonitorsUsers(currentUser.getId());
        ProxyBuilder.callProxy(parentMapActivity.this, callToGetChildren, returnedChildren -> responseChildren(returnedChildren));
    }

    private void responseChildren(List<User> returnedChildren) {
        if (returnedChildren.isEmpty()) {
            Toast.makeText(this, "No children found", Toast.LENGTH_SHORT).show();
        } else {
            monitoredChildren = returnedChildren;
            for (User child : returnedChildren) {
                List<Group> groupsOfChild = child.getMemberOfGroups();
                for (Group group : groupsOfChild) {
                    getFullGroupInfo(group.getId());
                }
            }
            getFullChildren();
        }
    }

    private void getFullGroupInfo(Long groupId) {
        Group group = groupList.getGroupById(groupId);
        if (!groupsOfChildren.contains(group)) {
            groupsOfChildren.add(group);
            getLeaderFromServer(group.getLeader().getId());
        }
    }

    private void getLeaderFromServer(long leaderId) {
        Call<User> callToGetLeader = proxy.getUserById(leaderId);
        ProxyBuilder.callProxy(parentMapActivity.this, callToGetLeader, returnedLeader -> responseToLeader(returnedLeader));
    }

    private void responseToLeader(User returnedLeader) {
        if (!leadersOfGroups.contains(returnedLeader)) {
            leadersOfGroups.add(returnedLeader);
        }
    }

    private void getFullChildren() {
        for (User child : monitoredChildren) {
            Call<User> callToGetChild = proxy.getUserById(child.getId());
            ProxyBuilder.callProxy(parentMapActivity.this, callToGetChild, returnedChild -> responseToChild(returnedChild));
        }
    }

    private void responseToChild(User returnedChild) {
        children.add(returnedChild);
        updateUI();
    }

    private void updateUI() {
        for (User child : children) {
            GpsLocation childGpsLocation = child.getLastGpsLocation();
            LatLng leaderLatLng = new LatLng(childGpsLocation.getLat(), childGpsLocation.getLng());

            java.sql.Timestamp timestamp = childGpsLocation.getTimestamp();
            String time = getTimeElapsed(timestamp);
            Log.i("timestamp: ", "" + timestamp);

            MarkerOptions leaderMarker = new MarkerOptions()
                    .position(leaderLatLng)
                    .title(child.getName())
                    .snippet("Location last updated: " + time)
                    .icon(BitmapDescriptorFactory.defaultMarker(CHILD_COLOR));

            mMap.addMarker(leaderMarker);
        }

        for (Group group : groupsOfChildren) {
            LatLng origin = new LatLng(group.getRouteLatArray().get(0), group.getRouteLngArray().get(0));
            LatLng destination = new LatLng(group.getRouteLatArray().get(1), group.getRouteLngArray().get(1));

            MarkerOptions originMarker = new MarkerOptions()
                    .position(origin)
                    .title(group.getGroupDescription() + ": Origin")
                    .snippet("")
                    .icon(BitmapDescriptorFactory.defaultMarker(ORIGIN_MARKER_COLOR));

            MarkerOptions destinationMarker = new MarkerOptions()
                    .position(destination)
                    .title(group.getGroupDescription() + ": Destination")
                    .snippet("")
                    .icon(BitmapDescriptorFactory.defaultMarker(DESTINATION_MARKER_COLOR));

            mMap.addMarker(originMarker);
            mMap.addMarker(destinationMarker);
        }

        for (User leader : leadersOfGroups) {
            GpsLocation leaderGpsLocation = leader.getLastGpsLocation();
            LatLng leaderLatLng = new LatLng(leaderGpsLocation.getLat(), leaderGpsLocation.getLng());
            java.sql.Timestamp timestamp = leaderGpsLocation.getTimestamp();

            String elapsedTime = getTimeElapsed(timestamp);
            Log.i("timestamp: ", "" + timestamp);

            List<Group> groupsLeadByLeader = leader.getLeadsGroups();
            String namesOfLeadGroups = "";
            for (Group groupLead : groupsLeadByLeader) {
                Group group = groupList.getGroupById(groupLead.getId());
                namesOfLeadGroups = namesOfLeadGroups + group.getGroupDescription() + ", ";
            }
            MarkerOptions leaderMarker = new MarkerOptions()
                    .position(leaderLatLng)
                    .title(leader.getName())
                    .snippet("Leader of group(s): " + namesOfLeadGroups + "\nLocation last updated: " + elapsedTime)
                    .icon(BitmapDescriptorFactory.defaultMarker(LEADER_COLOR));

            mMap.addMarker(leaderMarker);

        }
    }

    private void checkForMessages() {
        Call<List<Message>> caller = proxy.getUnreadMessages(sharedData.getUser().getId(), true);
        ProxyBuilder.callProxy(parentMapActivity.this, caller, returnedList -> showNumberOfUnreadEmergencyMessages(returnedList));
    }

    private void showNumberOfUnreadEmergencyMessages(List<Message> grabbedUnreadMessages) {
        TextView text = findViewById(R.id.txt_number_emergency_messages2);
        int number = grabbedUnreadMessages.size();
        text.setText("Emergency messages to read: " + number);

        Call<List<Message>> caller = proxy.getUnreadMessages(sharedData.getUser().getId(), false);
        ProxyBuilder.callProxy(parentMapActivity.this, caller, returnedList -> showNumberOfTotalUnreadMessages(returnedList, number));
    }

    private void showNumberOfTotalUnreadMessages(List<Message> grabbedUnreadMessages, int numberEmergency) {
        TextView text = findViewById(R.id.txt_total_number_unread_messages2);
        int total = grabbedUnreadMessages.size() + numberEmergency;
        text.setText("Total number of unread messages: " + total);
    }

    private void setupAutoRefresh(){
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForMessages();
                if (mMap != null) {
                    mMap.clear();
                    Log.i(TAG, "parents dashboard refreshed");
                    fetchGroupsFromTheServer();
                }
                handler.postDelayed(this, TIMER_TO_REFRESH);
            }
        }, TIMER_TO_REFRESH);
    }

    private String getTimeElapsed(java.sql.Timestamp timestamp) {
        String result = "";

        if (timestamp != null) {
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(utilDate.getTime());

            long oldMilliseconds = timestamp.getTime();
            long newMilliseconds = currentTimestamp.getTime();
            long timeElapsedMillis = (newMilliseconds - oldMilliseconds);

            long diffHours = timeElapsedMillis / (60 * 60 * 1000);
            long diffMinutes = ((timeElapsedMillis) / (60 * 1000)) - (diffHours * 60);

            if (diffHours == 0) {
                if (diffMinutes == 0) {
                    result = "Less than a minute ago";
                } else {
                    result = diffMinutes + " minutes ago.";
                }
            } else {
                if (diffHours <= 24) {
                    result = diffHours + ":" + diffMinutes + " hours ago.";
                }
                else{
                    long diffDays = diffHours / 24;
                    result = diffDays + " days ago.";
                }
            }
        }

        return result;
    }

    private void setupViewUnreadMessagesBtn() {
        Button btn = findViewById(R.id.btn_unread_messages2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UnreadMessagesMenuActivity.makeIntent(parentMapActivity.this);
                startActivity(intent);
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, parentMapActivity.class);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
