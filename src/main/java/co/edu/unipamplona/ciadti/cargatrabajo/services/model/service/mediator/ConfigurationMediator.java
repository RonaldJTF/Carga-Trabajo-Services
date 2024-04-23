package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConfigurationMediator {
    private final EstructuraService estructuraService;

    /**
     * Elimina una estructura por su id y todas sus subestructuras en cascada.
     * @param id: Identificador de la estructura a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStructure(Long id) throws CiadtiException{
        EstructuraEntity structureToDelete = estructuraService.findById(id);
        if (structureToDelete.getSubEstructuras() != null){
            for (EstructuraEntity e : structureToDelete.getSubEstructuras()){
                deleteStructure(e.getId());
            }
        }
        structureToDelete.setRegistradorDTO(RegisterContext.getRegistradorDTO());
        estructuraService.deleteByProcedure(id, structureToDelete);
    }
}
