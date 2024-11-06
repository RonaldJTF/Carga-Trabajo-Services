package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher.CipherService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CategoriaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLaboralService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/compensation")
public class WorksCompensationController {

    private final CipherService cipherService;
    private final CompensacionLaboralService compensacionLaboralService;
    private final ConfigurationMediator configurationMediator;
    private final CategoriaService categoriaService;

    @Operation(
            summary = "Obtener o listar las compensaciones laborales",
            description = "Obtiene o lista las compensaciones laborales de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador del la compensación laboral. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CompensacionLaboralEntity). " +
                    "Returns: Objeto o lista de objetos con información de la compensación laboral. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) String id, HttpServletRequest request) throws CiadtiException {
        Long idCompensation = id != null ? Long.valueOf(cipherService.decryptParam(id)) : null;
        ParameterConverter parameterConverter = new ParameterConverter(CompensacionLaboralEntity.class);
        CompensacionLaboralEntity filter = (CompensacionLaboralEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : idCompensation);
        return Methods.getResponseAccordingToId(idCompensation, compensacionLaboralService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crea una compensación laboral",
            description = "Crea una compensación laboral" +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "Returns: Objeto con la información asociada."
    )
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CompensacionLaboralEntity compensacionLaboralEntity) {
        return new ResponseEntity<>(compensacionLaboralService.save(compensacionLaboralEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una compensación laboral",
            description = "Actualiza una compensación laboral. " +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "id: identificador de la compensación laboral. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody CompensacionLaboralEntity compensacionLaboralEntity, @PathVariable String id) throws CiadtiException {
        Long idCompensation = Long.valueOf(cipherService.decryptParam(id));
        CompensacionLaboralEntity compensacionLaboralDB = compensacionLaboralService.findById(idCompensation);
        if (compensacionLaboralDB != null) {
            compensacionLaboralEntity.setId(compensacionLaboralDB.getId());
        }
        return new ResponseEntity<>(compensacionLaboralService.save(compensacionLaboralEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar compensación laboral por el id",
            description = "Elimina una compensación laboral por su id. " +
                    "Args: id: identificador de la compensación laboral a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) throws CiadtiException {
        configurationMediator.deleteCompensation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCompensations(@RequestBody List<String> personIds) throws CiadtiException {
        configurationMediator.deleteCompensations(personIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar las categorías de una compensación",
            description = "Obtiene o lista las categorías de una compensación de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador de la categoría." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CategoriaEntity)." +
                    "Returns: Objeto o lista de objetos con información de la categoría. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"/category", "/category/{id}"})
    public ResponseEntity<?> getCategory(@PathVariable(required = false) String id, HttpServletRequest request) throws CiadtiException {
        Long idCategory = id != null ? Long.valueOf(cipherService.decryptParam(id)) : null;
        ParameterConverter parameterConverter = new ParameterConverter(CategoriaEntity.class);
        CategoriaEntity filter = (CategoriaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : idCategory);
        List<CategoriaEntity> result = categoriaService.findAllFilteredBy(filter);
        return Methods.getResponseAccordingToParam(id, cipherService.encryptResponse(result));
    }

    @Operation(
            summary = "Crear un tipo de categoría",
            description = "Crea un tipo de categoría" +
                    "Args: categoriaEntity: objeto con información del tipo de categoría a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/category")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoriaEntity categoriaEntity) {
        CategoriaEntity categoriaNew = new CategoriaEntity();
        categoriaNew.setNombre(categoriaEntity.getNombre());
        categoriaNew.setDescripcion(categoriaEntity.getDescripcion());
        return new ResponseEntity<>(categoriaService.save(categoriaNew), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un tipo de categoría",
            description = "Actualiza un tipo de categoría." +
                    "Args: categoriaEntity: objeto con información del tipo de categoría." +
                    "id: identificador del tipo de categoría." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/category/{id}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoriaEntity categoriaEntity, @PathVariable String id) throws CiadtiException {
        Long idCategory = Long.valueOf(cipherService.decryptParam(id));
        CategoriaEntity categoriaDB = categoriaService.findById(idCategory);
        categoriaDB.setDescripcion(categoriaEntity.getDescripcion());
        categoriaDB.setNombre(categoriaEntity.getNombre());
        return new ResponseEntity<>(categoriaService.save(categoriaDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de categoría por el id",
            description = "Elimina un tipo de categoría por su id." +
                    "Args: id: identificador del tipo de categoría a eliminar.")
    @DeleteMapping("/category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) throws CiadtiException {
        configurationMediator.deleteCategory(Long.valueOf(cipherService.decryptParam(id)));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
