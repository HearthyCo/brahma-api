package actions;

import play.mvc.With;

import java.lang.annotation.*;

@With(ProfessionalAuthAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface ProfessionalAuth {
}
