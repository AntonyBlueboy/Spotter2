package ua.com.spottertest.spotter2.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolf on 07.03.2017.
 * * Энам для перечисления артиллерийских систем с их максимальной дальностями
 */
public enum ArtilleryType {

     /*Минимальные дальности стрельбы, как и дальность наблюдения принимаются за 500*/

    CANNON_122_D30(15300, "Д-30 122-мм гаубиця"),
    CANNON_152_D20(17400, "Д-20 152-мм гаубиця"),
    CANNON_152_MSTA_B(24700, "2А65 «МСТА-Б» 152-мм гаубиця"),
    MORTAR_120_SANI(7100, "2С12 «Сані» 120-мм міномет"),
    MORTAR_82_PIDNOS(3900, "2Б14 «Піднос» 82-мм міномет"),
    SPA_152_AKACIA(17000, "2С3 «Акація» 152-мм САУ "),
    SPA_152_MSTA_S(29000, "2С19 «МСТА-С» 152-мм САУ "),
    SPA_122_GVOZDIKA(15200, "2С1 «Гвоздика» 122-мм САУ");


    private String typeDescription;
    private int maxDistance;

    ArtilleryType(int maxDistance, String typeDescription) {
        this.maxDistance = maxDistance;
        this.typeDescription = typeDescription;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    /*Метод возвращает перечень всех описаний типов арты*/

    public static List<String> getDescriptions(){
        List<String> descriptions = new ArrayList<>();
        for (ArtilleryType type : ArtilleryType.values()) descriptions.add(type.getTypeDescription());
        return descriptions;
    }

    /*Метод принимает описание типа арты, а возвращает обьект ArtilleryType или null*/

    public static ArtilleryType getTypeForDescription(String typeDescription){
        for (ArtilleryType type : ArtilleryType.values()){
            if (type.getTypeDescription().equals(typeDescription)) return type;
        }
        return null;
    }
}
