package ua.com.spottertest.spotter2.core;

/**
 * Created by Rudolf on 07.03.2017.
 * Класс описывает корректуру в прицел и доворот.
 */
public class Correction {

    /*дальность меньше или нет*/

    private boolean isLower;

    /*Дальность изменить на количество метров*/

    private int distanceCorrection;

    /*Дальность изменить на количество делений прицела*/

    private int scaleCorrection;

    /*углемер левее или нет*/

    private boolean isTotheLeft;

    /*угломер изменить на количество тысячных*/

    private int angleCorrection;

    public Correction(boolean isLower, int distanceCorrection, int scaleCorrection, boolean isTotheLeft, int angleCorrection) {
        this.isLower = isLower;
        this.distanceCorrection = distanceCorrection;
        this.scaleCorrection = scaleCorrection;
        this.isTotheLeft = isTotheLeft;
        this.angleCorrection = angleCorrection;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Correction that = (Correction) o;

        if (isLower != that.isLower) return false;
        if (distanceCorrection != that.distanceCorrection) return false;
        if (scaleCorrection != that.scaleCorrection) return false;
        if (isTotheLeft != that.isTotheLeft) return false;
        return angleCorrection == that.angleCorrection;
    }

    @Override
    public int hashCode() {
        int result = (isLower ? 1 : 0);
        result = 31 * result + distanceCorrection;
        result = 31 * result + scaleCorrection;
        result = 31 * result + (isTotheLeft ? 1 : 0);
        result = 31 * result + angleCorrection;
        return result;
    }

    public boolean isLower() {
        return isLower;
    }

    public int getDistanceCorrection() {
        return distanceCorrection;
    }

    public int getScaleCorrection() {
        return scaleCorrection;
    }

    public boolean isTotheLeft() {
        return isTotheLeft;
    }

    public int getAngleCorrection() {
        return angleCorrection;
    }
}
