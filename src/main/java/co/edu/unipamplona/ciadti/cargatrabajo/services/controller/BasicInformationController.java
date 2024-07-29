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


    @Operation(
            summary = "Obtener o listar los tipos de documentos",
            description = "Obtiene o lista los tipos de documentos de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador del tipo de documento. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoDocumnetoEntity). " +
                    "Returns: Objeto o lista de objetos con información de la persona. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"document-type", "document-type/{id}"})
    public ResponseEntity<?> getDocumentType(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(TipoDocumentoEntity.class);
        TipoDocumentoEntity filter = (TipoDocumentoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoDocumentoService.findAllFilteredBy(filter));
    }


    @GetMapping(value = {"gender", "gender/{id}"})
    public ResponseEntity<?> getGender(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(GeneroEntity.class);
        GeneroEntity filter = (GeneroEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, generoService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"level", "level/{id}"})
    public ResponseEntity<?> getLevel(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(NivelEntity.class);
        NivelEntity filter = (NivelEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, nivelService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"role", "role/{id}"})
    public ResponseEntity<?> getRole(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(RolEntity.class);
        RolEntity filter = (RolEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, rolService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"inventory", "inventory/{id}"})
    public ResponseEntity<?> getInventory(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        return Methods.getResponseAccordingToId(id, tipologiaService.findInventarioTipologia());
    }

    @GetMapping(value = {"statistics", "statistics/{id}"})
    public ResponseEntity<?> getStatistics(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(EstructuraEntity.class);
        EstructuraEntity filter = (EstructuraEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return new ResponseEntity<>(estructuraService.statisticsDependence(filter), HttpStatus.OK);
    }


    //LPR: 18 de julio de 2024

    @GetMapping(value = {"typology", "typology/{id}"})
    public ResponseEntity<?> getTypology(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(TipologiaEntity.class);
        TipologiaEntity filter = (TipologiaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipologiaService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"ftp", "ftp/{id}"})
    public ResponseEntity<?> getFtp(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(FtpEntity.class);
        FtpEntity filter = (FtpEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, ftpService.findAllFilteredBy(filter));
    }

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

    @PutMapping("/role/{id}")
    public ResponseEntity<?> updateRole(@Valid @RequestBody RolEntity rolEntity, @PathVariable Long id) throws CiadtiException {
        RolEntity rolDB = rolService.findById(id);
        rolDB.setNombre(Methods.capitalizeFirstLetter(rolEntity.getNombre()));
        rolDB.setCodigo(rolEntity.getCodigo().toUpperCase());
        return new ResponseEntity<>(rolService.save(rolDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/role/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteRole(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/role")
    public ResponseEntity<?> deleteRoles(@RequestBody List<Long> roleIds) throws CiadtiException {
        configurationMediator.deleteRoles(roleIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear un género",
            description = "Crea un género" +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/gender")
    public ResponseEntity<?> createGender(@Valid @RequestBody GeneroEntity generoEntity) {
        GeneroEntity genderNew = new GeneroEntity();
        genderNew.setNombre(Methods.capitalizeFirstLetter(generoEntity.getNombre()));
        return new ResponseEntity<>(generoService.save(genderNew), HttpStatus.CREATED);
    }

    @PutMapping("/gender/{id}")
    public ResponseEntity<?> updateGender(@Valid @RequestBody GeneroEntity generoEntity, @PathVariable Long id) throws CiadtiException {
        GeneroEntity generoDB = generoService.findById(id);
        generoDB.setNombre(Methods.capitalizeFirstLetter(generoEntity.getNombre()));
        return new ResponseEntity<>(generoService.save(generoDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/gender/{id}")
    public ResponseEntity<?> deleteGender(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteGender(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/gender")
    public ResponseEntity<?> deleteGenders(@RequestBody List<Long> genderIds) throws CiadtiException {
        configurationMediator.deleteGenders(genderIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear un nivel ocupacional",
            description = "Crea un nivel ocupacional" +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/level")
    public ResponseEntity<?> createLevel(@Valid @RequestBody NivelEntity nivelEntity) {
        NivelEntity nivelNew = new NivelEntity();
        nivelNew.setDescripcion(Methods.capitalizeFirstLetter(nivelEntity.getDescripcion()));
        return new ResponseEntity<>(nivelService.save(nivelNew), HttpStatus.CREATED);
    }

    @PutMapping("/level/{id}")
    public ResponseEntity<?> updateLevel(@Valid @RequestBody NivelEntity nivelEntity, @PathVariable Long id) throws CiadtiException {
        NivelEntity nivelDB = nivelService.findById(id);
        nivelDB.setDescripcion(Methods.capitalizeFirstLetter(nivelEntity.getDescripcion()));
        return new ResponseEntity<>(nivelService.save(nivelDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/level/{id}")
    public ResponseEntity<?> deleteLevel(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteLevel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/level")
    public ResponseEntity<?> deleteLevels(@RequestBody List<Long> levelIds) throws CiadtiException {
        configurationMediator.deleteLevels(levelIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear un tipo de documento",
            description = "Crea un tipo de documento" +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/document-type")
    public ResponseEntity<?> createDocumentType(@Valid @RequestBody TipoDocumentoEntity tipoDocumentoEntity){
        TipoDocumentoEntity tipoDocumentoNew = new TipoDocumentoEntity();
        tipoDocumentoNew.setDescripcion(Methods.capitalizeFirstLetter(tipoDocumentoEntity.getDescripcion()));
        tipoDocumentoNew.setAbreviatura(tipoDocumentoEntity.getAbreviatura().toUpperCase());
        return new ResponseEntity<>(tipoDocumentoService.save(tipoDocumentoNew), HttpStatus.CREATED);
    }

    @PutMapping("/document-type/{id}")
    public ResponseEntity<?> updateDocumentType(@Valid @RequestBody TipoDocumentoEntity tipoDocumentoEntity, @PathVariable Long id) throws CiadtiException {
        TipoDocumentoEntity tipoDocumentoDB = tipoDocumentoService.findById(id);
        tipoDocumentoDB.setDescripcion(Methods.capitalizeFirstLetter(tipoDocumentoEntity.getDescripcion()));
        tipoDocumentoDB.setAbreviatura(tipoDocumentoEntity.getAbreviatura().toUpperCase());
        return new ResponseEntity<>(tipoDocumentoService.save(tipoDocumentoDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/document-type/{id}")
    public ResponseEntity<?> deleteDocumentType(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteDocumentType(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/document-type")
    public ResponseEntity<?> deleteDocumentTypes(@RequestBody List<Long> documentTypeIds) throws CiadtiException {
        configurationMediator.deleteDocumentTypes(documentTypeIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear una tipologia",
            description = "Crea una tipologia" +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/typology")
    public ResponseEntity<?> createTypology(@Valid @RequestBody TipologiaEntity tipologiaEntity){
        TipologiaEntity tipologiaNew = new TipologiaEntity();
        tipologiaNew.setNombre(tipologiaEntity.getNombre());
        tipologiaNew.setClaseIcono(tipologiaEntity.getClaseIcono());
        tipologiaNew.setNombreColor(tipologiaEntity.getNombreColor());
        tipologiaNew.setEsDependencia(tipologiaEntity.getEsDependencia());
        tipologiaNew.setIdTipologiaSiguiente(tipologiaEntity.getIdTipologiaSiguiente());
        return new ResponseEntity<>(tipologiaService.save(tipologiaNew), HttpStatus.CREATED);
    }

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

    @DeleteMapping("/typology/{id}")
    public ResponseEntity<?> deleteTypology(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteTypology(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/typology")
    public ResponseEntity<?> deleteTypologies(@RequestBody List<Long> typologyIds) throws CiadtiException {
        configurationMediator.deleteTypoligies(typologyIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear un servidor FTP",
            description = "Crea un servior FTP." +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/ftp")
    public ResponseEntity<?> createFtp(@Valid @RequestBody FtpEntity ftpEntity){
        FtpEntity ftpNew = new FtpEntity();
        ftpNew.setNombre(ftpEntity.getNombre());
        ftpNew.setDescripcion(ftpEntity.getDescripcion());
        ftpNew.setCodigo(ftpEntity.getCodigo());
        ftpNew.setActivo(ftpEntity.getActivo());
        return new ResponseEntity<>(ftpService.save(ftpNew), HttpStatus.CREATED);
    }

    @PutMapping("/ftp/{id}")
    public ResponseEntity<?> updateFtp(@Valid @RequestBody FtpEntity ftpEntity, @PathVariable Long id) throws CiadtiException {
        FtpEntity ftpDB = ftpService.findById(id);
        ftpDB.setNombre(ftpEntity.getNombre());
        ftpDB.setDescripcion(ftpEntity.getDescripcion());
        ftpDB.setCodigo(ftpEntity.getCodigo());
        ftpDB.setActivo(ftpEntity.getActivo());
        return new ResponseEntity<>(ftpService.save(ftpDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/ftp/{id}")
    public ResponseEntity<?> deleteFtp(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteFtp(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/ftp")
    public ResponseEntity<?> deleteFtps(@RequestBody List<Long> ftpIds) throws CiadtiException {
        configurationMediator.deleteFtps(ftpIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
            summary = "Crear un servidor FTP",
            description = "Crea un servior FTP." +
                    "Args: generoEntity: objeto con información del genero a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/action")
    public ResponseEntity<?> createAction(@Valid @RequestBody AccionEntity accionEntity){
        AccionEntity accionNew = new AccionEntity();
        accionNew.setNombre(accionEntity.getNombre());
        accionNew.setClaseIcono(accionEntity.getClaseIcono());
        accionNew.setClaseEstado(accionEntity.getClaseEstado());
        accionNew.setPath(accionEntity.getPath());
        return new ResponseEntity<>(accionService.save(accionNew), HttpStatus.CREATED);
    }

    @PutMapping("/action/{id}")
    public ResponseEntity<?> updateAction(@Valid @RequestBody AccionEntity accionEntity, @PathVariable Long id) throws CiadtiException {
        AccionEntity accionDB = accionService.findById(id);
        accionDB.setNombre(accionEntity.getNombre());
        accionDB.setClaseIcono(accionEntity.getClaseIcono());
        accionDB.setClaseEstado(accionEntity.getClaseEstado());
        accionDB.setPath(accionEntity.getPath());
        return new ResponseEntity<>(accionService.save(accionDB), HttpStatus.CREATED);
    }

    @DeleteMapping("/action/{id}")
    public ResponseEntity<?> deleteAction(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteAction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/action")
    public ResponseEntity<?> deleteActions(@RequestBody List<Long> acctionIds) throws CiadtiException {
        configurationMediator.deleteActions(acctionIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
