package v2;

public class Create extends Element {

    public Create(double delay) {
        super(delay);
        setTnext(0.0); // імітація розпочинається з події Create
    }

    @Override
    public void outAct() {
        super.outAct();
        setTnext(getTcurr() + getDelay()); // момент наступного надходження

        if (getNextElement() != null) {
            getNextElement().inAct();
        }
    }
}
