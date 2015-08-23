package jarrar.com.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by TOSHIBA on 09/07/2015.
 */
public class Movie implements Parcelable, Comparable<Movie> {

    String id;
    String title;
    String overview;
    String poster_Location;
    String backdrop_path;
    String release_Date;
    String rating;
    String popularity;

    public Movie() {
    }

    private Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        overview = in.readString();
        poster_Location = in.readString();
        backdrop_path = in.readString();
        release_Date = in.readString();
        rating = in.readString();
        popularity = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(poster_Location);
        dest.writeString(backdrop_path);
        dest.writeString(release_Date);
        dest.writeString(rating);
        dest.writeString(popularity);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPoster_Location(String poster_Location) {
        this.poster_Location = poster_Location;
    }

    public void setRelease_Date(String release_Date) {
        this.release_Date = release_Date;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static final Comparator<Movie> RATE_COMPARATOR = new Comparator<Movie>() {
        @Override
        public int compare(Movie lhs, Movie rhs) {
            return (int) Math.round(Float.parseFloat(rhs.rating) - Float.parseFloat(lhs.rating));
        }
    };
    public static final Comparator<Movie> POP_COMPARATOR = new Comparator<Movie>() {
        @Override
        public int compare(Movie lhs, Movie rhs) {
            return (int) Math.round(Float.parseFloat(rhs.popularity) - Float.parseFloat(lhs.popularity));
        }
    };

    @Override
    public int compareTo(Movie another) {
        return 0;
    }
}