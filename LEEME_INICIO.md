# Pixelworks API — Docker + PostgreSQL + Android

Proyecto revisado a partir de `pixelworks-api.zip`, adaptado para las guías **9_2 (Retrofit)** y **9_3 (JWT)**. **No incluye una app Android:** deja la API preparada para conectarla desde Android Studio. La primera construcción de Docker necesita Internet.

## 1. Requisitos en Windows

- Instalar **Docker Desktop** con WSL2 y abrirlo. Comprobar que esté iniciado.
- Tener **Android Studio** y un emulador o teléfono con depuración USB si se va a consumir la API desde Android.
- Extraer el ZIP completo, sin mover archivos fuera de la carpeta `pixelworks-api`.

## 2. Iniciar (modo de prueba local)

Ejecuta **`INICIAR.cmd`** dentro de `pixelworks-api` (doble clic). Este archivo ejecuta `INICIAR.ps1`, que:

1. Comprueba que Docker Compose esté instalado.
2. Si no existe `.env`, **crea uno automáticamente con contraseñas aleatorias** para PostgreSQL, JWT y el administrador de demostración.
3. Arranca primero PostgreSQL **sin borrar los volúmenes**, detecta si el volumen antiguo se creó con el usuario `postgres` o `pixelworks` y sincroniza la contraseña de ese usuario con `.env`. Esto repara el error `FATAL: password authentication failed for user "pixelworks"` causado por un volumen creado anteriormente con otra contraseña.
4. Prueba la conexión PostgreSQL por TCP con la contraseña configurada y luego ejecuta `docker compose up -d --build api` para construir e iniciar Spring Boot.
5. Espera una respuesta HTTP 200 de `/api/productos`. Si falla, muestra automáticamente los últimos logs, en vez de afirmar que todo funciona.
6. Muestra el estado de los contenedores (`docker compose ps`).

La configuración `.env` es privada y **no se incluye en el ZIP**. No la subas a Git ni compartas sus claves. Puedes abrir `.env` localmente para consultar `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` del usuario administrador de prueba.

También puedes ejecutar manualmente, desde PowerShell en la carpeta del proyecto:

```powershell
# Solo si NO existe .env: crea ese archivo mediante INICIAR.cmd primero.
docker compose up -d --build
```

**Explicación:** `docker compose` coordina los contenedores declarados en `docker-compose.yml`, `up` los inicia, `-d` deja el proceso en segundo plano y `--build` recompila la imagen de Spring Boot si se cambió el código. **Para la primera ejecución y para solucionar contraseñas incompatibles, usa `INICIAR.cmd`**, no el comando manual: el script hace una verificación adicional que Docker Compose por sí solo no hace.

## 3. Comprobar que responde

En el navegador de la PC abre:

`http://localhost:8080/api/productos`

Debe aparecer una lista JSON; en una instalación nueva con el modo demo, se crean **tres juegos de ejemplo** con stock de claves. También puedes usar PowerShell:

```powershell
.\PROBAR_API.ps1
```

**Explicación:** el script comprueba el catálogo público, el detalle de un juego, hace `POST /api/auth/login` con el administrador demo y consulta un endpoint protegido enviando el token. No muestra la contraseña ni el token completo.

Si sale error:

```powershell
docker compose ps
docker compose logs --tail 100 api
docker compose logs --tail 100 db
```

**Explicación:** `ps` enseña el estado y los puertos; `logs` muestra mensajes del contenedor; `--tail 100` limita la salida a sus últimas 100 líneas; `api` y `db` seleccionan el servicio.

## 4. Conectar el teléfono a Android Studio

Con el teléfono enchufado por USB, depuración USB activada y con el permiso autorizado, ejecuta:

```powershell
adb devices
adb reverse tcp:8080 tcp:8080
```

`adb devices` verifica el dispositivo conectado. `adb reverse` redirige las conexiones a `localhost:8080` **desde el teléfono** al puerto `8080` **de la PC**. Vuelve a ejecutar `adb reverse` si desconectas el teléfono o se reinicia ADB.

- **Teléfono USB con `adb reverse`**: BASE_URL `http://localhost:8080/api/` e imágenes `http://localhost:8080/images/`.
- **Emulador Android estándar sin `adb reverse`**: BASE_URL `http://10.0.2.2:8080/api/` e imágenes `http://10.0.2.2:8080/images/`.
- En `AndroidManifest.xml` de la **app Android** se necesitan el permiso `android.permission.INTERNET` y, para pruebas HTTP locales, `android:usesCleartextTraffic="true"`. En producción se usa HTTPS.

La API expone el puerto de la PC solo en `127.0.0.1` por seguridad. Si decides usar Wi-Fi local, cambia `API_BIND_HOST=0.0.0.0` en `.env`, reinicia `docker compose up -d` y configura la IP de la PC en Android; **no expongas este servidor de desarrollo a Internet**.

## 5. Contrato REAL de Pixelworks (distinto del ejemplo AutoParts)

Base: `http://localhost:8080/api/`

| Método | Ruta | Acceso |
|---|---|---|
| POST | `auth/register` | Público, crea jugador y devuelve token |
| POST | `auth/login` | Público, correo y contraseña, devuelve token |
| GET | `productos` | Público, catálogo |
| GET | `productos/{id}` | Público, detalle |
| GET | `categorias`, `categorias/{id}` | Público |
| GET | `desarrolladores`, `desarrolladores/{id}` | Público |
| GET | `ofertas`, `ofertas/{id}` | Público |
| GET | `claves/producto/{id}/stock` | Público |
| GET | `metodos-pago` | JWT |
| POST | `productos` | JWT rol ADMIN o DESARROLLADOR |
| PUT | `productos/{id}` | JWT rol ADMIN o DESARROLLADOR |
| DELETE | `productos/{id}` | JWT rol ADMIN o DESARROLLADOR |
| POST | `compras` | JWT rol JUGADOR |
| GET | `compras/mis-juegos` | JWT rol JUGADOR |
| GET | `/images/{archivo}` | Público (fuera de `/api/`) |

Otros endpoints administrativos conservan sus reglas originales. Un jugador registrado no puede crear, editar o eliminar productos, tal como exige Spring Security. No quites `@PreAuthorize` para sortear esa protección.

**Registrar usuario (solicitud JSON)**:

```json
{"nombre":"Jugador de prueba","correo":"jugador@example.com","password":"claveSegura123"}
```

**Login (solicitud JSON)**:

```json
{"correo":"jugador@example.com","password":"claveSegura123"}
```

**Respuesta del login/registro** (valores de ejemplo):

```json
{"token":"JWT_GENERADO_POR_LA_API","correo":"jugador@example.com","nombre":"Jugador de prueba","rol":"JUGADOR"}
```

**Importante:** Pixelworks devuelve el **token directamente en `token`**, no dentro de un objeto `auth` como AutoParts. El campo para identificar al usuario es **`correo`**, no `username`.

Para endpoints protegidos, Retrofit debe enviar `Authorization: Bearer <token>`. Guarda la sesión como en la guía 9_3 (DataStore); distingue `401` de `403`. La autenticación es necesaria incluso cuando el listado del catálogo público no la necesita.

**ProductoDTO** en la respuesta JSON:

```json
{
  "id": 1,
  "nombre": "Juego de demostracion",
  "descripcion": "Descripcion del juego",
  "anioLanzamiento": "2025-01-01",
  "precio": 12.99,
  "imagen": null,
  "categoriaId": 1,
  "desarrolladorId": 1,
  "stock": 3,
  "porcentajeDescuento": null,
  "precioConDescuento": null
}
```

`stock`, `porcentajeDescuento` y `precioConDescuento` son campos calculados por la API (se devuelven en la respuesta, no necesitan enviarse al crear). **El DTO de AutoParts no sirve aquí**.

## 6. Crear y editar productos con imágenes

La API admite dos formatos en los endpoints `POST /api/productos` y `PUT /api/productos/{id}`:

- `Content-Type: application/json` (formato original de Pixelworks, conserva compatibilidad).
- `Content-Type: multipart/form-data` (nuevo, para Android con imágenes):
  - parte `dto`, de tipo **`application/json`**, con `nombre`, `descripcion`, `precio`, `anioLanzamiento`, `categoriaId`, `desarrolladorId`;
  - parte opcional `imagen`, archivo JPG, PNG o WEBP (hasta 10 MB).

Ejemplo de la parte `dto`:

```json
{"nombre":"Nuevo juego","descripcion":"Aventura","precio":15.00,"anioLanzamiento":"2025-10-25","categoriaId":1,"desarrolladorId":1}
```

La API genera un nombre único para cada imagen y devuelve `imagen: "uuid.jpg"`. La app construye la URL:

`http://localhost:8080/images/uuid.jpg`

Si `imagen` llega `null` en una edición, **se conserva la imagen que ya tenía**. Los archivos se guardan en el volumen `pixelworks_uploads` y no se pierden al reiniciar. Si un producto fue creado con una URL externa en `imagen`, Android debe usar esa URL directamente en lugar de anteponer `/images/`.

En Postman: método POST/PUT, `Authorization` -> Bearer Token, `Body` -> `form-data`, `dto` en modo Text con tipo `application/json` e `imagen` en modo File. La parte JSON con `text/plain` será rechazada por Spring.

## 7. Base de datos y administración

Servicios definidos en Compose:

- `db`: PostgreSQL 16, base `pixelworks_db`, puerto local `5433`; datos persistentes en `pixelworks_db_data`.
- `api`: Spring Boot, puerto `8080`; archivos en `pixelworks_uploads`.

La primera instalación desde `INICIAR.cmd` activa `APP_DEMO_ENABLED=true` y prepara un administrador **solo para pruebas**, tres juegos, categorías, desarrollador, un método de pago simulado y claves ficticias. No se realizan cargos reales. En un despliegue real desactiva ese modo, configura un administrador por un procedimiento protegido y usa HTTPS. **No uses la cuenta demo en producción**.

```powershell
docker compose stop        # detiene los contenedores, sin borrarlos
docker compose start       # los inicia nuevamente
docker compose down        # elimina los contenedores y la red, pero conserva volúmenes
docker compose up -d       # vuelve a crearlos usando imágenes existentes
```

**No ejecutar** `docker compose down -v` si deseas conservar los datos: `-v` borra los volúmenes de PostgreSQL y de las imágenes.

**Recuperación de credenciales sin perder datos:** cambiar `POSTGRES_PASSWORD` en Docker Compose **no** modifica por sí solo la contraseña de un volumen existente. Por eso `INICIAR.cmd` detecta también los volúmenes originales que se inicializaron con `postgres` (en vez de `pixelworks`), adapta `DB_USER` en `.env` si es necesario y actualiza la clave del rol existente mediante el socket local de PostgreSQL. Luego verifica el acceso TCP y vuelve a iniciar la API. La contraseña del `ALTER ROLE` se envía a `psql` por entrada estándar; la prueba TCP usa una variable temporal `PGPASSWORD` en el comando de Docker. No se muestra la contraseña en la salida normal. Evita compartir la consola o listas de procesos mientras se ejecuta. El proceso **no elimina volúmenes, tablas, usuarios ni productos**. Es una reparación para la imagen oficial PostgreSQL 16 con autenticación local predeterminada. Si la configuración de autenticación local fue personalizada, el script detiene el arranque e informa del fallo sin borrar nada. El script conserva cualquier `.env` existente; si extraes en otra carpeta y se genera otro `.env`, sincronizará la nueva contraseña al ejecutar el inicio. En modo demostración, únicamente la cuenta reservada `admin@pixelworks.local` puede actualizar su contraseña de prueba desde el nuevo `.env`; las cuentas normales no se modifican.


### Error `role "pixelworks" does not exist` en Windows PowerShell

La version anterior de `INICIAR.ps1` podia detenerse en la deteccion del rol: Windows PowerShell 5.1 trataba como error fatal el mensaje de `psql` al probar un usuario inexistente. Esta version hace la deteccion dentro del contenedor, ignora solo ese error de comprobacion y luego prueba el rol antiguo `postgres`. Si existe, modifica `DB_USER` en `.env`, sincroniza la clave con el volumen persistente y continua con el arranque. **No elimina tablas ni volumenes**. Si se modifico la autenticacion local de PostgreSQL o no existe ninguno de los roles esperados, detiene el proceso sin cambiar la base.

## 8. Alcance de la revisión

Se corrigió el soporte HTTP de imágenes, la persistencia de fotografías, las consultas públicas de detalle, la conservación de imagen en edición, la generación local de credenciales y la inicialización opcional de datos para practicar desde Android. Se respetaron las entidades y la lógica original de ventas, ofertas, roles y claves.

**Verificación disponible en esta revisión:** las clases Java modificadas se compilaron mediante `javac` contra las bibliotecas del artefacto original; se probó el servicio de imágenes desde Java (guardar, leer, rechazar ruta inválida y borrar), y se validó el formato YAML de Docker Compose. El entorno de revisión **no dispone de Docker Desktop, Maven ni acceso de red para descargar imágenes**, por lo que no fue posible iniciar aquí los contenedores ni ejecutar toda la batería de pruebas Maven. El comando y el script de comprobación están incluidos para realizarlos en Windows con Docker instalado.

## Corrección específica V3 — PowerShell de Windows

La versión V2 intentaba detectar el rol con `sh -c` y `$1` dentro de `docker compose exec`, lo que en Windows terminaba consultando el rol `_`. En V3 las consultas de roles y la comprobación TCP llaman a `psql` directamente, sin `sh -c` ni argumentos posicionales. Se espera otra vez a PostgreSQL cuando se reutiliza un volumen antiguo con el rol `postgres`.

Si el volumen usa un nombre de rol diferente de `postgres` o `pixelworks`, el script no lo modifica automáticamente: se detiene sin borrar datos y muestra un mensaje claro.
