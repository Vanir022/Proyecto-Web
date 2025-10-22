package Proyecto.Proyecto4.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El número de habitación es obligatorio")
    @Size(max = 255, message = "El número de habitación no puede exceder 255 caracteres")
    @Column(nullable = false, unique = true, length = 255)
    private String numero;
    
    @NotBlank(message = "El tipo de habitación es obligatorio")
    @Size(max = 255, message = "El tipo no puede exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String tipo; // Suite, Estándar, Deluxe, etc.
    
    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1 persona")
    @Column(nullable = false)
    private Integer capacidad; // Número de personas
    
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @NotNull(message = "El estado de la habitación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabitacion estadoHabitacion = EstadoHabitacion.LIBRE;
    
    @Size(max = 255, message = "La ruta de la imagen no puede exceder 255 caracteres")
    @Column(length = 255)
    private String imagenPrincipal;
    
    @Size(max = 5000, message = "Las amenidades no pueden exceder 5000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String amenidades; // Lista de amenidades separadas por comas
    
    @Size(max = 255, message = "La vista no puede exceder 255 caracteres")
    @Column(length = 255)
    private String vista; // Mar, Jardín, Montaña, etc.
    
    @Min(value = 1, message = "Los metros cuadrados deben ser al menos 1")
    @Column
    private Integer metrosCuadrados;
    
    @Size(max = 255, message = "El tipo de cama no puede exceder 255 caracteres")
    @Column(length = 255)
    private String cama; // King, Queen, Twin, etc.
    
    @NotBlank(message = "El hotel es obligatorio")
    @Size(max = 255, message = "El nombre del hotel no puede exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String hotel; // Aranwa Cusco, Aranwa Paracas, Aranwa Colca, etc.

    // Relación OneToMany con Reservas (una habitación puede tener múltiples reservas)
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();
    
    // Enum para el estado de ocupación de la habitación
    public enum EstadoHabitacion {
        LIBRE,          // Habitación disponible para reserva
        OCUPADA,        // Habitación actualmente ocupada por huésped
        MANTENIMIENTO,  // Habitación en mantenimiento/limpieza
        BLOQUEADA       // Habitación bloqueada por administrador
    }

    // Métodos helper para gestionar reservas
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setHabitacion(this);
    }

    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setHabitacion(null);
    }
}