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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CategoriaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/compensation-category")
public class CompensationCategoryController {
    private final ConfigurationMediator configurationMediator;
    private final CategoriaService categoriaService;

    @Operation(
            summary = "Obtener o listar las categorías de una compensación",
            description = "Obtiene o lista las categorías de una compensación de acuerdo a ciertas variables o parámetros." +
                    "Args: id: identificador de la categoría." +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CategoriaEntity)." +
                    "Returns: Objeto o lista de objetos con información de la categoría. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getCategory(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(CategoriaEntity.class);
        CategoriaEntity filter = (CategoriaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        List<CategoriaEntity> result = categoriaService.findAllFilteredBy(filter);
        return Methods.getResponseAccordingToId(id, result);
    }

    @Operation(
            summary = "Crear un tipo de categoría",
            description = "Crea un tipo de categoría" +
                    "Args: categoriaEntity: objeto con información del tipo de categoría a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
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
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoriaEntity categoriaEntity, @PathVariable Long id) throws CiadtiException {
        CategoriaEntity categoriaDB = categoriaService.findById(id);
        categoriaDB.setDescripcion(categoriaEntity.getDescripcion());
        categoriaDB.setNombre(categoriaEntity.getNombre());
        return new ResponseEntity<>(categoriaService.save(categoriaDB), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar tipo de categoría por el id",
            description = "Elimina un tipo de categoría por su id." +
                    "Args: id: identificador del tipo de categoría a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } 
}
