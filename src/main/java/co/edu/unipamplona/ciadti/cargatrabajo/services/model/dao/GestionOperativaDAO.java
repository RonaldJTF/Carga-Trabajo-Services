package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportOperationalManagementDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
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
        select a from ActividadEntity a 
        inner join ActividadGestionOperativaEntity ago on (a.id = ago.idActividad)
        where ago.idGestionOperativa = :idGestionOperativa
    """)
    ActividadEntity findActividadByIdGestionOperativa(@Param("idGestionOperativa") Long idGestionOperativa);

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
        SELECT DISTINCT(p.*), a.* FROM padres as p
        LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA ago ON p.geop_id = ago.geop_id
        LEFT JOIN FORTALECIMIENTO.ACTIVIDAD a ON a.acti_id = ago.acti_id
    """, nativeQuery = true)
    List<GestionOperativaEntity> findNoAssignedOperationalsManagements(@Param("organizationalChartId") Long organizationalChartId);

    @Query(value = """
            WITH RECURSIVE GESTIONJERARQUICA AS (
                SELECT
                    GO.GEOP_ID AS idGestionOperativa,
                    NULL AS proceso,
                    NULL AS procesoDescripcion,
                    NULL AS procedimiento,
                    NULL AS procedimientoDescripcion,
                    GO.GEOP_NOMBRE AS actividad,
                    GO.GEOP_DESCRIPCION AS actividadDescripcion,
                    D.DEPE_NOMBRE AS dependencia,
                    D.DEPE_DESCRIPCION AS dependenciaDescripcion,
                    GO.GEOP_IDPADRE AS idGestionOperativaPadre,
                    T.TIPO_NOMBRE AS tipo
                FROM
                    FORTALECIMIENTO.ORGANIGRAMA O
                        JOIN FORTALECIMIENTO.JERARQUIA J ON O.ORGA_ID = J.ORGA_ID
                        JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA JGO ON J.JERA_ID = JGO.JERA_ID
                        JOIN FORTALECIMIENTO.GESTIONOPERATIVA GO ON JGO.GEOP_ID = GO.GEOP_ID
                        JOIN FORTALECIMIENTO.TIPOLOGIA T ON GO.TIPO_ID = T.TIPO_ID
                        JOIN FORTALECIMIENTO.DEPENDENCIA D ON J.DEPE_ID = D.DEPE_ID
                WHERE
                    O.ORGA_ID = :organizationChartId
            
                UNION ALL
            
                SELECT
                    GJ.idGestionOperativa AS idGestionOperativa,
                    CASE
                        WHEN T.TIPO_NOMBRE = 'Proceso' THEN GO_PADRE.GEOP_NOMBRE
                        ELSE GJ.proceso
                        END AS proceso,
                    CASE
                        WHEN T.TIPO_NOMBRE = 'Proceso' THEN GO_PADRE.GEOP_DESCRIPCION
                        ELSE GJ.procesoDescripcion
                        END AS procesoDescripcion,
                    CASE
                        WHEN T.TIPO_NOMBRE = 'Procedimiento' THEN GO_PADRE.GEOP_NOMBRE
                        ELSE GJ.procedimiento
                        END AS procedimiento,
                    CASE
                        WHEN T.TIPO_NOMBRE = 'Procedimiento' THEN GO_PADRE.GEOP_DESCRIPCION
                        ELSE GJ.procedimientoDescripcion
                        END AS procedimientoDescripcion,
                    GJ.actividad,
                    GJ.actividadDescripcion,
                    GJ.dependencia,
                    GJ.dependenciaDescripcion,
                    GO_PADRE.GEOP_IDPADRE AS idGestionOperativaPadre,
                    T.TIPO_NOMBRE AS tipo
                FROM
                    FORTALECIMIENTO.GESTIONOPERATIVA GO_PADRE
                        JOIN GESTIONJERARQUICA GJ ON GO_PADRE.GEOP_ID = GJ.idGestionOperativaPadre
                        JOIN FORTALECIMIENTO.TIPOLOGIA T ON GO_PADRE.TIPO_ID = T.TIPO_ID
            )
            
            SELECT
                GJ.idGestionOperativa,
                MAX(GJ.proceso) AS proceso,
                MAX(GJ.procesoDescripcion) AS procesoDescripcion,
                MAX(GJ.procedimiento) AS procedimiento,
                MAX(GJ.procedimientoDescripcion) AS procedimientoDescripcion,
                MAX(GJ.actividad) AS actividad,
                MAX(GJ.actividadDescripcion) AS actividadDescripcion,
                MAX(GJ.dependencia) AS dependencia,
                MAX(GJ.dependenciaDescripcion) AS dependenciaDescripcion,
                MAX(A.ACTI_ID) AS idActividad,
                MAX(A.ACTI_FRECUENCIA) AS frecuencia,
                MAX(A.ACTI_TIEMPOMINIMO) AS tiempoMinimo,
                MAX(A.ACTI_TIEMPOMAXIMO) AS tiempoMaximo,
                MAX(A.ACTI_TIEMPOPROMEDIO) AS tiempoPromedio,
                MAX(N.NIVE_ID) AS idNivel,
                MAX(N.NIVE_DESCRIPCION) AS nivel,
                MAX(GJ.idGestionOperativaPadre) AS idGestionOperativaPadre
            FROM
                GESTIONJERARQUICA GJ
                    LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA AGO ON GJ.idGestionOperativa = AGO.GEOP_ID
                    LEFT JOIN FORTALECIMIENTO.ACTIVIDAD A ON AGO.ACTI_ID = A.ACTI_ID
                    LEFT JOIN FORTALECIMIENTO.NIVEL N ON A.NIVE_ID = N.NIVE_ID
            GROUP BY
                GJ.idGestionOperativa
            ORDER BY
                proceso,
                procedimiento;
            """, nativeQuery = true)
    List<Object[]> findOperationalManagementByOrganizationChart(@Param("organizationChartId") List<Long> organizationChartId);
}
