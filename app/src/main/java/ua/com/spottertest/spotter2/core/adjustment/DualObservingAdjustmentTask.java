package ua.com.spottertest.spotter2.core.adjustment;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

import ua.com.spottertest.spotter2.core.mils.ArtilleryMilsUtil;

/**
 * Created by Rudolf on 29.03.2017.
 *
 * Это охуеть какой сложный класс для инкапсуляции процессов пристрелки по сопряженному наблюдению.
 * Технически может быть аккуратно унаследован от пристрелки с дальномером, может когда-то...
 */
public class DualObservingAdjustmentTask extends AdjustmentTask {

    /*Название пристрелки*/

    private String adjustmentTitle;

    /*Минимальная и максимальная дальность нахождения артиллерийского наблюдателя от цели*/

    private static final int MINIMUM_SPOTTER_DISTANCE = 500;
    private static final int MAXIMUM_SPOTTER_DISTANCE = 3000;

    /*Минимальная дальность для всех систем, взята в общем для случаев непрямой наводки*/

    private static final int MINDISTANCE = 500;

    /*Математическй потолок дирекционного угла в советской системе*/

    private static final int MAXIMAL_VIEWING_ANGLE = 6000;

    /*Обьект рандомизации, для генерирования условий стрельбы и разрывов*/

    private Random random;

    /*булева переменная определяющая какой КСП основной*/

    private boolean isLeftCommanderMain;

    /*Дальность огневой*/

    private double troopDistance;

    /*Дир угол стрельбы*/

    private int troopAngle;

 /*Дальность наблюдателя левого КСП*/

    private double leftCommanderDistance;

    /*Дальность наблюдателя правого КСП*/

    private double rightCommanderDistance;

    /*Дальность основного наблюдателя (сюда присваиваеться одна из дальностей КСП)*/

    private double mainCommanderDistance;

    /*смещение левого КСП*/

    private double leftParallax;

    /*смещение правого КСП*/

    private double rightParallax;

    /*Смещение основного наблюдателя (для сопряженного наблюдения, береться смещение ближайшего по углу КСП)*/

    private double mainParallax;

    /*Количество делений прицела на 100 м*/

    private int valueOfScale;

    /*Флажок расположения огневой с левой стороны от основного наблюдателя*/

    private boolean isLeftHand;

    /*Коэфициент удаления основного наблюдателя*/

    private double mainDistanceCoef;

    /*Шаг угломера основного наблюдателя*/

    private int mainProtractorStep;

    /*Угол между двумя КСП*/

    private int gama;

    /*Углы наблюдения с КСП*/

    private int leftViewingAngle;
    private int rightViewingAngle;


    /*Положение огневой относительно обоих КСП*/

    private String FPposition;

    /*исчисляемые коэфициенты КСП*/

    private int leftCoef;
    private int rightCoef;

    /*Mаксимальная дальность работы орудия, его название*/

    private int maxDistanсe;
    private String artylleryTypeName;

    /*Тип орудия*/

    private ArtilleryType type;

    /*Текущая корректура*/

    private Correction currentCorrection;

    /*Флаг, была ли успешной крайняя корректура*/

    private boolean isLastCorrectionSuccesful;

    /*Поле, необходимое при парселизации*/

    public static final Parcelable.Creator<DualObservingAdjustmentTask> CREATOR =
            new Parcelable.Creator<DualObservingAdjustmentTask>(){
                public DualObservingAdjustmentTask createFromParcel(Parcel in){
                    return new DualObservingAdjustmentTask(in);
                }

                @Override
                public DualObservingAdjustmentTask[] newArray(int size) {
                    return new DualObservingAdjustmentTask[size];
                }
            };

    public DualObservingAdjustmentTask(ArtilleryType type) {
        this.type = type;
        this.artylleryTypeName = type.getTypeDescription();
        this.adjustmentTitle = "Пристрілка з спряженими спостереженнями";
        this.maxDistanсe = type.getMaxDistance();
        this.random = new Random(new Date().getTime());

        /*Генерируем дальность огневой между минимальной и максимальной дальностями*/

        this.troopDistance =  (this.MINDISTANCE + (random.nextInt((this.maxDistanсe - this.MINDISTANCE) + 1)))/10*10;

        /*Генерируем дальности наблюдателей. Выдерживаем принципы:
        * наблюдатель не находится дальше 3000 м
        * наблюдатель находится дальше огневой в 20% случаев*/

        this.leftCommanderDistance = generateSpotterDistance();
        this.rightCommanderDistance = generateSpotterDistance();

        /*Генерируем ответ на вопрос "основное ли левое КСП?"*/

        this.isLeftCommanderMain = random.nextBoolean();

        /*Дальность одного из КСП присваиваем переменной mainCommanderDistance, в зависимости от булевой переменной isLeftCommanderMain */

        this.mainCommanderDistance = this.isLeftCommanderMain ? this.leftCommanderDistance : this.rightCommanderDistance;

        /*Генерируем угол засечки gama между двух КСП, от 01-00 до 14-00.
        (В дальнейшем добавить вероятность выпадения углов от 15-00 до 29-00)*/

        this.gama = (100 + random.nextInt(1400));

        /*Генерируем дирекционные углы, для отображения наблюдения в дирекционных углах*/

        this.leftViewingAngle = random.nextInt(MAXIMAL_VIEWING_ANGLE);
        if (leftViewingAngle > gama) this.rightViewingAngle = this.leftViewingAngle - gama;
        else this.rightViewingAngle = this.leftViewingAngle + MAXIMAL_VIEWING_ANGLE - gama;

        /*генерируем смещения для КСП*/

        /*Если основное КСП - левое, то
        * присваиваем левому КСП рандомное смещение от 0-00 до 05-00
        * присваиваем смещение левого КСП смещению основного КСП
        * если разница по модулю гамы и смещения левого меньше смещения левого (что не подходит, ведь основное ксп левое),
         * то принимаем положение огневой как "слева", а смещение правого,
         * как сумму смещения левого и гаммы (обе КСП правее огневой)
        * аналогично делаем для случая с основным КСП - правым
        * согласно взаимного расположения КСП и огневой рассчитываем Дир угол стрельбы*/
        if (this.isLeftCommanderMain){
            leftParallax = (random.nextInt(501)) / 10 * 10;
            mainParallax = leftParallax;
            if (Math.abs(gama - leftParallax) < leftParallax){
                isLeftHand = true;
                rightParallax = gama + leftParallax;
                troopAngle = leftViewingAngle + (int) leftParallax;
                if (troopAngle > MAXIMAL_VIEWING_ANGLE) troopAngle = troopAngle - 6000;
                FPposition = "зліва від обох КСП";
            }
            else{
                isLeftHand = false;
                rightParallax = Math.abs(gama - leftParallax);
                if (leftParallax < gama ) {
                    FPposition = "між двома КСП";
                }
                else {
                    FPposition = "зправа від обох КСП";
                }
                troopAngle = leftViewingAngle - (int) leftParallax;
                if (troopAngle < 0) troopAngle = 6000 + troopAngle; //troopangle минусовый
            }
        }
        if (!this.isLeftCommanderMain){
            rightParallax = (random.nextInt(501)) / 10 * 10;
            mainParallax = rightParallax;
            if (Math.abs(gama - rightParallax) < rightParallax) {
                isLeftHand = false;
                leftParallax = gama + rightParallax;
                FPposition = "зправа від обох КСП";
                troopAngle = leftViewingAngle - (int) leftParallax;
                if (troopAngle < 0) troopAngle = 6000 + troopAngle; //troopangle минусовый
            }
            else{
                isLeftHand = true;
                leftParallax = Math.abs(gama - rightParallax);
                if (rightParallax < gama) FPposition = "між двома КСП";
                else FPposition = "зліва від обох КСП";
                troopAngle = rightViewingAngle + (int) rightParallax;
                if (troopAngle > MAXIMAL_VIEWING_ANGLE) troopAngle = troopAngle - 6000;
            }
        }

        /*Считаем коэфициент удаления, округляем до 1 знака после нуля*/

        this.mainDistanceCoef = new BigDecimal(this.mainCommanderDistance / this.troopDistance)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        /*Считаем шаг угломера, округляем до целого числа*/

        this.mainProtractorStep = new BigDecimal(this.mainParallax / (this.troopDistance * 0.01))
                .setScale(0, RoundingMode.HALF_UP).intValue();

        /*Считаем коэфициенты КСП*/

        this.leftCoef = new BigDecimal(leftCommanderDistance/gama).setScale(0, RoundingMode.HALF_UP).intValue();
        this.rightCoef = new BigDecimal(rightCommanderDistance/gama).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /*Конструктор для парселизации*/

    public DualObservingAdjustmentTask(Parcel parcel) {

        adjustmentTitle = parcel.readString();
        isLeftCommanderMain = parcel.readInt() == 1;
        troopDistance = parcel.readDouble();
        troopAngle = parcel.readInt();
        mainCommanderDistance = parcel.readDouble();
        leftCommanderDistance = parcel.readDouble();
        rightCommanderDistance = parcel.readDouble();
        mainParallax = parcel.readDouble();
        leftParallax = parcel.readDouble();
        rightParallax = parcel.readDouble();
        valueOfScale = parcel.readInt();

        isLeftHand = parcel.readInt() == 1;

        mainDistanceCoef = parcel.readDouble();
        mainProtractorStep = parcel.readInt();

        gama = parcel.readInt();
        leftViewingAngle = parcel.readInt();
        rightViewingAngle = parcel.readInt();
        FPposition = parcel.readString();
        leftCoef = parcel.readInt();
        rightCoef = parcel.readInt();
        maxDistanсe = parcel.readInt();
        artylleryTypeName = parcel.readString();
        isLastCorrectionSuccesful = parcel.readInt() == 1;
        currentCorrection = parcel.readParcelable(Correction.class.getClassLoader());

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

        int isLCM = isLeftCommanderMain ? 1 : 0;
        parcel.writeInt(isLCM);

        parcel.writeDouble(troopDistance);
        parcel.writeInt(troopAngle);
        parcel.writeDouble(mainCommanderDistance);
        parcel.writeDouble(leftCommanderDistance);
        parcel.writeDouble(rightCommanderDistance);
        parcel.writeDouble(mainParallax);
        parcel.writeDouble(leftParallax);
        parcel.writeDouble(rightParallax);
        parcel.writeInt(valueOfScale);

        int isLefth = isLeftHand ? 1 : 0;
        parcel.writeInt(isLefth);

        parcel.writeDouble(mainDistanceCoef);
        parcel.writeInt(mainProtractorStep);
        parcel.writeInt(gama);
        parcel.writeInt(leftViewingAngle);
        parcel.writeInt(rightViewingAngle);
        parcel.writeString(FPposition);
        parcel.writeInt(leftCoef);
        parcel.writeInt(rightCoef);
        parcel.writeInt(maxDistanсe);
        parcel.writeString(artylleryTypeName);
        int isLCSuccessful = isLastCorrectionSuccesful ? 1 : 0;
        parcel.writeInt(isLCSuccessful);
        parcel.writeParcelable(currentCorrection, 0);
    }

    /*Вспомогательный метод для генерирования дальностей наблюдателей. Генерируем дальность основного наблюдателя. Выдерживаем принципы:
        * наблюдатель не находится дальше 3000 м
        * наблюдатель находится дальше огневой в 20% случаев*/

    private double generateSpotterDistance(){
        double spotterDistance;
        if (this.troopDistance > MAXIMUM_SPOTTER_DISTANCE) spotterDistance = (MINIMUM_SPOTTER_DISTANCE +
                (random.nextInt((MAXIMUM_SPOTTER_DISTANCE - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
        else {
            int randomNumber = random.nextInt(100);
            if (randomNumber <= 20) {
                spotterDistance = (MINIMUM_SPOTTER_DISTANCE +
                        (random.nextInt((MAXIMUM_SPOTTER_DISTANCE - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
            } else
                spotterDistance = (MINIMUM_SPOTTER_DISTANCE +
                        (random.nextInt(((int) this.troopDistance - MINIMUM_SPOTTER_DISTANCE) + 1))) / 10 * 10;
        }

        return spotterDistance;
    }

    /*Получаем исчисленые пользователем КУ и ШУ, коэфициенты в контейнере для Object, сверяем и возвращаем вердикт*/

    @Override
    public String checkPreparingResult(Object... preparedCoefs) {
        double userDistanceCoef = (double)preparedCoefs[0];
        int userProtratorStep = (int) preparedCoefs[1];
        int userLeftCoef = (int) preparedCoefs[2];
        int userRightCoef = (int) preparedCoefs[3];
        boolean userIsLeftMain = (Boolean) preparedCoefs[4];
        boolean userIsLefthand = (Boolean) preparedCoefs[5];

        String result;

        if ((this.mainDistanceCoef == userDistanceCoef) & (this.mainProtractorStep == userProtratorStep)
                & (this.leftCoef == userLeftCoef) & (this.rightCoef == userRightCoef) & (this.isLeftCommanderMain == userIsLeftMain)
                & (this.isLeftHand == userIsLefthand)) result = "Розраховано вірно. Можна розпочати пристрілку.";
        else result =  String.format("Розраховано невірно.\n Запишіть, головне СП - %s," +
                "вогнева %s,\nКВ = %.1f, КК = %s.\nКоефіціенти: лівий - %d, правий - %d. \n Можна почнати пристрілку.",
                (this.isLeftCommanderMain ? "ліве" : "праве"), (this.isLeftHand ? "зліва" : "зправа"),
                this.mainDistanceCoef, ArtilleryMilsUtil.convertToMilsFormat(this.mainProtractorStep),
                this.leftCoef, this.rightCoef);
        return result;
    }

    @Override
    public String[] getBurstDescription() {
        int burstDistanceToTarget;
        boolean burstIsOver = random.nextBoolean();
        int randomNum = random.nextInt(100);

        /*В менее чем 25% случаев перелет/недолет достигает 310 м*/

        if (randomNum < 25) burstDistanceToTarget = (30 + random.nextInt(281))/10*10;

        /*В остальных случаях перелет/недолет до 200 м*/

        else burstDistanceToTarget = (30 + random.nextInt(179))/10*10;

        /* Так же готовим отклонения по угломеру от огневой*/

        boolean burstIsLeft = random.nextBoolean();
        int burstAngleToTarget;
        int znakvForm = burstIsLeft ? -1 : 1;

        randomNum = random.nextInt(100);

        /*В менее чем 20% случаев отклонение по угломеру может достигать 0-85*/

        if (randomNum < 20) burstAngleToTarget =  znakvForm*(5 + random.nextInt(80) / 10 * 10 + 5);

        /*В менее чем 40% случаев отклонение по угломеру может достигать 0-55*/

        else if (randomNum < 50) burstAngleToTarget = znakvForm*(5 + random.nextInt(50) / 10 * 10 + 5);

        /*В менее чем 60% случаев отклонение по угломеру может достигать 0-30*/

        else burstAngleToTarget = znakvForm*(5 + random.nextInt(30) / 10 * 10 + 5);

        /*Создаем и обнуляем наблюдения с двух КСП, в том числе и в виде дирекционных углов*/

        int leftAngle = 0;
        boolean isLeftForLeft = false;
        int rightAngle = 0;
        boolean isLeftForRight = false;
        int leftDirAngle = 0;
        int rightDirAngle = 0;

        /* знак, подставляемый в формулу зависит от значения перелета/недолета и положения огневой относительно КСП*/

        int znak = 0;
        if (isLeftHand){
            if (!burstIsOver) znak = -1;
            else znak = +1;
        }
        else {
            if (burstIsOver) znak = -1;
            else znak = +1;
        }


        /*Генерируем сначала наблюдение с ведущего КСП из корректуры в дальность и угломер.
        Затем на его основе генерируем наблюдение со второго КСП. Используем обращение двух основных формул.*/

        if (isLeftCommanderMain){
            leftAngle = -new BigDecimal((burstAngleToTarget - znak * (((double)burstDistanceToTarget)/100*mainProtractorStep))/mainDistanceCoef)
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            isLeftForLeft = leftAngle < 0;
            int distanceCorrectureMeters = (burstIsOver ? -1 : +1) * burstDistanceToTarget;
            rightAngle = -new BigDecimal((distanceCorrectureMeters - leftAngle*leftCoef)/rightCoef)
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            isLeftForRight = rightAngle < 0;
            leftAngle = Math.abs(leftAngle);
            rightAngle = Math.abs(rightAngle);
        }
        if (!isLeftCommanderMain){
            rightAngle = -new BigDecimal((burstAngleToTarget - znak * (((double)burstDistanceToTarget)/100*mainProtractorStep))/mainDistanceCoef)
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            isLeftForRight = rightAngle < 0;
            int distanceCorrectureMeters = (burstIsOver ? -1 : +1) * burstDistanceToTarget;
            leftAngle = new BigDecimal((distanceCorrectureMeters + rightAngle*rightCoef)/leftCoef)
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            isLeftForLeft = leftAngle < 0;
            rightAngle = Math.abs(rightAngle);
            leftAngle = Math.abs(leftAngle);
        }

        /*Округляем наблюдения до 0-05*/

        while (leftAngle%5 != 0) leftAngle++;
        while (rightAngle%5 != 0) rightAngle++;

        leftDirAngle = leftViewingAngle + (isLeftForLeft ? -1 : +1) * leftAngle;
        rightDirAngle = rightViewingAngle + (isLeftForRight ? -1 : +1) * rightAngle;



        /*Создаем строковый доклад о разрыве в двух вариантах*/

        String burstDescription = "(Лівий СП) Розрив по цілі!\n(Правий СП) Розрив по цілі!";
        String burstDirDescription = String.format("(Лівий СП) Розрив! Дирекційний - %s" +
                "\n(Правий СП) Розрив! Дирекційний - %s",
                ArtilleryMilsUtil.convertToMilsFormat(leftDirAngle), ArtilleryMilsUtil.convertToMilsFormat(rightDirAngle));

        if (leftAngle == 0 & rightAngle!=0) {
            burstDescription = "(Лівий СП) Розрив по цілі! \n(Правий СП) Розрив! " +
                    (isLeftForRight ? ", ліво " : ", право ")+ ArtilleryMilsUtil.convertToMilsFormat(rightAngle) + "!";
        }
        else if (leftAngle != 0 & rightAngle==0) {
            burstDescription =  "(Лівий СП) Розрив! " + (isLeftForLeft ? "ліво " : "право ")
                    + ArtilleryMilsUtil.convertToMilsFormat(leftAngle) + "!\n(Правий СП) Розрив по цели!";
        }
        else if ( leftAngle != 0 & rightAngle!=0) burstDescription =  "(Лівий СП) Розрив! " + (isLeftForLeft ? "ліво " : "право ")
                + ArtilleryMilsUtil.convertToMilsFormat(leftAngle) + "! \n(Правий СП) Розрив! " +
                (isLeftForRight ? ", ліво " : ", право ")+ ArtilleryMilsUtil.convertToMilsFormat(rightAngle) + "!";


        /*Готовим данные для корректуры, создаем обьект корректуры и присваиваем его переменной currentCorrection*/

        int scaleCorrection;
        int angleCorrection;

        /*Gересчитываем корректуру из образованых наблюдений, что б убрать фактор округления*/

        int distanсeCorrection = (isLeftForLeft ? -1 : +1) * leftAngle * leftCoef -
                (isLeftForRight ? -1 : +1) * rightAngle * rightCoef;

        scaleCorrection = new BigDecimal((double)distanсeCorrection/100 * valueOfScale).setScale(0, RoundingMode.HALF_UP).intValue();

        burstIsLeft = (isLeftCommanderMain ? isLeftForLeft : isLeftForRight);
        burstAngleToTarget = (isLeftCommanderMain ? leftAngle : rightAngle);

        angleCorrection = (burstIsLeft ? +1 : -1) * new BigDecimal(burstAngleToTarget*mainDistanceCoef).setScale(0, RoundingMode.HALF_UP).intValue()
                + znak * new BigDecimal(((double)Math.abs(burstDistanceToTarget))/100*mainProtractorStep).setScale(0, RoundingMode.HALF_UP).intValue();

        if(angleCorrection==0 & distanсeCorrection==0) currentCorrection = null;
        else currentCorrection = new Correction((scaleCorrection <= 0),Math.abs(distanсeCorrection), Math.abs(scaleCorrection),
                (angleCorrection <= 0), Math.abs(angleCorrection) );

        return new String[]{burstDescription, burstDirDescription};
    }

    @Override
    public String getFormotion() {
        StringBuilder formotion = new StringBuilder();


        formotion.append("Дальність лівого СП - ").append((int)leftCommanderDistance).append(" м")
                .append(", дирекційний лівого СП - ").append(ArtilleryMilsUtil.convertToMilsFormat(leftViewingAngle))
                .append(", дальність правого СП - ").append((int)rightCommanderDistance).append(" м")
                .append(", дирекційний правого СП - ").append(ArtilleryMilsUtil.convertToMilsFormat(rightViewingAngle))
                .append(", дальность вогневої  - ").append((int)troopDistance).append(" м")
                .append(", дирекційний стрільби - ").append(ArtilleryMilsUtil.convertToMilsFormat(troopAngle));

        return formotion.toString();
    }

    @Override
    public String checkCorrection(Correction userCorretion, boolean isScaleUsed) {
        StringBuilder result = new StringBuilder();
        if (currentCorrection.equals(userCorretion)) {
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
        String result =  String.format("КСП - %s\n" +
                        "КВ = %.1f\n" +
                        "КК = %s\n" +
                        "Вогнева %s\n" +
                        "К лівого - %d\n" +
                        "α лівого - %s\n" +
                        "К правого - %d\n" +
                        "α правого - %s", (isLeftCommanderMain ? "лівий" : "правий"),
                mainDistanceCoef, ArtilleryMilsUtil.convertToMilsFormat(mainProtractorStep),
                (isLeftHand ? "ліворуч" : "праворуч"), leftCoef, ArtilleryMilsUtil.convertToMilsFormat(leftViewingAngle),
                rightCoef, ArtilleryMilsUtil.convertToMilsFormat(rightViewingAngle));
        if (valueOfScale != 0) result = result + "\nΔП на 100 м = " + valueOfScale;
        return result;
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
