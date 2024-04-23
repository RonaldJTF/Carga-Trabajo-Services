package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.UsuarioDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findById(Long id) throws CiadtiException{
        return usuarioDAO.findById(id).orElseThrow(() -> new CiadtiException("Usuario no encontrado para el id :: " + id, 404));
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
    public void deleteByProcedure(Long id, UsuarioEntity entity) {
       Integer rows = usuarioDAO.deleteByProcedure(id, entity.getRegistradorDTO().getJsonAsString());
       if (1 != rows) {
           throw new RuntimeException( "Se han afectado " + rows + " filas." );
       }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<UsuarioEntity> findAllFilteredBy(UsuarioEntity filter) {
        SpecificationCiadti<UsuarioEntity> specification = new SpecificationCiadti<UsuarioEntity>(filter);
        return usuarioDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findByUsername(String username) {
        return usuarioDAO.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Nombre de Usuario " + username + " no encontrado."));
    }
}
