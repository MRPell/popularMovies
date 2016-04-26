package com.example.android.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by MRPell on 4/19/2016.
 * Enhances array adapter to handle gridview and images using Picasso
 */
public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private ArrayList<String> imagePathArray;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param gridViewResourceId The resource ID for a layout file containing a gridView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the gridview.
     */

    public ImageAdapter(Context context, int gridViewResourceId,  ArrayList<String> objects) {
        super(context, gridViewResourceId, objects);
        imagePathArray = objects;
        mContext = context;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }
        //load image into image view with picasso
        Picasso.with(mContext).load(imagePathArray.get(position)).into(imageView);
        return imageView;
    }
}