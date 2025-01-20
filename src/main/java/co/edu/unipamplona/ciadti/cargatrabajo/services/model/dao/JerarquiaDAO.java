package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;

public interface JerarquiaDAO extends JpaRepository<JerarquiaEntity, Long>, JpaSpecificationExecutor<JerarquiaEntity>{

    @Modifying
    @Query(value = """
        update  JerarquiaEntity j set j.idOrganigrama = :idOrganigrama, j.idDependencia = :idDependencia,
        j.idPadre = :idPadre, j.orden = :orden,
        j.fechaCambio = :fechaCambio, j.registradoPor = :registradoPor where j.id = :id
    """)
    int update(@Param("idOrganigrama") Long idOrganigrama,
               @Param("idDependencia") Long idDependencia,
               @Param("idPadre") Long idPadre,
               @Param("orden") Long orden,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_JERARQUIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);

    @Query(value = """
        SELECT j FROM JerarquiaEntity j WHERE j.idOrganigrama = :idOrganigrama AND j.idDependencia = :idDependencia       
    """)
    JerarquiaEntity findByIdOrganigramaAndIdDependencia(@Param("idOrganigrama") Long idOrganigrama, @Param("idDependencia") Long idDependencia);

    @Query(value = """
        SELECT COALESCE(MAX(j.orden), 0)
        FROM JerarquiaEntity j
        WHERE (:idPadre IS NULL AND j.idPadre IS NULL) OR j.idPadre = :idPadre
    """)
    Long findLastOrderByIdPadre(@Param("idPadre") Long idPadre);

    @Modifying
    @Query(value = """
        update JerarquiaEntity j set j.orden = j.orden + :increment 
        where ((:idPadre IS NULL AND j.idPadre IS NULL) OR j.idPadre = :idPadre) 
            and j.orden >= :orden and j.id != :id
    """)
    int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(@Param("idPadre") Long idPadre,
                                                         @Param("orden") Long orden,
                                                         @Param("id") Long id,
                                                         @Param("increment") int increment);

    @Query(value = """
        select count(j) > 0 from JerarquiaEntity j 
        where ((:idPadre IS NULL AND j.idPadre IS NULL) OR j.idPadre = :idPadre)
            and j.orden = :orden and j.id != :id
    """)
    boolean existsByIdPadreAndOrdenAndNotId(@Param("idPadre") Long idPadre,
                                            @Param("orden") Long orden,
                                            @Param("id") Long id);

    @Modifying
    @Query(value = """
        update JerarquiaEntity j set j.orden = j.orden + :increment 
        where ((:idPadre IS NULL AND j.idPadre IS NULL) OR j.idPadre = :idPadre)
            and j.orden >= :inferiorOrder and  j.orden <= :superiorOrder and j.id != :id
    """)
    int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(@Param("idPadre") Long idPadre,
                                                    @Param("inferiorOrder") Long inferiorOrder,
                                                    @Param("superiorOrder") Long superiorOrder,
                                                    @Param("id") Long id,
                                                    @Param("increment") int increment);
}
