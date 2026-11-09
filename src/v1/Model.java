package v1;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private final List<Element> list;
    private double tnext, tcurr;
    private int eventCount; // кількість виконаних подій
    private boolean printProtocol = true; // друкувати протокол подій
    private boolean printResult = true;

    public Model(List<Element> elements) {
        list = new ArrayList<>(elements);
        tnext = 0.0;
        tcurr = 0.0;
        eventCount = 0;
    }

    public void simulate(double time) {
        while (tcurr < time) {
            // момент найближчої події серед усіх елементів
            tnext = Double.MAX_VALUE;
            for (Element e : list) {
                if (e.getTnext() < tnext) {
                    tnext = e.getTnext();
                }
            }

            // дораховуємо статистику до time
            if (tnext > time) {
                doStatistics(time - tcurr);
                setTcurr(time);
                break;
            }

            doStatistics(tnext - tcurr); // збір статистик. стан не змінювався на інтервалі [tcurr, tnext)
            setTcurr(tnext);

            if (printProtocol) {
                System.out.println("\nt = " + tcurr);
            }

            // виконуємо всі події цього моменту за спаданням пріоритету
            Element e;
            while ((e = nextEventAt(tcurr)) != null) {
                if (printProtocol) {
                    System.out.println("  event in " + e.getName());
                }
                e.outAct();
                eventCount++;
            }

            if (printProtocol) {
                printInfo();
            }
        }
        if (printResult) {
            printResult();
        }
    }

    // елемент з найвищим пріоритетом серед тих, чия подія настає в момент t
    private Element nextEventAt(double t) {
        Element best = null;
        for (Element e : list) {
            if (e.getTnext() == t && (best == null || e.getEventPriority() > best.getEventPriority())) {
                best = e;
            }
        }
        return best;
    }

    private void doStatistics(double delta) {
        for (Element e : list) {
            e.doStatistics(delta);
        }
    }

    private void setTcurr(double t) {
        tcurr = t;
        for (Element e : list) {
            e.setTcurr(tcurr);
        }
    }

    public void printInfo() {
        for (Element e : list) {
            e.printInfo();
        }
    }

    public void printResult() {
        System.out.println("\n-------------RESULTS-------------");
        System.out.println("simulation time = " + tcurr + ", events = " + eventCount);
        for (Element e : list) {
            e.printResult(tcurr);
        }
    }

    public void setPrintProtocol(boolean printProtocol) {
        this.printProtocol = printProtocol;
    }

    public void setPrintResult(boolean printResult) { this.printResult = printResult; }

    public double getTcurr() {
        return tcurr;
    }

    public int getEventCount() {
        return eventCount;
    }
}
