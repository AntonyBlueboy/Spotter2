package ua.com.spottertest.spotter2;

import java.math.BigDecimal;

/**
 * Created by Rudolf on 06.03.2017.
 * Класс инкапсулирующий статистику пользователя вне бд
 */
public class User {

    /*Имя юзера*/

    private String name;

    /*Количество выполненых задач*/

    private int tasks;

    /*Количество успешно выполненых задач*/

    private int successTasks;

    /*Количество ошибочно выполненых задач*/

    private int unSuccessTasks;

     /*Процент успешно выполненых задач*/

    private int percentageOfSuccess;

    /*Пустой конструктор для ручного заполнения полей*/

    public User() {
    }

    /*Конструктор для быстрого создания нулевого Юзера с именем*/

    public User(String name) {
        this.name = name;
        this.tasks = 0;
        this.successTasks = 0;
        this.unSuccessTasks = 0;
        this.percentageOfSuccess = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTasks() {
        return tasks;
    }

    public void setTasks(int tasks) {
        this.tasks = tasks;
    }

    public int getSuccessTasks() {
        return successTasks;
    }

    public void setSuccessTasks(int successTasks) {
        this.successTasks = successTasks;

    }

    public int getUnsuccessTasks() {
        return unSuccessTasks;
    }

    public void setUnsuccessTasks(int unSuccessTasks) {
        this.unSuccessTasks = unSuccessTasks;
    }

    public void setPercentageOfSuccess(int percentageOfSuccess) {
        this.percentageOfSuccess = percentageOfSuccess;
    }

    public int getPercentageOfSuccess() {
        return percentageOfSuccess;
    }

    /*Метод для пересчета процента успешных задач*/

    public void refreshPercentageOfSuccess() {
        this.percentageOfSuccess = new BigDecimal((double) successTasks/(double) tasks * 100)
                .setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    /*Увеличивает на 1 задачу число успешных задач и общее число*/

    public void incrementSuccessTasks(){
        successTasks++;
        tasks++;
        refreshPercentageOfSuccess();
    }

    /*Увеличивает на 1 задачу число ошибочных задач и общее число*/

    public void incrementUnsuccessTasks(){
        unSuccessTasks++;
        tasks++;
        refreshPercentageOfSuccess();
    }

    /*Очищает статистику*/

    public void clear(){
        tasks = 0;
        successTasks = 0;
        unSuccessTasks = 0;
    }
}
