package Proyecto.Proyecto4.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "contactos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contacto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, length = 150)
    private String email;
    
    @Column(length = 20)
    private String telefono;
    
    @Column(length = 100)
    private String hotel;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(length = 20, nullable = false)
    private String estado = "NUEVO"; // NUEVO, LEIDO, RESPONDIDO

    // Relación ManyToOne con Usuario (opcional - si el contacto es de un usuario registrado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarios_id")
    private Usuario usuario;
    
    // Constructor con parámetros principales
    public Contacto(String nombre, String email, String telefono, String hotel, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.hotel = hotel;
        this.mensaje = mensaje;
        this.fechaEnvio = LocalDateTime.now();
        this.estado = "NUEVO";
    }
    
    @Override
    public String toString() {
        return "Contacto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", hotel='" + hotel + '\'' +
                ", fechaEnvio=" + fechaEnvio +
                ", estado='" + estado + '\'' +
                '}';
    }
}