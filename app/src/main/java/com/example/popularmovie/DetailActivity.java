package com.example.popularmovie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovie.data.MovieProvider;
import com.example.popularmovie.data.MovieTableHelper;
import com.example.popularmovie.utils.Constants;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    ImageButton mFavoriteButton;

    ImageView movieImagePoster;
    TextView movieOriginalTitle,
            movieReleaseDate,
            movieVoteAverage,
            moviePlot;

    int mStateFavorite;
    long id;

    int mFavoriteLocale;

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

        String imagePath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.POSTER_PATH));
        String originalTitle = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
        double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
        String plot = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));

        mStateFavorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
        mFavoriteLocale = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));

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

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (mFavoriteLocale == 0) {

                    mFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_48);

                    mFavoriteLocale = 1;

                    ContentValues values = new ContentValues();
                    values.put(MovieTableHelper.FAVORITE, 1);

                    getContentResolver().update(MovieProvider.MOVIES_URI, values, MovieTableHelper.ID_MOVIE + "=" + id, null);
                }
                else if (mFavoriteLocale == 1) {

                    mFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_48);

                    mFavoriteLocale = 0;

                    ContentValues values = new ContentValues();
                    values.put(MovieTableHelper.FAVORITE, 0);

                    getContentResolver().update(MovieProvider.MOVIES_URI, values, MovieTableHelper.ID_MOVIE + "=" + id, null);
                }
            }
        });
    }
}
