package Proyecto.Proyecto4.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "La habitación es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;
    
    @NotNull(message = "La fecha de entrada es obligatoria")
    @FutureOrPresent(message = "La fecha de entrada debe ser hoy o en el futuro")
    @Column(nullable = false)
    private LocalDate fechaEntrada;
    
    @NotNull(message = "La fecha de salida es obligatoria")
    @FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro")
    @Column(nullable = false)
    private LocalDate fechaSalida;
    
    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Column(nullable = false)
    private Integer numeroHuespedes;
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();
    
    @Column(columnDefinition = "TEXT")
    private String comentarios;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
    @Column(length = 255)
    private String telefonoContacto;
    
    @Size(max = 5000, message = "Las solicitudes especiales no pueden exceder 5000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String solicitudesEspeciales;
    
    @Column(length = 255)
    private String codigoReserva; // Código único para la reserva
    
    @NotBlank(message = "El DNI del cliente es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener exactamente 8 dígitos")
    @Column(nullable = false, length = 8)
    private String dniCliente; // DNI del cliente que hace la reserva
    
    // Enum para el estado de la reserva
    public enum EstadoReserva {
        PENDIENTE,
        CONFIRMADA, 
        CANCELADA,
        COMPLETADA,
        NO_SHOW
    }
}