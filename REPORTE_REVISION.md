# Resultado de la revisión de Pixelworks API

## Ya existía y se conservó

- Java 21, Spring Boot, Maven, PostgreSQL y Docker multi-stage.
- Seguridad JWT, registro, login y control de roles.
- Endpoints de productos, categorías, desarrolladores, ofertas, claves, compras, pagos y reportes.
- Validación de negocio, lógica de stock y compras, pruebas unitarias y de integración originales.

## Se corrigió o mejoró

| Archivo | Cambio |
|---|---|
| `docker-compose.yml` | Contraseñas obligatorias, bases persistentes, PostgreSQL listo antes de iniciar API, puerto local por defecto y demo configurable |
| `.env.example` | Plantilla sin claves reales ni secretos compartidos |
| `INICIAR.cmd`, `INICIAR.ps1` | Arranque en Windows; crea `.env` aleatorio si no existe |
| `PROBAR_API.ps1` | Prueba de catálogo, detalle, login JWT y ruta protegida |
| `src/main/resources/application.properties` | Configuración de seguridad, DB y subida de imágenes desde el entorno, sin claves embebidas |
| `config/SecurityConfig.java` | GET de detalle de productos, categorías, ofertas, desarrolladores e imágenes accesibles públicamente; mutaciones siguen protegidas |
| `config/InicializadorDatos.java` | Rol JUGADOR siempre; modo demo explícito con admin y catálogo inicial sin duplicar catálogo existente |
| `controllers/ProductoController.java` | Conserva JSON y admite multipart de `dto` + `imagen` para POST y PUT |
| `controllers/ImagenController.java` | `GET /images/{uuid}` público |
| `services/ImagenService.java` | Almacenamiento persistente, tipos de imagen permitidos, nombres únicos y rutas limitadas |
| `services/ProductoService.java` | Si en edición no llega imagen, conserva la existente |
| `exceptions/GlobalExceptionHandler.java` | Respuesta 413 para archivos que superan 10 MB |
| `LEEME_INICIO.md` | Documentación para Docker + JWT + contrato Retrofit de Android |
| `src/test/.../ImagenServiceTest.java` | Pruebas JUnit de guardado, lectura y validación de imágenes (a ejecutar con Maven) |

## Comprobación y limitaciones

- Clases Java modificadas: **compilación `javac` correcta** con Java 21 y dependencias del paquete recibido. Además, un ejercicio de prueba ejecutado con Java comprobó guardado, lectura, tipo MIME, bloqueo de rutas inválidas y borrado de imágenes. (JUnit se deja preparado para correr con Maven.)
- El ZIP original incluía un JAR y resultados de 5 pruebas antiguas sin fallos, pero no son evidencia de ejecución del código corregido.
- **No se ejecutó `docker compose up` ni `mvn test`** porque este entorno no contiene Docker ni Maven y no tiene acceso a las dependencias externas.
- Para verificación de extremo a extremo en la computadora del usuario: `INICIAR.cmd` seguido de `.\PROBAR_API.ps1`.

Nota: El ZIP de entrega no incluye el directorio `.git`, las preferencias `.idea`, la carpeta `target` ni `.env` con secretos locales. Docker generará un nuevo JAR a partir de los fuentes cuando construya la imagen.

## Corrección adicional: Docker PostgreSQL (30 de octubre de 2026)

Los logs de la computadora del usuario mostraron `FATAL: password authentication failed for user "pixelworks"`. La API no podía arrancar JPA/Hibernate porque `.env` y el usuario persistido en PostgreSQL tenían contraseñas diferentes.

- `INICIAR.ps1` inicia primero `db`, espera su disponibilidad, detecta si el volumen proviene de la API original con el usuario `postgres` o de la revisión con `pixelworks`, sincroniza `DB_PASSWORD` con `ALTER ROLE` desde la conexión local por socket y valida el acceso TCP real antes de compilar/iniciar `api`.
- La contraseña viaja a `psql` por `stdin`, no se imprime y no se incluye en el ZIP.
- `docker-compose.yml` fija el nombre del proyecto `pixelworks-api` para conservar el volumen existente al mover la carpeta.
- `INICIAR.cmd` deja la ventana abierta para mostrar mensajes de error o el resultado.
- La API no se anuncia lista hasta que `GET /api/productos` devuelve HTTP 200.
- La cuenta local de demostración `admin@pixelworks.local` se sincroniza con la contraseña del `.env` en modo demo, sin modificar cuentas de otros usuarios.

**Límite de verificación:** no hay motor Docker ni PowerShell en el entorno de preparación; el procedimiento de recuperación requiere una ejecución real en Windows para confirmar el resultado en el equipo del usuario. No se ejecuta ningún `down -v` y no se borran volúmenes.


## Ajuste de arranque para PowerShell 5.1

Corregida la deteccion de roles de PostgreSQL en `INICIAR.ps1`: un rol no existente ahora no genera un `NativeCommandError` que interrumpe la deteccion del rol `postgres`. La prueba del rol se ejecuta con `sh` dentro del contenedor y devuelve la senal `SELECT 1` solo cuando existe.


## V3: reparacion del arranque en Windows PowerShell 5.1

Se elimino la deteccion de roles mediante `sh -c` y argumentos posicionales, que en Windows transmitian el usuario `_` a PostgreSQL. Ahora la deteccion usa `docker compose exec -T db psql` directamente, y suprime los errores esperados de roles inexistentes sin abortar PowerShell. La prueba TCP usa el usuario y la clave de `.env`; se espera la disponibilidad de PostgreSQL despues de reutilizar una base con rol `postgres`. Se conservan los volumenes.

**Limitacion de verificacion:** no se dispone de Docker Desktop ni PowerShell para Windows en este entorno: la validacion fue estatica y estructural, no una prueba de arranque real.
