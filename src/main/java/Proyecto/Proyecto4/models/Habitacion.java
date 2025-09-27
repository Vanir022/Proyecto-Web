package Proyecto.Proyecto4.models;

import java.math.BigDecimal;

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
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String numero;
    
    @Column(nullable = false)
    private String tipo; // Suite, Estándar, Deluxe, etc.
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Column(nullable = false)
    private Integer capacidad; // Número de personas
    
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabitacion estadoHabitacion = EstadoHabitacion.LIBRE;
    
    @Column
    private String imagenPrincipal;
    
    @Column(columnDefinition = "TEXT")
    private String amenidades; // Lista de amenidades separadas por comas
    
    @Column
    private String vista; // Mar, Jardín, Montaña, etc.
    
    @Column
    private Integer metrosCuadrados;
    
    @Column
    private String cama; // King, Queen, Twin, etc.
    
    @Column(nullable = false)
    private String hotel; // Aranwa Cusco, Aranwa Paracas, Aranwa Colca, etc.
    
    // Enum para el estado de ocupación de la habitación
    public enum EstadoHabitacion {
        LIBRE,          // Habitación disponible para reserva
        OCUPADA,        // Habitación actualmente ocupada por huésped
        MANTENIMIENTO,  // Habitación en mantenimiento/limpieza
        BLOQUEADA       // Habitación bloqueada por administrador
    }
}