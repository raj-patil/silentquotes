package com.silentquot.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.silentquot.socialcomponents.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eslem on 07/10/2014.
 */
public class MessageDAOImplSQLiteOpenHelper extends SQLiteOpenHelper implements MessagesDAO {
    private static int DATABASE_VERSION = 3;
    private String DATABASE_NAME = "chat";
    private static String TABLE_NAME = "messages";

    private String KEY_ID = "id";

    private String KEY_MESSAGE = "message";
    private String KEY_SENDER="sender";
    private  String KEY_RECEIVER="receiver";
    private String KEY_ISSEEN="isseen";
    private String KEY_MSGTYPE="msgtype";
    private  String KEY_CHATKEY="chatkey";
    private  String KEY_MSGID="msgid";
    private  String KEY_STATUS="status";
    private  String KEY_THUMBNAIL="thumbnail";

    public MessageDAOImplSQLiteOpenHelper(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
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
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_SENDER + " TEXT, "
                + KEY_RECEIVER + " TEXT, "
                + KEY_MESSAGE + " TEXT,"
                + KEY_ISSEEN + " INTEGER,"
                + KEY_CHATKEY + " STRING, "
                + KEY_MSGTYPE + " STRING,"
                + KEY_STATUS + " STRING,"
                + KEY_THUMBNAIL + " STRING,"
                + KEY_MSGID + " STRING)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    @Override
        public Message insert(Message message) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
  //      int own = (message.isOwn()) ? 1 : 0;

      contentValues.put(KEY_SENDER ,message.getSender());
        contentValues.put(KEY_RECEIVER ,message.getReceiver());
        contentValues.put(KEY_MESSAGE ,message.getMessage());
        contentValues.put(KEY_ISSEEN, message.isIsseen());
     contentValues.put(KEY_CHATKEY , message.getChat_key());
        contentValues.put(KEY_MSGTYPE , message.getMsgtype());
        contentValues.put(KEY_MSGID , message.getMsgid());
        contentValues.put(KEY_STATUS , message.getStatus());
        contentValues.put(KEY_THUMBNAIL , message.getThubmnail());

        int idInserted = (int) sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        message.setId(idInserted);
        sqLiteDatabase.close();
        return message;
    }

    @Override
    public Message get(int id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{KEY_SENDER, KEY_RECEIVER, KEY_MESSAGE, KEY_ISSEEN, KEY_MSGTYPE , KEY_CHATKEY},
                KEY_ID + "= ?", new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            Boolean isseen = (cursor.getInt(3) == 1) ? true : false;

            Message message = new Message(cursor.getString(0), cursor.getString(1), cursor.getString(2),isseen, cursor.getString(4),cursor.getString(5));
            return message;
        }
        return null;
    }

    @Override
    public boolean delete(int id) {
        SQLiteDatabase db =  this.getWritableDatabase();
        int delete = db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        if (delete == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Message> getAll() {
        List<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Boolean isseen = (cursor.getInt(3) == 1) ? true : false;
                Message message = new Message(cursor.getString(0), cursor.getString(1), cursor.getString(2),isseen, cursor.getString(4),cursor.getString(5));
                messages.add(message);

            } while (cursor.moveToNext());
            return messages;
        }

        db.close();
        return null;
    }

    @Override
    public List<Message> getAllConversation(String idConversation) {
        List<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_CHATKEY + "= ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { idConversation });

        if (cursor.moveToFirst()) {
            do {
                Boolean isseen = (cursor.getInt(4) == 1) ? true : false;
                Message message = new Message(cursor.getString(1), cursor.getString(2), cursor.getString(3),isseen, cursor.getString(5),cursor.getString(6));
                String msgid = (cursor.getString(9));
                String status = (cursor.getString(7));
                String thumbnail = (cursor.getString(8));
                message.setStatus(status);
                message.setMsgid(msgid);
                message.setThubmnail(thumbnail);
                messages.add(message);
            } while (cursor.moveToNext());
            return messages;
        }
       // cursor.close();
            db.close();
        return null;
    }

    @Override
    public boolean isMsgIdExist(String msgid) {


        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MSGID + "= ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { msgid });

        if (cursor.getCount() <=0)
        {
            cursor.close();
        return  false;
        }

        return true;
    }

    @Override
    public void updateStatus(Message message, OnUpdateComplete onUpdateComplete) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, message.getStatus());

        // updating row
        db.update(TABLE_NAME, values, KEY_MSGID + " = ?",
                new String[] { String.valueOf(message.getMsgid()) });

        onUpdateComplete.onComplete(true);
        db.close();
    }


    @Override
    public void updateIsseen(Message message, OnUpdateComplete onUpdateComplete) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_ISSEEN, message.isIsseen());

            // updating row
            db.update(TABLE_NAME, values, KEY_MSGID + " = ?",
                    new String[] { String.valueOf(message.getMsgid()) });

            onUpdateComplete.onComplete(true);
            db.close();
    }

    @Override
    public void updateMessage(Message message, OnUpdateComplete onUpdateComplete) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, message.getMessage());

        // updating row
        db.update(TABLE_NAME, values, KEY_MSGID + " = ?",
                new String[] { String.valueOf(message.getMsgid()) });

        onUpdateComplete.onComplete(true);
    }

    public void deleteAllData() {
        SQLiteDatabase sdb = this.getWritableDatabase();
        sdb.delete(TABLE_NAME, null, null);

    }
}
