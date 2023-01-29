# 📜 Canal de moderación

Esta función ha sido hecha especialmente para el servidor de Ibai, y es muy posible que no tenga un "uso lógico" fuera de este.

### Funcionamiento

El canal de moderación es un canal de texto por el que los moderadores pueden poner sanciones  de tipo `BAN` (`TEMP BAN`) y `MUTE` (`TEMP MUTE`) sin prefijo ni comandos.\
Por ejemplo: si un usuario pone lo siguiente por el canal: `339129842908004354 Prueba` el bot añadirá 2 botones al mensaje, uno para banear, y otro para silenciar, después de eso el usuario puede hacer click en el botón que quiera.

### Sanciones temporales

Esta función también admite sanciones temporales, simplemente se ha de añadir la duración de la sanción al mensaje que se mande, da igual el orden de los argumentos, siempre va a diferenciar entre usuarios, razón y duración

### Múltiples usuarios

Se puede sancionar a múltiples usuarios añadiendo sus IDs de usuario en el mensaje, por ejemplo el mensaje `339129842908004354 744375365631213678 1d Prueba` añadirá botones para banear o silenciar temporalmente durante **1 día** a dos usuarios.

### Imágenes y archivos

Si se desea, se pueden añadir todo tipo de adjuntos al mensaje, estos entrarán dentro de la razón de la sanción.

## Ejemplo de uso

En este ejemplo, se va a silenciar a 2 usuarios durante **1 hora** con la razón: `Prueba`

<figure><img src="../.gitbook/assets/expo.gif" alt=""><figcaption></figcaption></figure>
