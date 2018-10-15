package development.calebtoi.test.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import development.calebtoi.test.R;
import development.calebtoi.test.datamodels.HikingRoute;

public class MyRoutesRecyclerAdapter extends RecyclerView.Adapter<MyRoutesRecyclerAdapter.MyRoutesViewHolder> {

    private List<HikingRoute> mRoutes;

    /**
     *  Provides a reference to the type of views that we are using (custom ViewHolder)
     */
    static class MyRoutesViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView title;
        TextView desc;

        private MyRoutesViewHolder(View itemView) {
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
    public MyRoutesRecyclerAdapter(List<HikingRoute> routes) {
        mRoutes = routes;
    }

    public void setRoutes(List<HikingRoute> routes) {
        mRoutes = routes;
        notifyDataSetChanged();
    }

    // Creates new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyRoutesRecyclerAdapter.MyRoutesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_my_routes, parent, false);
        return new MyRoutesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRoutesViewHolder myRoutesViewHolder, int position) {
        HikingRoute route = mRoutes.get(position);
        myRoutesViewHolder.title.setText(route.getName());
        myRoutesViewHolder.desc.setText(route.getDescription());
    }

    @Override
    public int getItemCount() {
        return ((mRoutes != null) && (mRoutes.size() != 0) ? mRoutes.size() : 0);
    }
}
