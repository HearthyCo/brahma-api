package gl.glue.brahma.util;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "password", "email"
    };

    public static final String[] USER_MODIFICABLE_FIELDS = {
            "login", "password", "email", "gender", "name", "birthdate",
            "surname1", "surname2", "avatar", "nationalId", "meta"
    };

    public static final String[] SESSION_REQUIRED_FIELDS = { "service", "state" };

}
