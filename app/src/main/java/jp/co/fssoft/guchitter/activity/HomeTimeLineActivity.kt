package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TwitterApiFavoritesCreate
import jp.co.fssoft.guchitter.api.TwitterApiFavoritesDestroy
import jp.co.fssoft.guchitter.api.TwitterApiStatusRetweet
import jp.co.fssoft.guchitter.api.TwitterApiStatusUnretweet
import jp.co.fssoft.guchitter.widget.TweetScrollEvent
import jp.co.fssoft.guchitter.widget.TweetWrapRecycleView

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
                    adapter = TweetWrapRecycleView{commonId, type, parentPosition, childPosition ->
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
                                TwitterApiStatusRetweet(commonId).start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecycleView.Companion.ButtonType.REMOVE_RETWEET -> {
                                TwitterApiStatusUnretweet(commonId).start(database.writableDatabase, mapOf("id" to commonId.toString())) {
                                    (adapter as TweetWrapRecycleView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
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
