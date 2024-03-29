package jp.co.fssoft.guchitter.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TweetObject
import jp.co.fssoft.guchitter.utility.Imager
import jp.co.fssoft.guchitter.utility.Utility
import java.io.FileInputStream

/**
 * TODO
 *
 * @constructor
 * TODO
 *
 * @param view
 */
class TweetWrapViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetWrapViewHolder::class.qualifiedName
    }

    /**
     *
     */
    val tweetsView: RecyclerView = view.findViewById(R.id.tweet_recycler_view)

}

/**
 * TODO
 *
 * @property userId
 * @property callback
 */
class TweetWrapRecyclerView(private val userId: Long, private val callback: (Long, ButtonType, Int, Int)->Unit) : RecyclerView.Adapter<TweetWrapViewHolder>()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetWrapRecyclerView::class.qualifiedName

        /**
         * TODO
         *
         * @property effect
         */
        enum class ButtonType(private val effect: Int)
        {
            FAVORITE(1),
            REMOVE_FAVORITE(2),
            RETWEET(3),
            REMOVE_RETWEET(4),
            SHARE(5),
            USER(6),
            REPLY(7),
            OTHER_MY(8),
            OTHER_OTHER(9)
        }
    }

    /**
     *
     */
    private var tweetObjects: List<List<TweetObject>> = listOf()

    private var childAdapters: MutableList<RecyclerView.Adapter<TweetViewHolder>> = mutableListOf()

    public fun setTweetObjects(tweetObjects: List<List<TweetObject>>)
    {
        if (this.tweetObjects.isEmpty() == false) {
            tweetObjects.forEach { inList ->
                this.tweetObjects.forEach { list ->

                }
            }
        }
        this.tweetObjects = tweetObjects
    }

    /**
     * TODO
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetWrapViewHolder
    {
        Log.d(TAG, "[START]onCreateViewHolder(${parent}, ${viewType})")
        return TweetWrapViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tweet_recycler_view, parent, false))
    }

    /**
     * TODO
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: TweetWrapViewHolder, position: Int) {
        Log.d(TAG, "[START]onBindViewHolder(${holder}, ${position})")
        holder.tweetsView.findViewById<RecyclerView>(R.id.tweet_recycler_view).apply {
            setHasFixedSize(true)
            val adp = TweetRecycleView(position, callback, userId)
            childAdapters.add(adp)
            adapter = adp
            (adapter as TweetRecycleView).tweetObjects = tweetObjects[position]
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
        return tweetObjects.count()
    }
}

/**
 * Tweet view holder
 *
 * @property view
 * @constructor Create empty Tweet view holder
 */
internal class TweetViewHolder(private val view: View) : RecyclerView.ViewHolder(view)
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
    val icon: ImageButton = view.findViewById(R.id.tweet_recycler_view_user_icon)

    /**
     *
     */
    val nameText: TextView = view.findViewById(R.id.tweet_recycler_view_user_name)

    /**
     *
     */
    val atNameText: TextView = view.findViewById(R.id.tweet_recycler_view_at_user_name)

    /**
     *
     */
    val mainText: TextView = view.findViewById(R.id.tweet_recycler_view_main)

    /**
     *
     */
    val timeText: TextView = view.findViewById(R.id.tweet_recycler_view_post_time)

    /**
     *
     */
    val replyBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_reply_button)

    /**
     *
     */
    val replyText: TextView = view.findViewById(R.id.tweet_recycler_view_reply_count)

    /**
     *
     */
    val retweetBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_retweet_button)

    /**
     *
     */
    val retweetText: TextView = view.findViewById(R.id.tweet_recycler_view_retweet_count)

    /**
     *
     */
    val favoriteBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_favorite_button)

    /**
     *
     */
    val favoriteText: TextView = view.findViewById(R.id.tweet_recycler_view_favorite_count)

    /**
     *
     */
    val transferBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_transfer_button)

    /**
     *
     */
    val otherBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_other_button)

    /**
     *
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_share_button)

    /**
     *
     */
    val upperLine: View = view.findViewById(R.id.tweet_recycler_view_upper_line)

    /**
     *
     */
    val lowerLine: View = view.findViewById(R.id.tweet_recycler_view_lower_line)

    /**
     *
     */
    private val space: Space = view.findViewById(R.id.tweet_recycler_view_space)

    /**
     *
     */
    val mediaLayout: LinearLayout = view.findViewById(R.id.tweet_media_layout)

    /**
     *
     */
    init
    {
        Log.d(TAG, "[START]init")

        val display = (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = DisplayMetrics()
        display.getRealMetrics(size)

        icon.layoutParams.width = (size.widthPixels * 0.16).toInt()
        icon.layoutParams.height = icon.layoutParams.width

        replyBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        replyBtn.layoutParams.height = replyBtn.layoutParams.width
        replyBtn.setImageResource(R.drawable.tweet_reply)

        replyText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        replyText.layoutParams.height = replyText.layoutParams.width / 3

        favoriteBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        favoriteBtn.layoutParams.height = favoriteBtn.layoutParams.width

        favoriteText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        favoriteText.layoutParams.height = favoriteText.layoutParams.width / 3

        retweetBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        retweetBtn.layoutParams.height = retweetBtn.layoutParams.width

        retweetText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        retweetText.layoutParams.height = retweetText.layoutParams.width / 3

        transferBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        transferBtn.layoutParams.height = transferBtn.layoutParams.width
        transferBtn.setImageResource(R.drawable.tweet_transfer)

        space.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        space.layoutParams.height = space.layoutParams.width / 3

        shareBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        shareBtn.layoutParams.height = shareBtn.layoutParams.width
        shareBtn.setImageResource(R.drawable.tweet_share)

        otherBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        otherBtn.layoutParams.height = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()

        nameText.layoutParams.width = (size.widthPixels * 0.84 * 0.36).toInt()

        atNameText.layoutParams.width = (size.widthPixels * 0.84 * 0.36).toInt()

        Log.d(TAG, "[END]init")
    }
}

/**
 * TODO
 *
 * @property parentPosition
 * @property callback
 */
internal class TweetRecycleView(private val parentPosition: Int, private val callback: (Long, TweetWrapRecyclerView.Companion.ButtonType, Int, Int)->Unit, private val userId: Long ) : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetRecycleView::class.qualifiedName
    }

    /**
     *
     */
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
        return TweetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tweet_recycler_view_item, parent, false))
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
        holder.nameText.text =
            if (tweet.retweetedTweet == null) {
                tweet.user?.name
            }
            else {
                tweet.retweetedTweet.user?.name
            }
        holder.atNameText.text =
            if (tweet.retweetedTweet == null) {
                tweet.user?.screen_name
            }
            else {
                tweet.retweetedTweet.user?.screen_name
            }
        holder.timeText.text = Utility.createFuzzyDateTime(tweet.createdAt)
        holder.nameText.setOnClickListener {
            if (tweet.retweetedTweet == null) {
                callback(tweet.user!!.id, TweetWrapRecyclerView.Companion.ButtonType.USER, parentPosition, position)
            }
            else {
                callback(tweet.retweetedTweet.user!!.id, TweetWrapRecyclerView.Companion.ButtonType.USER, parentPosition, position)
            }
        }
        holder.mainText.text =
            if (tweet.retweetedTweet == null) {
                tweet.text
            }
            else {
                tweet.retweetedTweet.text
            }
        if (tweet.retweetedTweet == null) {
            Imager().loadImage(holder.icon.context, tweet.user?.profileImageUrl!!, Imager.Companion.ImagePrefix.USER) {
                holder.icon.post {
                    holder.icon.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(FileInputStream(it))))
                }
            }
        }
        else {
            Imager().loadImage(holder.icon.context, tweet.retweetedTweet.user?.profileImageUrl!!, Imager.Companion.ImagePrefix.USER) {
                holder.icon.post {
                    holder.icon.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(FileInputStream(it))))
                }
            }
        }

        holder.replyBtn.setOnClickListener {
            callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.REPLY, parentPosition, position)
        }

        holder.icon.setOnClickListener {
            if (tweet.retweetedTweet == null) {
                callback(tweet.user!!.id, TweetWrapRecyclerView.Companion.ButtonType.USER, parentPosition, position)
            }
            else {
                callback(tweet.retweetedTweet.user!!.id, TweetWrapRecyclerView.Companion.ButtonType.USER, parentPosition, position)
            }
        }

        holder.favoriteBtn.setImageResource(R.drawable.tweet_favorite)
        if (tweet.isFavorited == true) {
            holder.favoriteBtn.setImageResource(R.drawable.tweet_favorited)
        }
        holder.favoriteBtn.setOnClickListener {
            if (tweet.isFavorited == true) {
                callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.REMOVE_FAVORITE, parentPosition, position)
            }
            else {
                callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.FAVORITE, parentPosition, position)
            }
        }
        holder.favoriteText.text = ""
        if (tweet.retweetedTweet == null) {
            if (tweet.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.favorites)
            }
        }
        else {
            if (tweet.retweetedTweet.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.retweetedTweet?.favorites)
            }
        }

        holder.retweetBtn.setImageResource(R.drawable.tweet_retweet)
        if (tweet.retweeted == true) {
            holder.retweetBtn.setImageResource(R.drawable.tweet_retweeted)
        }
        holder.retweetBtn.setOnClickListener {
            if (tweet.retweeted == true) {
                callback(tweet.retweetedTweet!!.id, TweetWrapRecyclerView.Companion.ButtonType.REMOVE_RETWEET, parentPosition, position)
            }
            else {
                callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.RETWEET, parentPosition, position)
            }
        }
        holder.otherBtn.setOnClickListener {
            if (tweet.retweeted == true) {
            }
            else {
                if (tweet.user?.id == userId) {
                    callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.OTHER_MY, parentPosition, position)
                }
                else {
                    callback(tweet.id, TweetWrapRecyclerView.Companion.ButtonType.OTHER_OTHER, parentPosition, position)
                }
            }
        }

        holder.retweetText.text = ""
        if (tweet.retweetedTweet == null) {
            if (tweet.retweets != 0) {
                holder.retweetText.text = String.format("%,d", tweet.retweets)
            }
        }
        else {
            if (tweet.retweetedTweet.retweets != 0) {
                holder.retweetText.text = String.format("%,d", tweet.retweetedTweet?.retweets)
            }
        }

        if (position == 0) {
            holder.upperLine.visibility = View.INVISIBLE
        }

        if (position == tweetObjects.size - 1) {
            holder.lowerLine.visibility = View.INVISIBLE
        }

        if (tweet.extendedEntities?.medias?.isEmpty() == false) {
            holder.mediaLayout.removeAllViews()
            val inflater = LayoutInflater.from(holder.mediaLayout.context)
             when (tweet.extendedEntities.medias.size) {
                1 ->
                    inflater.inflate(R.layout.tweet_recycler_view_item_one_photo, holder.mediaLayout)
                2 ->
                     inflater.inflate(R.layout.tweet_recycler_view_item_two_photo, holder.mediaLayout)
                3 ->
                    inflater.inflate(R.layout.tweet_recycler_view_item_three_photo, holder.mediaLayout)
                4 ->
                     inflater.inflate(R.layout.tweet_recycler_view_item_four_photo, holder.mediaLayout)
            }

            val display = (holder.mediaLayout.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = DisplayMetrics()
            display.getRealMetrics(size)

            tweet.extendedEntities.medias.forEachIndexed { index, mediaObject ->
                val item: ImageView? =
                    when (index) {
                        0 ->
                            holder.mediaLayout.findViewById(R.id.tweet_recycler_view_1st_image)
                        1 ->
                            holder.mediaLayout.findViewById(R.id.tweet_recycler_view_2nd_image)
                        2 ->
                            holder.mediaLayout.findViewById(R.id.tweet_recycler_view_3rd_image)
                        3 ->
                            holder.mediaLayout.findViewById(R.id.tweet_recycler_view_4th_image)
                        else ->
                            null
                    }
                Imager().loadImage(holder.mediaLayout.context, mediaObject.mediaUrl, Imager.Companion.ImagePrefix.PICTURE) {
                    val bitmap = BitmapFactory.decodeStream(FileInputStream(it))
                    item?.post {
                        item.layoutParams?.width = (size.widthPixels - holder.icon.layoutParams.width) / tweet.extendedEntities.medias.size
                        item.layoutParams?.height = item.layoutParams?.width!! * bitmap.height / bitmap.width
                        item.setImageBitmap(bitmap)
                    }
                }
            }
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
internal class TweetScrollEvent(private val top: ((()->Unit)->Unit)? = null, private val bottom: ((()->Unit)->Unit)? = null) : RecyclerView.OnScrollListener()
{
    companion object
    {
        /**
         *
         */
        private val TAG = TweetScrollEvent::class.qualifiedName

        /**
         *
         */
        private var topLock = false

        /**
         *
         */
        private var bottomLock = false
    }

    /**
     * TODO
     *
     */
    private fun topUnlock()
    {
        Log.d(TAG, "[START]topUnlock()")
        topLock = false
        Log.d(TAG, "[END]topUnlock()")
    }

    /**
     * TODO
     *
     */
    private fun bottomUnlock()
    {
        Log.d(TAG, "[START]bottomUnlock()")
        bottomLock = false
        Log.d(TAG, "[END]bottomUnlock()")
    }

    private fun reload(recyclerView: RecyclerView)
    {
        Log.d(TAG, "[START]reload(${recyclerView})")

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        if (layoutManager.findFirstVisibleItemPosition() == 0) {
            if (recyclerView.getChildAt(0).top == 0) {
                Log.d(TAG, "top()")
                top?.let {
                    if (topLock == false) {
                        topLock = true
                        it(::topUnlock)
                    }
                }
            }
        }
        if (recyclerView.adapter?.itemCount == layoutManager.findFirstVisibleItemPosition() + recyclerView.childCount) {
            Log.d(TAG, "bottom()")
            bottom?.let {
                if (bottomLock == false) {
                    bottomLock = true
                    it(::bottomUnlock)
                }
            }
        }
        Log.d(TAG, "[END]reload(${recyclerView})")
    }

    /**
     * TODO
     *
     * @param recyclerView
     * @param newState
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
    {
        super.onScrollStateChanged(recyclerView, newState)

        Log.d(TAG, "[START]onScrollStateChanged(${recyclerView}, ${newState})")
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            reload(recyclerView)
        }
        Log.d(TAG, "[START]onScrollStateChanged(${recyclerView}, ${newState})")
    }
}
