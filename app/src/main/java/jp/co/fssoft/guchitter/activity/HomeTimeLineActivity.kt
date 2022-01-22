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
import jp.co.fssoft.guchitter.service.PostTweetService
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
     *
     */
    private var scroll = 0

    /**
     *
     */
    private var offset = 0

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
        findViewById<ImageButton>(R.id.tweet_write_btn).apply {
            val btnImage = Utility.resizeBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.tweet_pen),
                196
            )
            setImageBitmap(Utility.circleTransform(btnImage))
            setOnClickListener {
                startActivity(Intent(application, PostTweetActivity::class.java))
            }
        }

        /***********************************************
         * ユーザーデータがあるか確認する
         * なければ認証
         */
        val db = database.readableDatabase
        db.rawQuery("SELECT * FROM t_users WHERE current = ?", arrayOf("1")).use {
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
    override fun onResume()
    {
        super.onResume()

        Log.d(TAG, "[START]onResume()")

        val db = database.readableDatabase
        db.rawQuery("SELECT user_id FROM t_users WHERE current = ?", arrayOf("1")).use {
            if (it.count == 1) {
                it.moveToFirst()
                findViewById<RecyclerView>(R.id.tweet_wrap_recycle_view).apply {
                    setHasFixedSize(true)
                    val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                    layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                    (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(scroll, offset)
                    adapter = TweetWrapRecycleView(userId) {commonId, type, parentPosition, childPosition ->
                        when (type) {
                            TweetWrapRecycleView.Companion.ButtonType.REPLY -> {
                                val intent = Intent(applicationContext, PostTweetActivity::class.java).apply {
                                    putExtra("in_reply_to_status_id", commonId)
                                }
                                startActivity(intent)
                            }
                            TweetWrapRecycleView.Companion.ButtonType.FAVORITE -> {
                                TwitterApiFavoritesCreate(database.writableDatabase).start(mapOf("id" to commonId.toString())).callback = {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.REMOVE_FAVORITE -> {
                                TwitterApiFavoritesDestroy(database.writableDatabase).start(mapOf("id" to commonId.toString())).callback =  {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.RETWEET -> {
                                TwitterApiStatusesRetweet(commonId, database.writableDatabase).start(mapOf("id" to commonId.toString())).callback = {
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
                                        val tweet_id = it.getLong(it.getColumnIndexOrThrow("tweet_id"))
                                        Log.e(TAG, tweet_id.toString())
                                        movable = it.moveToNext()
                                    }
                                }
                                database.readableDatabase.rawQuery("SELECT data FROM t_time_lines WHERE tweet_id = ?", arrayOf(commonId.toString())).use {
                                    it.moveToFirst()
                                    val data = it.getString(it.getColumnIndexOrThrow("data"))
                                    val json = Json.jsonDecode(TweetObject.serializer(), data)
                                    retweetId = json.retweetedTweet!!.id
                                }

                                TwitterApiStatusesUnretweet(retweetId, database.writableDatabase).start(mapOf("id" to retweetId.toString())).callback = {
                                    TwitterApiStatusesShow(database.writableDatabase).start(mapOf("id" to commonId.toString())).callback = {
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
        Log.d(TAG, "[END]onResume()")
    }

    /**
     * TODO
     *
     * @param outState
     */
    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)

        Log.d(TAG, "[START]onSaveInstanceState(${outState})")
        outState.putInt("offset", offset)
        outState.putInt("scroll", scroll)
        Log.d(TAG, "[END]onSaveInstanceState(${outState})")
    }

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle)
    {
        super.onRestoreInstanceState(savedInstanceState)

        Log.d(TAG, "[START]onRestoreInstanceState(${savedInstanceState})")
        offset = savedInstanceState.getInt("offset")
        scroll = savedInstanceState.getInt("scroll")
        Log.d(TAG, "[END]onRestoreInstanceState(${savedInstanceState})")
    }
}
