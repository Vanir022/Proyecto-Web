package Proyecto.Proyecto4.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String nombres;
    
    @Column(nullable = false)
    private String apellidos;
    
    @Column
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
    
    @Column
    private String fotoPerfil;
    
    @Column
    private String hotel; // Hotel específico al que pertenece, null para super admin
    
    // Enum para roles de administrador
    public enum RolAdmin {
        SUPER_ADMIN, // Acceso completo a todo el sistema
        ADMIN,       // Administrador de hotel específico
        GERENTE,     // Gerente de hotel con permisos limitados
        RECEPCION    // Personal de recepción
    }
}