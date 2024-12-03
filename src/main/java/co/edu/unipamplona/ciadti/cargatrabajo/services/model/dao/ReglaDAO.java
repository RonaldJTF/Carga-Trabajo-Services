package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;

public interface ReglaDAO extends JpaRepository<ReglaEntity, Long>, JpaSpecificationExecutor<ReglaEntity>{
    
    @Modifying
    @Query(value = """
        update ReglaEntity r set r.nombre =:nombre, r.descripcion =:descripcion, r.condiciones =:condiciones, 
        r.global =:global, r.estado =:estado, r.fechaCambio =:fechaCambio, r.registradoPor =:registradoPor where r.id =:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("condiciones") String condiciones,
                @Param("global") String global,
                @Param("estado") String estado,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_REGLA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "select r from ReglaEntity r where r.condiciones like CONCAT('%[', :idVariable, ']%')")
    List<ReglaEntity> findAllWhereVariableIsIncluded(@Param("idVariable") Long idVariable);

    @Query("SELECT r.id, r.condiciones FROM ReglaEntity r")
    Optional<List<Object[]>> findAllNombresAndCondicionesAndId();

    @Query(value =  """
        SELECT DISTINCT re FROM ReglaEntity re   
        WHERE re.global = '1' AND re.estado = '1' 
        UNION 
        SELECT DISTINCT r FROM ReglaEntity r
        LEFT OUTER JOIN CompensacionLabNivelVigValorEntity cnvv on (r.id = cnvv.idRegla) 
        LEFT OUTER JOIN CompensacionLabNivelVigenciaEntity clnv on (cnvv.idCompensacionLabNivelVigencia = clnv.id) 
        WHERE (clnv.idNivel = :idNivel OR clnv.idNivel IS NULL) AND r.global = '0' AND r.estado = '1'
    """)
    List<ReglaEntity> getGlobalAndLevelActiveRules(@Param("idNivel") Long levelId);
}
