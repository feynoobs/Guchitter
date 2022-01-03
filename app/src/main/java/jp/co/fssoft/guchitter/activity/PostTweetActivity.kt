package jp.co.fssoft.guchitter.activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
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
        window.decorView.findViewById<View>(android.R.id.content).viewTreeObserver.addOnDrawListener {
            val r = Rect()
            window.decorView.getWindowVisibleDisplayFrame(r)
            findViewById<EditText>(R.id.tweet_body).height = r.bottom - r.top
            Log.e(TAG, r.toString())
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
