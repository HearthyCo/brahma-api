package gl.glue.brahma.util;

import org.apache.commons.lang3.ArrayUtils;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "password", "email"
    };

    public static final String[] USER_PROFILE_MODIFICABLE_FIELDS = {
            "gender", "name", "birthdate", "surname1", "surname2", "avatar", "nationalId"
    };

    public static final String[] USER_ACCOUNT_MODIFICABLE_FIELDS = {
            "login", "password", "email"
    };

    public static final String[] USER_MODIFICABLE_FIELDS =
            ArrayUtils.addAll(USER_ACCOUNT_MODIFICABLE_FIELDS, USER_PROFILE_MODIFICABLE_FIELDS);

    public static final String[] CREATE_SESSION_REQUIRED_FIELDS = { "service" };
    public static final String[] BOOK_SESSION_REQUIRED_FIELDS = { "service", "startDate" };

}
