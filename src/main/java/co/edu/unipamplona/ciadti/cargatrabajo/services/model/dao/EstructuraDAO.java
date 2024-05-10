package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraDAO extends JpaRepository<EstructuraEntity, Long>, JpaSpecificationExecutor<EstructuraEntity>{

    @Modifying
    @Query(value =  "update EstructuraEntity e set e.nombre = :nombre, e.descripcion = :descripcion, e.idPadre = :idPadre, " +
                    "e.idTipologia = :idTipologia, e.icono = :icono, e.mimetype = :mimetype,  e.fechaCambio = :fechaCambio, " + 
                    "e.registradoPor = :registradoPor where e.id = :id")
    int update( @Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("idPadre") Long idPadre,
                @Param("idTipologia") Long idTipologia,
                @Param("icono") byte[] icono,
                @Param("mimetype") String mimetype,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ESTRUCTURA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "WITH RECURSIVE ACTIVIDADES AS ( "
            + "SELECT estr_id, estr_nombre "
            + "FROM fortalecimiento.estructura "
            + "WHERE estr_id = :id "
            + "UNION ALL "
            + "SELECT e.estr_id, e.estr_nombre "
            + "FROM fortalecimiento.estructura e "
            + "INNER JOIN ACTIVIDADES s ON e.estr_idpadre = s.estr_id ) "
            + "SELECT "
            + "estr_nombre AS nombre, "
            + "a.acti_frecuencia AS frecuencia, "
            + "a.acti_tiempomaximo AS tiempoMaximo, "
            + "a.acti_tiempominimo AS tiempoMinimo, "
            + "a.acti_tiempopromedio AS tiempoPromedio, "
            + "n.nive_descripcion AS nivel "
            + "FROM ACTIVIDADES act "
            + "LEFT JOIN fortalecimiento.actividad a ON act.estr_id = a.estr_id "
            + "LEFT JOIN fortalecimiento.nivel n ON a.nive_id = n.nive_id "
            + "WHERE NOT EXISTS ( SELECT 1 FROM fortalecimiento.estructura WHERE estr_idpadre = act.estr_id ) "
            + "AND act.estr_id IN ( "
            + "SELECT estr_id "
            + "FROM fortalecimiento.estructura "
            + "WHERE tipo_id = ( SELECT tipo_id FROM fortalecimiento.tipologia WHERE tipo_nombre = 'Actividad' ) )", nativeQuery = true)
    List<ActividadDTO> statisticsDependence(@Param("id") Long id);


    @Query(value = "select e from EstructuraEntity e where e.id in :structureIds")
    List<EstructuraEntity> findAllFilteredByIds(@Param("structureIds") List<Long> structureIds);
}
