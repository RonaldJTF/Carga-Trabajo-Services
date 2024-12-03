package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TareaEntity;

public interface TareaDAO extends JpaRepository<TareaEntity, Long>, JpaSpecificationExecutor<TareaEntity>{
    
    @Modifying
    @Query(value = """
        update TareaEntity t set t.nombre =:nombre, t.descripcion =:descripcion, t.idEtapa = :idEtapa, 
        t.fechaInicio =:fechaInicio, t.fechaFin = :fechaFin, t.entregable = :entregable, t.responsable = :responsable, 
        t.activo = :activo, t.fechaCambio = :fechaCambio, t.registradoPor = :registradoPor where t.id=:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("idEtapa") Long idEtapa,
                @Param("fechaInicio") Date fechaInicio,
                @Param("fechaFin") Date fechaFin,
                @Param("entregable") String entregable,
                @Param("responsable") String responsable,
                @Param("activo") String activo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_TAREA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    List<TareaEntity> findAllByIdEtapa(Long idEtapa);

    @Modifying
    @Query(value =  "update TareaEntity t set t.activo = :activo, t.fechaCambio = :fechaCambio, t.registradoPor = :registradoPor where t.id=:id")
    int updateActivoById(@Param("activo") String activo,
                         @Param("fechaCambio") Date fechaCambio,
                         @Param("registradoPor") String registradoPor,
                         @Param("id") Long id);
}