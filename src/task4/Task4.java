package task4;

import task3.Task3;
import v1.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// завдання 4
public class Task4 {

    // параметри одного експерименту та його результат
    private static class Experiment {
        String id, change;
        double createDelay;
        double[] processDelays;
        int[] maxQueues;
        Task3.Chain result;
    }

    public static void main(String[] args) {
        double time = 10000.0;
        long seed = 1;

        List<Experiment> experiments = new ArrayList<>();
        Experiment e;

        experiments.add(base("0", "base"));

        e = base("1", "create x0.5");
        e.createDelay *= 0.5;
        experiments.add(e);

        e = base("2", "create x2");
        e.createDelay *= 2;
        experiments.add(e);

        e = base("3", "P1 x2");
        e.processDelays[0] *= 2;
        experiments.add(e);

        e = base("4", "P2 x2");
        e.processDelays[1] *= 2;
        experiments.add(e);

        e = base("5", "P3 x2");
        e.processDelays[2] *= 2;
        experiments.add(e);

        e = base("6", "P1 x0.5");
        e.processDelays[0] *= 0.5;
        experiments.add(e);

        e = base("7", "q1 = 0");
        e.maxQueues[0] = 0;
        experiments.add(e);

        e = base("8", "q2 x2");
        e.maxQueues[1] *= 2;
        experiments.add(e);

        e = base("9", "q3 = 0");
        e.maxQueues[2] = 0;
        experiments.add(e);

        for (Experiment ex : experiments) {
            ex.result = Task3.simulate(ex.createDelay, ex.processDelays, ex.maxQueues, time, seed, false, false);
        }

        System.out.println("=== Verification: one parameter changed per experiment, time = " + time + ", seed = " + seed);
        printInputTable(experiments);
        System.out.println();
        printOutputTable(experiments, time);
    }

    // копія базових параметрів, у якій далі змінюється один параметр
    private static Experiment base(String id, String change) {
        Experiment e = new Experiment();
        e.id = id;
        e.change = change;
        e.createDelay = Task3.CREATE_DELAY;
        e.processDelays = Task3.PROCESS_DELAYS.clone();
        e.maxQueues = Task3.MAX_QUEUES.clone();
        return e;
    }

    // таблиця 1: вхідні параметри (середні затримки та обмеження черг)
    private static void printInputTable(List<Experiment> experiments) {
        System.out.printf("%-4s %-12s %7s %7s %7s %7s %4s %4s %4s%n",
                "run", "change", "create", "P1", "P2", "P3", "q1", "q2", "q3");
        for (Experiment e : experiments) {
            System.out.printf(Locale.US, "%-4s %-12s %7.2f %7.2f %7.2f %7.2f %4d %4d %4d%n",
                    e.id, e.change, e.createDelay,
                    e.processDelays[0], e.processDelays[1], e.processDelays[2],
                    e.maxQueues[0], e.maxQueues[1], e.maxQueues[2]);
        }
    }

    // таблиця 2: вихідні характеристики кожного пристрою та мережі в цілому
    private static void printOutputTable(List<Experiment> experiments, double time) {
        System.out.printf("%-4s | %-23s | %-23s | %-23s | %-31s%n",
                "", "PROCESSOR_1", "PROCESSOR_2", "PROCESSOR_3", "NETWORK");
        System.out.printf("%-4s | %7s %7s %7s | %7s %7s %7s | %7s %7s %7s | %7s %7s %7s %7s%n",
                "run", "P(fail)", "queue", "load", "P(fail)", "queue", "load", "P(fail)", "queue", "load",
                "created", "out", "P(fail)", "balance");
        for (Experiment e : experiments) {
            Process[] p = e.result.processes;
            System.out.printf(Locale.US,
                    "%-4s | %7.4f %7.3f %7.3f | %7.4f %7.3f %7.3f | %7.4f %7.3f %7.3f | %7d %7d %7.4f %7s%n",
                    e.id,
                    p[0].getFailureProbability(), p[0].getMeanQueueLength(time), p[0].getMeanLoad(time),
                    p[1].getFailureProbability(), p[1].getMeanQueueLength(time), p[1].getMeanLoad(time),
                    p[2].getFailureProbability(), p[2].getMeanQueueLength(time), p[2].getMeanLoad(time),
                    e.result.create.getQuantity(), Task3.departures(e.result),
                    Task3.networkFailureProbability(e.result),
                    Task3.isBalanced(e.result) ? "OK" : "ERROR");
        }
    }
}
