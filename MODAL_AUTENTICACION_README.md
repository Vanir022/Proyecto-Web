# Sistema de Modal de Autenticación - Eden Resorts

## Descripción
Este sistema intercepta automáticamente los clics en botones y enlaces relacionados con reservas y servicios, mostrando un modal elegante que solicita al usuario iniciar sesión si no está autenticado.

## Características Principales

### 🔒 Protección Automática
- Detecta automáticamente botones de reservas y servicios
- Intercinta acciones que requieren autenticación
- Muestra modal informativo antes de redirigir al login

### 🎨 Diseño Elegante
- Modal responsive con estilo Eden Resorts
- Animaciones suaves y efectos visuales
- Indicadores opcionales en elementos protegidos

### 🚀 Funcionalidad Completa
- Verificación de estado de autenticación en tiempo real
- Protección basada en selectores CSS y contenido de texto
- Integración con Spring Security

## Archivos Implementados

### JavaScript Principal
- `/js/auth-modal.js` - Lógica principal del modal y protección

### CSS de Estilo
- `/css/auth-modal.css` - Estilos personalizados para el modal

### Páginas Actualizadas
- `index.html` - Página principal
- `html/Reservas.html` - Página de reservas de habitaciones
- `html/Servicios/Spa.html` - Servicios de spa
- `html/Servicios/Bodas.html` - Servicios de bodas
- `html/Servicios/Eventos.html` - Servicios de eventos corporativos

## Elementos Protegidos

### Selectores CSS Automáticos
- `a[href*="/reservas"]` - Enlaces a reservas
- `a[href*="tipoReserva="]` - Enlaces con parámetros de reserva
- `button[onclick*="reservar"]` - Botones de reserva
- `button[onclick*="solicitar"]` - Botones de solicitudes
- `button[onclick*="cotizar"]` - Botones de cotización
- `a.price-wedding` - Enlaces de precios de bodas

### Palabras Clave de Protección
- "reservar", "reserva"
- "confirmar reserva"
- "cotizar", "solicitar"
- "desde s/", "precio:"
- "planifica tu evento"

## Uso Avanzado

### Proteger Elementos Específicos
```javascript
// Proteger un formulario específico
protectForm('miFormulario');

// Proteger botones con selector específico
protectButton('.mi-boton-especial');

// Verificar autenticación antes de una acción
if (requireAuth(() => {
    // Código a ejecutar si está autenticado
    console.log('Usuario autenticado, proceder');
})) {
    // El usuario está autenticado
}
```

### Actualizar Estado de Autenticación
```javascript
// Actualizar después de login exitoso
window.authModal.updateAuthStatus(true);

// Actualizar después de logout
window.authModal.updateAuthStatus(false);
```

## Configuración de Spring Security

El sistema se integra automáticamente con Spring Security verificando la presencia del elemento `#userDropdown` en el DOM, que indica que el usuario está autenticado.

## Personalización

### Modificar Texto del Modal
Editar el archivo `/js/auth-modal.js` en la función `initModal()`.

### Agregar Nuevos Elementos Protegidos
Modificar el array `protectedSelectors` en el método `handleClick()`.

### Personalizar Estilos
Modificar el archivo `/css/auth-modal.css` para cambiar colores, animaciones y efectos.

## Notas Técnicas

- Compatible con Bootstrap 5.3.0
- Requiere Font Awesome 5 para iconos
- Se inicializa automáticamente al cargar la página
- Funciona en dispositivos móviles y desktop

## Testing

Para probar el sistema:
1. Cerrar sesión en la aplicación
2. Intentar hacer clic en cualquier botón de "Reservar" o "Cotizar"
3. Verificar que aparece el modal
4. Hacer clic en "Iniciar Sesión" para ser redirigido al login

## Soporte

El sistema es completamente automático y no requiere configuración adicional. Se integra con el sistema de autenticación existente de Spring Security.