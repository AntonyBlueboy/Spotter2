package ua.com.adjuster.adjuster.core.mils;

/**
 * Created by Rudolf on 07.03.2017.
 * Утилитарный клас для преобразования чисел в строки формата тысячных и назад
 */
public abstract class ArtilleryMilsUtil {

    /*Принимает угол в обычной записи, а возвращает в записи тысячных*/

    public static String convertToMilsFormat(double angleInDouble) {

        /*В целом метод очень грубый, стоит найти нормальную реализацию извлечения сотых долей*/

        int temp = (int) angleInDouble;
        int smallInt = ((int)((angleInDouble/100-temp/100)*100));
        int bigInt = temp/100;

        // костыль, призваный убрать непонятную потерю одной малой тысячной

        if ((bigInt*100 + smallInt) != temp) smallInt++;
        String small = smallInt + "";
        while (small.length() < 2) small = "0" + small;
        StringBuilder result = new StringBuilder();
        result.append(bigInt).append("-").append(small);
        return result.toString();
    }
    /*Принимает строку с углом в тысячных, а возвращает числом*/

    public static int convertToIntFormat(String angleInMils) throws NotMilsFormatException {
        try {
            String[] mils = angleInMils.split("-");
            int angle = Integer.parseInt(mils[0]) * 100 + Integer.parseInt(mils[1]);
            return angle;
        }
        catch (RuntimeException e) {throw new NotMilsFormatException();}
    }
}
