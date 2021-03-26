package ca.cmpt276.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Store information about a GPS location of a user.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsLocation {
     private double lat;
     private double lng;
     private Timestamp timestamp;

    public GpsLocation(){

    }

    public GpsLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = createTimeStamp();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timeStamp) {
        this.timestamp = timeStamp;
    }

    private Timestamp createTimeStamp() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    @Override
    public String toString() {
        return "lastGpsLocation{\n"
                + lat + "\n"
                + lng + "\n"
                + timestamp;
    }
}
