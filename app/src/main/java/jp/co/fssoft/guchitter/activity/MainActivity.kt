package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.widget.TweetRecycleView
import jp.co.fssoft.guchitter.widget.TweetScrollEvent

class MainActivity : RootActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = MainActivity::class.qualifiedName
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
        setContentView(R.layout.main_activity)

        /***********************************************
         * ユーザーデータがあるか確認する
         * なければ認証
         */
        val db = database.readableDatabase
        db.rawQuery("SELECT * FROM t_users WHERE this = 1", null).use {
            if (it.count != 1) {
                startActivity(Intent(application, AuthenticationActivity::class.java))
            }
        }
        getPrevHomeTweet(database.writableDatabase)
        findViewById<RecyclerView>(R.id.tweet_recycle_view).apply {
            setHasFixedSize(true)
            adapter = TweetRecycleView(db, 0) {}
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(TweetScrollEvent())
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
