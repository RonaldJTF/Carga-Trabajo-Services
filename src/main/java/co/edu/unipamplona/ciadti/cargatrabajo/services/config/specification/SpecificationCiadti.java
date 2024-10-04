package co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Permite construir consultas a partir de los atributos no nulos pasados en el objeto.
 * Se pueden ordenar los registros a partir del objeto 'orderBy' pasado como parámetro en el método constructor.
 * Si una propiedad de ordenamiento es un atributo del objeto como atributo del objeto como atributo... del objeto raíz,
 * estas deben de estar separadas por un punto (.), por ejemplo, del objeto de la clase Persona (objeto raíz),
 * se desea ordenar por el atributo 'nombre' de su atributo 'tipoDocumento' de la clase TipoDocumento,
 * así que la sintaxis a usar es tipoDocumento.nombre para la propiedad de ordenamiento.
 */
public class SpecificationCiadti<T> implements Specification<T> {

    private final T filter;
    private OrderBy orderBy;

    public SpecificationCiadti(T filter) {
        this.filter = filter;
    }

    public SpecificationCiadti(T filter, OrderBy orderBy) {
        this.filter = filter;
        this.orderBy = orderBy;
    }

    @Override
    public Predicate toPredicate(@SuppressWarnings("null") Root<T> root, @SuppressWarnings("null") CriteriaQuery<?> query, @SuppressWarnings("null") CriteriaBuilder criteriaBuilder) {
        Predicate predicate = generatePredicate(root, criteriaBuilder, filter.getClass(), filter);
        if (orderBy != null) {
            List<Order> orders = new ArrayList<>();
            for (OrderBy.OrderSpecifier orderSpecifier : orderBy.getOrderSpecifiers()) {
                Path<Object> orderPath = getPath(root, orderSpecifier.getProperty());
                Order order = orderSpecifier.isAscending() ? criteriaBuilder.asc(orderPath) : criteriaBuilder.desc(orderPath);
                orders.add(order);
            }
            query.orderBy(orders);
        }
        return predicate;
    }

    private <E> Path<Object> getPath(Path<E> path, String property) {
        if (property.contains(".")) {
            String[] nestedProperties = property.split("\\.");
            Path<Object> nestedPath = path.get(nestedProperties[0]);
            for (int i = 1; i < nestedProperties.length; i++) {
                nestedPath = nestedPath.get(nestedProperties[i]);
            }
            return nestedPath;
        } else {
            return path.get(property);
        }
    }

    private <E> Predicate generatePredicate(Path<E> path, CriteriaBuilder criteriaBuilder, Class<?> entityClass, Object filterObject) {
        List<Predicate> predicates = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(filterObject);
                if (value != null) {
                    if (field.getType().equals(String.class)) {
                        predicates.add(criteriaBuilder.like(criteriaBuilder.upper(path.get(field.getName())), "%" + value.toString().toUpperCase() + "%"));
                    } else if (field.getType().isPrimitive() || Number.class.isAssignableFrom(field.getType())) {
                        predicates.add(criteriaBuilder.equal(path.get(field.getName()), value));
                    }else if (Date.class.isAssignableFrom(field.getType()) ||
                                java.sql.Date.class.isAssignableFrom(field.getType()) ||
                                java.sql.Time.class.isAssignableFrom(field.getType()) ||
                                java.sql.Timestamp.class.isAssignableFrom(field.getType())) {
                        predicates.add(criteriaBuilder.equal(path.get(field.getName()), value));
                    } else {
                        Path<Object> nestedPath = path.get(field.getName());
                        predicates.add(this.generatePredicate(nestedPath, criteriaBuilder, field.getType(), value));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
