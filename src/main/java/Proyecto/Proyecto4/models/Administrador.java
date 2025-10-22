package Proyecto.Proyecto4.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false, length = 255)
    private String password;
    
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 255, message = "Los nombres deben tener entre 2 y 255 caracteres")
    @Column(nullable = false, length = 255)
    private String nombres;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 255, message = "Los apellidos deben tener entre 2 y 255 caracteres")
    @Column(nullable = false, length = 255)
    private String apellidos;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
    @Column(length = 255)
    private String telefono;
    
    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolAdmin rol = RolAdmin.ADMIN;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime ultimoAcceso;
    
    @Size(max = 255, message = "La ruta de la foto de perfil no puede exceder 255 caracteres")
    @Column(length = 255)
    private String fotoPerfil;
    
    @Size(max = 255, message = "El nombre del hotel no puede exceder 255 caracteres")
    @Column(length = 255)
    private String hotel; // Hotel específico al que pertenece, null para super admin

    // Relación ManyToMany con Usuarios (tabla intermedia administradores_usuarios)
    @ManyToMany(mappedBy = "administradores", fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();
    
    // Enum para roles de administrador
    public enum RolAdmin {
        SUPER_ADMIN, // Acceso completo a todo el sistema
        ADMIN,       // Administrador de hotel específico
        GERENTE,     // Gerente de hotel con permisos limitados
        RECEPCION    // Personal de recepción
    }

    // Métodos helper para gestionar usuarios
    public void addUsuario(Usuario usuario) {
        usuarios.add(usuario);
        usuario.getAdministradores().add(this);
    }

    public void removeUsuario(Usuario usuario) {
        usuarios.remove(usuario);
        usuario.getAdministradores().remove(this);
    }
}