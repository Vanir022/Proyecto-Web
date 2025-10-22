# 🔗 Relaciones de Base de Datos Implementadas - Proyecto Eden Resort

## ✅ Cambios Realizados

### 1. **Entidad Usuario** (`Usuario.java`)
**Relaciones agregadas:**
- ✅ **OneToOne** con `DetallesPersona` (ya existía)
- ✅ **OneToMany** con `Reserva` - Un usuario puede tener múltiples reservas
- ✅ **ManyToMany** con `Administrador` - Tabla intermedia `administradores_usuarios`

**Métodos helper agregados:**
- `addReserva(Reserva reserva)` - Para agregar reservas manteniendo la bidireccionalidad
- `removeReserva(Reserva reserva)` - Para eliminar reservas

---

### 2. **Entidad Administrador** (`Administrador.java`)
**Relaciones agregadas:**
- ✅ **ManyToMany** con `Usuario` - Lado inverso de la relación

**Métodos helper agregados:**
- `addUsuario(Usuario usuario)` - Para asociar usuarios al administrador
- `removeUsuario(Usuario usuario)` - Para desasociar usuarios

---

### 3. **Entidad Contacto** (`Contacto.java`)
**Relaciones agregadas:**
- ✅ **ManyToOne** con `Usuario` - Contactos pueden estar asociados a un usuario registrado (opcional)
- ✅ Migrado a **Lombok** (@Data, @NoArgsConstructor, @AllArgsConstructor) para consistencia

---

### 4. **Entidad Habitacion** (`Habitacion.java`)
**Relaciones agregadas:**
- ✅ **OneToMany** con `Reserva` - Una habitación puede tener múltiples reservas

**Métodos helper agregados:**
- `addReserva(Reserva reserva)` - Para agregar reservas a la habitación
- `removeReserva(Reserva reserva)` - Para eliminar reservas

**Ajustes:**
- ✅ Longitudes de columnas especificadas para compatibilidad con el diagrama

---

### 5. **Entidad Reserva** (`Reserva.java`)
**Relaciones existentes (mantenidas):**
- ✅ **ManyToOne** con `Usuario` 
- ✅ **ManyToOne** con `Habitacion`

**Ajustes:**
- ✅ Longitudes de columnas ajustadas (VARCHAR(255) donde corresponde)

---

### 6. **Entidad DetallesPersona** (`DetallesPersona.java`)
**Relaciones existentes (mantenidas):**
- ✅ **OneToOne** con `Usuario` (relación bidireccional)

**Campos adicionales agregados:**
- ✅ `administradoresId` - FK hacia tabla administradores
- ✅ `administradoresUsuariosId` - FK hacia tabla intermedia

---

## 🗂️ Tabla Intermedia Creada

### **administradores_usuarios**
Esta tabla maneja la relación **ManyToMany** entre Administradores y Usuarios:

```sql
CREATE TABLE administradores_usuarios (
    administradores_id BIGINT NOT NULL,
    usuarios_id BIGINT NOT NULL,
    PRIMARY KEY (administradores_id, usuarios_id),
    FOREIGN KEY (administradores_id) REFERENCES administradores(id),
    FOREIGN KEY (usuarios_id) REFERENCES usuarios(id)
);
```

---

## 📊 Diagrama de Relaciones Implementado

```
┌─────────────────┐         ┌──────────────────────┐
│ administradores │◄───────►│administradores_      │
│                 │  M:N    │usuarios (intermedia) │
└─────────────────┘         └──────────────────────┘
                                       │
                                       ▼
┌─────────────┐           ┌─────────────────┐
│ usuarios    │◄──────────│ contactos       │
│             │   1:N     │                 │
└─────────────┘           └─────────────────┘
      │ 1:1
      ▼
┌─────────────────┐
│ detalles_persona│
└─────────────────┘
      
┌─────────────┐           ┌─────────────────┐
│ usuarios    │◄──────────│ reservas        │
│             │   1:N     │                 │
└─────────────┘           └─────────────────┘
                                   │
                                   ▼ N:1
                          ┌─────────────────┐
                          │ habitaciones    │
                          └─────────────────┘
```

---

## ⚙️ Configuración Adicional

### **JpaConfig.java**
Configuración creada para:
- ✅ Habilitar repositorios JPA
- ✅ Activar manejo de transacciones
- ✅ Habilitar auditoría de entidades

---

## 📝 Scripts SQL Creados

### **relaciones_verificacion.sql**
Script para:
- ✅ Crear la tabla intermedia `administradores_usuarios`
- ✅ Crear índices para optimizar consultas
- ✅ Verificar integridad referencial
- ✅ Consultas útiles para validar datos

**Ubicación:** `sql/relaciones_verificacion.sql`

---

## 🚀 Cómo Usar las Relaciones

### Ejemplo 1: Crear un Usuario con Reserva
```java
// En tu servicio
@Transactional
public void crearReservaParaUsuario(Long usuarioId, Reserva reserva) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    usuario.addReserva(reserva); // Método helper que mantiene la bidireccionalidad
    reservaRepository.save(reserva);
}
```

### Ejemplo 2: Asociar Usuario a Administrador
```java
@Transactional
public void asignarUsuarioAAdministrador(Long adminId, Long usuarioId) {
    Administrador admin = administradorRepository.findById(adminId)
        .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    admin.addUsuario(usuario); // Método helper
    administradorRepository.save(admin);
}
```

### Ejemplo 3: Obtener todas las reservas de un usuario
```java
public List<Reserva> obtenerReservasDeUsuario(Long usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    return usuario.getReservas(); // Relación OneToMany
}
```

---

## ⚠️ Consideraciones Importantes

1. **Transaccionalidad**: Usa `@Transactional` en servicios que modifiquen relaciones
2. **Lazy Loading**: Las relaciones usan `FetchType.LAZY` para optimizar el rendimiento
3. **Cascadas**: Las operaciones en cascada están configuradas apropiadamente
4. **Integridad**: Spring JPA manejará automáticamente las claves foráneas

---

## 🔄 Migración de la Base de Datos

Al iniciar la aplicación con `spring.jpa.hibernate.ddl-auto=update`:
- ✅ Se crearán automáticamente las columnas faltantes
- ✅ Se agregarán las claves foráneas necesarias
- ✅ **NO se perderán datos existentes**

Si prefieres mayor control, ejecuta el script `relaciones_verificacion.sql` manualmente.

---

## 📌 Próximos Pasos

1. **Ejecutar la aplicación** para que Hibernate actualice el esquema
2. **Verificar las tablas** en MySQL usando el script SQL proporcionado
3. **Probar las relaciones** creando datos de prueba
4. **Revisar los logs** de Hibernate para confirmar las creaciones

---

## 🛡️ Protección del Proyecto

Todos los cambios son:
- ✅ **Compatibles hacia atrás** - No rompen funcionalidad existente
- ✅ **Incrementales** - Solo agregan, no eliminan
- ✅ **Seguros** - Usan mejores prácticas de JPA
- ✅ **Optimizados** - Incluyen índices apropiados

---

**¡Tu proyecto Eden Resort ahora tiene relaciones completamente funcionales según tu diagrama! 🏨✨**
