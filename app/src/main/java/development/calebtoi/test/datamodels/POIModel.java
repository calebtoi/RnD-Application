package development.calebtoi.test.datamodels;

import java.util.UUID;

public class POIModel {

    private String poiID;
    public String title;
    public String description;
    private LocationModel location;

    // TODO: add photo saving


    public POIModel(){}

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
    public LocationModel getLocation() {
        return location;
    }
}
