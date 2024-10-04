package co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Permite convertir una serie de parámetros (atributo 'parameters') en un objeto de la clase definida (atributo 'typeVO').
 * Args:
 * Parameters: Mapa de parámetros con clave y valores.
 * typeVO: Clase del objeto objetivo.
 * Delimiter: usado para separar los valores que tiene una misma clave(parámetro), ya que una misma clave
 * puede tener varios valores.
 * Nota:
 * 1. Si algún parámetro no corresponde como atributo en la clase indicada, entonces no es tenido en cuenta.
 * 2. Si ningún parámetro corresponde como atributo en la clase indicada, entonces se retorna únicamente la instancia
 * de la clase con todos sus campos en null.
 * 3. Si un objeto (Clase X) tiene por atributo a otro objeto (Clase Y), un valor String pasado como valor no puede ser
 * convertido a objeto de esta clase (Clase Y).
 * 4. Si un objeto (Clase X (clase del objeto objetivo definida en typeVO)) tiene por atributo a otro objeto (Clase Y)
 * y este tiene un atributo A, entonces el nombre del parámetro debe de ser: atributoClaseY.atributoA. Siga esta
 * regla para más valores en cascada (atributoClaseY.atributoClaseZ.atributoB...)
 */
@NoArgsConstructor
public class ParameterConverter {
    private Map<String, String[]>  parameters;
    private Class<?> clazz;
    private String delimiter;
    private final String DELIMITER_DEFAULT = ",";
    private final String REGEX_OF_SEPARATOR_OF_ATTRIBUTE = "\\.";
    private final String SEPARATOR_OF_ATTRIBUTE = ".";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParameterConverter (Class<?> clazz){
        this.clazz = clazz;
    }

    public Object converter(Map<String, String[]> parameters) throws CiadtiException {
        this.parameters = parameters;
        this.delimiter = this.DELIMITER_DEFAULT;
        return this.converterToObject();
    }

    public Object converter(Map<String, String[]> parameters, String delimiter) throws CiadtiException {
        this.parameters = parameters;
        this.delimiter = delimiter;
        return this.converterToObject();
    }

   private Object converterToObject() throws CiadtiException {
        Object instanceClass;
        try {
            Class<?> classVO = this.clazz;
            instanceClass = classVO.getDeclaredConstructor().newInstance();

            Map<String, Object> paramMap = new HashMap<>();
            for (Map.Entry<String, String[]> entry : this.parameters.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();
                String valueString = String.join(delimiter, values);

                if (name.contains(SEPARATOR_OF_ATTRIBUTE)) {
                    addNestedParam(paramMap, name.split(REGEX_OF_SEPARATOR_OF_ATTRIBUTE), valueString);
                } else {
                    paramMap.put(name, valueString);
                }
            }

            String jsonString = objectMapper.writeValueAsString(paramMap);
            instanceClass = objectMapper.readValue(jsonString, classVO);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | JsonProcessingException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
        return instanceClass;
    }

    @SuppressWarnings("unchecked")
    private void addNestedParam(Map<String, Object> map, String[] keys, String value) {
        Map<String, Object> currentMap = map;
        for (int i = 0; i < keys.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.computeIfAbsent(keys[i], k -> new HashMap<>());
        }
        currentMap.put(keys[keys.length - 1], value);
    }
}
