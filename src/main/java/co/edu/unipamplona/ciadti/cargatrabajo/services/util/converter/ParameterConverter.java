package co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
@Service
public class ParameterConverter {
    private Map<String, String[]>  parameters;
    private Class<?> clazz;
    private String delimiter;
    private final String DELIMITER_DEFAULT = ",";
    private final String REGEX_OF_SEPARATOR_OF_ATTRIBUTE = "\\.";
    private final String SEPARATOR_OF_ATTRIBUTE = ".";

    @Autowired
    @Qualifier("mvcConversionService")
    private ConversionService conversionService;

    public Object converter(Map<String, String[]> parameters,  Class<?> clazz) throws CiadtiException {
        this.parameters = parameters;
        this.clazz = clazz;
        this.delimiter = this.DELIMITER_DEFAULT;
        return this.converterToObject();
    }

    public Object converter(Map<String, String[]> parameters, Class<?> clazz, String delimiter) throws CiadtiException {
        this.parameters = parameters;
        this.clazz = clazz;
        this.delimiter = delimiter;
        return this.converterToObject();
    }

    private Object converterToObject() throws CiadtiException {
        Object instanceClass;
        try {
            Class<?> classVO = this.clazz;
            instanceClass = classVO.newInstance();
            for (Map.Entry<String, String[]> entry : this.parameters.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();
                String valueString = String.join(delimiter, values);
                if (name.contains(SEPARATOR_OF_ATTRIBUTE)){
                    this.addObjectToObject(classVO, instanceClass, name, valueString);
                }else {
                    this.addAttributeToObject(classVO, instanceClass, name, valueString);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
        return instanceClass;
    }

    private Object addAttributeToObject(Class<?> classVO, Object instanceClass, String nameField, String valueField) throws CiadtiException {
        Field field;
        try {
            field = getDeclaredFieldIgnoreCase(classVO, nameField.substring(0, nameField.length()));
            if (field != null) {
                field.setAccessible(true);
                field.set(instanceClass, convertStringToType(valueField, field.getType()) );
            }
        } catch (SecurityException | IllegalAccessException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
        return classVO;
    }

    private void addObjectToObject(Class<?> classVO,  Object rootInstance, String nameParameter,  String value) throws CiadtiException {
        Field field;
        Class<?> classTemp = classVO;
        boolean isValid = true;
        try {
            String[] attrs = nameParameter.split(REGEX_OF_SEPARATOR_OF_ATTRIBUTE);
            for (String attr : attrs) {
                field = getDeclaredFieldIgnoreCase(classTemp, attr);
                if (field != null) {
                    field.setAccessible(true);
                    classTemp = field.getType();
                } else {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                Object actualObject = rootInstance;
                for (int i = 0; i < attrs.length - 1; i++) {
                    field = actualObject.getClass().getDeclaredField(attrs[i]);
                    field.setAccessible(true);
                    Object attributeValue = field.get(actualObject);
                    if (attributeValue == null) {
                        Class<?> fieldType = field.getType();
                        attributeValue = fieldType.getDeclaredConstructor().newInstance();
                        field.set(actualObject, attributeValue);
                    }
                    actualObject = attributeValue;
                }
                Field lastField = actualObject.getClass().getDeclaredField(attrs[attrs.length - 1]);
                lastField.setAccessible(true);
                lastField.set(actualObject, this.convertStringToType(value, lastField.getType()));
            }
        } catch (RuntimeException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException |
                 InstantiationException | InvocationTargetException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
    }

    private Field getDeclaredFieldIgnoreCase(Class<?> clazz, String fieldName) {
        Field field = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase(fieldName)) {
                field = f;
                break;
            }
        }
        return field;
    }

    private <T> T convertStringToType(String str, Class<T> targetType) {
        return conversionService.convert(str, targetType);
    }
}
