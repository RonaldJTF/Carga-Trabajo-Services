package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;

public interface GestionOperativaService extends CommonService<GestionOperativaEntity>{
    Long findLastOrderByIdPadre(Long idPadre);
    boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden,  Long id);
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment);
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment);
    List<GestionOperativaEntity> findAssignedOperationalsManagements(Long hierarchyId);
    List<GestionOperativaEntity> findNoAssignedOperationalsManagements(Long organizationalChartId);
    List<Object[]> findOperationalManagementByOrganizationChart(Long id);
    List<GestionOperativaEntity> findAssignedOperationalsManagementsByOrganizationChartId(Long organizationalChartId);
    List<GestionOperativaEntity> findAssignedOperationalsManagements(Long hierarchyId, List<Long> operationalManagementIds);
    List<GestionOperativaEntity> findAllFilteredByIds(List<Long> operationalManagementIds);
}
