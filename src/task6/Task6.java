package task6;

import common.FunRand;
import v3.Create;
import v3.Dispose;
import v3.Element;
import v3.Model;
import v3.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// завдання 6
public class Task6 {

    public static void main(String[] args) {
        // 6.1: невелика мережа з поверненням у той самий пристрій і в попередній
        System.out.println("=== Run 6.1a: example network with event protocol, time = 10");
        simulate(exampleNetwork(), 10.0, 1, true, true);

        System.out.println("\n=== Run 6.1b: the same network, time = 10000 (route shares must be close to probabilities)");
        List<Element> example = exampleNetwork();
        simulate(example, 10000.0, 1, false, true);
        printNetworkSummary(example);

        // 6.2: мережа з лекції 5, порівняння з результатами аналітичного розрахунку
        double time = 100000.0;
        System.out.println("\n=== Run 6.2: network from lecture 5, time = " + time + ", seed = 1");
        List<Element> lecture = lectureNetwork();
        simulate(lecture, time, 1, false, false);
        printLectureComparison(lecture, time);
        printNetworkSummary(lecture);
    }

    // Create -> P1 -> (P2 з ймовірністю 0.7 | P3 з ймовірністю 0.3)
    // P2 -> (вихід 0.8 | повернення в P2 0.2), P3 -> (вихід 0.5 | повернення в P1 0.5)
    private static List<Element> exampleNetwork() {
        Create c = new Create(2.0);
        c.setName("CREATOR");
        c.setDistribution("exp");

        Process p1 = process("PROCESSOR_1", 1.0, 5, 1);
        Process p2 = process("PROCESSOR_2", 1.0, 5, 1);
        Process p3 = process("PROCESSOR_3", 1.0, 5, 1);
        Dispose d = new Dispose();

        c.setNextElement(p1);
        p1.addNextElement(p2, 0.7);
        p1.addNextElement(p3, 0.3);
        p2.addNextElement(d, 0.8);
        p2.addNextElement(p2, 0.2); // повернення в той самий пристрій
        p3.addNextElement(d, 0.5);
        p3.addNextElement(p1, 0.5); // повернення в попередній пристрій

        return new ArrayList<>(List.of(c, p1, p2, p3, d));
    }

    // мережа з лекції 5: Create exp(2) -> SMO_1 exp(0.6);
    // з SMO_1: у SMO_2 (0.15), SMO_3 (0.13), SMO_4 (0.3), вихід (0.42);
    // SMO_2 exp(0.3), SMO_3 exp(0.4), SMO_4 exp(0.1) з двома каналами повертають заявки в SMO_1; черги необмежені
    private static List<Element> lectureNetwork() {
        Create c = new Create(2.0);
        c.setName("CREATOR");
        c.setDistribution("exp");

        Process s1 = process("SMO_1", 0.6, Integer.MAX_VALUE, 1);
        Process s2 = process("SMO_2", 0.3, Integer.MAX_VALUE, 1);
        Process s3 = process("SMO_3", 0.4, Integer.MAX_VALUE, 1);
        Process s4 = process("SMO_4", 0.1, Integer.MAX_VALUE, 2);
        Dispose d = new Dispose();

        c.setNextElement(s1);
        s1.addNextElement(s2, 0.15);
        s1.addNextElement(s3, 0.13);
        s1.addNextElement(s4, 0.30);
        s1.addNextElement(d, 0.42);
        s2.setNextElement(s1);
        s3.setNextElement(s1);
        s4.setNextElement(s1);

        return new ArrayList<>(List.of(c, s1, s2, s3, s4, d));
    }

    private static Process process(String name, double delay, int maxQueue, int channels) {
        Process p = new Process(delay);
        p.setName(name);
        p.setDistribution("exp");
        p.setMaxqueue(maxQueue);
        p.setChannels(channels);
        return p;
    }

    private static void simulate(List<Element> elements, double time, long seed,
                                 boolean printProtocol, boolean printResult) {
        Model model = new Model(elements);
        model.setPrintProtocol(printProtocol);
        model.setPrintResult(printResult);
        FunRand.setSeed(seed);
        model.simulate(time);
    }

    // порівняння з результатами аналітичного розрахунку з аналітичними результатами
    private static void printLectureComparison(List<Element> elements, double time) {
        double[] analyticQueue = {1.786, 0.003, 0.004, 0.00001};
        double[] analyticBusy = {0.714, 0.054, 0.062, 0.036};

        System.out.printf("%-6s | %10s %10s | %12s %12s%n",
                "SMO", "queue", "analytic", "busy devices", "analytic");
        int k = 0;
        for (Element e : elements) {
            if (e instanceof Process) {
                Process p = (Process) e;
                System.out.printf(Locale.US, "%-6s | %10.5f %10.5f | %12.4f %12.4f%n",
                        p.getName(), p.getMeanQueueLength(time), analyticQueue[k],
                        p.getMeanBusyChannels(time), analyticBusy[k]);
                k++;
            }
        }

        // частки маршрутів з SMO_1 порівняно із заданими ймовірностями
        Process s1 = (Process) elements.get(1);
        int total = 0;
        for (int i = 0; i < s1.getNextElements().size(); i++) {
            total += s1.getRouteCount(i);
        }
        System.out.println("\nroutes from " + s1.getName() + ":");
        for (int i = 0; i < s1.getNextElements().size(); i++) {
            System.out.printf(Locale.US, "  -> %-8s p = %.2f, share = %.4f%n",
                    s1.getNextElements().get(i).getName(), s1.getRouteProbability(i),
                    (double) s1.getRouteCount(i) / total);
        }

        // скільки разів у середньому заявка проходить SMO_1 (теоретично 1 / 0.42)
        int created = elements.get(0).getQuantity();
        System.out.printf(Locale.US, "mean visits to %s per job = %.4f (analytic %.4f)%n",
                s1.getName(), (double) s1.getArrivals() / created, 1 / 0.42);
    }

    // кожна створена заявка або вийшла з мережі, або отримала відмову, або ще в мережі
    private static void printNetworkSummary(List<Element> elements) {
        int created = 0, left = 0, failures = 0, inNetwork = 0;
        for (Element e : elements) {
            if (e instanceof Create) {
                created += e.getQuantity();
            } else if (e instanceof Dispose) {
                left += e.getQuantity();
            } else if (e instanceof Process) {
                Process p = (Process) e;
                failures += p.getFailure();
                inNetwork += p.getQueue() + p.getState(); // state - кількість зайнятих каналів
            }
        }
        boolean balanced = created == left + failures + inNetwork;

        System.out.println("\nNETWORK"
                + "\n  created = " + created
                + "\n  left the network = " + left
                + "\n  failures = " + failures
                + "\n  still in network = " + inNetwork
                + "\n  network failure probability = "
                + String.format(Locale.US, "%.4f", created == 0 ? 0.0 : (double) failures / created)
                + "\n  balance = " + (balanced ? "OK" : "ERROR"));
    }
}
