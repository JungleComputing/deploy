package ibis.deploy;

public interface StateListener {

    public void stateUpdated(State newState, Exception exception);
}
