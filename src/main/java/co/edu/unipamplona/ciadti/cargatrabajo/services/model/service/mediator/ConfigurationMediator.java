package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EtapaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FotoPersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.RolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TareaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioRolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ArchivoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EtapaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.FotoPersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PlanTrabajoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.SeguimientoArchivoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.SeguimientoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TareaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioRolService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Routes;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.status.Active;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConfigurationMediator {
    private final EstructuraService estructuraService;
    private final ActividadService actividadService;
    private final PersonaService personaService;
    private final FotoPersonaService fotoPersonaService;
    private final UsuarioService usuarioService;
    private final UsuarioRolService usuarioRolService;
    private final PlanTrabajoService planTrabajoService;
    private final EtapaService etapaService;
    private final TareaService tareaService;
    private final SeguimientoService seguimientoService;
    private final ArchivoService archivoService;
    private final SeguimientoArchivoService seguimientoArchivoService;
    private final MediaMediator mediaMediator;

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
        ActividadEntity activityToDelete = actividadService.findByIdEstructura(id);
        if (activityToDelete != null){
            actividadService.deleteByProcedure(activityToDelete.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        estructuraService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
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
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    private void deleteStructure(Long id, List<Long> deletedStructures ) throws CiadtiException{
        EstructuraEntity structure = estructuraService.findById(id);
        if (structure.getSubEstructuras() != null){
            for (EstructuraEntity e : structure.getSubEstructuras()){
                deleteStructure(e.getId(), deletedStructures);
            }
        }
        if (!deletedStructures.contains(id)){
            ActividadEntity activityToDelete = actividadService.findByIdEstructura(id);
            if (activityToDelete != null){
                actividadService.deleteByProcedure(activityToDelete.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
            }
            estructuraService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
            deletedStructures.add(id);
        }
    }

    /**
     * Crea o actualiza la información de una persona con su respectiva foto de perfil
     * @param personaEntity
     * @param photoFile
     * @return PersonaEntity
     * @throws IOException
     * @throws CloneNotSupportedException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public PersonaEntity savePerson(PersonaEntity personaEntity, MultipartFile photoFile) throws IOException, CloneNotSupportedException {
        FotoPersonaEntity fotoPersonaEntity;

        PersonaEntity personToSave = (PersonaEntity) personaEntity.clone();
        personToSave.setUsuario(null);
        personaService.save(personToSave);
        personaEntity.setId(personToSave.getId());

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
     * Elimina todas las estructuras pasadas en el parámetro structureIds
     * @param personIds: Contiene los identificadores de las estructuras a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deletePeople(List<Long> personIds) throws CiadtiException {
        for (Long id : personIds){
            deletePerson(id);
        }
    }

    /**
     * Elimina una Persona junto a su objeto FotoPersona si tiene relacionada una foto de perfil
     * @param id
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deletePerson(Long id){
        FotoPersonaEntity fotoPersona = fotoPersonaService.findByIdPersona(id);
        UsuarioEntity usuarioEntity = usuarioService.findByIdPersona(id);
        if (usuarioEntity != null){
            deleteUser(usuarioEntity.getId());
        }
        if (fotoPersona != null){
            fotoPersonaService.deleteByProcedure(fotoPersona.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        personaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Crea la información de un usuario junto a sus roles.
     * @param usuarioEntity: Objeto con información del usuario a crear o actualizar
     * @return
     * @throws CloneNotSupportedException 
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public UsuarioEntity createUser(UsuarioEntity usuarioEntity) throws CloneNotSupportedException {
        UsuarioRolEntity usuarioRolEntity;
        UsuarioEntity usuarioEntityToSave = (UsuarioEntity) usuarioEntity.clone();
        usuarioEntityToSave.setRoles(null);
        usuarioService.save(usuarioEntityToSave);
        usuarioEntity.setId(usuarioEntityToSave.getId());
        if (usuarioEntity.getRoles() != null){
            for (RolEntity rol : usuarioEntity.getRoles()) {
                usuarioRolEntity =  UsuarioRolEntity.builder()
                                    .idRol(rol.getId())
                                    .idUsuario(usuarioEntity.getId())
                                    .build();
                usuarioRolService.save(usuarioRolEntity);
            }    
        }
        return usuarioEntity;
    }

    /**
     * Actualiza la información de un usuario junto a sus roles.
     * Nota: Si en la lista de los nuevos roles no se encuentra un 
     * rol que ya existía en la base de datos, entonces se procede a eliminarlo.
     * @param usuarioEntity: Objeto con información del usuario a crear o actualizar
     * @return
     * @throws CloneNotSupportedException 
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public UsuarioEntity updateUser(UsuarioEntity usuarioEntity, List<RolEntity> newRoles) throws CloneNotSupportedException {
        UsuarioRolEntity usuarioRolEntity;
        List<RolEntity> oldRoles = usuarioEntity.getRoles();
        List<RolEntity> rolesToDelete = oldRoles.stream()
                .filter(e -> !newRoles.stream().map(RolEntity::getId).collect(Collectors.toList()).contains(e.getId()))
                .collect(Collectors.toList());
        List<RolEntity> rolesToInsert = newRoles.stream()
                .filter(e -> !oldRoles.stream().map(RolEntity::getId).collect(Collectors.toList()).contains(e.getId()))
                .collect(Collectors.toList());

        UsuarioEntity usuarioEntityToSave = (UsuarioEntity) usuarioEntity.clone();
        usuarioEntityToSave.setRoles(null);
        usuarioService.save(usuarioEntityToSave);

        for (RolEntity e : rolesToInsert){
            usuarioRolEntity =  UsuarioRolEntity.builder()
                                    .idRol(e.getId())
                                    .idUsuario(usuarioEntity.getId())
                                    .build();
            usuarioRolService.save(usuarioRolEntity);
        }

        for (RolEntity e : rolesToDelete){
            usuarioRolEntity =  usuarioRolService.findByIdUsuarioAndIdRol(usuarioEntity.getId(), e.getId());
            usuarioRolService.deleteByProcedure(usuarioRolEntity.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        usuarioEntity.setRoles(newRoles);
        return usuarioEntity;
    }

    /**
     * Elimina un usuario junto a su relación con los roles que tiene.
     * @param id: Identificador del usuario
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteUser(Long id) {
        List<UsuarioRolEntity> usuarioRoles = usuarioRolService.findAllByIdUsuario(id);
        usuarioRoles.forEach( e -> {
            usuarioRolService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        });
        usuarioService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina un plan de trabajo junto a sus etapas.
     * @param id: Identificador del plan de trabajo
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteWorkplan(Long id) {
        List<EtapaEntity> stagesToDelete = etapaService.findAllByIdPlanTrabajo(id);
        for (EtapaEntity e : stagesToDelete){
            deleteStage(e.getId());
        }
        planTrabajoService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }  

    /**
     * Elimina una etapa junto a sus tareas
     * @param id: identificador de la etapa
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStage(Long id){
        List<TareaEntity> taskToDelete = tareaService.findAllByIdEtapa(id);
        List<EtapaEntity> subStagesToDelete = etapaService.findAllSubstages(id);
        for (TareaEntity t : taskToDelete){
            deleteTask(t.getId());
        }
        for (EtapaEntity e : subStagesToDelete){
            deleteStage(e.getId());
        }
        etapaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina una tarea junto a los seguimientos realizados
     * @param id: Identificador de la tarea
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTask(Long id){
        List<SeguimientoEntity> followUpList = seguimientoService.findAllByIdTarea(id);
        for(SeguimientoEntity s : followUpList){
            deleteFollowUp(s.getId());
        }
        tareaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina un seguimiento junto a los archivos soportes cargados
     * @param id: Identificador del seguimiento
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteFollowUp(Long id){
        List<ArchivoEntity> files = archivoService.findAllByIdSeguimiento(id);
        SeguimientoArchivoEntity seguimientoArchivoEntity;
        for (ArchivoEntity f : files){
            seguimientoArchivoEntity = seguimientoArchivoService.findByIdSeguimientoAndIdArchivo(id, f.getId());
            seguimientoArchivoService.deleteByProcedure(seguimientoArchivoEntity.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        seguimientoService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Crea o actualiza un seguimiento junto a los nuevos archivos soportes.
     * Nota: En el atributo archivos del objeto de la clase SeguimientoEntity se definen los archivos que no ha sido removidos en la actualización.
     * @param seguimientoEntity: Objeto con información del seguimiento
     * @param files: Lista de nuevos archivos soportes
     * @return: SeguimientoEntity
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public SeguimientoEntity saveFollowUp(SeguimientoEntity seguimientoEntity, List<MultipartFile> files) throws CloneNotSupportedException {
        ArchivoEntity archivoEntity;
    
        seguimientoEntity.setActivo(seguimientoEntity.getPorcentajeAvance() >= 100 ? Active.ACTIVATED : Active.INACTIVATED);
        tareaService.updateActivoById(seguimientoEntity.getIdTarea(), seguimientoEntity.getActivo(), RegisterContext.getRegistradorDTO().getJsonAsString());

        SeguimientoEntity seguimientoEntityToSave = (SeguimientoEntity) seguimientoEntity.clone();
        seguimientoService.save(seguimientoEntityToSave);
        seguimientoEntity.setId(seguimientoEntityToSave.getId());
        seguimientoEntity.setFecha(seguimientoEntityToSave.getFecha());

        SeguimientoArchivoEntity seguimientoArchivoEntity;
        List<ArchivoEntity> filesBD = archivoService.findAllByIdSeguimiento(seguimientoEntity.getId());
        List<ArchivoEntity>  filesToDelete = filesBD.stream()
                    .filter(obj -> seguimientoEntity.getArchivos().stream().noneMatch(filtro -> Objects.equals(filtro.getId(), obj.getId()))).toList();
        for (ArchivoEntity a : filesToDelete){
            seguimientoArchivoEntity =  seguimientoArchivoService.findByIdSeguimientoAndIdArchivo(seguimientoEntity.getId(), a.getId());
            seguimientoArchivoService.deleteByProcedure(seguimientoArchivoEntity.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }

        if (files != null){
            for (MultipartFile file : files){
                archivoEntity = mediaMediator.saveFile(file,  Routes.PATH_SUPPORTS.getPath());
                seguimientoArchivoService.save(
                    SeguimientoArchivoEntity
                        .builder()
                        .idSeguimiento(seguimientoEntity.getId())
                        .idArchivo(archivoEntity.getId())
                        .build()
                );
                if (seguimientoEntity.getArchivos() == null){
                    seguimientoEntity.setArchivos(new ArrayList<>());
                }
                seguimientoEntity.getArchivos().add(archivoEntity);
            }
        }
        return seguimientoEntity;
    }


     /**
     * Elimina todos los planes de trabajos pasados en el parámetro workplanIds
     * @param workplanIds: Contiene los identificadores de los planes de trabajo a eliminar
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteWorkplans(List<Long> workplanIds) {
        for (Long id : workplanIds){
            deleteWorkplan(id);
        }
    } 

    /**
     * Elimina todas las etapas pasadas en el parámetro stageIds
     * @param stageIds: Contiene los identificadores de las etapas a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStages(List<Long> stageIds) throws CiadtiException {
        List<Long> delectedStages = new ArrayList<>();
        for (Long id : stageIds){
            deleteStage(id, delectedStages);
        }
    } 

    /**
     * Elimina una etapa y sus subetapas de manera recursiva
     * @param id: identificador de la etapa a eliminar
     * @param deletedStructures: almacena las etapas que se han eliminado, esto para evitar tratar 
     *                           de eliminar una etapa que ha sido eliminada en el mismo proceso
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    private void deleteStage(Long id, List<Long> deleteStages) throws CiadtiException{
        EtapaEntity stage = etapaService.findById(id);
        if (stage.getSubEtapas() != null){
            for (EtapaEntity e : stage.getSubEtapas()){
                deleteStage(e.getId(), deleteStages);
            }
        }
        if (!deleteStages.contains(id)){
            deleteStage(id);
            deleteStages.add(id);
        }
    }

    /**
     * Elimina las tareas con sus seguimientos
     * @param taskIds: Lista de tareas a eliminar
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTasks(List<Long> taskIds) {
        for (Long id : taskIds){
            deleteTask(id);
        }
    }

}
