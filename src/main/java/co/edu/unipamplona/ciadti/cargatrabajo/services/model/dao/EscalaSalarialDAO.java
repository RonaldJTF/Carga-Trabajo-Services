package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;

public interface EscalaSalarialDAO extends JpaRepository<EscalaSalarialEntity, Long>, JpaSpecificationExecutor<EscalaSalarialEntity>{
    
    @Modifying
    @Query(value = """
        update EscalaSalarialEntity es set es.nombre =:nombre, es.codigo =:codigo, 
        es.incrementoPorcentual =:incrementoPorcentual, es.idNivel =:idNivel, 
        es.idNormatividad =:idNormatividad, es.estado =:estado, es.fechaCambio =:fechaCambio, 
        es.registradoPor =:registradoPor where es.id =:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("codigo") String codigo,
                @Param("incrementoPorcentual") Long incrementoPorcentual,
                @Param("idNivel") Long idNivel,
                @Param("idNormatividad") Long idNormatividad,
                @Param("estado") String estado,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ESCALASALARIAL(?1,?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Modifying
    @Query(value = """
        update EscalaSalarialEntity es set es.estado = :estado, 
        es.fechaCambio = :fechaCambio, es.registradoPor = :registradoPor where es.idNormatividad = :idNormatividad 
    """)
    int updateStatusByNormativityId(@Param("estado") String estado,
                                    @Param("idNormatividad") Long idNormatividad,
                                    @Param("registradoPor")  String registradoPor,
                                    @Param("fechaCambio")  Date fechaCambio);

    @Query(value = "select count(es.id) from EscalaSalarialEntity es where es.estado =:estado and es.idNormatividad =:normativityId")                                
    int countByStatusAndNormativityId(@Param("estado") String status, @Param("normativityId") Long normativityId);
}
