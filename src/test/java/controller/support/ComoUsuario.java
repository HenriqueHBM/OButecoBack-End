package controller.support;


import org.springframework.security.test.context.support.WithSecurityContext;
import projeto.obuteco.model.enums.CargoEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithSecurityContext(factory = ComoUsuarioFactory.class)
public @interface ComoUsuario {
    CargoEnum cargo() default CargoEnum.ADMIN;
    long id() default 1L;
}
