package gl.glue.brahma.util;

import org.apache.commons.lang3.ArrayUtils;

public class ModelSecurity {

    public static final String[] USER_REQUIRED_FIELDS = {
            "password", "email"
    };

    public static final String[] USER_PROFILE_MODIFIABLE_FIELDS = {
            "gender", "name", "birthdate", "surname1", "surname2", "avatar", "nationalId",
            "meta.address.*", "meta.cv.*", "meta.account"
    };

    public static final String[] USER_ACCOUNT_MODIFIABLE_FIELDS = {
            "login", "email"
    };

    public static final String[] USER_MODIFIABLE_FIELDS =
            ArrayUtils.addAll(USER_ACCOUNT_MODIFIABLE_FIELDS, USER_PROFILE_MODIFIABLE_FIELDS);

    public static final String[] CREATE_SESSION_REQUIRED_FIELDS = { "service" };
    public static final String[] BOOK_SESSION_REQUIRED_FIELDS = { "service", "startDate" };

}
