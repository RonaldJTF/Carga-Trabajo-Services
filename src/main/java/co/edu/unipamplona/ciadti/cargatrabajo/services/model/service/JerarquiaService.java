package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;

public interface JerarquiaService extends CommonService<JerarquiaEntity>{
    JerarquiaEntity findByIdOrganigramaAndIdDependencia(Long idOrganigrama, Long idDependencia);
    Long findLastOrderByIdPadre(Long idPadre);
    boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden, Long id);
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment);
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment);
}
