package org.jmetano.precondicoes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PreCondicao {

	Class<?> alvo() default Object.class;

	String casoDeTeste();
}
