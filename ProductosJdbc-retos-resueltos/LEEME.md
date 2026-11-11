# ProductosJdbc · retos resueltos y comentados

**Qué es:** el código que la profe dejó tras la clase del 10-nov (`logistica-src-clase10Nov.zip`: retos 1, 2, 3 y 4)
con el `pom.xml` y el wrapper de `producto-jdbc-ALUMNO.zip`, más comentarios que dicen a qué reto y a qué paso
pertenece cada trozo. El recorrido de cada reto está en `GUIA-RETOS.md`.

**Abrir:** IntelliJ → File → Open → esta carpeta → esperar a Maven → ejecutar `ProductoJdbcApplication` →
`http://localhost:8080`. (Sin internet la página se ve sin estilos: usa Bootstrap por CDN. Funciona igual.)

**Buscar** (`Ctrl+Shift+F`): `RETO 1`, `RETO 2`, `RETO 3`, `RETO 4`, `PLANTILLA`.

| Reto | Qué es | Pasos |
|---|---|---|
| RETO 4 | Columna `stock` hasta la vista | 10 sitios |
| RETO 3 | No permitir dos fabricantes con el mismo nombre | 6 pasos |
| RETO 2 | Página con los productos de un fabricante | 6 pasos |
| RETO 1 | Nº de productos por fabricante en el listado | 6 pasos |

**Tres cambios respecto al código de clase** (el resto es suyo, solo con comentarios añadidos):

1. `productos/lista.html`: añadida la columna **Stock** (en clase no se llegó a poner en el listado).
2. `ProductoDto.stock`: de `int` a `Integer`, con `@NotNull` + `@PositiveOrZero` (en clase: `int` con `@Positive @Min(1) @Max(100)`;
   con eso no se podían editar los productos con stock 0 de `data.sql`). El rango de la profe queda en un comentario.
3. `productos/formulario.html`: `step="1"` en el campo stock.

**Comprobado:** tipos de todo el Java (compilación contra versiones simuladas de Spring/Lombok), todas las sentencias SQL
contra una BD de prueba, y las plantillas (etiquetas y nombres de campos). **No comprobado:** arrancarlo de verdad con
Spring Boot (no tenía Maven disponible). Arráncalo y haz las "pruebas a mano" de la guía.
