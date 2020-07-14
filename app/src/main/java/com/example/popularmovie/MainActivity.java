package com.example.popularmovie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovie.adapter.MovieAdapter;
import com.example.popularmovie.data.MovieProvider;
import com.example.popularmovie.data.MovieTableHelper;
import com.example.popularmovie.fragment.AddFavoriteDialogfragment;
import com.example.popularmovie.fragment.IAddFavoriteDialogfragmentListener;
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

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, IAddFavoriteDialogfragmentListener {

    private MovieAdapter movieAdapter;

    private WebService webService;

    RecyclerView movieRecycleView;

    TextView mTextEmptyState;

    ImageButton mRefreshButton;

    Toolbar mToolbar;

    SearchView searchView;

    private int currentPage = 1;

    private boolean loading = true;

    int mStateList = 0,
            mOldStateList = 0;

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

        movieAdapter = new MovieAdapter(this,this);

        movieRecycleView.setAdapter(movieAdapter);


        if (isDbEmpty()) {
            if (isOnline()) {
                mTextEmptyState.setVisibility(View.INVISIBLE);
                mRefreshButton.setVisibility(View.VISIBLE);
                loadNextPageMovies(currentPage);
            } else {
                movieRecycleView.setVisibility(View.INVISIBLE);
                mTextEmptyState.setText("Non ci sono film salvati, attiva internet per vedere i film");
                Toast.makeText(this, "Internet is not available", Toast.LENGTH_LONG).show();
            }
        } else {
            if (savedInstanceState != null) {
                String str = savedInstanceState.getString("query");

                if (str.equals("")) {
                    if (mStateList == 0) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    } else if (mStateList == 1) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    } else if (mStateList == 2) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    }
                } else {
                    mStateList = 3;
                    mMovieList = loadFilteredlist(str);
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                }
            } else {
                mRefreshButton.setVisibility(View.INVISIBLE);
                mMovieList = loadMoviesFromDb();
                movieAdapter.setMoviesData(mMovieList);
                movieAdapter.notifyDataSetChanged();
            }
        }


        if (savedInstanceState != null) {
            //mMovieList = (List<Movie>) savedInstanceState.get("list");
            lastPosition = savedInstanceState.getInt("h");
        }


        movieRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();


                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 0)) {
                        if (initId == 0) initId = 20;
                        if (getCountMoviesDb() > initId) {
                            List<Movie> newMovieList = loadMoviesFromDb();
                            if (newMovieList.size() == 20) {
                                mMovieList.addAll(newMovieList);
                                movieAdapter.setMoviesData(mMovieList);
                                movieAdapter.notifyDataSetChanged();
                                initId += 20;
                            } else {
                                loadNextPageMovies(currentPage);
                                currentPage++;
                            }
                        } else {
                            loadNextPageMovies(currentPage);
                            currentPage++;
                        }

                    } else if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 1)) {
                        if (initId == 0) initId = 20;
                        if (getCountMoviesDb() > initId) {
                            List<Movie> newMovieList = loadMoviesFromDb();
                            if (newMovieList.size() == 20) {
                                mMovieList.addAll(newMovieList);
                                movieAdapter.setMoviesData(mMovieList);
                                movieAdapter.notifyDataSetChanged();
                                initId += 20;
                            } else {
                                loadNextPageMovies(currentPage);
                                currentPage++;
                            }
                        } else {
                            loadNextPageMovies(currentPage);
                            currentPage++;
                        }
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
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);

                } else if (mStateList == 1) {
                    mTextEmptyState.setVisibility(View.INVISIBLE);
                    mRefreshButton.setVisibility(View.INVISIBLE);
                    movieRecycleView.setVisibility(View.VISIBLE);
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                }
            }
        });


    }


    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", searchView.getQuery().toString());
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

    @Override
    public void onLongClick(Movie clickedMovie) {
        FragmentManager fm = getSupportFragmentManager();

        int favorite = clickedMovie.getFavorite();

        if (favorite == 0) {
            AddFavoriteDialogfragment dialogfragment = new AddFavoriteDialogfragment("Aggiungere ai preferiti?", "", clickedMovie.getId());
            dialogfragment.show(fm, "dialogFragmentFavorite");
        } else if (favorite == 1) {
            AddFavoriteDialogfragment dialogfragment = new AddFavoriteDialogfragment("Eliminare dai preferiti?", "", clickedMovie.getId());
            dialogfragment.show(fm, "dialogFragmentFavorite");
        }

    }

    private int getCountMoviesDb() {

        Cursor cursor = null;

        if (mStateList == 0 || mStateList == 1)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, null, null);
        else if (mStateList == 2)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.FAVORITE + " = " + 1, null, null, null);

        return cursor.getCount();
    }

    public void loadNextPageMovies(int page) {

        if (isOnline()) {

            MovieService movieService = WebService.movieService(webService);

            Call<MovieCollection> collectionCall = null;

            if (mStateList == 0)
                collectionCall = movieService.getPopularMovies(Constants.API_KEY, "it-IT", page);
            else if (mStateList == 1)
                collectionCall = movieService.getTopRatedMovies(Constants.API_KEY, "it-IT", page);

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
                            values.put(MovieTableHelper.BACKDROP_PATH, movie.getBackdropPath());

                            cursor.close();
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public List<Movie> loadMoviesFromDb() {

        Cursor cursor = null;

        if (mStateList == 0)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.POPULARITY + " DESC LIMIT " + initId + ",20", null);
        else if (mStateList == 1)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.VOTE_COUNT + " DESC LIMIT " + initId + ",20", null);
        else if (mStateList == 2)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.FAVORITE + " = " + 1, null, null, null);


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
                String backdropPath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.BACKDROP_PATH));

                list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount, backdropPath));
                cursor.moveToNext();
            }
        }

        cursor.close();
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

    public List<Movie> loadFilteredlist(String filter) {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.TITLE + " LIKE '" + filter + "%'", null, null);

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
                String backdropPath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.BACKDROP_PATH));

                list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount, backdropPath));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return list;
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

                if (!query.isEmpty()) {
                    mStateList = 3;
                    mMovieList = loadFilteredlist(query);
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 0 && query.equals("")) {
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 1 && query.equals("")) {
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 2 && query.equals("")) {
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                }

                return false;

            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed


                if (!query.isEmpty()) {
                    mStateList = 3;
                    mMovieList = loadFilteredlist(query);
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 0 && query.equals("")) {
                    mStateList = mOldStateList;
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 1 && query.equals("")) {
                    mStateList = mOldStateList;
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                } else if (mOldStateList == 2 && query.equals("")) {
                    mStateList = mOldStateList;
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                    movieAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_popular) {
            mStateList = 0;
            mOldStateList = 0;
            initId = 0;
            currentPage = 1;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.action_top_rated) {
            mStateList = 1;
            mOldStateList = 1;
            initId = 0;
            currentPage = 1;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.action_favorite) {
            mStateList = 2;
            mOldStateList = 2;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveClick(long id) {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + "=" + id, null, null);
        cursor.moveToNext();
        int favorite = 0;

        favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));


        ContentValues values = new ContentValues();

        if (favorite == 0) {
            values.put(MovieTableHelper.FAVORITE, 1);
        } else if (favorite == 1) {
            values.put(MovieTableHelper.FAVORITE, 0);
        }

        getContentResolver().update(MovieProvider.MOVIES_URI, values, MovieTableHelper.ID_MOVIE + " = " + id, null);
        cursor.close();

        if(mStateList == 2) {
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNegativeClick() {

    }
}
