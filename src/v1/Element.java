package v1;

import common.ExceptionInvalidTimeDelay;
import common.FunRand;

public class Element {
    private String name;
    private double tnext;
    private double delayMean, delayDev;
    private String distribution;
    private int quantity;
    private double tcurr;
    private int state;
    private Element nextElement; // зв'язування з наступним елементом
    private int eventPriority; // пріоритет події при збігу моментів часу (більше значення - раніше)
    private static int nextId = 0;
    private int id;

    public Element() {
        this(1.0);
    }

    public Element(double delay) {
        this("element" + nextId, delay);
    }

    public Element(String nameOfElement, double delay) {
        name = nameOfElement;
        tnext = Double.MAX_VALUE;
        delayMean = delay;
        distribution = "";
        tcurr = 0.0;
        state = 0;
        nextElement = null;
        eventPriority = 0;
        id = nextId;
        nextId++;
    }

    // генерує часову затримку за заданим законом розподілу
    public double getDelay() {
        double delay;
        try {
            delay = switch (distribution.toLowerCase()) {
                case "exp" -> FunRand.exp(delayMean);
                case "norm" -> // delayMean - середнє, delayDev - стандартне відхилення
                        FunRand.norm(delayMean, delayDev);
                case "unif" -> // delayMean - середнє, delayDev - півширена інтервалу [mean - dev, mean + dev]
                        FunRand.unif(delayMean - delayDev, delayMean + delayDev);
                case "" -> // детермінована затримка
                        delayMean;
                default -> throw new ExceptionInvalidTimeDelay("невідомий закон розподілу \"" + distribution + "\"");
            };
        } catch (ExceptionInvalidTimeDelay ex) {
            throw new ExceptionInvalidTimeDelay(name + ": " + ex.getMessage());
        }

        if (delay < 0) {
            throw new ExceptionInvalidTimeDelay(name + ": від'ємна затримка " + delay);
        }
        return delay;
    }

    public void inAct() {

    }

    public void outAct() {
        quantity++;
    }

    // накопичення статистики на інтервалі delta (протягом якого стан не змінювався)
    public void doStatistics(double delta) {

    }

    public void printInfo() {
        System.out.println(name + " state = " + state
                + " quantity = " + quantity
                + " tnext = " + formatTime(tnext));
    }

    public void printResult(double simTime) {
        System.out.println(name + " quantity = " + quantity);
    }

    protected static String formatTime(double t) {
        return t == Double.MAX_VALUE ? "-" : String.valueOf(t);
    }

    public double getDelayMean() {
        return delayMean;
    }

    public void setDelayMean(double delayMean) {
        this.delayMean = delayMean;
    }

    public double getDelayDev() {
        return delayDev;
    }

    public void setDelayDev(double delayDev) {
        this.delayDev = delayDev;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTcurr() {
        return tcurr;
    }

    public void setTcurr(double tcurr) {
        this.tcurr = tcurr;
    }

    public double getTnext() {
        return tnext;
    }

    public void setTnext(double tnext) {
        this.tnext = tnext;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Element getNextElement() {
        return nextElement;
    }

    public void setNextElement(Element nextElement) {
        this.nextElement = nextElement;
    }

    public int getEventPriority() {
        return eventPriority;
    }

    public void setEventPriority(int eventPriority) {
        this.eventPriority = eventPriority;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
