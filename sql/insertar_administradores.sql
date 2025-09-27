-- Script para crear administradores iniciales
-- IMPORTANTE: Las contraseñas deben estar encriptadas con BCrypt

-- Super Admin
INSERT INTO administradores (email, password, nombres, apellidos, telefono, rol, activo, fecha_creacion, hotel) 
VALUES (
    'superadmin@aranwa.com', 
    '$2a$10$N9qo8uLOickgx2ZrVzUhruN7QKVeNzbWvEBAhR4LU5HMfDhRUyZiC', -- password: admin123
    'Super', 
    'Administrador', 
    '+51987654321', 
    'SUPER_ADMIN', 
    true, 
    NOW(), 
    NULL
);

-- Admin Hotel Cusco
INSERT INTO administradores (email, password, nombres, apellidos, telefono, rol, activo, fecha_creacion, hotel) 
VALUES (
    'admin.cusco@aranwa.com', 
    '$2a$10$N9qo8uLOickgx2ZrVzUhruN7QKVeNzbWvEBAhR4LU5HMfDhRUyZiC', -- password: admin123
    'Juan Carlos', 
    'Pérez Silva', 
    '+51987654322', 
    'ADMIN', 
    true, 
    NOW(), 
    'Aranwa Cusco'
);

-- Admin Hotel Paracas
INSERT INTO administradores (email, password, nombres, apellidos, telefono, rol, activo, fecha_creacion, hotel) 
VALUES (
    'admin.paracas@aranwa.com', 
    '$2a$10$N9qo8uLOickgx2ZrVzUhruN7QKVeNzbWvEBAhR4LU5HMfDhRUyZiC', -- password: admin123
    'María Elena', 
    'Rodríguez García', 
    '+51987654323', 
    'ADMIN', 
    true, 
    NOW(), 
    'Aranwa Paracas'
);

-- Recepcionista Cusco
INSERT INTO administradores (email, password, nombres, apellidos, telefono, rol, activo, fecha_creacion, hotel) 
VALUES (
    'recepcion.cusco@aranwa.com', 
    '$2a$10$N9qo8uLOickgx2ZrVzUhruN7QKVeNzbWvEBAhR4LU5HMfDhRUyZiC', -- password: admin123
    'Luis Miguel', 
    'Torres Vargas', 
    '+51987654324', 
    'RECEPCION', 
    true, 
    NOW(), 
    'Aranwa Cusco'
);