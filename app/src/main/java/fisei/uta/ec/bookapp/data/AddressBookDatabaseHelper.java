package fisei.uta.ec.bookapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fisei.uta.ec.bookapp.data.DatabaseDescription.Contact;

class AddressBookDatabaseHelper  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AddressBook.db";
    private static final int DATABASE_VERSION = 2;

    public AddressBookDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        final String CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + Contact.TABLE_NAME + "(" +
                        Contact._ID + " INTEGER PRIMARY KEY, " +
                        Contact.COLUMN_NAME + " TEXT, " +
                        Contact.COLUMN_PHONE + " TEXT, " +
                        Contact.COLUMN_EMAIL + " TEXT, " +
                        Contact.COLUMN_STREET + " TEXT, " +
                        Contact.COLUMN_CITY + " TEXT, " +
                        Contact.COLUMN_STATE + " TEXT, " +
                        Contact.COLUMN_ZIP + " TEXT);";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + Contact.TABLE_NAME);
        onCreate(db);
    }

}
