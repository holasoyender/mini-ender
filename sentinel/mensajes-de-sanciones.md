# 📑 Mensajes de sanciones

Los mensajes que reciben los usuarios por mensaje directo cuando reciben una infracción es completamente personalizable en el [archivo de configuración](../config/archivo-de-configuracion.md).

### Sanciones

El mensaje que recibirá un usuario al ser sancionado es siempre el mismo, y puede ser configurado en el apartado `sanction` del módulo de `messages`

Las variables que admite este mensaje son las siguientes:

```
{user} - Nombre del usuario
{userid} - ID del usuario
{server} - Nombre del servidor
{reason} - Razón de la sanción
{sanction} - Tipo de sanción (ej: baneado, expulsado, silenciado)
```

### Mensajes del sistema anti-links

En caso de usar el [sistema de anti-links](antilinks.md) todos los mensajes que se envían a los usuarios pueden ser configurados

#### Nuevo link o link bajo revisión

Cuando un usuario manda un link que aun no ha sido revisado, se le enviará por mensaje directo el mensaje establecido en el apartado `anti_links_new_link` del módulo de `messages`. Si el link aun está siendo revisado el mensaje que enviará será el del apartado `anti_links_under_revision`

Las variables que admite este mensaje son las siguientes:

```
{user} - Nombre del usuario
{userid} - ID del usuario
{server} - Nombre del servidor
{channel} - Mención del canal de texto
{message} - El mensaje que ha mandado el usuario
{domain} - El dominio que aun no ha sido verificado
```

#### Sanción por el sistema de anti-links

El mensaje que recibirá el usuario que ha enviado un mensaje con un domino bloqueado se puede configurar en el apartado `anti_links_sanction` del módulo de `messages`

Las variables que admite este mensaje son las siguientes:

```
{user} - Nombre del usuario
{userid} - ID del usuario
{server} - Nombre del servidor
{sanction} - Tipo de sanción (ej: baneado, expulsado, silenciado)
{domain} - El dominio que aun no ha sido verificado
```
