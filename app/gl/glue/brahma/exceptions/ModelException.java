package gl.glue.brahma.exceptions;

public class ModelException extends RuntimeException {

    private int id;
    private Class entityClass;

    public Class getEntityClass() {
        return entityClass;
    }

    public int getId() {
        return id;
    }

    public int getState() {
        return 400;
    }

    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Class entityClass, int id) {
        super(message);
        this.entityClass = entityClass;
        this.id = id;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
