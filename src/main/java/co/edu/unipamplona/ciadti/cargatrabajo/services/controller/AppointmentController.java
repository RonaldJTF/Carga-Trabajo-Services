package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CargoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.GeneralExpressionMediator;
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
    private final VariableService variableService;
    private final ConfigurationMediator configurationMediator;
    private final GeneralExpressionMediator generalExpressionMediator;

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
        return new ResponseEntity<>(cargoService.findAllBy(filters), HttpStatus.OK);
    }
    

    @Operation(
        summary = "Obtener informacion de un cargo por su id",
        description = "Obtiene la información de una asignación laboral" +
                "Args: id: identificador de la carga. " +
                "Returns: Objeto con información de la carga laboral. ")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointment(@PathVariable Long id, HttpServletRequest request) throws CiadtiException {
        CargoEntity result = cargoService.findByAppointmentId(id);
        result.setAsignacionBasica(generalExpressionMediator.getValueOfVariable(34L, 1L, variableService.findAll()));
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
}
