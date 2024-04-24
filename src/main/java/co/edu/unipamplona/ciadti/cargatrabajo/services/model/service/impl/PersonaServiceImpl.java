package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.PersonaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaDAO personaDAO;

    @Override
    @Transactional(readOnly = true)
    public PersonaEntity findById(Long id) throws CiadtiException {
        return personaDAO.findById(id).orElseThrow(() -> new CiadtiException("Persona no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<PersonaEntity> findAll() {
        return personaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public PersonaEntity save(PersonaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            personaDAO.update(
                    entity.getIdTipoDocumento(),
                    entity.getIdGenero(),
                    entity.getPrimerNombre(),
                    entity.getSegundoNombre(),
                    entity.getPrimerApellido(),
                    entity.getSegundoApellido(),
                    entity.getDocumento(),
                    entity.getCorreo(),
                    entity.getTelefono(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return personaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<PersonaEntity> save(Collection<PersonaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = personaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaEntity> findAllFilteredBy(PersonaEntity filter) {
        OrderBy orderBy = new OrderBy();
        orderBy.addOrder("activo", false);
        orderBy.addOrder("nombre", true);
        Specification<PersonaEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return personaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaEntity findByIdUsuario(Long idUsuario) {
        return personaDAO.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaEntity> findAllPeopleWithUserByFilter(String filter, String active) {
        return active != null && !active.isEmpty()
            ? personaDAO.findAllPeopleWithUserByFilterAndActive(filter, active)
            : personaDAO.findAllPeopleWithUserByFilter(filter);
    }
}
