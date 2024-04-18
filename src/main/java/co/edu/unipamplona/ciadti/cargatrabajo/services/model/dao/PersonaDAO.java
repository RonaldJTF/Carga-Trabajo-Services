package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;

import java.util.Date;
import java.util.List;

public interface PersonaDAO extends JpaRepository<PersonaEntity, Long>, JpaSpecificationExecutor<PersonaEntity> {

    /*@Query( "select p from PersonaEntity p " +
            "inner join UsuarioEntity u on (p.id = u.idPersona) " +
            "where u.id =:idUsuario")
    PersonaEntity findByIdUsuario(@Param("idUsuario") Long idUsuario);*/

    /*@Query( "SELECT p FROM PersonaEntity p " +
            "INNER JOIN UsuarioEntity u ON (p.id = u.idPersona)" +
            "WHERE LOWER(p.primerNombre || ' ' || p.segundoNombre || ' ' || p.primerApellido || ' ' || p.segundoApellido) " + 
            "LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%') )")
    List<PersonaEntity> findAllPeopleWithUserByFilter(@Param("filter") String filter);

    @Query( "SELECT p FROM PersonaEntity p " +
            "INNER JOIN UsuarioEntity u ON (p.id = u.idPersona)" +
            "WHERE u.activo = :active " +
            "AND (" +
            "   LOWER((p.primerNombre || ' ' || p.segundoNombre || ' ' || p.primerApellido || ' ' || p.segundoApellido) " +
            "   LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "   OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    List<PersonaEntity> findAllPeopleWithUserByFilterAndActive(@Param("filter") String filter, @Param("active") String active);*/

    @Modifying
    @Query(value = "update PersonaEntity p set p.idTipoDocumento = :idTipoDocumento, p.idGenero = :idGenero, p.primerNombre = :primerNombre, " +
            "p.segundoNombre = :segundoNombre, p.primerApellido = :primerApellido, p.segundoApellido = :segundoApellido, " +
            "p.documento = :documento, p.correo = :correo, p.telefono = :telefono, p.fechaCambio = :fechaCambio, " + 
            "p.registradoPor = :registradoPor where p.id = :id")
    int update(@Param("idTipoDocumento") Long idTipoDocumento,
               @Param("idGenero") Long idGenero,
               @Param("primerNombre") String primerNombre,
               @Param("segundoNombre") String segundoNombre,
               @Param("primerApellido") String primerApellido,
               @Param("segundoApellido") String segundoApellido,
               @Param("documento") String documento,
               @Param("correo") String correo,
               @Param("telefono") String telefono,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "select FORTALECIMIENTO.pr_fortalecimiento_d_persona(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

}
