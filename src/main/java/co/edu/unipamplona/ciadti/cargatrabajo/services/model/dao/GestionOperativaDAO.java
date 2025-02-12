package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;

public interface GestionOperativaDAO extends JpaRepository<GestionOperativaEntity, Long>, JpaSpecificationExecutor<GestionOperativaEntity>{

    @Query(value = """
        SELECT COALESCE(MAX(g.orden), 0)
        FROM GestionOperativaEntity g
        WHERE (:idPadre IS NULL AND g.idPadre IS NULL) OR g.idPadre = :idPadre
    """)
    Long findLastOrderByIdPadre(@Param("idPadre") Long idPadre);

    @Modifying
    @Query(value = """
        update GestionOperativaEntity g set g.orden = g.orden + :increment 
        where ((:idPadre IS NULL AND g.idPadre IS NULL) OR g.idPadre = :idPadre) 
            and g.orden >= :orden and g.id != :id
    """)
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(@Param("idPadre") Long idPadre,
                                                         @Param("orden") Long orden,
                                                         @Param("id") Long id,
                                                         @Param("increment") int increment);

    @Query(value = """
        select count(g) > 0 from GestionOperativaEntity g 
        where ((:idPadre IS NULL AND g.idPadre IS NULL) OR g.idPadre = :idPadre)
            and g.orden = :orden and g.id != :id
    """)
    boolean existsByIdPadreAndOrdenAndNotId(@Param("idPadre") Long idPadre,
                                            @Param("orden") Long orden,
                                            @Param("id") Long id);

    @Modifying
    @Query(value = """
        update EstructuraEntity e set e.orden = e.orden + :increment where e.idPadre = :idPadre 
        and e.orden >= :inferiorOrder and  e.orden <= :superiorOrder and e.id != :id
    """)
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(@Param("idPadre") Long idPadre,
                                                    @Param("inferiorOrder") Long inferiorOrder,
                                                    @Param("superiorOrder") Long superiorOrder,
                                                    @Param("id") Long id,
                                                    @Param("increment") int increment);
                                                    
    @Modifying
    @Query(value = """
        update  GestionOperativaEntity go set go.idPadre = :idPadre, go.idTipologia = :idTipologia,
        go.nombre = :nombre, go.descripcion = :descripcion, go.orden = :orden,
        go.fechaCambio = :fechaCambio, go.registradoPor = :registradoPor where go.id = :id
    """)
    int update(@Param("idPadre") Long idPadre,
               @Param("idTipologia") Long idTipologia,
               @Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("orden") Long orden,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_GESTIONOPERATIVA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);

    @Query(value = """
        WITH RECURSIVE padres AS (
            SELECT go.*
            FROM FORTALECIMIENTO.GESTIONOPERATIVA go
            INNER JOIN FORTALECIMIENTO.TIPOLOGIA t on (t.tipo_id = go.tipo_id)
            WHERE t.tipo_idtipologiasiguiente IS NULL AND  go.geop_id not in (
                SELECT go.geop_id  FROM FORTALECIMIENTO.GESTIONOPERATIVA go
                INNER JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = go.geop_id
                INNER JOIN FORTALECIMIENTO.JERARQUIA j ON jgo.jera_id = j.jera_id 
                where j.orga_id = :organizationalChartId
            )

            UNION ALL

            SELECT padre.*
            FROM FORTALECIMIENTO.GESTIONOPERATIVA padre
            INNER JOIN padres hijo ON hijo.geop_idpadre = padre.geop_id
        )
        SELECT DISTINCT(p.*), ag.acge_id, ag.nive_id, ag.acge_frecuencia, ag.acge_tiempomaximo, ag.acge_tiempominimo, ag.acge_tiempopromedio
        FROM padres as p
        LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTION ag ON p.geop_id = ag.geop_id
    """, nativeQuery = true)
    List<GestionOperativaEntity> findNoAssignedOperationalsManagements(@Param("organizationalChartId") Long organizationalChartId);

    @Query(value = """
              WITH RECURSIVE GESTIONJERARQUICA AS (
                            SELECT
                                GEO.GEOP_ID,
                                GEO.GEOP_NOMBRE,
                                GEO.GEOP_DESCRIPCION,
                                GEO.GEOP_IDPADRE,
                                GEO.TIPO_ID AS idtipologia,
                                0 AS nivel,
                                DEP.DEPE_NOMBRE AS dependencia,
                                O.ORGA_NOMBRE,
                                O.ORGA_DESCRIPCION
                            FROM
                                FORTALECIMIENTO.ORGANIGRAMA O
                                    JOIN FORTALECIMIENTO.JERARQUIA JERA ON O.ORGA_ID = JERA.ORGA_ID
                                    JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA JGEO ON JERA.JERA_ID = JGEO.JERA_ID
                                    JOIN FORTALECIMIENTO.GESTIONOPERATIVA GEO ON JGEO.GEOP_ID = GEO.GEOP_ID
                                    JOIN FORTALECIMIENTO.DEPENDENCIA DEP ON JERA.DEPE_ID = DEP.DEPE_ID
                            WHERE
                                O.ORGA_ID = :organizationChartId
                       
                            UNION ALL
                       
                            SELECT
                                GPADRE.GEOP_ID,
                                GPADRE.GEOP_NOMBRE,
                                GPADRE.GEOP_DESCRIPCION,
                                GPADRE.GEOP_IDPADRE,
                                GPADRE.TIPO_ID AS idTipologia,
                                GACTUAL.nivel + 1,
                                GACTUAL.dependencia,
                                GACTUAL.ORGA_NOMBRE,
                                GACTUAL.ORGA_DESCRIPCION
                            FROM
                                FORTALECIMIENTO.GESTIONOPERATIVA GPADRE
                                JOIN GESTIONJERARQUICA GACTUAL ON GACTUAL.GEOP_IDPADRE = GPADRE.GEOP_ID
                        )
                       
                        SELECT
                            GJ.*,
                            ACGE.ACGE_ID,
                            ACGE.NIVE_ID,
                            ACGE.ACGE_FRECUENCIA,
                            ACGE.ACGE_TIEMPOMAXIMO,
                            ACGE.ACGE_TIEMPOMINIMO,
                            ACGE.ACGE_TIEMPOPROMEDIO,
                            N.NIVE_DESCRIPCION,
                            TACTUAL.TIPO_NOMBRE AS tipologia
                        FROM
                            GESTIONJERARQUICA GJ
                            LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTION ACGE ON GJ.GEOP_ID = ACGE.GEOP_ID
                            LEFT JOIN FORTALECIMIENTO.NIVEL N ON ACGE.NIVE_ID = N.NIVE_ID
                            LEFT JOIN FORTALECIMIENTO.TIPOLOGIA TACTUAL ON GJ.idTipologia = TACTUAL.TIPO_ID
            """, nativeQuery = true)
    List<Object[]> findOperationalManagementByOrganizationChart(@Param("organizationChartId") Long organizationChartId);

    @Query(value = "select go from GestionOperativaEntity go where go.id in :operationalManagementIds")
    List<GestionOperativaEntity> findAllFilteredByIds(@Param("operationalManagementIds") List<Long> operationalManagementIds);

    @Query(value = """
            SELECT  AG.ACGO_ID, AG.GEOP_ID, A.* FROM FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA AG
            INNER JOIN FORTALECIMIENTO.ACTIVIDAD A ON AG.ACTI_ID = A.ACTI_ID           
    """, nativeQuery = true)
    List<Object[]> findActivityByOperationalManagement();
}
