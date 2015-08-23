package jarrar.com.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by TOSHIBA on 11/07/2015.
 */
public class MoviesAdapter extends ArrayAdapter<Movie> {

    Context mContext;

    public MoviesAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.grid_image);
        Picasso.with(mContext).load(mContext.getString(R.string.BASE_URL_POSTER) + movie.poster_Location)

                .into(imageView);

        return convertView;
    }
}
