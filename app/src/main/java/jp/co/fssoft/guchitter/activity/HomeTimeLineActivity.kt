package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.*
import jp.co.fssoft.guchitter.utility.Json
import jp.co.fssoft.guchitter.utility.Utility
import jp.co.fssoft.guchitter.widget.TweetScrollEvent
import jp.co.fssoft.guchitter.widget.TweetWrapRecycleView

/**
 * TODO
 *
 */
class HomeTimeLineActivity : RootActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = HomeTimeLineActivity::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "[START]onCreate(${savedInstanceState})")

        val contents: LinearLayout = findViewById(R.id.contents)
        contents.removeAllViews()
        layoutInflater.inflate(R.layout.home_time_line_activity, contents)
        val tweetBtn: ImageButton = findViewById(R.id.tweet_write_btn)
        tweetBtn.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeResource(resources, R.drawable.tweet_pen)))

        /***********************************************
         * ユーザーデータがあるか確認する
         * なければ認証
         */
        val db = database.readableDatabase
        db.rawQuery("SELECT * FROM t_users WHERE current = 1", null).use {
            if (it.count == 0) {
                startActivity(Intent(application, AuthenticationActivity::class.java))
            }
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }

    /**
     * TODO
     *
     */
    override fun onStart()
    {
        super.onStart()

        Log.d(TAG, "[START]onStart()")

        val db = database.readableDatabase
        db.rawQuery("SELECT user_id FROM t_users WHERE current = 1", null).use {
            if (it.count == 1) {
                it.moveToFirst()
                findViewById<RecyclerView>(R.id.tweet_wrap_recycle_view).apply {
                    setHasFixedSize(true)
                    val userId = it.getLong(it.getColumnIndex("user_id"))
                    layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                    adapter = TweetWrapRecycleView(userId) {commonId, type, parentPosition, childPosition ->
                        when (type) {
                            TweetWrapRecycleView.Companion.ButtonType.FAVORITE -> {
                                TwitterApiFavoritesCreate().start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.REMOVE_FAVORITE -> {
                                TwitterApiFavoritesDestroy().start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.RETWEET -> {
                                TwitterApiStatusesRetweet(commonId).start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.REMOVE_RETWEET -> {

                                var retweetId = 0L
                                Log.e(TAG, commonId.toString())
                                database.readableDatabase.rawQuery("SELECT tweet_id FROM t_time_lines ORDER BY tweet_id DESC", null).use {
                                    var movable = it.moveToFirst()
                                    while (movable) {
                                        val tweet_id = it.getLong(it.getColumnIndex("tweet_id"))
                                        Log.e(TAG, tweet_id.toString())
                                        movable = it.moveToNext()
                                    }
                                }
                                database.readableDatabase.rawQuery("SELECT data FROM t_time_lines WHERE tweet_id = ${commonId}", null).use {
                                    it.moveToFirst()
                                    val data = it.getString(it.getColumnIndex("data"))
                                    val json = Json.jsonDecode(TweetObject.serializer(), data)
                                    retweetId = json.retweetedTweet!!.id
                                }

                                TwitterApiStatusesUnretweet(retweetId).start(database.writableDatabase, mapOf("id" to retweetId.toString())) {
                                    TwitterApiStatusesShow().start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                        (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                        runOnUiThread {
                                            adapter?.notifyItemRangeChanged(parentPosition, 1)
                                        }
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.USER -> {
                                val intent = Intent(applicationContext, UserTimeLineActivity::class.java).apply {
                                    putExtra("user_id", commonId)
                                }
                                startActivity(intent)
                            }
                        }
                    }
                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                    addItemDecoration(DividerItemDecoration(this@HomeTimeLineActivity, DividerItemDecoration.VERTICAL))
                    addOnScrollListener(TweetScrollEvent(
                        {
                            callback -> getNextHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    adapter?.notifyDataSetChanged()
                                    callback()
                                }
                            }
                        },
                        {
                            callback -> getPrevHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    adapter?.notifyDataSetChanged()
                                    callback()
                                }
                            }
                        }
                    ))
                    runOnUiThread {
                        (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                        adapter?.notifyDataSetChanged()
                        if ((adapter as TweetWrapRecycleView).tweetObjects.isEmpty() == true) {
                            getNextHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    adapter?.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "[END]onStart()")
    }
}
