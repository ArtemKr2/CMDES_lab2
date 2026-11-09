package v3;

public class Dispose extends Element {
    public Dispose() {
        super(0.0);
        setName("DISPOSE");
        // власних подій немає,і tnext залишається Double.MAX_VALUE назавжди, тому Model ніколи не обирає цей елемент
    }

    // заявка, що надійшла, одразу залишає модель
    @Override
    public void inAct() {
        outAct(); // quantity++
    }

    @Override
    public void printInfo() {
        System.out.println(getName() + " quantity = " + getQuantity());
    }

    @Override
    public void printResult(double simTime) {
        System.out.println(getName() + " quantity (left the network) = " + getQuantity());
    }
}
