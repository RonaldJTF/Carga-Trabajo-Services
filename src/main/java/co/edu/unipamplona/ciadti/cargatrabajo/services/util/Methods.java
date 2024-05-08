package co.edu.unipamplona.ciadti.cargatrabajo.services.util;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.mimeTypes.MimeTypesConfig;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Time;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class Methods {
    public Methods() {
    }

    /**
     * Retorna un objeto Pageable de cuerdo al número de la página (pageNo), el tamaño de la página (pageSize),
     * el campo por el cual se va a ordenar (sortField) y la dirección de ordenamiento (sortDirection).
     * Nota: El sentido del orden puede ser, por ejemplo, ASC o DESC. El sortField es estrictamente un atributo
     * de la clase del objeto a ordenar.
     */
    public static Pageable pageable(Integer pageNo, Integer pageSize, String sortField, String sortDirection) {
        int page = pageNo - 1;
        Sort sort = Sort.Direction.ASC.name().equalsIgnoreCase(sortDirection) ? Sort.by(new String[]{sortField}).ascending() : Sort.by(new String[]{sortField}).descending();
        if (page < 0) {
            page = 0;
        }
        byte size;
        if (pageSize <= 10) {
            size = 10;
        } else if (pageSize <= 15) {
            size = 15;
        } else if (pageSize <= 20) {
            size = 20;
        } else {
            size = 30;
        }
        return PageRequest.of(page, size, sort);
    }

    /**
     * Retorna un objeto Pageable de cuerdo al número de la página (pageNo), el tamaño de la página (pageSize) y una
     * cadena de texto que tiene la siguiente estructura: "nombreDelCampo1:diceccionOrdenamiento,nombreDelCampo2:diceccionOrdenamiento,..."
     * por ejemplo, "nombre:asc,apellidos:asc". El nombreDelCampo es un atributo del objeto a ordenar, y diceccionOrdenamiento
     * el sentido del orden, por ejemplo, ASC o DESC.
     */
    public static Pageable pageable(Integer pageNo, Integer pageSize, String order) {
        int page = pageNo - 1;
        if (page < 0) {
            page = 0;
        }
        byte size;
        if (pageSize <= 10) {
            size = 10;
        } else if (pageSize <= 15) {
            size = 15;
        } else if (pageSize <= 20) {
            size = 20;
        } else {
            size = 30;
        }

        String[] orderFields = order.split(",");
        List<Sort.Order> sorts = new ArrayList<>();

        for (String orderField : orderFields) {
            String[] orderArr = orderField.split(":");
            String field = orderArr[0];
            String direction = orderArr.length > 1 ? orderArr[1] : "asc";
            sorts.add(new Sort.Order(Sort.Direction.fromString(direction), field));
        }
        return PageRequest.of(page, size, Sort.by(sorts));
    }

    public static ResponseEntity<?> handleRapException(CiadtiException e) {
        if (404 == e.getCode()) {
            return new ResponseEntity(new HashMap(), HttpStatus.NOT_FOUND);
        } else {
            Map<String, Object> responseError = new HashMap();
            responseError.put("isException", true);
            responseError.put("code", e.getCode());
            responseError.put("message", e.getMessage());
            return null == e.getCode() ? new ResponseEntity(responseError, HttpStatus.INTERNAL_SERVER_ERROR) : new ResponseEntity(responseError, (HttpStatus) Objects.requireNonNull(HttpStatus.resolve(e.getCode())));
        }
    }

    public static ResponseEntity<?> handleResponseError(String error, String message) {
        Map<String, String> responseError = new HashMap();
        responseError.put("error", error);
        responseError.put("message", message.concat(" O servicio temporalmente no disponible."));
        return new ResponseEntity(responseError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

   
    /**
     * Obtiene la fecha formateada de un String pasado como parámetro.
     * */
    public static Date getFormattedDate(String dateIn) throws ParseException {
        SimpleDateFormat formato = null;
        Date fecha = null;

        if (dateIn.contains("/")){
            formato = new SimpleDateFormat("dd/MM/yyyy");
        }else{
            formato = new SimpleDateFormat("dd-MM-yyyy");
        }

        if (formato != null) {
            fecha = formato.parse(dateIn);
        }
        return fecha;
    }

    /**
     * Obtiene el tipo de contenido de un archivo que viene definido con su tipo,
     * por ejemplo, miArchivo.pdf para el cual debería retornar algo como "application/pdf"
     * */
    public static MediaType getContentType(String filename){
        MimeMappings mimeMappings = MimeTypesConfig.mimeMappings();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        String contentType = mimeMappings.get(extension);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Tipo de contenido genérico
        }
        return MediaType.parseMediaType(contentType);
    }

    /**
     * Obtiene el nombre de un archivo contenido en el path pasado como un string,
     * por ejemplo, folder1/folder2/miArchivo.pdf para el cual debería retornar algo como "miArchivo.pdf"
     * */
    public static String getFilenameFromPath(String path){
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {return path;}
        return path.substring(lastSlashIndex + 1);
    }

    /**
     * Obtiene el nombre de un archivo contenido en el path pasado como un string,
     * por ejemplo, folder1/folder2/miArchivo.pdf para el cual debería retornar algo como "miArchivo.pdf"
     * */
    public static String getExtensionFromFileName(String fileName){
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * Genera de manera aleatoria una cadena de texto de una longitud dada (esto es considerada como una contraseña)
    * */
    public static String generatePassword(int size) throws CiadtiException{
        Objects.requireNonNull( size, "size is null" );
        String alphabet = "0123456789abcdefghijklmnopqrstuvwyzABCDEFGHIJKLMNOPQRSTUVWXYZ*+-_#";
        StringBuilder code = new StringBuilder();
        String result = null;

        if ( size < 0 ) {
            throw new IllegalArgumentException( "El tamaño no debe ser negativo" );
        }
        try
        {
            SecureRandom secureRandom = SecureRandom.getInstance( "SHA1PRNG" );
            for ( int j = 0; j < size; j++ ) {
                code.append( alphabet.charAt( secureRandom.nextInt( alphabet.length() ) ) );
            }
            result = code.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new CiadtiException(e.getMessage());
        }
        return result;
    }

    /**
     * Permite obtener el nombre del método de donde se hace el llamado de este método,
     * junto a los parámetros y sus tipos que este tiene.
    */
    public static String getCurrentMethodName(Class<?> clazz) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            StackTraceElement element = stackTrace[2];
            String methodName = element.getMethodName();
            String methodParameters = getParameterNames(clazz, methodName);
            return methodName.concat(" (").concat(methodParameters).concat(") ");
        }
        return "Unknown";
    }

    /**
     * Permite obtener los parámetros de un método en una determinada clase.
     */
    public static String getParameterNames(Class<?> clazz, String methodName) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Parameter[] parameters = method.getParameters();
                    StringBuilder parameterNames = new StringBuilder();
                    for (Parameter parameter : parameters) {
                        parameterNames.append(parameter.getType().getSimpleName() + " " + parameter.getName()).append(", ");
                    }
                    if (parameterNames.length() > 0) {
                        parameterNames.setLength(parameterNames.length() - 2); // Eliminar la última coma y espacio
                    }
                    return parameterNames.toString();
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }


    /**
     * Asigna un valor a un atributo de un objeto si este es nulo.
     * @param target Objeto al que se le asignará el valor y tiene que tener un atributo con el mismo nombre attributeName.
     * @param attributeName tiene que ser el nombre del atributo.
     * @param value es el valor que se le asignará al atributo del objeto target.
     * Nota: 1. El tipo de dato del atributo tiene que coincidir con el tipo de dato del parámetro value.
     * 2. Si el atributo es nulo, se asigna el valor al atributo, de lo contrario no se hace nada.
     * 3. Si el parámetro attributeName no existe en el objeto target, no se hace nada.
     */
    public static <T> void assignIfNull(T target, String attributeName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(attributeName);
            field.setAccessible(true);
            Object fieldValue = field.get(target);
            if (fieldValue == null) {
                field.set(target, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Trace.logError(Methods.class.getName(), Methods.getCurrentMethodName(Methods.class), e);
        }
    }

    public static <T> T assignIfNull(T field, T value) {
        if (field == null) {
            return value;
        }else {
            return field;
        }
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    /**
     * Calcula la duracion entre dos fechas. Si una de las dos fechas es null, retorna -1L, de lo contrario,
     * realiza la operacián.
     */
    public static long calculateDuration(Date initDate, Date endDate, Time time) {
        if (initDate == null || endDate == null){
            return -1L;
        }
        return (initDate.getTime() - endDate.getTime())/ time.getValue();
    }

    /**
     * Convierte un String a booleano
     * */
    public static boolean convertToBoolean(String s) {
        s = s.toUpperCase();
        if (s.equals("0") || s.equals("NO") || s.equals("FALSE") || s.isBlank()){
            return false;
        }
        return true;
    }

    /**
     * Convierte un String a año, removiendo los caracteres que no sean números, por ejemplo, los puntos, espacios y comas.
     */
    public static Long convertToYear(String s) {
        Objects.requireNonNull(s, "No se admite un valor null");
        s = s.replaceAll("[.,\\s]", "");
        return Long.parseLong(s);
    }

    /**
     * Convierte un String a documento, removiendo los caracteres que no sean números, por ejemplo, los puntos, espacios y comas.
     */
    public static String convertToDocument(String s) {
        Objects.requireNonNull(s, "No se admite un valor null");
        s = s.replaceAll("[.,'\\s]", "");
        return s;
    }

    /**
     * Genera un valor único a partir de un String.
     */
    public static String generateUniqueValue(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static ResponseEntity getResponseAccordingToId(Long id, Object objeto){
        if (id == null) {
            if (((List<?>) objeto).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            if (((List<?>) objeto).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            objeto = ((List<?>) objeto).get(0);
        }
        return new ResponseEntity<>(objeto, HttpStatus.OK);
    }

    public static int[] getImageDimensions(byte[] imageBytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            ImageInputStream iis = ImageIO.createImageInputStream(bis);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    int width = reader.getWidth(0); 
                    int height = reader.getHeight(0);
                    return new int[]{width, height};
                } finally {
                    reader.dispose();
                    iis.close();
                    bis.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[]{100, 100}; 
    }
}
