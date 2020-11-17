package jp.co.fssoft.guchitter.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, "guchitter.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?)
    {
        db?.execSQL(
            """
                    CREATE TABLE t_users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        oauth_token TEXT DEFAULT NULL,
                        oauth_token_secret TEXT DEFAULT NULL,
                        my INTEGER DEFAULT NULL,
                        current INTEGER DEFAULT NULL,
                        data JSON,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL 
                    )
                """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_user_id ON t_users (user_id)
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_my ON t_users (my)
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_current ON t_users (current)
            """
        )
        db?.execSQL(
            """
                    CREATE TABLE t_time_lines(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tweet_id INTEGER NOT NULL,
                        reply_tweet_id INTEGER DEFAULT NULL,
                        user_id INTEGER NOT NULL,
                        data JSON,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_tweet_id ON t_time_lines (tweet_id)
            """
        )
        db?.execSQL(
            """
                CREATE INDEX index_reply_tweet_id ON t_time_lines (reply_tweet_id)
            """
        )
        db?.execSQL(
            """
                CREATE INDEX index_user_id ON t_time_lines (user_id)
            """
        )
        db?.execSQL(
            """
                    CREATE TABLE r_home_tweets(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        tweet_id INTEGER NOT NULL,
                        my INTEGER DEFAULT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_user_id_tweet_id ON r_home_tweets (user_id, tweet_id)
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        TODO("Not yet implemented")
    }
}