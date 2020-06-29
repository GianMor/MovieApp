package com.example.popularmovie.data;

import android.provider.BaseColumns;

public class MovieTableHelper implements BaseColumns {
    public static final String TABLE_NAME = "movies";
    public static final String TITLE = "title";
    public static final String ID_MOVIE = "id_movie";
    public static final String POSTER_PATH = "poster_path";
    public static final String DESCRIPTION = "description";
    public static final String VOTE = "vote_average";
    public static final String RELEASE_DATE = "release_date";
    public static final String FAVORITE = "favorite";
    public static final String POPULARITY = "popularity";
    public static final String VOTE_COUNT = "vote_count";



    public static final String CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID_MOVIE + " INTEGER PRIMARY KEY , " +
            TITLE + " TEXT , " +
            DESCRIPTION + " TEXT , " +
            POSTER_PATH + " TEXT , " +
            VOTE + " REAL , " +
            RELEASE_DATE + " TEXT , " +
            FAVORITE + " INTEGER DEFAULT 0, " +
            POPULARITY + " REAL , " +
            VOTE_COUNT + " REAL , " +
            "UNIQUE (" + ID_MOVIE + ") ON CONFLICT REPLACE ) ; ";


}

