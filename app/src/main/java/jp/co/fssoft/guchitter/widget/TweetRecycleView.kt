package jp.co.fssoft.guchitter.widget

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.Space
import android.widget.TextView
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
    val transferBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_transfer_button)


    /**
     *
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycle_view_share_button)

    /**
     *
     */
    private val space: Space = view.findViewById(R.id.tweet_recycle_view_space)

    /**
     *
     */
    init
    {
        Log.d(TAG, "[START]init")

        val display = (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = DisplayMetrics()
        display.getRealMetrics(size)

        var params: ViewGroup.LayoutParams = icon.layoutParams
        params.width = (size.widthPixels * 0.2).toInt()
        params.height = params.width
        icon.layoutParams = params

        params = replyBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.25).toInt()
        params.height = params.width
        replyBtn.layoutParams = params
        replyBtn.setImageResource(R.drawable.tweet_reply)

        params = replyText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.75).toInt()
        params.height = params.width
        replyText.layoutParams = params

        params = favoriteBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.25).toInt()
        params.height = params.width
        favoriteBtn.layoutParams = params

        params = favoriteText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.75).toInt()
        params.height = params.width
        favoriteText.layoutParams = params

        params = retweetBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.25).toInt()
        params.height = params.width
        retweetBtn.layoutParams = params

        params = retweetText.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.75).toInt()
        params.height = params.width
        retweetText.layoutParams = params

        params = transferBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.25).toInt()
        params.height = params.width
        shareBtn.layoutParams = params
        transferBtn.setImageResource(R.drawable.tweet_transfer)

        params = space.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.75).toInt()
        params.height = params.width
        space.layoutParams = params

        params = shareBtn.layoutParams
        params.width = (size.widthPixels * 0.8 * 0.2 * 0.25).toInt()
        params.height = params.width
        shareBtn.layoutParams = params
        shareBtn.setImageResource(R.drawable.tweet_share)

        Log.d(TAG, "[END]init")
    }
}

/**
 * TODO
 *
 */
class TweetRecycleView(private val db: SQLiteDatabase, private val callback: (Long, ButtonType)->Unit) : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetRecycleView::class.qualifiedName

        /**
         * TODO
         *
         * @property effect
         */
        enum class ButtonType(private val effect: Int)
        {
            FAVORITE(1),
            RETWEET(2),
            SHARE(3),
            USER(4)
        }
    }

    public var tweetObjects: List<TweetObject> = mutableListOf()

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

        val tweet = tweetObjects[position]

        holder.nameText.text = tweet.user?.name
        holder.mainText.text =
            if (tweet.retweetedTweet == null) {
                tweet.text
            }
            else {
                tweet.retweetedTweet.text
            }
        val image: String =
            if (tweet.retweetedTweet == null) {
                "${Utility.Companion.ImagePrefix.USER}_${URLUtil.guessFileName(tweet.user?.profileImageUrl, null, null)}"
            }
            else {
                "${Utility.Companion.ImagePrefix.USER}_${URLUtil.guessFileName(tweet.retweetedTweet.user?.profileImageUrl, null, null)}"
            }
        holder.icon.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(holder.icon.context.openFileInput(image))))
        holder.icon.setOnClickListener {
            if (tweet.retweetedTweet == null) {
                callback(tweet.user!!.id, ButtonType.USER)
            }
            else {
                callback(tweet.retweetedTweet.user!!.id, ButtonType.USER)
            }
        }

        holder.favoriteBtn.setImageResource(R.drawable.tweet_favorite)
        if (tweet.isFavorited == true) {
            holder.favoriteBtn.setImageResource(R.drawable.tweet_favorited)
        }
        holder.favoriteBtn.setOnClickListener {
            callback(tweet.id, ButtonType.FAVORITE)
        }

        if (tweet.favorites != 0) {
            holder.favoriteText.text = tweet.favorites.toString()
        }

        holder.retweetBtn.setImageResource(R.drawable.tweet_retweet)
        if (tweet.retweeted == true) {
            holder.retweetBtn.setImageResource(R.drawable.tweet_retweeted)
        }
        holder.retweetBtn.setOnClickListener {
            callback(tweet.id, ButtonType.RETWEET)
        }

        if (tweet.retweets != 0) {
            holder.retweetText.text = tweet.retweets.toString()
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
        Log.d(TAG, "[START]getItemCount() -> ${tweetObjects.size}")

        return tweetObjects.size
    }
}

/**
 * TODO
 *
 * @property top
 * @property bottom
 */
class TweetScrollEvent(private val top: ((()->Unit)->Unit)? = null, private val bottom: ((()->Unit)->Unit)? = null) : RecyclerView.OnScrollListener()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetScrollEvent::class.qualifiedName

        private var lock = false
    }

    /**
     *
     */
    init {
        top?.let {
            if (lock == false) {
                lock = true
                it(::unlock)
            }
        }
    }

    private fun unlock()
    {
        Log.d(TAG, "[START]unlock()")
        lock = false
        Log.d(TAG, "[END]unlock()")
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
                top?.let {
                    if (lock == false) {
                        lock = true
                        it(::unlock)
                    }
                }
            }
        }
        if (recyclerView.adapter?.itemCount == layoutManager.findFirstVisibleItemPosition() + recyclerView.childCount) {
            Log.d(TAG, "bottom()")
            bottom?.let {
                if (lock == false) {
                    lock = true
                    it(::unlock)
                }
            }
        }
        Log.d(TAG, "[END]onScrolled(${recyclerView}, ${dx}, ${dy})")
    }
}
