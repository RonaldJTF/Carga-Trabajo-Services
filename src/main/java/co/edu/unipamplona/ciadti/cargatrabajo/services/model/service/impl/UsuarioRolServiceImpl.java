package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.UsuarioRolDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioRolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioRolService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UsuarioRolServiceImpl implements UsuarioRolService{
    private final UsuarioRolDAO usuarioRolDAO;

    @Override
    @Transactional(readOnly = true)
    public UsuarioRolEntity findById(Long id) throws CiadtiException {
        return usuarioRolDAO.findById(id).orElseThrow(() -> new CiadtiException("UsuarioRol no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<UsuarioRolEntity> findAll() {
        return usuarioRolDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public UsuarioRolEntity save(UsuarioRolEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            usuarioRolDAO.update(
                entity.getIdUsuario(), 
                entity.getIdRol(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return usuarioRolDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<UsuarioRolEntity> save(Collection<UsuarioRolEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = usuarioRolDAO.deleteByProcedure(id, register);
        if (1 != rows){
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolEntity> findAllFilteredBy(UsuarioRolEntity filter) {
        SpecificationCiadti<UsuarioRolEntity> specification = new SpecificationCiadti<UsuarioRolEntity>(filter);
        return usuarioRolDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolEntity> findAllByIdUsuario(Long idUsuario) {
        return usuarioRolDAO.findAllByIdUsuario(idUsuario);
    }

}
