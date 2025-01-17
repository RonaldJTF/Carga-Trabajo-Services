package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

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
        update GestionOperativaEntity g set g.orden = g.orden + :increment 
        where ((:idPadre IS NULL AND g.idPadre IS NULL) OR g.idPadre = :idPadre)
            and g.orden >= :inferiorOrder and  g.orden <= :superiorOrder and g.id != :id
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
}
