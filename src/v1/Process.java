package v1;

public class Process extends Element {

    private int queue, maxqueue, failure;
    private int arrivals; // кількість заявок, що надійшли в елемент
    private double meanQueue; // накопичена сума queue * delta
    private double meanLoad; // накопичена сума state * delta

    public Process(double delay) {
        super(delay);
        queue = 0;
        maxqueue = Integer.MAX_VALUE;
        meanQueue = 0.0;
        meanLoad = 0.0;
        setEventPriority(1); // при збігу моментів звільнення пристрою виконується раніше за надходження
    }

    @Override
    public void inAct() {
        arrivals++;
        if (getState() == 0) {
            double delay = getDelay();
            setState(1);
            setTnext(getTcurr() + delay);
        } else {
            if (queue < maxqueue) {
                queue++;
            } else {
                failure++;
            }
        }
    }

    @Override
    public void outAct() {
        super.outAct();

        if (queue > 0) {
            // заявка з черги одразу займає пристрій що звільнився
            double delay = getDelay();
            queue--;
            setTnext(getTcurr() + delay);
        } else {
            setState(0);
            setTnext(Double.MAX_VALUE);
        }

        if (getNextElement() != null) {
            getNextElement().inAct();
        }
    }

    @Override
    public void doStatistics(double delta) {
        meanQueue += queue * delta;
        meanLoad += getState() * delta; // завдання 2
    }

    public double getFailureProbability() {
        return arrivals == 0 ? 0.0 : (double) failure / arrivals;
    }

    public double getMeanQueueLength(double simTime) {
        return meanQueue / simTime;
    }

    // завдання 2
    public double getMeanLoad(double simTime) {
        return meanLoad / simTime;
    }

    @Override
    public void printInfo() {
        System.out.println(getName() + " state = " + getState()
                + " queue = " + queue
                + " quantity = " + getQuantity()
                + " failure = " + failure
                + " tnext = " + formatTime(getTnext()));
    }

    @Override
    public void printResult(double simTime) {
        System.out.println(getName()
                + "\n  arrivals = " + arrivals
                + "\n  quantity (served) = " + getQuantity()
                + "\n  failure = " + failure
                + "\n  failure probability = " + getFailureProbability()
                + "\n  mean queue length = " + getMeanQueueLength(simTime)
                + "\n  mean load = " + getMeanLoad(simTime)); // завдання 2
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getMaxqueue() {
        return maxqueue;
    }

    public void setMaxqueue(int maxqueue) {
        this.maxqueue = maxqueue;
    }

    public int getFailure() {
        return failure;
    }

    public int getArrivals() {
        return arrivals;
    }
}
