package ibis.deploy.monitoring.collection.exceptions;

public class BeyondAllowedRangeException extends Exception {
    public BeyondAllowedRangeException(String string) {
        super(string);
    }

    public BeyondAllowedRangeException() {
        super();
    }

    private static final long serialVersionUID = -5607306073519879401L;

}
