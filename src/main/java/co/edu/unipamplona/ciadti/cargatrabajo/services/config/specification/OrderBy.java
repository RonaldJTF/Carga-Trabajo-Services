package co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification;

import java.util.ArrayList;
import java.util.List;

public class OrderBy {

    private final List<OrderSpecifier> orderSpecifiers;

    public OrderBy() {
        this.orderSpecifiers = new ArrayList<>();
    }

    public OrderBy(String property, boolean ascending) {
        this.orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(new OrderSpecifier(property, ascending));
    }

    public void addOrder(String property, boolean ascending) {
        orderSpecifiers.add(new OrderSpecifier(property, ascending));
    }

    public List<OrderSpecifier> getOrderSpecifiers() {
        return orderSpecifiers;
    }

    public static class OrderSpecifier {
        private final String property;
        private final boolean ascending;

        public OrderSpecifier(String property, boolean ascending) {
            this.property = property;
            this.ascending = ascending;
        }

        public String getProperty() {
            return property;
        }

        public boolean isAscending() {
            return ascending;
        }
    }
}