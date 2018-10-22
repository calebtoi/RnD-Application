package development.calebtoi.test.datamodels;

import android.util.Log;

import java.util.List;

public class HikingRoute {
    private String routeID;
    private String name;
    private String description;
    private float difficulty;
    public String userID;
    public List<LocationModel> route;
    private List<POIModel> poi;


    public HikingRoute() {
    }

    // With POI
    public HikingRoute(String rID, String name, String description, float difficulty, String uID, List<LocationModel> route, List<POIModel> poi){
        this.routeID = rID;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.userID = uID;
        this.route = route;
        this.poi = poi;
    }

    // Without POI
    public HikingRoute(String rID, String name, String description, float difficulty, String uID, List<LocationModel> route){
        this.routeID = rID;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.userID = uID;
        this.route = route;
    }

    /** GETTERS **/
    public String getRouteID() {
        return routeID;
    }
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
    public String getUserID() {
        return userID;
    }

    /** SETTERS **/
    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }
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
