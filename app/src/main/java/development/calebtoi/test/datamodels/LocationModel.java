package development.calebtoi.test.datamodels;

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
        return lat;
    }
    public double getLng() {
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
