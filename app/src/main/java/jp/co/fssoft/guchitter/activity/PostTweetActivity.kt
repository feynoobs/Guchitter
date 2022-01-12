package jp.co.fssoft.guchitter.activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
        val bodyTextView = findViewById<EditText>(R.id.tweet_body)
        window.decorView.findViewById<View>(android.R.id.content).viewTreeObserver.addOnDrawListener {
            val r = Rect()
            window.decorView.getWindowVisibleDisplayFrame(r)
            val height = r.bottom - r.top - findViewById<LinearLayout>(R.id.tweet_send_bar1).height
            if (bodyTextView.height != height) {
                if (height != 0) {
                    bodyTextView.height = height
                }
            }
        }

        findViewById<Button>(R.id.tweet_send_btn).setOnClickListener {
            val params = mapOf(
                "status" to bodyTextView.text.toString(),
                "display_coordinates" to false.toString()
            )
            val progress = findViewById<ProgressBar>(R.id.tweet_progress)
            progress.visibility = View.VISIBLE
            TwitterApiUpdate(DatabaseHelper(applicationContext).readableDatabase).start(params).callback =  {
                runOnUiThread {
                    progress.visibility = View.INVISIBLE
                    Log.e(TAG, "${it}")
                    it ?: run {
                        Toast.makeText(applicationContext, getString(R.string.post_tweet_fail), Toast.LENGTH_LONG).show()
                    }
                    finish()
                }
            }
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
