-- Script SQL para crear la tabla contactos manualmente si es necesario

CREATE TABLE IF NOT EXISTS contactos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    hotel VARCHAR(100),
    mensaje TEXT NOT NULL,
    fecha_envio DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'NUEVO'
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_contactos_estado ON contactos(estado);
CREATE INDEX idx_contactos_fecha ON contactos(fecha_envio);
CREATE INDEX idx_contactos_email ON contactos(email);