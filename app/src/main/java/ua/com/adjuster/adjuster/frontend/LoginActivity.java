package ua.com.adjuster.adjuster.frontend;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import ua.com.adjuster.adjuster.R;
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;

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
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("Увійдіть в обліковий запис, або створіть новий");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginInfoMenuItem:
                String message = getString(R.string.loginActInfoMessage);
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_info_black_24dp);
                makeDialogWindowMessage( "Інфо",
                        message, drawable);
        }
        return super.onOptionsItemSelected(item);
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

    private void makeDialogWindowMessage(String title, String message, Drawable drawable){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(false).setNegativeButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setIcon(drawable);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
