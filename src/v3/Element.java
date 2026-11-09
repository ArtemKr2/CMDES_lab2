package v3;

import common.ExceptionInvalidTimeDelay;
import common.FunRand;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Element {
    private String name;
    private double tnext;
    private double delayMean, delayDev;
    private String distribution;
    private int quantity;
    private double tcurr;
    private int state;
    private final List<Element> nextElements = new ArrayList<>(); // наступні елементи
    private final List<Double> probabilities = new ArrayList<>(); // ймовірнсті переходу
    private final List<Integer> routeCounts = new ArrayList<>(); // кількість відправлених заявок по маршруту
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
        System.out.println(name + " quantity = " + quantity + routesToString());
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

    // якщо потрібно задати тільки один маршрут
    public void setNextElement(Element nextElement) {
        nextElements.clear();
        probabilities.clear();
        routeCounts.clear();
        if (nextElement != null) {
            addNextElement(nextElement, 1.0);
        }
    }

    // додає маршрут до наступного елемента із заданою ймовірністю
    public void addNextElement(Element nextElement, double probability) {
        if (nextElement == null) {
            throw new IllegalArgumentException(name + ": наступний елемент не може бути null");
        }
        if (!(probability > 0 && probability <= 1)) {
            throw new IllegalArgumentException(name + ": ймовірність маршруту має бути в (0, 1], отримано " + probability);
        }
        nextElements.add(nextElement);
        probabilities.add(probability);
        routeCounts.add(0);
    }

    // передає заявку наступному елементу, обраному за ймовірностями маршрутів
    protected void sendToNext() {
        int i = chooseRoute();
        if (i >= 0) {
            routeCounts.set(i, routeCounts.get(i) + 1);
            nextElements.get(i).inAct();
        }
    }

    // номер обраного маршруту або -1, якщо маршрутів немає
    private int chooseRoute() {
        int n = nextElements.size();
        if (n == 0) {
            return -1;
        }
        if (n == 1) {
            return 0; // випадкове число не генерується для 1 маршруту
        }

        double sum = 0;
        for (double p : probabilities) {
            sum += p;
        }
        if (Math.abs(sum - 1.0) > 1e-9) { // допуск, бо, наприклад, 0.7 + 0.2 + 0.1 != 1.0 у double
            throw new IllegalStateException(name + ": сума ймовірностей маршрутів = " + sum + ", а має бути 1");
        }

        double r = FunRand.unif(0.0, 1.0);
        double cumulative = 0;
        for (int i = 0; i < n; i++) {
            cumulative += probabilities.get(i);
            if (r < cumulative) {
                return i;
            }
        }
        return n - 1;
    }

    // кількість заявок, відправлених кожним маршрутом, та їх частка (виводиться, якщо маршрутів більше одного)
    protected String routesToString() {
        if (nextElements.size() < 2) {
            return "";
        }
        int total = 0;
        for (int c : routeCounts) {
            total += c;
        }
        StringBuilder sb = new StringBuilder("\n  routes:");
        for (int i = 0; i < nextElements.size(); i++) {
            double share = total == 0 ? 0.0 : (double) routeCounts.get(i) / total;
            sb.append(String.format(Locale.US, "\n    -> %s: p = %.3f, sent = %d (%.4f)",
                    nextElements.get(i).getName(), probabilities.get(i), routeCounts.get(i), share));
        }
        return sb.toString();
    }

    public List<Element> getNextElements() {
        return nextElements;
    }

    public int getRouteCount(int i) {
        return routeCounts.get(i);
    }

    public double getRouteProbability(int i) {
        return probabilities.get(i);
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
