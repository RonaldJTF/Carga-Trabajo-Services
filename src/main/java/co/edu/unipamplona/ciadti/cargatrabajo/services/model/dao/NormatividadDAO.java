package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;

public interface NormatividadDAO extends JpaRepository<NormatividadEntity, Long>, JpaSpecificationExecutor<NormatividadEntity> {
    
    @Modifying
    @Query(value = "update NormatividadEntity n set n.nombre =:nombre, n.descripcion =:descripcion, " +
                    "n.emisor =:emisor, n.fechaInicioVigencia =:fechaInicioVigencia, n.fechaFinVigencia =:fechaFinVigencia, " +
                    "n.estado =:estado, n.escalaSalarial =:escalaSalarial, n.idAlcance =:idAlcance, n.idTipoNormatividad =:idTipoNormatividad, " +
                    "n.fechaCambio =:fechaCambio, n.registradoPor =:registradoPor where n.id =:id")
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("emisor") String emisor,
               @Param("fechaInicioVigencia") Date fechaInicioVigencia,
               @Param("fechaFinVigencia") Date fechaFinVigencia,
               @Param("estado") String estado,
               @Param("escalaSalarial") String escalaSalarial,
               @Param("idAlcance") Long idAlcance,
               @Param("idTipoNormatividad") Long idTipoNormatividad,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_NORMATIVIDAD(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}

