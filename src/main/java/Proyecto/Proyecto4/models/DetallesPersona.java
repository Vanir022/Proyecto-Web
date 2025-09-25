package Proyecto.Proyecto4.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "detalles_persona")
public class DetallesPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 500)
    private String intereses; // Guardamos los intereses como string separado por comas

    @Column(name = "acepta_marketing")
    private Boolean aceptaMarketing = false;

    @OneToOne(mappedBy = "detallesPersona")
    private Usuario usuario;

    // Constructor por defecto
    public DetallesPersona() {}

    // Constructor con parámetros principales
    public DetallesPersona(String nombres, String apellidos, String telefono, LocalDate fechaNacimiento) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}