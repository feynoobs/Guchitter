package jp.co.fssoft.guchitter.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R

/**
 * TODO
 *
 */
class PostTweetActivity : AppCompatActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = PostTweetActivity::class.qualifiedName
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
        setContentView(R.layout.post_tweet_activity)
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
