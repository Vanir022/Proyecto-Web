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
    
    @Column(nullable = false, unique = true, length = 255)
    private String numero;
    
    @Column(nullable = false, length = 255)
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
    
    @Column(length = 255)
    private String imagenPrincipal;
    
    @Column(columnDefinition = "TEXT")
    private String amenidades; // Lista de amenidades separadas por comas
    
    @Column(length = 255)
    private String vista; // Mar, Jardín, Montaña, etc.
    
    @Column
    private Integer metrosCuadrados;
    
    @Column(length = 255)
    private String cama; // King, Queen, Twin, etc.
    
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