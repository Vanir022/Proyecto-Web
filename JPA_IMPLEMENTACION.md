# 🔷 Implementación de JPA en Proyecto4 - Resort Eden

## 📌 ¿Qué es JPA?

**JPA (Jakarta Persistence API)** es la especificación estándar de Java para mapear objetos a bases de datos relacionales. Define un conjunto de **anotaciones y APIs** que frameworks como Hibernate implementan.

### Roles en tu proyecto:
- **JPA**: Define las reglas (especificación)
- **Hibernate**: Implementa las reglas (motor ORM)
- **Spring Data JPA**: Simplifica el uso con repositorios automáticos

---

## ⚙️ Configuración de JPA

### **Dependencias (pom.xml)**

```xml
<!-- Spring Data JPA: incluye JPA + Hibernate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Validaciones -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### **Configuración (application.properties)**

```properties
# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/resort_eden
spring.datasource.username=root
spring.datasource.password=12345678

# Configuración de Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

| Propiedad | Descripción |
|-----------|-------------|
| `ddl-auto=update` | Actualiza el esquema automáticamente sin perder datos |
| `show-sql=true` | Muestra el SQL generado en consola |

---

## 📝 Anotaciones JPA Principales

### **1. Definición de Entidades**

#### `@Entity` y `@Table`
```java
@Entity                        // Marca la clase como entidad JPA
@Table(name = "usuarios")      // Define el nombre de la tabla
public class Usuario {
    // ...
}
```

#### `@Id` y `@GeneratedValue`
```java
@Id                                              // Clave primaria
@GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento
private Long id;
```

**Estrategias de generación:**
- `IDENTITY`: Auto-incremento (MySQL, PostgreSQL)
- `SEQUENCE`: Secuencias (Oracle)
- `TABLE`: Tabla separada para IDs
- `AUTO`: JPA decide automáticamente

---

### **2. Mapeo de Columnas**

#### `@Column`
```java
@Column(
    name = "email",              // Nombre en BD
    nullable = false,            // NOT NULL
    unique = true,               // UNIQUE
    length = 150                 // VARCHAR(150)
)
private String email;

@Column(precision = 10, scale = 2)  // DECIMAL(10,2) para dinero
private BigDecimal precio;

@Column(columnDefinition = "TEXT")  // Tipo específico
private String descripcion;
```

---

### **3. Relaciones entre Entidades**

#### **OneToOne (1:1)** - Usuario ↔ DetallesPersona

```java
// Lado propietario (tiene la FK)
@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JoinColumn(name = "detalles_persona_id", referencedColumnName = "id")
private DetallesPersona detallesPersona;

// Lado inverso
@OneToOne(mappedBy = "detallesPersona")
private Usuario usuario;
```

**Genera:**
```sql
ALTER TABLE usuarios ADD FOREIGN KEY (detalles_persona_id) 
REFERENCES detalles_persona(id);
```

---

#### **OneToMany (1:N)** - Usuario → Reservas

```java
@OneToMany(
    mappedBy = "usuario",        // Campo en clase Reserva
    cascade = CascadeType.ALL,   // Propaga operaciones
    orphanRemoval = true         // Elimina reservas huérfanas
)
private List<Reserva> reservas = new ArrayList<>();

// Métodos helper
public void addReserva(Reserva reserva) {
    reservas.add(reserva);
    reserva.setUsuario(this);
}
```

---

#### **ManyToOne (N:1)** - Reserva → Usuario

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "usuario_id", nullable = false)
private Usuario usuario;
```

**Genera:**
```sql
ALTER TABLE reservas ADD FOREIGN KEY (usuario_id) 
REFERENCES usuarios(id);
```

---

#### **ManyToMany (N:N)** - Usuario ↔ Administrador

```java
// Lado propietario (crea tabla intermedia)
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "administradores_usuarios",
    joinColumns = @JoinColumn(name = "usuarios_id"),
    inverseJoinColumns = @JoinColumn(name = "administradores_id")
)
private List<Administrador> administradores = new ArrayList<>();

// Lado inverso
@ManyToMany(mappedBy = "administradores")
private List<Usuario> usuarios = new ArrayList<>();
```

**Genera:**
```sql
CREATE TABLE administradores_usuarios (
    usuarios_id BIGINT NOT NULL,
    administradores_id BIGINT NOT NULL,
    PRIMARY KEY (usuarios_id, administradores_id)
);
```

---

### **4. Enumeraciones**

#### `@Enumerated`

```java
public enum EstadoReserva {
    PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, NO_SHOW
}

@Enumerated(EnumType.STRING)  // Guarda "PENDIENTE", no 0
@Column(nullable = false)
private EstadoReserva estado = EstadoReserva.PENDIENTE;
```

| Tipo | Guardado | Ventaja | Desventaja |
|------|----------|---------|------------|
| `STRING` | "PENDIENTE" | Legible | Más espacio |
| `ORDINAL` | 0, 1, 2 | Compacto | Frágil si reordenas |

**Recomendación:** Siempre `EnumType.STRING` ✅

---

### **5. Tipos Temporales**

```java
// Java 8+ (no necesita @Temporal)
@Column(name = "fecha_nacimiento")
private LocalDate fechaNacimiento;

@Column(nullable = false)
private LocalDateTime fechaReserva = LocalDateTime.now();
```

---

## 🔄 Parámetros de Relaciones

### **Cascade (Propagación)**

```java
@OneToOne(cascade = CascadeType.ALL)
```

| Tipo | Qué propaga |
|------|-------------|
| `PERSIST` | save() |
| `MERGE` | update() |
| `REMOVE` | delete() |
| `REFRESH` | refresh() |
| `DETACH` | detach() |
| `ALL` | Todas las anteriores |

**Ejemplo:**
```java
// Al guardar usuario, también guarda detalles
usuario.setDetallesPersona(detalles);
usuarioRepository.save(usuario);  // Guarda ambos
```

---

### **Fetch (Estrategia de Carga)**

| Tipo | Cuándo carga |
|------|--------------|
| `LAZY` | Solo cuando accedes al campo |
| `EAGER` | Inmediatamente con la entidad |

**Defaults:**
- `@OneToOne`: EAGER
- `@ManyToOne`: EAGER  
- `@OneToMany`: LAZY ✅
- `@ManyToMany`: LAZY ✅

**Recomendación:** Usa `LAZY` para colecciones

```java
@OneToMany(fetch = FetchType.LAZY)
private List<Reserva> reservas;
```

---

## 📚 Spring Data JPA Repositories

### **Interfaz Base: JpaRepository**

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Métodos CRUD automáticos heredados:
    // save(), findById(), findAll(), deleteById(), count()
}
```

### **Métodos Derivados**

Spring genera automáticamente el SQL a partir del nombre del método:

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);
    
    // SELECT * FROM usuarios WHERE rol = ?
    List<Usuario> findByRol(String rol);
    
    // SELECT * FROM usuarios WHERE email = ? AND rol = ?
    Optional<Usuario> findByEmailAndRol(String email, String rol);
    
    // SELECT COUNT(*) FROM usuarios WHERE rol = ?
    long countByRol(String rol);
    
    // SELECT * FROM usuarios WHERE email LIKE %?%
    List<Usuario> findByEmailContaining(String texto);
}
```

**Palabras clave soportadas:**
- `findBy`, `getBy`, `queryBy`
- `And`, `Or`
- `Between`, `LessThan`, `GreaterThan`
- `Like`, `Containing`, `StartingWith`, `EndingWith`
- `OrderBy`

---

### **Consultas Personalizadas con @Query**

#### JPQL (Java Persistence Query Language)

```java
// Consulta simple
@Query("SELECT u FROM Usuario u WHERE u.email = :email")
Optional<Usuario> buscarPorEmail(@Param("email") String email);

// Consulta con JOIN
@Query("SELECT r FROM Reserva r JOIN FETCH r.usuario WHERE r.id = :id")
Optional<Reserva> findByIdWithUsuario(@Param("id") Long id);

// Consulta de agregación
@Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
long contarPorEstado(@Param("estado") EstadoReserva estado);

// Consulta con fechas
@Query("SELECT r FROM Reserva r WHERE r.fechaEntrada BETWEEN :inicio AND :fin")
List<Reserva> findEnRango(@Param("inicio") LocalDate inicio, 
                          @Param("fin") LocalDate fin);
```

#### SQL Nativo

```java
@Query(value = "SELECT * FROM usuarios WHERE email = ?1", nativeQuery = true)
Optional<Usuario> findByEmailNative(String email);
```

---

## 🎯 Ejemplo Completo: Entidad Reserva

```java
@Entity
@Table(name = "reservas")
@Data
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación N:1 con Usuario
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Relación N:1 con Habitacion
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;
    
    // Fechas (Java 8 Time API)
    @NotNull
    @Column(nullable = false)
    private LocalDate fechaEntrada;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate fechaSalida;
    
    // Decimal para dinero
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    // Enumeración
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    // Timestamp automático
    @Column(nullable = false)
    private LocalDateTime fechaReserva = LocalDateTime.now();
    
    public enum EstadoReserva {
        PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, NO_SHOW
    }
}
```

**SQL Generado:**
```sql
CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    fecha_reserva DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (habitacion_id) REFERENCES habitaciones(id)
);
```

---

## 🔐 Transacciones

### **@Transactional**

Garantiza operaciones ACID (Atomicidad, Consistencia, Aislamiento, Durabilidad)

```java
@Service
public class ReservaService {
    
    @Transactional  // Todo o nada
    public Reserva crearReserva(Long usuarioId, Long habitacionId, ...) {
        // 1. Buscar habitación
        Habitacion hab = habitacionRepo.findById(habitacionId)
            .orElseThrow(() -> new RuntimeException("No existe"));
        
        // 2. Verificar disponibilidad
        if (!hab.getDisponible()) {
            throw new RuntimeException("No disponible");
        }
        
        // 3. Crear reserva
        Reserva reserva = new Reserva();
        reserva.setHabitacion(hab);
        
        // 4. Actualizar habitación
        hab.setDisponible(false);
        habitacionRepo.save(hab);
        
        // 5. Guardar reserva
        return reservaRepo.save(reserva);
        
        // Si algo falla, TODO se revierte
    }
}
```

---

## 📊 Ciclo de Vida de Entidades

```
┌─────────────┐
│  TRANSIENT  │  ← new Usuario() (JPA no la conoce)
└──────┬──────┘
       │ save()
       ↓
┌─────────────┐
│   MANAGED   │  ← JPA la gestiona (cambios automáticos)
└──────┬──────┘
       │ 
       ↓
┌─────────────┐
│  DETACHED   │  ← Fuera del contexto de persistencia
└──────┬──────┘
       │ merge()
       ↓
┌─────────────┐
│   REMOVED   │  ← Marcada para eliminar
└─────────────┘
```

---

## ⚡ Optimizaciones

### **Problema N+1**

```java
// ❌ MAL - Genera 1 + N queries
List<Usuario> usuarios = usuarioRepo.findAll();
for (Usuario u : usuarios) {
    System.out.println(u.getReservas().size());  // Query por cada usuario!
}

// ✅ BIEN - Fetch Join (1 query)
@Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.reservas")
List<Usuario> findAllWithReservas();
```

### **Proyecciones**

```java
// Solo trae los campos necesarios
@Query("SELECT u.nombre, u.email FROM Usuario u WHERE u.rol = :rol")
List<Object[]> findNombresYEmailsPorRol(@Param("rol") String rol);
```

---

## 🎓 Resumen

### **Anotaciones Clave**
- `@Entity`, `@Table` → Define entidades
- `@Id`, `@GeneratedValue` → Claves primarias
- `@Column` → Configuración de columnas
- `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany` → Relaciones
- `@Enumerated` → Enumeraciones
- `@Query` → Consultas personalizadas

### **Parámetros Importantes**
- `cascade` → Propaga operaciones
- `fetch` → LAZY vs EAGER
- `mappedBy` → Lado inverso de relación
- `orphanRemoval` → Elimina huérfanos

### **Ventajas en tu Proyecto**
✅ No escribes SQL manualmente  
✅ Relaciones automáticas entre tablas  
✅ Validaciones integradas  
✅ Transacciones ACID  
✅ Repositorios con CRUD automático  
✅ Independencia de la base de datos  

---

**Proyecto:** Resort Eden  
**Spring Boot:** 3.5.6  
**Java:** 21  
**Base de Datos:** MySQL 8.x  
**Implementación JPA:** Hibernate 6.x
