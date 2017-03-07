package ua.com.spottertest.spotter2;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText loginET, firstPassET, secondPassET;
    private CheckBox loginCB;
    private Spinner loginSpinner;
    private DataBaseHelper DBHelper;
    private ArrayAdapter<String> adapter;
    private List<String> usernames;
    private Button loginButton;
    private Intent selectActIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DBHelper = new DataBaseHelper(this);

        DBHelper.insertUser("Anton", "aaaa");
        DBHelper.insertUser("Vasili", "vvvv");
        DBHelper.insertUser("Batia", "bbbb");

        selectActIntent = new Intent(this,  SelectionActivity.class);

        loginButton = (Button) findViewById(R.id.loginButt);
        loginButton.setOnClickListener(this);
        loginET = (EditText) findViewById(R.id.loginET);
        firstPassET = (EditText) findViewById(R.id.firstPassET);
        secondPassET = (EditText) findViewById(R.id.secondPassET);
        loginCB = (CheckBox) findViewById(R.id.loginCB);

        loginCB.setOnClickListener(this);

        loginSpinner = (Spinner) findViewById(R.id.loginSpinner);

        usernames = DBHelper.getAllUsersNames();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, usernames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loginSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loginCB:
                if (!loginCB.isChecked()){
                    loginET.setClickable(true);
                    loginET.setCursorVisible(true);
                    loginET.setFocusable(true);
                    loginET.setFocusableInTouchMode(true);
                    loginET.setVisibility(View.VISIBLE);
                    secondPassET.setClickable(true);
                    secondPassET.setCursorVisible(true);
                    secondPassET.setFocusable(true);
                    secondPassET.setFocusableInTouchMode(true);
                    secondPassET.setVisibility(View.VISIBLE);
                }
                else {
                    loginET.setClickable(false);
                    loginET.setCursorVisible(false);
                    loginET.setFocusable(false);
                    loginET.setFocusableInTouchMode(false);
                    loginET.setVisibility(View.INVISIBLE);
                    secondPassET.setClickable(false);
                    secondPassET.setCursorVisible(false);
                    secondPassET.setFocusable(false);
                    secondPassET.setFocusableInTouchMode(false);
                    secondPassET.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.loginButt:
                if (!loginCB.isChecked()){
                    String userName = loginET.getText().toString();
                    String password = firstPassET.getText().toString();
                    String repeatingPassword = secondPassET.getText().toString();

                    if (userName.equals("") || password.equals("")
                            || repeatingPassword.equals(""))
                        makeToastMessage("Для створення нового облікового запису заповніть всі поля");
                    else {
                        if (!password.equals(repeatingPassword))
                            makeToastMessage("При повторному введені паролю допущено помилку");
                        else {
                            if (!DBHelper.insertUser(userName, password))
                                makeToastMessage("Такий позивний вже зареєстровано");
                            else {
                                selectActIntent.putExtra("userName", userName);
                                startActivity(selectActIntent);
                                finish();
                            }

                        }
                    }
                }
                else {
                    if(loginSpinner.getSelectedItem() == null) makeToastMessage("Виберіть позивний");
                    else {
                        String userName = loginSpinner.getSelectedItem().toString();
                        String password = firstPassET.getText().toString();
                        if (password.equals("")) makeToastMessage("Введіть пароль");
                        else {
                            if(DBHelper.checkPasswordForUser(userName, password)){
                                selectActIntent.putExtra("userName", userName);
                                startActivity(selectActIntent);
                                finish();
                            }
                            else makeToastMessage("Невірний пароль!");
                        }
                    }


                }
                break;
        }
    }

    private void makeToastMessage(String message){
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }
}
