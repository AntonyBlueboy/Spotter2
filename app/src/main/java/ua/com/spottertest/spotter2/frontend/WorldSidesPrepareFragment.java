package ua.com.spottertest.spotter2.frontend;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.adjustment.ArtilleryType;
import ua.com.spottertest.spotter2.core.adjustment.RangefinderAdjustmentTask;
import ua.com.spottertest.spotter2.core.adjustment.WorldSidesAdjustmentTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorldSidesPrepareFragment extends Fragment implements View.OnClickListener {

    /*Переменная целевой задачи и типа артиллерии, а также идентификатора целевой задачи*/

    WorldSidesAdjustmentTask task;
    ArtilleryType type;
    int taskId;

    /*Билдер для диалогового окна*/

    AlertDialog.Builder builder;

    /*Переменные вью*/

    private TextView worldSidesPrepDescrText;
    private EditText worldSidesPrepMetPerMilET, worldSidesPrepDeltaET;
    private CheckBox worldSidesPrepCB;
    private Button worldSidesPrepCheckBut, worldSidesPrepGoBut;
    private ImageButton worldSidesChangeBut;

    /*Переменная для интента для перехода к следующей активити*/

    Intent adjustmentActIntent;

    /*Переменная для позывного*/

    private String userName;

    /*Булева переменная, для желающих не рассчитывать деления прицела*/

    private boolean isWithoutScale = false;

    /*Переменная для проверки, проведена ли подготовка стрельбы*/

    private boolean isChacked = false;

    public WorldSidesPrepareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_world_sides_prepare, container, false);
    }

    /*сеттер для внедрения в фрагмент идентификатора пристрелки, типа артиллерии и создания таска*/

    public void setAdjustmentTask(int taskId, ArtilleryType type){
        this.taskId = taskId;
        this.type = type;
        task = new WorldSidesAdjustmentTask(this.type);
    }

    /*Метод жизненного цикла стартующий сразу после создания активити и фрагмента
    * В нем инициируем все вью и переменную логина*/


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        worldSidesPrepDescrText = (TextView) getView().findViewById(R.id.worldSidesPrepDescrText);
        worldSidesPrepDescrText.setText(task.getFormotion());
        worldSidesPrepMetPerMilET = (EditText) getView().findViewById(R.id.worldSidesPrepMetPerMilET);
        worldSidesPrepDeltaET = (EditText) getView().findViewById(R.id.worldSidesPrepDeltaET);
        worldSidesPrepCB = (CheckBox) getView().findViewById(R.id.worldSidesPrepCB);
        worldSidesPrepCB.setOnClickListener(this);
        worldSidesPrepCheckBut = (Button) getView().findViewById(R.id.worldSidesPrepCheckBut);
        worldSidesPrepCheckBut.setOnClickListener(this);
        worldSidesPrepGoBut = (Button) getView().findViewById(R.id.worldSidesPrepGoBut);
        worldSidesPrepGoBut.setOnClickListener(this);
        worldSidesChangeBut = (ImageButton) getView().findViewById(R.id.worldSidesChangeBut);
        worldSidesChangeBut.setOnClickListener(this);
        userName = getActivity().getIntent().getStringExtra("userName");

        builder = new AlertDialog.Builder(getActivity());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.worldSidesPrepCB:

                /*Если нажата/убрана галочка "не нужны" для графы с дП100, то убираем/выставляем графу с экрана,
                 инициируем булеву переменную true/false*/

                isWithoutScale = worldSidesPrepCB.isChecked();
                worldSidesPrepDeltaET.setEnabled(!isWithoutScale);
                break;
            case R.id.worldSidesPrepCheckBut:
                /*Если нажата кнопка "Проверить", то
                * создаем и обнуляем локальные переменные результатов подготовки стрельбы */

                int userMetForMil = 0;
                int valueOfScale = 0;

                /*Пытаемся получить данные из граф, при некорректности/отсутствии данных, выводим мэссэдж*/
                try{
                    userMetForMil = Integer.parseInt(worldSidesPrepMetPerMilET.getText().toString());
                    if (!isWithoutScale) valueOfScale = Integer.parseInt(worldSidesPrepDeltaET.getText().toString());
                    worldSidesPrepCheckBut.setEnabled(false);
                    worldSidesChangeBut.setEnabled(false);

                }
                catch (NumberFormatException e){
                    makeToastMessage(getView().getResources().getString(R.string.numExMessage));
                    break;
                }
                /*Выводим в результат проверки в Диалоговое окно*/

                makeDialogWindowMessage("Результат", task.checkPreparingResult(userMetForMil));

                /*Задаем дП100 в таск, если 0, то 0*/

                task.setValueofScale(valueOfScale);

                /*Выставляем флажок "проверено"*/

                isChacked = true;
                break;
            case R.id.worldSidesPrepGoBut:
                /*Если нажата кнопка "Перейти дальше", то
                 * проверяем, выполнялась ли подготовка, если нет - предупреждение
                  * если да, создаем интент, добавляем позывной, идентиф. прстрелки и сам таск, стартуем*/

                if (!isChacked) makeToastMessage(getView().getResources().getString(R.string.rangPrepNotCheckedMessage));
                else {
                    adjustmentActIntent = new Intent(getActivity().getApplicationContext(), AdjustmentActivity.class );
                    adjustmentActIntent.putExtra("userName", userName);
                    adjustmentActIntent.putExtra("taskId", taskId);
                    adjustmentActIntent.putExtra(WorldSidesAdjustmentTask.class.getCanonicalName(), task);
                    startActivity(adjustmentActIntent);
                    getActivity().finish();
                }
                break;
            case R.id.worldSidesChangeBut:
                changeTask();
                break;
        }
    }

    /*Метод для вывода далогового окна*/

    private void makeDialogWindowMessage(String title, String message){

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

    private void changeTask(){
        task = new WorldSidesAdjustmentTask(this.type);
        worldSidesPrepDescrText.setText(task.getFormotion());
    }
}
