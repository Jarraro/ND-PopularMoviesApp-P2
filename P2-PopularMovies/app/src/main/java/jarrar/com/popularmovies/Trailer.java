package jarrar.com.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by TOSHIBA on 23/08/2015.
 */
public class Trailer implements Parcelable{
    private String name;
    private String path;

    public Trailer() {
    }

    protected Trailer(Parcel in) {
        name = in.readString();
        path = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
    }
}
