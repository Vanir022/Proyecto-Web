package Proyecto.Proyecto4.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_persona")
public class DetallesPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener exactamente 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
    @Column(length = 20)
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Size(max = 500, message = "Los intereses no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String intereses; // Guardamos los intereses como string separado por comas

    @Column(name = "acepta_marketing")
    private Boolean aceptaMarketing = false;

    @Size(max = 500, message = "La ruta de la foto de perfil no puede exceder 500 caracteres")
    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil; // Ruta o nombre del archivo de la foto

    // Relación OneToOne bidireccional con Usuario
    @OneToOne(mappedBy = "detallesPersona")
    private Usuario usuario;

    // Relación ManyToOne con Administrador para asociar detalles con administrador
    // (según el diagrama hay una FK administradores_id)
    @Column(name = "administradores_id")
    private Long administradoresId;

    @Column(name = "administradores_usuarios_id")
    private Long administradoresUsuariosId;

    // Constructor por defecto
    public DetallesPersona() {}

    // Constructor con parámetros principales
    public DetallesPersona(String nombres, String apellidos, String dni, String telefono, LocalDate fechaNacimiento) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}