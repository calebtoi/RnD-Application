package development.calebtoi.test.datamodels;

import android.net.Uri;

public class POIImage {

    public Uri imageUri;
    public String poiID;

    public POIImage(){}

    public POIImage(Uri imURI, String imID){
        this.imageUri = imURI;
        this.poiID = imID;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getPoiID() {
        return poiID;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setPoiID(String poiID) {
        this.poiID = poiID;
    }
}
