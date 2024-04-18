package co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator;

import java.util.Comparator;
import java.util.List;

public class MultiPropertyComparator<T> implements Comparator<T> {
    private final List<PropertyComparator<T>> propertyComparators;
    public MultiPropertyComparator(List<PropertyComparator<T>> propertyComparators) {
        this.propertyComparators = propertyComparators;
    }

    @Override
    public int compare(T obj1, T obj2) {
        for (PropertyComparator<T> propertyComparator : propertyComparators) {
            int result = propertyComparator.compare(obj1, obj2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
