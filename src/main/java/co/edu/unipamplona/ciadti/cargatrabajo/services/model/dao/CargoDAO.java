package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;

public interface CargoDAO extends JpaRepository<CargoEntity, Long>, JpaSpecificationExecutor<CargoEntity>{
    @Modifying
    @Query(value = """
        update CargoEntity c set c.asignacionBasicaMensual =:asignacionBasicaMensual, c.totalCargos =:totalCargos, 
        c.idEstructura =:idEstructura, c.idNivel =:idNivel, c.idNormatividad =:idNormatividad, c.idEscalaSalarial =:idEscalaSalarial, 
        c.idAlcance =:idAlcance, c.idVigencia =:idVigencia, c.fechaCambio =:fechaCambio, c.registradoPor =:registradoPor where c.id =:id
    """)
    int update (@Param("asignacionBasicaMensual") Double asignacionBasicaMensual,
                @Param("totalCargos") Integer totalCargos,
                @Param("idEstructura") Long idEstructura,
                @Param("idNivel") Long idNivel,
                @Param("idNormatividad") Long idNormatividad,
                @Param("idEscalaSalarial") Long idEscalaSalarial,
                @Param("idAlcance") Long idAlcance,
                @Param("idVigencia") Long idVigencia,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_CARGO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
