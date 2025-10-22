package Proyecto.Proyecto4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de JPA para manejar correctamente las relaciones entre entidades
 * y asegurar la integridad referencial
 */
@Configuration
@EnableJpaRepositories(basePackages = "Proyecto.Proyecto4.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {
    
    // Esta clase configura JPA para:
    // 1. Habilitar los repositorios JPA en el paquete especificado
    // 2. Activar el manejo de transacciones para operaciones en cascada
    // 3. Habilitar auditoría automática de entidades (para campos como fechaCreacion, etc.)
    
}
