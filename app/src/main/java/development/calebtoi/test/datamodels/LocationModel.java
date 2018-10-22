package development.calebtoi.test.datamodels;

import android.util.Log;

public class LocationModel {

    private double lat, lng;

    public LocationModel(){
    }

    public LocationModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    /** GETTERS **/
    public double getLat() {
        Log.i("LocationModel", "Getting LAT: " + lat );
        return lat;
    }
    public double getLng() {
        Log.i("LocationModel", "Getting LNG: " + lng );
        return lng;
    }

    /** SETTERS**/
    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
}
