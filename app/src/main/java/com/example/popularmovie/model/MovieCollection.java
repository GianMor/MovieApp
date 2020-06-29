package com.example.popularmovie.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieCollection implements Parcelable {

    @SerializedName("results")
    List<Movie> movieResults;

    @SerializedName("page")
    private int page;

    @SerializedName("total_results")
    private int totalResults;

    @SerializedName("total_pages")
    private int totalPages;

    public List<Movie> getMovieResults() {
        return movieResults;
    }

    public void setMovieResults(List<Movie> movieResults) {
        this.movieResults = movieResults;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public MovieCollection(List<Movie> movieResults, int page, int totalResults, int totalPages) {
        this.movieResults = movieResults;
        this.page = page;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }

    private MovieCollection(Parcel in) {
        this.movieResults = in.createTypedArrayList(Movie.CREATOR);
        this.page = in.readInt();
        this.totalResults = in.readInt();
        this.totalPages = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeTypedList(this.movieResults);
        dest.writeInt(this.totalResults);
        dest.writeInt(this.totalPages);
    }

}
