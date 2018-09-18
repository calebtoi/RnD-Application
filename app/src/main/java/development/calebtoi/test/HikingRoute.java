package development.calebtoi.test;

import android.location.Location;

import com.google.android.gms.maps.model.Marker;

public class HikingRoute {
    private String name;
    private Location route[];
    private Marker pointsOfInterests[];

    public HikingRoute(String name, Location route[], Marker pointsOfInterests[]){
        this.name = name;
        this.route = route;
        this.pointsOfInterests = pointsOfInterests;
    }

    public String getName() {
        return name;
    }

    public Location[] getRoute() {
        return route;
    }

    public Marker[] getPointsOfInterests() {
        return pointsOfInterests;
    }
}
