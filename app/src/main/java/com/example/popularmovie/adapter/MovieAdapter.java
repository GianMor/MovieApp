package com.example.popularmovie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popularmovie.R;
import com.example.popularmovie.model.Movie;
import com.example.popularmovie.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private List<Movie> movieList;

    private List<Movie> mFilteredMovies;

    private final MovieAdapterOnClickHandler clickHandler;

    int pos;


    public interface MovieAdapterOnClickHandler {
        void onClick(Movie clickedMovie);
    }


    public MovieAdapter(MovieAdapterOnClickHandler click) {
        clickHandler = click;
        movieList = new ArrayList<>();
        mFilteredMovies = new ArrayList<>();
    }


    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {

        String movieItem = movieList.get(position).getPosterPath();

        String poster = Constants.MOVIE_POSTER_W500_PATH + movieItem;
        Picasso.get()
                .load(poster)
                .into(holder.movieImage);

        pos = holder.getAdapterPosition();

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void setMoviesData(List<Movie> moviesData) {
        movieList = moviesData;
        notifyDataSetChanged();
        Constants.INIT_ID_DB = 20;
    }


    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView movieImage;

        MovieAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            movieImage = itemView.findViewById(R.id.iv_movie);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movieClick = movieList.get(adapterPosition);
            clickHandler.onClick(movieClick);
        }

    }

}