package co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher;

import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class CipherService {
    /**
     * Descifra una cadena de caracteres con una estructura definida.
     * input = key + iv + salt + cipherText
     */
    public String decryptCredential(String input) {
        Objects.requireNonNull(input, "cipher is null");
        String passphrase = null;
        String iv = null;
        String salt = null;
        String cipherText = null;
        AES aes = new AES(128, 1000);
        String decryptedInput = new String(Base64.getDecoder().decode(input));
        if (decryptedInput.split("\\|").length == 4) {
            passphrase = decryptedInput.split("\\|")[0];
            iv = decryptedInput.split("\\|")[1];
            salt = decryptedInput.split("\\|")[2];
            cipherText = decryptedInput.split("\\|")[3];
        } else {
            return input;
        }
        return aes.decrypt(salt, iv, passphrase, cipherText);
    }

    public String decryptParam(String message) {
        AES aes = new AES(128, 1000);
        return aes.decryptParam(new String(Base64.getDecoder().decode(message)));
    }

    public String encryptParam(String message) {
        AES aes = new AES(128, 1000);
        return aes.encryptParam(message);
    }

    public <T> List<String> encryptResponse(List<T> lista) {
        List<String> listaCifrada = new ArrayList<>();
        for (T objeto : lista) {
            listaCifrada.add(encryptObject(objeto));
        }
        return listaCifrada;
    }

    public <T> String encryptResponse(T objeto) {
        return encryptObject(objeto);
    }

    private <T> String encryptObject(T objeto) {
        AES aes = new AES(128, 1000);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String objetoJson = objectMapper.writeValueAsString(objeto);
            return aes.encryptParam(objetoJson);
        } catch (JsonProcessingException e) {
            Trace.logError(e);
            return null;
        }
    }
}