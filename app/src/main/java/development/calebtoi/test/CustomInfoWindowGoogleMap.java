package development.calebtoi.test;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_info_window, null);

        TextView name = view.findViewById(R.id.infoWindowName);
        TextView desc = view.findViewById(R.id.infoWindowSnippet);

        name.setText(marker.getTitle());
        desc.setText(marker.getSnippet());

        return view;
    }
}
