package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.service.PostTweetService

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
     * Images
     */
    private var keepImages: ArrayList<String> = arrayListOf()

    /**
     * Launcher
     */
    private val launcher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        Log.e(TAG, it.toString())
        keepImages.clear()
        it.forEach {
            keepImages.add(it.toString())
        }
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
        findViewById<Button>(R.id.tweet_image_btn).setOnClickListener {
            launcher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
        }

        findViewById<Button>(R.id.tweet_send_btn).setOnClickListener {
            val serviceIntent = Intent(applicationContext, PostTweetService::class.java).apply {
                putExtra("status", bodyTextView.text.toString())
                putStringArrayListExtra("images", keepImages)
                val replyId = intent.getLongExtra("in_reply_to_status_id", -1L)
                if (replyId != -1L) {
                    putExtra("in_reply_to_status_id", replyId)
                }
            }
            startService(serviceIntent)
            finish()
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
