package com.shadow.favoritecataloguemovie.ui.detail

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.shadow.favoritecataloguemovie.R
import com.shadow.favoritecataloguemovie.data.local.db.DbKeys
import com.shadow.favoritecataloguemovie.data.local.db.DbKeys.MovieColoumn.Companion.CONTENT_MOVIE
import com.shadow.favoritecataloguemovie.data.model.response.ItemMovie
import com.shadow.favoritecataloguemovie.data.remote.api.ApiKeys
import com.shadow.favoritecataloguemovie.ui.BaseActivity
import com.shadow.favoritecataloguemovie.util.Utilities
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*

class MovieDetailActivity : BaseActivity(), DetailContract, View.OnClickListener {

    private lateinit var item: ItemMovie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initComponent(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(MOVIE_RESULT, savedMovie)
        super.onSaveInstanceState(outState)
    }

    override fun initComponent(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            item = savedInstanceState.getParcelable<ItemMovie>(MOVIE_RESULT) as ItemMovie
        } else {
            item = intent.getParcelableExtra("MovieDetail") as ItemMovie
            savedMovie = item
        }

        itemTitle.text = item.original_title
        itemDesc.text = item.overview
        itemRating.text = item.vote_average.toString()
        itemVote.text = item.vote_count.toString()
        itemLanguage.text = item.original_language
        itemReleaseDate.text = item.release_date

        val backdropUrl = ApiKeys.IMAGE_HIGH + item.backdrop_path
        setImage(backdropUrl, backdropImage)

        val posterUrl = ApiKeys.IMAGE_MEDIUM + item.poster_path
        setImage(posterUrl, itemPoster)

        if (checkItem()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_delete)
        }

        buttonBack.setOnClickListener(this)
        favoriteButton.setOnClickListener(this)
    }

    override fun onErrorNetwork(view: ImageView) {
        onShowToast("No Connection Internet")
        view.setImageResource(R.drawable.ic_error)
        view.scaleType = ImageView.ScaleType.CENTER
    }

    override fun setImage(url: String, view: ImageView) {
        if (Utilities.isNetAvailable(this)) {
            Picasso.get().load(url).into(view)
        } else {
            onErrorNetwork(view)
        }
    }

    override fun addToFavorite() {
        val contentValues = ContentValues()

        contentValues.put(DbKeys.MovieColoumn.ID, item.id)
        contentValues.put(DbKeys.MovieColoumn.TITLE, item.original_title)
        contentValues.put(DbKeys.MovieColoumn.DESCRIPTION, item.overview)
        contentValues.put(DbKeys.MovieColoumn.POSTER, item.poster_path)
        contentValues.put(DbKeys.MovieColoumn.BACKDROP, item.backdrop_path)
        contentValues.put(DbKeys.MovieColoumn.VOTE_AVERAGE, item.vote_average)
        contentValues.put(DbKeys.MovieColoumn.VOTE_COUNT, item.vote_count)
        contentValues.put(DbKeys.MovieColoumn.ORIGINAL_LANGUAGE, item.original_language)
        contentValues.put(DbKeys.MovieColoumn.RELEASE_DATE, item.release_date)

        contentResolver.insert(CONTENT_MOVIE, contentValues)

        favoriteButton.setImageResource(R.drawable.ic_delete)

        onShowToast(resources.getString(R.string.addFavToast))
    }

    override fun removeFromFavorite() {
        contentResolver.delete(Uri.parse("${CONTENT_MOVIE}/${item.id}"), null, null)

        favoriteButton.setImageResource(R.drawable.ic_favorite)

        onShowToast(resources.getString(R.string.delFavToast))
    }

    override fun checkItem(): Boolean {
        val cursor = contentResolver.query(
            Uri.parse("${CONTENT_MOVIE}/${item.id}"), null, null, null, null
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) return false
            cursor.close()
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBack -> {
                onBackPressed()
            }
            R.id.favoriteButton -> {
                if (checkItem()) {
                    addToFavorite()
                } else {
                    removeFromFavorite()
                }
            }
        }
    }

    override fun onShowToast(message: String) {
        showToast(message)
    }

    companion object{
        val MOVIE_RESULT = "movie_result"
        lateinit var savedMovie: ItemMovie
    }
}