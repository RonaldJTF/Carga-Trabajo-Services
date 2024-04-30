package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collection;
import java.util.List;

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
    public Iterable<UsuarioEntity> findAll() {
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
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return usuarioDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<UsuarioEntity> save(Collection<UsuarioEntity> entities) {
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
}
