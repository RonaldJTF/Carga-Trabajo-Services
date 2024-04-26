package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FotoPersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.RolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioRolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.FotoPersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioRolService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConfigurationMediator {
    private final EstructuraService estructuraService;
    private final PersonaService personaService;
    private final FotoPersonaService fotoPersonaService;
    private final UsuarioService usuarioService;
    private final UsuarioRolService usuarioRolService;

    /**
     * Elimina una estructura por su id y todas sus subestructuras en cascada.
     * @param id: Identificador de la estructura a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStructure(Long id) throws CiadtiException{
        EstructuraEntity structure = estructuraService.findById(id);
        if (structure.getSubEstructuras() != null){
            for (EstructuraEntity e : structure.getSubEstructuras()){
                deleteStructure(e.getId());
            }
        }
        estructuraService.deleteByProcedure(id, "RegisterContext.getRegistradorDTO().getJsonAsString()");
    }

    /**
     * Elimina todas las estructuras pasadas en el parámetro structureIds
     * @param structureIds: Contiene los identificadores de las estructuras a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStructures(List<Long> structureIds) throws CiadtiException {
        List<Long> deletedStructures = new ArrayList<>();
        for (Long id : structureIds){
            deleteStructure(id, deletedStructures);
        }
    } 

    /**
     * Elimina una estructura y sus subestructuras de manera recursiva
     * @param id: identificador de la estructura a eliminar
     * @param deletedStructures: almacena las estructuras que se han eliminado, esto para evitar tratar 
     *                           de eliminar una estructura que ha sido eliminada en el mismo proceso
     * @throws CiadtiException
     */
    private void deleteStructure(Long id, List<Long> deletedStructures ) throws CiadtiException{
        EstructuraEntity structure = estructuraService.findById(id);
        if (structure.getSubEstructuras() != null){
            for (EstructuraEntity e : structure.getSubEstructuras()){
                deleteStructure(e.getId(), deletedStructures);
            }
        }
        if (!deletedStructures.contains(id)){
            estructuraService.deleteByProcedure(id, "RegisterContext.getRegistradorDTO().getJsonAsString()");
            deletedStructures.add(id);
        }
    }

    /**
     * Crea o actualiza la información de una persona con su respectiva foto de perfil
     * @param personaEntity
     * @param photoFile
     * @return PersonaEntity
     * @throws IOException
     */
    public PersonaEntity savePerson(PersonaEntity personaEntity, MultipartFile photoFile) throws IOException {
        FotoPersonaEntity fotoPersonaEntity;
        personaEntity = personaService.save(personaEntity);
        if (photoFile != null){
            fotoPersonaEntity = fotoPersonaService.findByIdPersona(personaEntity.getId());
            if (fotoPersonaEntity != null){
                fotoPersonaEntity.setArchivo(photoFile.getBytes());
                fotoPersonaEntity.setMimetype(photoFile.getContentType());
            }else{
                fotoPersonaEntity = FotoPersonaEntity.builder()
                                .idPersona(personaEntity.getId())
                                .archivo(photoFile.getBytes())
                                .mimetype(photoFile.getContentType())
                                .build();
            }
            fotoPersonaService.save(fotoPersonaEntity);
            personaEntity.setFotoPersona(fotoPersonaEntity);
        }
        return personaEntity;
    }

    /**
     * Elimina una Persona junto a su objeto FotoPersona si tiene relacionada una foto de perfil
     * @param id
     */
    public void deletePerson(Long id){
        FotoPersonaEntity fotoPersona = fotoPersonaService.findByIdPersona(id);
        if (fotoPersona != null){
            fotoPersonaService.deleteByProcedure(fotoPersona.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        personaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Crea o actualiza la información de un usuario junto a sus roles.
     * Nota: Si en la lista de los nuevos roles que trae el objeto 'usuarioEntity' no se encuentra un 
     * rol que ya existía en la base de datos, entonces se procede a eliminarlo.
     * @param usuarioEntity: Objeto con información del usuario a crear o actualizar
     * @return
     */
    public UsuarioEntity saveUser(UsuarioEntity usuarioEntity) {
        usuarioEntity = usuarioService.save(usuarioEntity);
        UsuarioRolEntity usuarioRolEntity;
        List<UsuarioRolEntity> usuarioRolesToDelete = new ArrayList<>();
        List<UsuarioRolEntity> usuarioRolesBD = usuarioRolService.findAllByIdUsuario(usuarioEntity.getId());

        if (usuarioEntity.getRoles() != null){
            for (RolEntity rol : usuarioEntity.getRoles()) {
                usuarioRolEntity =  UsuarioRolEntity.builder()
                                    .idRol(rol.getId())
                                    .idUsuario(usuarioEntity.getId())
                                    .build();
                usuarioRolService.save(usuarioRolEntity);
            }    
            for (UsuarioRolEntity urBD : usuarioRolesBD) {
                if (usuarioEntity.getRoles().stream().noneMatch(rol -> Objects.equals(rol.getId(), urBD.getIdRol()))){
                    usuarioRolesToDelete.add(urBD);
                }
            }
        }else{usuarioRolesToDelete = usuarioRolesBD;}

        usuarioRolesToDelete.forEach( e -> {
            usuarioRolService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        });
        
        return usuarioEntity;
    }

    /**
     * Elimina un usuario junto a su relación con los roles que tiene.
     * @param id: Identificador del usuario
     */
    public void deleteUser(Long id) {
        List<UsuarioRolEntity> usuarioRoles = usuarioRolService.findAllByIdUsuario(id);
        usuarioRoles.forEach( e -> {
            usuarioRolService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        });
        usuarioService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }  
}
