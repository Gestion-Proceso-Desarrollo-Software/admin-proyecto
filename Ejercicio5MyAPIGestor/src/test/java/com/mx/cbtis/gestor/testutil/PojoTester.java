package com.mx.cbtis.gestor.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Prueba un POJO/entidad sin conocer sus campos: constructor vacio, constructores con
 * parametros, cada setter/getter (comprobando que el valor guardado es el que se lee)
 * y cualquier otro metodo publico sin parametros (toString, hashCode, etc.).
 */
public final class PojoTester {

    private PojoTester() {
    }

    /**
     * Ejecuta las comprobaciones sobre la clase indicada y devuelve la instancia
     * poblada con los valores de prueba para que el metodo @Test pueda realizar
     * aserciones adicionales directas sobre el objeto resultante.
     */
    public static <T> T probar(Class<T> tipo) throws Exception {
        T objeto = tipo.getDeclaredConstructor().newInstance();
        assertNotNull(objeto);

        for (PropertyDescriptor pd : Introspector.getBeanInfo(tipo, Object.class).getPropertyDescriptors()) {
            Method lector = pd.getReadMethod();
            Method escritor = pd.getWriteMethod();
            if (escritor != null && lector != null) {
                Object valor = muestra(pd.getPropertyType());
                escritor.invoke(objeto, valor);
                assertEquals(valor, lector.invoke(objeto), "Propiedad " + pd.getName() + " de " + tipo.getSimpleName());
            } else if (lector != null) {
                lector.invoke(objeto);
            }
        }

        for (Constructor<?> c : tipo.getDeclaredConstructors()) {
            if (c.getParameterCount() > 0) {
                Object[] args = Arrays.stream(c.getParameterTypes()).map(PojoTester::muestra).toArray();
                Object construido = c.newInstance(args);
                assertNotNull(construido, "Constructor con parametros de " + tipo.getSimpleName());
            }
        }

        for (Method m : tipo.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                    && m.getParameterCount() == 0 && !m.isSynthetic()) {
                m.invoke(objeto);
            }
        }

        return objeto;
    }

    private static Object muestra(Class<?> t) {
        if (t == String.class) {
            return "x";
        }
        if (t == Integer.class || t == int.class) {
            return 7;
        }
        if (t == Long.class || t == long.class) {
            return 7L;
        }
        if (t == Double.class || t == double.class) {
            return 7.5d;
        }
        if (t == Boolean.class || t == boolean.class) {
            return Boolean.TRUE;
        }
        return null;
    }
}
