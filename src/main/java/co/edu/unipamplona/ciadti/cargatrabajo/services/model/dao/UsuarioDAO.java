package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;

import java.util.Date;
import java.util.Optional;

public interface UsuarioDAO extends JpaRepository<UsuarioEntity, Long>, JpaSpecificationExecutor<UsuarioEntity> {

    Optional<UsuarioEntity> findByUsername (String username);

    @Modifying
    @Query(value = """
        update UsuarioEntity u set u.idPersona = :idPersona, u.username = :username, u.password = :password, 
        u.activo = :activo, u.tokenPassword = :tokenPassword, u.fechaCambio = :fechaCambio, u.registradoPor = :registradoPor where u.id = :id 
    """)
    int update(@Param("idPersona") Long idPersona,
               @Param("username") String username,
               @Param("password") String password,
               @Param("activo") String activo,
               @Param("tokenPassword") String tokenPassword,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_USUARIO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    UsuarioEntity findByIdPersona(Long idPersona);

    Optional<UsuarioEntity> findByIdAndActivo(Long id, String activated);

    @Query("SELECT U FROM UsuarioEntity U INNER JOIN U.persona P WHERE (U.username = :username OR LOWER(P.correo) = :correo) AND U.activo = :activo")
    Optional<UsuarioEntity>findByUsernameOrEmail(@Param("username") String username, @Param("correo") String correo, @Param("activo") String activo);

    Optional<UsuarioEntity>getByTokenPassword(String tokenPassword);

    @Modifying
    @Query(value = "update UsuarioEntity u set u.tokenPassword = :tokenPassword, u.fechaCambio = :fechaCambio where u.id = :id ")
    int updateTokenPassword(@Param("id") Long id,
                            @Param("tokenPassword")  String tokenPassword,
                            @Param("fechaCambio")  Date fechaCambio);

    @Modifying
    @Query(value = """
        update UsuarioEntity u set u.password = :password, u.tokenPassword = :tokenPassword,
        u.fechaCambio = :fechaCambio, u.registradoPor = :registradoPor where u.id = :id 
    """)
    int updatePasswordAndTokenPassword(@Param("id") Long id,
                                        @Param("password")  String password,
                                        @Param("tokenPassword")  String tokenPassword,
                                        @Param("registradoPor")  String registradoPor,
                                        @Param("fechaCambio")  Date fechaCambio);

    @Modifying
    @Query(value = "update UsuarioEntity u set u.password = :password, u.fechaCambio = :fechaCambio, u.registradoPor = :registradoPor where u.id = :id ")
    int updatePassword(@Param("id") Long id,
                       @Param("password")  String password,
                       @Param("registradoPor")  String registradoPor,
                       @Param("fechaCambio")  Date fechaCambio);
}
