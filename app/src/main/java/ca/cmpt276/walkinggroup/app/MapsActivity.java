package ca.cmpt276.walkinggroup.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.DirectionsParser;
import ca.cmpt276.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroup.dataobjects.GroupList;
import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private enum LocationUpdateState {
        UPLOADING_ENABLED, UPLOADING_DISABLED
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 112;

    private static final float DEFAULT_ZOOM = 15f;
    private static final float DESTINATION_CIRCLE_RADIUS_METERS = 100;
    private static final long LOCATION_UPDATE_RATE_MS = 30000;
    private static final float GROUP_ORIGIN_MARKER_COLOR = BitmapDescriptorFactory.HUE_YELLOW;
    private static final float GROUP_DESTINATION_MARKER_COLOR = BitmapDescriptorFactory.HUE_BLUE;
    private static final int CREATE_GROUP_REQUEST_CODE = 67;

    private int destReachedCount = 0;
    private static final int REWARD_POINTS_FOR_COMPLETE_WALK = 100;
    private Location walkingGroupMeeting;
    private Location walkingGroupDestination;

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean locationPermissionGranted = false;
    private boolean isCurrentGroupSaved = false;
    private ArrayList<LatLng> locationPointsForMarkers;
    private ArrayList<Marker> currentDirectionMarkers;
    private ArrayList<MarkerOptions> currentDirectionMarkerOptions;


    private Polyline currentDirectionPolyline;
    private PolylineOptions currentDirectionPolylineOptions;
    private HashMap<Marker, Long> groupMarkersIdsHashMap;
    private Location lastKnownLocation;

    private GoogleMap mMap;
    private Group group;
    private ArrayList<Location> followsGroupsDestinations;
    //    private ArrayList<Long> followsGroupIds;
    private List<Group> groups;
    private ArrayList<Double> groupLats;
    private ArrayList<Double> groupLngs;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private WGServerProxy proxy;
    private SharedData sharedData = SharedData.getSharedData();
    private GroupList groupList;

    private LocationUpdateState locationUpdateState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        locationPointsForMarkers = new ArrayList<>();
        groupLats = new ArrayList<>();
        groupLngs = new ArrayList<>();
        followsGroupsDestinations = new ArrayList<>();
//        followsGroupIds = new ArrayList<>();
        currentDirectionMarkers = new ArrayList<>();
        currentDirectionMarkerOptions = new ArrayList<>();
        groupMarkersIdsHashMap = new HashMap<>();

        getLocationPermission();
//        getMemberOfGroupsIds();
        proxy = sharedData.getProxy();
        groupList = GroupList.getInstance();

        setupCreateGroupButton();
        setupSaveGroupButton();
        setupLocationSwitch();

        locationUpdateState = LocationUpdateState.UPLOADING_DISABLED;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Get the location permissions from the user if they are not already granted.
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            openMap();
            activateLocationServices();

        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    openMap();
                    activateLocationServices();
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

    //------------------------------------------------------------------------------------------------------------------


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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        setupMarkerClickListenerToJoinGroup();
        if (locationPermissionGranted) {
            getDeviceLocation();

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                enableMapRouteSelection();
                fetchGroupsFromTheServer();
            }
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

                                    moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                                    userLocationUpdate(location);
                                } else {
                                    Log.i("LOCATION: ", "Current Location is null.");
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
        }
    }

    // Called when map is ready.
    // Enable selecting a route and adding a new group by long pressing the map location.
    // Note: this will only be called if the location permissions are granted by the user.
    private void enableMapRouteSelection() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                // Remove both the markers if there are already two markers.
                if (locationPointsForMarkers.size() == 2) {
                    locationPointsForMarkers.clear();
                    groupLngs.clear();
                    groupLats.clear();
                    currentDirectionMarkers.get(0).remove();
                    currentDirectionMarkers.get(1).remove();
                    currentDirectionMarkers.clear();
                    currentDirectionMarkerOptions.clear();
                    currentDirectionPolyline.remove();
                    // Fail check to ensure that the user only goes to manage a group activity if it has saved the group.
                    isCurrentGroupSaved = false;
                }

                // Add the selected latLng in the arrayList.
                // Note: If the arrayList is empty it will go to the first position, else it will go to the second position
                // Also, if the arrayList has two latLngs already it has been cleared in the section of code above.
                locationPointsForMarkers.add(latLng);
                groupLats.add(latLng.latitude);
                groupLngs.add(latLng.longitude);

                // Set the position for the marker.
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                // Add first marker of color red and set the title as Origin.
                // Add second marker of color green and set the title as Destination.
                if (locationPointsForMarkers.size() == 1) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    markerOptions.title("Origin");
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    markerOptions.title("Destination");
                }

                currentDirectionMarkerOptions.add(markerOptions);
                currentDirectionMarkers.add(mMap.addMarker(markerOptions));

                // Request the the URL and draw the polyline between the selected points.
                if (locationPointsForMarkers.size() == 2) {
                    String url = getRequestUrlForPath(locationPointsForMarkers.get(0), locationPointsForMarkers.get(1));
                    RequestDirections requestDirections = new RequestDirections();
                    requestDirections.execute(url);
                }
            }
        });
    }

    //---------------------------------------------------------
    // Provides the url to get the location path between the origin and destination from google maps API.
    private String getRequestUrlForPath(LatLng origin, LatLng destination) {
        // Set the origin and destination strings.
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_destination = "destination=" + destination.latitude + "," + destination.longitude;

        // set other parameters.
        String sensor = "sensor=false";
        String mode = "mode=walking";

        // get full parameters.
        String param = str_origin + "&" + str_destination + "&" + sensor + "&" + mode;

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + param;
        // Sample: https://maps.googleapis.com/maps/api/directions/json?origin=Chicago,IL&destination=Los+Angeles,CA&waypoints=Joplin,MO|Oklahoma+City,OK&key=YOUR_API_KEY

        return url;
    }

    private String getDirectionsFromUrl(String requestUrl) {
        String response = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            // Get the response result.
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            response = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (Exception e) {
            Log.w("exception: ", e.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.w("exception: ", e.toString());
                }
            }
            httpURLConnection.disconnect();
        }
        return response;
    }

    public class RequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            response = getDirectionsFromUrl(strings[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ParseTask parseTask = new ParseTask();
            parseTask.execute(s);
        }
    }

    public class ParseTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> path = null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                path = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                Log.w("Exception: ", e.toString());
            }
            return path;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList points;
            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }
                polylineOptions.geodesic(true);
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.BLUE);
            }

            // Add polyline on the map.
            if (polylineOptions != null) {
                currentDirectionPolylineOptions = polylineOptions;
                currentDirectionPolyline = mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.directions_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }
    // -------------------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------------------
    // Activate location services if the permissions are granted.
    private void activateLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationUpdateCallback();
        startLocationUpdateRequests();
    }

    // Initializing the Location Request instance.
    // LOCATION_UPDATE_RATE_MS: sets the time after which the location is requested.
    private void createLocationRequest() {
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(LOCATION_UPDATE_RATE_MS)
                .setFastestInterval(LOCATION_UPDATE_RATE_MS);
    }

    // Callback when the request to get the current location of the device.
    private void createLocationUpdateCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastKnownLocation = locationResult.getLastLocation();

                if (sharedData.getUser().getWalkWithGroupId() != 0) {
                    if (locationReached(walkingGroupDestination)) {
                        Log.i("dest Count", destReachedCount + "");
                        if (destReachedCount >= 10) {
                            Switch locationUpdateSwitch = findViewById(R.id.maps_activity_location_updates_btn);
                            locationUpdateSwitch.setChecked(false);
                            stopLocationUpdateRequests();
                            locationUpdateState = LocationUpdateState.UPLOADING_DISABLED;
                            Toast.makeText(MapsActivity.this, getString(R.string.destination_reached) + REWARD_POINTS_FOR_COMPLETE_WALK
                                    , Toast.LENGTH_SHORT).show();
                            destReachedCount = 0;
                            walkComplete();
                        }
                    }
                }
                userLocationUpdate(lastKnownLocation);
            }
        };
    }

    private void walkComplete() {
        User user = sharedData.getUser();
        user.addPoints(REWARD_POINTS_FOR_COMPLETE_WALK);
        user.setWalkWithGroupId(0);
        sharedData.setUser(user);
        TextView walkTextView = findViewById(R.id.maps_activity_walk_text);
        walkTextView.setText(getString(R.string.not_walking_with_any_group));

        Call<User> caller = proxy.editUser(user.getId(), user);
        ProxyBuilder.callProxy(MapsActivity.this, caller, returnedUser -> responseToEditUser(returnedUser));
    }

    private void responseToEditUser(User returnedUser) {
        //Do nothing
    }

    // Start the Location updates for the current location of the device.
    private void startLocationUpdateRequests() {
        if (locationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            locationUpdateState = LocationUpdateState.UPLOADING_ENABLED;
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    // Send the GPS location of the user to the User on the server.
    private void userLocationUpdate(Location location) {
        GpsLocation gpsLocation = new GpsLocation(location.getLatitude(), location.getLongitude());
//        gpsLocation.setTimestamp(createTimeStamp());
        Log.i("Location: ", gpsLocation.toString());

        // Make a call to upload the location on server.
        Call<GpsLocation> caller = proxy.setLastGpsLocation(sharedData.getUser().getId(), gpsLocation);
        ProxyBuilder.callProxy(MapsActivity.this, caller, returnedLocation -> response(returnedLocation));
    }

    // Response to server callback when a location is uploaded.
    private void response(GpsLocation returnedLocation) {
        Log.i("Location on server: ", "" + returnedLocation.toString());
    }

    // Stop uploading the user location to the server.
    private void stopLocationUpdateRequests() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Check if the destination of a group is reached.
    // Returns a boolean: true if destination reached and false if not reached.
    private boolean locationReached(Location location) {
        if (groupList.getGroups() != null) {
            if (lastKnownLocation.distanceTo(location) < DESTINATION_CIRCLE_RADIUS_METERS) {
                if (location == walkingGroupDestination) {
                    destReachedCount++;
                } else {
                    destReachedCount = 0;
                }
                return true;
            }
        }
        return false;
    }
    //----------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------
    // Fetch existing groups from the server and show them on the map using markers.
    // Called when the map is ready.
    private void fetchGroupsFromTheServer() {
        Call<List<Group>> caller = proxy.getGroups();
        followsGroupsDestinations.clear();
        ProxyBuilder.callProxy(MapsActivity.this, caller, returnedGroups -> response(returnedGroups));
    }

    // Respond to the returned groups list from the server.
    private void response(List<Group> returnedGroups) {
        groups = returnedGroups;
        groupList.setGroups(returnedGroups);

        if (!returnedGroups.isEmpty()) {
            TextView walkTextView = findViewById(R.id.maps_activity_walk_text);
            if (sharedData.getUser().getWalkWithGroupId() != 0) {
                Group walkingGroup = GroupList.getInstance().getGroupById(sharedData.getUser().getWalkWithGroupId());
                walkTextView.setText(getString(R.string.walking_with_group) + walkingGroup.getGroupDescription());

                walkingGroupDestination = new Location("");
                walkingGroupMeeting = new Location("");
                walkingGroupMeeting.setLatitude(walkingGroup.getRouteLatArray().get(0));
                walkingGroupMeeting.setLongitude(walkingGroup.getRouteLngArray().get(0));
                walkingGroupDestination.setLatitude(walkingGroup.getRouteLatArray().get(1));
                walkingGroupDestination.setLongitude(walkingGroup.getRouteLngArray().get(1));
            } else {
                walkTextView.setText(getString(R.string.not_walking_with_any_group));
            }

            for (Group group : groups) {
                showGroupOnMap(group);
            }
            Log.i("DESTS", followsGroupsDestinations.toString());
        } else {
            Toast.makeText(MapsActivity.this, getString(R.string.no_groups_found) + groups.isEmpty(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showGroupOnMap(Group group) {
        LatLng originLatLng = new LatLng(group.getRouteLatArray().get(0), group.getRouteLngArray().get(0));
        MarkerOptions markerOptionsOrigin = new MarkerOptions();
        markerOptionsOrigin.position(originLatLng);
        markerOptionsOrigin.icon(BitmapDescriptorFactory.defaultMarker(GROUP_ORIGIN_MARKER_COLOR));
        markerOptionsOrigin.title(group.getGroupDescription() + ": Origin");
        markerOptionsOrigin.snippet(getString(R.string.manage_join_group));

        LatLng destLatLng = new LatLng(group.getRouteLatArray().get(1), group.getRouteLngArray().get(1));
        MarkerOptions markerOptionsDest = new MarkerOptions();
        markerOptionsDest.position(destLatLng);
        markerOptionsDest.icon(BitmapDescriptorFactory.defaultMarker(GROUP_DESTINATION_MARKER_COLOR));
        markerOptionsDest.title(group.getGroupDescription() + ": Destination");
        markerOptionsDest.snippet(getString(R.string.manage_join_group));

        Marker originMarker = mMap.addMarker(markerOptionsOrigin);
        Marker destinationMarker = mMap.addMarker(markerOptionsDest);

        groupMarkersIdsHashMap.put(originMarker, group.getGroupId());
        groupMarkersIdsHashMap.put(destinationMarker, group.getGroupId());
    }
    //----------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------
    // Setup the create group button to create a new group.
    // Note: Creates a new group only if the starting point and the Destination are set.
    private void setupCreateGroupButton() {
        Button createGroupBtn = findViewById(R.id.maps_activity_create_btn);
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupLats.size() != 2) {
                    Log.i("LATS: ", groupLats + "");
                    Toast.makeText(MapsActivity.this, getString(R.string.please_select_origin_destination), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = CreateGroupActivity.makeIntent(MapsActivity.this);
                    startActivityForResult(intent, CREATE_GROUP_REQUEST_CODE);
                }
            }
        });
    }

    // Create new Group.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_GROUP_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Button saveBtn = findViewById(R.id.maps_activity_save_btn);
                    saveBtn.setVisibility(View.VISIBLE);
                    saveBtn.setEnabled(true);

                    Group group = SharedData.getSharedData().getGroup();
                    Call<Group> caller = proxy.createGroup(group);
                    ProxyBuilder.callProxy(MapsActivity.this, caller, returnedGroup -> response(returnedGroup));
                }
        }
    }

    private void response(Group returnedGroup) {
        sharedData.setGroup(returnedGroup);
        group = returnedGroup;
        saveGroup();
        showGroupOnMap(group);
    }
    //----------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------
    // Update group path on the server whenever the save group button is pressed.
    private void setupSaveGroupButton() {
        Button saveBtn = findViewById(R.id.maps_activity_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (groupLats.size() != 2) {
                    Log.i("LATS: ", groupLats + "");
                    Toast.makeText(MapsActivity.this, "Please select an Origin and Destination.", Toast.LENGTH_SHORT).show();
                    Log.i("Group: ", group.toString());
                } else if (group == null) {
                    Toast.makeText(MapsActivity.this, "Please create a group first.", Toast.LENGTH_SHORT).show();
                } else {
                    saveGroup();
                }
            }
        });
    }

    // called when the save group button is pressed or when a new group is created to upload the group data to the server.
    private void saveGroup() {
        isCurrentGroupSaved = true;
        group.setRouteLatArray(groupLats);
        group.setRouteLngArray(groupLngs);
        sharedData.setGroup(group);
        groupList.replaceGroup(group);
        groupList.addGroup(group);

        Call<Group> groupCaller = proxy.updateGroup(group.getId(), group);
        ProxyBuilder.callProxy(MapsActivity.this, groupCaller, updatedGroup -> updateGroupResponse(updatedGroup));

        currentDirectionMarkers.get(0).setSnippet("Manage group");
        currentDirectionMarkers.get(1).setSnippet("Manage group");
    }

    private void updateGroupResponse(Group updatedGroup) {
        sharedData.setGroup(updatedGroup);
        group = updatedGroup;
        Toast.makeText(this, "Group saved: " + updatedGroup.toString(), Toast.LENGTH_SHORT).show();
    }
    //----------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------
    private void setupMarkerClickListenerToJoinGroup() {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Long groupId;
                if (groupMarkersIdsHashMap.containsKey(marker)) {
                    groupId = groupMarkersIdsHashMap.get(marker);
                    Intent intent = JoinGroupActivity.makeIntentWithData(MapsActivity.this, groupId);
                    startActivity(intent);
                } else {
                    if (isCurrentGroupSaved) {
                        Intent intent = JoinGroupActivity.makeIntentWithData(MapsActivity.this, group.getId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MapsActivity.this, getString(R.string.save_group_first), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MapsActivity.class);
    }

    private void setupLocationSwitch() {
        Switch locationUpdateSwitch = findViewById(R.id.maps_activity_location_updates_btn);
        locationUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startLocationUpdateRequests();
                    locationUpdateState = LocationUpdateState.UPLOADING_ENABLED;
                } else {
                    stopLocationUpdateRequests();
                    locationUpdateState = LocationUpdateState.UPLOADING_DISABLED;
                }
            }
        });
    }

    private void deleteGroup(long groupId) {
        Call<Void> caller = proxy.deleteGroup(groupId);
        ProxyBuilder.callProxy(MapsActivity.this, caller, returnedNothing -> response(returnedNothing));
    }

    private void response(Void returnedNothing) {
        // Do nothing.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
            fetchGroupsFromTheServer();
        }
    }
}
