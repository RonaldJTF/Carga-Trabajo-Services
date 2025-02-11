package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoDenominacionEmpleoEntity;


public interface CargoDenominacionEmpleoDAO extends JpaRepository<CargoDenominacionEmpleoEntity, Long>, JpaSpecificationExecutor<CargoDenominacionEmpleoEntity>{
    
    @Modifying
    @Query(value = """
        update CargoDenominacionEmpleoEntity cde set cde.idCargo = :idCargo, cde.idDenominacionEmpleo = :idDenominacionEmpleo,
        cde.totalCargos = :totalCargos, cde.fechaCambio = :fechaCambio, cde.registradoPor = :registradoPor where cde.id = :id
    """)
    int update(@Param("idCargo") Long idCargo,
               @Param("idDenominacionEmpleo") Long idDenominacionEmpleo,
               @Param("totalCargos") Long totalCargos,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_CARGODENOMINACIONEMPLEO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);

    CargoDenominacionEmpleoEntity findByIdCargoAndIdDenominacionEmpleo(Long idCargo, Long idDenominacionEmpleo);

    List<CargoDenominacionEmpleoEntity> findAllByIdCargo(Long idCargo);
}
