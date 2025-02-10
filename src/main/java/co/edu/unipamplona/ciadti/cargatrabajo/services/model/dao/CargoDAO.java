package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DenominacionEmpleoEntity;

public interface CargoDAO extends JpaRepository<CargoEntity, Long>, JpaSpecificationExecutor<CargoEntity>{
    @Modifying
    @Query(value = """
        update CargoEntity c set c.asignacionBasicaMensual =:asignacionBasicaMensual, c.totalCargos =:totalCargos, 
        c.idJerarquia =:idJerarquia, c.idNivel =:idNivel, c.idNormatividad =:idNormatividad, c.idEscalaSalarial =:idEscalaSalarial, 
        c.idAlcance =:idAlcance, c.idVigencia =:idVigencia, c.fechaCambio =:fechaCambio, c.registradoPor =:registradoPor where c.id =:id
    """)
    int update (@Param("asignacionBasicaMensual") Double asignacionBasicaMensual,
                @Param("totalCargos") Integer totalCargos,
                @Param("idJerarquia") Long idJerarquia,
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

    @Query(value = """
            SELECT de FROM DenominacionEmpleoEntity de 
            INNER JOIN CargoDenominacionEmpleoEntity cde ON (cde.idDenominacionEmpleo = de.id)
            WHERE cde.idCargo = :appointmentId
    """)
    List<DenominacionEmpleoEntity> findAllJobTitlesByAppointmentId(@Param("appointmentId") Long appointmentId);
}
