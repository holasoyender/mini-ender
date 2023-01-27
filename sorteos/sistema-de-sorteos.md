---
cover: ../.gitbook/assets/banners sorteo.png
coverY: 0
---

# Sistema de sorteos

{% hint style="warning" %}
Todos los sub-modulos de este proyecto están en constante desarrollo y sujetos a cambios
{% endhint %}

Por comodidad, los comandos para crear y gestionar sorteos son exclusivos de slash commands (`/`)

### `/sorteo crear`

Para crear un sorteo, el comando a usar es `/sorteo crear`, el cual requiere de los siguientes argumentos:

#### Canal

El canal de texto por el que se enviará el mensaje con el que los usuarios podrán entrar al sorteo.

#### Tiempo

La duración del sorteo, por ejemplo `2d` para dos días o `10m` para 10 minutos, la máxima duración del sorteo es de 7 días

#### Ganadores

El número de ganadores del sorteo, el mínimo es 1 ganador y el máximo son 10 ganadores, si hay más ganadores que participantes el sorteo se cancelará

#### Premio

El premio del sorteo que recibirá(n) el o los ganadores del sorteo una vez este acabe.

#### Host

El host del sorteo, es la persona que alojará el sorteo independientemente de quien ejecute el comando

#### Estilo (Opcional)

El estilo que tendrá el embed del sorteo que se enviará al canal, puedes ver más abajo como se ven los diferentes estilos.

### `/sorteo info <ID Mensaje>`

Este comando da toda la información útil a cerca del sorteo cuya ID de mensaje sea la ID especificada como argumento.

### `/sorteo acabar <ID Mensaje>`

Acaba el sorteo cuya ID de mensaje sea la ID especificada como argumento, anunciando a el o los ganadores, si es que hay participantes.

### `/sorteo repetir <ID Mensaje>`

Repetir el sorteo cuya ID de mensaje sea la ID especificada como argumento, si no hay más participantes que los ganadores anteriores este no se repetirá.

## Estilos

#### Por defecto

<figure><img src="https://cdn.discordapp.com/attachments/1026084700189638738/1068638929730932886/image.png" alt=""><figcaption></figcaption></figure>

#### Mínima información

<figure><img src="https://cdn.discordapp.com/attachments/1026084700189638738/1068639050979876864/image.png" alt=""><figcaption></figcaption></figure>

#### Ibai

<figure><img src="https://cdn.discordapp.com/attachments/1026084700189638738/1068639166495203458/image.png" alt=""><figcaption></figcaption></figure>
