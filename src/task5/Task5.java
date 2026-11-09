package task5;

import common.FunRand;
import task3.Task3;
import v2.Create;
import v2.Element;
import v2.Model;
import v2.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// завдання 5: багатоканальний Process (m ідентичних каналів з однією чергою)
public class Task5 {

    public static void main(String[] args) {
        // 5.1: m = 1 з параметрами прогону 1.2 - результати мають збігтися з 1.2
        System.out.println("=== Run 5.1: m = 1, Create exp(2), Process exp(1), maxqueue = 5, time = 1000, seed = 1"
                + " (must match run 1.2)");
        simulateSingle("exp", 2.0, "exp", 1.0, 5, 1, 1000.0, 1, false);

        // 5.2: m = 2, детерміновані затримки, протокол подій
        System.out.println("\n=== Run 5.2: m = 2, Create det(1), Process det(2.5), maxqueue = 1, time = 10");
        simulateSingle("", 1.0, "", 2.5, 1, 2, 10.0, 1, true);

        // 5.3: ланцюжок із завдання 3 з базовими параметрами, m = 1 (має збігтися з 3.2) і m = 2
        double time = 10000.0;
        long seed = 1;
        System.out.println("\n=== Run 5.3: chain from task 3 (base parameters), time = " + time + ", seed = " + seed);
        printChainHeader();
        printChainRow(1, simulateChain(1, time, seed), time);
        printChainRow(2, simulateChain(2, time, seed), time);
    }

    // модель Create -> Process (m каналів)
    private static Process simulateSingle(String distCreate, double delayCreate,
                                          String distProcess, double delayProcess,
                                          int maxQueue, int channels, double time, long seed, boolean printProtocol) {
        Create c = new Create(delayCreate);
        c.setName("CREATOR");
        c.setDistribution(distCreate);

        Process p = new Process(delayProcess);
        p.setName("PROCESSOR");
        p.setDistribution(distProcess);
        p.setMaxqueue(maxQueue);
        p.setChannels(channels);

        c.setNextElement(p);

        List<Element> list = new ArrayList<>();
        list.add(c);
        list.add(p);

        Model model = new Model(list);
        model.setPrintProtocol(printProtocol);
        model.setPrintResult(true);

        FunRand.setSeed(seed);
        model.simulate(time);
        return p;
    }

    // ланцюжок Create -> P1 -> P2 -> P3 з базовими параметрами завдання 3, у кожному пристрої m каналів;
    private static Element[] simulateChain(int channels, double time, long seed) {
        Create c = new Create(Task3.CREATE_DELAY);
        c.setName("CREATOR");
        c.setDistribution("exp");

        List<Element> list = new ArrayList<>();
        list.add(c);

        Element prev = c;
        for (int i = 0; i < Task3.PROCESS_DELAYS.length; i++) {
            Process p = new Process(Task3.PROCESS_DELAYS[i]);
            p.setName("PROCESSOR_" + (i + 1));
            p.setDistribution("exp");
            p.setMaxqueue(Task3.MAX_QUEUES[i]);
            p.setChannels(channels);
            p.setEventPriority(i + 1);

            prev.setNextElement(p);
            prev = p;
            list.add(p);
        }

        Model model = new Model(list);
        model.setPrintProtocol(false);
        model.setPrintResult(false);

        FunRand.setSeed(seed);
        model.simulate(time);
        return list.toArray(new Element[0]);
    }

    private static void printChainHeader() {
        System.out.printf("%-4s | %-23s | %-23s | %-23s | %-31s%n",
                "", "PROCESSOR_1", "PROCESSOR_2", "PROCESSOR_3", "NETWORK");
        System.out.printf("%-4s | %7s %7s %7s | %7s %7s %7s | %7s %7s %7s | %7s %7s %7s %7s%n",
                "m", "P(fail)", "queue", "load", "P(fail)", "queue", "load", "P(fail)", "queue", "load",
                "created", "out", "P(fail)", "balance");
    }

    private static void printChainRow(int channels, Element[] elements, double time) {
        int created = elements[0].getQuantity();
        int failures = 0, inSystem = 0;
        StringBuilder row = new StringBuilder(String.format("%-4d", channels));

        for (int i = 1; i < elements.length; i++) {
            Process p = (Process) elements[i];
            failures += p.getFailure();
            inSystem += p.getQueue() + p.getState(); // state - кількість зайнятих каналів
            row.append(String.format(Locale.US, " | %7.4f %7.3f %7.3f",
                    p.getFailureProbability(), p.getMeanQueueLength(time), p.getMeanLoad(time)));
        }

        int out = elements[elements.length - 1].getQuantity();
        boolean balanced = created == out + failures + inSystem;
        row.append(String.format(Locale.US, " | %7d %7d %7.4f %7s",
                created, out, created == 0 ? 0.0 : (double) failures / created, balanced ? "OK" : "ERROR"));
        System.out.println(row);
    }
}
