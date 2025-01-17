package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

<<<<<<< Updated upstream
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
=======
import java.util.List;

>>>>>>> Stashed changes
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;

public interface GestionOperativaService extends CommonService<GestionOperativaEntity>{
    Long findLastOrderByIdPadre(Long idPadre);
    boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden,  Long id);
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment);
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment);
<<<<<<< Updated upstream
    ActividadEntity findActividadByIdGestionOperativa(Long idGestionOperativa);
=======
    List<GestionOperativaEntity> findOperationalManagementByHierarchy(Long id);
>>>>>>> Stashed changes
}
