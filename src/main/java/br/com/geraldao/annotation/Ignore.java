package br.com.geraldao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação criada para utilização junto a anotação @Column do JPA. Utilizando desta anotação, ao utilizar procedure, o @column será ignorado.
 * 
 * @author victor.bello
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
