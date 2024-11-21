package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.DependenciaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraDAO extends JpaRepository<EstructuraEntity, Long>, JpaSpecificationExecutor<EstructuraEntity> {

    @Modifying
    @Query(value = "update EstructuraEntity e set e.nombre = :nombre, e.descripcion = :descripcion, e.idPadre = :idPadre, " +
            "e.idTipologia = :idTipologia, e.icono = :icono, e.mimetype = :mimetype, e.orden =:orden,  e.fechaCambio = :fechaCambio, " +
            "e.registradoPor = :registradoPor where e.id = :id")
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("idPadre") Long idPadre,
               @Param("idTipologia") Long idTipologia,
               @Param("icono") byte[] icono,
               @Param("mimetype") String mimetype,
               @Param("orden") Long orden,
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

    @Query(value = "select coalesce (Max(e.orden), 0) from EstructuraEntity e where e.idPadre = :idPadre")
    Long findLastOrderByIdPadre(@Param("idPadre") Long idPadre);

    @Modifying
    @Query(value = "update EstructuraEntity e set e.orden = e.orden + :increment where e.idPadre = :idPadre and e.orden >= :orden and e.id != :id")
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(@Param("idPadre") Long idPadre,
                                                         @Param("orden") Long orden,
                                                         @Param("id") Long id,
                                                         @Param("increment") int increment);

    @Query(value = "select count(e) > 0 from EstructuraEntity e where e.idPadre = :idPadre and e.orden = :orden and e.id != :id")
    boolean existsByIdPadreAndOrdenAndNotId(@Param("idPadre") Long idPadre,
                                            @Param("orden") Long orden,
                                            @Param("id") Long id);

    @Modifying
    @Query(value = "update EstructuraEntity e set e.orden = e.orden + :increment where e.idPadre = :idPadre " +
            "and e.orden >= :inferiorOrder and  e.orden <= :superiorOrder and e.id != :id")
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(@Param("idPadre") Long idPadre,
                                                    @Param("inferiorOrder") Long inferiorOrder,
                                                    @Param("superiorOrder") Long superiorOrder,
                                                    @Param("id") Long id,
                                                    @Param("increment") int increment);

    @Query(value = "SELECT "
            + "estr_id id, "
            + "estr_nombre nombre, "
            + "estr_descripcion descripcion, "
            + "estr_idpadre idPadre, "
            + "estr_registradopor registradoPor, "
            + "estr_fechacambio fechaCambio, "
            + "e.tipo_id idTipo, "
            + "estr_icono icono, "
            + "estr_mimetype mimeType, "
            + "estr_orden orden "
            + "FROM fortalecimiento.estructura e "
            + "LEFT JOIN fortalecimiento.tipologia t ON (e.tipo_id = t.tipo_id) "
            + "WHERE t.tipo_esdependencia = '1'", nativeQuery = true)
    List<DependenciaDTO> findAllDependencies();

    @Query(value = "SELECT e FROM EstructuraEntity e where e.idPadre = :idPadre")
    List<EstructuraEntity> findByIdPadre(@Param("idPadre") Long idPadre);
}