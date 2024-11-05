package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.apache.el.stream.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;

public interface VariableDAO extends JpaRepository<VariableEntity, Long>, JpaSpecificationExecutor<VariableEntity>{
    
    @Modifying
    @Query(value = "update VariableEntity v set v.nombre =:nombre, v.descripcion =:descripcion, v.valor =:valor, " +
                    "v.primaria =:primaria, v.global =:global, v.porVigencia =:porVigencia, v.estado =:estado, " +
                    "v.fechaCambio =:fechaCambio, v.registradoPor =:registradoPor where v.id =:id")
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("valor") String valor,
                @Param("primaria") String primaria,
                @Param("global") String global,
                @Param("porVigencia") String porVigencia,
                @Param("estado") String estado,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_VARIABLE(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
    
    @Query("SELECT v FROM VariableEntity v WHERE v.nombre = :nombre")
    VariableEntity findByNombre(@Param("nombre") String nombre);

    @Query(value="SELECT v FROM VariableEntity v " +
            "left outer join ValorVigenciaEntity vv on (v.id = vv.idVariable) " + 
            "where v.id = :id and (vv.idVigencia = :idVigencia or vv.idVigencia is null)")
    VariableEntity findByIdAndValidityId(@Param("id") Long id, @Param("idVigencia") Long idVigencia); 
}
