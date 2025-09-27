package Proyecto.Proyecto4.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import Proyecto.Proyecto4.models.DetallesPersona;
import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.DetallesPersonaRepository;
import Proyecto.Proyecto4.repository.HabitacionRepository;
import Proyecto.Proyecto4.repository.UsuarioRepository;
import Proyecto.Proyecto4.services.AdministradorService;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdministradorService administradorService;
    
    @Autowired
    private HabitacionRepository habitacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private DetallesPersonaRepository detallesPersonaRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear super admin inicial si no existe
        administradorService.crearSuperAdminInicial();
        
        // Crear usuarios de ejemplo si no existen
        if (usuarioRepository.count() == 0) {
            crearUsuariosEjemplo();
        }
        
        // Crear habitaciones de ejemplo si no existen
        if (habitacionRepository.count() == 0) {
            crearHabitacionesEjemplo();
        }
    }
    
    private void crearHabitacionesEjemplo() {
        // Habitaciones para Aranwa Cusco
        Habitacion hab1 = new Habitacion();
        hab1.setNumero("101");
        hab1.setTipo("Standard");
        hab1.setCapacidad(2);
        hab1.setPrecio(new BigDecimal("180.00"));
        hab1.setHotel("Aranwa Cusco");
        hab1.setDisponible(true);
        hab1.setAmenidades("WiFi, Minibar, TV");
        hab1.setVista("Plaza de Armas");
        hab1.setCama("Queen");
        hab1.setMetrosCuadrados(25);
        hab1.setDescripcion("Habitación standard con vista a la plaza de armas");
        
        Habitacion hab2 = new Habitacion();
        hab2.setNumero("201");
        hab2.setTipo("Suite");
        hab2.setCapacidad(4);
        hab2.setPrecio(new BigDecimal("350.00"));
        hab2.setHotel("Aranwa Cusco");
        hab2.setDisponible(true);
        hab2.setAmenidades("WiFi, Minibar, TV, Balcón, Jacuzzi");
        hab2.setVista("Panorámica");
        hab2.setCama("King");
        hab2.setMetrosCuadrados(45);
        hab2.setDescripcion("Suite familiar con balcón y vista panorámica");
        
        // Habitaciones para Aranwa Paracas
        Habitacion hab3 = new Habitacion();
        hab3.setNumero("102");
        hab3.setTipo("Superior");
        hab3.setCapacidad(2);
        hab3.setPrecio(new BigDecimal("250.00"));
        hab3.setHotel("Aranwa Paracas");
        hab3.setDisponible(true);
        hab3.setAmenidades("WiFi, Minibar, TV, Terraza");
        hab3.setVista("Mar");
        hab3.setCama("Queen");
        hab3.setMetrosCuadrados(30);
        hab3.setDescripcion("Habitación superior con vista al mar");
        
        Habitacion hab4 = new Habitacion();
        hab4.setNumero("301");
        hab4.setTipo("Master Suite");
        hab4.setCapacidad(6);
        hab4.setPrecio(new BigDecimal("450.00"));
        hab4.setHotel("Aranwa Paracas");
        hab4.setDisponible(false); // Ocupada
        hab4.setAmenidades("WiFi, Minibar, TV, Balcón, Jacuzzi, Sala de estar");
        hab4.setVista("Mar");
        hab4.setCama("King");
        hab4.setMetrosCuadrados(65);
        hab4.setDescripcion("Master Suite con todas las amenidades");
        
        // Habitaciones para Aranwa Colca
        Habitacion hab5 = new Habitacion();
        hab5.setNumero("105");
        hab5.setTipo("Standard");
        hab5.setCapacidad(2);
        hab5.setPrecio(new BigDecimal("160.00"));
        hab5.setHotel("Aranwa Colca");
        hab5.setDisponible(true);
        hab5.setAmenidades("WiFi, TV");
        hab5.setVista("Cañón");
        hab5.setCama("Queen");
        hab5.setMetrosCuadrados(22);
        hab5.setDescripcion("Habitación standard con vista al cañón");
        
        Habitacion hab6 = new Habitacion();
        hab6.setNumero("205");
        hab6.setTipo("Suite");
        hab6.setCapacidad(3);
        hab6.setPrecio(new BigDecimal("280.00"));
        hab6.setHotel("Aranwa Colca");
        hab6.setDisponible(true);
        hab6.setAmenidades("WiFi, Minibar, TV, Balcón");
        hab6.setVista("Valle");
        hab6.setCama("King");
        hab6.setMetrosCuadrados(40);
        hab6.setDescripcion("Suite con vista al valle del Colca");
        
        // Habitaciones para Aranwa Valle Sagrado
        Habitacion hab7 = new Habitacion();
        hab7.setNumero("110");
        hab7.setTipo("Superior");
        hab7.setCapacidad(2);
        hab7.setPrecio(new BigDecimal("200.00"));
        hab7.setHotel("Aranwa Valle Sagrado");
        hab7.setDisponible(true);
        hab7.setAmenidades("WiFi, Minibar, TV, Chimenea");
        hab7.setVista("Montaña");
        hab7.setCama("Queen");
        hab7.setMetrosCuadrados(28);
        hab7.setDescripcion("Habitación superior con chimenea y vista a la montaña");
        
        Habitacion hab8 = new Habitacion();
        hab8.setNumero("210");
        hab8.setTipo("Suite");
        hab8.setCapacidad(4);
        hab8.setPrecio(new BigDecimal("320.00"));
        hab8.setHotel("Aranwa Valle Sagrado");
        hab8.setDisponible(true);
        hab8.setAmenidades("WiFi, Minibar, TV, Balcón, Chimenea, Sala de estar");
        hab8.setVista("Valle Sagrado");
        hab8.setCama("King");
        hab8.setMetrosCuadrados(50);
        hab8.setDescripcion("Suite con vista al Valle Sagrado y chimenea");
        
        // Guardar todas las habitaciones
        habitacionRepository.save(hab1);
        habitacionRepository.save(hab2);
        habitacionRepository.save(hab3);
        habitacionRepository.save(hab4);
        habitacionRepository.save(hab5);
        habitacionRepository.save(hab6);
        habitacionRepository.save(hab7);
        habitacionRepository.save(hab8);
        
        System.out.println("✅ Se crearon " + habitacionRepository.count() + " habitaciones de ejemplo");
    }
    
    private void crearUsuariosEjemplo() {
        try {
            // Usuario 1: Juan Pérez
            DetallesPersona detalles1 = new DetallesPersona();
            detalles1.setNombres("Juan Carlos");
            detalles1.setApellidos("Pérez García");
            detalles1.setTelefono("987654321");
            detalles1.setFechaNacimiento(LocalDate.of(1985, 3, 15));
            detalles1.setIntereses("Turismo, Gastronomía");
            detalles1.setAceptaMarketing(true);
            detalles1 = detallesPersonaRepository.save(detalles1);
            
            Usuario usuario1 = new Usuario();
            usuario1.setNombre("Juan Carlos Pérez García");
            usuario1.setEmail("juan.perez@email.com");
            usuario1.setPassword(passwordEncoder.encode("password123"));
            usuario1.setRol("ROLE_USER");
            usuario1.setDetallesPersona(detalles1);
            usuarioRepository.save(usuario1);

            // Usuario 2: María González
            DetallesPersona detalles2 = new DetallesPersona();
            detalles2.setNombres("María Elena");
            detalles2.setApellidos("González Ríos");
            detalles2.setTelefono("956789123");
            detalles2.setFechaNacimiento(LocalDate.of(1992, 7, 22));
            detalles2.setIntereses("Spa, Aventura");
            detalles2.setAceptaMarketing(false);
            detalles2 = detallesPersonaRepository.save(detalles2);
            
            Usuario usuario2 = new Usuario();
            usuario2.setNombre("María Elena González Ríos");
            usuario2.setEmail("maria.gonzalez@email.com");
            usuario2.setPassword(passwordEncoder.encode("password456"));
            usuario2.setRol("ROLE_USER");
            usuario2.setDetallesPersona(detalles2);
            usuarioRepository.save(usuario2);

            // Usuario 3: Carlos Mendoza
            DetallesPersona detalles3 = new DetallesPersona();
            detalles3.setNombres("Carlos Alberto");
            detalles3.setApellidos("Mendoza Silva");
            detalles3.setTelefono("923456789");
            detalles3.setFechaNacimiento(LocalDate.of(1978, 11, 8));
            detalles3.setIntereses("Historia, Cultura");
            detalles3.setAceptaMarketing(true);
            detalles3 = detallesPersonaRepository.save(detalles3);
            
            Usuario usuario3 = new Usuario();
            usuario3.setNombre("Carlos Alberto Mendoza Silva");
            usuario3.setEmail("carlos.mendoza@email.com");
            usuario3.setPassword(passwordEncoder.encode("password789"));
            usuario3.setRol("ROLE_USER");
            usuario3.setDetallesPersona(detalles3);
            usuarioRepository.save(usuario3);

            // Usuario 4: Ana Torres (sin detalles personales completos)
            Usuario usuario4 = new Usuario();
            usuario4.setNombre("Ana María Torres");
            usuario4.setEmail("ana.torres@email.com");
            usuario4.setPassword(passwordEncoder.encode("password000"));
            usuario4.setRol("ROLE_USER");
            usuarioRepository.save(usuario4);
            
            System.out.println("✅ Se crearon " + usuarioRepository.count() + " usuarios de ejemplo");
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear usuarios de ejemplo: " + e.getMessage());
        }
    }
}