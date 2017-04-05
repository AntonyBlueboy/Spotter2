package ua.com.spottertest.spotter2.frontend;


import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.mils.ArtilleryMilsUtil;
import ua.com.spottertest.spotter2.core.adjustment.ArtilleryType;
import ua.com.spottertest.spotter2.core.mils.NotMilsFormatException;
import ua.com.spottertest.spotter2.core.adjustment.RangefinderAdjustmentTask;
import ua.com.spottertest.spotter2.frontend.AdjustmentActivity;


/**
 * A simple {@link Fragment} subclass.
 * Фрагмент с логикой активити задач по подготовке стрельбы с дальномером
 */
public class RangeFinderPrepareFragment extends Fragment implements View.OnClickListener{

    /*Переменная целевой задачи и типа артиллерии, а также идентификатора целевой задачи*/

    RangefinderAdjustmentTask task;
    ArtilleryType type;
    int taskId;

    /*Билдер для диалогового окна*/

    AlertDialog.Builder builder;

    /*Переменные используемых вью*/

    private TextView rangPrepDescrTV;
    private EditText rangPrepDistCoefET, rangPrepProtStepET, rangPrepDeltaET;
    private CheckBox rangPrepCB;
    private Button rangPrepCheckBut, rangPrepGoBut;

    /*Переменная для интента для перехода к следующей активити*/

    Intent adjustmentActIntent;

    /*Переменная для позывного*/

    private String userName;

    /*Булева переменная, для желающих не рассчитывать деления прицела*/

    private boolean isWithoutScale = false;

    /*Переменная для проверки, проведена ли подготовка стрельбы*/

    private boolean isChacked = false;



    public RangeFinderPrepareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_range_finder_prepare, container, false);
    }

    /*сеттер для внедрения в фрагмент идентификатора пристрелки, типа артиллерии и создания таска*/

    public void setAdjustmentTask(int taskId, ArtilleryType type){
        this.taskId = taskId;
        this.type = type;
        task = new RangefinderAdjustmentTask(this.type);
    }

    /*Метод жизненного цикла стартующий сразу после создания активити и фрагмента
    * В нем инициируем все вью и переменную логина*/

    @Override
    public void onStart() {
        super.onStart();
        rangPrepDescrTV = (TextView) getView().findViewById(R.id.rangPrepDescrText);
        rangPrepDescrTV.setText(task.getFormotion());
        rangPrepCB = (CheckBox) getView().findViewById(R.id.rangPrepCB);
        rangPrepCB.setOnClickListener(this);
        rangPrepDistCoefET = (EditText) getView().findViewById(R.id.rangPrepDistCoefET);
        rangPrepProtStepET = (EditText) getView().findViewById(R.id.rangPrepProtStepET);
        rangPrepDeltaET = (EditText) getView().findViewById(R.id.rangPrepDeltaET);
        rangPrepCheckBut = (Button) getView().findViewById(R.id.rangPrepCheckBut);
        rangPrepCheckBut.setOnClickListener(this);
        rangPrepGoBut = (Button) getView().findViewById(R.id.rangPrepGoBut);
        rangPrepGoBut.setOnClickListener(this);
        userName = getActivity().getIntent().getStringExtra("userName");

        builder = new AlertDialog.Builder(getActivity());
    }



    /*Метод реакции на клик по любому вью*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rangPrepCB:

                /*Если нажата/убрана галочка "не нужны" для графы с дП100, то убираем/выставляем графу с экрана,
                 инициируем булеву переменную true/false*/

                isWithoutScale = rangPrepCB.isChecked();
                rangPrepDeltaET.setEnabled(!isWithoutScale);
                break;
            case R.id.rangPrepCheckBut:

                /*Если нажата кнопка "Проверить", то
                * создаем и обнуляем локальные переменные результатов подготовки стрельбы */

                double userDistanceCoef = 0;
                int userProtratorStep = 0;
                int valueOfScale = 0;

                /*Пытаемся получить данные из граф, при некорректности/отсутствии данных, выводим мэссэдж*/

                try{
                userDistanceCoef = Double.parseDouble(rangPrepDistCoefET.getText().toString());
                userProtratorStep = ArtilleryMilsUtil.convertToIntFormat(rangPrepProtStepET.getText().toString());
                    if (!isWithoutScale) valueOfScale = Integer.parseInt(rangPrepDeltaET.getText().toString());
                }
                catch (NotMilsFormatException e){
                    makeToastMessage(getView().getResources().getString(R.string.milsExMessage));
                    break;
                }
                catch (NumberFormatException e){
                    makeToastMessage(getView().getResources().getString(R.string.numExMessage));
                    break;
                }

                /*Выводим в результат проверки в Диалоговое окно*/

                makeDialogWindowMessage("Результат", task.checkPreparingResult(userDistanceCoef, userProtratorStep));

                /*Задаем дП100 в таск, если 0, то 0*/

                task.setValueofScale(valueOfScale);

                /*Выставляем флажок "проверено"*/

                isChacked = true;
                break;
            case R.id.rangPrepGoBut:

                /*Если нажата кнопка "Перейти дальше", то
                 * проверяем, выполнялась ли подготовка, если нет - предупреждение
                  * если да, создаем интент, добавляем позывной, идентиф. прстрелки и сам таск, стартуем*/

                if (!isChacked) makeToastMessage(getView().getResources().getString(R.string.rangPrepNotCheckedMessage));
                else {
                    adjustmentActIntent = new Intent(getActivity().getApplicationContext(), AdjustmentActivity.class );
                    adjustmentActIntent.putExtra("userName", userName);
                    adjustmentActIntent.putExtra("taskId", taskId);
                    adjustmentActIntent.putExtra(RangefinderAdjustmentTask.class.getCanonicalName(), task);
                    startActivity(adjustmentActIntent);
                    getActivity().finish();
                }
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
}
