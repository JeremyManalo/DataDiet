package jhmanalo.example.datadiet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.json.JSONObject;

public class ProductDbHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;
    Context ctx;
    static String DB_NAME = "PRODUCT_DATABASE";
    static String TABLE_NAME = "PRODUCT_TABLE";
    static int VERSION = 1;
    public static String strSeparator = "_,_";

    public ProductDbHelper(Context context) {
        super(context, DB_NAME, null, 1);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //CREATE TABLE NAME_TABLE (_id INTEGER PRIMARY KEY, FIRST_NAME STRING, LAST_NAME STRING);
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(_id INTEGER PRIMARY KEY, PRODUCT_NAME STRING, PRODUCT_URL STRING, WARNINGS STRING, LABELS STRING);");
        //Toast.makeText(ctx, "TABLE IS CREATED", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //DROP TABLE IF EXISTS NAME_TABLE;
        if(VERSION == oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
            VERSION = newVersion;
            onCreate(db);
            //Toast.makeText(ctx, "TABLE IS UPGRADED", Toast.LENGTH_LONG).show();
        }

    }

    public void insert(String name, String url, String warnings, String labels){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("PRODUCT_NAME", name);
        cv.put("PRODUCT_URL", url);
        cv.put("WARNINGS", warnings);
        cv.put("LABELS", labels);

        db.insert(TABLE_NAME, null, cv);
    }

    public Cursor getLatestProduct()
    {
        db = getReadableDatabase();
        String query = "SELECT * FROM "  + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        while(cursor.moveToNext())
        {
            //just moving cursor to the end
        }
        cursor.moveToPrevious();
        return cursor;
    }

    public Cursor getAllProducts()
    {
        db = getReadableDatabase();
        String query = "SELECT * FROM "  + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public void deleteName(String name) {
        db = getWritableDatabase();
        //db.delete(TABLE_NAME, "FIRST_NAME = ? AND LAST_NAME = ?", new String[]{firstname, lastname});
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE PRODUCT_NAME = \"" + name + "\";");
    }

    public void deleteURL(String url) {
        db = getWritableDatabase();
        //db.delete(TABLE_NAME, "FIRST_NAME = ? AND LAST_NAME = ?", new String[]{firstname, lastname});
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE PRODUCT_URL = \"" + url + "\";");
    }

    public int count() {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
        return c.getCount();
    }

    public void erase() {
        db = getWritableDatabase();
        onUpgrade(db, VERSION, 1);

    }
}
