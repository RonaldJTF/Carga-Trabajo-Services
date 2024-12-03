package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;

public interface PlanTrabajoDAO extends JpaRepository<PlanTrabajoEntity, Long>, JpaSpecificationExecutor<PlanTrabajoEntity>{
    
    @Modifying
    @Query(value = """
        update PlanTrabajoEntity pt set pt.nombre =:nombre, pt.descripcion =:descripcion, 
        pt.fechaCambio = :fechaCambio, pt.registradoPor = :registradoPor where pt.id=:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_PLANTRABAJO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = """
        WITH UltimoSeguimiento AS ( 
            SELECT s.tare_id, s.segu_porcentajeavance as avance, 
            ROW_NUMBER() OVER (PARTITION BY s.tare_id ORDER BY s.segu_fecha DESC) as rn 
            FROM fortalecimiento.seguimiento s 
        ), 
        AvanceTarea AS ( 
            SELECT t.etap_id, us.avance 
            FROM fortalecimiento.tarea t 
            JOIN UltimoSeguimiento us ON t.tare_id = us.tare_id 
            WHERE us.rn = 1 
        ), 
        AvanceEtapa AS ( 
            SELECT e.pltr_id, AVG(at.avance) as promedio_avance_etapa 
            FROM fortalecimiento.etapa e 
            LEFT JOIN AvanceTarea at ON e.etap_id = at.etap_id 
            GROUP BY e.pltr_id 
        ) 
        SELECT pt.pltr_id, AVG(ae.promedio_avance_etapa) as porcentaje_avance 
        FROM fortalecimiento.plantrabajo pt 
        LEFT JOIN AvanceEtapa ae ON pt.pltr_id = ae.pltr_id 
        GROUP BY pt.pltr_id
    """, nativeQuery = true)
    List<Object[]> getAllAvances();

    @Query(value = "select distinct (p) from PlanTrabajoEntity as p inner join EtapaEntity e on (p.id = e.idPlanTrabajo) where e.id =:idEtapa")
    PlanTrabajoEntity findByIdStage(@Param("idEtapa") Long idEtapa);
}
