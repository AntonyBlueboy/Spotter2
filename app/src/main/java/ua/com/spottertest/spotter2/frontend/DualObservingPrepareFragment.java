package ua.com.spottertest.spotter2.frontend;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.mils.ArtilleryMilsUtil;
import ua.com.spottertest.spotter2.core.adjustment.ArtilleryType;
import ua.com.spottertest.spotter2.core.adjustment.DualObservingAdjustmentTask;
import ua.com.spottertest.spotter2.core.mils.NotMilsFormatException;
import ua.com.spottertest.spotter2.frontend.AdjustmentActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class DualObservingPrepareFragment extends Fragment implements View.OnClickListener{

    /*Переменная целевой задачи и типа артиллерии, а также идентификатора целевой задачи*/

    DualObservingAdjustmentTask task;
    ArtilleryType type;
    int taskId;

    /*Билдер для диалогового окна*/

    AlertDialog.Builder builder;

    /*Переменные используемых вью*/

    private TextView dualPrepDescrText;
    private EditText dualPrepDistCoefET, dualPrepProtStepET, dualPrepLeftCoefET, dualPrepRightCoefET,dualPrepDeltaET;
    private RadioGroup dualPrepMainCommanderRG, dualPrepHandRG;
    private RadioButton dualPrepMainCommanderLeftRB, dualPrepMainCommanderRightRB,
            dualPrepHandLeftRB, dualPrepHandRightRB;
    private CheckBox dualPrepCB;
    private Button dualPrepCheckBut, dualPrepGoBut;
    private ImageButton dualObservingChangeBut;

    /*Переменная для интента для перехода к следующей активити*/

    Intent adjustmentActIntent;

    /*Переменная для позывного*/

    private String userName;

    /*Булева переменная, для желающих не рассчитывать деления прицела*/

    private boolean isWithoutScale = false;

    /*Переменная для проверки, проведена ли подготовка стрельбы*/

    private boolean isChacked = false;


    public DualObservingPrepareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dual_observing_prepare, container, false);
    }

    /*сеттер для внедрения в фрагмент идентификатора пристрелки, типа артиллерии и создания таска*/

    public void setAdjustmentTask(int taskId, ArtilleryType type){
        this.taskId = taskId;
        this.type = type;
        task = new DualObservingAdjustmentTask(this.type);
    }

    /*Метод жизненного цикла стартующий сразу после создания активити и фрагмента
    * В нем инициируем все вью и переменную логина*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dualPrepDescrText = (TextView) getView().findViewById(R.id.dualPrepDescrText);
        dualPrepDescrText.setText(task.getFormotion());
        dualPrepDistCoefET = (EditText) getView().findViewById(R.id.dualPrepDistCoefET);
        dualPrepProtStepET = (EditText) getView().findViewById(R.id.dualPrepProtStepET);
        dualPrepLeftCoefET = (EditText) getView().findViewById(R.id.dualPrepLeftCoefET);
        dualPrepRightCoefET = (EditText) getView().findViewById(R.id.dualPrepRightCoefET);
        dualPrepDeltaET = (EditText) getView().findViewById(R.id.dualPrepDeltaET);
        dualPrepMainCommanderRG = (RadioGroup) getView().findViewById(R.id.dualPrepMainCommanderRG);
        dualPrepHandRG = (RadioGroup) getView().findViewById(R.id.dualPrepHandRG);
        dualPrepMainCommanderLeftRB = (RadioButton) getView().findViewById(R.id.dualPrepMainCommanderLeftRB);
        dualPrepMainCommanderRightRB = (RadioButton) getView().findViewById(R.id.dualPrepMainCommanderRightRB);
        dualPrepHandLeftRB = (RadioButton) getView().findViewById(R.id.dualPrepHandLeftRB);
        dualPrepHandRightRB = (RadioButton) getView().findViewById(R.id.dualPrepHandRightRB);
        dualPrepCB = (CheckBox) getView().findViewById(R.id.dualPrepCB);
        dualPrepCB.setOnClickListener(this);
        dualPrepCheckBut = (Button) getView().findViewById(R.id.dualPrepCheckBut);
        dualPrepCheckBut.setOnClickListener(this);
        dualPrepGoBut = (Button) getView().findViewById(R.id.dualPrepGoBut);
        dualPrepGoBut.setOnClickListener(this);
        dualObservingChangeBut = (ImageButton) getView().findViewById(R.id.dualObservingChangeBut);
        dualObservingChangeBut.setOnClickListener(this);

        userName = getActivity().getIntent().getStringExtra("userName");

        builder = new AlertDialog.Builder(getActivity());
    }

    /*Стандартный метод для выведения на экран тоста*/

    private void makeToastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    /*Метод интерфейса для реакции на все клики*/

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.dualPrepCB:

                /*Если нажата/убрана галочка "не нужны" для графы с дП100, то убираем/выставляем графу с экрана,
                 инициируем булеву переменную true/false*/

                isWithoutScale = dualPrepCB.isChecked();
                dualPrepDeltaET.setEnabled(!isWithoutScale);
                break;
            case R.id.dualPrepCheckBut:

                /*Если нажата кнопка "Проверить", то
                * создаем и обнуляем локальные переменные результатов подготовки стрельбы */

                double userDistanceCoef = 0;
                int userProtratorStep = 0;
                int leftCoef = 0;
                int rightCoef = 0;
                boolean isLeftMain = false;
                boolean isLefthand = false;
                int valueOfScale = 0;

                /*Пытаемся получить данные из граф, при некорректности/отсутствии данных, выводим мэссэдж*/

                try{
                    userDistanceCoef = Double.parseDouble(dualPrepDistCoefET.getText().toString());
                    userProtratorStep = ArtilleryMilsUtil.convertToIntFormat(dualPrepProtStepET.getText().toString());
                    if (!isWithoutScale) valueOfScale = Integer.parseInt(dualPrepDeltaET.getText().toString());
                    leftCoef = Integer.parseInt(dualPrepLeftCoefET.getText().toString());
                    rightCoef = Integer.parseInt(dualPrepRightCoefET.getText().toString());
                    isLeftMain = dualPrepMainCommanderLeftRB.isChecked();
                    isLefthand = dualPrepHandLeftRB.isChecked();
                    dualPrepCheckBut.setEnabled(false);
                    dualObservingChangeBut.setEnabled(false);
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

                makeDialogWindowMessage("Результат", task.checkPreparingResult(userDistanceCoef, userProtratorStep,
                        leftCoef, rightCoef, isLeftMain, isLefthand));

                /*Задаем дП100 в таск, если 0, то 0*/

                task.setValueofScale(valueOfScale);

                /*Выставляем флажок "проверено"*/

                isChacked = true;
                break;
            case R.id.dualPrepGoBut:
                /*Если нажата кнопка "Перейти дальше", то
                    * проверяем, выполнялась ли подготовка, если нет - предупреждение
                    * если да, создаем интент, добавляем позывной, идентиф. прстрелки и сам таск, стартуем*/

                if (!isChacked) makeToastMessage(getView().getResources().getString(R.string.rangPrepNotCheckedMessage));
                else {
                    adjustmentActIntent = new Intent(getActivity().getApplicationContext(), AdjustmentActivity.class );
                    adjustmentActIntent.putExtra("userName", userName);
                    adjustmentActIntent.putExtra("taskId", taskId);
                    adjustmentActIntent.putExtra(DualObservingAdjustmentTask.class.getCanonicalName(), task);
                    startActivity(adjustmentActIntent);
                    getActivity().finish();
                }
                break;
            case R.id.dualObservingChangeBut:
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

    private void changeTask(){
        task = new DualObservingAdjustmentTask(this.type);
        dualPrepDescrText.setText(task.getFormotion());
    }
}
