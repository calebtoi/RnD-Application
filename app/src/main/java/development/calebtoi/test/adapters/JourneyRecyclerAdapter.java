package development.calebtoi.test.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import development.calebtoi.test.R;
import development.calebtoi.test.datamodels.HikingRoute;

public class JourneyRecyclerAdapter extends RecyclerView.Adapter<JourneyRecyclerAdapter.JourneyViewHolder> {

    private List<HikingRoute> mRoutes;

    /**
     *  Provides a reference to the type of views that we are using (custom ViewHolder)
     */
    static class JourneyViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView title;
        TextView desc;

        private JourneyViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.card_my_route_img);
            title = itemView.findViewById(R.id.card_my_route_title);
            desc = itemView.findViewById(R.id.card_my_route_description);
        }

    }

    /**
     *  Initialises the DataSet of the Adapter
     *
     *  @param routes contains the data to populate tthe views to be used by the RecyclerView
     */
    public JourneyRecyclerAdapter(List<HikingRoute> routes) {
        mRoutes = routes;
    }

    public void setRoutes(List<HikingRoute> routes) {
        mRoutes = routes;
        notifyDataSetChanged();
    }

    // Creates new views (invoked by the layout manager)
    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_my_routes, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JourneyViewHolder journeyViewHolder, int position) {
        HikingRoute route = mRoutes.get(position);

        StorageReference imageRef = FirebaseStorage.getInstance().getReference("map_images/"+route.getRouteID());
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                journeyViewHolder.img.setImageBitmap(bmp);
            }
        });
        journeyViewHolder.title.setText(route.getName());
        journeyViewHolder.desc.setText(route.getDescription());
    }

    @Override
    public int getItemCount() {
        return ((mRoutes != null) && (mRoutes.size() != 0) ? mRoutes.size() : 0);
    }
}
