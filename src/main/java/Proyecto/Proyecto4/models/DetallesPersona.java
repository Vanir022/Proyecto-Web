package Proyecto.Proyecto4.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 500)
    private String intereses; // Guardamos los intereses como string separado por comas

    @Column(name = "acepta_marketing")
    private Boolean aceptaMarketing = false;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil; // Ruta o nombre del archivo de la foto

    @OneToOne(mappedBy = "detallesPersona")
    private Usuario usuario;

    // Constructor por defecto
    public DetallesPersona() {}

    // Constructor con parámetros principales
    public DetallesPersona(String nombres, String apellidos, String dni, String telefono, LocalDate fechaNacimiento) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}