package ua.com.spottertest.spotter2.core.database;

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

   /*Среднее время задачи*/

    private long averigeTime;

    private int milTasks;

    private int successMilTasks;

    private int unSuccessMilTasks;

    private int percentageOfSuccessMilTasks;

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
        this.averigeTime = 0;
        this.milTasks = 0;
        this.successMilTasks = 0;
        this.unSuccessMilTasks = 0;
        this.percentageOfSuccessMilTasks = 0;
    }

    /*Метод для пересчета процента успешных задач*/

    public void refreshPercentageOfSuccess() {
        if (tasks != 0)this.percentageOfSuccess = new BigDecimal((double) successTasks/(double) tasks * 100)
                .setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    /*Метод для пересчета процента успешных задач на тысячные*/

    public void refreshPercentageOfSuccessMilTasks() {
        if (milTasks != 0)this.percentageOfSuccessMilTasks = new BigDecimal((double) successMilTasks/(double) milTasks * 100)
                .setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    /*Увеличивает на 1 задачу число успешных задач и общее число*/

    public void incrementSuccessTasks(long averigeTime){
        successTasks++;
        tasks++;
        refreshPercentageOfSuccess();
        refreshTime(averigeTime);
    }

    /*Увеличивает на 1 задачу число успешных задач на тысячные и общее число*/

    public void incrementSuccessMilTasks(){
        successMilTasks++;
        milTasks++;
        refreshPercentageOfSuccessMilTasks();
    }

    /*Увеличивает на 1 задачу число ошибочных задач и общее число*/

    public void incrementUnsuccessTasks(long averigeTime){
        unSuccessTasks++;
        tasks++;
        refreshPercentageOfSuccess();
        refreshTime(averigeTime);
    }

    /*Увеличивает на 1 задачу число ошибочных задач на тысячные и общее число*/

    public void incrementUnsuccessMilTasks(){
        unSuccessMilTasks++;
        milTasks++;
        refreshPercentageOfSuccess();
    }

    /*Очищает статистику, кроме времени*/

    public void clear(){
        tasks = 0;
        successTasks = 0;
        unSuccessTasks = 0;
        milTasks = 0;
        successMilTasks = 0;
        unSuccessMilTasks = 0;
    }

    private void refreshTime(long averigeTime){

        if(this.averigeTime == 0) this.averigeTime = averigeTime;
        else this.averigeTime = (this.averigeTime + averigeTime)/2;
    }

    public void pourInUserStats(User user){
        this.tasks += user.getTasks();
        this.successTasks += user.getSuccessTasks();
        this.unSuccessTasks += user.getUnsuccessTasks();
        this.refreshPercentageOfSuccess();
        this.averigeTime = (this.averigeTime + user.getAverigeTime())/2;
        this.milTasks += user.getMilTasks();
        this.successMilTasks += user.getSuccessMilTasks();
        this.unSuccessMilTasks += user.getUnSuccessMilTasks();
        this.refreshPercentageOfSuccessMilTasks();
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

    public int getPercentageOfSuccess() {
        return percentageOfSuccess;
    }

    public void setPercentageOfSuccess(int percentageOfSuccess) {
        this.percentageOfSuccess = percentageOfSuccess;
    }

    public long getAverigeTime() {
        return averigeTime;
    }

    public void setAverigeTime(long averigeTime) {
        this.averigeTime = averigeTime;
    }

    public int getMilTasks() {
        return milTasks;
    }

    public void setMilTasks(int milTasks) {
        this.milTasks = milTasks;
    }

    public int getSuccessMilTasks() {
        return successMilTasks;
    }

    public void setSuccessMilTasks(int successMilTasks) {
        this.successMilTasks = successMilTasks;
    }

    public int getUnSuccessMilTasks() {
        return unSuccessMilTasks;
    }

    public void setUnSuccessMilTasks(int unSuccessMilTasks) {
        this.unSuccessMilTasks = unSuccessMilTasks;
    }

    public int getPercentageOfSuccessMilTasks() {
        return percentageOfSuccessMilTasks;
    }

    public void setPercentageOfSuccessMilTasks(int percentageOfSuccessMilTasks) {
        this.percentageOfSuccessMilTasks = percentageOfSuccessMilTasks;
    }


}
