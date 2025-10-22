-- Script SQL para verificar y optimizar las relaciones del proyecto Eden Resort
-- Este script verifica las claves foráneas y crea índices necesarios

USE resort_eden;

-- Verificar estructura de tablas y relaciones existentes
SHOW TABLES;

-- 1. Verificar tabla administradores_usuarios (tabla intermedia para relación ManyToMany)
-- Esta tabla conecta usuarios con administradores
CREATE TABLE IF NOT EXISTS administradores_usuarios (
    administradores_id BIGINT NOT NULL,
    usuarios_id BIGINT NOT NULL,
    PRIMARY KEY (administradores_id, usuarios_id),
    FOREIGN KEY (administradores_id) REFERENCES administradores(id) ON DELETE CASCADE,
    FOREIGN KEY (usuarios_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Crear índices para mejorar el rendimiento de las consultas

-- Índices en tabla usuarios
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_detalles_persona ON usuarios(detalles_persona_id);

-- Índices en tabla administradores
CREATE INDEX idx_administradores_email ON administradores(email);
CREATE INDEX idx_administradores_hotel ON administradores(hotel);
CREATE INDEX idx_administradores_activo ON administradores(activo);

-- Índices en tabla reservas
CREATE INDEX idx_reservas_usuario ON reservas(usuario_id);
CREATE INDEX idx_reservas_habitacion ON reservas(habitacion_id);
CREATE INDEX idx_reservas_fechas ON reservas(fecha_entrada, fecha_salida);
CREATE INDEX idx_reservas_estado ON reservas(estado);
CREATE INDEX idx_reservas_codigo ON reservas(codigo_reserva);

-- Índices en tabla habitaciones
CREATE INDEX idx_habitaciones_numero ON habitaciones(numero);
CREATE INDEX idx_habitaciones_tipo ON habitaciones(tipo);
CREATE INDEX idx_habitaciones_hotel ON habitaciones(hotel);
CREATE INDEX idx_habitaciones_disponible ON habitaciones(disponible);
CREATE INDEX idx_habitaciones_estado ON habitaciones(estado_habitacion);

-- Índices en tabla contactos
CREATE INDEX idx_contactos_estado ON contactos(estado);
CREATE INDEX idx_contactos_hotel ON contactos(hotel);
CREATE INDEX idx_contactos_fecha ON contactos(fecha_envio);
CREATE INDEX idx_contactos_usuario ON contactos(usuarios_id);

-- Índices en tabla detalles_persona
CREATE INDEX idx_detalles_dni ON detalles_persona(dni);
CREATE INDEX idx_detalles_telefono ON detalles_persona(telefono);

-- 3. Verificar las relaciones existentes
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'resort_eden'
    AND TABLE_SCHEMA = 'resort_eden'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 4. Consultas útiles para verificar la integridad de datos

-- Verificar usuarios sin detalles de persona
SELECT u.id, u.nombre, u.email 
FROM usuarios u 
LEFT JOIN detalles_persona dp ON u.detalles_persona_id = dp.id 
WHERE dp.id IS NULL;

-- Verificar reservas activas
SELECT 
    r.id,
    r.codigo_reserva,
    u.nombre AS usuario,
    h.numero AS habitacion,
    r.fecha_entrada,
    r.fecha_salida,
    r.estado
FROM reservas r
INNER JOIN usuarios u ON r.usuario_id = u.id
INNER JOIN habitaciones h ON r.habitacion_id = h.id
WHERE r.estado IN ('PENDIENTE', 'CONFIRMADA')
ORDER BY r.fecha_entrada;

-- Verificar habitaciones ocupadas
SELECT 
    h.numero,
    h.tipo,
    h.hotel,
    h.estado_habitacion,
    COUNT(r.id) AS reservas_activas
FROM habitaciones h
LEFT JOIN reservas r ON h.id = r.habitacion_id 
    AND r.estado IN ('CONFIRMADA', 'PENDIENTE')
    AND CURDATE() BETWEEN r.fecha_entrada AND r.fecha_salida
GROUP BY h.id, h.numero, h.tipo, h.hotel, h.estado_habitacion
HAVING reservas_activas > 0;

-- Verificar contactos de usuarios registrados vs no registrados
SELECT 
    CASE WHEN c.usuarios_id IS NOT NULL THEN 'Usuario Registrado' ELSE 'Usuario Anónimo' END AS tipo_contacto,
    COUNT(*) AS total
FROM contactos c
GROUP BY tipo_contacto;

-- 5. Estadísticas del sistema
SELECT 
    'Usuarios' AS tabla, COUNT(*) AS total FROM usuarios
UNION ALL
SELECT 'Administradores', COUNT(*) FROM administradores
UNION ALL
SELECT 'Reservas', COUNT(*) FROM reservas
UNION ALL
SELECT 'Habitaciones', COUNT(*) FROM habitaciones
UNION ALL
SELECT 'Contactos', COUNT(*) FROM contactos
UNION ALL
SELECT 'Detalles Persona', COUNT(*) FROM detalles_persona;
