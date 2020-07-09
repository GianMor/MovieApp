package com.example.popularmovie;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovie.data.MovieProvider;
import com.example.popularmovie.data.MovieTableHelper;
import com.example.popularmovie.utils.Constants;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    ImageView mFavoriteButton;

    ImageView movieImagePoster;
    TextView movieOriginalTitle,
            movieReleaseDate,
            movieVoteAverage,
            moviePlot;

    int mStateFavorite;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mFavoriteButton = findViewById(R.id.btn_favorite);

        movieImagePoster = findViewById(R.id.iv_detail_movie_poster);

        movieOriginalTitle = findViewById(R.id.tv_original_title_movie);
        movieReleaseDate = findViewById(R.id.tv_release_date_movie);
        movieVoteAverage = findViewById(R.id.tv_vote_average_movie);
        moviePlot = findViewById(R.id.tv_plot_movie);

        id = getIntent().getLongExtra("id_movie", 0);

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + "=" + id, null, null);
        Log.d("TAG", "onCreate: " + cursor);
        cursor.moveToNext();

        String imagePath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.BACKDROP_PATH));
        String originalTitle = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
        double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
        String plot = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));

        mStateFavorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));

        switch (mStateFavorite) {

            case 0:
                mFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_48);
                break;

            case 1:
                mFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_48);
                break;
        }

        Picasso.get()
                .load(Constants.MOVIE_POSTER_W500_PATH + imagePath)
                .into(movieImagePoster);

        movieOriginalTitle.setText(originalTitle);
        movieReleaseDate.setText(releaseDate);
        movieVoteAverage.setText("" + voteAverage);
        moviePlot.setText(plot);
    }
}
