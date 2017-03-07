package ua.com.spottertest.spotter2.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

/**
 * Created by Rudolf on 07.03.2017.
 * Класс описывающий занятие по пристрелке с наблюдениями дальномерщика
 */

public class RangefinderAdjustmentTask extends AdjustmentTask {

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

    public RangefinderAdjustmentTask(ArtilleryType type) {
        this.type = type;
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

    /*Тут будет также конструктор для парселизации класса*/

    /*Получаем исчисленые пользователем КУ и ШУ, сверяем и возвращаем true или false*/

    public boolean checkPreparingResult( double userDistanceCoef, int userProtratorStep)
    {
        boolean isCorrect = false;
        if (this.mainDistanceCoef == userDistanceCoef & this.mainProtractorStep == userProtratorStep) isCorrect = true;
        return isCorrect;
    }

    /*Генерируем текст наблюдения по залпу, вида "Перелет/недолет х, лево/право у", рассчитываем и
    *  инициализируем переменную currentCorrection
    *  ВНИМАНИЕ: нулевые наблюдения отсутствуют. Их нужно будет ввести при лоработке*/

    @Override
    public String getBurstDescription() {

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

        /*Формируем текстовое описание разрыва.*/

        String burstDescription = "Наблюдаю разрыв! \n" + (isOver ? "Перелет " : "Недолет ")
                + distanceToTarget +
                (isLeft ? ", лево " : ", право ")+ ArtilleryMilsUtil.convertToMilsFormat(angleToTarget) + "!";

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

        /*Возвращаем описание разрыва*/
        return burstDescription;
    }

    @Override
    public Correction getCorrection() {
        return currentCorrection;
    }

    @Override
    public String getFormotion() {
        return new StringBuilder().append("Дальность командира - ").append((int)mainCommanderDistance)
                .append(".\n Дальность цели - ").append((int)troopDistance).append(".\n Поправка на смещение - ")
                .append(ArtilleryMilsUtil.convertToMilsFormat(mainParallax)).append(".\n Положение огневой - ")
                .append(isLeftHand ? "слева" : "справа")
                .toString();
    }

    public void setValueOfScale(int valueOfScale) {
        this.valueOfScale = valueOfScale;
    }

    public double getMainDistanceCoef() {
        return mainDistanceCoef;
    }

    public int getMainProtractorStep() {
        return mainProtractorStep;
    }

    public boolean isLeftHand() {
        return isLeftHand;
    }

    public int getValueOfScale() {
        return valueOfScale;
    }
}

