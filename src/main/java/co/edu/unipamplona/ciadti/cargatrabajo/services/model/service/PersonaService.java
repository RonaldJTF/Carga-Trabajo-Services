package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;

public interface PersonaService extends CommonService<PersonaEntity> {
    void deleteByProcedure(Long id, PersonaEntity personaEntity);
    List<PersonaEntity> findAllFilteredBy(PersonaEntity filter);
    PersonaEntity  findByIdUsuario (Long idUsuario);
    List<PersonaEntity> findAllPeopleWithUserByFilter(String filter, String active);
}
