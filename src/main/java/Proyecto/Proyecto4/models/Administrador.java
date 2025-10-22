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
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 255)
    private String nombres;
    
    @Column(nullable = false, length = 255)
    private String apellidos;
    
    @Column(length = 255)
    private String telefono;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolAdmin rol = RolAdmin.ADMIN;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime ultimoAcceso;
    
    @Column(length = 255)
    private String fotoPerfil;
    
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