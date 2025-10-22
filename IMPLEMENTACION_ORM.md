# 🗄️ Implementación de ORM en Proyecto4 - Resort Eden

## 📋 Índice
1. [Introducción](#introducción)
2. [Tecnologías Utilizadas](#tecnologías-utilizadas)
3. [Configuración](#configuración)
4. [Arquitectura del ORM](#arquitectura-del-orm)
5. [Modelos y Entidades](#modelos-y-entidades)
6. [Relaciones entre Entidades](#relaciones-entre-entidades)
7. [Repositorios](#repositorios)
8. [Patrones de Diseño](#patrones-de-diseño)
9. [Ventajas de la Implementación](#ventajas-de-la-implementación)
10. [Mejores Prácticas](#mejores-prácticas)

---

## 📖 Introducción

### ¿Qué es ORM?

**ORM (Object-Relational Mapping)** es una técnica de programación que permite convertir datos entre sistemas de tipos incompatibles utilizando lenguajes de programación orientados a objetos. En este proyecto, ORM permite:

- **Mapear objetos Java a tablas de bases de datos**
- **Realizar operaciones CRUD sin escribir SQL manualmente**
- **Gestionar relaciones entre entidades de forma automática**
- **Abstraer la capa de persistencia de datos**

### ¿Por qué usar ORM?

✅ **Productividad** - Menos código boilerplate  
✅ **Mantenibilidad** - Código más limpio y organizado  
✅ **Portabilidad** - Independencia de la base de datos  
✅ **Seguridad** - Prevención de inyección SQL  
✅ **Rendimiento** - Optimizaciones automáticas  

---

## 🛠️ Tecnologías Utilizadas

### 1. **JPA (Java Persistence API)**
- **Especificación:** API estándar de Java para ORM
- **Versión:** Jakarta Persistence API 3.0+
- **Propósito:** Definir cómo mapear objetos Java a bases de datos relacionales

### 2. **Hibernate**
- **Implementación:** Framework ORM que implementa JPA
- **Versión:** 6.x (incluido en Spring Boot 3.5.6)
- **Rol:** Motor de persistencia que ejecuta las operaciones reales

### 3. **Spring Data JPA**
- **Framework:** Capa de abstracción sobre JPA
- **Versión:** Incluida en Spring Boot 3.5.6
- **Características:** Repositorios automáticos, consultas derivadas, paginación

### 4. **MySQL**
- **Base de datos:** Sistema de gestión de bases de datos relacional
- **Versión:** MySQL 8.x
- **Dialecto:** `org.hibernate.dialect.MySQL8Dialect`

---

## ⚙️ Configuración

### 1. Dependencias Maven (`pom.xml`)

```xml
<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Spring Boot Starter Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Explicación:**
- `spring-boot-starter-data-jpa`: Incluye Hibernate, JPA, y Spring Data JPA
- `mysql-connector-j`: Driver JDBC para conectar con MySQL
- `spring-boot-starter-validation`: Bean Validation para validar entidades

---

### 2. Configuración de Conexión (`application.properties`)

```properties
# Nombre de la aplicación
spring.application.name=Proyecto4

# Configuración de la base de datos MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/resort_eden?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=12345678

# Configuración de JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

**Explicación detallada:**

#### Datasource (Conexión a BD)
- **`spring.datasource.url`**: URL de conexión a la base de datos
  - `jdbc:mysql://localhost:3306/resort_eden`: Protocolo, host, puerto y nombre de BD
  - `useSSL=false`: Desactiva SSL (desarrollo local)
  - `serverTimezone=UTC`: Zona horaria del servidor
  - `allowPublicKeyRetrieval=true`: Permite autenticación con clave pública

- **`spring.datasource.username`**: Usuario de MySQL
- **`spring.datasource.password`**: Contraseña de MySQL

#### Configuración JPA/Hibernate
- **`spring.jpa.hibernate.ddl-auto=update`**: 
  - **`create`**: Elimina y recrea las tablas cada vez (NO RECOMENDADO en producción)
  - **`update`**: Actualiza el esquema sin eliminar datos (RECOMENDADO para desarrollo)
  - **`validate`**: Solo valida que el esquema coincida
  - **`none`**: No hace nada (RECOMENDADO para producción)

- **`spring.jpa.show-sql=true`**: Muestra las consultas SQL en la consola (debugging)

- **`spring.jpa.database-platform`**: Dialecto SQL específico de MySQL 8

---

## 🏗️ Arquitectura del ORM

### Capas de la Arquitectura

```
┌─────────────────────────────────────────────┐
│          CAPA DE PRESENTACIÓN               │
│     (Controllers - HTML/Thymeleaf)          │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│          CAPA DE SERVICIO                   │
│      (Services - Lógica de negocio)         │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│       CAPA DE REPOSITORIO (ORM)             │
│   (Repositories - Spring Data JPA)          │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│          CAPA DE PERSISTENCIA               │
│        (Hibernate - JPA Provider)           │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│            BASE DE DATOS                    │
│          (MySQL - resort_eden)              │
└─────────────────────────────────────────────┘
```

---

## 🗂️ Modelos y Entidades

El proyecto cuenta con **6 entidades principales** que mapean las tablas de la base de datos.

### 1. **Usuario** (`Usuario.java`)

**Tabla:** `usuarios`

```java
@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255)
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Column(nullable = false, length = 255)
    private String rol; // "ROLE_USER", "ROLE_ADMIN"

    // Relaciones
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "detalles_persona_id")
    private DetallesPersona detallesPersona;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "administradores_usuarios",
        joinColumns = @JoinColumn(name = "usuarios_id"),
        inverseJoinColumns = @JoinColumn(name = "administradores_id")
    )
    private List<Administrador> administradores = new ArrayList<>();
}
```

**Explicación de Anotaciones:**

- **`@Entity`**: Marca la clase como entidad JPA
- **`@Table(name = "usuarios")`**: Especifica el nombre de la tabla en la BD
- **`@Id`**: Marca el campo como clave primaria
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`**: Auto-incremento
- **`@Column`**: Configura propiedades de la columna
  - `nullable = false`: No permite valores NULL
  - `unique = true`: Valores únicos (índice único)
  - `length = 100`: Longitud máxima de VARCHAR
- **`@NotBlank`, `@Email`, `@Size`**: Validaciones de Bean Validation

---

### 2. **DetallesPersona** (`DetallesPersona.java`)

**Tabla:** `detalles_persona`

```java
@Getter
@Setter
@Entity
@Table(name = "detalles_persona")
public class DetallesPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Pattern(regexp = "^[0-9]{7,20}$|^$")
    @Column(length = 20)
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 500)
    private String intereses;

    @Column(name = "acepta_marketing")
    private Boolean aceptaMarketing = false;

    @Size(max = 500)
    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    // Relación inversa
    @OneToOne(mappedBy = "detallesPersona")
    private Usuario usuario;
}
```

**Características:**
- Relación **1:1** con `Usuario`
- Almacena información personal extendida
- Validaciones de DNI peruano (8 dígitos)
- Soporte para foto de perfil

---

### 3. **Contacto** (`Contacto.java`)

**Tabla:** `contactos`

```java
@Entity
@Table(name = "contactos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, length = 150)
    private String email;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$")
    @Column(length = 20)
    private String telefono;
    
    @Size(max = 100)
    @Column(length = 100)
    private String hotel;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 5000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(length = 20, nullable = false)
    private String estado = "NUEVO"; // NUEVO, LEIDO, RESPONDIDO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarios_id")
    private Usuario usuario;
}
```

**Características:**
- Formulario de contacto del sitio web
- Relación **N:1** opcional con `Usuario`
- Estados de seguimiento (NUEVO, LEIDO, RESPONDIDO)
- Tipo de dato `TEXT` para mensajes largos

---

### 4. **Habitacion** (`Habitacion.java`)

**Tabla:** `habitaciones`

```java
@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El número es obligatorio")
    @Column(nullable = false, unique = true, length = 255)
    private String numero;
    
    @NotBlank(message = "El tipo es obligatorio")
    @Column(nullable = false, length = 255)
    private String tipo; // Suite, Estándar, Deluxe
    
    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer capacidad;
    
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabitacion estadoHabitacion = EstadoHabitacion.LIBRE;
    
    @NotBlank(message = "El hotel es obligatorio")
    @Column(nullable = false, length = 255)
    private String hotel; // Aranwa Cusco, Aranwa Paracas, etc.

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL)
    private List<Reserva> reservas = new ArrayList<>();
    
    public enum EstadoHabitacion {
        LIBRE, OCUPADA, MANTENIMIENTO, BLOQUEADA
    }
}
```

**Características:**
- Catálogo de habitaciones del hotel
- Enumeración para estados (`EstadoHabitacion`)
- Tipo de dato `DECIMAL` para precios
- Relación **1:N** con `Reserva`

---

### 5. **Reserva** (`Reserva.java`)

**Tabla:** `reservas`

```java
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
    @FutureOrPresent(message = "Debe ser hoy o futuro")
    @Column(nullable = false)
    private LocalDate fechaEntrada;
    
    @NotNull(message = "La fecha de salida es obligatoria")
    @FutureOrPresent(message = "Debe ser hoy o futuro")
    @Column(nullable = false)
    private LocalDate fechaSalida;
    
    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Column(nullable = false)
    private Integer numeroHuespedes;
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "Debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();
    
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
    @Column(nullable = false, length = 8)
    private String dniCliente;
    
    @Column(length = 255)
    private String codigoReserva;
    
    public enum EstadoReserva {
        PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, NO_SHOW
    }
}
```

**Características:**
- Entidad central del sistema de reservas
- Relaciones **N:1** con `Usuario` y `Habitacion`
- Enumeración para estados de reserva
- Validaciones de fechas futuras

---

### 6. **Administrador** (`Administrador.java`)

**Tabla:** `administradores`

```java
@Entity
@Table(name = "administradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255)
    @Column(nullable = false, length = 255)
    private String password;
    
    @NotBlank(message = "Los nombres son obligatorios")
    @Column(nullable = false, length = 255)
    private String nombres;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(nullable = false, length = 255)
    private String apellidos;
    
    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolAdmin rol = RolAdmin.ADMIN;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime ultimoAcceso;
    
    @Column(length = 255)
    private String hotel; // Hotel específico o null para super admin

    @ManyToMany(mappedBy = "administradores", fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();
    
    public enum RolAdmin {
        SUPER_ADMIN, ADMIN, GERENTE, RECEPCION
    }
}
```

**Características:**
- Sistema de roles jerárquico
- Relación **N:N** con `Usuario`
- Auditoría (fecha creación, último acceso)
- Enumeración de roles administrativos

---

## 🔗 Relaciones entre Entidades

### Diagrama de Relaciones

```
┌──────────────────┐
│   Administrador  │
│  (administradores)│
└────────┬─────────┘
         │
         │ N:N (administradores_usuarios)
         │
         ↓
┌──────────────────┐         ┌──────────────────┐
│     Usuario      │  1:1    │ DetallesPersona  │
│   (usuarios)     │◄────────┤(detalles_persona)│
└────────┬─────────┘         └──────────────────┘
         │
         │ 1:N
         │
         ↓
┌──────────────────┐         ┌──────────────────┐
│     Reserva      │  N:1    │   Habitacion     │
│   (reservas)     │────────►│  (habitaciones)  │
└──────────────────┘         └──────────────────┘
         │
         │ N:1 (opcional)
         │
         ↓
┌──────────────────┐
│    Contacto      │
│   (contactos)    │
└──────────────────┘
```

---

### 1. **OneToOne (1:1)**

**Usuario ↔ DetallesPersona**

```java
// En Usuario.java
@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JoinColumn(name = "detalles_persona_id", referencedColumnName = "id")
private DetallesPersona detallesPersona;

// En DetallesPersona.java
@OneToOne(mappedBy = "detallesPersona")
private Usuario usuario;
```

**Explicación:**
- **Cascada**: `CascadeType.ALL` - Al guardar/eliminar Usuario, se hace lo mismo con DetallesPersona
- **Fetch**: `FetchType.LAZY` - Carga perezosa (solo cuando se accede)
- **JoinColumn**: Especifica la columna FK en la tabla `usuarios`
- **mappedBy**: Indica que esta es la parte "inversa" de la relación

**SQL Generado:**
```sql
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detalles_persona_id BIGINT,
    FOREIGN KEY (detalles_persona_id) REFERENCES detalles_persona(id)
);
```

---

### 2. **OneToMany (1:N)**

**Usuario → Reservas**

```java
// En Usuario.java
@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Reserva> reservas = new ArrayList<>();
```

**Explicación:**
- **mappedBy**: `"usuario"` - Indica que `Reserva.usuario` es el dueño de la relación
- **cascade**: Propaga operaciones a las reservas relacionadas
- **orphanRemoval**: `true` - Elimina reservas huérfanas (sin usuario)

**Métodos Helper:**
```java
public void addReserva(Reserva reserva) {
    reservas.add(reserva);
    reserva.setUsuario(this);
}

public void removeReserva(Reserva reserva) {
    reservas.remove(reserva);
    reserva.setUsuario(null);
}
```

---

### 3. **ManyToOne (N:1)**

**Reserva → Usuario**

```java
// En Reserva.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "usuario_id", nullable = false)
private Usuario usuario;
```

**Explicación:**
- **Lado propietario** de la relación (tiene la FK)
- **nullable = false**: La reserva DEBE tener un usuario
- Genera columna `usuario_id` en tabla `reservas`

**SQL Generado:**
```sql
CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

---

### 4. **ManyToMany (N:N)**

**Usuario ↔ Administrador**

```java
// En Usuario.java (lado propietario)
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "administradores_usuarios",
    joinColumns = @JoinColumn(name = "usuarios_id"),
    inverseJoinColumns = @JoinColumn(name = "administradores_id")
)
private List<Administrador> administradores = new ArrayList<>();

// En Administrador.java (lado inverso)
@ManyToMany(mappedBy = "administradores", fetch = FetchType.LAZY)
private List<Usuario> usuarios = new ArrayList<>();
```

**Explicación:**
- **@JoinTable**: Crea tabla intermedia `administradores_usuarios`
- **joinColumns**: FK que apunta a Usuario
- **inverseJoinColumns**: FK que apunta a Administrador
- **mappedBy**: Lado inverso (no genera tabla)

**SQL Generado:**
```sql
CREATE TABLE administradores_usuarios (
    usuarios_id BIGINT NOT NULL,
    administradores_id BIGINT NOT NULL,
    PRIMARY KEY (usuarios_id, administradores_id),
    FOREIGN KEY (usuarios_id) REFERENCES usuarios(id),
    FOREIGN KEY (administradores_id) REFERENCES administradores(id)
);
```

---

## 📚 Repositorios

Spring Data JPA proporciona repositorios que eliminan la necesidad de implementar DAOs manualmente.

### 1. **UsuarioRepository**

```java
package Proyecto.Proyecto4.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import Proyecto.Proyecto4.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método de consulta derivado
    Optional<Usuario> findByEmail(String email);
}
```

**Métodos heredados de `JpaRepository`:**

```java
// CRUD básico (automáticos)
Usuario save(Usuario usuario);                    // INSERT o UPDATE
Optional<Usuario> findById(Long id);              // SELECT por ID
List<Usuario> findAll();                          // SELECT ALL
void deleteById(Long id);                         // DELETE por ID
long count();                                     // COUNT

// Operaciones batch
List<Usuario> saveAll(Iterable<Usuario> usuarios);
void deleteAll();

// Paginación y ordenamiento
Page<Usuario> findAll(Pageable pageable);
List<Usuario> findAll(Sort sort);
```

**Método de consulta derivado:**
```java
Optional<Usuario> findByEmail(String email);
```

Spring Data JPA genera automáticamente:
```sql
SELECT * FROM usuarios WHERE email = ?
```

---

### 2. **ContactoRepository**

```java
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    // Consultas derivadas
    List<Contacto> findByEstado(String estado);
    List<Contacto> findByEmail(String email);
    
    // Consulta personalizada con @Query
    @Query("SELECT COUNT(c) FROM Contacto c WHERE c.estado = ?1")
    long countByEstado(String estado);
}
```

---

### 3. **ReservaRepository**

```java
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByHabitacion(Habitacion habitacion);
    List<Reserva> findByEstado(EstadoReserva estado);
    
    @Query("SELECT r FROM Reserva r WHERE r.habitacion = ?1 " +
           "AND ((r.fechaEntrada <= ?3 AND r.fechaSalida >= ?2) " +
           "AND r.estado != 'CANCELADA')")
    List<Reserva> findReservasEnRango(Habitacion habitacion, 
                                      LocalDate fechaInicio, 
                                      LocalDate fechaFin);
}
```

---

### 4. **HabitacionRepository**

```java
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByHotel(String hotel);
    List<Habitacion> findByDisponible(Boolean disponible);
    List<Habitacion> findByTipoAndHotel(String tipo, String hotel);
    
    @Query("SELECT h FROM Habitacion h WHERE h.precio BETWEEN ?1 AND ?2")
    List<Habitacion> findByPrecioRango(BigDecimal min, BigDecimal max);
}
```

---

### 5. **DetallesPersonaRepository**

```java
public interface DetallesPersonaRepository extends JpaRepository<DetallesPersona, Long> {
    Optional<DetallesPersona> findByDni(String dni);
    Optional<DetallesPersona> findByUsuario(Usuario usuario);
}
```

---

### 6. **AdministradorRepository**

```java
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByEmail(String email);
    List<Administrador> findByRol(RolAdmin rol);
    List<Administrador> findByActivo(Boolean activo);
    List<Administrador> findByHotel(String hotel);
}
```

---

## 🎨 Patrones de Diseño Implementados

### 1. **Repository Pattern**

Abstrae el acceso a datos, proporcionando una interfaz de colección.

```java
// En lugar de escribir SQL...
public List<Usuario> obtenerUsuarios() {
    return usuarioRepository.findAll();
}

// O consultas específicas
public Optional<Usuario> buscarPorEmail(String email) {
    return usuarioRepository.findByEmail(email);
}
```

---

### 2. **Data Transfer Object (DTO) Pattern**

Transferir datos entre capas sin exponer entidades directamente.

```java
@Getter
@Setter
public class RegistroUsuarioDTO {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6)
    private String password;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    private String dni;
    
    @NotNull
    @Past
    private LocalDate birthDate;
    
    @AssertTrue
    private Boolean acceptTerms;
}
```

**Uso:**
```java
@PostMapping("/registro")
public ResponseEntity<?> registrar(@Valid @RequestBody RegistroUsuarioDTO dto) {
    Usuario usuario = usuarioService.registrarConDetalles(dto);
    return ResponseEntity.ok(usuario);
}
```

---

### 3. **Service Layer Pattern**

Encapsula la lógica de negocio.

```java
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final DetallesPersonaRepository detallesRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Usuario registrarConDetalles(RegistroUsuarioDTO dto) {
        // Validar email único
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombreCompleto());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol("ROLE_USER");
        
        // Crear detalles
        DetallesPersona detalles = new DetallesPersona();
        detalles.setNombres(dto.getFirstName());
        detalles.setApellidos(dto.getLastName());
        detalles.setDni(dto.getDni());
        detalles.setFechaNacimiento(dto.getBirthDate());
        
        // Asociar
        usuario.setDetallesPersona(detalles);
        
        // Guardar (cascada guardará detalles también)
        return usuarioRepository.save(usuario);
    }
}
```

---

### 4. **Lazy Loading Pattern**

Carga de datos bajo demanda para optimizar rendimiento.

```java
// Usuario cargado, pero detallesPersona NO
Usuario usuario = usuarioRepository.findById(1L).get();

// Se carga solo cuando se accede
String nombres = usuario.getDetallesPersona().getNombres(); // Query aquí
```

---

### 5. **Cascade Operations**

Propagación automática de operaciones.

```java
@OneToOne(cascade = CascadeType.ALL)
private DetallesPersona detallesPersona;

// Al guardar usuario, también guarda detallesPersona
usuarioRepository.save(usuario);
```

**Tipos de Cascade:**
- `PERSIST`: Propaga operaciones de guardar
- `MERGE`: Propaga operaciones de actualizar
- `REMOVE`: Propaga operaciones de eliminar
- `REFRESH`: Propaga operaciones de refrescar
- `DETACH`: Propaga operaciones de desvincular
- `ALL`: Todas las anteriores

---

## ✅ Ventajas de la Implementación

### 1. **Productividad**
- ✅ No escribir SQL manualmente
- ✅ Métodos CRUD automáticos
- ✅ Consultas derivadas de nombres de métodos
- ✅ Menos código boilerplate

### 2. **Mantenibilidad**
- ✅ Código más limpio y legible
- ✅ Cambios centralizados en entidades
- ✅ Refactorización más sencilla
- ✅ Documentación implícita en anotaciones

### 3. **Portabilidad**
- ✅ Fácil cambiar de MySQL a PostgreSQL
- ✅ Dialecto gestionado por Hibernate
- ✅ SQL generado automáticamente
- ✅ Independencia de la BD

### 4. **Seguridad**
- ✅ Prevención de inyección SQL
- ✅ Consultas parametrizadas
- ✅ Validaciones en entidades
- ✅ Transacciones ACID

### 5. **Rendimiento**
- ✅ Lazy Loading
- ✅ Cache de primer nivel (sesión)
- ✅ Batch operations
- ✅ Optimización de queries

---

## 🎯 Mejores Prácticas Implementadas

### 1. **Uso de DTOs**
```java
// ✅ CORRECTO - Usar DTO para entrada
@PostMapping("/registro")
public ResponseEntity<?> registrar(@Valid @RequestBody RegistroUsuarioDTO dto) {
    // ...
}

// ❌ INCORRECTO - Exponer entidad directamente
@PostMapping("/registro")
public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
    // ...
}
```

---

### 2. **Transacciones**
```java
@Service
public class ReservaService {
    
    @Transactional // Asegura atomicidad
    public Reserva crearReserva(Usuario usuario, Habitacion habitacion, ...) {
        // Verificar disponibilidad
        if (!verificarDisponibilidad(habitacion, fechaEntrada, fechaSalida)) {
            throw new RuntimeException("Habitación no disponible");
        }
        
        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setHabitacion(habitacion);
        // ... más configuraciones
        
        // Actualizar estado habitación
        habitacion.setDisponible(false);
        habitacionRepository.save(habitacion);
        
        // Guardar reserva
        return reservaRepository.save(reserva);
        
        // Si falla algo, se hace ROLLBACK automático
    }
}
```

---

### 3. **Lazy Loading y N+1 Problem**
```java
// ❌ PROBLEMA N+1
List<Usuario> usuarios = usuarioRepository.findAll();
for (Usuario u : usuarios) {
    System.out.println(u.getDetallesPersona().getNombres()); // Query por cada usuario!
}

// ✅ SOLUCIÓN - Fetch Join
@Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.detallesPersona")
List<Usuario> findAllWithDetalles();
```

---

### 4. **Validaciones en Múltiples Capas**
```java
// Capa 1: Validación de Bean
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email debe ser válido")
private String email;

// Capa 2: Validación en Servicio
if (usuarioRepository.findByEmail(email).isPresent()) {
    throw new RuntimeException("Email ya registrado");
}

// Capa 3: Restricción de BD
@Column(nullable = false, unique = true)
private String email;
```

---

### 5. **Índices y Optimización**
```java
// Crear índices en columnas frecuentemente consultadas
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_rol", columnList = "rol")
})
```

---

### 6. **Soft Delete (Borrado Lógico)**
```java
@Entity
public class Usuario {
    // ...
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;
}

// Servicio
@Transactional
public void eliminarUsuario(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    usuario.setActivo(false);
    usuario.setFechaEliminacion(LocalDateTime.now());
    usuarioRepository.save(usuario);
}
```

---

### 7. **Auditoría Automática**
```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Reserva {
    // ...
    
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @CreatedBy
    @Column(name = "creado_por")
    private String creadoPor;
    
    @LastModifiedBy
    @Column(name = "modificado_por")
    private String modificadoPor;
}
```

---

## 📊 Resumen de la Implementación

| Aspecto | Detalle |
|---------|---------|
| **Framework ORM** | Hibernate 6.x |
| **API de Persistencia** | JPA (Jakarta Persistence API) |
| **Abstracción de Repositorio** | Spring Data JPA |
| **Base de Datos** | MySQL 8.x |
| **Entidades** | 6 modelos mapeados |
| **Relaciones** | 1:1, 1:N, N:1, N:N |
| **Repositorios** | 6 interfaces JPA |
| **Validaciones** | Bean Validation (Jakarta Validation) |
| **Estrategia de ID** | AUTO_INCREMENT |
| **DDL Auto** | `update` (desarrollo) |
| **Fetch Strategy** | LAZY (por defecto) |
| **Transacciones** | `@Transactional` |

---

## 🎓 Conclusión

La implementación de ORM en este proyecto **Proyecto4 - Resort Eden** utiliza las mejores prácticas y patrones de diseño modernos:

✅ **JPA/Hibernate** como motor de persistencia robusto  
✅ **Spring Data JPA** para simplificar el acceso a datos  
✅ **Relaciones bien definidas** entre entidades  
✅ **Validaciones en múltiples capas** para integridad de datos  
✅ **Patrones de diseño** (Repository, DTO, Service Layer)  
✅ **Optimizaciones** (Lazy Loading, Batch Operations)  
✅ **Seguridad** (prevención de SQL injection)  

Esta arquitectura permite desarrollar de forma ágil, mantener el código limpio y escalar la aplicación fácilmente a medida que crezcan los requisitos del negocio.

---

**Proyecto:** Proyecto4 - Resort Eden  
**Fecha:** 22 de octubre de 2025  
**Versión de Spring Boot:** 3.5.6  
**Versión de Java:** 21  
**Base de Datos:** MySQL 8.x
