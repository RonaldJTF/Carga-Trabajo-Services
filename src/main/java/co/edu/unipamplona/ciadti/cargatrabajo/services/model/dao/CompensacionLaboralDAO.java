package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;

public interface CompensacionLaboralDAO extends JpaRepository<CompensacionLaboralEntity, Long>, JpaSpecificationExecutor<CompensacionLaboralEntity>{
    
    @Modifying
    @Query(value = """
        update CompensacionLaboralEntity cl set cl.nombre =:nombre, cl.descripcion =:descripcion, 
        cl.estado =:estado, cl.idCategoria =:idCategoria, cl.idPeriodicidad =:idPeriodicidad, 
        cl.fechaCambio =:fechaCambio, cl.registradoPor =:registradoPor where cl.id =:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("estado") String estado,
                @Param("idCategoria") Long idCategoria,
                @Param("idPeriodicidad") Long idPeriodicidad,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_COMPENSACIONLABORAL(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
