package development.calebtoi.test.datamodels;

import java.util.List;

public class HikingRoute {
    private String name;
    private String description;
    private float difficulty;
    public String userID;
    public List<LocationModel> route;
    public List<POIModel> poi;


    public HikingRoute() {
    }

    // With POI
    public HikingRoute(String name, String description, float difficulty, String uID, List<LocationModel> route, List<POIModel> poi){
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.userID = uID;
        this.route = route;
        this.poi = poi;
    }

    // Without POI
    public HikingRoute(String name, String description, float difficulty, String uID, List<LocationModel> route){
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.userID = uID;
        this.route = route;
    }

    /** GETTERS **/
    public String getName() {
        return name;
    }
    public List<LocationModel> getRoute() {
        return route;
    }
    public List<POIModel> getPoi() {
        return poi;
    }
    public String getDescription() {
        return description;
    }

    /** SETTERS **/
    public void setName(String name) {
        this.name = name;
    }
    public void setRoute(List<LocationModel> loc){
        this.route = loc;
    }
    public void setPoi(List<POIModel> poi) {
        this.poi = poi;
    }
}
