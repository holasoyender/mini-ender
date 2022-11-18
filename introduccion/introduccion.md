---
description: ¿Que es mini-ender?
---

# Introducción

Mini ender es un bot de uso general escrito en **Kotlin** y **Java** dividido en distintos sub-modulos.\
Cada módulo de especializa en una sola cosa y es independiente de los demás aunque funcionan en paralelo.

El proyecto es público, eso significa que cualquier persona podrá añadir el bot a su servidor, aunque su uso sea pensado especialmente para el servidor de Discord de **Ibai**. También es open source, y cualquiera puede acceder y contribuir en el [**repositorio de GitHub.**](https://github.com/holasoyender/mini-ender)

{% hint style="warning" %}
Tanto esta página como el bot pueden estar sujetos a cambios en un futuro, y algunos de estos pueden romper funcionalidades existentes.
{% endhint %}

Por defecto gran parte de los módulos del bot están deshabilitados y deben de activarse manualmente, esto es una medida de seguridad por si el bot se añade al servidor con permisos de administrador.

#### Prefijo por defecto

El prefijo por defecto del bot es `-` para los comandos por mensaje, aunque también cuenta con comandos de slash, estos no se pueden modificar de ninguna manera. Puedes comprobar en cualquier momento el prefijo del bot mencionándole en cualquier canal de texto, ej: _`@mini ender#1186`_

### :warning: Sistema interno de avisos del servidor

Cuando surge algún error interno en el bot relacionado con el servidor este los guarda en una base de datos, en fin de esto es que los administradores revisen los errores para intentar arreglarlos.

Para listar todos los avisos activos del servidor se usa el comando `warnings`. Cada aviso tiene un grado de severidad, entre `LOW` y `CRITICAL` (mostrado por los colores) y una cuenta de las veces que ha aparecido.

### Requisitos al ejecutar un comando

Cada comando tiene una forma de uso única, algunos requieren unos permisos especiales por parte del usuario, otros por parte del bot y otros necesitar argumentos específicos.

#### Argumentos entre signos ( < > )

Los argumentos entre los signos `<>` son obligatorios, y su ausencia causará un error en el comando, sin estos el comando no puede funcionar.

Ejemplo:

`-avisar 381194051686498315`

#### Argumentos entre corchetes ( \[ ] )

Los argumentos entre los signos `[ ]`son opcionales y en gran parte de los casos son reemplazados por un mensaje por defecto, por ejemplo en el comando avisar si no se especifica una razón esta pasará a ser `Sin razón`

Ejemplo:

`-avisar 381194051686498315 Spam`
