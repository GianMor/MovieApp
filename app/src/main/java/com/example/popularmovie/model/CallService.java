package com.example.popularmovie.model;

import android.content.ContentValues;
import android.content.Context;

import com.example.popularmovie.adapter.MovieAdapter;
import com.example.popularmovie.data.MovieProvider;
import com.example.popularmovie.data.MovieTableHelper;
import com.example.popularmovie.network.MovieService;
import com.example.popularmovie.network.WebService;
import com.example.popularmovie.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallService {

    List<Movie> mList;

    public void loadMoviesData(WebService webService, final Context context, final MovieAdapter adapter) {

        MovieService movieService = WebService.movieService(webService);
        Call<MovieCollection> collectionCall = movieService.getPopularMovies(Constants.API_KEY, "it-IT", 1);

        collectionCall.enqueue(new Callback<MovieCollection>() {
            @Override
            public void onResponse(Call<MovieCollection> call, Response<MovieCollection> response) {
                List<Movie> movieList = response.body().getMovieResults();
                setList(movieList);
                adapter.setMoviesData(movieList);

                for (Movie movie : movieList) {
                    ContentValues values = new ContentValues();
                    values.put(MovieTableHelper.TITLE, movie.getOriginalTitle());
                    values.put(MovieTableHelper.DESCRIPTION, movie.getOverview());
                    values.put(MovieTableHelper.POSTER_PATH, Constants.MOVIE_POSTER_W500_PATH + movie.getPosterPath());
                    values.put(MovieTableHelper.RELEASE_DATE, movie.getReleaseDate());
                    values.put(MovieTableHelper.VOTE, movie.getVoteAverage());


                    context.getContentResolver().insert(MovieProvider.MOVIES_URI, values);
                }


            }

            @Override
            public void onFailure(Call<MovieCollection> call, Throwable t) {
            }
        });


    }

    public List<Movie> getList() {
        return mList;
    }

    public void setList(final List<Movie> list) {
        mList = list;
    }
}
