package co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.deserializer;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateDeserializer extends StdDeserializer<Date> {
    private static SimpleDateFormat formatter;
    public DateDeserializer() {
        this(null);
    }
    public DateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(com.fasterxml.jackson.core.JsonParser jsonParser, DeserializationContext deserializationContext)
            throws java.io.IOException {
        String date = jsonParser.getText();
        String format;
        try {
            if (this.isMatcher(date, Format.DATE)){
                format = Format.DATE.value;
            }else if (this.isMatcher(date, Format.DATETIME)){
                format = Format.DATETIME.value;
            }else if (this.isMatcher(date, Format.TIME)){
                format = Format.TIME.value;
            }else if (this.isMatcher(date, Format.FULLTIME)){
                format = Format.FULLTIME.value;
            }else{
                format = Format.DEFAULT.value;
            }
            formatter = new SimpleDateFormat(format);
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMatcher(String input, Format format) {
        DateTimeFormatter formatter;
        try {
            switch (format) {
                case DATE:
                    formatter = DateTimeFormatter.ofPattern(format.value);
                    LocalDate.parse(input, formatter);
                    return true;
                case DATETIME:
                    formatter = DateTimeFormatter.ofPattern(format.value);
                    LocalDateTime.parse(input, formatter);
                    return true;
                case TIME:
                    formatter = DateTimeFormatter.ofPattern(format.value);
                    LocalTime.parse(input, formatter);
                    return true;
                case FULLTIME:
                    formatter = DateTimeFormatter.ofPattern(format.value);
                    LocalTime.parse(input, formatter);
                    return true;
                default:
                    return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Getter
    public enum Format {
        DATE ("yyyy-MM-dd", "Solo fecha. Ej: 2023-07-24"),
        DATETIME ("yyyy-MM-dd HH:mm:ss", "Fecha y hora. Ej: 2023-07-24 15:30:00"),
        TIME ("HH:mm:ss", "Solo hora. Ej: 15:30:00"),
        FULLTIME ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "Formato completo con zona horaria. Ej: 2023-07-24T15:30:00.000Z"),
        DEFAULT ("yyyy-MM-dd'T'HH:mm:ssZ", "Formato completo con zona horaria. Ej: 2023-07-24T15:30:00+0530");

        private final String value;
        private final String description;

        Format(String value, String description){
            this.value = value;
            this.description = description;
        }
    }
}