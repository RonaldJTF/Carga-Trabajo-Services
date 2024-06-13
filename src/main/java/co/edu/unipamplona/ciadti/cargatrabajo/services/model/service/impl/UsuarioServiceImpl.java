package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ChangePasswordDTO;
import lombok.RequiredArgsConstructor;

import org.hibernate.Session;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.UsuarioDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.status.Active;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    @PersistenceContext
    private EntityManager entityManager;
    private final UsuarioDAO usuarioDAO;

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findById(Long id) throws CiadtiException{
        UsuarioEntity entity = usuarioDAO.findById(id).orElseThrow(() -> new CiadtiException("Usuario no encontrado para el id :: " + id, 404));
        Session session = entityManager.unwrap(Session.class);
        session.evict(entity);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioEntity> findAll() {
        return usuarioDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public UsuarioEntity save(UsuarioEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            usuarioDAO.update(
                entity.getIdPersona(), 
                entity.getUsername(), 
                entity.getPassword(),
                entity.getActivo(),
                entity.getTokenPassword(),
                entity.getFechaCambio(),
                entity.getRegistradoPor(),
                entity.getId());
            return entity;
        }
        return usuarioDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<UsuarioEntity> save(Collection<UsuarioEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
       Integer rows = usuarioDAO.deleteByProcedure(id, register);
       if (1 != rows) {
           throw new RuntimeException( "Se han afectado " + rows + " filas." );
       }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioEntity> findAllFilteredBy(UsuarioEntity filter) {
        SpecificationCiadti<UsuarioEntity> specification = new SpecificationCiadti<UsuarioEntity>(filter);
        return usuarioDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findByUsername(String username) {
        return usuarioDAO.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Nombre de Usuario " + username + " no encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findByIdPersona(Long idPersona) {
        return usuarioDAO.findByIdPersona(idPersona);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity isActivo(Long id) throws CiadtiException {
        UsuarioEntity entity = usuarioDAO.findByIdAndActivo(id, Active.ACTIVATED).orElseThrow(() -> new CiadtiException("El usuario no se encuentra activo", 500));
        Session session = entityManager.unwrap(Session.class);
        session.evict(entity);
        return entity;
    }

    @Override
    public Optional<UsuarioEntity> findByUsernameOrEmail(String username, String correo, String activo) {
        return usuarioDAO.findByUsernameOrEmail(username, correo, activo);
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CiadtiException changePassword(ChangePasswordDTO data)throws CiadtiException{
      Optional<UsuarioEntity> usuarioOpt = usuarioDAO.getByTokenPassword(data.getTokenPassword());
        if(usuarioOpt.isEmpty())
            throw new CiadtiException("El usuario no existe");
        UsuarioEntity usuario = this.usuarioDAO.findById(usuarioOpt.get().getId()).get();
        usuario.setPassword(data.getPassword());
        usuario.setTokenPassword(null);
        this.usuarioDAO.save(usuario);
        return new CiadtiException("Contraseña actualizada", 200);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int updateTokenPassword(UsuarioEntity usuario) {
        return usuarioDAO.updateTokenPassword(usuario.getId(), usuario.getTokenPassword(), new Date());
    }
}