package jp.co.fssoft.guchitter.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.utility.Imager
import jp.co.fssoft.guchitter.utility.Utility
import jp.co.fssoft.guchitter.widget.TweetScrollEvent
import jp.co.fssoft.guchitter.widget.TweetWrapRecycleView
import java.io.FileInputStream

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
        val userObject = getUser(userId)

        val contents: LinearLayout = findViewById(R.id.contents)
        contents.removeAllViews()
        layoutInflater.inflate(R.layout.user_time_line_activity, contents)

        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = DisplayMetrics()
        display.getRealMetrics(size)

        findViewById<RelativeLayout>(R.id.user_timeline_header_layout).apply {
            layoutParams.height = size.heightPixels / 16
        }

        findViewById<ImageButton>(R.id.user_timeline_activity_header_prev).apply {
            layoutParams.width = size.heightPixels / 16
            layoutParams.height = layoutParams.width
            setOnClickListener {
                finish()
            }
        }
        findViewById<ImageButton>(R.id.user_timeline_activity_header_other).apply {
            layoutParams.width = size.heightPixels / 16
            layoutParams.height = layoutParams.width
        }
        findViewById<ImageButton>(R.id.user_timeline_activity_header_user_icon).apply {
            Imager().loadImage(applicationContext, userObject.profileImageUrl, Imager.Companion.ImagePrefix.USER) {
                setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(FileInputStream(it))))
            }
            layoutParams.width = size.heightPixels / 16
            layoutParams.height = layoutParams.width
        }
        findViewById<TextView>(R.id.user_timeline_header_name).apply {
            text = userObject.name
            layoutParams.width = size.widthPixels / 2
        }
        findViewById<TextView>(R.id.user_timeline_header_tweets).apply {
            text = String.format("%,d", userObject.tweets)
        }
        findViewById<RecyclerView>(R.id.tweet_wrap_recycle_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@UserTimeLineActivity, LinearLayoutManager.VERTICAL, false)
            adapter = TweetWrapRecycleView { commonId, type, parentPosition, childPosition ->
                when (type) {
                    TweetWrapRecycleView.Companion.ButtonType.USER -> {
                        if (userId == commonId) {
                            val target = layoutManager?.findViewByPosition(parentPosition)?.findViewById<RecyclerView>(R.id.tweet_recycle_view)?.layoutManager?.findViewByPosition(childPosition)
                            target?.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.shake))
                        }
                        else {
                            val intent = Intent(applicationContext, UserTimeLineActivity::class.java)
                            intent.putExtra("user_id", commonId)
                            startActivity(intent)
                        }
                    }
                }
            }
            runOnUiThread {
                (adapter as TweetWrapRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                adapter?.notifyDataSetChanged()
                if ((adapter as TweetWrapRecycleView).tweetObjects.isEmpty() == true) {
                    getNextHomeTweet(database.writableDatabase, userId, false) {
                        runOnUiThread {
                            (adapter as TweetWrapRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            addItemDecoration(DividerItemDecoration(this@UserTimeLineActivity, DividerItemDecoration.VERTICAL))
            addOnScrollListener(TweetScrollEvent(
                {
                    callback -> getNextUserTweet(database.writableDatabase, userId, false) {
                        runOnUiThread {
                            val beforeCount = (adapter as TweetWrapRecycleView).tweetObjects.size
                            (adapter as TweetWrapRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                            val afterCount = (adapter as TweetWrapRecycleView).tweetObjects.size
                            adapter?.notifyDataSetChanged()
                            callback()
                            layoutManager!!.scrollToPosition(afterCount - beforeCount + 1)
                        }
                    }
                },
                {
                    callback -> getPrevUserTweet(database.writableDatabase, userId, false) {
                        runOnUiThread {
                            val beforeCount = (adapter as TweetWrapRecycleView).tweetObjects.size
                            (adapter as TweetWrapRecycleView).tweetObjects = getCurrentUserTweet(database.readableDatabase, userId)
                            adapter?.notifyDataSetChanged()
                            callback()
                            layoutManager!!.scrollToPosition(beforeCount)
                        }
                    }
                }
            ))
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
