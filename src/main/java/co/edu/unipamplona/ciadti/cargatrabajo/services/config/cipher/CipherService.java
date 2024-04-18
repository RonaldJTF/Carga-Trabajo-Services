package co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CipherService {
    /**
     * Descifra una cadena de caracteres con una estructura definida.
     * input = key + iv + salt + cipherText
     */
    public String decryptCredential(String input)
    {
        Objects.requireNonNull(input, "cipher is null");
        String passphrase = null;
        String iv = null;
        String salt = null;
        String cipherText = null;
        AES aes = new AES(128, 1000);
        String decryptedInput =  new String(java.util.Base64.getDecoder().decode(input));
        if (decryptedInput.split("\\|").length == 4) {
            passphrase = decryptedInput.split("\\|")[0];
            iv = decryptedInput.split("\\|")[1];
            salt = decryptedInput.split("\\|")[2];
            cipherText = decryptedInput.split("\\|")[3];
        }else{
            return input;
        }
        return aes.decrypt(salt, iv, passphrase, cipherText);
    }
}