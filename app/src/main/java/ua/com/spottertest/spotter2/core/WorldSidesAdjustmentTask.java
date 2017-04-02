package ua.com.spottertest.spotter2.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

/**
 * Created by Rudolf on 02.04.2017.
 */
public class WorldSidesAdjustmentTask extends AdjustmentTask {

    /*Название пристрелки*/

    private String adjustmentTitle;

    /*Максимальное значение угла в тысячных*/

    private static final int MAXIMAL_VIEWING_ANGLE = 6000;

    /*Минимальная дальность для всех систем, взята в общем для случаев непрямой наводки*/

    private static final int MINDISTANCE = 500;

    /*Обьект рандомизации, для генерирования условий стрельбы и разрывов*/
    private Random random;

    /*Дальность огневой*/
    private double troopDistance;

    /*Дир угол стрельбы*/

    private int troopAngle;

    /*Изменение прицела на 100 м*/
    private int valueOfScale;

    /*Минимальная и максимальная дальности работы орудия, его название*/

    private int maxDistanсe;
    private String artylleryTypeName;

    /*Текущая корректура*/
    private Correction currentCorrection;

    /*Флаг, была ли успешной крайняя корректура*/

    private boolean isLastCorrectionSuccesful;

    /*Величина тысячной в метрах*/

    private int metersInMils;

    /*Константа колчичество радиан в тысячной*/

    private static final double RADIANS_IN_MIL = 0.001d;

    /*Поле для парселизации*/

    public static final Parcelable.Creator<WorldSidesAdjustmentTask> CREATOR =
            new Parcelable.Creator<WorldSidesAdjustmentTask>(){
                public WorldSidesAdjustmentTask createFromParcel(Parcel in){
                    return new WorldSidesAdjustmentTask(in);
                }

                @Override
                public WorldSidesAdjustmentTask[] newArray(int size) {
                    return new WorldSidesAdjustmentTask[size];
                }
            };

    public WorldSidesAdjustmentTask(ArtilleryType type) {

        /*Инициируем необходимые переменные*/

        this.adjustmentTitle = "Пристрілка за сторонами світу";
        this.artylleryTypeName = type.getTypeDescription();
        this.maxDistanсe = type.getMaxDistance();
        this.random = new Random(new Date().getTime());

        /*Дальность огневой инициируем числом между минимальной и максимальной дистанциями стрельбы*/

        this.troopDistance =  (this.MINDISTANCE + (random.nextInt((this.maxDistanсe - this.MINDISTANCE) + 1)))/10*10;

        /*Тысячную дальности рассчиываем и округляем до целых*/

        this.metersInMils = new BigDecimal(((double) troopDistance)/1000).setScale(0, RoundingMode.HALF_UP).intValue();

        /*Дирекц. угол стрельбы берем в пределе между 0-00 и 60-00*/

        this.troopAngle = random.nextInt(MAXIMAL_VIEWING_ANGLE);

    }

    /*Конструктор для восстановления из парсела*/

    public WorldSidesAdjustmentTask(Parcel parcel) {
        adjustmentTitle = parcel.readString();
        troopDistance = parcel.readDouble();
        troopAngle = parcel.readInt();
        valueOfScale = parcel.readInt();
        maxDistanсe = parcel.readInt();
        artylleryTypeName = parcel.readString();
        metersInMils = parcel.readInt();

        random = new Random(new Date().getTime());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(adjustmentTitle);
        parcel.writeDouble(troopDistance);
        parcel.writeInt(troopAngle);
        parcel.writeInt(valueOfScale);
        parcel.writeInt(maxDistanсe);
        parcel.writeString(artylleryTypeName);
        parcel.writeInt(metersInMils);
    }

    /*Метод обязательный при имплементировании парселизации*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String checkPreparingResult(Object... preparedCoefs) {
        int usersMetersInMil = (int) preparedCoefs[0];
        String result;
        if (usersMetersInMil == this.metersInMils) result = "Розраховано вірно. Можна розпочати пристрілку.";
        else result = String.format("Розраховано невірно. Запишіть, 0-01 для вогневої дорівнює %d м.\n" +
                        " Можна почнати пристрілку.", this.metersInMils);
        return result;
    }

    /*Генерируем текст наблюдения по залпу, вида "Север х, Запад у", рассчитываем и
                *  инициализируем переменную currentCorrection*/

    @Override
    public String[] getBurstDescription() {

       /*Генерируем наблюдение*/

        boolean isNorth = random.nextBoolean();
        boolean isWest = random.nextBoolean();
        double dx = 0;
        double dy = 0;
        do {
            dx = getDistanceToTarget();
            dy = getDistanceToTarget();
        }
        while (dx == 0 & dy == 0); // если оба генератора вернули 0, то нужно пересчитать


        /*Переменные корректуры*/
        /*дальность меньше или нет*/

        boolean isLower;

        /*Дальность изменить на количество метров*/

        int distanceCorrection;

        /*Дальность изменить на количество делений прицела*/

        int scaleCorrection;

        /*углемер левее или нет*/

        boolean isTotheLeft;

        /*угломер изменить на количество тысячных*/

        int angleCorrection;

        /*Дальность цель-разрыв из теоремы Пифагора*/

        int distanceFromTargetToBurst = (int)Math.sqrt(dx*dx + dy*dy);

        /*Получаем дирекционный угол цель-разрыв*/

        double angleFromTargetToBurst = getAngleFromTargetToBurst(isNorth, isWest, dx, dy);

        /*Получаем угол между дир. по цели и цель- разрыв*/

        double y = (troopAngle * RADIANS_IN_MIL) - angleFromTargetToBurst;

        /*Если цель левее напр стрельбы корректура будет правее*/

        isTotheLeft = y < 0;

        /*Если угол у меньше  90, то прицел уменьшать, иначе - увеличивать*/

        isLower = Math.abs(y) < Math.toRadians(90);

        /*Если угол у больше 90, для дальнейших рассчетов он не годится, значит нужно его изменить на 180 град*/

        if (Math.abs(y) > Math.toRadians(90))
            y = Math.toRadians(180) - y;

        /*Считаем корректуры в дальность и угломер по формулам тригонометрии*/

        distanceCorrection = (int) ((double)distanceFromTargetToBurst * Math.sin(y));

        angleCorrection = (int) ((double)distanceFromTargetToBurst * Math.cos(y));

        /*Переводим метры доворота в тысячные доворота по формуле тысячной*/

        angleCorrection = new BigDecimal(((double) angleCorrection)/metersInMils).setScale(0, RoundingMode.HALF_UP).intValue();

        /*Переводим метры дальности в деления прицела*/

        scaleCorrection = new BigDecimal((double) distanceCorrection / 100 * valueOfScale)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        /*Создаем и присваиваем обьект корректуры*/

        currentCorrection = new Correction(isLower, distanceCorrection, scaleCorrection, isTotheLeft, angleCorrection);

        /*Собираем текстовое наблюдение для разрыва*/

        String burstDescription;
        if (dx == 0) burstDescription = String.format("Розрив! %s %d", (isWest ? "Захід" : "Схід"), dy);
        else if (dy == 0) burstDescription = String.format("Розрив! %s %d", (isNorth ? "Північ" : "Південь"), dx);
        else burstDescription = String.format("Разрыв! %s %d\n" +
                                              "        %s %d", (isWest ? "Захід" : "Схід"), dy,
                                                                (isNorth ? "Північ" : "Південь"), dx);
        return new String[]{burstDescription};
    }
        /*Метод возвращающй описание боевого порядка для подготовительных активити*/

    @Override
    public String getFormotion() {
        return String.format("Дальність вогневої  - %d,\n" +
                             "Дирекційний на ціль - %d", (int) troopDistance, ArtilleryMilsUtil.convertToMilsFormat(troopAngle));
    }

    /*Метод для проверки корректур, возвращает соответствующий вердикт*/

    @Override
    public String checkCorrection(Correction userCorretion, boolean isScaleUsed) {
        StringBuilder result = new StringBuilder();
        if (currentCorrection.equals(userCorretion)) {
            isLastCorrectionSuccesful = true;
            result.append("Точна коректура!\nЧас розрахунку - .");

        }
        else {
            isLastCorrectionSuccesful = false;
            result.append("Коригування не точне!\n" +
                    "Мало бути:\n");
            if (currentCorrection.getAngleCorrection() == 0) result.append("Приціл без змін");
            else {
                result.append("Приціл " + (currentCorrection.isLower() ? "менше " : "більше "));
                if (isScaleUsed) result.append(currentCorrection.getScaleCorrection());
                else result.append(String.format("%d метрів", currentCorrection.getDistanceCorrection()));
            }
            result.append("\n");
            if (currentCorrection.getAngleCorrection() == 0) result.append("Кутомір без змін");
            else {
                result.append("Кутомір " + (currentCorrection.isTotheLeft() ? "лівіше " : "правіше ")
                        + ArtilleryMilsUtil.convertToMilsFormat(currentCorrection.getAngleCorrection()));
            }
        }
        return result.toString();
    }

    @Override
    public String getCoefsDescription() {
        String result;
        if (valueOfScale == 0) result = String.format("α = %s\n" +
                                                      "0-01 = %d", ArtilleryMilsUtil.convertToMilsFormat(troopAngle), metersInMils);
        else result = String.format("α = %s\n" +
                                    "0-01 = %d\n" +
                                    "∆П100 = %d", ArtilleryMilsUtil.convertToMilsFormat(troopAngle), metersInMils, valueOfScale);
        return result;
    }



    /*Метод-генератор отклонений разрыва по осям координат в 10 случаях из ста дает отклонения 300-600 м
    * в 40 случаях - 100-300 м
    * в 50 случаях 30 - 100 м
    * в 10 случаях - нули */

    private int getDistanceToTarget(){
        int temp = random.nextInt(100);
        int d = 0;
        if (temp > 90) d =  ( 300 + (random.nextInt((300))))/10*10;
        else if (temp > 50) d = ( 100 + (random.nextInt((200))))/10*10;
        else if (temp > 40) d = 0;
        else d = (30 + (random.nextInt((70))))/10*10;
        return d;
    }
        /*Метод для определения дирекционного угла на разрыв с цели из доклада наблюдателя*/

    private double getAngleFromTargetToBurst(boolean isNorth, boolean isWest, double dx, double dy){

        /*Определение четвертей. Схема четвертей - 4 | 1
        *                                          3 | 2  */

        int quarter = 0;
        if (isNorth & !isWest) quarter = 1;
        else if (!isNorth & !isWest) quarter = 2;
        else if (!isNorth & isWest) quarter = 3;
        else if (isNorth & isWest) quarter = 4;

        /*Получаем дальность цель - разрыв по теореме Пифагора*/

        int distanceFromTargetToBurst = (int)Math.sqrt(dx*dx + dy*dy);

        /*Получаем острый угол между углом цель - разрыв и осью х*/

        double angleFromTargetToBurst = Math.asin(dy/distanceFromTargetToBurst);

        /*Если разрыв в 1 четверти от ничего не меняем, если во второй то отнимаем острый угол от 180 для получ дирекционного
        * если в третьей, то прибавляем острый угол к 180, если в 4, то отнимаем от 360*/

        switch (quarter){
            case 2:
                angleFromTargetToBurst = Math.toRadians(180) - angleFromTargetToBurst;
                break;
            case 3:
                angleFromTargetToBurst = Math.toRadians(180) + angleFromTargetToBurst;
                break;
            case 4:
                angleFromTargetToBurst = Math.toRadians(360) - angleFromTargetToBurst;
        }
        return angleFromTargetToBurst;
    }

    @Override
    public int getValueOfScale() {
        return valueOfScale;
    }

    @Override
    public boolean isLastCorrectionSuccessful() {
        return isLastCorrectionSuccesful;
    }

    @Override
    public String getAdjustmentTitle() {
        return adjustmentTitle;
    }

    @Override
    public String getArtylleryTypeName() {
        return artylleryTypeName;
    }

    @Override
    public void setValueofScale(int valueofScale) {
        this.valueOfScale = valueofScale;
    }
}
