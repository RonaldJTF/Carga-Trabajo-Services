package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;

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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
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


    @Operation(
        summary = "Obtener o listar los cargos",
        description = "Obtiene o lista los cargos de acuerdo a ciertas variables o parámetros. " +
                "Args: id: identificador del cargo. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CargoEntity). " +
                "Returns: Objeto o lista de objetos con información del cargo. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(CargoEntity.class);
        CargoEntity filter = (CargoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, cargoService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un tipo de cargo",
            description = "Crea un tipo de cargo" +
                    "Args: categoriaEntity: objeto con información del tipo de cargo a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CargoEntity cargoEntity) {
        CargoEntity cargoNew = new CargoEntity();
        cargoNew.setAsignacionBasica(cargoEntity.getAsignacionBasica());
        cargoNew.setTotalCargo(cargoEntity.getTotalCargo());
        return new ResponseEntity<>(cargoService.save(cargoNew), HttpStatus.CREATED);
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
        cargoEntityBD.setTotalCargo(cargoEntity.getTotalCargo());
        return new ResponseEntity<>(cargoService.save(cargoEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar un cargo",
        description = "Elimina un cargo" + 
            "Args: id: identificador del cargo a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete (@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteAppointment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar posiciones por el id",
            description = "Elimina lista de posiciones por su id." +
                    "Args: appointmentIds: identificadores de los tipos de posiciones a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteAppointments(@RequestBody List<Long> appointmentIds) throws CiadtiException {
        configurationMediator.deleteAppointments(appointmentIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
