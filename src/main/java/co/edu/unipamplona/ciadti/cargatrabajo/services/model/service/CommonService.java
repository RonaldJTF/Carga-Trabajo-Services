package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.Collection;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

public interface CommonService<T> {
    T findById(Long id) throws CiadtiException;
    List<T> findAll();
    T save (T entity);
    List<T> save(Collection<T> entities);
    void deleteByProcedure(Long id, String register);
    List<T> findAllFilteredBy(T filter);
}
