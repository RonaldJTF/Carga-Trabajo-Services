package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.DependenciaDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraService extends CommonService<EstructuraEntity>{
    List<EstructuraEntity> findAllFilteredByIds(List<Long> structureIds);

    Long findLastOrderByIdPadre(Long idPadre);

    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment);

    boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden,  Long id);

    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment);

    List<DependenciaDTO> findAllDependencies() throws CiadtiException;

    List<EstructuraEntity> findByIdPadre(Long idPadre);

    Long findTypologyIdOfStructure(Long id);

    List<ActividadDTO> getTimeStatistics(Long structureId);

}
