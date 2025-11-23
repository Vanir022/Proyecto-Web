package Proyecto.Proyecto4.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.UsuarioRepository;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    // Map para almacenar intentos fallidos por usuario (email)
    private final ConcurrentHashMap<String, Integer> loginFailureCounts = new ConcurrentHashMap<>();

    // Máximo intentos permitidos
    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void resetearIntentosFallidos(String email) {
        if (email != null && loginFailureCounts.containsKey(email)) {
            loginFailureCounts.remove(email);
            logger.info("Intentos fallidos reseteados para el usuario '{}'", email);
        }
    }

@Override
public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {

    String email = request.getParameter("email");
    String errorMessage = "Email o contraseña incorrecta";

    if(email != null) {
        int attempts = loginFailureCounts.getOrDefault(email, 0);
        attempts++;
        loginFailureCounts.put(email, attempts);

        logger.warn("Intento de autenticación fallido para usuario '{}'. Intento número {}", email, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            logger.warn("El usuario '{}' ha superado el número máximo de intentos fallidos. Cuenta bloqueada temporalmente.", email);
            // Bloqueo real: actualizar en base de datos
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                usuario.setAccountNonLocked(false);
                usuario.setLockTime(java.time.LocalDateTime.now());
                usuarioRepository.save(usuario);
            }
            errorMessage = "Su cuenta ha sido bloqueada debido a demasiados intentos fallidos. Por favor contacte al administrador.";
        } else if (exception instanceof org.springframework.security.authentication.LockedException) {
            // Si la cuenta está bloqueada al momento de la autenticación
            errorMessage = "Su cuenta está bloqueada. Por favor contacte al administrador.";
        } else if (exception instanceof org.springframework.security.authentication.DisabledException) {
            errorMessage = "Su cuenta está deshabilitada.";
        }

    }

    super.setDefaultFailureUrl("/login?error=true&message=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
    super.onAuthenticationFailure(request, response, exception);
}
}
