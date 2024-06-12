package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ActividadOutDTO;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraService extends CommonService<EstructuraEntity>{
    List<ActividadOutDTO> statisticsDependence(EstructuraEntity filter);

    List<EstructuraEntity> findAllFilteredByIds(List<Long> structureIds);

    Long findLastOrderByIdPadre(Long idPadre);

    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment);

    boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden,  Long id);

    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment);

}
