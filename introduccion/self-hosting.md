---
description: Guía de self-hosting del bot
---

# 💾 Self hosting

Tanto el código fuente como una versión del bot ya compilada están disponibles en el [repositorio de GitHub](https://github.com/holasoyender/mini-ender).&#x20;

#### Instalación de Java

El bot requiere la versión del SDK de Java `18` o superiór para funcionar correctamente. Esta se puede descargar desde la [página oficial de Oracle](https://www.oracle.com/java/technologies/downloads/).

#### Descargar la versión compilada

Existe una versión ya compilada del bot en la página de releases del repositorio, esta se puede descargar desde el siguiente link:

El bot soporta cualquier sistema operativo basado en **Linux** o **Windows** en cualquier arquitectura.

{% embed url="https://github.com/holasoyender/mini-ender/releases" %}

#### Descargar usando Docker

Está disponible una imagen de Docker del bot para su uso en un contenedor, esta opción es la más recomendable ya que no necesita una instalación de Java en la maquina y proporciona seguridad extra.

En el siguiente link se encuentra la imagen y las instrucciones para el uso de Docker en el bot.

{% embed url="https://hub.docker.com/r/holasoyender/miniender" %}

### Archivo de configuración

El bot requiere un archivo de entorno para iniciarse, este debe de llamarse `.env` y estar en el mismo directorio que el bot. Los campos obligatorios a rellenar se pueden consultar en el [archivo de ejemplo](https://github.com/holasoyender/mini-ender/blob/main/.env.example) o en el siguiente dialogo:

```properties
TOKEN=
PREFIX=
API_TOKEN=
API_URL=

POSTGRES_PASSWORD=
POSTGRES_USER=
POSTGRES_DB=
POSTGRES_HOST=
POSTGRES_SSL=

REDIS_PASSWORD=
REDIS_USER=
REDIS_HOST=

OAUTH2_CLIENT_ID=
OAUTH2_CLIENT_SECRET=

ERROR_CHANNEL_ID=

TWITCH_CLIENT_ID=
TWITCH_CLIENT_SECRET=
```

#### TOKEN

En este campo se debe de establecer en token de Discord del bot, que se encuentra en la página de tu aplicación de [Discord Developers](https://discord.com/developers/applications)

#### PREFIX

Este será el prefijo por defecto del bot, podrá ser modificado por cada servidor.

#### API\_TOKEN

Este es el token de la API de KenaBot, actualmente es de uso privado, por lo que no se debe de rellenar

#### POSTGRES

Esta es la configuración de la base de datos de postgres, en el apartado `PASSWORD` se debe de establecer la contraseña del usuario especificado en el apartado `USER`. El apartado `DB` será el nombre de la base de datos que usará el bot del host especificado en el apartado `HOST`. Por ultimo, el apartado `SSL` será `true` o `false` si la base de datos remota cuenta con un certificado SSL.&#x20;

#### REDIS

Esta configuración es opcional pero recomendable, Redis es una base de datos de caché que hará que el bot sea mucho más rápido, en caso de usar un servidor de Redis remoto se debe de rellenar el apartado `USER` y `PASSWORD`

#### OAUTH2

Estos campos serán la ID del cliente de Discord y su secret para iniciar sesión en la API interna

#### ERROR\_CHANNEL\_ID

Esta será la ID del canal de texto en la que se enviaran los errores reportados con el comando `error`

#### TWITCH

Esta es la configuración del sistema de notificaciones de **Twitch**, junto al apartado `API_URL`, que servirá para los webhooks en el caso de que la API sea expuesta a internet

### Iniciar el bot

Para iniciar el bot se debe de ejecutar el siguiente comando dentro de la carpeta en la que se encuentra tanto el archivo `.jar` del bot como en archivo `.env`

```shell
$ java -jar mini-ender.jar
```

### API Interna

El bot abrirá una API en el puerto `6690`, por la que se podrán acceder a la información de sorteos, infracciones y estado del bot.

No es necesario usarla para el bot, pero si se quiere usar el [módulo de Twitch ](../twitch/integracion-con-twitch.md)es obligatorio tener abierta a internet esta API, y establecer su URL en la variable de entorno `API_URL`&#x20;
