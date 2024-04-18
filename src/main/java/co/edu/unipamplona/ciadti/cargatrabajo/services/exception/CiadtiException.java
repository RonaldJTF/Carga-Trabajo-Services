package co.edu.unipamplona.ciadti.cargatrabajo.services.exception;

public class CiadtiException extends Exception{
    private Integer code = 500;

    public CiadtiException(String message) {
        super(message);
    }

    public CiadtiException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
