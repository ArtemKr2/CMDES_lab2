package v3;

public class Create extends Element {

    public Create(double delay) {
        super(delay);
        setTnext(0.0); // імітація розпочинається з події Create
    }

    @Override
    public void outAct() {
        super.outAct();
        setTnext(getTcurr() + getDelay()); // момент наступного надходження

        sendToNext(); // вибір наступного елемента за ймовірностями маршрутів
    }
}
