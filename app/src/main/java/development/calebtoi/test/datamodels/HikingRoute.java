package development.calebtoi.test.datamodels;

import java.util.List;

public class HikingRoute {
    private String name;
    public String userID;
    public List<LocationModel> route;
    public List<POIModel> poi;


    public HikingRoute() {
    }

    public HikingRoute(String name, String uID, List<LocationModel> route, List<POIModel> poi){
        this.name = name;
        this.userID = uID;
        this.route = route;
        this.poi = poi;
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
