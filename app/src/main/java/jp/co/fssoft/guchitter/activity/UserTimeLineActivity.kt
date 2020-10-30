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

/**
 * TODO
 *
 */
class UserTimeLineActivity : RootActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = UserTimeLineActivity::class.qualifiedName
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

        val userId = intent.getLongExtra("user_id", 0)

        val contents: LinearLayout = findViewById(R.id.contents)
        contents.removeAllViews()
        layoutInflater.inflate(R.layout.user_time_line_activity, contents)
        findViewById<RecyclerView>(R.id.tweet_recycle_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@UserTimeLineActivity, LinearLayoutManager.VERTICAL, false)
            adapter = TweetRecycleView(database.readableDatabase) { commonId, type ->
                when (type) {
                }
            }
            addItemDecoration(DividerItemDecoration(this@UserTimeLineActivity, DividerItemDecoration.VERTICAL))
            addOnScrollListener(TweetScrollEvent(
                {
                    callback -> getNextUserTweet(database.writableDatabase, userId, false) {
                        runOnUiThread {
                            (adapter as TweetRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                            adapter?.notifyDataSetChanged()
                            callback()
                        }
                    }
                },
                {
                    callback -> getPrevUserTweet(database.writableDatabase, userId, false) {
                        runOnUiThread {
                            (adapter as TweetRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                            adapter?.notifyDataSetChanged()
                            callback()
                        }
                    }
                }
            ))
        }


        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
