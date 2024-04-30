package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AccionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Image;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.Report;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/structure")
public class EstructuraController {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final ActividadService actividadService;
    private final ConfigurationMediator configurationMediator;
    private final ParameterConverter parameterConverter;
    private final Report report;
    @Operation(
        summary = "Obtener o listar las estructuras (Dependencia, Procesos, Procedimientos, Actividad, etc.)",
        description = "Obtiene o lista las estructuras de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la estructura. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase EstructuraEntity). " +
            "Returns: Objeto o lista de objetos con información de la estructura. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get (@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        EstructuraEntity filter = (EstructuraEntity) parameterConverter.converter(request.getParameterMap(), EstructuraEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, estructuraService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una estructura junto a sus subestructuras",
        description = "Crea una estructura junto a sus subestructuras si estas son definidas. " + 
            "Args: estructuraEntity: objeto con información de la estructura. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestParam("structure") String estructuraJSON, 
                                    @RequestParam(value="file", required = false) MultipartFile file) throws IOException, CiadtiException {
        ObjectMapper objectMapper = new ObjectMapper();
        EstructuraEntity estructuraEntity = objectMapper.readValue(estructuraJSON, EstructuraEntity.class);
        TipologiaEntity firstTypology = tipologiaService.findFirstTipology();
        estructuraEntity.setIdTipologia(estructuraEntity.getIdTipologia() == null ? firstTypology.getId() : estructuraEntity.getIdTipologia());
        estructuraEntity.setTipologia(tipologiaService.findById(estructuraEntity.getIdTipologia()));
        if(file != null){
            estructuraEntity.setMimetype(file.getContentType());
            estructuraEntity.setIcono(file.getBytes());
        }
        return new ResponseEntity<>(estructuraService.save(estructuraEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una estructura junto a sus subestructuras",
        description = "Actualiza una estructura junto a sus subestructuras si estas son definidas. " + 
            "Args: estructuraEntity: objeto con información de la estructura. " +
            "id: identificador de la estructura. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestParam("structure") String estructuraJSON, 
                                     @RequestParam(value = "file", required = false) MultipartFile file,
                                     @PathVariable Long id) throws CiadtiException, IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        EstructuraEntity estructuraEntity = objectMapper.readValue(estructuraJSON, EstructuraEntity.class);

        EstructuraEntity estructuraEntityBD = estructuraService.findById(id);
        estructuraEntityBD.setNombre(estructuraEntity.getNombre());
        estructuraEntityBD.setDescripcion(estructuraEntity.getDescripcion());
        if(file != null){
            estructuraEntityBD.setMimetype(file.getContentType());
            estructuraEntityBD.setIcono(file.getBytes());
        }
        return new ResponseEntity<>(estructuraService.save(estructuraEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una estructura por su id",
        description = "Elimina una estructura y sus subestructuras por su id." + 
            "Args: id: identificador de la estructura a eliminar." )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete (@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteStructure(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    

    @DeleteMapping
    public ResponseEntity<?> deleteStructures(@RequestBody List<Long> structureIds) throws CiadtiException {
        configurationMediator.deleteStructures(structureIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar las actividades",
        description = "Obtiene o lista las actividades de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la actividad. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase ActividadEntity). " +
            "Returns: Objeto o lista de objetos con información de la actividad. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/activity", "/activity/{id}"})
    public ResponseEntity<?> getActivity(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ActividadEntity filter = (ActividadEntity) parameterConverter.converter(request.getParameterMap(), ActividadEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, actividadService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una actividad o detalle de la estructura de tipología actividad",
        description = "Crea una actividad o detalle de la estructura de tipología actividad. " + 
            "Args: actividadEntity: objeto con información de la actividad. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/activity")
    public ResponseEntity<?> createActivity(@Valid @RequestBody ActividadEntity actividadEntity ){
        return new ResponseEntity<>(actividadService.save(actividadEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Crear una actividad o detalle de la estructura de tipología actividad",
        description = "Crea una actividad o detalle de la estructura de tipología actividad. " + 
            "Args: actividadEntity: objeto con información de la actividad. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/activity/{id}")
    public ResponseEntity<?> updateActivity(@Valid @RequestBody ActividadEntity actividadEntity, @PathVariable Long id ){
        actividadEntity.setId(id);
        return new ResponseEntity<>(actividadService.save(actividadEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una actividad por su id",
        description = "Elimina una actividad por su id." + 
            "Args: id: identificador de la actividad a eliminar." )
    @DeleteMapping("/activity/{id}")
    public ResponseEntity<?> deleteActivity (@PathVariable Long id) throws CiadtiException{
        actividadService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<?> downloadReportPDF() throws CiadtiException, JRException  {
        String filePath = "reports\\structures\\Structures.jrxml";
        String filePathDependency = "reports\\structures\\Dependency.jrxml";
        Map<String, Object> parameters = new HashMap<String, Object>();

        Map<String, Object> dependencyParameters = new HashMap<String, Object>(); 
    
        List<EstructuraEntity> structures = estructuraService.findAllFilteredBy(EstructuraEntity.builder().descripcion("a").build());
        JRBeanCollectionDataSource structureDataSource = new JRBeanCollectionDataSource(structures);
        parameters.put("structureDataset", structureDataSource);
        
        dependencyParameters.put("dependencyDataset",  new JRBeanCollectionDataSource(structures));//DataSource de lista resumen
        JasperReport dependencyReport = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(filePathDependency));

        parameters.put("dependencyReport", dependencyReport);
        parameters.put("dependencyParameter", dependencyParameters);

        byte[] fileBytes = report.converterToPDF(parameters, null, filePath);

        if (fileBytes != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "reporte.pdf");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Type");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .body(fileBytes);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
