package jp.co.fssoft.guchitter.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.*
import jp.co.fssoft.guchitter.utility.Json
import jp.co.fssoft.guchitter.utility.Utility
import jp.co.fssoft.guchitter.widget.OverlayMenuRecyclerView
import jp.co.fssoft.guchitter.widget.TweetScrollEvent
import jp.co.fssoft.guchitter.widget.TweetWrapRecyclerView

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
     * Keep id
     */
    private var keepId = 0L

    /**
     * Keep parent position
     */
    private var keepParentPosition = 0

    /**
     * Keep child position
     */
    private var keepChildPosition = 0

    private var keepOverlayView: View? = null

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
                findViewById<RecyclerView>(R.id.tweet_wrap_recycler_view).apply {
                    setHasFixedSize(true)
                    val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                    layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                    (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(scroll, offset)
                    adapter = TweetWrapRecyclerView(userId) {commonId, type, parentPosition, childPosition ->
                        keepId = commonId
                        keepParentPosition = parentPosition
                        keepChildPosition = childPosition

                        when (type) {
                            TweetWrapRecyclerView.Companion.ButtonType.REPLY -> {
                                val intent = Intent(applicationContext, PostTweetActivity::class.java).apply {
                                    putExtra("in_reply_to_status_id", commonId)
                                }
                                startActivity(intent)
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.FAVORITE -> {
                                TwitterApiFavoritesCreate(database.writableDatabase).start(mapOf("id" to commonId.toString())).callback = {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.REMOVE_FAVORITE -> {
                                TwitterApiFavoritesDestroy(database.writableDatabase).start(mapOf("id" to commonId.toString())).callback =  {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.RETWEET -> {
                                TwitterApiStatusesRetweet(commonId, database.writableDatabase).start(mapOf("id" to commonId.toString())).callback = {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                    runOnUiThread {
                                        adapter?.notifyItemRangeChanged(parentPosition, 1)
                                    }
                                }
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.REMOVE_RETWEET -> {
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
                                        (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                        runOnUiThread {
                                            adapter?.notifyItemRangeChanged(parentPosition, 1)
                                        }
                                    }
                                }
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.USER -> {
                                val intent = Intent(applicationContext, UserTimeLineActivity::class.java).apply {
                                    putExtra("user_id", commonId)
                                }
                                startActivity(intent)
                            }
                            TweetWrapRecyclerView.Companion.ButtonType.OTHER_MY -> {
                                val overlayView = layoutInflater.inflate(R.layout.overlay_view, null).apply {
                                    findViewById<LinearLayout>(R.id.message1_gray_layout).setBackgroundColor(Color.argb(127, 0, 63, 255))
                                    findViewById<RecyclerView>(R.id.overlay_menu_recycler_view).apply {
                                        setHasFixedSize(true)
                                        layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                                        adapter = OverlayMenuRecyclerView {
                                            when (it) {
                                                1 -> {
                                                    AlertDialog.Builder(this@HomeTimeLineActivity).apply {
                                                        title = getString(R.string.remove_tweet_title)
                                                        setMessage(R.string.remove_tweet_body)
                                                        setPositiveButton(R.string.common_remove_btn) { dialog, which ->
                                                            TwitterApiStatusesDestroy(keepId, db).start(mapOf()).callback = {
                                                                db.delete("t_users", "current = ?", arrayOf(keepId.toString()))
                                                                runOnUiThread {
                                                                    adapter?.notifyItemRangeChanged(parentPosition, 1)
                                                                }
                                                            }
                                                        }
                                                        setNegativeButton(R.string.common_cancel_btn) { dialog, which ->
                                                        }
                                                        show()
                                                    }
                                                }
                                            }
                                            windowManager.removeView(keepOverlayView)
                                        }
                                        (adapter as OverlayMenuRecyclerView).overlayMenuObjects = listOf(OverlayMenuRecyclerView.OverlayMenuData(1, R.drawable.trash, "削除"))
                                    }
                                }
                                keepOverlayView = overlayView
                                val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAGS_CHANGED, PixelFormat.TRANSLUCENT)
                                windowManager.addView(overlayView, params)
                                overlayView.setOnClickListener {
                                    windowManager.removeView(it)
                                }
                            }
                        }
                    }

                    addItemDecoration(DividerItemDecoration(this@HomeTimeLineActivity, DividerItemDecoration.VERTICAL))
                    addOnScrollListener(TweetScrollEvent(
                        { callback ->
                            val prevData = getCurrentHomeTweet(userId)
                            getNextHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                    var insets = 0
                                    for (i in 0 until (adapter as TweetWrapRecyclerView).tweetObjects.size) {
                                        if ((adapter as TweetWrapRecyclerView).tweetObjects[i][0].id > prevData[0][0].id) {
                                            ++insets
                                        }
                                        else {
                                            break
                                        }
                                    }
                                    if (insets > 0) {
                                        adapter?.notifyItemRangeInserted(0, insets)
                                        Toast.makeText(applicationContext, getString(R.string.next_tweet), Toast.LENGTH_LONG).show()
                                    }

                                    for (i in 0 until (adapter as TweetWrapRecyclerView).tweetObjects.size) {
                                        for (j in 0 until prevData.size) {
                                            if ((adapter as TweetWrapRecyclerView).tweetObjects[i][0].id == prevData[j][0].id) {
                                                if ((adapter as TweetWrapRecyclerView).tweetObjects[i].size != prevData[j].size) {
                                                    adapter?.notifyItemChanged(i)
                                                }
                                            }
                                        }
                                    }
                                    callback()
                                }
                            }
                        },
                        { callback ->
                            val prevData = getCurrentHomeTweet(userId)
                            getPrevHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                                    var insets = 0
                                    for (i in 0 until (adapter as TweetWrapRecyclerView).tweetObjects.size) {
                                        if ((adapter as TweetWrapRecyclerView).tweetObjects[i][0].id < prevData[prevData.size - 1][0].id) {
                                            ++insets
                                        }
                                        else {
                                            break
                                        }
                                    }
                                    if (insets > 0) {
                                        adapter?.notifyItemRangeInserted(prevData.size - 1, insets)
                                        Toast.makeText(applicationContext, getString(R.string.next_tweet), Toast.LENGTH_LONG).show()
                                    }
                                    for (i in 0 until (adapter as TweetWrapRecyclerView).tweetObjects.size) {
                                        for (j in 0 until prevData.size) {
                                            if ((adapter as TweetWrapRecyclerView).tweetObjects[i][0].id == prevData[j][0].id) {
                                                if ((adapter as TweetWrapRecyclerView).tweetObjects[i].size != prevData[j].size) {
                                                    adapter?.notifyItemChanged(i)
                                                }
                                            }
                                        }
                                    }
                                    callback()
                                }
                            }
                        }
                    ))
                    runOnUiThread {
                        (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
                        adapter?.notifyDataSetChanged()
                        if ((adapter as TweetWrapRecyclerView).tweetObjects.isEmpty() == true) {
                            getNextHomeTweet(userId, false) {
                                runOnUiThread {
                                    (adapter as TweetWrapRecyclerView).tweetObjects = getCurrentHomeTweet(userId)
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
