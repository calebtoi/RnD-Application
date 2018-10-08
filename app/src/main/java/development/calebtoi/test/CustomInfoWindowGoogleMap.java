package development.calebtoi.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
        ImageView img = view.findViewById(R.id.infoWindowImage);

        if(marker.getTag() != null ) {
//            String imgURI = marker.getTag().toString();
//            img.setImageURI(Uri.parse(imgURI));
//            Uri imgURI = Uri.parse(marker.getTag().toString());

            Bitmap bm = (Bitmap) marker.getTag();
            img.setImageBitmap(bm);

            Toast.makeText(view.getContext(), "RECEIVING TAG URI", Toast.LENGTH_LONG).show();




        }


        name.setText(marker.getTitle());
        desc.setText(marker.getSnippet());
;
        return view;
    }
}
