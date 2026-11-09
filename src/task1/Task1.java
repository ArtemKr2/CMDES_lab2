package task1;

import common.FunRand;
import v1.Create;
import v1.Element;
import v1.Model;
import v1.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// завдання 1
public class Task1 {

    public static void main(String[] args) {
        // 1.1: детермінований прогін з протоколом подій
        System.out.println("=== Run 1.1: Create det(1), Process det(1.5), maxqueue = 1, time = 10");
        simulate("", 1.0, "", 1.5, 1, 10.0, 1, true);

        // 1.2-1.6: базовий випадок і зміна одного параметра
        long seed = 1;
        double time = 1000.0;
        System.out.println("\n=== Runs 1.2-1.6: one parameter changed per run, time = " + time + ", seed = " + seed);
        printHeader();
        experiment("1.2", "base", "exp", 2.0, "exp", 1.0, 5, time, seed);
        experiment("1.3", "create x0.5", "exp", 1.0, "exp", 1.0, 5, time, seed);
        experiment("1.4", "process x2", "exp", 2.0, "exp", 2.0, 5, time, seed);
        experiment("1.5", "process x4", "exp", 2.0, "exp", 4.0, 5, time, seed);
        experiment("1.6", "maxqueue 0", "exp", 2.0, "exp", 1.0, 0, time, seed);
    }

    // будує модель Create -> Process, виконує імітацію і повертає Process для зчитування результатів
    private static Process simulate(String distCreate, double delayCreate,
                                    String distProcess, double delayProcess,
                                    int maxQueue, double time, long seed, boolean print) {
        Create c = new Create(delayCreate);
        c.setName("CREATOR");
        c.setDistribution(distCreate);

        Process p = new Process(delayProcess);
        p.setName("PROCESSOR");
        p.setDistribution(distProcess);
        p.setMaxqueue(maxQueue);

        c.setNextElement(p);

        List<Element> list = new ArrayList<>();
        list.add(c);
        list.add(p);

        Model model = new Model(list);
        model.setPrintProtocol(print);
        model.setPrintResult(print);

        FunRand.setSeed(seed);
        model.simulate(time);
        return p;
    }

    // один прогін без протоколу + рядок зведеної таблиці
    private static void experiment(String id, String change,
                                   String distCreate, double delayCreate,
                                   String distProcess, double delayProcess,
                                   int maxQueue, double time, long seed) {
        Process p = simulate(distCreate, delayCreate, distProcess, delayProcess, maxQueue, time, seed, false);

        // кожна заявка, що надійшла або обслужена або в черзі або в пристрої або отримала відмову
        boolean balanced = p.getArrivals() == p.getQuantity() + p.getQueue() + p.getState() + p.getFailure();

        System.out.printf(Locale.US, "%-4s %-12s %-9s %-9s %-5d %8d %7d %7d %8.4f %10.4f %9.4f %8s%n",
                id, change,
                describe(distCreate, delayCreate), describe(distProcess, delayProcess), maxQueue,
                p.getArrivals(), p.getQuantity(), p.getFailure(),
                p.getFailureProbability(), p.getMeanQueueLength(time), p.getMeanLoad(time),
                balanced ? "OK" : "ERROR");
    }

    private static void printHeader() {
        System.out.printf("%-4s %-12s %-9s %-9s %-5s %8s %7s %7s %8s %10s %9s %8s%n",
                "run", "change", "create", "process", "maxq",
                "arrivals", "served", "failure", "P(fail)", "mean queue", "mean load", "balance");
    }

    private static String describe(String distribution, double delay) {
        return (distribution.isEmpty() ? "det" : distribution) + "(" + delay + ")";
    }
}
