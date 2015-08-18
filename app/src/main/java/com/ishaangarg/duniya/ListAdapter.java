package com.ishaangarg.duniya;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ishaan on 8/16/2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private JSONObject[] mDataset;
    //ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, loc, distance, cuisines;
        ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.rest_title);
            loc = (TextView) v.findViewById(R.id.loc);
            distance = (TextView) v.findViewById(R.id.distance);
            cuisines = (TextView) v.findViewById(R.id.cuisines);
            imageView = (ImageView) v.findViewById(R.id.restaurant_hero);
        }
    }

    public ListAdapter(JSONObject[] myDataset) {
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

    int count = 3;

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        try {
            holder.name.setText(mDataset[position].get("OutletName").toString());
            holder.loc.setText(mDataset[position].get("NeighbourhoodName").toString());

            JSONArray categories = mDataset[position].getJSONArray("Categories");

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

            String IMAGE_URL = mDataset[position].get("CoverURL").toString();

            Picasso.with(mContext).load(IMAGE_URL).into(holder.imageView);
            if (position + count < 29) {
                Picasso.with(mContext)
                        .load(mDataset[position + count].get("CoverURL").toString())
                        .fetch();
            }

            ++count;
            if (position + count < 29) {
                Picasso.with(mContext)
                        .load(mDataset[position + count].get("CoverURL").toString())
                        .fetch();
            }
            //holder.imageView.setImageUrl(IMAGE_URL, imageLoader);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
