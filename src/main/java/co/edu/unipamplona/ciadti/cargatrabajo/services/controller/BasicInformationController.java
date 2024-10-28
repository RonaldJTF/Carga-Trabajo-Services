package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.*;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.*;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class BasicInformationController {

    private final TipoDocumentoService tipoDocumentoService;
    private final NivelService nivelService;
    private final GeneroService generoService;
    private final RolService rolService;
    private final TipologiaService tipologiaService;
    private final EstructuraService estructuraService;
    private final ConfigurationMediator configurationMediator;
    private final FtpService ftpService;
    private final AccionService accionService;
    private final TipologiaAccionService tipologiaAccionService;
    private final AlcanceService alcanceService;
    private final TipoNormatividadService tipoNormatividadService;
    private final CategoriaService categoriaService;
    private final PeriodicidadService periodicidadService;


    @Operation(
            summary = "Obtener o listar los tipos de documentos",
            description = "Obtiene o lista los tipos de documentos de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del tipo de documento." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoDocumnetoEntity)." +
                    "Returns: Objeto o lista de objetos con información de la persona. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"document-type", "document-type/{id}"})
    public ResponseEntity<?> getDocumentType(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(TipoDocumentoEntity.class);
        TipoDocumentoEntity filter = (TipoDocumentoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoDocumentoService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar los géneros",
            description = "Obtiene  lista de géneros de a cuerdo a ciertas variables o parámetros" +
                    "Args: id: identificador del género. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase GeneroEntity)." +
                    "Returns: Objeto o lista de objetos con información de los géneros. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"gender", "gender/{id}"})
    public ResponseEntity<?> getGender(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(GeneroEntity.class);
        GeneroEntity filter = (GeneroEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, generoService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar los roles",
            description = "Obtiene lista de roles de acuerdo a ciertas variables o parámetros" +
                    "Args: id: identificador del rol. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase RolEntity). " +
                    "Returns: Objeto o lista de objetos con información de los roles. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"role", "role/{id}"})
    public ResponseEntity<?> getRole(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(RolEntity.class);
        RolEntity filter = (RolEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, rolService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener las tipologías con la cantidad de relaciones en la tabla estructura",
            description = "Obtiene lista de tipología registradas en la base de datos con las cantidad de relaciones en la tabla estructura" +
                    "Args: id: identificador de la tipología. " +
                    "Returns: Lista de objetos (InventarioTipologiaDTO) con información de las tipologías y la cantidad de registros en la tabla estructura.")
    @GetMapping(value = {"inventory", "inventory/{id}"})
    public ResponseEntity<?> getInventory(@PathVariable(required = false) Long id) {
        return Methods.getResponseAccordingToId(id, tipologiaService.findInventarioTipologia());
    }

    @Operation(
            summary = "Obtener información de las actividades por nivel de ocupación de una dependencia",
            description = "Obtiene la información de las actividades por nivel de ocupación de una dependencia" +
                    "Args: id: identificador de la dependencia." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase EstructuraEntity)." +
                    "Returns: Lista de objetos (ActividadOutDTO) con las estadísticas de la dependencia.")
    @GetMapping(value = {"statistics", "statistics/{id}"})
    public ResponseEntity<?> getStatistics(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(EstructuraEntity.class);
        EstructuraEntity filter = (EstructuraEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return new ResponseEntity<>(estructuraService.statisticsDependence(filter), HttpStatus.OK);
    }


    //LPR: 18 de julio de 2024

    @Operation(
            summary = "Obtener o listar las tipologías",
            description = "Obtiene o lista las tipologías de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador de la tipología." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipologiaEntity)." +
                    "Returns: Objeto o lista de objetos con información de las tipologías." +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"typology", "typology/{id}"})
    public ResponseEntity<?> getTypology(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(TipologiaEntity.class);
        TipologiaEntity filter = (TipologiaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipologiaService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar los ftp",
            description = "Obtiene o lista los ftp de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del ftp." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase FtpEntity)." +
                    "Returns: Objeto o lista de objetos con información de los ftp." +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"ftp", "ftp/{id}"})
    public ResponseEntity<?> getFtp(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(FtpEntity.class);
        FtpEntity filter = (FtpEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, ftpService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar las acciones",
            description = "Obtiene o lista las acciones de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del la acción." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase AccionEntity)." +
                    "Returns: Objeto o lista de objetos con información de las acciones." +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"action", "action/{id}"})
    public ResponseEntity<?> getAction(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(AccionEntity.class);
        AccionEntity filter = (AccionEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, accionService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un rol",
            description = "Crea un rol" +
                    "Args: rolEntity: objeto con información del rol a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/role")
    public ResponseEntity<?> createRole(@Valid @RequestBody RolEntity rolEntity) {
        RolEntity rolNew = new RolEntity();
        rolNew.setNombre(Methods.capitalizeFirstLetter(rolEntity.getNombre()));
        rolNew.setCodigo(rolEntity.getCodigo().toUpperCase());
        return new ResponseEntity<>(rolService.save(rolNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un rol",
            description = "Actualiza un rol. " +
                    "Args: rolEntity: objeto con información del rol." +
                    "id: identificador del rol." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/role/{id}")
    public ResponseEntity<?> updateRole(@Valid @RequestBody RolEntity rolEntity, @PathVariable Long id) throws CiadtiException {
        RolEntity rolDB = rolService.findById(id);
        rolDB.setNombre(Methods.capitalizeFirstLetter(rolEntity.getNombre()));
        rolDB.setCodigo(rolEntity.getCodigo().toUpperCase());
        return new ResponseEntity<>(rolService.save(rolDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar rol por el id",
            description = "Elimina un rol por su id." +
                    "Args: id: identificador del rol a eliminar.")
    @DeleteMapping("/role/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteRole(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar lista de roles por el id",
            description = "Elimina una lista de roles por su id." +
                    "Args: roleIds: identificadores de los roles a eliminar.")
    @DeleteMapping("/role")
    public ResponseEntity<?> deleteRoles(@RequestBody List<Long> roleIds) throws CiadtiException {
        configurationMediator.deleteRoles(roleIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear un género",
            description = "Crea un género" +
                    "Args: generoEntity: objeto con información del genero a registrar." +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/gender")
    public ResponseEntity<?> createGender(@Valid @RequestBody GeneroEntity generoEntity) {
        GeneroEntity genderNew = new GeneroEntity();
        genderNew.setNombre(Methods.capitalizeFirstLetter(generoEntity.getNombre()));
        return new ResponseEntity<>(generoService.save(genderNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un genero",
            description = "Actualiza un genero. " +
                    "Args: generoEntity: objeto con información del genero." +
                    "id: identificador del genero." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/gender/{id}")
    public ResponseEntity<?> updateGender(@Valid @RequestBody GeneroEntity generoEntity, @PathVariable Long id) throws CiadtiException {
        GeneroEntity generoDB = generoService.findById(id);
        generoDB.setNombre(Methods.capitalizeFirstLetter(generoEntity.getNombre()));
        return new ResponseEntity<>(generoService.save(generoDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar genero por el id",
            description = "Elimina un genero por su id." +
                    "Args: id: identificador del genero a eliminar.")
    @DeleteMapping("/gender/{id}")
    public ResponseEntity<?> deleteGender(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteGender(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar lista de géneros por el id",
            description = "Elimina una lista de géneros por su id." +
                    "Args: roleIds: identificadores de los géneros a eliminar.")
    @DeleteMapping("/gender")
    public ResponseEntity<?> deleteGenders(@RequestBody List<Long> genderIds) throws CiadtiException {
        configurationMediator.deleteGenders(genderIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear un tipo de documento",
            description = "Crea un tipo de documento" +
                    "Args: tipoDocumentoEntity: objeto con información del tipo de documento a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/document-type")
    public ResponseEntity<?> createDocumentType(@Valid @RequestBody TipoDocumentoEntity tipoDocumentoEntity) {
        TipoDocumentoEntity tipoDocumentoNew = new TipoDocumentoEntity();
        tipoDocumentoNew.setDescripcion(Methods.capitalizeFirstLetter(tipoDocumentoEntity.getDescripcion()));
        tipoDocumentoNew.setAbreviatura(tipoDocumentoEntity.getAbreviatura().toUpperCase());
        return new ResponseEntity<>(tipoDocumentoService.save(tipoDocumentoNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un tipo de documento",
            description = "Actualiza un tipo de documento." +
                    "Args: tipoDocumentoEntity: objeto con información del tipo de documento." +
                    "id: identificador del tipo de documento." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/document-type/{id}")
    public ResponseEntity<?> updateDocumentType(@Valid @RequestBody TipoDocumentoEntity tipoDocumentoEntity, @PathVariable Long id) throws CiadtiException {
        TipoDocumentoEntity tipoDocumentoDB = tipoDocumentoService.findById(id);
        tipoDocumentoDB.setDescripcion(Methods.capitalizeFirstLetter(tipoDocumentoEntity.getDescripcion()));
        tipoDocumentoDB.setAbreviatura(tipoDocumentoEntity.getAbreviatura().toUpperCase());
        return new ResponseEntity<>(tipoDocumentoService.save(tipoDocumentoDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de documento por el id",
            description = "Elimina un tipo de documento por su id." +
                    "Args: id: identificador del tipo de documento a eliminar.")
    @DeleteMapping("/document-type/{id}")
    public ResponseEntity<?> deleteDocumentType(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteDocumentType(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar tipos de documentos por el id",
            description = "Elimina lista de tipos de documentos por su id." +
                    "Args: documentTypeIds: identificadores de los tipos de documentos a eliminar.")
    @DeleteMapping("/document-type")
    public ResponseEntity<?> deleteDocumentTypes(@RequestBody List<Long> documentTypeIds) throws CiadtiException {
        configurationMediator.deleteDocumentTypes(documentTypeIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear una tipología",
            description = "Crea una tipología" +
                    "Args: tipologiaEntity: objeto con información de la tipología a registrar." +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/typology")
    public ResponseEntity<?> createTypology(@Valid @RequestBody TipologiaEntity tipologiaEntity) {
        TipologiaEntity tipologiaNew = new TipologiaEntity();
        tipologiaNew.setNombre(tipologiaEntity.getNombre());
        tipologiaNew.setClaseIcono(tipologiaEntity.getClaseIcono());
        tipologiaNew.setNombreColor(tipologiaEntity.getNombreColor());
        tipologiaNew.setEsDependencia(tipologiaEntity.getEsDependencia());
        tipologiaNew.setIdTipologiaSiguiente(tipologiaEntity.getIdTipologiaSiguiente());
        return new ResponseEntity<>(tipologiaService.save(tipologiaNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una tipología",
            description = "Actualiza una tipología." +
                    "Args: tipologiaEntity: objeto con información de la tipología." +
                    "id: identificador del tipo de documento." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/typology/{id}")
    public ResponseEntity<?> updateTypology(@Valid @RequestBody TipologiaEntity tipologiaEntity, @PathVariable Long id) throws CiadtiException {
        TipologiaEntity tipologiaDB = tipologiaService.findById(id);
        tipologiaDB.setNombre(tipologiaEntity.getNombre());
        tipologiaDB.setNombreColor(tipologiaEntity.getNombreColor());
        tipologiaDB.setClaseIcono(tipologiaEntity.getClaseIcono());
        tipologiaDB.setEsDependencia(tipologiaEntity.getEsDependencia());
        tipologiaDB.setIdTipologiaSiguiente(tipologiaEntity.getIdTipologiaSiguiente());
        return new ResponseEntity<>(tipologiaService.save(tipologiaDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipología por el id",
            description = "Elimina una tipología por su id." +
                    "Args: id: identificador de la tipología a eliminar.")
    @DeleteMapping("/typology/{id}")
    public ResponseEntity<?> deleteTypology(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteTypology(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar tipologías por el id",
            description = "Elimina una tipología por su id." +
                    "Args: typologyIds: identificadores de las tipologías a eliminar.")
    @DeleteMapping("/typology")
    public ResponseEntity<?> deleteTypologies(@RequestBody List<Long> typologyIds) throws CiadtiException {
        configurationMediator.deleteTypoligies(typologyIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear un servidor FTP",
            description = "Crea un servidor FTP." +
                    "Args: ftpEntity: objeto con información del ftp a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/ftp")
    public ResponseEntity<?> createFtp(@Valid @RequestBody FtpEntity ftpEntity) {
        FtpEntity ftpNew = new FtpEntity();
        ftpNew.setNombre(ftpEntity.getNombre());
        ftpNew.setDescripcion(ftpEntity.getDescripcion());
        ftpNew.setCodigo(ftpEntity.getCodigo());
        ftpNew.setActivo(ftpEntity.getActivo());
        return new ResponseEntity<>(ftpService.save(ftpNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un servidor FTP",
            description = "Actualiza un servidor FTP." +
                    "Args: ftpEntity: objeto con información del servidor FTP." +
                    "id: identificador del FTP." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/ftp/{id}")
    public ResponseEntity<?> updateFtp(@Valid @RequestBody FtpEntity ftpEntity, @PathVariable Long id) throws CiadtiException {
        FtpEntity ftpDB = ftpService.findById(id);
        ftpDB.setNombre(ftpEntity.getNombre());
        ftpDB.setDescripcion(ftpEntity.getDescripcion());
        ftpDB.setCodigo(ftpEntity.getCodigo());
        ftpDB.setActivo(ftpEntity.getActivo());
        return new ResponseEntity<>(ftpService.save(ftpDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar servidor FTP por el id",
            description = "Elimina un servidor FTP por su id." +
                    "Args: id: identificador de la tipología a eliminar.")
    @DeleteMapping("/ftp/{id}")
    public ResponseEntity<?> deleteFtp(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteFtp(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar FTP's por el id",
            description = "Elimina FTP's por su id." +
                    "Args: ftpIds: identificadores de los FTP's a eliminar.")
    @DeleteMapping("/ftp")
    public ResponseEntity<?> deleteFtps(@RequestBody List<Long> ftpIds) throws CiadtiException {
        configurationMediator.deleteFtps(ftpIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear una acción",
            description = "Crea una acción." +
                    "Args: accionEntity: objeto con información de la acción a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/action")
    public ResponseEntity<?> createAction(@Valid @RequestBody AccionEntity accionEntity) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.saveActionProcedure(accionEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una acción",
            description = "Actualiza una acción." +
                    "Args: accionEntity: objeto con información de la acción." +
                    "id: identificador de la acción." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/action/{id}")
    public ResponseEntity<?> updateAction(@Valid @RequestBody AccionEntity accionEntity, @PathVariable Long id) throws CiadtiException {
        AccionEntity accionDB = accionService.findById(id);
        accionDB.setNombre(accionEntity.getNombre());
        accionDB.setClaseIcono(accionEntity.getClaseIcono());
        accionDB.setClaseEstado(accionEntity.getClaseEstado());
        accionDB.setPath(accionEntity.getPath());
        return new ResponseEntity<>(accionService.save(accionDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar acción por el id",
            description = "Elimina una acción por su id." +
                    "Args: id: identificador de la acción a eliminar.")
    @DeleteMapping("/action/{id}")
    public ResponseEntity<?> deleteAction(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteAction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar acciones por el id",
            description = "Elimina acciones por su id." +
                    "Args: acctionIds: identificadores de las acciones a eliminar.")
    @DeleteMapping("/action")
    public ResponseEntity<?> deleteActions(@RequestBody List<Long> acctionIds) throws CiadtiException {
        configurationMediator.deleteActions(acctionIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear la relación entre una tipología y una acción",
            description = "Crea una acción." +
                    "Args: accionEntity: objeto con información de la acción a relacionar. " +
                    "id: identificador de la tipología a relacionar con la acción" +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/typology-action/{id}")
    public ResponseEntity<?> createTypologyAction(@Valid @RequestBody AccionEntity accionEntity, @PathVariable Long id) throws CiadtiException {
        TipologiaAccionEntity taNew = new TipologiaAccionEntity();
        taNew.setIdTipologia(id);
        if (accionEntity.getId() != null) {
            taNew.setIdAccion(accionEntity.getId());
        } else {
            taNew.setIdAccion(configurationMediator.saveActionProcedure(accionEntity).getId());
        }
        return new ResponseEntity<>(tipologiaAccionService.save(taNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar relación Tipología-Acción por id de la tipología y el id de la acción",
            description = "Elimina relación Tipología-Acción por id de la tipología y el id de la acción." +
                    "Args: tipologiaAccionEntity: objeto con los identificadores de las TipologiaAccion a eliminar.")
    @DeleteMapping("/typology-action")
    public ResponseEntity<?> deleteTypologyAction(@Valid @RequestBody TipologiaAccionEntity tipologiaAccionEntity) throws CiadtiException {
        configurationMediator.deleteTypologyAction(tipologiaAccionEntity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/typology-action/{id}")
    public ResponseEntity<?> deleteTypologyActions(@Valid @RequestBody List<Long> acctionIds, @PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteTypologyActions(id, acctionIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar los tipos de alcances",
            description = "Obtiene o lista los tipos de alcances de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del tipo de alcance." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase AlcanceEntity)." +
                    "Returns: Objeto o lista de objetos con información del alcance. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"scope", "scope/{id}"})
    public ResponseEntity<?> getScope(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(AlcanceEntity.class);
        AlcanceEntity filter = (AlcanceEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, alcanceService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un tipo de alcance",
            description = "Crea un tipo de alcance" +
                    "Args: alcanceEntity: objeto con información del tipo de alcance o a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/scope")
    public ResponseEntity<?> createScope(@Valid @RequestBody AlcanceEntity alcanceEntity) {

        AlcanceEntity alcanceNew = new AlcanceEntity();
        alcanceNew.setNombre(alcanceEntity.getNombre().toUpperCase());
        alcanceNew.setDescripcion(Methods.capitalizeFirstLetter(alcanceEntity.getDescripcion()));
        return new ResponseEntity<>(alcanceService.save(alcanceNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un tipo de alcance",
            description = "Actualiza un tipo de alcance." +
                    "Args: alcanceEntity: objeto con información del tipo de alcance." +
                    "id: identificador del tipo de alcance." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/scope/{id}")
    public ResponseEntity<?> updateScope(@Valid @RequestBody AlcanceEntity alcanceEntity, @PathVariable Long id) throws CiadtiException {
        AlcanceEntity alcanceDB = alcanceService.findById(id);
        alcanceDB.setDescripcion(Methods.capitalizeFirstLetter(alcanceEntity.getDescripcion()));
        alcanceDB.setNombre(alcanceEntity.getNombre().toUpperCase());
        return new ResponseEntity<>(alcanceService.save(alcanceDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de alcance por el id",
            description = "Elimina un tipo de alcance por su id." +
                    "Args: id: identificador del tipo de alcance a eliminar.")
    @DeleteMapping("/scope/{id}")
    public ResponseEntity<?> deleteScope(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteScope(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar tipos de alcances por el id",
            description = "Elimina lista de tipos de alcances por su id." +
                    "Args: documentTypeIds: identificadores de los tipos de alcances a eliminar.")
    @DeleteMapping("/scope")
    public ResponseEntity<?> deleteScopes(@RequestBody List<Long> scopeIds) throws CiadtiException {
        configurationMediator.deleteScopes(scopeIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar los tipos de normatividades",
            description = "Obtiene o lista los tipos de normatividades de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del tipo de normatividad." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoNormatividadEntity)." +
                    "Returns: Objeto o lista de objetos con información del tipo de normatividad. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"normativity-type", "normativity-type/{id}"})
    public ResponseEntity<?> getNormativityType(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(TipoNormatividadEntity.class);
        TipoNormatividadEntity filter = (TipoNormatividadEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoNormatividadService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar los tipos de categorías",
            description = "Obtiene o lista los tipos de categorías de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del tipo de categoría." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CategoriaEntity)." +
                    "Returns: Objeto o lista de objetos con información de la categoría. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"category", "category/{id}"})
    public ResponseEntity<?> getCategory(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(CategoriaEntity.class);
        CategoriaEntity filter = (CategoriaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, categoriaService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un tipo de categoría",
            description = "Crea un tipo de categoría" +
                    "Args: categoriaEntity: objeto con información del tipo de categoría a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/category")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoriaEntity categoriaEntity) {
        CategoriaEntity categoriaNew = new CategoriaEntity();
        categoriaNew.setNombre(categoriaEntity.getNombre().toUpperCase());
        categoriaNew.setDescripcion(Methods.capitalizeFirstLetter(categoriaEntity.getDescripcion()));
        return new ResponseEntity<>(categoriaService.save(categoriaNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un tipo de categoría",
            description = "Actualiza un tipo de categoría." +
                    "Args: categoriaEntity: objeto con información del tipo de categoría." +
                    "id: identificador del tipo de categoría." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/category/{id}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoriaEntity categoriaEntity, @PathVariable Long id) throws CiadtiException {
        CategoriaEntity categoriaDB = categoriaService.findById(id);
        categoriaDB.setDescripcion(Methods.capitalizeFirstLetter(categoriaEntity.getDescripcion()));
        categoriaDB.setNombre(categoriaEntity.getNombre().toUpperCase());
        return new ResponseEntity<>(categoriaService.save(categoriaDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de categoría por el id",
            description = "Elimina un tipo de categoría por su id." +
                    "Args: id: identificador del tipo de categoría a eliminar.")
    @DeleteMapping("/category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar tipos de categorías por el id",
            description = "Elimina lista de tipos de categorías por su id." +
                    "Args: documentTypeIds: identificadores de los tipos de categorías a eliminar.")
    @DeleteMapping("/category")
    public ResponseEntity<?> deleteCategories(@RequestBody List<Long> categoriesIds) throws CiadtiException {
        configurationMediator.deleteCategories(categoriesIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar los tipos de periodicidad",
            description = "Obtiene o lista los tipos de periodicidad de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador del tipo de periodicidad." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase PeriodicidadEntity)." +
                    "Returns: Objeto o lista de objetos con información de la periodicidad. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"periodicity", "periodicity/{id}"})
    public ResponseEntity<?> getPeriodicity(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(PeriodicidadEntity.class);
        PeriodicidadEntity filter = (PeriodicidadEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, periodicidadService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un tipo de periodicidad",
            description = "Crea un tipo de periodicidad" +
                    "Args: periodicidadEntity: objeto con información del tipo de periodicidad a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/periodicity")
    public ResponseEntity<?> createPeriodicity(@Valid @RequestBody PeriodicidadEntity periodicidadEntity) {
        PeriodicidadEntity periodicidadNew = new PeriodicidadEntity();
        periodicidadNew.setNombre(periodicidadEntity.getNombre().toUpperCase());
        periodicidadNew.setFrecuenciaAnual(periodicidadEntity.getFrecuenciaAnual());
        System.out.println("CREAR: " + periodicidadNew);
        return new ResponseEntity<>(periodicidadService.save(periodicidadNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un tipo de periodicidad",
            description = "Actualiza un tipo de periodicidad." +
                    "Args: periodicidadEntity: objeto con información del tipo de periodicidad." +
                    "id: identificador del tipo de periodicidad." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/periodicity/{id}")
    public ResponseEntity<?> updatePeriodicity(@Valid @RequestBody PeriodicidadEntity periodicidadEntity, @PathVariable Long id) throws CiadtiException {
        PeriodicidadEntity periodicidadDB = periodicidadService.findById(id);
        periodicidadDB.setNombre(periodicidadEntity.getNombre().toUpperCase());
        periodicidadDB.setFrecuenciaAnual(periodicidadEntity.getFrecuenciaAnual());
        return new ResponseEntity<>(periodicidadService.save(periodicidadDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de periodicidad por el id",
            description = "Elimina un tipo de periodicidad por su id." +
                    "Args: id: identificador del tipo de periodicidad a eliminar.")
    @DeleteMapping("/periodicity/{id}")
    public ResponseEntity<?> deletePeriodicity(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deletePeriodicity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar tipos de periodicidad por el id",
            description = "Elimina lista de tipos de periodicidad por su id." +
                    "Args: documentTypeIds: identificadores de los tipos de periodicidad a eliminar.")
    @DeleteMapping("/periodicity")
    public ResponseEntity<?> deletePeriodicities(@RequestBody List<Long> periodicitiesIds) throws CiadtiException {
        configurationMediator.deletePeriodicities(periodicitiesIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
