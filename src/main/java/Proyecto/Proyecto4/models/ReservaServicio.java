package Proyecto.Proyecto4.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservas_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaServicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "El tipo de servicio es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoServicio tipoServicio;
    
    @NotBlank(message = "El nombre del paquete es obligatorio")
    @Size(max = 200, message = "El nombre del paquete no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombrePaquete;
    
    @NotNull(message = "La fecha del servicio es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaServicio;
    
    @Column
    private LocalTime horaServicio;
    
    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Column(nullable = false)
    private Integer numeroPersonas;
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();
    
    @Column(length = 255)
    private String codigoReserva;
    
    @NotBlank(message = "El DNI del cliente es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener exactamente 8 dígitos")
    @Column(nullable = false, length = 8)
    private String dniCliente;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
    @Column(length = 20)
    private String telefonoContacto;
    
    @Column(length = 255)
    private String emailContacto;
    
    @Size(max = 100, message = "El hotel no puede exceder 100 caracteres")
    @Column(length = 100)
    private String hotel; // Para eventos y bodas
    
    @Size(max = 100, message = "El salón no puede exceder 100 caracteres")
    @Column(length = 100)
    private String salon; // Para eventos y bodas
    
    @Size(max = 5000, message = "Las solicitudes especiales no pueden exceder 5000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String solicitudesEspeciales;
    
    @Column(columnDefinition = "TEXT")
    private String comentarios;
    
    // Enum para el tipo de servicio
    public enum TipoServicio {
        SPA("Spa"),
        BODA("Boda"),
        EVENTO("Evento");
        
        private final String nombre;
        
        TipoServicio(String nombre) {
            this.nombre = nombre;
        }
        
        public String getNombre() {
            return nombre;
        }
    }
    
    // Enum para el estado de la reserva
    public enum EstadoReserva {
        PENDIENTE,
        CONFIRMADA,
        CANCELADA,
        COMPLETADA,
        NO_SHOW
    }
}
