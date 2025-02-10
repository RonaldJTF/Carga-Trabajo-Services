package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;

public interface NivelDAO extends JpaRepository<NivelEntity, Long>, JpaSpecificationExecutor<NivelEntity>{

    @Modifying
    @Query(value = """
        update NivelEntity n set n.descripcion =:descripcion, 
        n.fechaCambio = :fechaCambio, n.registradoPor = :registradoPor where n.id=:id
    """)
    int update (@Param("descripcion") String descripcion,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_NIVEL(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = """
        select distinct n from NivelEntity n 
        inner join ActividadEntity a on a.idNivel = n.id 
        order by n.id 
    """)
    List<NivelEntity> findAllInSomeActivity();
}
