package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.widget.TweetRecycleView
import jp.co.fssoft.guchitter.widget.TweetScrollEvent

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
                findViewById<RecyclerView>(R.id.tweet_recycle_view).apply {
                    setHasFixedSize(true)
                    val userId = it.getLong(it.getColumnIndex("user_id"))
                    layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                    adapter = TweetRecycleView(database.readableDatabase) { commonId, type, position ->
                        when (type) {
                            TweetRecycleView.Companion.ButtonType.FAVORITE -> {

                            }
                            TweetRecycleView.Companion.ButtonType.RETWEET -> {

                            }
                            TweetRecycleView.Companion.ButtonType.USER -> {
                                val intent = Intent(applicationContext, UserTimeLineActivity::class.java).apply {
                                    putExtra("user_id", commonId)
                                }
                                startActivity(intent)
                            }
                        }
                    }
                    runOnUiThread {
                        (adapter as TweetRecycleView).tweetObjects = getCurrentHomeTweet(db, userId)
                        adapter?.notifyDataSetChanged()
                        if ((adapter as TweetRecycleView).tweetObjects.isEmpty() == true) {
                            getNextHomeTweet(database.writableDatabase, userId, false) {
                                runOnUiThread {
                                    (adapter as TweetRecycleView).tweetObjects = getCurrentHomeTweet(db, userId)
                                    adapter?.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    addItemDecoration(DividerItemDecoration(this@HomeTimeLineActivity, DividerItemDecoration.VERTICAL))
                    addOnScrollListener(TweetScrollEvent(
                        {
                            callback -> getNextHomeTweet(database.writableDatabase, userId, false) {
                                runOnUiThread {
                                    (adapter as TweetRecycleView).tweetObjects = getCurrentHomeTweet(db, userId)
                                    adapter?.notifyDataSetChanged()
                                    callback()
                                }
                            }
                        },
                        {
                            callback -> getPrevHomeTweet(database.writableDatabase, userId, false) {
                                runOnUiThread {
                                    (adapter as TweetRecycleView).tweetObjects = getCurrentHomeTweet(db, userId)
                                    adapter?.notifyDataSetChanged()
                                    callback()
                                }
                            }
                        }
                    ))
                }
            }
        }
        Log.d(TAG, "[END]onStart()")
    }
}
