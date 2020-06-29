package com.example.popularmovie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovie.adapter.MovieAdapter;
import com.example.popularmovie.data.MovieProvider;
import com.example.popularmovie.data.MovieTableHelper;
import com.example.popularmovie.model.Movie;
import com.example.popularmovie.model.MovieCollection;
import com.example.popularmovie.network.MovieService;
import com.example.popularmovie.network.WebService;
import com.example.popularmovie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private MovieAdapter movieAdapter;

    private WebService webService;

    RecyclerView movieRecycleView;

    TextView mTextEmptyState;

    ImageButton mRefreshButton;

    Toolbar mToolbar;

    SearchView searchView;

    private int currentPage = 1;

    int totPage = 0;

    private boolean loading = true;

    int mStateList = 0;

    List<Movie> mMovieList;

    int initId = 0;

    int lastPosition;

    int pastVisiblesItems,
            visibleItemCount,
            totalItemCount;

    GridLayoutManager layoutManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        mMovieList = new ArrayList<>();

        webService = WebService.getInstance();

        movieRecycleView = findViewById(R.id.recyclerview_movie);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(this, 4);
        } else {
            layoutManager = new GridLayoutManager(this, 2);
        }

        mTextEmptyState = findViewById(R.id.tv_no_movie);

        mRefreshButton = findViewById(R.id.btn_refresh);

        movieRecycleView.setLayoutManager(layoutManager);

        movieRecycleView.setHasFixedSize(true);

        movieAdapter = new MovieAdapter(this);

        movieRecycleView.setAdapter(movieAdapter);


        if (isDbEmpty()) {
            if (isOnline()) {
                mTextEmptyState.setVisibility(View.INVISIBLE);
                mRefreshButton.setVisibility(View.VISIBLE);
                loadNextPagePopularMovies(currentPage);
            } else {
                movieRecycleView.setVisibility(View.INVISIBLE);
                mTextEmptyState.setText("Non ci sono film salvati, attiva internet per vedere i film");
                Toast.makeText(this, "Internet is not available", Toast.LENGTH_LONG).show();
            }
        } else {
            mRefreshButton.setVisibility(View.INVISIBLE);
            mMovieList = loadPopularMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
        }


        if(savedInstanceState != null) {
            mMovieList = (List<Movie>) savedInstanceState.get("list");
            lastPosition = savedInstanceState.getInt("h");
            pastVisiblesItems = lastPosition;
        }


        movieRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                //pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                pastVisiblesItems = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 0)) {
                        if (initId == 0) initId = 20;
                        if (getCountMoviesDb() > initId) {
                            List<Movie> newMovieList = loadPopularMoviesFromDb();
                            if (newMovieList.size() == 20) {
                                mMovieList.addAll(newMovieList);
                                movieAdapter.setMoviesData(mMovieList);
                                movieAdapter.notifyDataSetChanged();
                                initId += 20;
                            } else {
                                loadNextPagePopularMovies(currentPage);
                                currentPage++;
                            }
                        } else {
                            loadNextPagePopularMovies(currentPage);
                            currentPage++;
                        }

                    } else if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 1)) {
                        loadNextPageTopRatedMovies(currentPage);
                        currentPage++;
                        movieAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mStateList == 0) {
                    mTextEmptyState.setVisibility(View.INVISIBLE);
                    mRefreshButton.setVisibility(View.INVISIBLE);
                    movieRecycleView.setVisibility(View.VISIBLE);
                    mMovieList = loadPopularMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);

                } else if (mStateList == 1) {
                    mTextEmptyState.setVisibility(View.INVISIBLE);
                    mRefreshButton.setVisibility(View.INVISIBLE);
                    movieRecycleView.setVisibility(View.VISIBLE);
                    loadTopRatedMoviesFromDb();
                }
            }
        });

    }


    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mMovieList);
        outState.putInt("h", pastVisiblesItems);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        movieAdapter.setMoviesData((List<Movie>) savedInstanceState.get("list"));
        movieRecycleView.scrollToPosition(lastPosition);

    }

    @Override
    public void onClick(Movie clickedMovie) {

        Intent intentToStartDetailActivity = new Intent(MainActivity.this, DetailActivity.class);

        intentToStartDetailActivity.putExtra("id_movie", clickedMovie.getId());

        startActivity(intentToStartDetailActivity);
    }

    private int getCountMoviesDb() {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, null, null);
        return cursor.getCount();
    }

    public void loadNextPagePopularMovies(int page) {

        if (isOnline()) {

            MovieService movieService = WebService.movieService(webService);
            Call<MovieCollection> collectionCall = movieService.getPopularMovies(Constants.API_KEY, "it-IT", page);

            collectionCall.enqueue(new Callback<MovieCollection>() {
                @Override
                public void onResponse(Call<MovieCollection> call, Response<MovieCollection> response) {
                    List<Movie> movieList = response.body().getMovieResults();


                    for (Movie movie : movieList) {

                        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + " = " + movie.getId(), null, null);

                        if (cursor != null && !cursor.moveToNext()) {

                            ContentValues values = new ContentValues();
                            values.put(MovieTableHelper.ID_MOVIE, movie.getId());
                            values.put(MovieTableHelper.TITLE, movie.getOriginalTitle());
                            values.put(MovieTableHelper.DESCRIPTION, movie.getOverview());
                            values.put(MovieTableHelper.POSTER_PATH, movie.getPosterPath());
                            values.put(MovieTableHelper.RELEASE_DATE, movie.getReleaseDate());
                            values.put(MovieTableHelper.VOTE, movie.getVoteAverage());
                            values.put(MovieTableHelper.POPULARITY, movie.getPopularity());
                            values.put(MovieTableHelper.VOTE_COUNT, movie.getVoteCount());


                            getContentResolver().insert(MovieProvider.MOVIES_URI, values);

                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieCollection> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "error network", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void loadNextPageTopRatedMovies(int page) {

        if (isOnline()) {

            MovieService movieService = WebService.movieService(webService);
            Call<MovieCollection> collectionCall = movieService.getTopRatedMovies(Constants.API_KEY, "it-IT", page);

            collectionCall.enqueue(new Callback<MovieCollection>() {
                @Override
                public void onResponse(Call<MovieCollection> call, Response<MovieCollection> response) {
                    List<Movie> movieList = response.body().getMovieResults();
                    mMovieList.addAll(movieList);
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();

                    for (Movie movie : movieList) {
                        ContentValues values = new ContentValues();
                        values.put(MovieTableHelper.ID_MOVIE, movie.getId());
                        values.put(MovieTableHelper.TITLE, movie.getOriginalTitle());
                        values.put(MovieTableHelper.DESCRIPTION, movie.getOverview());
                        values.put(MovieTableHelper.POSTER_PATH, movie.getPosterPath());
                        values.put(MovieTableHelper.RELEASE_DATE, movie.getReleaseDate());
                        values.put(MovieTableHelper.VOTE, movie.getVoteAverage());
                        values.put(MovieTableHelper.POPULARITY, movie.getPopularity());
                        values.put(MovieTableHelper.VOTE_COUNT, movie.getVoteCount());

                        getContentResolver().insert(MovieProvider.MOVIES_URI, values);
                    }
                }

                @Override
                public void onFailure(Call<MovieCollection> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "error network", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public List<Movie> loadPopularMoviesFromDb() {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.POPULARITY + " DESC LIMIT " + initId + ",20", null);

        List<Movie> list = new ArrayList<>();

        if (cursor != null && cursor.getCount() != 0) {

            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {

                int id = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.ID_MOVIE));
                String title = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
                Double vote = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
                String poster = cursor.getString(cursor.getColumnIndex(MovieTableHelper.POSTER_PATH));
                int favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
                float popularity = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.POPULARITY));
                float voteCount = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.VOTE_COUNT));

                list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount));
                cursor.moveToNext();
            }
        }

        return list;

    }


    public List<Movie> loadTopRatedMoviesFromDb() {

        Cursor cursor = null;
        cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.VOTE_COUNT + " DESC", null);

        List<Movie> list = new ArrayList<>();

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            int id = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.ID_MOVIE));
            String title = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
            String desc = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
            Double vote = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
            String poster = cursor.getString(cursor.getColumnIndex(MovieTableHelper.POSTER_PATH));
            int favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
            float popularity = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.POPULARITY));
            float voteCount = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.VOTE_COUNT));

            list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount));
            cursor.moveToNext();
        }
        return list;
    }

    public List<Movie> loadFavoriteMoviesFromDb() {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.FAVORITE + " = " + 1, null, null, null);

        List<Movie> list = new ArrayList<>();

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            int id = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.ID_MOVIE));
            String title = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
            String desc = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
            Double vote = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
            String poster = cursor.getString(cursor.getColumnIndex(MovieTableHelper.POSTER_PATH));
            int favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
            float popularity = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.POPULARITY));
            float voteCount = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.VOTE_COUNT));

            list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount));
            cursor.moveToNext();
        }
        return list;
    }

    public boolean isDbEmpty() {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, null, null);

        if (cursor != null && !cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();

            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                String filter = query.toLowerCase();
                List<Movie> mfilterList = new ArrayList<>();

                for (int i = 0; i < mMovieList.size(); i++) {
                    String filterTitle = mMovieList.get(i).getOriginalTitle().toLowerCase();
                    if (filterTitle.startsWith(filter)) {
                        mfilterList.add(mMovieList.get(i));
                    }
                }
                movieAdapter.setMoviesData(mfilterList);
                movieAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                String filter = query.toLowerCase();
                List<Movie> mfilterList = new ArrayList<>();

                for (int i = 0; i < mMovieList.size(); i++) {
                    String filterTitle = mMovieList.get(i).getOriginalTitle().toLowerCase();
                    if (filterTitle.startsWith(filter)) {
                        mfilterList.add(mMovieList.get(i));
                    }
                }
                movieAdapter.setMoviesData(mfilterList);

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_popular) {
            mStateList = 0;
            if (isOnline()) loadPopularMoviesFromDb();
            else loadPopularMoviesFromDb();
            return true;
        }

        if (id == R.id.action_top_rated) {
            mStateList = 1;
            if (isOnline()) loadTopRatedMoviesFromDb();
            else loadTopRatedMoviesFromDb();
            return true;
        }

        if (id == R.id.action_favorite) {
            mStateList = 2;
            movieAdapter.setMoviesData(loadFavoriteMoviesFromDb());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
