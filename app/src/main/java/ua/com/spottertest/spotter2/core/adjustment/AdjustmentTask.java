package ua.com.spottertest.spotter2.core.adjustment;

import android.os.Parcelable;

/**
 * Created by Rudolf on 07.03.2017.
 * Абстрактный класс - родитель различных пристрелок, по совместительству контейнер
 * с идентификаторами пристрелок в виде констант
 * Наследует интерфейс для автоупаковки и распаковки обьекта при передаче между активти
 */
public abstract class AdjustmentTask implements Parcelable{

    /*Константы - идентификаторы типов пристрелки.*/

    public static final int RANGE_FINDER_TYPE = 1;
    public static final int DUAL_OBSERVINGS_TYPE = 2;
    public static final int WORLD_SIDES_TYPE = 3;

     /* метод выдает игроку наблюдения в виде строки, массив для различных разновидностей доклада*/

    abstract public String[] getBurstDescription();

    /*Возвращает описанее боевого порядка*/

    abstract public String getFormotion();

    /*метод для проверки результатов подготовки стрельбы, аргумент - для разных пристрелок унифицированый, расшряемый контейнер,
    * возвращает либо подтверждение, либо корректные коэфициенты*/

    abstract public String checkPreparingResult( Object... preparedCoefs);

    /*Метод проверки корректуры. Получает от активити обьект корректуры и флаг использования дП на 100
    * Возвращает сообщение о результатах стрельбы и выставляет флаг успешной корректировки*/

    abstract public String checkCorrection(Correction userCorretion, boolean isScaleUsed);

    /*Геттеры обязательные для всех пристрелок*/

    abstract public int getValueOfScale();

    abstract public boolean isLastCorrectionSuccessful();

    abstract public String getAdjustmentTitle();

    abstract public String getArtylleryTypeName();

    abstract public void setValueofScale(int valueofScale);

    /*Метод возвращающй коефициенты для пристрелки в строке*/

    abstract public String getCoefsDescription();





}
