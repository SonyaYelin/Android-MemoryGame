package memoryGame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

import memoryGame.bl.Score;

public class Database extends SQLiteOpenHelper{

    public static final int     DATABASE_VERSION = 1;
    public static final String  DATABASE_NAME = "database.db";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_HIGHSCORE);
        db.execSQL(SQL_CREATE_TABLE);
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FeedScore.TABLE_NAME + " (" +
                    FeedScore._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedScore.COLUMN_NAME_NAME + " TEXT," +
                    FeedScore.COLUMN_NAME_SCORE + " INTEGER," +
                    FeedScore.COLUMN_NAME_LATITUDE + " REAL," +
                    FeedScore.COLUMN_NAME_LONGITUDE + " REAL," +
                    FeedScore.COLUMN_NAME_LOCATION + " TEXT" +
                    " );";

    private static final String SQL_DELETE_HIGHSCORE =
            "DROP TABLE IF EXISTS " + FeedScore.TABLE_NAME;

    public void addScores (Score score){
        ContentValues values = new ContentValues();
        values.put(FeedScore.COLUMN_NAME_NAME, score.getName());
        values.put(FeedScore.COLUMN_NAME_SCORE,score.getValue());
        values.put(FeedScore.COLUMN_NAME_LATITUDE,score.getLocation().latitude);
        values.put(FeedScore.COLUMN_NAME_LONGITUDE,score.getLocation().longitude);
        values.put(FeedScore.COLUMN_NAME_LOCATION,score.getStrLocation());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(FeedScore.TABLE_NAME,null,values);
        trimToTopTen(db);
        db.close();
    }

    private void trimToTopTen (SQLiteDatabase db){

        final String DELETE_LAST = "DELETE FROM " + FeedScore.TABLE_NAME +
                " WHERE " + FeedScore._ID + " NOT IN ( SELECT " + FeedScore._ID +
                " FROM " + FeedScore.TABLE_NAME +
                " ORDER BY " + FeedScore.COLUMN_NAME_SCORE + " DESC" +
                " LIMIT 10" + ")";

        db.execSQL(DELETE_LAST);
    }

    public ArrayList<Score> getScoreList (){
        ArrayList <Score> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'highScore'", null);
        if (c==null)
            db.execSQL(SQL_CREATE_TABLE);

        String[] projection = {FeedScore.COLUMN_NAME_NAME,FeedScore.COLUMN_NAME_SCORE,
                                FeedScore.COLUMN_NAME_LATITUDE,FeedScore.COLUMN_NAME_LONGITUDE, FeedScore.COLUMN_NAME_LOCATION};

        String selection = FeedScore.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { "*" };
        String sortOrder = FeedScore.COLUMN_NAME_SCORE + " DESC";
        Cursor cursor = db.query(FeedScore.TABLE_NAME,projection,null,null,null,null,sortOrder);

        while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(FeedScore.COLUMN_NAME_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(FeedScore.COLUMN_NAME_SCORE));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedScore.COLUMN_NAME_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedScore.COLUMN_NAME_LONGITUDE));
                String strLocation = cursor.getString(cursor.getColumnIndexOrThrow(FeedScore.COLUMN_NAME_LOCATION));
                LatLng location = new LatLng(latitude, longitude);
                list.add(new Score(score, name, location, strLocation));
            }
            cursor.close();
        return list;
    }

    public class FeedScore implements BaseColumns {
        private static final String TABLE_NAME = "ScoreTable";
        private static final String COLUMN_NAME_NAME = "name";
        private static final String COLUMN_NAME_SCORE = "score";
        private static final String COLUMN_NAME_LATITUDE = "latitude";
        private static final String COLUMN_NAME_LONGITUDE = "longitude";
        private static final String COLUMN_NAME_LOCATION = "location";
    }
}