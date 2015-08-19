package com.ishaangarg.duniya;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ishaan on 8/16/2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private ArrayList<JSONObject> mDataset;
    //ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, loc, distance, cuisines, numCoupons;
        ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.rest_title);
            loc = (TextView) v.findViewById(R.id.loc);
            distance = (TextView) v.findViewById(R.id.distance);
            cuisines = (TextView) v.findViewById(R.id.cuisines);
            numCoupons = (TextView) v.findViewById(R.id.coupons);
            imageView = (ImageView) v.findViewById(R.id.restaurant_hero);
        }
    }

    public ListAdapter(ArrayList<JSONObject> myDataset) {
        mDataset = myDataset;
    }

    Context mContext;

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        mContext = parent.getContext();
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        try {
            holder.name.setText(mDataset.get(position).get("OutletName").toString());
            holder.loc.setText(mDataset.get(position).get("NeighbourhoodName").toString());
            String coupons = mDataset.get(position).get("NumCoupons").toString();

            holder.numCoupons.setText(coupons.equals("1")?coupons+" Offer" : coupons+" Offers");

            String distance = mDataset.get(position).get("DistText").toString();
            holder.distance.setText(distance);

            JSONArray categories = mDataset.get(position).getJSONArray("Categories");

            String cuisineString = "";

            for (int i = 0; i < categories.length(); i++) {
                JSONObject object = categories.getJSONObject(i);
                String cuisine = object.get("CategoryType").toString();

                if (cuisineString.equals("") && cuisine.equals("Cuisine")){
                    cuisineString = object.get("Name").toString();
                }
                else if (cuisine.equals("Cuisine")) {
                    cuisineString = cuisineString + ", " + object.get("Name").toString();
                }
            }
            holder.cuisines.setText(cuisineString);

            String IMAGE_URL = mDataset.get(position).get("CoverURL").toString();

            Picasso.with(mContext).load(IMAGE_URL).into(holder.imageView);
            if (position + 3 < 29) {
                Picasso.with(mContext)
                        .load(mDataset.get(position+3).get("CoverURL").toString())
                        .fetch();
            }
            //holder.imageView.setImageUrl(IMAGE_URL, imageLoader);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
