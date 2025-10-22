# 💡 Ejemplos Prácticos de Spring Validator

## 🎯 Cómo Usar las Validaciones

### Ejemplo 1: Registro de Usuario (REST API)

#### Request JSON:
```json
{
  "email": "usuario@hotel.com",
  "password": "password123",
  "confirmPassword": "password123",
  "firstName": "Juan",
  "lastName": "Pérez",
  "dni": "12345678",
  "phone": "987654321",
  "birthDate": "1990-05-15",
  "acceptTerms": true,
  "acceptMarketing": false
}
```

#### ✅ Respuesta Exitosa:
```json
{
  "message": "Usuario registrado exitosamente",
  "email": "usuario@hotel.com",
  "nombre": "Juan Pérez"
}
```

#### ❌ Respuesta con Errores:
```json
{
  "status": "error",
  "message": "Error de validación",
  "errors": {
    "email": "El email debe ser válido",
    "dni": "El DNI debe contener exactamente 8 dígitos",
    "acceptTerms": "Debe aceptar los términos y condiciones"
  }
}
```

---

### Ejemplo 2: Formulario de Contacto

#### Datos del Formulario:
```html
<form method="POST" action="/contactos/enviar">
  <input name="nombre" value="María López">
  <input name="email" value="maria@email.com">
  <input name="telefono" value="999888777">
  <input name="hotel" value="Aranwa Cusco">
  <textarea name="mensaje">Me gustaría información sobre...</textarea>
  <button type="submit">Enviar</button>
</form>
```

#### ✅ Si es válido:
Redirige a `/contactos` con mensaje:
```
¡Gracias por contactarnos! Hemos recibido tu mensaje.
```

#### ❌ Si hay errores:
Redirige a `/contactos` con mensaje:
```
El mensaje debe tener entre 10 y 5000 caracteres
```

---

### Ejemplo 3: Crear Reserva

#### Request:
```
POST /reservas/crear
habitacionId: 1
fechaEntrada: 2025-10-25
fechaSalida: 2025-10-28
numeroHuespedes: 2
dniCliente: 87654321
telefonoContacto: 987123456
comentarios: Habitación con vista al mar
```

#### ✅ Validaciones que se ejecutan automáticamente:
- `habitacionId`: No puede ser null
- `fechaEntrada`: Debe ser hoy o en el futuro
- `fechaSalida`: Debe ser hoy o en el futuro
- `numeroHuespedes`: Debe ser al menos 1
- `dniCliente`: Debe ser exactamente 8 dígitos

#### ❌ Ejemplo de error:
Si `dniCliente = "123"` (menos de 8 dígitos):
```
Redirige con error: "El DNI debe contener exactamente 8 dígitos"
```

---

## 🔍 Validaciones Comunes

### Validación de Email
```java
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email debe ser válido")
private String email;
```

**Acepta:**
- `usuario@dominio.com` ✅
- `nombre.apellido@empresa.com.pe` ✅

**Rechaza:**
- `usuario@` ❌
- `@dominio.com` ❌
- `usuariodominio.com` ❌

---

### Validación de DNI (Perú)
```java
@NotBlank(message = "El DNI es obligatorio")
@Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener exactamente 8 dígitos")
private String dni;
```

**Acepta:**
- `12345678` ✅
- `87654321` ✅

**Rechaza:**
- `1234567` ❌ (7 dígitos)
- `123456789` ❌ (9 dígitos)
- `1234567A` ❌ (letras)
- `` ❌ (vacío)

---

### Validación de Teléfono
```java
@Pattern(regexp = "^[0-9]{7,20}$|^$", message = "El teléfono debe contener entre 7 y 20 dígitos")
private String telefono;
```

**Acepta:**
- `987654321` ✅ (9 dígitos)
- `01234567` ✅ (8 dígitos)
- `5551234567890` ✅ (internacional)
- `` ✅ (vacío, es opcional)

**Rechaza:**
- `12345` ❌ (muy corto)
- `987-654-321` ❌ (con guiones)
- `+51987654321` ❌ (con +)

---

### Validación de Fechas
```java
@NotNull(message = "La fecha de entrada es obligatoria")
@FutureOrPresent(message = "La fecha de entrada debe ser hoy o en el futuro")
private LocalDate fechaEntrada;
```

**Acepta:**
- `2025-10-22` ✅ (hoy)
- `2025-10-25` ✅ (futuro)
- `2026-01-01` ✅ (futuro)

**Rechaza:**
- `2025-10-21` ❌ (pasado)
- `2020-01-01` ❌ (pasado)
- `null` ❌ (vacío)

---

### Validación de Precio/Monto
```java
@NotNull(message = "El precio es obligatorio")
@DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
private BigDecimal precio;
```

**Acepta:**
- `150.00` ✅
- `0.01` ✅
- `9999.99` ✅

**Rechaza:**
- `0.00` ❌
- `-10.00` ❌
- `null` ❌

---

### Validación de Contraseña
```java
@NotBlank(message = "La contraseña es obligatoria")
@Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
private String password;
```

**Acepta:**
- `password123` ✅ (más de 6)
- `MiContraseñaSegura123!` ✅

**Rechaza:**
- `12345` ❌ (menos de 6)
- `abc` ❌ (menos de 6)
- `` ❌ (vacío)

---

## 🧪 Pruebas Recomendadas

### Test 1: Email inválido
```bash
curl -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalido@",
    "password": "pass123",
    "confirmPassword": "pass123",
    "firstName": "Test",
    "lastName": "User",
    "dni": "12345678",
    "phone": "987654321",
    "birthDate": "1990-01-01",
    "acceptTerms": true
  }'
```

**Respuesta esperada:** Error de validación en email

---

### Test 2: DNI inválido
```bash
curl -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@hotel.com",
    "password": "pass123",
    "confirmPassword": "pass123",
    "firstName": "Test",
    "lastName": "User",
    "dni": "123",
    "phone": "987654321",
    "birthDate": "1990-01-01",
    "acceptTerms": true
  }'
```

**Respuesta esperada:** Error "El DNI debe contener exactamente 8 dígitos"

---

### Test 3: Contraseña muy corta
```bash
curl -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@hotel.com",
    "password": "123",
    "confirmPassword": "123",
    "firstName": "Test",
    "lastName": "User",
    "dni": "12345678",
    "phone": "987654321",
    "birthDate": "1990-01-01",
    "acceptTerms": true
  }'
```

**Respuesta esperada:** Error "La contraseña debe tener al menos 6 caracteres"

---

## 📝 Tips de Uso

### 1. Siempre usar `@Valid` en controladores REST
```java
@PostMapping("/api/recurso")
public ResponseEntity<?> crear(@Valid @RequestBody MiDTO dto, BindingResult result) {
    if (result.hasErrors()) {
        // Manejar errores
    }
}
```

### 2. Usar `@Validated` para validar parámetros individuales
```java
@Controller
@Validated
public class MiController {
    @GetMapping("/buscar")
    public String buscar(
        @Min(1) @RequestParam Integer id,
        @Email @RequestParam String email
    ) {
        // Lógica
    }
}
```

### 3. Capturar errores con `BindingResult`
```java
if (bindingResult.hasErrors()) {
    String error = bindingResult.getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .findFirst()
        .orElse("Error de validación");
    return error;
}
```

### 4. Usar `RedirectAttributes` en formularios web
```java
@PostMapping("/formulario")
public String submit(@Valid @ModelAttribute MiModelo modelo,
                    BindingResult result,
                    RedirectAttributes attr) {
    if (result.hasErrors()) {
        attr.addFlashAttribute("error", result.getFieldError().getDefaultMessage());
        return "redirect:/formulario";
    }
}
```

---

## 🎨 Mejores Prácticas

1. **Mensajes claros y en español**: Todos los mensajes deben ser comprensibles para el usuario final

2. **Validar en capas**: Validar en controlador Y en modelo

3. **No confiar solo en frontend**: Siempre validar en backend

4. **Usar patrones comunes**: Para DNI, teléfono, email, etc.

5. **Documentar reglas de negocio**: Explicar por qué ciertas validaciones existen

---

## 🚀 Próximos Pasos

- [ ] Agregar validaciones personalizadas si es necesario
- [ ] Crear tests unitarios para las validaciones
- [ ] Implementar validaciones en más formularios
- [ ] Documentar reglas de validación en el README

---

**Proyecto:** Proyecto4 - Resort Eden  
**Fecha:** 22 de octubre de 2025
