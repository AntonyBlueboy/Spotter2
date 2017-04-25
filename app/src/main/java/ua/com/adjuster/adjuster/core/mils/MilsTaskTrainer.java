package ua.com.adjuster.adjuster.core.mils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

/**
 * Created by Rudolf on 03.04.2017.
 * Класс инкапсулирующий логику задач на "дуй в 1000"
 */
public class MilsTaskTrainer {

    public static final int DISTANCE_TASK = 1;
    public static final int ANGLE_TASK = 2;
    public static final int SIZE_TASK = 3;


    private int angleSize;
    private int distance;
    private double meterSize;
    private static Random random = new Random(new Date().getTime());
    private static final String [] object_with_size = {"висота танку", "довжина танку", "ширина танку", "висота людини",
            "висота стрілка на коліні", "висота телеграфного стовпа", "висота одноповерхового будинку з дахом", "висота БТР",
            "довжина БТР", "ширина БТР", "висота поверху житлового будинку", "висота заводської труби",
            "висота пасажирського залізничного вагону","ловжина пасажирського залізничного вагону", "ширина залізничного вагону",
            "висота залізничної цистерни", "висота товарного вагону","висота залізничної платформи", "висота вантажівки",
            "ширина вантажівки", "висота легкової автівки","ширина легкової автівки"};
    private static final double [] sizes_for_objects = {2.5, 7.3, 3.4, 1.7, 1.1, 6.0,  7.0, 1.9, 5.5, 2.6, 3.5, 30.0, 4.3, 25.0
            , 2.8, 3.0, 3.9, 1.6, 2.0, 2.3, 1.6, 1.5 };
    private static final String [] random_objects = {"Ширина окопу", "Фронт батареї", "Фронт опорного пункту", "Довжина колони",
            "Довжина ешелону", "Відстань від цілі до цілі"};


    public String getDistanceTask(){
        int object = random.nextInt(object_with_size.length);
        meterSize = sizes_for_objects[object];
        angleSize =1 + random.nextInt(20);
        distance = new BigDecimal(meterSize *1000/ angleSize).setScale(0, RoundingMode.HALF_UP).intValue();
        String result = String.format("Таблична %s %.1f м спостерігається під кутом %s.\nВизначте відстань до нього."
                , object_with_size[object], sizes_for_objects[object], ArtilleryMilsUtil.convertToMilsFormat(angleSize));
        return result;
    }

    public String getAngleTask(){
        String object = random_objects[random.nextInt(random_objects.length)];
        meterSize = (50 + random.nextInt(300));
        angleSize = 5 + random.nextInt(50);
        distance = new BigDecimal(meterSize *1000/ angleSize).setScale(0, RoundingMode.HALF_UP).intValue();
        String result = String.format("%s %d м.\nПід яким кутом ми спостерігаємо його на відстані %d м?", object ,
                (int) meterSize, distance);
        return result;
    }

    public String getSizeTask(){
        String object = random_objects[random.nextInt(random_objects.length)];
        meterSize = (50 + random.nextInt(300));
        angleSize = 5 + random.nextInt(50);
        distance = new BigDecimal(meterSize *1000/ angleSize).setScale(0, RoundingMode.HALF_UP).intValue();
        String result = String.format("%s спостерігається під кутом %s на відстані %d.\nЯка його лінійна величина в метрах",
                object, ArtilleryMilsUtil.convertToMilsFormat(angleSize), distance);
        return result;
    }

    public int getAngleSize() {
        return angleSize;
    }

    public int getDistance() {
        return distance;
    }

    public double getMeterSize() {
        return meterSize;
    }
}
