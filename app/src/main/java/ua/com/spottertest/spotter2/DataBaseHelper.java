package ua.com.spottertest.spotter2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolf on 04.03.2017.
 * Оболочка-адаптер для работы с SQLite таблицей
 */
public class DataBaseHelper extends SQLiteOpenHelper{

    /*Название бд*/

    private static final String DB_NAME = "SPOTTER_DATABASE";

    /*Название таблицы*/

    private static final String USERS_TABLE_NAME = "USERS";

    /*Версия бд*/

    private static final int DB_VERSION = 1;

    /*Названия колонок
    * имя юзера, пароль, количество решенных задач, количество успешных задач, процент успешных задач*/

    private static final String USERNAME = "NAME";
    private static final String PASSWORD = "PASSWORD";

    /* Выполненые задания, выполненые успешно, неуспешно и процент выполненых соответственно*/

    private static final String TASKS = "TASKS";
    private static final String SUCCESS_TASKS = "SUCCESS_TASKS";
    private static final String UNSUCCESS_TASKS = "UNSUCCESS_TASKS";
    private static final String PERCENTAGE_OF_SUCCESS = "PERCENTAGE_OF_SUCCESS";
    private static final String AVERAGE_TIME = "AVERAGE_TIME";


    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        /*Создаем таблицу с 7-ю соответствующими колонками*/

        db.execSQL("CREATE TABLE " + USERS_TABLE_NAME+ " ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME + " TEXT, " +
                PASSWORD + " TEXT, " +
                TASKS + " INTEGER, " +
                SUCCESS_TASKS + " INTEGER, " +
                UNSUCCESS_TASKS + " INTEGER, " +
                PERCENTAGE_OF_SUCCESS + " INTEGER, " +
                AVERAGE_TIME + " LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        /*для тестирования, обнуляем таблицу каждый раз*/
/*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);

        // Create tables again
        onCreate(db);
        */
    }

    /*Метод вставляет нового юзера в таблицу с нулевой статистикой*/

    public boolean insertUser(String userName, String password){
        if (this.getAllUsersNames().contains(userName)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, userName);
        values.put(PASSWORD, password);
        values.put(TASKS, 0);
        values.put(SUCCESS_TASKS, 0);
        values.put(UNSUCCESS_TASKS, 0);
        values.put(PERCENTAGE_OF_SUCCESS, 0);
        values.put(AVERAGE_TIME, 0);

        db.insert(USERS_TABLE_NAME, null, values);
        db.close();
        return true;
    }

    /*Метод возвращает List<String> всех имен юзеров в бд*/

    public List<String> getAllUsersNames(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> userNames = new ArrayList<>();
        Cursor cursor = db.query(USERS_TABLE_NAME, new String[]{USERNAME}, null, null, null, null, null);
        if (cursor.moveToFirst()){
            do {
                userNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return userNames;
    }

    /*метод принимает имя, возвращает обьект User с инкапсуллироваными данными по юзеру из бд.
    * при отсутствии переданого имени в бд, возвращает null*/

    public User getUserForName(String userName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USERS_TABLE_NAME, new String[]{USERNAME, TASKS,
                SUCCESS_TASKS, UNSUCCESS_TASKS, AVERAGE_TIME}
                ,USERNAME + " = ?", new String[]{userName}, null, null, null);
        if (cursor.moveToFirst()){
            User user = new User();
            user.setName(cursor.getString(0));
            user.setTasks(cursor.getInt(1));
            user.setSuccessTasks(cursor.getInt(2));
            user.setUnsuccessTasks(cursor.getInt(3));
            user.refreshPercentageOfSuccess();
            user.setAverigeTime(cursor.getLong(4));

            cursor.close();
            db.close();

            return user;
        }

        cursor.close();
        db.close();
        return null;
    }

    /*Метод принимает обьект User с статистикой текущей сессии и обновляет бд*/

    public void refreshUserStats(User user){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            Cursor cursor = db.query(USERS_TABLE_NAME, new String[]{TASKS, SUCCESS_TASKS, UNSUCCESS_TASKS, AVERAGE_TIME}
                    , USERNAME + " = ?", new String[]{user.getName()}, null, null, null);
            cursor.moveToFirst();
            user.setTasks(user.getTasks() + cursor.getInt(0));
            user.setSuccessTasks(user.getSuccessTasks() + cursor.getInt(1));
            user.setUnsuccessTasks(user.getUnsuccessTasks() + cursor.getInt(2));
            user.setAverigeTime((user.getAverigeTime() + cursor.getLong(3))/2);
            user.refreshPercentageOfSuccess();

            values.put(TASKS, user.getTasks());
            values.put(SUCCESS_TASKS, user.getSuccessTasks());
            values.put(UNSUCCESS_TASKS, user.getUnsuccessTasks());
            values.put(PERCENTAGE_OF_SUCCESS, user.getPercentageOfSuccess());
            values.put(AVERAGE_TIME, user.getAverigeTime());

            db.update(USERS_TABLE_NAME, values, USERNAME + " = ?", new String[]{user.getName()});

            cursor.close();
            db.close();

    }

    /*Метод принимает имя и пароль существующего юзера и возвращает булеву правильность соответствия
    * указаного пароля для юзера в бд и переданого*/
    public boolean checkPasswordForUser(String userName, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isCorrect = false;
        Cursor cursor = db.query(USERS_TABLE_NAME, new String[]{PASSWORD}
                ,USERNAME + " = ?", new String[]{userName}, null, null, null);
        if (cursor.moveToFirst()) {
            String tablePass = cursor.getString(0);
            isCorrect = password.equals(tablePass);
        }
        return isCorrect;
    }

    public void removeUser(String userName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE_NAME, USERNAME + " = ?", new String[]{userName});
        db.close();
    }
}
