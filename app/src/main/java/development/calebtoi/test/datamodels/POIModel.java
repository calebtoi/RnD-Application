package development.calebtoi.test.datamodels;

public class POIModel {

    public String title;
    public String description;
    public LocationModel location;

    // TODO: add photo saving


    public POIModel(){}

    public POIModel(String title, LocationModel loc) {
        this.title = title;
        this.location = loc;
    }



}
