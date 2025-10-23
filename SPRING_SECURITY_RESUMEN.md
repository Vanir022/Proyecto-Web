# 🔐 Spring Security en Proyecto4 - Resort Eden

## 📌 ¿Qué es Spring Security?

**Spring Security** es el framework de seguridad estándar para aplicaciones Spring Boot. Proporciona **autenticación**, **autorización** y **protección contra ataques comunes**.

### Funcionalidades principales:
✅ **Autenticación** - Verificar la identidad del usuario  
✅ **Autorización** - Control de acceso basado en roles  
✅ **Protección CSRF** - Contra ataques Cross-Site Request Forgery  
✅ **Encriptación de contraseñas** - BCrypt  
✅ **Gestión de sesiones** - Control de sesiones de usuario  

---

## 🔧 Configuración

### **Dependencias (pom.xml)**

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Para encriptar contraseñas con BCrypt -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- Thymeleaf + Spring Security -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

---

## 🛠️ Componentes de Spring Security

### **Arquitectura de Seguridad**

```
┌─────────────────────────────────────────┐
│         Usuario hace request            │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│     SecurityFilterChain                 │
│  (Filtros de seguridad de Spring)       │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    AuthenticationManager                │
│  (Gestiona el proceso de autenticación) │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    UserDetailsService                   │
│  (Carga datos del usuario desde la BD)  │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    PasswordEncoder (BCrypt)             │
│  (Verifica la contraseña encriptada)    │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│  Usuario autenticado → Acceso concedido │
└─────────────────────────────────────────┘
```

---

## 🔐 SecurityConfig.java

### **Configuración Principal**

```java
@Configuration
public class SecurityConfig {
    
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivar CSRF (para APIs REST)
            .csrf(csrf -> csrf.disable())
            
            // Configurar autorización de rutas
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (sin autenticación)
                .requestMatchers("/", "/nosotros", "/contactos", 
                                "/login", "/register").permitAll()
                .requestMatchers("/css/**", "/js/**", "/imagenes/**").permitAll()
                .requestMatchers("/auth/registro", "/auth/welcome").permitAll()
                .requestMatchers("/reservas", "/reservas/buscar").permitAll()
                
                // Rutas protegidas por rol ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Rutas que requieren autenticación (cualquier rol)
                .requestMatchers("/api/perfil/**").authenticated()
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar formulario de login
            .formLogin(form -> form
                .loginPage("/login")              // Página personalizada
                .loginProcessingUrl("/login")     // URL que procesa el login
                .usernameParameter("email")       // Campo de email
                .passwordParameter("password")    // Campo de contraseña
                .successHandler(successHandler)   // Handler personalizado
                .failureUrl("/login?error=true")  // Si falla
                .permitAll()
            )
            
            // Configurar logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)      // Invalida sesión
                .deleteCookies("JSESSIONID")      // Elimina cookies
                .permitAll()
            );
        
        return http.build();
    }

    // Bean para encriptar contraseñas
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para gestionar autenticación
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## 🔑 Autenticación

### **1. UserDetailsService (Cargar usuario desde BD)**

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) 
            throws UsernameNotFoundException {
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + email));
        
        // Convertir a UserDetails de Spring Security
        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPassword())  // Ya debe estar encriptada
            .roles(usuario.getRol().replace("ROLE_", ""))  // ADMIN, USER
            .build();
    }
}
```

**¿Qué hace?**
- Spring Security llama este método cuando un usuario intenta hacer login
- Busca el usuario en la base de datos por email
- Convierte el `Usuario` de tu aplicación a `UserDetails` de Spring Security
- Spring Security compara la contraseña automáticamente

---

### **2. Encriptación de Contraseñas (BCrypt)**

```java
@Service
public class UsuarioService {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public Usuario registrar(Usuario usuario) {
        // Encriptar contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("ROLE_USER");  // Rol por defecto
        
        return usuarioRepository.save(usuario);
    }
    
    public boolean verificarPassword(String passwordPlano, String passwordEncriptado) {
        // Verificar si la contraseña coincide
        return passwordEncoder.matches(passwordPlano, passwordEncriptado);
    }
}
```

**BCrypt:**
- Algoritmo de encriptación de una sola vía (no se puede desencriptar)
- Incluye "salt" automático (protección contra rainbow tables)
- Ejemplo: `password123` → `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

---

### **3. Handler de Éxito Personalizado**

```java
@Component
public class CustomAuthenticationSuccessHandler 
        extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        
        // Redirigir según el rol del usuario
        String redirectUrl = "/dashboard";  // Por defecto
        
        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            redirectUrl = "/admin/dashboard";
        } else if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            redirectUrl = "/dashboard";
        }
        
        // Limpiar y redirigir
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
```

**¿Qué hace?**
- Se ejecuta después de un login exitoso
- Redirige a diferentes páginas según el rol
- ADMIN → `/admin/dashboard`
- USER → `/dashboard`

---

## 🛡️ Autorización (Control de Acceso)

### **Configuración de Rutas**

```java
.authorizeHttpRequests(auth -> auth
    // ========== RUTAS PÚBLICAS ==========
    .requestMatchers("/", "/nosotros", "/contactos").permitAll()
    .requestMatchers("/login", "/register").permitAll()
    .requestMatchers("/css/**", "/js/**", "/imagenes/**").permitAll()
    .requestMatchers("/auth/registro").permitAll()
    
    // ========== RUTAS PROTEGIDAS POR ROL ==========
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    
    // ========== RUTAS QUE REQUIEREN AUTENTICACIÓN ==========
    .requestMatchers("/api/perfil/**").authenticated()
    .requestMatchers("/reservas/mis-reservas").authenticated()
    
    // ========== TODAS LAS DEMÁS ==========
    .anyRequest().authenticated()
)
```

### **Métodos de Autorización**

| Método | Descripción |
|--------|-------------|
| `.permitAll()` | Acceso público (sin login) |
| `.authenticated()` | Requiere estar autenticado (cualquier rol) |
| `.hasRole("ADMIN")` | Solo usuarios con rol ADMIN |
| `.hasAnyRole("ADMIN", "USER")` | ADMIN o USER |
| `.hasAuthority("ROLE_ADMIN")` | Autoridad específica |
| `.denyAll()` | Niega todo acceso |

---

## 🔒 Anotaciones de Seguridad en Métodos

### **@PreAuthorize (Antes de ejecutar el método)**

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    // Solo ADMIN puede acceder
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioService.findAll();
    }
    
    // Solo el dueño o ADMIN pueden acceder
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    @GetMapping("/perfil/{email}")
    public Usuario verPerfil(@PathVariable String email) {
        return usuarioService.findByEmail(email);
    }
}
```

### **@Secured (Requiere rol específico)**

```java
@Secured("ROLE_ADMIN")
@DeleteMapping("/usuarios/{id}")
public void eliminarUsuario(@PathVariable Long id) {
    usuarioService.eliminar(id);
}
```

---

## 🎭 Uso en Thymeleaf

### **Mostrar contenido según autenticación**

```html
<!-- Mostrar si está autenticado -->
<div sec:authorize="isAuthenticated()">
    <p>Bienvenido, <span sec:authentication="name"></span></p>
</div>

<!-- Mostrar si NO está autenticado -->
<div sec:authorize="!isAuthenticated()">
    <a href="/login">Iniciar Sesión</a>
</div>

<!-- Mostrar solo para ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/dashboard">Panel de Administración</a>
</div>

<!-- Mostrar solo para USER -->
<div sec:authorize="hasRole('USER')">
    <a href="/dashboard">Mi Dashboard</a>
</div>

<!-- Obtener datos del usuario autenticado -->
<p>Email: <span sec:authentication="principal.username"></span></p>
<p>Rol: <span sec:authentication="principal.authorities"></span></p>
```

---

## 🚪 Login y Logout

### **Formulario de Login (HTML)**

```html
<form th:action="@{/login}" method="post">
    <!-- Email -->
    <input type="email" name="email" placeholder="Email" required>
    
    <!-- Contraseña -->
    <input type="password" name="password" placeholder="Contraseña" required>
    
    <!-- Botón de submit -->
    <button type="submit">Iniciar Sesión</button>
    
    <!-- Mensaje de error -->
    <div th:if="${param.error}">
        <p style="color: red;">Credenciales incorrectas</p>
    </div>
    
    <!-- Mensaje de logout -->
    <div th:if="${param.logout}">
        <p style="color: green;">Has cerrado sesión correctamente</p>
    </div>
</form>
```

**Importante:**
- El `name="email"` debe coincidir con `.usernameParameter("email")`
- El `name="password"` debe coincidir con `.passwordParameter("password")`
- Spring Security procesa automáticamente el formulario

---

### **Botón de Logout**

```html
<!-- Opción 1: Enlace simple -->
<a th:href="@{/logout}">Cerrar Sesión</a>

<!-- Opción 2: Formulario (más seguro) -->
<form th:action="@{/logout}" method="post">
    <button type="submit">Cerrar Sesión</button>
</form>
```

---

## 👤 Obtener Usuario Autenticado en Controladores

### **Método 1: Con Principal**

```java
@GetMapping("/dashboard")
public String dashboard(Principal principal, Model model) {
    String email = principal.getName();  // Email del usuario
    Usuario usuario = usuarioService.findByEmail(email);
    model.addAttribute("usuario", usuario);
    return "dashboard";
}
```

### **Método 2: Con @AuthenticationPrincipal**

```java
@GetMapping("/perfil")
public String perfil(@AuthenticationPrincipal UserDetails userDetails, 
                    Model model) {
    String email = userDetails.getUsername();
    Usuario usuario = usuarioService.findByEmail(email);
    model.addAttribute("usuario", usuario);
    return "perfil";
}
```

### **Método 3: Con SecurityContextHolder**

```java
@GetMapping("/mis-reservas")
public String misReservas(Model model) {
    Authentication auth = SecurityContextHolder.getContext()
                                               .getAuthentication();
    String email = auth.getName();
    
    Usuario usuario = usuarioService.findByEmail(email);
    List<Reserva> reservas = reservaService.findByUsuario(usuario);
    
    model.addAttribute("reservas", reservas);
    return "mis-reservas";
}
```

---

## 🛡️ Protección CSRF

### **¿Qué es CSRF?**

**Cross-Site Request Forgery** - Un atacante engaña al usuario para que ejecute acciones no deseadas en una aplicación donde está autenticado.

### **Configuración en tu proyecto**

```java
// CSRF desactivado (para APIs REST)
.csrf(csrf -> csrf.disable())
```

**¿Cuándo desactivar CSRF?**
- ✅ APIs REST stateless (sin sesiones)
- ✅ APIs que usan tokens JWT

**¿Cuándo mantener CSRF activo?**
- ✅ Aplicaciones web tradicionales con sesiones
- ✅ Formularios HTML que modifican datos

### **Uso con CSRF activado**

```html
<!-- Thymeleaf incluye automáticamente el token CSRF -->
<form th:action="@{/reservas/crear}" method="post">
    <!-- Spring Security inyecta automáticamente: -->
    <!-- <input type="hidden" name="_csrf" value="token..."/> -->
    
    <input type="text" name="habitacionId">
    <button type="submit">Reservar</button>
</form>
```

---

## 🔐 Roles en tu Proyecto

### **Estructura de Roles**

```java
// En Usuario.java
@Column(nullable = false, length = 255)
private String rol;  // "ROLE_USER" o "ROLE_ADMIN"
```

### **Asignación de Roles**

```java
// Registro normal
usuario.setRol("ROLE_USER");

// Crear administrador
usuario.setRol("ROLE_ADMIN");
```

### **Verificación de Roles en Código**

```java
@Service
public class ReservaService {
    
    public List<Reserva> listarReservas(String email) {
        Authentication auth = SecurityContextHolder.getContext()
                                                   .getAuthentication();
        
        // Verificar si es ADMIN
        boolean esAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (esAdmin) {
            // ADMIN ve todas las reservas
            return reservaRepository.findAll();
        } else {
            // USER solo ve sus reservas
            Usuario usuario = usuarioService.findByEmail(email);
            return reservaRepository.findByUsuario(usuario);
        }
    }
}
```

---

## ⚙️ Gestión de Sesiones

### **Configuración de Sesión**

```java
.sessionManagement(session -> session
    .maximumSessions(1)  // Solo 1 sesión activa por usuario
    .maxSessionsPreventsLogin(true)  // Bloquea nuevos logins
    .expiredUrl("/login?expired=true")  // Redirige si expira
)
```

### **Configuración en application.properties**

```properties
# Timeout de sesión (30 minutos)
server.servlet.session.timeout=30m

# Cookie de sesión
server.servlet.session.cookie.name=JSESSIONID
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false  # true en producción (HTTPS)
```

---

## 🔄 Flujo Completo de Autenticación

```
1. Usuario visita /login
   ↓
2. Ingresa email y password
   ↓
3. Spring Security intercepta el formulario
   ↓
4. AuthenticationManager procesa la autenticación
   ↓
5. UserDetailsService busca el usuario en la BD
   ↓
6. PasswordEncoder verifica la contraseña encriptada
   ↓
7. ✅ Si es correcta:
   - Crea Authentication object
   - Guarda en SecurityContext
   - CustomSuccessHandler redirige según rol
   ↓
8. ❌ Si es incorrecta:
   - Redirige a /login?error=true
```

---

## 📊 Resumen de Componentes

| Componente | Responsabilidad |
|------------|----------------|
| `SecurityConfig` | Configuración principal de seguridad |
| `UserDetailsService` | Carga usuario desde BD |
| `PasswordEncoder` | Encripta y verifica contraseñas |
| `AuthenticationManager` | Gestiona autenticación |
| `CustomSuccessHandler` | Maneja login exitoso |
| `SecurityFilterChain` | Cadena de filtros de seguridad |

---

## ✅ Mejores Prácticas

### ✅ **DO (Hacer)**

```java
// Encriptar contraseñas SIEMPRE
usuario.setPassword(passwordEncoder.encode(password));

// Usar roles con prefijo ROLE_
usuario.setRol("ROLE_USER");

// Invalidar sesión al hacer logout
.invalidateHttpSession(true)

// Usar HTTPS en producción
.requiresSecure()
```

### ❌ **DON'T (No hacer)**

```java
// Guardar contraseñas en texto plano
usuario.setPassword(password);  // ❌ NUNCA

// Comparar contraseñas directamente
if (password.equals(usuario.getPassword()))  // ❌ INCORRECTO

// Desactivar seguridad en producción
.permitAll()  // ❌ Cuidado con esto
```

---

## 🎓 Conclusión

**Spring Security en tu proyecto proporciona:**

✅ **Autenticación robusta** con BCrypt  
✅ **Control de acceso** basado en roles (ADMIN, USER)  
✅ **Protección de rutas** públicas y privadas  
✅ **Redirección personalizada** según rol  
✅ **Gestión de sesiones** segura  
✅ **Integración con Thymeleaf** para mostrar contenido condicional  

**Flujo típico:**
```
Usuario → Login → UserDetailsService → BD → 
PasswordEncoder → Autenticación → SecurityContext → 
Acceso concedido
```

---

**Proyecto:** Resort Eden  
**Spring Boot:** 3.5.6  
**Java:** 21  
**Spring Security:** 6.x
