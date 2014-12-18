package gl.glue.brahma.util;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "user.login", "user.password", "user.gender", "user.name", "user.birthdate"
    };

    public static final String[] USER_MODIFICABLE_FIELDS = {
            "user.login", "user.password", "user.gender", "user.name", "user.birthdate",
            "user.surname1", "user.surname2", "user.avatar", "user.nationalId", "user.meta"
    };

}
