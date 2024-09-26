package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.InventarioTipologiaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;

public interface TipologiaDAO extends JpaRepository<TipologiaEntity, Long>, JpaSpecificationExecutor<TipologiaEntity> {

    @Query(value = "select t from TipologiaEntity t " +
            "left join TipologiaEntity t2 on (t.id = t2.idTipologiaSiguiente) " +
            "where t2.idTipologiaSiguiente is null")
    TipologiaEntity findFirstTipology();

    @Query(value = "select t from TipologiaEntity t where t.esDependencia = '1'")
    TipologiaEntity findDependencyTipology();

    @Modifying
    @Query(value = "update TipologiaEntity t set t.idTipologiaSiguiente =:idTipologiaSiguiente, t.nombre =:nombre,  t.claseIcono = :claseIcono, " +
            "t.nombreColor =:nombreColor, t.esDependencia =:esDependencia,  t.fechaCambio = :fechaCambio, " +
            "t.registradoPor = :registradoPor where  t.id = :id")
    int update(@Param("idTipologiaSiguiente") Long idTipologiaSiguiente,
               @Param("nombre") String nombre,
               @Param("claseIcono") String claseIcono,
               @Param("nombreColor") String nombreColor,
               @Param("esDependencia") String esDependencia,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_TIPOLOGIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "select distinct(t) from TipologiaEntity t " +
            "inner join EstructuraEntity e on (t.id = e.idTipologia) order by t.id desc")
    List<TipologiaEntity> findAllManagement();

    @Query(value = "SELECT t.tipo_nombre AS nombre, t.tipo_claseicono AS claseIcono, t.tipo_nombrecolor AS nombreColor, COUNT(e.estr_id) AS cantidad " +
            "FROM fortalecimiento.tipologia t " +
            "JOIN fortalecimiento.estructura e ON t.tipo_id = e.tipo_id " +
            "WHERE (t.tipo_esdependencia  = '1') OR  NOT EXISTS (" +
            "SELECT 1 FROM fortalecimiento.estructura e2 WHERE e2.tipo_id = e.tipo_id AND e2.estr_idpadre  = e.estr_id) " +
            "GROUP BY t.tipo_id, t.tipo_nombre, t.tipo_claseicono, t.tipo_nombrecolor " +
            "ORDER BY t.tipo_id DESC;", nativeQuery = true)
    List<InventarioTipologiaDTO> findInventarioTipologia();
}
