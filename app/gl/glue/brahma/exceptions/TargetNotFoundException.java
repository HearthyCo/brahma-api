package gl.glue.brahma.exceptions;

public class TargetNotFoundException extends ModelException {

    @Override
    public int getState() {
        return 404;
    }

    public TargetNotFoundException(Class entityClass, int id) {
        super("Target not found: " + entityClass.getSimpleName() + " #" + id, entityClass, id);
    }

}
