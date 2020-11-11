package com.silentquot.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.silentquot.Model.Chatlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eslem on 07/10/2014.
 */
public class ConversationDAOImplSQLiteOpenHelper extends SQLiteOpenHelper implements ConversationDAO{
    private static int DATABASE_VERSION=2;
    private String DATABASE_NAME="chat";
    private static String TABLE_NAME="conversations";

    private String KEY_ID="id";
    private String KEY_USERID="userid";
    private String KEY_USERNAME="username";
    private String KEY_LASTMESSAGE="lastMessage";
    private  String KEY_CHATTYPE="chattype";
    private String KEY_TIMESTAMP="timestamp";
    private  String KEY_CHATKEY="chatKey";
    private String KEY_FIMGURL="firebaseimgeurl";
    private String KEY_LIMGURL="localimgurl";

    public ConversationDAOImplSQLiteOpenHelper(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public Chatlist insert(Chatlist conversation) {
        SQLiteDatabase sqLiteDatabase= this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_USERID, conversation.getId());
        contentValues.put(KEY_CHATTYPE, conversation.getChat_type());
        contentValues.put(KEY_CHATKEY, conversation.getKey());
        contentValues.put(KEY_TIMESTAMP , conversation.getTimestamp());
        contentValues.put(KEY_LASTMESSAGE , conversation.getLastmessage());
        contentValues.put(KEY_USERNAME,conversation.getUsername());
        contentValues.put(KEY_FIMGURL,conversation.getFimgurl());
        contentValues.put(KEY_LIMGURL,conversation.getLimgurl());

        int idInserted = (int) sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        //conversation.setId(idInserted);
        sqLiteDatabase.close();
        return conversation;
    }

    @Override
    public Chatlist get(String id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{KEY_ID, KEY_USERID, KEY_CHATTYPE, KEY_CHATKEY , KEY_TIMESTAMP,KEY_LASTMESSAGE , KEY_USERNAME , KEY_FIMGURL , KEY_LIMGURL},
                KEY_USERID +"= ?", new String[]{id},
                null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            Chatlist conversation = new Chatlist(cursor.getString(1), cursor.getString(2) ,cursor.getString(3),cursor.getLong(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8) );

            return conversation;
        }
        return null;
    }

    public String getFimgurl(String chatId)
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{KEY_FIMGURL},
                KEY_USERID +"= ?", new String[]{chatId},
                null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            String fimgurl=cursor.getString(0);
          return  fimgurl;
        }
        return null;

    }

//    @Override
//    public Chatlist getFromUser(String idUser) {
//        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
//
//        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{KEY_ID, KEY_USERID, KEY_CHATTYPE,KEY_CHATKEY,KEY_TIMESTAMP, KEY_LASTMESSAGE},
//                KEY_USERID +"=?", new String[]{String.valueOf(idUser)},
//                null, null, null, null);
//        if(cursor != null && cursor.getCount()>0){
//            cursor.moveToFirst();
//            Conversation conversation = new Conversation(Integer.parseInt(cursor.getString(0)), idUser);
//            conversation.setUserName(cursor.getString(2));
//            conversation.setLastMessage(cursor.getString(3));
//            return conversation;
//        }
//        return null;
//    }

    @Override
    public boolean delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete= db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
        if(delete == 0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public List<Chatlist> getAll() {
        List<Chatlist> conversations = new ArrayList<Chatlist>();

        String query = "SELECT * FROM "+TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                Chatlist conversation = new Chatlist(cursor.getString(1), cursor.getString(2) ,cursor.getString(3),cursor.getLong(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8) );
//                conversation.set(cursor.getString(2))
//                if(cursor.getColumnCount()>3) {
//                    conversation.setLastMessage(cursor.getString(3));
//                }
                conversations.add(conversation);

            }while(cursor.moveToNext());
            return conversations;
        }
db.close();
        return null;
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"("+KEY_ID+" INTEGER PRIMARY KEY, "
                +KEY_USERID+ " INTEGER, "+KEY_CHATTYPE+" TEXT, "+KEY_CHATKEY+" TEXT , "+KEY_TIMESTAMP+" INTEGER, "+KEY_LASTMESSAGE+" TEXT,"+KEY_USERNAME+" TEXT, "+KEY_FIMGURL+" STRING,"+KEY_LIMGURL+" STRING )";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void deleteAllData() {
        SQLiteDatabase sdb = this.getWritableDatabase();
        sdb.delete(TABLE_NAME, null, null);

    }

    @Override
    public void update(Chatlist conversation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LASTMESSAGE, conversation.getLastmessage());
        values.put(KEY_TIMESTAMP , conversation.getTimestamp());

        // updating row
        db.update(TABLE_NAME, values, KEY_USERID + " = ?",
                new String[] { String.valueOf(conversation.getId()) });
    }

    public void updateLimg(String LimgPath, String fimgUrl , String chatid)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LIMGURL, LimgPath);
        values.put(KEY_FIMGURL ,fimgUrl);

        // updating row
        db.update(TABLE_NAME, values, KEY_USERID + " = ?",
                new String[] { chatid});

    }
}
