package ua.com.spottertest.spotter2.core.adjustment;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

import ua.com.spottertest.spotter2.core.mils.ArtilleryMilsUtil;

/**
 * Created by Rudolf on 07.03.2017.
 * Класс описывающий занятие по пристрелке с наблюдениями дальномерщика
 */

public class RangefinderAdjustmentTask extends AdjustmentTask {

    /*Название пристрелки*/
    private String adjustmentTitle;


    /*Минимальная дальность нахождения артиллерийского наблюдателя от цели*/
    private static final int MINIMUM_SPOTTER_DISTANCE = 500;
    private static final int MAXIMUM_SPOTTER_DISTANCE = 3000;

    /*Минимальная дальность для всех систем, взята в общем для случаев непрямой наводки*/

    private static final int MINDISTANCE = 500;

    /*Обьект рандомизации, для генерирования условий стрельбы и разрывов*/
    private Random random;

    /*Дальность огневой*/
    private double troopDistance;

    /*Дальность основного наблюдателя*/
    private double mainCommanderDistance;

    /*Смещение основного наблюдателя*/
    private double mainParallax;

    /*Изменение прицела на 100 м*/
    private int valueOfScale;

    /*Флажок расположения огневой с левой стороны от наблюдателя*/
    private boolean isLeftHand;

    /*Коэфициент удаления основного наблюдателя*/
    private double mainDistanceCoef;

    /*Шаг угломера основного наблюдателя*/
    private int mainProtractorStep;

    /*Минимальная и максимальная дальности работы орудия, его название*/

    private int maxDistanсe;
    private String artylleryTypeName;

    /*Тип орудия*/
    private ArtilleryType type;

    /*Текущая корректура*/
    private Correction currentCorrection;

    /*Флаг, была ли успешной крайняя корректура*/

    private boolean isLastCorrectionSuccesful;

    /*Поле, необходимое при парселизации*/

    public static final Parcelable.Creator<RangefinderAdjustmentTask> CREATOR =
            new Parcelable.Creator<RangefinderAdjustmentTask>(){
                public RangefinderAdjustmentTask createFromParcel(Parcel in){
                    return new RangefinderAdjustmentTask(in);
                }

                @Override
                public RangefinderAdjustmentTask[] newArray(int size) {
                    return new RangefinderAdjustmentTask[size];
                }
            };

    public RangefinderAdjustmentTask(ArtilleryType type) {
        this.type = type;
        this.adjustmentTitle = "Пристрілка з далекоміром";
        this.artylleryTypeName = type.getTypeDescription();
        this.maxDistanсe = type.getMaxDistance();
        this.random = new Random(new Date().getTime());

        /*Генерируем дальность огневой между минимальной и максимальной дальностями*/
        this.troopDistance =  (this.MINDISTANCE + (random.nextInt((this.maxDistanсe - this.MINDISTANCE) + 1)))/10*10;

        /*Генерируем дальность основного наблюдателя. Выдерживаем принципы:
        * наблюдатель не находится дальше 3000 м
        * наблюдатель находится дальше огневой в 20% случаев*/

        if (this.troopDistance > MAXIMUM_SPOTTER_DISTANCE) this.mainCommanderDistance = (MINIMUM_SPOTTER_DISTANCE +
                (random.nextInt((MAXIMUM_SPOTTER_DISTANCE - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
        else {
            int randomNumber = random.nextInt(100);
            if (randomNumber <= 20) {
                this.mainCommanderDistance = (MINIMUM_SPOTTER_DISTANCE +
                        (random.nextInt((MAXIMUM_SPOTTER_DISTANCE - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
            } else
                this.mainCommanderDistance = (MINIMUM_SPOTTER_DISTANCE +
                        (random.nextInt(((int) this.troopDistance - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
        }

        /*Генерируем смещение основного наблюдателя. Для аналитического метода исчисления корректур оно не превышает 5-00*/

        this.mainParallax = (random.nextInt(501)) / 10 * 10;

        /*Генерируем положение огневой относительно основного наблюдателя*/

        this.isLeftHand = random.nextBoolean();

        /*Считаем коэфициент удаления основного наблюдателя округляя до десятых долей*/

        this.mainDistanceCoef = new BigDecimal(this.mainCommanderDistance / this.troopDistance)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        /*Считаем шаг угломера основного наблюдателя округляя до целых*/

        double temp = this.mainParallax / (this.troopDistance * 0.01);
        this.mainProtractorStep = new BigDecimal(temp).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /*Конструктор для парселизации класса*/

    public RangefinderAdjustmentTask(Parcel parcel) {
        adjustmentTitle = parcel.readString();
        troopDistance = parcel.readDouble();
        mainCommanderDistance = parcel.readDouble();
        mainParallax = parcel.readDouble();
        valueOfScale = parcel.readInt();

        isLeftHand = parcel.readInt()==1;

        mainDistanceCoef = parcel.readDouble();
        mainProtractorStep = parcel.readInt();
        maxDistanсe = parcel.readInt();
        artylleryTypeName = parcel.readString();
        type = ArtilleryType.getTypeForDescription(artylleryTypeName);
        random = new Random(new Date().getTime());
    }

    /*Метод обязательный при имплементировании парселизации*/

    @Override
    public int describeContents() {
        return 0;
    }

    /*Метод для упаковки полей в парсель*/

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(adjustmentTitle);
        parcel.writeDouble(troopDistance);
        parcel.writeDouble(mainCommanderDistance);
        parcel.writeDouble(mainParallax);
        parcel.writeInt(valueOfScale);

        int isLefth = isLeftHand ? 1 : 0;
        parcel.writeInt(isLefth);

        parcel.writeDouble(mainDistanceCoef);
        parcel.writeInt(mainProtractorStep);
        parcel.writeInt(maxDistanсe);
        parcel.writeString(artylleryTypeName);
    }

    /*Получаем исчисленые пользователем КУ и ШУ в контейнере для Object, сверяем и возвразаем вердикт*/

    @Override
    public String checkPreparingResult( Object... preparedCoefs )
    {
        double userDistanceCoef = (double)preparedCoefs[0];
        int userProtratorStep = (int) preparedCoefs[1];
        String result;
        if (this.mainDistanceCoef == userDistanceCoef & this.mainProtractorStep == userProtratorStep)
            result = "Розраховано вірно. Можна розпочати пристрілку.";
        else result = String.format("Розраховано невірно. Запишіть, КВ = %.1f, КК = %s. \n Можна почнати пристрілку.",
                mainDistanceCoef, ArtilleryMilsUtil.convertToMilsFormat(mainProtractorStep));

        return result;

    }

    /*Генерируем текст наблюдения по залпу, вида "Перелет/недолет х, лево/право у", рассчитываем и
    *  инициализируем переменную currentCorrection
    *  ВНИМАНИЕ: нулевые наблюдения отсутствуют. Их нужно будет ввести при лоработке*/

    @Override
    public String[] getBurstDescription() {

        /*генерируем флажок "перелет"*/

        boolean isOver = random.nextBoolean();

        /*генерируем величину отклонения по дальности в метрах. Используем переменную randomNumber, что б свести вероятность
        * больших отклонений ниже 25%. Отклонения начинаются с 30 м, поскольку 25 и меньше считаются стрельбой по цели*/

        int distanceToTarget;
        int randomNumber = random.nextInt(100);

        /*Отклонения до 300 м - ниже 25% вероятность*/

        if (randomNumber <= 25) distanceToTarget = (30 + random.nextInt((300 - 30) + 1))/10*10;

        /*Отклонения до 150 м - ниже 75% вероятность*/

        else distanceToTarget = (30 + random.nextInt((150 - 30) + 1))/10*10;

        /*Генерируем флаг отклонения по направлению "лево"*/

        boolean isLeft = random.nextBoolean();

        /*Генерируем отклонение по углу в тысячных. Поскольку вероятность больших отклонений по угломеру еще
        * меньше чем по дальности, то используем randomNumber.*/

        int angleToTarget;
        randomNumber = random.nextInt();

        /*Отклонения до 1-20 - ниже 13%*/

        if (randomNumber <= 13) {
            angleToTarget = 5 + random.nextInt(120) / 10 * 10 + 5;
        }

        /*Отклонения до 0-70 - ниже 25%*/

        else if (randomNumber <= 25) angleToTarget = 5 + random.nextInt(70) / 10 * 10 + 5;

        /*Отклонения до 0-30 - ниже 75%*/

        else angleToTarget = random.nextInt(30) / 10 * 10 + 5;

        /*Формируем текстовое описание разрыва для случая с отклонениями.*/

        String burstRejectionDescription = String.format("Спостерігаю розрив! " + (isOver ? "Переліт " : "Недоліт ")
                + "%d" +
                (isLeft ? ", ліво " : ", право ")+ "%s" + "!",
                distanceToTarget, ArtilleryMilsUtil.convertToMilsFormat(angleToTarget));

        /*Формируем текстовое описание разрыва для случая с дистанцией.*/

        String burstDistanceDescription = String.format("Спостерігаю розрив! " + ("Дистанція - %.0f") +
                (isLeft ? ", ліво " : ", право ") + "%s" + "!",
                (isOver ? (mainCommanderDistance + distanceToTarget) : (mainCommanderDistance - distanceToTarget)),
                ArtilleryMilsUtil.convertToMilsFormat(angleToTarget));

        /*Исчисляем атрибуты корректуры, создаем ее и присваиваем.*/

        /*Определяем знак подставляемый в формулу, исходя из конфигурации боевого порядка.
        * При огневой слева и перелете, а также при огневой справа и недолете знак "+", в остальных случаях "-"*/

        int znak = (isLeftHand & isOver || ((!isLeftHand)& !isOver))? +1 : -1;

        /*Создаем и присваиваем исчисленую корректуру в угломер*/

        int angleCorrection = (isLeft ? +1 : -1) * new BigDecimal(angleToTarget*mainDistanceCoef).setScale(0, RoundingMode.HALF_UP).intValue()
                + znak * new BigDecimal((double)distanceToTarget/100*mainProtractorStep).setScale(0, RoundingMode.HALF_UP).intValue();

        /*Создаем и присваиваем исчисленую метровую корректуру в дальность */

        int distanceCorrection = (isOver ? -1 : +1) * distanceToTarget;

        /*Создаем и присваиваем исчисленую корректуру в дальность в делениях прицела*/

        int scaleCorrection = (isOver ? -1 : +1) * new BigDecimal((double)distanceToTarget / 100 * valueOfScale)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        /*Создаем и присваиваем переменную currentCorrection*/
        /*если исчисленые корректуры равны нулю, создать нулевую ссылку*/
        if(angleCorrection==0 & distanceCorrection==0) currentCorrection = null;

        else currentCorrection = new Correction((distanceCorrection < 0), Math.abs(distanceCorrection), Math.abs(scaleCorrection),
                (angleCorrection < 0), Math.abs(angleCorrection) );

        /*Возвращаем массив с двумя разными описаниями одного разрыва*/
        return new String[]{burstRejectionDescription, burstDistanceDescription};
    }

    /*Метод возвращающй описание боевого порядка*/

    @Override
    public String getFormotion() {
        return new StringBuilder().append("Дальність командира - ").append((int)mainCommanderDistance)
                .append(".\n Дальність ціли - ").append((int)troopDistance).append(".\n Поправка на зміщення - ")
                .append(ArtilleryMilsUtil.convertToMilsFormat(mainParallax)).append(".\n Положення вогневої - ")
                .append(isLeftHand ? "зліва" : "зправа")
                .toString();
    }

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
        return String.format("КВ = %.1f\n" +
                                "КК = %s\n" +
                                "Вогнева %s", mainDistanceCoef,
                ArtilleryMilsUtil.convertToMilsFormat(mainProtractorStep),
                (isLeftHand ? "зліва" : "зправа"));
    }

    @Override
    public boolean isLastCorrectionSuccessful() {
        return isLastCorrectionSuccesful;
    }


    @Override
    public void setValueofScale(int valueofScale) {
        this.valueOfScale = valueofScale;
    }


    public String getAdjustmentTitle() {
        return adjustmentTitle;
    }

    @Override
    public int getValueOfScale() {
        return valueOfScale;
    }

    public String getArtylleryTypeName() {
        return artylleryTypeName;
    }

}

