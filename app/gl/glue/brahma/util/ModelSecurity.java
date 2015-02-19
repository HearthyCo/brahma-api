package gl.glue.brahma.util;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "password", "email"
    };

    public static final String[] USER_MODIFICABLE_FIELDS = {
            "login", "password", "email", "gender", "name", "birthdate",
            "surname1", "surname2", "avatar", "nationalId", "meta"
    };

    public static final String[] CREATE_SESSION_REQUIRED_FIELDS = { "service" };
    public static final String[] BOOK_SESSION_REQUIRED_FIELDS = { "service", "startDate" };

}
