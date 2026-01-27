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

//COMONENTE PERSONALIZADO PARA MANEJAR FALLOS DE AUTENTICACION
@Component
//HEREDA DE SIMPLEURLAUTHENTICATIONFAILUREHANDLER SPRING SECURITY
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    //MEMORIA DEL SERVIDOR PARA APUNTAR EL INICIO DE SESION EMAIL Y N VECES
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    // Mapa para almacenar intentos fallidos por usuario (email) String email / Integer N veces fallida
    private final ConcurrentHashMap<String, Integer> loginFailureCounts = new ConcurrentHashMap<>();

    // Máximo intentos permitidos
    private static final int MAX_ATTEMPTS = 5;

    // Repositorio de usuarios para buscar en la base de datos y actualizar información
    @Autowired
    private UsuarioRepository usuarioRepository;

    //Buscar Email y ver el N° Fallos
    public void resetearIntentosFallidos(String email) {
        if (email != null && loginFailureCounts.containsKey(email)) {
            loginFailureCounts.remove(email);
            logger.info("Intentos fallidos reseteados para el usuario '{}'", email);
        }
    }

    //Metodo que manejo el fallo de autenticacion
@Override
public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {

            //Extraer email del usuario que intenta loguearse
    String email = request.getParameter("email");
    String errorMessage = "Email o contraseña incorrecta";

    //VERIFICAR QUE EL EMAIL NO SEA NULO
    if(email != null) {
        int attempts = loginFailureCounts.getOrDefault(email, 0);
        //AUMENTAR EN 1 EL NUMERO DE INTENTOS FALLIDOS
        attempts++;
        loginFailureCounts.put(email, attempts);

        logger.warn("Intento de autenticación fallido para usuario '{}'. Intento número {}", email, attempts);

        //CONDICION PRINICIPAL DE LA SEGURIDAD
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
