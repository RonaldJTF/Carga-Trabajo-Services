package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.Collection;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

public interface CommonService<T> {
    T findById(Long id) throws CiadtiException;
    Iterable<T> findAll();
    T save (T entity);
    Iterable<T> save(Collection<T> entities);
    void deleteByProcedure(Long id, String register);
    Iterable<T> findAllFilteredBy(T filter);
}
