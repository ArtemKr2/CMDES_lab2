package task3;

import common.FunRand;
import v1.Create;
import v1.Element;
import v1.Model;
import v1.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// завдання 3
public class Task3 {
    // базові параметри моделі
    public static final double CREATE_DELAY = 2.0;
    public static final double[] PROCESS_DELAYS = {1.5, 1.5, 1.5};
    public static final int[] MAX_QUEUES = {3, 3, 3};

    // елементи побудованої моделі, щоб після імітації зчитати результати
    public static class Chain {
        public final Create create;
        public final Process[] processes;

        Chain(Create create, Process[] processes) {
            this.create = create;
            this.processes = processes;
        }
    }

    public static void main(String[] args) {
        // 3.1: короткий прогін з протоколом
        System.out.println("=== Run 3.1: base parameters with event protocol, time = 10");
        Chain chain1 = simulate(CREATE_DELAY, PROCESS_DELAYS, MAX_QUEUES, 10.0, 1, true, true);
        printNetworkSummary(chain1);

        // 3.2: довгий прогін без протоколу
        double time = 10000.0;
        System.out.println("\n=== Run 3.2: base parameters, time = " + time);
        Chain chain2 = simulate(CREATE_DELAY, PROCESS_DELAYS, MAX_QUEUES, time, 1, false, true);
        printNetworkSummary(chain2);
    }

    public static Chain simulate(double createDelay, double[] processDelays, int[] maxQueues, double time,
                                 long seed, boolean printProtocol, boolean printResult)
    {
        Create c = new Create(createDelay);
        c.setName("CREATOR");
        c.setDistribution("exp");

        List<Element> list = new ArrayList<>();
        list.add(c);

        Process[] p = new Process[processDelays.length];
        Element prev = c;
        for (int i = 0; i < p.length; i++) {
            p[i] = new Process(processDelays[i]);
            p[i].setName("PROCESSOR_" + (i + 1));
            p[i].setDistribution("exp");
            p[i].setMaxqueue(maxQueues[i]);
            p[i].setEventPriority(i + 1); // при збігу моментів пристрій далі по ланцюжку звільняється раніше

            prev.setNextElement(p[i]);
            prev = p[i];
            list.add(p[i]);
        }

        Model model = new Model(list);
        model.setPrintProtocol(printProtocol);
        model.setPrintResult(printResult);

        FunRand.setSeed(seed);
        model.simulate(time);
        return new Chain(c, p);
    }

    // сумарна кількість відмов у всіх пристроях
    public static int totalFailures(Chain chain) {
        int sum = 0;
        for (Process p : chain.processes) {
            sum += p.getFailure();
        }
        return sum;
    }

    // кількість заявок, які на момент закінчення ще в мережі (у чергах або в пристроях)
    public static int inSystem(Chain chain) {
        int sum = 0;
        for (Process p : chain.processes) {
            sum += p.getQueue() + p.getState();
        }
        return sum;
    }

    // кількість заявок, що пройшли всю мережу (вийшли з останнього пристрою)
    public static int departures(Chain chain) {
        return chain.processes[chain.processes.length - 1].getQuantity();
    }

    // ймовірність, що створена заявка отримає відмову в будь-якому з пристроїв
    public static double networkFailureProbability(Chain chain) {
        int created = chain.create.getQuantity();
        return created == 0 ? 0.0 : (double) totalFailures(chain) / created;
    }

    // кожна створена заявка або вийшла з мережі, або отримала відмову, або ще в мережі
    public static boolean isBalanced(Chain chain) {
        return chain.create.getQuantity() == departures(chain) + totalFailures(chain) + inSystem(chain);
    }

    public static void printNetworkSummary(Chain chain) {
        System.out.println("\nNETWORK"
                + "\n  created = " + chain.create.getQuantity()
                + "\n  departures (passed all processors) = " + departures(chain)
                + "\n  failures (all processors) = " + totalFailures(chain)
                + "\n  still in network = " + inSystem(chain)
                + "\n  network failure probability = "
                + String.format(Locale.US, "%.4f", networkFailureProbability(chain))
                + "\n  balance = " + (isBalanced(chain) ? "OK" : "ERROR"));
    }
}
