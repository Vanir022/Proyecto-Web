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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;
    
    @Column(nullable = false)
    private LocalDate fechaEntrada;
    
    @Column(nullable = false)
    private LocalDate fechaSalida;
    
    @Column(nullable = false)
    private Integer numeroHuespedes;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();
    
    @Column(columnDefinition = "TEXT")
    private String comentarios;
    
    @Column
    private String telefonoContacto;
    
    @Column
    private String solicitudesEspeciales;
    
    @Column
    private String codigoReserva; // Código único para la reserva
    
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