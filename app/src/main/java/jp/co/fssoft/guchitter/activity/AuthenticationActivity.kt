package jp.co.fssoft.guchitter.activity

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TwitterApiAccessToken
import jp.co.fssoft.guchitter.api.TwitterApiCommon
import jp.co.fssoft.guchitter.api.TwitterApiRequestToken
import jp.co.fssoft.guchitter.api.TwitterApiUsersShow
import jp.co.fssoft.guchitter.database.DatabaseHelper
import jp.co.fssoft.guchitter.utility.Utility

/**
 * TODO
 *
 */
class AuthenticationActivity : AppCompatActivity()
{

    companion object
    {
        /**
         *
         */
        private val TAG = AuthenticationActivity::class.qualifiedName
    }

    /**
     *
     */
    private val database by lazy {
        DatabaseHelper(applicationContext)
    }

    /**
     * TODO
     *
     * @param url
     * @param token
     */
    private fun shouldOverrideUrlLoadingCommon(url: String, token: Map<String, String>)
    {
        Log.d(TAG, "[START]shouldOverrideUrlLoadingCommon(${url}, ${token})")
        val query = url.replace("${TwitterApiCommon.CALLBACK_URL}?", "")
        val resultMap = Utility.splitQuery(query).toMutableMap()
        resultMap["oauth_token_secret"] = token["oauth_token_secret"] as String
        TwitterApiAccessToken(database.readableDatabase).start(resultMap).callback = {
            val resultMap = Utility.splitQuery(it!!).toMutableMap()
            resultMap.remove("screen_name")
            TwitterApiUsersShow(database.writableDatabase).start(resultMap).callback = {
                finish()
            }
        }
        Log.d(TAG, "[END]shouldOverrideUrlLoadingCommon(${url}, ${token})")
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
        setContentView(R.layout.authentication_activity)
        val webView: WebView = findViewById(R.id.authentication_view)
        TwitterApiRequestToken(database.readableDatabase).start().callback = {
            val token = Utility.splitQuery(it!!)

            runOnUiThread {
                webView.webViewClient = object : WebViewClient() {
                    /**
                     * TODO
                     *
                     * @param view
                     * @param url
                     * @return
                     */
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        Log.d(TAG, "[START]shouldOverrideUrlLoading(${view}, ${url})")
                        shouldOverrideUrlLoadingCommon(url!!, token)
                        Log.d(TAG, "[END]shouldOverrideUrlLoading(${view}, ${url})")
                        return true
                    }

                    /**
                     * TODO
                     *
                     * @param view
                     * @param request
                     * @return
                     */
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        Log.d(TAG, "[START]shouldOverrideUrlLoading(${view}, ${request})")
                        shouldOverrideUrlLoadingCommon(request!!.url.toString(), token)
                        Log.d(TAG, "[END]shouldOverrideUrlLoading(${view}, ${request})")
                        return true
                    }
                }
                webView.loadUrl("https://api.twitter.com/oauth/authorize?oauth_token=${token["oauth_token"]}")
            }
        }

        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
