package com.example.popularmovie.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    public static final String AUTORITY = "com.example.popularmovie.data.ContentProvider";

    public static final String BASE_PATH_MOVIES = "movies";

    public static final int ALL_MOVIE = 1;
    public static final int SINGLE_MOVIE = 0;


    public static final String MIME_TYPE_MOVIES = ContentResolver.CURSOR_DIR_BASE_TYPE + "vnd.all_movies";
    public static final String MIME_TYPE_MOVIE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd.single_movie";


    public static final Uri MOVIES_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTORITY
            + "/" + BASE_PATH_MOVIES);


    private MovieDB database;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTORITY, BASE_PATH_MOVIES, ALL_MOVIE);
        uriMatcher.addURI(AUTORITY, BASE_PATH_MOVIES + "/#", SINGLE_MOVIE);
    }
    @Override
    public boolean onCreate() {
        database = new MovieDB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = database.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case SINGLE_MOVIE:
                builder.setTables(MovieTableHelper.TABLE_NAME);
                builder.appendWhere(MovieTableHelper._ID + " = " + uri.getLastPathSegment());
                break;
            case ALL_MOVIE:
                builder.setTables(MovieTableHelper.TABLE_NAME);
                break;

        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SINGLE_MOVIE:
                return MIME_TYPE_MOVIE;
            case ALL_MOVIE:
                return MIME_TYPE_MOVIES;
        }
        return null;    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) == ALL_MOVIE) {
            SQLiteDatabase db = database.getWritableDatabase();
            long result = db.insert(MovieTableHelper.TABLE_NAME, null, values);
            String resultString = ContentResolver.SCHEME_CONTENT + "://" + BASE_PATH_MOVIES + "/" + result;
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(resultString);
        } else if (uriMatcher.match(uri) == SINGLE_MOVIE) {
            SQLiteDatabase db = database.getWritableDatabase();
            long result = db.insert(MovieTableHelper.TABLE_NAME, null, values);
            String resultString = ContentResolver.SCHEME_CONTENT + "://" + BASE_PATH_MOVIES + "/" + result;
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(resultString);
        }
        return null;    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = "", query = "";
        SQLiteDatabase db = database.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case SINGLE_MOVIE:
                table = MovieTableHelper.TABLE_NAME;
                query = MovieTableHelper._ID + " = " + uri.getLastPathSegment();
                if (selection != null) {
                    query += " AND " + selection;
                }
                break;
            case ALL_MOVIE:
                table = MovieTableHelper.TABLE_NAME;
                query = selection;
                break;
        }
        int deletedRows = db.delete(table, query, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = "", query = "";
        SQLiteDatabase db = database.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case SINGLE_MOVIE:
                table = MovieTableHelper.TABLE_NAME;
                query = MovieTableHelper._ID + " = " + uri.getLastPathSegment();
                if (selection != null) {
                    query += " AND " + selection;
                }
                break;
            case ALL_MOVIE:
                table = MovieTableHelper.TABLE_NAME;
                query = selection;
                break;
        }
        int updatedRows = db.update(table, values, query, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return updatedRows;
    }
}
