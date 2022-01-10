package jp.co.fssoft.guchitter.activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TwitterApiUpdate
import jp.co.fssoft.guchitter.database.DatabaseHelper

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
        }

        findViewById<Button>(R.id.tweet_send_btn).setOnClickListener {
            val params = mapOf(
                "status" to findViewById<EditText>(R.id.tweet_body).text.toString(),
                "display_coordinates" to false.toString()
            )
            TwitterApiUpdate().start(DatabaseHelper(applicationContext).readableDatabase, params)
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
