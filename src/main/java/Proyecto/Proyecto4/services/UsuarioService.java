package Proyecto.Proyecto4.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Proyecto.Proyecto4.config.CustomAuthenticationFailureHandler;
import Proyecto.Proyecto4.dto.RegistroUsuarioDTO;
import Proyecto.Proyecto4.models.DetallesPersona;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.DetallesPersonaRepository;
import Proyecto.Proyecto4.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;
    private final DetallesPersonaRepository detallesPersonaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
            DetallesPersonaRepository detallesPersonaRepository,
            BCryptPasswordEncoder passwordEncoder,
            CustomAuthenticationFailureHandler failureHandler) {
        this.usuarioRepository = usuarioRepository;
        this.detallesPersonaRepository = detallesPersonaRepository;
        this.passwordEncoder = passwordEncoder;
        this.failureHandler = failureHandler;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario registrar(Usuario usuario) {
        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar contraseña y asignar rol por defecto
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("ROLE_USER");
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarConDetalles(RegistroUsuarioDTO registroDTO) {
        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(registroDTO.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar DNI
        if (registroDTO.getDni() == null || registroDTO.getDni().trim().isEmpty()) {
            throw new RuntimeException("El DNI es obligatorio");
        }

        if (!registroDTO.getDni().matches("\\d{8}")) {
            throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
        }

        // Verificar si el DNI ya existe
        if (detallesPersonaRepository.existsByDni(registroDTO.getDni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        // Crear y guardar DetallesPersona
        DetallesPersona detallesPersona = new DetallesPersona();
        detallesPersona.setNombres(registroDTO.getFirstName());
        detallesPersona.setApellidos(registroDTO.getLastName());
        detallesPersona.setDni(registroDTO.getDni());
        detallesPersona.setTelefono(registroDTO.getPhone());
        detallesPersona.setFechaNacimiento(registroDTO.getBirthDate());
        detallesPersona.setIntereses(registroDTO.getInteresesAsString());
        detallesPersona.setAceptaMarketing(
                registroDTO.getAcceptMarketing() != null ? registroDTO.getAcceptMarketing() : false);

        // Guardar detalles primero
        detallesPersona = detallesPersonaRepository.save(detallesPersona);

        // Crear Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombreCompleto());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setRol("ROLE_USER");
        usuario.setDetallesPersona(detallesPersona);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean validarPassword(String passwordRaw, String passwordEncoded) {
        return passwordEncoder.matches(passwordRaw, passwordEncoded);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public void eliminar(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getDetallesPersona() != null) {
                detallesPersonaRepository.deleteById(usuario.getDetallesPersona().getId());
            }
            usuarioRepository.deleteById(id);
        }
    }

    public void cambiarPassword(Usuario usuario, String nuevaPassword) {
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public boolean desbloquearUsuario(Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setAccountNonLocked(true);
            usuario.setFailedAttempt(0);
            usuario.setLockTime(null);
            usuarioRepository.save(usuario);
            try {
                failureHandler.resetearIntentosFallidos(usuario.getEmail());
            } catch (Exception e) {
                logger.warn("No se pudo resetear contador en memoria para {}: {}", usuario.getEmail(), e.getMessage());
            }
            logger.info("Usuario desbloqueado: id={}, email={}", usuario.getId(), usuario.getEmail());
            return true;
        } else {
            logger.warn("Intento de desbloqueo para usuario inexistente: id={}", usuarioId);
            return false;
        }
    }

    // Métodos para validaciones individuales
    public boolean emailExiste(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    public boolean telefonoExiste(String telefono) {
        return detallesPersonaRepository.existsByTelefono(telefono);
    }

    public boolean dniExiste(String dni) {
        return detallesPersonaRepository.existsByDni(dni);
    }
}
