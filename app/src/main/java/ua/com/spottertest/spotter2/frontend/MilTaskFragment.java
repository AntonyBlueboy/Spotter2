package ua.com.spottertest.spotter2.frontend;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.database.User;
import ua.com.spottertest.spotter2.core.mils.ArtilleryMilsUtil;
import ua.com.spottertest.spotter2.core.mils.MilsTaskTrainer;
import ua.com.spottertest.spotter2.core.mils.NotMilsFormatException;


/**
 * A simple {@link Fragment} subclass.
 * Фрагмент для решения задач по тысячным
 */
public class MilTaskFragment extends Fragment implements View.OnClickListener{

    int taskId;

    /*Переменные текущей фабрики задач, статистики юзера и булевая "Решались ли задачи в фрагменте"*/

    private MilsTaskTrainer milsTaskTrainer;
    private User currentUser;
    private boolean isUsed = false;

    /*Переменные необходимые для вывода статистики тут, в отличии от User не очищаются при отображении глобальной статы*/

    private int tasks = 0;
    private int succesfulTasks = 0;
    private double percent = 0.0;

    private TextView distMilTaskDescripTV,distanceMilsStatTV;
    private EditText distMilTaskET;
    private Button distanceMilsStartButton, distanceMilsCheckButton;

    private boolean isChacked = false;

    public MilTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mil_task, container, false);

    }

   /*Метод жизненного цикла стартующий сразу после создания активити и фрагмента
    * В нем инициируем все вью и переменную логина*/

    @Override
    public void onStart() {
        super.onStart();
        distMilTaskDescripTV = (TextView) getView().findViewById(R.id.distMilTaskDescripTV);
        distMilTaskET = (EditText) getView().findViewById(R.id.distMilTaskET);
        distanceMilsStartButton = (Button) getView().findViewById(R.id.distanceMilsStartButton);
        distanceMilsStartButton.setOnClickListener(this);
        distanceMilsCheckButton = (Button) getView().findViewById(R.id.distanceMilsCheckButton);
        distanceMilsCheckButton.setOnClickListener(this);
        distanceMilsStatTV = (TextView) getView().findViewById(R.id.distanceMilsStatTV);
        milsTaskTrainer = new MilsTaskTrainer();
        distMilTaskET.setEnabled(false);
        if (taskId == MilsTaskTrainer.SIZE_TASK) distMilTaskET.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        else if(taskId == MilsTaskTrainer.ANGLE_TASK) distMilTaskET.setInputType(InputType.TYPE_CLASS_TEXT);



        /*Текствью для вывода статистики*/


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.distanceMilsStartButton:
                /*При нажатии кнопки получения задачи
                * ставим флаг "проверено" в false
                * Выводим условие задачи в тектсвью
                * регулирем доступность кнопок и поля ввода*/
                isChacked = false;
                switch (taskId) {
                    case MilsTaskTrainer.DISTANCE_TASK:
                        distMilTaskDescripTV.setText(milsTaskTrainer.getDistanceTask());
                        distMilTaskET.setHint("Введіть цілі метри");
                        break;
                    case MilsTaskTrainer.ANGLE_TASK:
                        distMilTaskDescripTV.setText(milsTaskTrainer.getAngleTask());
                        distMilTaskET.setHint("Введіть тисячні");
                        break;
                    case MilsTaskTrainer.SIZE_TASK:
                        distMilTaskDescripTV.setText(milsTaskTrainer.getSizeTask());
                        distMilTaskET.setHint("Введіть метри");
                }
                distanceMilsCheckButton.setEnabled(!isChacked);
                distanceMilsStartButton.setEnabled(isChacked);
                distMilTaskET.setEnabled(true);
                break;
            case R.id.distanceMilsCheckButton:
                /*При нажатии кнопки проверки
                * создаем переменную ответа
                * получаем ответ, и высталяем флаг isSuccesful и isChecked
                * регулируем доступность кнопок и поля, очищаем поле и вью с условием возвращаем в первозданный вид*/
                boolean isSuccesful = false;
                try {
                    switch (taskId){
                        case MilsTaskTrainer.DISTANCE_TASK:
                            int usersDistance = Integer.parseInt(distMilTaskET.getText().toString());
                            isSuccesful = usersDistance == milsTaskTrainer.getDistance();
                            break;
                        case MilsTaskTrainer.ANGLE_TASK:
                            int userAngle = ArtilleryMilsUtil.convertToIntFormat(distMilTaskET.getText().toString());
                            isSuccesful = userAngle == milsTaskTrainer.getAngleSize();
                            break;
                        case MilsTaskTrainer.SIZE_TASK:
                            double userSize = Double.parseDouble(distMilTaskET.getText().toString());
                            isSuccesful = userSize == milsTaskTrainer.getMeterSize();
                    }

                    onTaskDecided(isSuccesful);
                    isChacked = true;
                    distMilTaskDescripTV.setText("Отримайте задачу");
                    distanceMilsCheckButton.setEnabled(!isChacked);
                    distanceMilsStartButton.setEnabled(isChacked);
                    distMilTaskET.setText("");
                    distMilTaskET.setEnabled(false);
                    isUsed = true;
                }
                catch (NotMilsFormatException e){
                    makeToastMessage(getResources().getString(R.string.milsExMessage));
                }
                catch (NumberFormatException e){
                    makeToastMessage(getResources().getString(R.string.numExMessage));
                }
        }
    }

    /*Метод реакции на решение задачи*/

    private void onTaskDecided(boolean isSuccessful){

        /*При решенни задачи
        * инкрементируем переменную подсчета задач в фрагменте
        * если ответ верный, инкрементируем переменную удачных задач
        * При любых раскладах инкрементируем поля User
        * Заполняем строки для передачи в окно*/

        tasks++;
        String title;
        String message = "";
        if (isSuccessful){
            title = "";
            message = "Вірна відповідь";
            currentUser.incrementSuccessMilTasks();
            succesfulTasks++;
        }
        else {
            title = "Невірна відповідь";
            switch (taskId){
                case MilsTaskTrainer.DISTANCE_TASK:
                message = String.format("Дистанція дорівнює %d м", milsTaskTrainer.getDistance());
                    break;
                case MilsTaskTrainer.ANGLE_TASK:
                    message = String.format("Кут дорівнює %s м",
                            ArtilleryMilsUtil.convertToIntFormat(milsTaskTrainer.getAngleTask()));
                    break;
                case MilsTaskTrainer.SIZE_TASK:
                    message = String.format("Лінійний розмір дорівнює %.1f м", milsTaskTrainer.getMeterSize());
            }

            currentUser.incrementUnsuccessMilTasks();
        }
        /*Выводим в текствью статистику решений. Проверяем процент удачных и подсвечиваем вью соответствующим цветом
        * выводим окно с резльтатом*/

        distanceMilsStatTV.setText(String.format("%d з %d (%.1f%%)", succesfulTasks, tasks, percent));
        if (percent < 50) distanceMilsStatTV.setBackgroundColor(Color.RED);
        else if (percent > 80) distanceMilsStatTV.setBackgroundColor(Color.GREEN);
        else distanceMilsStatTV.setBackgroundColor(Color.YELLOW);

        makeDialogWindowMessage(title, message);
    }

    /*Метод для вывода далогового окна*/

    private void makeDialogWindowMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message).setCancelable(false).setNegativeButton("До стрільби",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*Стандартный метод для выведения на экран тоста*/

    private void makeToastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    /*Геттер для работы с активити*/

    public boolean isUsed() {
        return isUsed;
    }

    /*Сеттер для передачи имени юзера из Активити*/

    public void setUser(String userName) {
        currentUser = new User(userName);
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /*Метод возвращает статистику занятия в активити*/
    public User getStats(){
        return currentUser;
    }
}
