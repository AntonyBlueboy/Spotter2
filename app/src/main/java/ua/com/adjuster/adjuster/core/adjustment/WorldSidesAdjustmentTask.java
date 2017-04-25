package ua.com.adjuster.adjuster.core.adjustment;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

import ua.com.adjuster.adjuster.core.mils.ArtilleryMilsUtil;

/**
 * Created by Rudolf on 02.04.2017.
 * Класс описывающий логику пристрелки по сторонам света
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

        this.metersInMils = new BigDecimal(( troopDistance)/1000).setScale(0, RoundingMode.HALF_UP).intValue();

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
        isLastCorrectionSuccesful = parcel.readInt() == 1;
        currentCorrection = parcel.readParcelable(Correction.class.getClassLoader());

        random = new Random(new Date().getTime());
    }

    /*Метод для упаковки переменных в парсел*/

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(adjustmentTitle);
        parcel.writeDouble(troopDistance);
        parcel.writeInt(troopAngle);
        parcel.writeInt(valueOfScale);
        parcel.writeInt(maxDistanсe);
        parcel.writeString(artylleryTypeName);
        parcel.writeInt(metersInMils);
        int isLCSuccessful = isLastCorrectionSuccesful ? 1 : 0;
        parcel.writeInt(isLCSuccessful);
        parcel.writeParcelable(currentCorrection, 0);
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

        /*Получаем угол между дир. по цели и цель- разрыв, если угол цели меньше, то добавляем 6000*/
        int troopDirect = troopAngle;
        double sharpAngle = 0;
        double y = (troopDirect * RADIANS_IN_MIL) - angleFromTargetToBurst;
        if (y < 0) {
            troopDirect += MAXIMAL_VIEWING_ANGLE;
            y = (troopDirect * RADIANS_IN_MIL) - angleFromTargetToBurst;
        }

        if (y < Math.toRadians(90)){
            isTotheLeft = false;
            isLower = true;
            sharpAngle = y;
        }
        else if(y < Math.toRadians(180)){
            isTotheLeft = false;
            isLower = false;
            sharpAngle = Math.toRadians(180) - y;
        }
        else if(y < Math.toRadians(270)){
            isTotheLeft = true;
            isLower = false;
            sharpAngle = y - Math.toRadians(180);
        }
        else {
            isTotheLeft = true;
            isLower = true;
            sharpAngle = Math.toRadians(360) - y;
        }


        /*Считаем корректуры в дальность и угломер по формулам тригонометрии*/

        distanceCorrection = (int) ((double)distanceFromTargetToBurst * Math.cos(sharpAngle));

        angleCorrection = (int) ((double)distanceFromTargetToBurst * Math.sin(sharpAngle));

        /*Переводим метры доворота в тысячные доворота по формуле тысячной*/

        angleCorrection = new BigDecimal(((double) angleCorrection)/metersInMils).setScale(0, RoundingMode.HALF_UP).intValue();

        /*Переводим метры дальности в деления прицела*/

        scaleCorrection = new BigDecimal((double) distanceCorrection / 100 * valueOfScale)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        /*Создаем и присваиваем обьект корректуры*/

        currentCorrection = new Correction(isLower, distanceCorrection, scaleCorrection, isTotheLeft, angleCorrection);

        /*Собираем текстовое наблюдение для разрыва*/

        String burstDescription;
        if (dx == 0) burstDescription = String.format("Розрив! %s %.0f", (isWest ? "Захід" : "Схід"), dy);
        else if (dy == 0) burstDescription = String.format("Розрив! %s %.0f", (isNorth ? "Північ" : "Південь"), dx);
        else burstDescription = String.format("Розрив! %s %.0f, " +
                                                        "%s %.0f", (isWest ? "Захід" : "Схід"), dy,
                                                                (isNorth ? "Північ" : "Південь"), dx);
        return new String[]{burstDescription};
    }
        /*Метод возвращающй описание боевого порядка для подготовительных активити*/

    @Override
    public String getFormotion() {
        return String.format("Дальність вогневої  - %d м,\n" +
                             "Дирекційний на ціль - %s", (int) troopDistance, ArtilleryMilsUtil.convertToMilsFormat(troopAngle));
    }

    /*Метод для проверки корректур, возвращает соответствующий вердикт*/

    @Override
    public String checkCorrection(Correction userCorretion, boolean isScaleUsed) {

        StringBuilder result = new StringBuilder();
        if (currentCorrection.equals(userCorretion)) {
            isLastCorrectionSuccesful = true;
            result.append("Точна коректура!");

        }
        else if (checkWithLimits(userCorretion, isScaleUsed)){
            isLastCorrectionSuccesful = true;
            result.append("Точна коректура!");
        }
        else {
            isLastCorrectionSuccesful = false;
            result.append("Коригування не точне!\n" +
                    "Мало бути:\n");
            if (currentCorrection.getDistanceCorrection() == 0) result.append("Приціл без змін");
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
                                                      "0-01 = %d м", ArtilleryMilsUtil.convertToMilsFormat(troopAngle), metersInMils);
        else result = String.format("α = %s\n" +
                                    "0-01 = %d м\n" +
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

        /*Определение четвертей. Схема четвертей - 4 _|_ 1
        *                                          3  |  2  */

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

    /*Метод возвращает true, если в корректуре все совпадает с точностью до 20 м*/

    private boolean checkWithLimits(Correction userCorrection, boolean isScaleUsed){
        if (userCorrection == null) return false;
        if (userCorrection.isLower() != currentCorrection.isLower()) return false;
        if (userCorrection.isTotheLeft() != currentCorrection.isTotheLeft()) return false;

        int userAngleCorrection = userCorrection.getAngleCorrection();
        int currentAngleCorrection = currentCorrection.getAngleCorrection();
        int limitAngle = new BigDecimal(20.0d/metersInMils).setScale(0, RoundingMode.HALF_UP).intValue();
        if ((userAngleCorrection > (currentAngleCorrection + limitAngle))
                || (userAngleCorrection < (currentAngleCorrection - limitAngle))) return false;
        int limitDistance = 20;
        int userDistanceCorrection = userCorrection.getDistanceCorrection();
        int currentDistancecorrection = currentCorrection.getDistanceCorrection();
        return  !((userDistanceCorrection > (currentDistancecorrection + limitDistance))
                || (userDistanceCorrection < (currentDistancecorrection - limitDistance) ));
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
