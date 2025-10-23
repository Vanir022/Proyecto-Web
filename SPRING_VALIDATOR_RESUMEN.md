# 🔐 Spring Validator en Proyecto4 - Resort Eden

## 📌 ¿Qué es Spring Validator?

**Spring Validator** (Jakarta Bean Validation / Hibernate Validator) es un framework que permite **validar automáticamente** los datos de entrada en tu aplicación usando **anotaciones declarativas**.

### Ventajas:
✅ Validaciones automáticas sin código manual  
✅ Mensajes de error personalizados  
✅ Código más limpio y mantenible  
✅ Validaciones en múltiples capas (DTOs, Entidades, Parámetros)  

---

## 🔧 Configuración

### **Dependencia (pom.xml)**

```xml
<!-- Spring Boot Starter Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Incluye:**
- Jakarta Bean Validation API
- Hibernate Validator (implementación)
- Integración con Spring

---

## 📝 Anotaciones de Validación

### **Validaciones Comunes**

| Anotación | Descripción | Ejemplo |
|-----------|-------------|---------|
| `@NotNull` | No puede ser null | `@NotNull private Long id;` |
| `@NotBlank` | String no vacío ni null | `@NotBlank private String nombre;` |
| `@NotEmpty` | Colección/array no vacío | `@NotEmpty private List<String> items;` |
| `@Email` | Formato de email válido | `@Email private String email;` |
| `@Size` | Longitud de string/colección | `@Size(min=2, max=100)` |
| `@Min` / `@Max` | Valor mínimo/máximo | `@Min(1) private Integer cantidad;` |
| `@DecimalMin` / `@DecimalMax` | Decimal mínimo/máximo | `@DecimalMin("0.01")` |
| `@Pattern` | Expresión regular | `@Pattern(regexp="^[0-9]{8}$")` |
| `@Past` / `@Future` | Fecha pasada/futura | `@Past private LocalDate fecha;` |
| `@FutureOrPresent` | Fecha actual o futura | `@FutureOrPresent` |
| `@AssertTrue` / `@AssertFalse` | Boolean true/false | `@AssertTrue private Boolean terms;` |
| `@Valid` | Validación en cascada | `@Valid @RequestBody DTO dto` |
| `@Validated` | A nivel de clase | `@Validated class Controller` |

---

## 🎯 Uso en el Proyecto

### **1. Validaciones en Entidades**

#### Ejemplo: Usuario.java

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "Mínimo 6 caracteres")
    @Column(nullable = false, length = 255)
    private String password;
}
```

---

#### Ejemplo: Reserva.java

```java
@Entity
@Table(name = "reservas")
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La fecha de entrada es obligatoria")
    @FutureOrPresent(message = "Debe ser hoy o en el futuro")
    @Column(nullable = false)
    private LocalDate fechaEntrada;
    
    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Column(nullable = false)
    private Integer numeroHuespedes;
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "Debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @NotBlank(message = "El DNI del cliente es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
    @Column(nullable = false, length = 8)
    private String dniCliente;
}
```

---

#### Ejemplo: DetallesPersona.java

```java
@Entity
@Table(name = "detalles_persona")
public class DetallesPersona {
    
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;
    
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;
    
    @Pattern(regexp = "^[0-9]{7,20}$|^$", message = "Entre 7 y 20 dígitos")
    @Column(length = 20)
    private String telefono;
    
    @Past(message = "La fecha debe ser en el pasado")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}
```

---

### **2. Validaciones en DTOs**

#### Ejemplo: RegistroUsuarioDTO.java

```java
@Getter
@Setter
public class RegistroUsuarioDTO {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "Mínimo 6 caracteres")
    private String password;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
    private String firstName;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
    private String lastName;
    
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
    private String dni;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,20}$", message = "Entre 7 y 20 dígitos")
    private String phone;
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "Debe ser en el pasado")
    private LocalDate birthDate;
    
    @NotNull(message = "Debe aceptar los términos")
    @AssertTrue(message = "Debe aceptar los términos")
    private Boolean acceptTerms;
}
```

---

### **3. Validaciones en Controladores**

#### **REST API con @Valid**

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @Valid @RequestBody RegistroUsuarioDTO dto,
            BindingResult result) {
        
        // Si hay errores de validación
        if (result.hasErrors()) {
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    error -> error.getDefaultMessage()
                ));
            return ResponseEntity.badRequest().body(errores);
        }
        
        // Procesar registro
        Usuario usuario = usuarioService.registrar(dto);
        return ResponseEntity.ok(usuario);
    }
}
```

**Respuesta de error JSON:**
```json
{
    "email": "El email debe ser válido",
    "dni": "El DNI debe contener exactamente 8 dígitos",
    "password": "La contraseña debe tener al menos 6 caracteres"
}
```

---

#### **Formularios Web con @ModelAttribute**

```java
@Controller
@RequestMapping("/contactos")
public class ContactoController {
    
    @PostMapping("/enviar")
    public String enviarMensaje(
            @Valid @ModelAttribute Contacto contacto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        // Si hay errores de validación
        if (result.hasErrors()) {
            String errorMsg = result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Error de validación");
            
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/contactos";
        }
        
        // Guardar contacto
        contactoService.guardar(contacto);
        redirectAttributes.addFlashAttribute("success", "Mensaje enviado");
        return "redirect:/contactos";
    }
}
```

---

#### **Validación de Parámetros con @Validated**

```java
@Controller
@RequestMapping("/reservas")
@Validated  // ← Habilita validación de parámetros
public class ReservasController {
    
    @PostMapping("/crear")
    public String crearReserva(
            @NotNull(message = "La habitación es obligatoria")
            @RequestParam Long habitacionId,
            
            @NotNull(message = "La fecha de entrada es obligatoria")
            @FutureOrPresent(message = "Debe ser hoy o en el futuro")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate fechaEntrada,
            
            @NotBlank(message = "El DNI es obligatorio")
            @Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
            @RequestParam String dniCliente,
            
            @Min(value = 1, message = "Mínimo 1 huésped")
            @RequestParam Integer numeroHuespedes,
            
            RedirectAttributes redirectAttributes) {
        
        // Crear reserva
        reservaService.crear(habitacionId, fechaEntrada, dniCliente, numeroHuespedes);
        
        redirectAttributes.addFlashAttribute("success", "Reserva creada");
        return "redirect:/reservas";
    }
}
```

---

## 🛡️ Manejador Global de Excepciones

### **GlobalExceptionHandler.java**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Manejo de validaciones de objetos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Error de validación");
        response.put("errors", errores);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Manejo de validaciones de parámetros (@Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        Map<String, String> errores = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Error de validación en parámetros");
        response.put("errors", errores);
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

---

## 🔍 Validaciones Personalizadas

### **Expresiones Regulares Comunes**

```java
// DNI peruano (8 dígitos)
@Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
private String dni;

// Teléfono (7-20 dígitos)
@Pattern(regexp = "^[0-9]{7,20}$", message = "Entre 7 y 20 dígitos")
private String telefono;

// Código postal (5 dígitos)
@Pattern(regexp = "^[0-9]{5}$", message = "Código postal de 5 dígitos")
private String codigoPostal;

// Solo letras
@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Solo letras")
private String nombre;

// Alfanumérico
@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Solo letras y números")
private String username;
```

---

### **Validación Personalizada (Anotación Propia)**

#### 1. Crear la anotación

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MayorDeEdadValidator.class)
@Documented
public @interface MayorDeEdad {
    String message() default "Debe ser mayor de edad";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### 2. Implementar el validador

```java
public class MayorDeEdadValidator 
        implements ConstraintValidator<MayorDeEdad, LocalDate> {
    
    @Override
    public boolean isValid(LocalDate fechaNacimiento, 
                          ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            return true;
        }
        return Period.between(fechaNacimiento, LocalDate.now())
                     .getYears() >= 18;
    }
}
```

#### 3. Usar la anotación

```java
@MayorDeEdad(message = "Debe ser mayor de 18 años")
private LocalDate fechaNacimiento;
```

---

## 📊 Ejemplos por Tipo de Dato

### **Strings**

```java
@NotBlank(message = "El nombre es obligatorio")
@Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
private String nombre;

@Email(message = "Email inválido")
private String email;

@Pattern(regexp = "^[0-9]{8}$", message = "DNI de 8 dígitos")
private String dni;
```

### **Números**

```java
@NotNull(message = "El precio es obligatorio")
@DecimalMin(value = "0.01", message = "Debe ser mayor a 0")
@DecimalMax(value = "10000.00", message = "Máximo 10,000")
private BigDecimal precio;

@Min(value = 1, message = "Mínimo 1")
@Max(value = 10, message = "Máximo 10")
private Integer cantidad;
```

### **Fechas**

```java
@NotNull(message = "La fecha es obligatoria")
@Past(message = "Debe ser en el pasado")
private LocalDate fechaNacimiento;

@FutureOrPresent(message = "Debe ser hoy o futura")
private LocalDate fechaReserva;

@Future(message = "Debe ser en el futuro")
private LocalDateTime fechaEvento;
```

### **Booleanos**

```java
@NotNull(message = "Debe aceptar los términos")
@AssertTrue(message = "Debe ser verdadero")
private Boolean acceptTerms;

@AssertFalse(message = "No debe estar marcado")
private Boolean esSpam;
```

### **Colecciones**

```java
@NotEmpty(message = "Debe tener al menos un elemento")
@Size(min = 1, max = 5, message = "Entre 1 y 5 elementos")
private List<String> intereses;
```

---

## ⚡ Validación en Cascada

### **@Valid en Relaciones**

```java
@Entity
public class Usuario {
    
    @Valid  // ← Valida también DetallesPersona
    @OneToOne(cascade = CascadeType.ALL)
    private DetallesPersona detallesPersona;
}

// Al guardar Usuario, también valida DetallesPersona
usuarioRepository.save(usuario);
```

---

## 🎯 Mejores Prácticas

### ✅ **DO (Hacer)**

```java
// Mensajes claros y específicos
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email debe tener un formato válido")
private String email;

// Validar en DTOs, no en controladores
@Valid @RequestBody RegistroUsuarioDTO dto

// Usar @Validated en clase para parámetros
@Validated
public class ReservasController { }
```

### ❌ **DON'T (No hacer)**

```java
// Mensajes genéricos
@NotBlank  // Sin mensaje personalizado

// Validaciones manuales en controlador
if (email == null || email.isEmpty()) { }

// Olvidar @Valid
@RequestBody RegistroUsuarioDTO dto  // No valida!
```

---

## 🎓 Resumen

### **¿Qué hace Spring Validator en tu proyecto?**

1. **Valida automáticamente** datos de entrada
2. **Reduce código** manual de validación
3. **Proporciona mensajes** claros de error
4. **Funciona en múltiples capas**: Entidades, DTOs, Parámetros
5. **Se integra con Spring Boot** sin configuración adicional

### **Flujo de validación:**

```
Usuario envía datos → @Valid detecta → Spring valida → 
Si hay errores → BindingResult → Retorna errores →
Si no hay errores → Procesa la petición
```

### **Entidades con validaciones en tu proyecto:**
✅ Usuario  
✅ DetallesPersona  
✅ Contacto  
✅ Reserva  
✅ Habitacion  
✅ Administrador  
✅ RegistroUsuarioDTO  

---

**Proyecto:** Resort Eden  
**Spring Boot:** 3.5.6  
**Java:** 21  
**Validación:** Hibernate Validator 8.x
