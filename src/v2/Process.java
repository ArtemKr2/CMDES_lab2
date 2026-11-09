package v2;

import java.util.Arrays;

public class Process extends Element {
    private int queue, maxqueue, failure;
    private int arrivals; // кількість заявок, що надійшли в елемент
    private double meanQueue; // накопичена сума queue * delta
    private double meanLoad; // накопичена сума (кількість зайнятих каналів) * delta

    private int channels; // кількість каналів
    private int[] channelState; // стан кожного каналу
    private double[] channelTnext; // момент завершення обслуговування в кожному каналі
    private double[] channelLoad; // накопичена зайнятість кожного каналу

    public Process(double delay) {
        super(delay);
        queue = 0;
        maxqueue = Integer.MAX_VALUE;
        meanQueue = 0.0;
        meanLoad = 0.0;
        setChannels(1);
        setEventPriority(1); // при збігу моментів звільнення пристрою виконується раніше за надходження
    }

    // задає кількість каналів; викликається до початку імітації
    public void setChannels(int channels) {
        if (channels < 1) {
            throw new IllegalArgumentException(getName() + ": кількість каналів має бути >= 1");
        }
        this.channels = channels;
        channelState = new int[channels];
        channelTnext = new double[channels];
        channelLoad = new double[channels];
        Arrays.fill(channelTnext, Double.MAX_VALUE);
        updateState();
    }

    @Override
    public void inAct() {
        arrivals++;
        int free = findFreeChannel();
        if (free >= 0) {
            double delay = getDelay();
            channelState[free] = 1;
            channelTnext[free] = getTcurr() + delay;
            updateState();
        } else {
            if (queue < maxqueue) {
                queue++;
            } else {
                failure++;
            }
        }
    }

    // завершення обслуговування в одному каналі; якщо одночасно завершуються кілька каналів,
    // Model викличе outAct ще раз, бо tnext знову дорівнюватиме поточному часу
    @Override
    public void outAct() {
        super.outAct();

        int ch = findFinishedChannel();
        if (queue > 0) {
            // заявка з черги одразу займає канал, що звільнився
            double delay = getDelay();
            queue--;
            channelTnext[ch] = getTcurr() + delay;
        } else {
            channelState[ch] = 0;
            channelTnext[ch] = Double.MAX_VALUE;
        }
        updateState();

        if (getNextElement() != null) {
            getNextElement().inAct();
        }
    }

    // перший вільний канал за номером або -1, якщо всі зайняті
    private int findFreeChannel() {
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    // канал, у якому обслуговування завершується в поточний момент
    private int findFinishedChannel() {
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 1 && channelTnext[i] == getTcurr()) {
                return i;
            }
        }
        throw new IllegalStateException(getName() + ": немає каналу, що завершує обслуговування в t = " + getTcurr());
    }

    // state - кількість зайнятих каналів, tnext - найближче завершення серед усіх каналів
    private void updateState() {
        int busy = 0;
        double tmin = Double.MAX_VALUE;
        for (int i = 0; i < channels; i++) {
            busy += channelState[i];
            if (channelTnext[i] < tmin) {
                tmin = channelTnext[i];
            }
        }
        setState(busy);
        setTnext(tmin);
    }

    @Override
    public void doStatistics(double delta) {
        meanQueue += queue * delta;
        meanLoad += getState() * delta;
        for (int i = 0; i < channels; i++) {
            channelLoad[i] += channelState[i] * delta;
        }
    }

    public double getFailureProbability() {
        return arrivals == 0 ? 0.0 : (double) failure / arrivals;
    }

    public double getMeanQueueLength(double simTime) {
        return meanQueue / simTime;
    }

    // середня кількість зайнятих каналів
    public double getMeanBusyChannels(double simTime) {
        return meanLoad / simTime;
    }

    // середнє завантаження одного каналу (від 0 до 1)
    public double getMeanLoad(double simTime) {
        return meanLoad / simTime / channels;
    }

    // середнє завантаження каналу з номером i
    public double getChannelLoad(int i, double simTime) {
        return channelLoad[i] / simTime;
    }

    @Override
    public void printInfo() {
        StringBuilder tnexts = new StringBuilder();
        for (int i = 0; i < channels; i++) {
            if (i > 0) {
                tnexts.append(", ");
            }
            tnexts.append(formatTime(channelTnext[i]));
        }
        System.out.println(getName() + " state = " + getState()
                + " channels = " + Arrays.toString(channelState)
                + " queue = " + queue
                + " quantity = " + getQuantity()
                + " failure = " + failure
                + " tnext = [" + tnexts + "]");
    }

    @Override
    public void printResult(double simTime) {
        StringBuilder loads = new StringBuilder();
        for (int i = 0; i < channels; i++) {
            loads.append("\n    channel ").append(i + 1).append(" load = ").append(getChannelLoad(i, simTime));
        }
        System.out.println(getName()
                + "\n  channels = " + channels
                + "\n  arrivals = " + arrivals
                + "\n  quantity (served) = " + getQuantity()
                + "\n  failure = " + failure
                + "\n  failure probability = " + getFailureProbability()
                + "\n  mean queue length = " + getMeanQueueLength(simTime)
                + "\n  mean busy channels = " + getMeanBusyChannels(simTime)
                + "\n  mean load (per channel) = " + getMeanLoad(simTime)
                + loads);
    }

    public int getChannels() {
        return channels;
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
