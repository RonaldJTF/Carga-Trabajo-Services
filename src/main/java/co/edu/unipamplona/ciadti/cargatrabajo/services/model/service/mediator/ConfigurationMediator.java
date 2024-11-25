package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher.CipherService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ConsolidatedOfWorkplanDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ConsolidatedOfWorkplanDTO.DateAdvance;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.DependenciaDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.*;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.*;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Routes;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.status.Active;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.status.State;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final PasswordEncoder passwordEncoder;
    private final CipherService cipherService;
    private final RolService rolService;
    private final GeneroService generoService;
    private final NivelService nivelService;
    private final TipoDocumentoService tipoDocumentoService;
    private final TipologiaService tipologiaService;
    private final FtpService ftpService;
    private final AccionService accionService;
    private final TipologiaAccionService tipologiaAccionService;
    private final AlcanceService alcanceService;
    private final CategoriaService categoriaService;
    private final PeriodicidadService periodicidadService;
    private final CompensacionLaboralService compensacionLaboralService;
    private final CargoService cargoService;
    private final VigenciaService vigenciaService;
    private final ValorVigenciaService valorVigenciaService;
    private final VariableService variableService;
    private final NormatividadService normatividadService;
    private final EscalaSalarialService escalaSalarialService;
    private final TipoNormatividadService tipoNormatividadService;
    private final ReglaService reglaService;
    private final CompensacionLabNivelVigenciaService compensacionLabNivelVigenciaService;
    private final CompensacionLabNivelVigValorService compensacionLabNivelVigValorService;
    private final GeneralExpressionMediator generalExpressionMediator;

    /**
     * Crea una estructura, y reorganiza las subestructuras en la estructura padre que lo contiene
     * @param structure
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public EstructuraEntity createStructure(EstructuraEntity structure) {
        estructuraService.save(structure);
        boolean exists = estructuraService.existsByIdPadreAndOrdenAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId());
        if (exists) {
            estructuraService.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId(), 1);
        }
        return structure;
    }

    /**
     * Actualiza una estructura, y reorganiza las subestructuras en la estructura padre que lo contiene
     * @param structure
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public EstructuraEntity updateStructure(EstructuraEntity structure, Long previousOrder) {
        estructuraService.save(structure);
        boolean exists = estructuraService.existsByIdPadreAndOrdenAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId());
        if (exists) {
            if (previousOrder != null) {
                if (previousOrder >= structure.getOrden()) {
                    estructuraService.updateOrdenByIdPadreAndOrdenBeetwenAndNotId(structure.getIdPadre(), structure.getOrden(), previousOrder, structure.getId(), 1);
                } else {
                    estructuraService.updateOrdenByIdPadreAndOrdenBeetwenAndNotId(structure.getIdPadre(), previousOrder, structure.getOrden(), structure.getId(), -1);
                }
            } else {
                estructuraService.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId(), 1);
            }
        }
        return structure;
    }

    /**
     * Elimina una estructura por su id y todas sus subestructuras en cascada.
     * @param id: Identificador de la estructura a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStructure(Long id) throws CiadtiException {
        EstructuraEntity structure = estructuraService.findById(id);
        estructuraService.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId(), -1);
        if (structure.getSubEstructuras() != null) {
            for (EstructuraEntity e : structure.getSubEstructuras()) {
                deleteStructure(e.getId());
            }
        }
        ActividadEntity activityToDelete = actividadService.findByIdEstructura(id);
        if (activityToDelete != null) {
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
        for (Long id : structureIds) {
            deleteStructure(id, deletedStructures);
        }
    }

    /**
     * Elimina una estructura y sus subestructuras de manera recursiva
     * @param id:                identificador de la estructura a eliminar
     * @param deletedStructures: almacena las estructuras que se han eliminado, esto para evitar tratar
     *                           de eliminar una estructura que ha sido eliminada en el mismo proceso
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    private void deleteStructure(Long id, List<Long> deletedStructures) throws CiadtiException {
        EstructuraEntity structure = estructuraService.findById(id);
        estructuraService.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(structure.getIdPadre(), structure.getOrden(), structure.getId(), -1);
        if (structure.getSubEstructuras() != null) {
            for (EstructuraEntity e : structure.getSubEstructuras()) {
                deleteStructure(e.getId(), deletedStructures);
            }
        }
        if (!deletedStructures.contains(id)) {
            ActividadEntity activityToDelete = actividadService.findByIdEstructura(id);
            if (activityToDelete != null) {
                actividadService.deleteByProcedure(activityToDelete.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
            }
            estructuraService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
            deletedStructures.add(id);
        }
    }

    /**
     * Crea una copia de una estructura con ids actualizados sus procedimientos, procesos y actividades si este cuanta con ellas.
     * @param id
     * @return 
    * @throws CiadtiException  
    */

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void updateParentIds(EstructuraEntity copiedStructure, Long newParentId) throws CiadtiException {
        Long order = estructuraService.findLastOrderByIdPadre(newParentId);
        copiedStructure.setIdPadre(newParentId);
        copiedStructure.setOrden(order + 1);
        estructuraService.save(copiedStructure);
    }

    /**
     * Pega las estructuras copiadas respetando las jerarquías de tipología
     * @param copiedStructure
     * @param newParentId
     * @throws CiadtiException
    * @throws CloneNotSupportedException 
    */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void pasteStructure(EstructuraEntity copiedStructure, Long newParentId) throws Exception {
        ActividadEntity activity = null;

        if (copiedStructure.getActividad() != null) {
            activity = (ActividadEntity) copiedStructure.getActividad().clone();
        }

        copiedStructure.setActividad(activity);
        copiedStructure.setId(null);

        copiedStructure.setIdPadre(newParentId);
        estructuraService.save(copiedStructure);
        
        if(activity != null){
            activity.setId(null);
            activity.setIdEstructura(copiedStructure.getId());
            actividadService.save(activity);
        }
        
        if (copiedStructure.getSubEstructuras() != null && !copiedStructure.getSubEstructuras().isEmpty()) {
            for (EstructuraEntity subStructure : copiedStructure.getSubEstructuras()) {
                pasteStructure(subStructure, copiedStructure.getId());
            }
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

        if (photoFile != null) {
            fotoPersonaEntity = fotoPersonaService.findByIdPersona(personaEntity.getId());
            if (fotoPersonaEntity != null) {
                fotoPersonaEntity.setArchivo(photoFile.getBytes());
                fotoPersonaEntity.setMimetype(photoFile.getContentType());
            } else {
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
        for (Long id : personIds) {
            deletePerson(id);
        }
    }

    /**
     * Elimina una Persona junto a su objeto FotoPersona si tiene relacionada una foto de perfil
     * @param id
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deletePerson(Long id) {
        FotoPersonaEntity fotoPersona = fotoPersonaService.findByIdPersona(id);
        UsuarioEntity usuarioEntity = usuarioService.findByIdPersona(id);
        if (usuarioEntity != null) {
            deleteUser(usuarioEntity.getId());
        }
        if (fotoPersona != null) {
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
    public UsuarioEntity createUser(UsuarioEntity usuarioEntity) throws CloneNotSupportedException, CiadtiException {
        UsuarioRolEntity usuarioRolEntity;
        UsuarioEntity usuarioEntityToSave = (UsuarioEntity) usuarioEntity.clone();
        usuarioEntityToSave.setRoles(null);
        try {
            usuarioService.save(usuarioEntityToSave);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException constraintViolationException) {
                if (constraintViolationException.getSQLException().getMessage().contains("usua_username_uk")) {
                    throw new CiadtiException("El nombre de usuario ya existe.", 500);
                }
            }
            throw new CiadtiException("Error al guardar el usuario", 500);
        }
        usuarioEntity.setId(usuarioEntityToSave.getId());
        if (usuarioEntity.getRoles() != null) {
            for (RolEntity rol : usuarioEntity.getRoles()) {
                usuarioRolEntity = UsuarioRolEntity.builder()
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

        for (RolEntity e : rolesToInsert) {
            usuarioRolEntity = UsuarioRolEntity.builder()
                    .idRol(e.getId())
                    .idUsuario(usuarioEntity.getId())
                    .build();
            usuarioRolService.save(usuarioRolEntity);
        }

        for (RolEntity e : rolesToDelete) {
            usuarioRolEntity = usuarioRolService.findByIdUsuarioAndIdRol(usuarioEntity.getId(), e.getId());
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
        usuarioRoles.forEach(e -> {
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
        for (EtapaEntity e : stagesToDelete) {
            deleteStage(e.getId());
        }
        planTrabajoService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina una etapa junto a sus tareas
     * @param id: identificador de la etapa
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteStage(Long id) {
        List<TareaEntity> taskToDelete = tareaService.findAllByIdEtapa(id);
        List<EtapaEntity> subStagesToDelete = etapaService.findAllSubstages(id);
        for (TareaEntity t : taskToDelete) {
            deleteTask(t.getId());
        }
        for (EtapaEntity e : subStagesToDelete) {
            deleteStage(e.getId());
        }
        etapaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina una tarea junto a los seguimientos realizados
     * @param id: Identificador de la tarea
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTask(Long id) {
        List<SeguimientoEntity> followUpList = seguimientoService.findAllByIdTarea(id);
        for (SeguimientoEntity s : followUpList) {
            deleteFollowUp(s.getId());
        }
        tareaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina un seguimiento junto a los archivos soportes cargados
     * @param id: Identificador del seguimiento
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteFollowUp(Long id) {
        List<ArchivoEntity> files = archivoService.findAllByIdSeguimiento(id);
        SeguimientoArchivoEntity seguimientoArchivoEntity;
        for (ArchivoEntity f : files) {
            seguimientoArchivoEntity = seguimientoArchivoService.findByIdSeguimientoAndIdArchivo(id, f.getId());
            seguimientoArchivoService.deleteByProcedure(seguimientoArchivoEntity.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        seguimientoService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Crea o actualiza un seguimiento junto a los nuevos archivos soportes.
     * Nota: En el atributo archivos del objeto de la clase SeguimientoEntity se definen los archivos que no ha sido removidos en la actualización.
     * @param seguimientoEntity: Objeto con información del seguimiento
     * @param files:             Lista de nuevos archivos soportes
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
        List<ArchivoEntity> filesToDelete = filesBD.stream()
                .filter(obj -> seguimientoEntity.getArchivos().stream().noneMatch(filtro -> Objects.equals(filtro.getId(), obj.getId()))).toList();
        for (ArchivoEntity a : filesToDelete) {
            seguimientoArchivoEntity = seguimientoArchivoService.findByIdSeguimientoAndIdArchivo(seguimientoEntity.getId(), a.getId());
            seguimientoArchivoService.deleteByProcedure(seguimientoArchivoEntity.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }

        if (files != null) {
            for (MultipartFile file : files) {
                archivoEntity = mediaMediator.saveFile(file, Routes.PATH_SUPPORTS.getPath());
                seguimientoArchivoService.save(
                        SeguimientoArchivoEntity
                                .builder()
                                .idSeguimiento(seguimientoEntity.getId())
                                .idArchivo(archivoEntity.getId())
                                .build()
                );
                if (seguimientoEntity.getArchivos() == null) {
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
        for (Long id : workplanIds) {
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
        for (Long id : stageIds) {
            deleteStage(id, delectedStages);
        }
    }

    /**
     * Elimina una etapa y sus subetapas de manera recursiva
     * @param id:                identificador de la etapa a eliminar
     * @param deletedStructures: almacena las etapas que se han eliminado, esto para evitar tratar
     *                           de eliminar una etapa que ha sido eliminada en el mismo proceso
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    private void deleteStage(Long id, List<Long> deleteStages) throws CiadtiException {
        EtapaEntity stage = etapaService.findById(id);
        if (stage.getSubEtapas() != null) {
            for (EtapaEntity e : stage.getSubEtapas()) {
                deleteStage(e.getId(), deleteStages);
            }
        }
        if (!deleteStages.contains(id)) {
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
        for (Long id : taskIds) {
            deleteTask(id);
        }
    }

    public List<EtapaEntity> findAllStagesByIds(List<Long> stageIds) {
        List<EtapaEntity> stages = etapaService.findAllFilteredByIds(stageIds);
        stages.forEach(e -> {
            assignInformation(e);
        });
        return stages;
    }

    public List<EtapaEntity> findAllStagesByIdWorkplan(Long idWorkplan) {
        List<EtapaEntity> stages = etapaService.findAllFilteredBy(EtapaEntity.builder().idPlanTrabajo(idWorkplan).build());
        stages.forEach(e -> {
            assignInformation(e);
        });
        return stages;
    }

    private Map<String, Number> assignInformation(EtapaEntity stage) {
        if (stage == null) {
            return null;
        }
        Double totalAdvance = 0.0;
        Integer totalTasks = 0;
        int count = 0;
        if (stage.getTareas() != null && !stage.getTareas().isEmpty()) {
            for (TareaEntity task : stage.getTareas()) {
                if (task.getSeguimientos() != null && !task.getSeguimientos().isEmpty()) {
                    SeguimientoEntity lastFollowUp = task.getSeguimientos().stream()
                            .max((f1, f2) -> f1.getFecha().compareTo(f2.getFecha()))
                            .orElse(null);

                    if (lastFollowUp != null) {
                        task.setAvance(Math.round(lastFollowUp.getPorcentajeAvance() * 10.0) / 10.0);
                    }
                } else {
                    task.setAvance(0.0);
                }
                totalAdvance += task.getAvance();
                count++;
            }
            totalTasks += stage.getTareas().size();
        }
        if (stage.getSubEtapas() != null && !stage.getSubEtapas().isEmpty()) {
            for (EtapaEntity subStage : stage.getSubEtapas()) {
                Map<String, Number> out = assignInformation(subStage);
                if (out != null) {
                    totalAdvance += (Double) out.get("advance");
                    totalTasks += (Integer) out.get("totalTasks");
                }
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        stage.setAvance(Math.round((totalAdvance / count) * 10.0) / 10.0);
        stage.setTotalTareas(totalTasks);

        Map<String, Number> out = new HashMap<String, Number>();
        out.put("advance", stage.getAvance());
        out.put("totalTasks", stage.getTotalTareas());
        return out;
    }

    public Object getConsolidatedByTime(Long idWorkplan, String dateType) throws CiadtiException {
        PlanTrabajoEntity workplan = planTrabajoService.findById(idWorkplan);
        List<EtapaEntity> stages = etapaService.findAllFilteredBy(EtapaEntity.builder().idPlanTrabajo(idWorkplan).build());

        ConsolidatedOfWorkplanDTO consolidated = new ConsolidatedOfWorkplanDTO();
        consolidated.setPlanTrabajo(workplan);
        List<DateAdvance> dateAdvances = new ArrayList<DateAdvance>();

        Double advance;
        Double idealAdvance;
        Date init = new Date(Long.MAX_VALUE);
        Date end = new Date(Long.MIN_VALUE);
        Map<String, Date> scheduleDates = new HashMap<>();
        scheduleDates.put("start", init);
        scheduleDates.put("end", end);
        scheduleDates = getDates(stages, scheduleDates);
        if (scheduleDates.get("start") == init || scheduleDates.get("end") == end) {
            scheduleDates.put("start", null);
            scheduleDates.put("end", null);
        }

        Date todayDate = new Date();
        Date endDate = null;
        Date lastFollowUpDate = getLastFollowUpDate(stages, end);

        if (lastFollowUpDate == end) {
            lastFollowUpDate = null;
        }

        if (lastFollowUpDate != null && scheduleDates.get("end") != null) {
            if (lastFollowUpDate.before(scheduleDates.get("end"))) {
                endDate = todayDate.before(scheduleDates.get("end")) ? todayDate : scheduleDates.get("end");
            } else {
                endDate = lastFollowUpDate;
            }
        }

        if (scheduleDates.get("start") != null && endDate != null) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(scheduleDates.get("start"));
            endCalendar.setTime(endDate);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            endCalendar.set(Calendar.MILLISECOND, 0);

            switch ((dateType != null ? dateType : "").toUpperCase()) {
                case "DAY":
                    while (!calendar.getTime().after(endCalendar.getTime())) {
                        advance = 0.0;
                        idealAdvance = 0.0;
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, 0);
                        for (EtapaEntity e : stages) {
                            assignInformation(e, calendar.getTime());
                            advance += (e.getAvance() != null ? e.getAvance() : 0.0) / stages.size();
                            idealAdvance += (e.getIdealAvance() != null ? e.getIdealAvance() : 0.0) / stages.size();
                        }
                        dateAdvances.add(DateAdvance.builder().date(calendar.getTime()).advance(advance).idealAdvance(idealAdvance).build());
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    break;
                case "WEEK":
                    endCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                    endCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    while (!calendar.getTime().after(endCalendar.getTime())) {
                        advance = 0.0;
                        idealAdvance = 0.0;
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, 0);
                        for (EtapaEntity e : stages) {
                            assignInformation(e, calendar.getTime());
                            advance += (e.getAvance() != null ? e.getAvance() : 0.0) / stages.size();
                            idealAdvance += (e.getIdealAvance() != null ? e.getIdealAvance() : 0.0) / stages.size();
                        }
                        dateAdvances.add(DateAdvance.builder().date(calendar.getTime()).advance(advance).idealAdvance(idealAdvance).build());
                        calendar.add(Calendar.DAY_OF_MONTH, 7);
                    }
                    break;
                case "MONTH":
                    endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    while (!calendar.getTime().after(endCalendar.getTime())) {
                        advance = 0.0;
                        idealAdvance = 0.0;
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, 0);
                        for (EtapaEntity e : stages) {
                            assignInformation(e, calendar.getTime());
                            advance += (e.getAvance() != null ? e.getAvance() : 0.0) / stages.size();
                            idealAdvance += (e.getIdealAvance() != null ? e.getIdealAvance() : 0.0) / stages.size();
                        }
                        dateAdvances.add(DateAdvance.builder().date(calendar.getTime()).advance(advance).idealAdvance(idealAdvance).format("MMMM").build());
                        calendar.add(Calendar.MONTH, 1);
                    }
                    break;
                default:
                    break;
            }
        }
        consolidated.setDateAdvances(dateAdvances);
        return consolidated;
    }

    private Map<String, Date> getDates(List<EtapaEntity> stages, Map<String, Date> limitDates) {
        for (EtapaEntity stage : stages) {
            if (stage.getSubEtapas() != null && stage.getSubEtapas().size() > 0) {
                limitDates = getDates(stage.getSubEtapas(), limitDates);
            }
            if (stage.getTareas() != null && stage.getTareas().size() > 0) {
                Date tempStartDate = Collections.min(stage.getTareas().stream().map(e -> e.getFechaInicio()).toList());
                Date tempEndDate = Collections.max(stage.getTareas().stream().map(e -> e.getFechaFin()).toList());

                if (tempStartDate.before(limitDates.get("start"))) {
                    limitDates.put("start", tempStartDate);
                }
                if (tempEndDate.after(limitDates.get("end"))) {
                    limitDates.put("end", tempEndDate);
                }
            }
        }
        return limitDates;
    }


    private void assignInformation(EtapaEntity stage, Date date) {
        Double totalAdvance = 0.0;
        Double totalIdealAdvance = 0.0;
        int count = 0;
        if (stage != null) {
            if (stage.getTareas() != null) {
                for (TareaEntity task : stage.getTareas()) {
                    if (task.getSeguimientos() != null && !task.getSeguimientos().isEmpty()) {
                        Optional<SeguimientoEntity> seguimientoOpt = task.getSeguimientos().stream()
                                .filter(seguimiento -> !seguimiento.getFecha().after(date))
                                .max(Comparator.comparing(SeguimientoEntity::getFecha));

                        SeguimientoEntity seguimiento = seguimientoOpt.orElse(null);

                        if (seguimiento != null) {
                            task.setAvance(seguimiento.getPorcentajeAvance());
                        }
                    }
                    totalAdvance += task.getAvance() != null ? task.getAvance() : 0.0;
                    totalIdealAdvance += task.getFechaFin().before(date) ? 100.0 : 0.0;
                    count++;
                }
            }
            if (stage.getSubEtapas() != null) {
                for (EtapaEntity subStage : stage.getSubEtapas()) {
                    assignInformation(subStage, date);
                    totalAdvance += subStage.getAvance() != null ? subStage.getAvance() : 0.0;
                    totalIdealAdvance += subStage.getIdealAvance() != null ? subStage.getIdealAvance() : 0.0;
                    count++;
                }
            }
            if (count != 0) {
                stage.setAvance(Math.round((totalAdvance / count) * 10.0) / 10.0);
                stage.setIdealAvance(Math.round((totalIdealAdvance / count) * 10.0) / 10.0);
            }
        }
    }

    private Date getLastFollowUpDate(List<EtapaEntity> stages, Date lastDate) {
        for (EtapaEntity stage : stages) {
            if (stage.getSubEtapas() != null && stage.getSubEtapas().size() > 0) {
                lastDate = getLastFollowUpDate(stage.getSubEtapas(), lastDate);
            }
            if (stage.getTareas() != null && stage.getTareas().size() > 0) {
                for (TareaEntity task : stage.getTareas()) {
                    if (task.getSeguimientos() != null && !task.getSeguimientos().isEmpty()) {
                        SeguimientoEntity lastFollowUp = task.getSeguimientos().stream()
                                .max((f1, f2) -> f1.getFecha().compareTo(f2.getFecha()))
                                .orElse(null);

                        if (lastFollowUp != null) {
                            if (lastDate.before(lastFollowUp.getFecha())) {
                                lastDate = lastFollowUp.getFecha();
                            }
                        }
                    }
                }
            }
        }
        return lastDate;
    }

    /**
     * Método para validar si la contraseña es correcta para el usuario
     * @param usuarioEntity, objeto con los parámetros a validar
     * @return UsuarioEntity, objeto con la coincidencia encontrada
     */
    public UsuarioEntity validatePassword(UsuarioEntity usuarioEntity) {
        UsuarioEntity user = usuarioService.findByUsername(usuarioEntity.getUsername());
        if (user != null && passwordEncoder.matches(usuarioEntity.getPassword(), user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * Método para actualizar la contraseña de un usuario
     * @param data, objeto con la información del usuario a actualizar
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void changePassword(UsuarioEntity data) throws CiadtiException {
        String password = cipherService.decryptCredential(data.getPassword());
        UsuarioEntity usuario = usuarioService.findById(data.getId());
        if (usuario.getId() != null) {
            usuario.onUpdate();
            usuario.setPassword(passwordEncoder.encode(password));
            usuarioService.updatePassword(usuario);
        }
    }

    /**
     * Eliminar un rol
     * @param roleId, identificador único del rol que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteRole(Long roleId) throws CiadtiException {
        RolEntity rolDB = rolService.findById(roleId);
        if (rolDB != null) {
            rolService.deleteByProcedure(rolDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de roles
     * @param roleIds, lista de identificadores de roles a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteRoles(List<Long> roleIds) throws CiadtiException {
        for (Long id : roleIds) {
            deleteRole(id);
        }
    }

    /**
     * Eliminar un género
     * @param genderId, identificador único del género que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteGender(Long genderId) throws CiadtiException {
        GeneroEntity generoDB = generoService.findById(genderId);
        if (generoDB != null) {
            generoService.deleteByProcedure(generoDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de géneros
     * @param genderIds, lista de identificadores de géneros a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteGenders(List<Long> genderIds) throws CiadtiException {
        for (Long id : genderIds) {
            deleteGender(id);
        }
    }

    /**
     * Eliminar un nivel de ocupación
     * @param levelId, identificador único del nivel de ocupación que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteLevel(Long levelId) throws CiadtiException {
        NivelEntity nivelDB = nivelService.findById(levelId);
        List<EscalaSalarialEntity> escalasSalarialesToDelete = escalaSalarialService.findAllFilteredBy(EscalaSalarialEntity.builder().idNivel(levelId).build());

        for (EscalaSalarialEntity e : escalasSalarialesToDelete) {
            escalaSalarialService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        if (nivelDB != null) {
            nivelService.deleteByProcedure(nivelDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de niveles
     * @param levelIds, lista de identificadores de los niveles a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteLevels(List<Long> levelIds) throws CiadtiException {
        for (Long id : levelIds) {
            deleteLevel(id);
        }
    }

    /**
     * Eliminar un tipo de documento
     * @param documentTypeId, identificador único del tipo de documento que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteDocumentType(Long documentTypeId) throws CiadtiException {
        TipoDocumentoEntity tipoDocumentoDB = tipoDocumentoService.findById(documentTypeId);
        if (tipoDocumentoDB != null) {
            tipoDocumentoService.deleteByProcedure(tipoDocumentoDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de tipos de documentos
     * @param documentTypeIds, lista de identificadores de los tipos de documentos a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteDocumentTypes(List<Long> documentTypeIds) throws CiadtiException {
        for (Long id : documentTypeIds) {
            deleteDocumentType(id);
        }
    }

    /**
     * Eliminar una tipología
     * @param typologyId, identificador único de la tipología que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTypology(Long typologyId) throws CiadtiException {
        tipologiaService.deleteByProcedure(typologyId, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina lista de tipologías
     * @param typologiesIds, lista de identificadores de las tipologías a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTypoligies(List<Long> typologiesIds) throws CiadtiException {
        for (Long id : typologiesIds) {
            deleteTypology(id);
        }
    }

    /**
     * Eliminar un FTP
     * @param ftpId, identificador único del FTP que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteFtp(Long ftpId) throws CiadtiException {
        ftpService.deleteByProcedure(ftpId, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina lista de FTP's
     * @param ftpIds, lista de identificadores de los FTP's
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteFtps(List<Long> ftpIds) throws CiadtiException {
        for (Long id : ftpIds) {
            deleteFtp(id);
        }
    }

    /**
     * Guardar una acción
     * @param actionEntity, Objeto con la información de la acción a guardar
     * @return, Objeto con el id de la acción guardada
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public AccionEntity saveActionProcedure(AccionEntity actionEntity) throws CiadtiException {
        return accionService.saveActionProcedure(actionEntity);
    }

    /**
     * Eliminar una acción
     * @param actionId, identificador único de la acción que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteAction(Long actionId) throws CiadtiException {
        accionService.deleteByProcedure(actionId, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina lista de acciones
     * @param actionIds, lista de identificadores de las acciones a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteActions(List<Long> actionIds) throws CiadtiException {
        for (Long id : actionIds) {
            deleteAction(id);
        }
    }

    /**
     * Elimina relación Tipología-Acción
     * @param tipologiaAccionEntity, Objeto con información utilizada para la eliminación del registro
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTypologyAction(TipologiaAccionEntity tipologiaAccionEntity) throws CiadtiException{
        List<TipologiaAccionEntity> taDB = tipologiaAccionService.findAllFilteredBy(tipologiaAccionEntity);
        for (TipologiaAccionEntity ta : taDB) {
            tipologiaAccionService.deleteByProcedure(ta.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Eliminar lista de relaciones Tipología-Acción
     * @param idTypology, identificador único de la tipología
     * @param actionIds, lista de ids de las acciones relacionadas con la tipología
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteTypologyActions(Long idTypology, List<Long> actionIds) throws CiadtiException {
        for (Long id : actionIds) {
            TipologiaAccionEntity tipologiaAccion = new TipologiaAccionEntity();
            tipologiaAccion.setIdAccion(id);
            tipologiaAccion.setIdTipologia(idTypology);
            deleteTypologyAction(tipologiaAccion);
        }
    }

    /**
     * Obtiene las dependencias con la relación de las subdependencias.
     * @return
     * @throws CiadtiException
     */
    public List<EstructuraEntity> getDependencies() throws CiadtiException{
        List<DependenciaDTO> results = this.estructuraService.findAllDependencies();
        return results != null ? buildDependencies(results) : null;
    }

    private void filterByOnlyDependencies(List<EstructuraEntity> structures) {
        if (structures != null) {
            Iterator<EstructuraEntity> iterator = structures.iterator();
            while (iterator.hasNext()) {
                EstructuraEntity e = (EstructuraEntity) iterator.next();
                if (!Methods.convertToBoolean(e.getTipologia().getEsDependencia())) {
                    iterator.remove();
                } else {
                    this.filterByOnlyDependencies(e.getSubEstructuras());
                }
            }
        }
    }

    /**
     * Obtiene la dependencia por su id
     * @param idDependency
     * @return
     * @throws CiadtiException
     */
    public EstructuraEntity getDependencyInformation(Long idDependency) throws CiadtiException {
        EstructuraEntity dependency = (EstructuraEntity) this.estructuraService.findById(idDependency);
        if (dependency.getSubEstructuras() != null) {
            Iterator<EstructuraEntity> iterator = dependency.getSubEstructuras().iterator();
            while (iterator.hasNext()) {
                EstructuraEntity e = (EstructuraEntity) iterator.next();
                if (Methods.convertToBoolean(e.getTipologia().getEsDependencia())) {
                    this.filterByOnlyDependencies(e.getSubEstructuras());
                }
            }
        }

        return dependency;
    }

    /**
     * Método que construye un objeto con las dependencias obtenidas, se construye un mapa con la lista
     * de las dependencias, cada una con sus subdependencia sin importar la profundidad del objeto
     * @param dependencias, lista de dependencias obtenidas en la consulta
     * @return lista de estructuras
     */
    private @NonNull List<EstructuraEntity> buildDependencies(List<DependenciaDTO> dependencias) {
        Map<Long, EstructuraEntity> nodoMap = new HashMap<>();
        List<EstructuraEntity> estructura = new ArrayList<>();

        TipologiaEntity tipologia = tipologiaService.findDependencyTipology();
        List<EstructuraEntity> builtStruture = structureBuilder(dependencias, tipologia);

        for (EstructuraEntity nodo : builtStruture) {
            nodoMap.put(nodo.getId(), nodo);
        }

        for (EstructuraEntity nodo : builtStruture) {
            if (nodo.getIdPadre() != null) {
                EstructuraEntity padre = nodoMap.get(nodo.getIdPadre());
                if (padre != null) {
                    if (padre.getSubEstructuras() == null) {
                        padre.setSubEstructuras(new ArrayList<>());
                    }
                    padre.getSubEstructuras().add(nodo);
                }
            } else {
                estructura.add(nodo);
            }
        }
        return estructura;
    }

    private @NonNull List<EstructuraEntity> structureBuilder(@NonNull List<DependenciaDTO> dependencia, TipologiaEntity tipologia) {
        List<EstructuraEntity> result = new ArrayList<>();
        for (DependenciaDTO item : dependencia) {
            EstructuraEntity estructuraEntity = new EstructuraEntity();
            estructuraEntity.setId(item.getId());
            estructuraEntity.setNombre(item.getNombre());
            estructuraEntity.setDescripcion(item.getDescripcion());
            estructuraEntity.setIdPadre(item.getIdPadre());
            estructuraEntity.setRegistradoPor(item.getRegistradoPor());
            estructuraEntity.setFechaCambio(item.getFechaCambio());
            estructuraEntity.setIdTipologia(item.getIdTipo());
            estructuraEntity.setIcono(item.getIcono());
            estructuraEntity.setMimetype(item.getMimeType());
            estructuraEntity.setOrden(item.getOrden());
            if (item.getIdTipo().equals(tipologia.getId())) {
                estructuraEntity.setTipologia(tipologia);
            }
            result.add(estructuraEntity);
        }
        return result;
    }

    /**
     * Eliminar un alcance
     * @param scopeId, identificador único del alcance que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteScope(Long scopeId) throws CiadtiException {
        AlcanceEntity scopeDB = alcanceService.findById(scopeId);
        if (scopeDB != null) {
            alcanceService.deleteByProcedure(scopeDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de alcances
     * @param scopesIds, lista de identificadores de los alcances a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteScopes(List<Long> scopesIds) throws CiadtiException {
        for (Long id : scopesIds) {
            deleteScope(id);
        }
    }

    /**
     * Eliminar un tipo de categorías
     * @param categoryId, identificador único del tipo de categoría que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteCategory(Long categoryId) throws CiadtiException {
        CategoriaEntity categoryDB = categoriaService.findById(categoryId);
        if (categoryDB != null) {
            CompensacionLaboralEntity filter = new CompensacionLaboralEntity();
            filter.setIdCategoria(categoryId);
            ArrayList<CompensacionLaboralEntity> list = (ArrayList<CompensacionLaboralEntity>) compensacionLaboralService.findAllFilteredBy(filter);
            if (list != null) {
                for (CompensacionLaboralEntity c : list) {
                    compensacionLaboralService.deleteByProcedure(c.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
                }
            }
            categoriaService.deleteByProcedure(categoryDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de tipos de categorías
     * @param categoriesIds, lista de identificadores de los tipos de categorías a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteCategories(List<Long> categoriesIds) throws CiadtiException {
        for (Long id : categoriesIds) {
            deleteCategory(id);
        }
    }

    /**
     * Eliminar un tipo de periodicidad
     * @param periodicityId, identificador único del tipo de periodicidad que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deletePeriodicity(Long periodicityId) throws CiadtiException {
        PeriodicidadEntity periodicityDB = periodicidadService.findById(periodicityId);
        if (periodicityDB != null) {
            periodicidadService.deleteByProcedure(periodicityDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de periodicidad
     * @param periodicitiesIds, lista de identificadores de periodicidades a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deletePeriodicities(List<Long> periodicitiesIds) throws CiadtiException {
        for (Long id : periodicitiesIds) {
            deletePeriodicity(id);
        }
    }

    /**
     * Elimina una compensación laboral
     *
     * @param id, identificador único de la compensación laboral
     * @throws CiadtiException, excepción
     */
    public void deleteCompensation(Long id) throws CiadtiException {
       compensacionLaboralService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
    }

    /**
     * Elimina lista de compensaciones laborales
     *
     * @param compensationsIds, lista de identificadores de las compensaciones laborales a eliminar
     * @throws CiadtiException, excepción
     */
    public void deleteCompensations(List<Long> compensationsIds) throws CiadtiException {
        for (Long id : compensationsIds) {
            deleteCompensation(id);
        }
    }

    /**
     * Eliminar un tipo de vigencia
     * @param validityId, identificador único del tipo de vigencia que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteValidity(Long validityId) throws CiadtiException {
        VigenciaEntity vigenciaDB = vigenciaService.findById(validityId);
        if (vigenciaDB != null) {
            List<ValorVigenciaEntity> valoresVigencia = valorVigenciaService.findAllFilteredBy(ValorVigenciaEntity.builder().idVigencia(validityId).build());
            if (valoresVigencia != null){
                for (ValorVigenciaEntity e : valoresVigencia){
                    valorVigenciaService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
                }
            }
            vigenciaService.deleteByProcedure(vigenciaDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de tipos de vigencia
     * @param validitiesIds, lista de identificadores de los tipos de vigencia a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteValidities(List<Long> validityIds) throws CiadtiException {
        for (Long id : validityIds) {
            deleteValidity(id);
        }
    }

    /**
     * Eliminar variable junto a las variables que tienen relación con ella, de tal manera que si la que tenía relación con ella 
     * está relacionada en otras variables, estas tambien son eliminadas. Esta tarea se hace en cascada.
     * @param variableId, identificador único del tipo de variable que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteVariable(Long variableId) throws CiadtiException {
        VariableEntity variableDB = variableService.findById(variableId);
        if (variableDB != null) {
            deleteRelashionshipWithVariable(variableDB.getId());
            variableService.deleteByProcedure(variableDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
            List<ReglaEntity> rulesToDelete = reglaService.findAllWhereVariableIsIncluded(variableDB.getId());
            for (ReglaEntity r : rulesToDelete){
                reglaService.deleteByProcedure(r.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
            }
        }
    }

    /**
     * Elimina lista de variables
     * @param variableIds, lista de identificadores de los tipos de variable a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteVariables(List<Long> variableIds) throws CiadtiException {
        for (Long id : variableIds) {
            deleteVariable(id);
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    private void deleteRelashionshipWithVariable (Long id){
        List<VariableEntity> variables = variableService.findAllWhereIdIsIncluded(id);
        for (VariableEntity v : variables){
            try {
                deleteVariable(v.getId());
            } catch (CiadtiException ignored) {} //Puede que ya se eliminó en cascada otra variable seleccionada para eliminar y lance la excepción de que removió 0 filas
        }
    }

    /**
     * Eliminar una asignación de cargos
     * @param appointmentId, identificador único del cargo que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteAppointment(Long appointmentId) throws CiadtiException {
        CargoEntity cargoDB = cargoService.findById(appointmentId);
        if (cargoDB != null) {
            cargoService.deleteByProcedure(cargoDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina varias desiganciones de cargos por sus ids.
     * @param appointmentIds
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteAppointments(List<Long> appointmentIds) throws CiadtiException {
        for (Long id : appointmentIds) {
            deleteAppointment(id);
        }
    }

    /**
     * Eliminar un valor de una variable en una vigencia
     * @param valueInValidityId, identificador único del valor de  la variable en una vigencia que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteValueInValidity(Long valueInValidityId) throws CiadtiException {
        ValorVigenciaEntity valorVigenciaDB = valorVigenciaService.findById(valueInValidityId);
        if (valorVigenciaDB != null) {
            valorVigenciaService.deleteByProcedure(valorVigenciaDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

     /**
     * Eliminar una normatividad junto a sus escalas salariales
     * @param normativityId, identificador único de la normatividad que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteNormativity(Long normativityId) throws CiadtiException {
        NormatividadEntity normatividadEntityBD = normatividadService.findById(normativityId);
        List<EscalaSalarialEntity> salaryScalesToDelete = escalaSalarialService.findAllFilteredBy(EscalaSalarialEntity.builder().idNormatividad(normativityId).build());
        for (EscalaSalarialEntity e : salaryScalesToDelete){
            escalaSalarialService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        normatividadService.deleteByProcedure(normatividadEntityBD.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
    }


    /**
     * Crea o actualiza un nivel de ocupación junto a sus nuevas escalas salariales.
     * Aqui se trae la nueva lista de las escalas salariales, si ya no se incluye alguna de las existentes, 
     * entonces se es eliminada de la BD, y si se trae una nueva, entonces se es insertada en BD.
     * @param nivelEntity: Nivel de ocupación junto a las escalas salariales.
     * @return Objeto NivelEntity con la información relacionada de las escalas salariales definidas.
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public NivelEntity saveLevel(NivelEntity nivelEntity) throws CiadtiException{
        List<EscalaSalarialEntity> newSalaryScales = nivelEntity.getEscalasSalariales();
        nivelEntity = nivelService.save(nivelEntity);        
        List<EscalaSalarialEntity> oldSalaryScales = escalaSalarialService.findAllFilteredBy(EscalaSalarialEntity.builder().idNivel(nivelEntity.getId()).build());
            
        List<EscalaSalarialEntity> salaryScalesToDelete = oldSalaryScales.stream()
                .filter(e -> !newSalaryScales.stream().map(EscalaSalarialEntity :: getId).collect(Collectors.toList()).contains(e.getId()))
                .collect(Collectors.toList());

        for (EscalaSalarialEntity e : newSalaryScales) {
            e.setIdNivel(nivelEntity.getId());
            escalaSalarialService.save(e);
        }

        for (EscalaSalarialEntity e : salaryScalesToDelete) {
            escalaSalarialService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }     

        return nivelEntity;
    }

    /**
     * Actualiza una normatividad. Si el estado de la normatividad es inactivo (No vigente), entonces se inactivan todas 
     * las escalas de valoración que se rigen bajo esa normatividad. Si el estado de la normatividad es activo, entonces 
     * se verifica que todas las escalas estén inactivas para que sean activadas.
     * En todos lo contrario, los estados de las escalas salariales no se actualizan.
     * @param normatividadEntity
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public NormatividadEntity updateNormativity(NormatividadEntity normatividadEntity) {
        normatividadService.save(normatividadEntity);
        int totalActive = escalaSalarialService.countByStatusAndNormativityId(State.ACTIVATED, normatividadEntity.getId());
        if (normatividadEntity.getEstado().equals(State.INACTIVATED) || totalActive == 0){
            escalaSalarialService.updateStatusByNormativityId(
                EscalaSalarialEntity.builder().estado(normatividadEntity.getEstado()).idNormatividad(normatividadEntity.getId()).build()
            );
        }
        return normatividadEntity;
    }

    /**
     * Elimina una escala salarial de acuerdo a su id
     * @param id
     * @throws CiadtiException 
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteSalaryScale(Long id) throws CiadtiException {
        EscalaSalarialEntity salaryScaleTodelete = escalaSalarialService.findById(id);
        if (salaryScaleTodelete != null) {
            escalaSalarialService.deleteByProcedure(salaryScaleTodelete.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }


    /**
     * Crea o actualiza una vigencia junto los valores de las variables en la vigencia
     * Aqui se trae la nueva lista de los valores de las variables en la vigencia, si ya no se incluye alguna de las existentes, 
     * entonces se es eliminada de la BD, y si se trae una nueva, entonces se es insertada en BD.
     * @param vigenciaEntity
     * @return
     * @throws CiadtiException
    */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public VigenciaEntity saveValidity(VigenciaEntity vigenciaEntity) throws CiadtiException{
        List<ValorVigenciaEntity> newValuesInValidity = vigenciaEntity.getValoresVigencia();

        if(Methods.convertToBoolean(vigenciaEntity.getEstado())){
            vigenciaService.updateStateToAllValidities(State.INACTIVATED);
        }
        vigenciaEntity = vigenciaService.save(vigenciaEntity);    

        List<ValorVigenciaEntity> oldValuesInValidity = valorVigenciaService.findAllFilteredBy(ValorVigenciaEntity.builder().idVigencia(vigenciaEntity.getId()).build());
            
        List<ValorVigenciaEntity> valuesInValidityToDelete = oldValuesInValidity.stream()
                .filter(e -> !newValuesInValidity.stream().map(ValorVigenciaEntity :: getId).collect(Collectors.toList()).contains(e.getId()))
                .collect(Collectors.toList());

        for (ValorVigenciaEntity e : newValuesInValidity) {
            e.setIdVigencia(vigenciaEntity.getId());
            valorVigenciaService.save(e);
        }

        for (ValorVigenciaEntity e : valuesInValidityToDelete) {
            valorVigenciaService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }     
        return vigenciaEntity;
    }

    /**
     * Consulta una variable junto a las variables que tiene relacionada
     * @param id
     * @return
     * @throws CiadtiException
     */
    public VariableEntity findVariable(Long id) throws CiadtiException {
        VariableEntity variableEntity = variableService.findById(id);
        variableEntity.setVariablesRelacionadas(variableService.findAllByIds(extractVariableIds(variableEntity.getValor())));
        return variableEntity;
    }

    public ReglaEntity findRule(Long id) throws CiadtiException {
        ReglaEntity reglaEntity = reglaService.findById(id);
        reglaEntity.setVariablesRelacionadas(variableService.findAllByIds(extractVariableIds(reglaEntity.getCondiciones())));
        return reglaEntity;
    }


    /**
     * Extrae los id de las variables involucradas en una expresión.
     * Ejemplo: de la expresión 0.5*$[1] + $[2] extrae y devuelve la lista con los ids 1 y 2.
     * @param expression
     * @return
     */
    private List<Long> extractVariableIds(String expression) {
        List<Long> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            numbers.add(Long.parseLong(matcher.group(1)));
        }
        return numbers;
    }

    /**
     * Eliminar una regla
     * @param ruleId, identificador único de la regla que se desea eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteRule(Long ruleId) throws CiadtiException {
        ReglaEntity reglaDB = reglaService.findById(ruleId);
        if (reglaDB != null) {
            reglaService.deleteByProcedure(reglaDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de reglas de vigencia
     * @param ruleIds, lista de identificadores de las reglas a eliminar
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteRules(List<Long> ruleIds) throws CiadtiException {
        for (Long id : ruleIds) {
            deleteRule(id);
        }
    }


    /**
     * Eliminar un tipo de normatividad
     * @param normativityTypeId, identificador único del tipo de normatividad que se desea eliminar
     * @throws CiadtiException
     */
    public void deleteNormativityType(Long normativityTypeId) throws CiadtiException {
        TipoNormatividadEntity tipoNormatividadDB = tipoNormatividadService.findById(normativityTypeId);
        if (tipoNormatividadDB != null) {
            tipoNormatividadService.deleteByProcedure(tipoNormatividadDB.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
    }

    /**
     * Elimina lista de los tipos de normatividades
     * @param validityValueIds, lista de identificadores de los tipos de normatividades a eliminar
     * @throws CiadtiException
     */
    public void deleteNormativityTypes(List<Long> validityValueIds) throws CiadtiException {
        for (Long id : validityValueIds) {
            deleteNormativityType(id);
        }
    }

    /**
     * Obtiene la lista de las asignaciones laborales con sus respectivos valores por cada compenación laboral.
     * @param filters
     * @return
     * @throws CiadtiException
     */
    @Transactional(readOnly = true)
    public List<CargoEntity> findAppointments( Map<String, Long[]> filters) throws CiadtiException{
        List<CargoEntity> appointments = cargoService.findAllBy(filters);
        List<VariableEntity> allVariablesInDB = variableService.findAll();
        Map<String, Double> primaryVariables = new HashMap<>();
        for (CargoEntity appointment : appointments){
            Double asignacionTotal = 0.0;
            primaryVariables.put("${ASIGNACION_BASICA_MENSUAL}", appointment.getAsignacionBasica());
            for (CompensacionLabNivelVigenciaEntity clnv : appointment.getCompensacionesLaboralesAplicadas()){
                for (CompensacionLabNivelVigValorEntity cnvv : clnv.getValoresCompensacionLabNivelVigencia()){
                    if(cnvv.getIdRegla() == null || generalExpressionMediator.evaluateRuleConditions(cnvv.getIdRegla(),  appointment.getIdVigencia(), allVariablesInDB, primaryVariables)){
                        Double value = generalExpressionMediator.getValueOfVariable(cnvv.getIdVariable(), appointment.getIdVigencia(), allVariablesInDB, primaryVariables);
                        Long frecuency =  clnv.getCompensacionLaboral().getPeriodicidad().getFrecuenciaAnual();
                        clnv.setValorAplicado(value * frecuency);
                        asignacionTotal += clnv.getValorAplicado();
                        break;
                    }
                }
            }
            appointment.setAsignacionTotal(asignacionTotal + appointment.getAsignacionBasica());
        }
        return appointments;
    }


    /**
     * Elimina la relacion de la compensación laboral para un nivel ocupacional en una vigencia dada.
     *
     * @param id, identificador único de la relacion de la compensación laboral para un nivel ocupacional en una vigencia dada.
     * @throws CiadtiException, excepción
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteLevelCompensation(Long id) throws CiadtiException {
        List<CompensacionLabNivelVigValorEntity> valuesByRules = compensacionLabNivelVigValorService.findValuesByRulesOfLevelCompensation(id);
        for(CompensacionLabNivelVigValorEntity e : valuesByRules){
            compensacionLabNivelVigValorService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        compensacionLabNivelVigenciaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
     }
 
     /**
      * Elimina lista de relaciones de compensaciones laborales para un nivel ocupacional en vigencias dada.
      *
      * @param compensationsIds, lista de identificadores de objetos CompensacionLabNivelVigencia a eliminar.
      * @throws CiadtiException, excepción
      */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteLevelCompensations(List<Long> levelCompensationsIds) throws CiadtiException {
        for (Long id : levelCompensationsIds) {
            deleteLevelCompensation(id);
        }
    }

    /**
     * Crea o actualiza una relación entre una compensación laboral para un nivel ocupacional en una vigencia 
     * y los valores que puede tomar bajo una regla.
     * Aqui se trae la nueva lista de los valores por regla. Si ya no se incluye alguna de los valores por regla existentes, 
     * entonces se es eliminada de la BD, y si se trae una nueva, entonces se es insertada en BD.
     * @param compensacionLabNivelVigenciaEntity: Relación entre una compensación laboral para un nivel ocupacional en una vigencia y los valores por reglas.
     * @return Objeto CompensacionLabNivelVigenciaEntity con la información relacionada de los valores por regla.
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CompensacionLabNivelVigenciaEntity saveLevelCompensation(CompensacionLabNivelVigenciaEntity compensacionLabNivelVigenciaEntity) throws CiadtiException{
        List<CompensacionLabNivelVigValorEntity> newValesByRules = compensacionLabNivelVigenciaEntity.getValoresCompensacionLabNivelVigencia();
        compensacionLabNivelVigenciaEntity = compensacionLabNivelVigenciaService.save(compensacionLabNivelVigenciaEntity);        
        List<CompensacionLabNivelVigValorEntity> oldValuesByRules = compensacionLabNivelVigValorService.findAllFilteredBy(CompensacionLabNivelVigValorEntity.builder().idCompensacionLabNivelVigencia(compensacionLabNivelVigenciaEntity.getId()).build());
            
        List<CompensacionLabNivelVigValorEntity> valuesByRulesToDelete = oldValuesByRules.stream()
                .filter(e -> !newValesByRules.stream().map(CompensacionLabNivelVigValorEntity :: getId).collect(Collectors.toList()).contains(e.getId()))
                .collect(Collectors.toList());

        for (CompensacionLabNivelVigValorEntity e : newValesByRules) {
            e.setIdCompensacionLabNivelVigencia(compensacionLabNivelVigenciaEntity.getId());
            compensacionLabNivelVigValorService.save(e);
        }

        for (CompensacionLabNivelVigValorEntity e : valuesByRulesToDelete) {
            compensacionLabNivelVigValorService.deleteByProcedure(e.getId(), RegisterContext.getRegistradorDTO().getJsonAsString());
        }
        return compensacionLabNivelVigenciaEntity;
    }

    /**
     * Encuentra cada uno de los valores asociados a una regla (puede no haber regla) para la aplicación
     * de una compensacion laboral en una vigencia para un nivel ocupacional o escala salarial.
     * Si la variable que define el valor es configurada por una vigencia, entonces le asocia el valor en esa vigencia.
     * Asocia el nombre de las variables que se relacionan en las variables y en las reglas.
     * @param levelCompensationId
     * @return
     * @throws CiadtiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CompensacionLabNivelVigValorEntity> findValuesByRulesOfLevelCompensation(Long levelCompensationId) throws CiadtiException{
        List<CompensacionLabNivelVigValorEntity> list = compensacionLabNivelVigValorService.findValuesByRulesOfLevelCompensation(levelCompensationId);
        for (CompensacionLabNivelVigValorEntity cnvv : list){
            List<VariableEntity> includedVariablesInValue = variableService.findAllIncludedVariablesInVariable(cnvv.getIdVariable());
            List<VariableEntity> includedVariablesInRule = variableService.findAllIncludedVariablesInRule(cnvv.getIdRegla());
            cnvv.getVariable().setExpresionValor(generalExpressionMediator.getExpressionWithVariableNames(cnvv.getVariable().getValor(), includedVariablesInValue));
            if(cnvv.getRegla() != null){
                cnvv.getRegla().setExpresionCondiciones(generalExpressionMediator.getExpressionWithVariableNames(cnvv.getRegla().getCondiciones(), includedVariablesInRule));
            }
            if(Methods.convertToBoolean(cnvv.getVariable().getPorVigencia())){
                Double valueInValidity = compensacionLabNivelVigValorService.getValueInValidityOfValueByRule(cnvv.getId());
                cnvv.setValueInValidity(valueInValidity);
            }
        }
        return list;
    }
}
