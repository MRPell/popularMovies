package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by MRPell on 4/19/2016.
 * Enhances array adapter to handle gridview and images using Picasso
 */
public class ImageAdapter extends CursorAdapter {
    private Context mContext;
    private String imageBasePath = "http://image.tmdb.org/t/p/";
    private String imageSize = "w185/";
    private String imagePath = imageBasePath + imageSize;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final ImageView gridView;
        public final TextView releaseDate;
        public final TextView synopsisView;
        public final TextView userRatingView;
        public final TextView movieTitleView;


        public ViewHolder(View view) {
            gridView = (ImageView) view.findViewById(R.id.grid_item_movie_imageview);
            iconView = (ImageView) view.findViewById(R.id.detail_poster_image);
            releaseDate = (TextView) view.findViewById(R.id.detail_release_date_text);
            synopsisView = (TextView) view.findViewById(R.id.detail_synopsis_text);
            userRatingView = (TextView) view.findViewById(R.id.detail_rating_text);
            movieTitleView = (TextView) view.findViewById(R.id.detail_title_text);
        }
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param c       The resource ID for a layout file containing a gridView to use when
     *                instantiating views.
     * @param flags   The objects to represent in the gridview.
     */

    public ImageAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int layoutId = R.layout.movie_image_grid_view;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Get movie poster icon
        Picasso.with(context).load(imagePath +
                cursor.getString(MainActivityFragment.COL_MOVIE_POSTER)).into(viewHolder.gridView);

//        iconView = (ImageView) view.findViewById(R.id.detail_poster_image);
//        synopsisView = (TextView) view.findViewById(R.id.detail_synopsis_text);
//        userRatingView = (TextView) view.findViewById(R.id.detail_rating_text);
//        movieTitleView = (TextView) view.findViewById(R.id.detail_title_text);
        // Read synopsis from cursor
//        String synopsis = cursor.getString(MainActivityFragment.COL_MOVIE_SYNOPIS);
//        // Find TextView and set formatted date on it
//        viewHolder.synopsisView.setText(synopsis);
//
//        // Read weather forecast from cursor
//        String title = cursor.getString(MainActivityFragment.COL_MOVIE_TITLE);
//        // Find TextView and set weather forecast on it
//        viewHolder.movieTitleView.setText(title);
//
//        // For accessibility, add a content description to the icon field
//        viewHolder.iconView.setContentDescription(title);
//
//        // Read high temperature from cursor
//        String releaseDate = cursor.getString(MainActivityFragment.COL_MOVIE_RELEASE_DATE);
//        viewHolder.releaseDate.setText(releaseDate);
//
//        // Read low temperature from cursor
//        double userRating = cursor.getDouble(MainActivityFragment.COL_MOVIE_USER_RATING);
//        viewHolder.userRatingView.setText(Double.toString(userRating));
//
//        String poster = cursor.getString(MainActivityFragment.COL_MOVIE_POSTER);
//        Picasso.with(mContext).load(poster).into(viewHolder.iconView);


    }


//    // create a new ImageView for each item referenced by the Adapter
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView;
//        if (convertView == null) {
//            // if it's not recycled, initialize some attributes
//            imageView = new ImageView(mContext);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            imageView.setAdjustViewBounds(true);
//        } else {
//            imageView = (ImageView) convertView;
//        }
//        //load image into image view with picasso
//        Picasso.with(mContext).load(imagePathArray.get(position)).into(imageView);
//        return imageView;
//    }
}