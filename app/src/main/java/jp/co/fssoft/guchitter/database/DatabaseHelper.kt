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
                        screen_name TEXT NOT NULL,
                        oauth_token TEXT NOT NULL,
                        oauth_token_secret TEXT NOT NULL,
                        this INTEGER NOT NULL DEFAULT 0,
                        data JSON DEFAULT NULL,
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
                CREATE INDEX index_this ON t_users (this)
            """
        )
        db?.execSQL(
            """
                    CREATE TABLE t_timelines(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tweet_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        data JSON DEFAULT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_tweet_id ON t_timelines (tweet_id)
            """
        )
        db?.execSQL(
            """
                CREATE INDEX index_user_id ON t_timelines (user_id)
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        TODO("Not yet implemented")
    }
}