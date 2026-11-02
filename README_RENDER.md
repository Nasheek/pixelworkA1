# Pixelworks API en Render

El repositorio incluye el código fuente, las pruebas, Maven y un `Dockerfile` con Java 21. Las instrucciones locales están en [LEEME_INICIO.md](LEEME_INICIO.md).

## Servicio web

1. Crea una base PostgreSQL y un **Web Service** en Render.
2. Conecta este repositorio y selecciona la rama `main`.
3. Selecciona **Docker**, con el contexto en la raíz y el archivo `./Dockerfile`. El Dockerfile ya contiene la compilación y el comando de arranque.
4. Configura las variables siguientes en **Environment** antes del primer despliegue.
5. Para comprobar el servicio, utiliza `GET /api/productos` como Health Check Path. La raíz `/` requiere autenticación.

| Variable | Valor en Render |
| --- | --- |
| `SERVER_PORT` | `10000` |
| `DB_HOST` | Host de la base PostgreSQL desplegada; usa el host interno si está en la misma región de Render |
| `DB_PORT` | Puerto de PostgreSQL, normalmente `5432` |
| `DB_NAME` | Nombre real de la base desplegada |
| `DB_USER` | Usuario de la base desplegada |
| `DB_PASSWORD` | Contraseña de esa base, almacenada únicamente en Render |
| `JWT_SECRET` | Secreto aleatorio nuevo de al menos 32 bytes, almacenado únicamente en Render |
| `APP_DEMO_ENABLED` | `false` para desactivar los datos y el administrador de demostración |
| `JPA_DDL_AUTO` | `update`, comportamiento actual del proyecto |
| `JPA_SHOW_SQL` | `false` |

La aplicación ya escucha en `0.0.0.0`. Su configuración utiliza `SERVER_PORT` y las variables `DB_*`; no interpreta automáticamente una variable `DATABASE_URL` con formato `postgresql://...`.

## Credenciales y archivos

**No subas `.env` a GitHub.** `.env.example` contiene la plantilla sin secretos. Configura los valores de producción en Render; las credenciales de PostgreSQL local no conectan con la base de Render. No necesitas un archivo `.env` dentro de la imagen Docker.

El `docker-compose.yml` y los scripts `INICIAR.*` sirven para ejecutar el proyecto localmente. Para este despliegue, Render construye el Dockerfile y PostgreSQL se configura como servicio aparte.

Si se activa voluntariamente el modo demo, también deben configurarse `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` en Render.

## Imágenes y datos persistentes

El Dockerfile guarda imágenes en `/app/uploads`. El almacenamiento normal de Render es temporal: los archivos nuevos pueden perderse en un reinicio o despliegue. Para conservar imágenes con el almacenamiento actual, configura un disco persistente montado en `/app/uploads` en un plan compatible, o adapta el almacenamiento a un servicio externo.

La subida del código a GitHub no copia los registros ni los volúmenes de tu PostgreSQL local. Si necesitas esos datos, realiza una migración separada a la base desplegada.

## Referencias oficiales

- [Servicios web y puertos](https://render.com/docs/web-services)
- [Variables de entorno y secretos](https://render.com/docs/configure-environment-variables)
- [Discos persistentes](https://render.com/docs/disks)
