package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Iterable<UsuarioEntity> findAll() {
        return null;
    }

    @Override
    public UsuarioEntity save(UsuarioEntity entity) {
        return null;
    }

    @Override
    public Iterable<UsuarioEntity> save(Collection<UsuarioEntity> entities) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioEntity findByUsername(String username) {
        return usuarioDAO.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Nombre de Usuario " + username + " no encontrado."));
    }
}
