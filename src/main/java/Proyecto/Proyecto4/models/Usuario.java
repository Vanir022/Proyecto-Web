package Proyecto.Proyecto4.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "failed_attempt")
    private Integer failedAttempt = 0;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "lock_time")
    private java.time.LocalDateTime lockTime;

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public void setFailedAttempt(int failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    public boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public java.time.LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(java.time.LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    @NotBlank(message = "El email es obligatorio") // Email no puede estar vacío
    @Email(message = "El email debe ser válido") // Validar formato de email
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Column(nullable = false, unique = true, length = 150) // Email único
    private String email;

    @NotBlank(message = "La contraseña es obligatoria") 
    @Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Size(max = 255, message = "El rol no puede exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String rol; // Ejemplo: "ROLE_USER", "ROLE_ADMIN"

    @Column(nullable = false)
    private Boolean activo = true; // Estado del usuario: true = activo, false = inactivo

    // Relación OneToOne con DetallesPersona
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "detalles_persona_id", referencedColumnName = "id")
    private DetallesPersona detallesPersona;

    // Relación OneToMany con Reservas (un usuario puede tener múltiples reservas)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    // Relación ManyToMany con Administradores (tabla intermedia administradores_usuarios)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "administradores_usuarios",
        joinColumns = @JoinColumn(name = "usuarios_id"),
        inverseJoinColumns = @JoinColumn(name = "administradores_id")
    )
    private List<Administrador> administradores = new ArrayList<>();

    // Constructor por defecto
    public Usuario() {}

    // Constructor con parámetros básicos
    public Usuario(String nombre, String email, String password, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Métodos helper para gestionar reservas
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setUsuario(this);
    }

    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setUsuario(null);
    }
}