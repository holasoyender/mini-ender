---
description: Sistema interno de avisos del bot
cover: ../.gitbook/assets/banners warnings.png
coverY: 0
---

# Avisos

Mini ender no es a prueba de balas, es posible que falle bajo ciertas circunstancias, es por eso que el bot cuenta con un sistema que recolecta todos los errores que el bot se ha encontrado para que un administrador del servidor pueda arreglarlo

#### Tipos de avisos

Dependiendo de la importancia de la acción, la gravedad del aviso será más o menos importante. Los distintos tipos de gravedad son `VERY LOW`, `LOW`, `MEDIUM`, `HIGH`, `VERY HIGH`, `CRITICAL` y `NONE`

### Listar todos los avisos

La interfaz de avisos del servidor se muestra con el comando `warnings` (`avisos`), si este se ejecuta sin ningún argumento devolverá una lista de todos los avisos que aun no han sido arreglados del servidor.

#### `warnings <ignored/resolved/`[`<tipo de aviso>`](avisos.md#tipos-de-avisos)`>`

Listará todos los avisos que son del tipo especificado, si no se especifica tipo se mostrarán por defecto todos los avisos sin resolver

#### `warnings resolve <ID>`

Resuelve el aviso con la ID que se ha especificado, y dejará de aparecer en la lista de avisos principal

#### `warnings delete <ID>`

Borra permanentemente el aviso, y no aparecerá en ninguna lista

#### `warnings ignore <ID>`

Ignora el aviso con la ID que se ha especificado, en caso de que vuelva a suceder no aparecerá en la lista de avisos

#### `warnings info <ID>`

Da información sobre el aviso con la ID que se ha especificado
