package ua.com.spottertest.spotter2.frontend;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.database.DataBaseHelper;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText loginET, firstPassET, secondPassET;
    private CheckBox loginCB;
    private Spinner loginSpinner;
    private DataBaseHelper DBHelper;
    private ArrayAdapter<String> adapter;
    private List<String> usernames;
    private Button loginButton;
    private Intent selectActIntent;
    private Toolbar toolbar;

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

        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Увійдіть в обліковий запис, або створіть новий");

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loginCB:
                boolean isChecked = loginCB.isChecked();
                    loginET.setEnabled(!isChecked);
                    secondPassET.setEnabled(!isChecked);
                    loginSpinner.setEnabled(isChecked);
                break;
            case R.id.loginButt:
                if (!loginCB.isChecked()){
                    String userName = loginET.getText().toString();
                    String password = firstPassET.getText().toString();
                    String repeatingPassword = secondPassET.getText().toString();

                    if (userName.equals("") || password.equals("")
                            || repeatingPassword.equals(""))
                        makeToastMessage(getResources().getString(R.string.loginActInputAllToastMessage));
                    else {
                        if (!password.equals(repeatingPassword))
                            makeToastMessage(getResources().getString(R.string.loginActSecondPassErrorMessage));
                        else {
                            if (!DBHelper.insertUser(userName, password))
                                makeToastMessage(getResources().getString(R.string.loginActDublicateLoginErrorMessage));
                            else {
                                selectActIntent.putExtra("userName", userName);
                                startActivity(selectActIntent);
                                finish();
                            }

                        }
                    }
                }
                else {
                    if(loginSpinner.getSelectedItem() == null) makeToastMessage(getResources()
                            .getString(R.string.loginActSelectLoginMessage));
                    else {
                        String userName = loginSpinner.getSelectedItem().toString();
                        String password = firstPassET.getText().toString();
                        if (password.equals("")) makeToastMessage(getResources().getString(R.string.loginActInputPassMessage));
                        else {
                            if(DBHelper.checkPasswordForUser(userName, password)){
                                selectActIntent.putExtra("userName", userName);
                                startActivity(selectActIntent);
                                finish();
                            }
                            else makeToastMessage(getResources().getString(R.string.loginActWrongPassMessage));
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
