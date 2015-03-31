package gl.glue.brahma.exceptions;

public class InvalidStateException extends ModelException {

    @Override
    public int getState() {
        return 409;
    }

    public InvalidStateException(String details) {
        super("Invalid state: " + details);
    }

    public InvalidStateException(String details, Class entityClass, int id) {
        super("Invalid state: " + entityClass.getSimpleName() + " #" + id + ": " + details, entityClass, id);
    }

}
