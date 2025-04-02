-- Insertar roles si no existen
INSERT INTO roles (idrol, nombre) 
SELECT 1, 'ADMIN' 
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE idrol = 1);

INSERT INTO roles (idrol, nombre) 
SELECT 2, 'USER' 
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE idrol = 2);

-- Insertar usuario de prueba si no existe
INSERT INTO usuarios (nombre, correo, contrasena, idrol, estatus) 
SELECT 'Irvin Alexis', 'hirvinalexis5@gmail.com', 'irvin123', 1, 1 
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'hirvinalexis5@gmail.com');
