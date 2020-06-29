package com.example.popularmovie.network;


import com.example.popularmovie.model.Movie;
import com.example.popularmovie.model.MovieCollection;
import com.example.popularmovie.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebService {

    public static final String API_URL = "https://api.themoviedb.org/3/";

    private static WebService instance;

    private MovieService movieService;




    private WebService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        movieService = retrofit.create(MovieService.class);
    }
    public static WebService getInstance() {
        if (instance == null)
            instance = new WebService();
        return instance;
    }

    public static MovieService movieService(WebService retrofit) {
        return retrofit.movieService;
    }
}
