package co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator;

import java.lang.reflect.Field;
import java.util.Comparator;

import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;

public class PropertyComparator<T> implements Comparator<T> {
    private final String propertyName;
    private final boolean ascending;

    public PropertyComparator(String propertyName, boolean ascending) {
        this.propertyName = propertyName;
        this.ascending = ascending;
    }

    @Override
    public int compare(T obj1, T obj2) {
        try {
            Field field = obj1.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            Object value1 = field.get(obj1);
            Object value2 = field.get(obj2);

            @SuppressWarnings("unchecked")
            Comparable<Object> comparableValue1 = (Comparable<Object>) value1;

            if (ascending) {
                return comparableValue1.compareTo(value2);
            } else {
                return comparableValue1.compareTo(value2) * -1;
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), e);
            return 0;
        }
    }
}
