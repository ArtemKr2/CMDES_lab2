package common;

import java.util.Random;

// генератори випадкових чисел за законами розподілу
public class FunRand {

    private static Random random = new Random();

    private FunRand() {

    }

    // встановлення певного сіда для відтворюваності
    public static void setSeed(long seed) {
        random = new Random(seed);
    }

    // експоненційний розподіл
    public static double exp(double timeMean) {
        if (timeMean < 0) {
            throw new ExceptionInvalidTimeDelay("exp: від'ємне середнє timeMean = " + timeMean);
        }

        double a = 1.0 - random.nextDouble();
        return -timeMean * Math.log(a);
    }

    // рівномірний розподіл
    public static double unif(double timeMin, double timeMax) {
        if (timeMin < 0) {
            throw new ExceptionInvalidTimeDelay("unif: від'ємна нижня межа timeMin = " + timeMin);
        }
        if (timeMax < timeMin) {
            throw new ExceptionInvalidTimeDelay("unif: timeMax = " + timeMax
                    + " менше за timeMin = " + timeMin);
        }
        return timeMin + random.nextDouble() * (timeMax - timeMin);
    }

    // нормальний закон розподілу
    public static double norm(double timeMean, double timeDeviation) {
        if (timeMean < 0) {
            throw new ExceptionInvalidTimeDelay("norm: від'ємне середнє timeMean = " + timeMean);
        }
        if (timeDeviation < 0) {
            throw new ExceptionInvalidTimeDelay("norm: від'ємне відхилення timeDeviation = "
                    + timeDeviation);
        }
        double a;
        do {
            a = timeMean + timeDeviation * random.nextGaussian();
        } while (a < 0); // перегенеровуємо, якщо від'ємне отримали
        return a;
    }

    // емпіричний закон розподілу за табличним методом
    public static double empiric(double[] x, double[] y) {
        int n = x.length;
        if (n < 2 || y.length != n) {
            throw new ExceptionInvalidTimeDelay("empiric: масиви x і y мають бути однакової довжини >= 2");
        }
        if (y[0] != 0.0 || y[n - 1] != 1.0) {
            throw new ExceptionInvalidTimeDelay("empiric: має бути y[0] = 0 і y[n-1] = 1");
        }
        if (x[0] < 0) {
            throw new ExceptionInvalidTimeDelay("empiric: від'ємне значення x[0] = " + x[0]);
        }
        for (int i = 1; i < n; i++) {
            if (x[i] < x[i - 1] || y[i] < y[i - 1]) {
                throw new ExceptionInvalidTimeDelay("empiric: x і y мають бути неспадними");
            }
        }

        double r = random.nextDouble(); // r з [0, 1)
        for (int i = 1; i < n; i++) {
            // відрізки з нульовою ймовірністю (y[i] == y[i-1]) пропускаються
            if (r <= y[i] && y[i] > y[i - 1]) {
                return x[i - 1] + (r - y[i - 1]) * (x[i] - x[i - 1]) / (y[i] - y[i - 1]);
            }
        }
        return x[n - 1]; // сюди не доходимо, бо r < 1 = y[n-1]
    }
}