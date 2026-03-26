package com.example.secura.splashloginregister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// This is a conceptual class for educational purposes.
// It DOES NOT implement secure password handling (hashing/salting).
// NEVER store passwords in plain text or with simple hashing in a real application.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserCredentials.db";
    // IMPORTANT: Increment DATABASE_VERSION if you change the schema (e.g., add columns)
    // If you've already run the app with the previous schema, you MUST reinstall the app
    // on your device/emulator for the new schema to take effect, or implement proper migrations.
    private static final int DATABASE_VERSION = 2; // Incremented from 1 to 2

    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD_HASH = "password_hash"; // Placeholder for hashed password
    public static final String COL_SALT = "salt"; // Placeholder for salt
    public static final String COL_PROFILE_PIC_URI = "profile_pic_uri"; // New column for profile picture URI

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_NAME + " TEXT," +
                    COL_USERNAME + " TEXT UNIQUE," + // Username should be unique
                    COL_PASSWORD_HASH + " TEXT," +
                    COL_SALT + " TEXT," +
                    COL_PROFILE_PIC_URI + " TEXT)"; // Add profile picture URI column

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_USERS;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This simple onUpgrade drops the table and recreates it.
        // In a real application, you should handle schema migrations carefully
        // to preserve existing user data.
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public boolean insertUser(String name, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_USERNAME, username);
        // For educational purposes, storing plain text password.
        // In a real app: contentValues.put(COL_PASSWORD_HASH, hashPassword(password, salt));
        // contentValues.put(COL_SALT, salt);
        contentValues.put(COL_PASSWORD_HASH, password); // **SECURITY VULNERABILITY - DO NOT USE IN PRODUCTION**
        contentValues.put(COL_PROFILE_PIC_URI, ""); // Initialize with empty string

        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close(); // Close the database after insertion
        return result != -1; // If result is -1, insertion failed
    }


    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ? AND " + COL_PASSWORD_HASH + " = ?", new String[]{username, password});
            // In a real app: retrieve hashed password and salt, then verify password
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Do not close db here. It's managed by the SQLiteOpenHelper framework.
            // Closing it here might lead to "database is closed" errors if other operations
            // try to use it in the same context.
        }
    }

    /**
     * Retrieves user details (name, username, profile picture URI) for a given username.
     * @param username The username of the user to retrieve.
     * @return A Cursor containing the user's data, or null if not found.
     */
    public Cursor getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_NAME + ", " + COL_USERNAME + ", " + COL_PROFILE_PIC_URI + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user details: " + e.getMessage());
        }
        return cursor; // Return cursor, it will be closed by the caller
    }

    /**
     * Updates the name, username, and profile picture URI for a given user.
     * @param oldUsername The current username of the user to update.
     * @param newName The new name for the user.
     * @param newUsername The new username for the user.
     * @param newProfilePicUri The new profile picture URI for the user.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateUserDetails(String oldUsername, String newName, String newUsername, String newProfilePicUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, newName);
        contentValues.put(COL_USERNAME, newUsername);
        contentValues.put(COL_PROFILE_PIC_URI, newProfilePicUri);

        // Update based on the old username
        int result = db.update(TABLE_USERS, contentValues, COL_USERNAME + " = ?", new String[]{oldUsername});
        db.close();
        return result > 0;
    }

    /**
     * Retrieves the password hash for a given username.
     * This is used to maintain the same password when updating other user details.
     * @param username The username to query.
     * @return The password hash as a String, or null if the user is not found.
     */
    public String getPasswordHash(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String passwordHash = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_PASSWORD_HASH + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting password hash: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Do not close db here.
        }
        return passwordHash;
    }

    // --- NEW METHODS ADDED BELOW THIS LINE ---

    /**
     * Retrieves all user details (name, username, password hash) from the database.
     * This method is for administrative purposes to display all registered accounts.
     * In a real application, password hashes should NEVER be exposed directly.
     *
     * @return A Cursor containing all users' name, username, and password hash.
     * The caller is responsible for closing the cursor.
     */
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_NAME + ", " + COL_USERNAME + ", " + COL_PASSWORD_HASH + " FROM " + TABLE_USERS, null);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all users: " + e.getMessage());
        }
        return cursor; // Return cursor, it will be closed by the caller
    }

    /**
     * Updates the name, username, and password for a given user identified by their old username.
     * This method allows full credential modification.
     *
     * @param oldUsername The current username of the user to update.
     * @param newName The new name for the user.
     * @param newUsername The new username for the user.
     * @param newPassword The new password (plain text for this conceptual app).
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateUserCredentials(String oldUsername, String newName, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, newName);
        contentValues.put(COL_USERNAME, newUsername);
        contentValues.put(COL_PASSWORD_HASH, newPassword); // **SECURITY VULNERABILITY - DO NOT USE IN PRODUCTION**

        // Update based on the old username
        int result = db.update(TABLE_USERS, contentValues, COL_USERNAME + " = ?", new String[]{oldUsername});
        db.close();
        return result > 0;
    }

    /**
     * Deletes a user from the database based on their username.
     *
     * @param username The username of the user to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COL_USERNAME + " = ?", new String[]{username});
        db.close();
        return result > 0;
    }
}