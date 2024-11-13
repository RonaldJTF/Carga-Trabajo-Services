package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CargoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.AppointmentReportExcelJXLS;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final CargoService cargoService;
    private final ConfigurationMediator configurationMediator;
    private final AppointmentReportExcelJXLS appointmentReportExcelJXLS;

    @Operation(
        summary = "Obtener o listar los cargos de acuerdo a una lista de Ids de dependencias, Ids Vigencias, etc",
        description = "Obtiene o lista los  cargos de acuerdo a una lista de Ids de dependencias, Ids Vigencias, etc. " +
                "Args: id: identificador del cargo. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CargoEntity). " +
                "Returns: Objeto o lista de objetos con información del cargo. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping
    public ResponseEntity<?> get(HttpServletRequest request) throws CiadtiException {
        Map<String, Long[]> filters = Methods.convertParameterMap(request.getParameterMap());
        List<CargoEntity> result = configurationMediator.findAppointments(filters);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    

    @Operation(
        summary = "Obtener informacion de un cargo por su id",
        description = "Obtiene la información de una asignación laboral" +
                "Args: id: identificador de la carga. " +
                "Returns: Objeto con información de la carga laboral. ")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointment(@PathVariable Long id, HttpServletRequest request) throws CiadtiException {
        CargoEntity result = cargoService.findByAppointmentId(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Crear un tipo de cargo",
            description = "Crea un tipo de cargo" +
                    "Args: categoriaEntity: objeto con información del tipo de cargo a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CargoEntity cargoEntity) {
        return new ResponseEntity<>(cargoService.save(cargoEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar un cargo",
        description = "Actualiza un cargo. " + 
            "Args: cargoEntity: objeto con información del cargo. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestBody CargoEntity cargoEntity, @PathVariable Long id) throws CiadtiException{
        CargoEntity cargoEntityBD = cargoService.findById(id);
        cargoEntityBD.setAsignacionBasica(cargoEntity.getAsignacionBasica());
        cargoEntityBD.setTotalCargos(cargoEntity.getTotalCargos());
        cargoEntityBD.setIdEstructura(cargoEntity.getIdEstructura());
        cargoEntityBD.setIdVigencia(cargoEntity.getIdVigencia());
        cargoEntityBD.setIdNivel(cargoEntity.getIdNivel());
        cargoEntityBD.setIdEscalaSalarial(cargoEntity.getIdEscalaSalarial());
        cargoEntityBD.setIdNormatividad(cargoEntity.getIdNormatividad());
        cargoEntityBD.setIdAlcance(cargoEntity.getIdAlcance());
        return new ResponseEntity<>(cargoService.save(cargoEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar un cargo",
        description = "Elimina un cargo" + 
            "Args: id: identificador del cargo a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete (@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteAppointment(id);
        configurationMediator.deleteAppointment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Eliminar varios cargos por su id",
        description = "Elimina varios cargos por su id" + 
            "Args: appointmentIds: lista de id de los cargos a eliminar. ")
    @DeleteMapping
    public ResponseEntity<?> deleteAppointments (@RequestBody List<Long> appointmentIds) throws CiadtiException{
        configurationMediator.deleteAppointments(appointmentIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SuppressWarnings("null")
    @GetMapping("/report")
    public ResponseEntity<?> downloadReportExcel(@RequestParam(name = "type", required = false) String type, HttpServletRequest request) throws Exception {
        Map<String, Long[]> filters = Methods.convertParameterMap(request.getParameterMap());
       
        byte[] fileBytes = null;
        String extension = null;
        String mediaType = null;

        if(type == null || "EXCEL".equals(type.toUpperCase())){
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = appointmentReportExcelJXLS.generate(filters);
        }

        DateFormat dateFormatter = new SimpleDateFormat(          "yyyy-MM-dd:hh:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String fileName = String.format("reporte_%s.%s", currentDateTime, extension);

        if (fileBytes != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Type");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(mediaType))
                    .body(fileBytes);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }    
}
