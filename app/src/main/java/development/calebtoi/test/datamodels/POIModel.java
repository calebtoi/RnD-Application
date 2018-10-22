package development.calebtoi.test.datamodels;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class POIModel {

    private String poiID;
    public String title;
    public String description;
    private LocationModel location;

    public POIModel(){
    }

    public POIModel(String title, String desc, LocationModel loc) {
        this.poiID = UUID.randomUUID().toString();
        this.title = title;
        this.description = desc;
        this.location = loc;
    }

    // Getters
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getPoiID() {
        return poiID;
    }
    public LatLng getLocation() {
        LatLng temp = new LatLng(location.getLat(), location.getLng());

        return temp;
    }
}
