// Modal de autenticación - Eden Resorts
class AuthModal {
    constructor() {
        this.isAuthenticated = this.checkAuthStatus();
        this.initModal();
        this.bindEvents();
    }

    // Verificar si el usuario está autenticado
    checkAuthStatus() {
        // Verificamos si existe el dropdown de usuario en el DOM
        return document.querySelector('#userDropdown') !== null;
    }

    // Crear el modal HTML
    initModal() {
        // Solo crear el modal si no existe
        if (document.getElementById('loginModal')) {
            return;
        }

        const modalHTML = `
            <!-- Modal de Inicio de Sesión -->
            <div class="modal fade" id="loginModal" tabindex="-1" aria-labelledby="loginModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-primary text-white">
                            <h5 class="modal-title" id="loginModalLabel">
                                <i class="fas fa-shield-alt me-2"></i>Acceso Requerido
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body text-center py-4">
                            <div class="mb-4">
                                <i class="fas fa-user-lock fa-4x text-primary mb-3"></i>
                                <h4 class="text-primary mb-3">¡Inicia Sesión para Continuar!</h4>
                                <p class="text-muted mb-3">Para realizar reservas y acceder a nuestros servicios exclusivos de Eden Resorts, necesitas estar registrado e iniciar sesión.</p>
                            </div>
                            
                            <div class="alert auth-info-alert d-flex align-items-center mb-4" role="alert">
                                <i class="fas fa-info-circle me-2 text-primary"></i>
                                <div class="text-start">
                                    <strong>¿No tienes cuenta?</strong><br>
                                    <small>Puedes registrarte fácilmente desde la página de login. ¡Es gratis y rápido!</small>
                                </div>
                            </div>
                            
                            <div class="row g-2">
                                <div class="col-12 col-md-6">
                                    <a href="/login" class="btn btn-primary btn-lg w-100 d-flex align-items-center justify-content-center">
                                        <i class="fas fa-sign-in-alt me-2"></i>Iniciar Sesión
                                    </a>
                                </div>
                                <div class="col-12 col-md-6">
                                    <button type="button" class="btn btn-outline-secondary btn-lg w-100 d-flex align-items-center justify-content-center" data-bs-dismiss="modal">
                                        <i class="fas fa-times me-2"></i>Cancelar
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light justify-content-center">
                            <div class="text-center">
                                <small class="text-muted">
                                    <i class="fas fa-heart text-danger me-1"></i>
                                    <strong>Eden Resorts</strong> - Donde tus recuerdos comienzan
                                </small>
                                <br>
                                <small class="text-muted">
                                    <i class="fas fa-shield-alt me-1"></i>
                                    Tus datos están seguros con nosotros
                                </small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Agregar el modal al body
        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    // Vincular eventos
    bindEvents() {
        // Interceptar clics en botones de reserva y servicios
        document.addEventListener('click', (e) => this.handleClick(e));
    }

    // Manejar clics en elementos protegidos
    handleClick(event) {
        // Si el usuario está autenticado, permitir la acción
        if (this.isAuthenticated) {
            return true;
        }

        const target = event.target;
        const link = target.closest('a, button');
        
        if (!link) {
            return true;
        }

        // Lista de selectores que requieren autenticación
        const protectedSelectors = [
            // Enlaces de reservas
            'a[href*="/reservas"]',
            'a[href*="/reserva"]',
            'a[href*="tipoReserva="]',
            // Botones de reservas
            'button[onclick*="reservar"]',
            'button[onclick*="mostrarDetalles"]',
            'button[onclick*="buscarDisponibilidad"]',
            // Enlaces de servicios que requieren reserva
            'a[href*="/spa"]',
            'a[href*="/bodas"]',
            'a[href*="/eventos"]',
            // Formularios de servicios
            'form[id*="Form"] button[type="submit"]',
            'button[onclick*="solicitar"]',
            'button[onclick*="cotizar"]',
            'button[onclick*="confirmar"]',
            // Selectores específicos de las páginas
            '.btn[href*="reserva"]',
            '.btn[onclick*="reserva"]',
            'a.price-wedding',
            'button[onclick*="mostrarDisponibilidadHabitacion"]'
        ];

        // Verificar si el elemento coincide con algún selector protegido
        const isProtected = protectedSelectors.some(selector => {
            try {
                return link.matches(selector);
            } catch (e) {
                return false;
            }
        }) || this.containsProtectedText(link) || this.hasProtectedClass(link);

        if (isProtected) {
            event.preventDefault();
            event.stopPropagation();
            this.showLoginModal();
            return false;
        }

        return true;
    }

    // Verificar si el elemento tiene clases que indican que requiere autenticación
    hasProtectedClass(element) {
        const protectedClasses = [
            'reservar-btn',
            'booking-btn',
            'auth-required',
            'requires-login'
        ];
        
        return protectedClasses.some(className => element.classList.contains(className));
    }

    // Verificar si el texto del elemento indica que requiere autenticación
    containsProtectedText(element) {
        const text = element.textContent.toLowerCase();
        const protectedKeywords = [
            'reservar',
            'reserva',
            'confirmar reserva',
            'ver detalles',
            'cotizar',
            'solicitar',
            'enviar solicitud',
            'confirmar',
            'agendar',
            'planifica tu evento',
            'solicitar cotización',
            'desde s/',
            'precio:',
            'paquete:'
        ];

        return protectedKeywords.some(keyword => text.includes(keyword));
    }

    // Mostrar el modal de login
    showLoginModal() {
        const modal = new bootstrap.Modal(document.getElementById('loginModal'), {
            keyboard: true,
            backdrop: true
        });
        modal.show();
    }

    // Actualizar estado de autenticación
    updateAuthStatus(isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    // Marcar visualmente elementos que requieren autenticación (opcional)
    markProtectedElements() {
        if (this.isAuthenticated) {
            return; // No marcar si ya está autenticado
        }

        const protectedSelectors = [
            'a[href*="/reservas"]',
            'a[href*="tipoReserva="]',
            'button[onclick*="reservar"]',
            'button[onclick*="solicitar"]',
            'button[onclick*="cotizar"]',
            '.price-wedding'
        ];

        protectedSelectors.forEach(selector => {
            document.querySelectorAll(selector).forEach(element => {
                if (!element.classList.contains('requires-auth')) {
                    element.classList.add('requires-auth');
                    element.title = 'Requiere iniciar sesión para continuar';
                }
            });
        });
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    // Esperar un poco para que se carguen otros scripts
    setTimeout(() => {
        window.authModal = new AuthModal();
        // Marcar elementos protegidos visualmente (opcional)
        window.authModal.markProtectedElements();
    }, 100);
});

// Función global para verificar autenticación antes de acciones específicas
function requireAuth(callback) {
    if (window.authModal && window.authModal.isAuthenticated) {
        if (typeof callback === 'function') {
            callback();
        }
        return true;
    } else {
        if (window.authModal) {
            window.authModal.showLoginModal();
        }
        return false;
    }
}

// Función para integrar con formularios existentes
function protectForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!window.authModal || !window.authModal.isAuthenticated) {
                e.preventDefault();
                if (window.authModal) {
                    window.authModal.showLoginModal();
                }
            }
        });
    }
}

// Función para proteger botones específicos
function protectButton(buttonSelector, customMessage = null) {
    document.addEventListener('click', function(e) {
        if (e.target.matches(buttonSelector) || e.target.closest(buttonSelector)) {
            if (!window.authModal || !window.authModal.isAuthenticated) {
                e.preventDefault();
                e.stopPropagation();
                if (window.authModal) {
                    window.authModal.showLoginModal();
                }
            }
        }
    });
}