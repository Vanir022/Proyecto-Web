package Proyecto.Proyecto4.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Administrador.RolAdmin;
import Proyecto.Proyecto4.repository.AdministradorRepository;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Administrador crearAdministrador(String email, String password, String nombres,
            String apellidos, String telefono, RolAdmin rol, String hotel) {

        // Verificar que el email no exista
        if (administradorRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un administrador con ese email");
        }

        Administrador admin = new Administrador();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password)); // Encriptar contraseña
        admin.setNombres(nombres);
        admin.setApellidos(apellidos);
        admin.setTelefono(telefono);
        admin.setRol(rol);
        admin.setHotel(hotel);
        admin.setActivo(true);

        return administradorRepository.save(admin);
    }

    public List<Administrador> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll();
    }

    public List<Administrador> obtenerAdministradoresPorHotel(String hotel) {
        return administradorRepository.findByHotel(hotel);
    }

    public Optional<Administrador> buscarPorEmail(String email) {
        return administradorRepository.findByEmail(email);
    }

    public Administrador activarDesactivar(Long id, Boolean activo) {
        Optional<Administrador> adminOpt = administradorRepository.findById(id);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            admin.setActivo(activo);
            return administradorRepository.save(admin);
        }
        throw new RuntimeException("Administrador no encontrado");
    }

    public void eliminarAdministrador(Long id) {
        administradorRepository.deleteById(id);
    }

    public Administrador cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Optional<Administrador> adminOpt = administradorRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();

            // Verificar contraseña actual
            if (passwordEncoder.matches(passwordActual, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(passwordNueva));
                return administradorRepository.save(admin);
            } else {
                throw new RuntimeException("Contraseña actual incorrecta");
            }
        }
        throw new RuntimeException("Administrador no encontrado");
    }

    // Método para crear el super admin inicial
    public void crearSuperAdminInicial() {
        if (!administradorRepository.existsByEmail("superadmin@aranwa.com")) {
            crearAdministrador(
                    "superadmin@aranwa.com",
                    "admin123",
                    "Super",
                    "Administrador",
                    "+51987654321",
                    RolAdmin.SUPER_ADMIN,
                    null);
            System.out.println("Super Admin creado: superadmin@aranwa.com / admin123");
        }
    }
}