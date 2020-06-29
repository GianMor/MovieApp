package com.example.popularmovie.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class Movie implements Parcelable {

    @SerializedName("original_title")
    private String  originalTitle;


    @SerializedName("id")
    private long  id;

    @SerializedName("poster_path")
    private String  posterPath;

    @SerializedName("overview")
    private String  overview;

    @SerializedName("vote_average")
    private double  voteAverage;

    @SerializedName("release_date")
    private String  releaseDate;

    @SerializedName("favorite")
    private int  favorite;

    @SerializedName("popularity")
    private float  popularity;

    @SerializedName("vote_count")
    private float  voteCount;

    public Movie(long id,String movieOriginalTitle,String moviePosterPath,String movieOverview,
                 double movieVoteAverage, String movieReleaseDate,int movieFavorite,float moviePopularity,float movieVoteCount) {

        this.id = id;
        this.originalTitle = movieOriginalTitle;
        this.posterPath = moviePosterPath;
        this.overview = movieOverview;
        this.voteAverage = movieVoteAverage;
        this.releaseDate = movieReleaseDate;
        this.favorite = movieFavorite;
        this.popularity = moviePopularity;
        this.voteCount = movieVoteCount;
    }


    private Movie(Parcel in) {
        this.id = in.readInt();
        this.originalTitle = in.readString();
        this.posterPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
        this.favorite = in.readInt();
        this.popularity = in.readFloat();
        this.voteCount = in.readFloat();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeDouble(voteAverage);
        dest.writeString(releaseDate);
        dest.writeInt(favorite);
        dest.writeFloat(popularity);
        dest.writeFloat(voteCount);
    }


    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }


    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(final int favorite) {
        this.favorite = favorite;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(final float popularity) {
        this.popularity = popularity;
    }

    public float getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(final float voteCount) {
        this.voteCount = voteCount;
    }
}
