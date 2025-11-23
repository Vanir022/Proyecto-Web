/* Script de prueba para el Modal de Autenticación */

// Función para simular diferentes estados de autenticación (solo para testing)
function testAuthModal() {
    console.log('🧪 Iniciando pruebas del Modal de Autenticación...');
    
    if (!window.authModal) {
        console.error('❌ authModal no encontrado. Asegúrate de que auth-modal.js esté cargado.');
        return;
    }
    
    // Mostrar estado actual
    console.log(`📊 Estado actual de autenticación: ${window.authModal.isAuthenticated}`);
    
    // Probar forzar estado no autenticado (solo para pruebas)
    const originalState = window.authModal.isAuthenticated;
    
    console.log('🔓 Simulando usuario no autenticado...');
    window.authModal.updateAuthStatus(false);
    
    // Buscar elementos protegidos
    const protectedElements = document.querySelectorAll('a[href*="/reservas"], button[onclick*="reservar"], .price-wedding');
    console.log(`🛡️ Elementos protegidos encontrados: ${protectedElements.length}`);
    
    if (protectedElements.length > 0) {
        console.log('✅ Elementos protegidos detectados correctamente:');
        protectedElements.forEach((el, index) => {
            console.log(`   ${index + 1}. ${el.tagName}: "${el.textContent.substring(0, 50)}..."`);
        });
        
        // Simular clic en primer elemento (sin ejecutar realmente)
        console.log('🖱️ Para probar, haz clic en cualquier botón de reserva...');
    } else {
        console.warn('⚠️ No se encontraron elementos protegidos en esta página.');
    }
    
    // Restaurar estado original después de 5 segundos
    setTimeout(() => {
        window.authModal.updateAuthStatus(originalState);
        console.log(`🔄 Estado de autenticación restaurado a: ${originalState}`);
    }, 5000);
}

// Función para mostrar el modal manualmente (para pruebas)
function showTestModal() {
    if (window.authModal) {
        console.log('🎭 Mostrando modal de prueba...');
        window.authModal.showLoginModal();
    } else {
        console.error('❌ authModal no disponible');
    }
}

// Auto-ejecutar pruebas si estamos en modo desarrollo
document.addEventListener('DOMContentLoaded', function() {
    // Descomentar la siguiente línea para ejecutar pruebas automáticamente
    // setTimeout(testAuthModal, 2000);
    
    console.log('🚀 Sistema de Modal de Autenticación cargado');
    console.log('💡 Ejecuta testAuthModal() en la consola para probar');
    console.log('🎭 Ejecuta showTestModal() para mostrar el modal');
});