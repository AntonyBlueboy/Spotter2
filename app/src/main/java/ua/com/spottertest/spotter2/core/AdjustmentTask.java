package ua.com.spottertest.spotter2.core;

/**
 * Created by Rudolf on 07.03.2017.
 */
public abstract class AdjustmentTask {

    /*Константы - идентификаторы типов пристрелки.*/

    public static final int RANGEFINDER_TYPE = 0;

     /* метод выдает игроку наблюдения в виде строки*/

    abstract String getBurstDescription();

    /* Метод проверяет корректуру принятую в виде обьекта Correcture.*/

    abstract Correction getCorrection();

    /*Возвращает описанее боевого порядка*/

    abstract String getFormotion();
}
