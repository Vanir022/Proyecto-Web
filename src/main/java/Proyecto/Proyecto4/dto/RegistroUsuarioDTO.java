package Proyecto.Proyecto4.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RegistroUsuarioDTO {
    
    // Datos básicos del usuario
    private String email;
    private String password;
    private String confirmPassword;
    
    // Datos personales
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    
    // Intereses (checkbox)
    private List<String> interests;
    
    // Preferencias
    private Boolean acceptTerms;
    private Boolean acceptMarketing;

    // Constructor por defecto
    public RegistroUsuarioDTO() {}

    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        return firstName + " " + lastName;
    }

    // Método helper para convertir intereses a string
    public String getInteresesAsString() {
        if (interests == null || interests.isEmpty()) {
            return "";
        }
        return String.join(",", interests);
    }
}