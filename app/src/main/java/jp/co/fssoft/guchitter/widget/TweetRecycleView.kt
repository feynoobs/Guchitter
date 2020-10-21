package jp.co.fssoft.guchitter.widget

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.database.sqlite.SQLiteDatabase
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TweetObject
import jp.co.fssoft.guchitter.utility.Utility


/**
 * TODO
 *
 * @constructor
 * TODO
 *
 * @param view
 */
class TweetViewHolder(private val view: View) : RecyclerView.ViewHolder(view)
{

    companion object
    {
        /**
         *
         */
        private val TAG = TweetViewHolder::class.qualifiedName
    }

    /**
     *
     */
    val icon: ImageButton = view.findViewById(R.id.tweet_recycle_view_user_icon)

    /**
     *
     */
    val nameText: TextView = view.findViewById(R.id.tweet_recycle_view_user_name)

    /**
     *
     */
    val mainText: TextView = view.findViewById(R.id.tweet_recycle_view_main)

    /**
     *
     */
    val timeText: TextView = view.findViewById(R.id.tweet_recycle_view_post_time)

    /**
     *
     */
    val replyBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_reply_button)

    /**
     *
     */
    val replyText: TextView = view.findViewById(R.id.tweet_recycle_view_reply_count)

    /**
     *
     */
    val retweetBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_retweet_button)

    /**
     *
     */
    val retweetText: TextView = view.findViewById(R.id.tweet_recycle_view_retweet_count)

    /**
     *
     */
    val favoriteBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_favorite_button)

    /**
     *
     */
    val favoriteText: TextView = view.findViewById(R.id.tweet_recycle_view_favorite_count)

    /**
     *
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_share_button)

    /**
     *
     */
    val space1: Space = view.findViewById(R.id.tweet_recycle_view_space1)

    /**
     *
     */
    val space2: Space = view.findViewById(R.id.tweet_recycle_view_space2)

    /**
     *
     */
    val space3: Space = view.findViewById(R.id.tweet_recycle_view_space3)

    /**
     *
     */
    init
    {
        Log.d(TAG, "${(view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay}")
        Log.d(TAG, "${(view as LinearLayout).width}")
        Log.d(TAG, "${ContextCompat.getSystemService(view.context, WINDOW_SERVICE::class.java)}")
        val display = (ContextCompat.getSystemService(view.context, WINDOW_SERVICE::class.java) as WindowManager).defaultDisplay
        val size = DisplayMetrics()
        display.getRealMetrics(size)

        var params: ViewGroup.LayoutParams = icon.layoutParams
        params.width = (size.widthPixels * 0.2).toInt()
        params.height = params.width
        icon.layoutParams = params

        params = replyBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        replyBtn.layoutParams = params

        params = replyText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        replyText.layoutParams = params

        params = space1.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        space1.layoutParams = params

        params = favoriteBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.3333).toInt()
        params.height = params.width
        favoriteBtn.layoutParams = params

        params = favoriteText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        favoriteText.layoutParams = params

        params = space2.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        space2.layoutParams = params

        params = retweetBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        retweetBtn.layoutParams = params

        params = retweetText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        retweetText.layoutParams = params

        params = space3.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        space3.layoutParams = params

        params = shareBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.25 * 0.333).toInt()
        params.height = params.width
        shareBtn.layoutParams = params


        view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            /**
             * TODO
             *
             */
            override fun onGlobalLayout()
            {
                if (view.measuredWidth > 0) {
                    if (view.measuredHeight > 0) {
//                        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        /*
                        var params: ViewGroup.LayoutParams = icon.layoutParams
                        params.width = (view.width * 0.2).toInt()
                        params.height = params.width
                        icon.layoutParams = params

                        params = replyBtn.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        replyBtn.layoutParams = params

                        params = replyText.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        replyText.layoutParams = params

                        params = space1.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        space1.layoutParams = params

                        params = favoriteBtn.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.3333).toInt()
                        params.height = params.width
                        favoriteBtn.layoutParams = params

                        params = favoriteText.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        favoriteText.layoutParams = params

                        params = space2.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        space2.layoutParams = params

                        params = retweetBtn.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        retweetBtn.layoutParams = params

                        params = retweetText.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        retweetText.layoutParams = params

                        params = space3.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        space3.layoutParams = params

                        params = shareBtn.layoutParams
                        params.width = (view.width * 0.8 * 0.25 * 0.333).toInt()
                        params.height = params.width
                        shareBtn.layoutParams = params

                         */
                    }
                }
            }
        })
    }
}

/**
 * TODO
 *
 */
class TweetRecycleView(private val db: SQLiteDatabase, private val userId: Long, callback: (Int)->Unit) : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetRecycleView::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder
    {
        Log.d(TAG, "[START]onCreateViewHolder(${parent}, ${viewType})")
        return TweetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tweet_recycle_view, parent, false))
    }

    /**
     * TODO
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: TweetViewHolder, position: Int)
    {
        Log.d(TAG, "[START]onBindViewHolder(${holder}, ${position})")

        var query = "SELECT data FROM t_timelines WHERE user_id = ${userId} ORDER BY tweet_id DESC LIMIT 1 OFFSET ${position}";
        if (userId == 0L) {
            query = "SELECT data FROM t_timelines WHERE home = 1 ORDER BY tweet_id DESC LIMIT 1 OFFSET ${position}";
        }

        db.rawQuery(query, null).use {
            it.moveToFirst()
            val tweet = Utility.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndex("data")))
            holder.nameText.text = tweet.user.name
            holder.mainText.text = tweet.text
            holder.favoriteBtn.setImageResource(R.drawable.tweet_favorite)
        }

        Log.d(TAG, "[END]onBindViewHolder(${holder}, ${position})")
    }

    /**
     * TODO
     *
     * @return
     */
    override fun getItemCount(): Int
    {

        Log.d(TAG, "[START]getItemCount()")

        var query = "SELECT id FROM t_timelines WHERE user_id = ${userId}";
        if (userId == 0L) {
            query = "SELECT id FROM t_timelines WHERE home = 1"
        }
        var count = 0
        db.rawQuery(query, null).use {
            count = it.count
        }

        Log.d(TAG, "[END]getItemCount() -> ${count}")

        return count
    }
}

/**
 * TODO
 *
 * @property top
 * @property bottom
 */
class TweetScrollEvent(private val top: (() -> Unit)? = null, private val bottom: (() -> Unit)? = null) : RecyclerView.OnScrollListener()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetScrollEvent::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param recyclerView
     * @param dx
     * @param dy
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
    {
        super.onScrolled(recyclerView, dx, dy)
        Log.d(TAG, "[START]onScrolled(${recyclerView}, ${dx}, ${dy})")

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        if (layoutManager.findFirstVisibleItemPosition() == 0) {
            if (recyclerView.getChildAt(0).top == 0) {
                Log.d(TAG, "top()")
                top?.let { it() }
            }
        }
        if (recyclerView.adapter?.itemCount == layoutManager.findFirstVisibleItemPosition() + recyclerView.childCount) {
            Log.d(TAG, "bottom()")
            bottom?.let { it() }
        }
        Log.d(TAG, "[END]onScrolled(${recyclerView}, ${dx}, ${dy})")
    }
}
