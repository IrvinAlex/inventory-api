# Proyecto de Inventario

## Especificaciones de Desarrollo

### Entorno de Desarrollo
- **IDE:** Spring Tool Suite 4 (STS4)
- **Lenguaje de Programación:** Java 17
- **Framework:** Spring Boot 3.x
- **Sistema de Gestión de Base de Datos:** MySQL Server 8.0

## Instrucciones Detalladas de Configuración

### 1. Configuración de MySQL Server
1. Descarga MySQL Server 8.0 del sitio web oficial
   - Visita: https://dev.mysql.com/downloads/mysql/
   - Elige MySQL Community Server 8.0.x
   - Descarga la versión apropiada para tu sistema operativo

2. Instalar MySQL Server
   - Ejecuta el instalador
   - Selecciona la instalación tipo "Developer Default"
   - Establece una contraseña root durante la instalación (¡recuerda esta contraseña!)
   - Completa el proceso de instalación

3. Verificar la Instalación de MySQL
   - Abre el Símbolo del Sistema o Terminal
   - Escribe: `mysql --version`
   - Debería mostrar MySQL 8.0.x

4. Iniciar el Servicio MySQL
   - Windows:
     ```
     net start mysql80
     ```
   - Linux/Mac:
     ```
     sudo systemctl start mysql
     ```

5. Acceder a MySQL
   ```
   mysql -u root -p
   ```
   Ingresa tu contraseña root cuando se solicite

### 2. Creación de la Base de Datos
1. Crear la Base de Datos del Proyecto:
   ```sql
   CREATE DATABASE inventario;
   USE inventario;
   ```


### 3. Configuración de la Aplicación
1. Configurar la Conexión a la Base de Datos:
   - Abre `application.properties` en tu proyecto
   - Agrega estas configuraciones:
     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/inventario
     spring.datasource.username=inventario_user
     spring.datasource.password=tu_contraseña
     spring.jpa.hibernate.ddl-auto=update
     spring.jpa.show-sql=true
     ```

### 4. Ejecutar la Aplicación
1. Iniciar MySQL Server (si no está en ejecución)
2. Abrir Spring Tool Suite 4
3. Importar tu proyecto
4. Clic derecho en el proyecto > Run As > Spring Boot App
5. Acceder a la aplicación en `http://localhost:8080`

### Comandos Comunes de MySQL
- Mostrar bases de datos: `SHOW DATABASES;`
- Mostrar tablas: `SHOW TABLES;`
- Describir estructura de tabla: `DESCRIBE nombre_tabla;`
- Consulta básica: `SELECT * FROM nombre_tabla;`

### Solución de Problemas
- Si el servicio MySQL no inicia:
  - Revisar logs de error en: `C:\ProgramData\MySQL\MySQL Server 8.0\Data`
  - Verificar que el puerto 3306 no esté en uso
- Si la conexión falla:
  - Verificar que MySQL esté en ejecución
  - Revisar credenciales en application.properties
  - Asegurar que la base de datos existe
