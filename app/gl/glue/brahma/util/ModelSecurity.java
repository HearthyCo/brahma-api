package gl.glue.brahma.util;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "login", "password", "gender", "name", "birthdate"
    };

    public static final String[] USER_MODIFICABLE_FIELDS = {
            "login", "password", "gender", "name", "birthdate",
            "surname1", "surname2", "avatar", "nationalId", "meta"
    };

}
