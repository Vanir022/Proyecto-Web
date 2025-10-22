package Proyecto.Proyecto4.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contactos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contacto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String email;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
    @Column(length = 20)
    private String telefono;
    
    @Size(max = 100, message = "El nombre del hotel no puede exceder 100 caracteres")
    @Column(length = 100)
    private String hotel;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 5000, message = "El mensaje debe tener entre 10 y 5000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(length = 20, nullable = false)
    private String estado = "NUEVO"; // NUEVO, LEIDO, RESPONDIDO

    // Relación ManyToOne con Usuario (opcional - si el contacto es de un usuario registrado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarios_id")
    private Usuario usuario;
    
    // Constructor con parámetros principales
    public Contacto(String nombre, String email, String telefono, String hotel, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.hotel = hotel;
        this.mensaje = mensaje;
        this.fechaEnvio = LocalDateTime.now();
        this.estado = "NUEVO";
    }
    
    @Override
    public String toString() {
        return "Contacto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", hotel='" + hotel + '\'' +
                ", fechaEnvio=" + fechaEnvio +
                ", estado='" + estado + '\'' +
                '}';
    }
}