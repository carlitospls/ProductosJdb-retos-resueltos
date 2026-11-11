# GUÍA DE RETOS · Examen UD2 DWES (Spring MVC + Thymeleaf + JDBC)

Material de apoyo para el examen (sin IA, con apuntes y proyectos en local).

**Qué tienes:**

| Material | Qué es |
|---|---|
| `ProductosJdbc-retos-resueltos` | El código de clase de la profe (10-nov) con los **retos 1, 2, 3 y 4 resueltos** y comentados paso a paso |
| `BibliotecaJdbc-retos-resueltos` | Mi BibliotecaJdbc (con mis marcas `>>> EXAMEN`) + el **reto del año de nacimiento del autor resuelto** y comentado |
| Esta guía | Qué es cada reto, cómo reconocerlo en un enunciado sin pistas, el recorrido exacto de archivos y las trampas |

**Cómo buscar en IntelliJ** (`Ctrl+Shift+F`, buscar en todo el proyecto):

| Busco | Encuentro |
|---|---|
| `RETO 4` | Los 10 sitios de la columna nueva (stock), numerados `sitio n/10` |
| `RETO 3` | Los 6 pasos de la regla de no duplicados |
| `RETO 2` | Los 6 pasos de la consulta + página nueva |
| `RETO 1` | Los 6 pasos del contador con JOIN |
| `RETO AUTOR` | (Biblioteca) Los 10 sitios + los 2 "extra" del año de nacimiento |
| `>>> EXAMEN` | (Biblioteca) Mis marcas antiguas: filtro, validación, mensaje, regla… |
| `PLANTILLA` | (Productos) Los métodos JDBC que sirven de molde para copiar |

---

## 0. Protocolo (siempre igual)

1. **Arranca el proyecto tal cual** antes de tocar nada. Si no arranca, avisa.
2. Lee todo el enunciado. Para cada petición decide **qué reto es** (tabla de la sección 1).
3. **Una petición cada vez.** Termínala, arranca y pruébala antes de empezar la siguiente.
4. Tras tocar `schema.sql` o `data.sql` → **reiniciar** (la BD en memoria se recrea al arrancar).
5. Capas: controlador → servicio → repositorio (interfaz) → `...Jdbc`. El controlador **nunca** llama a un repositorio. Las reglas van en el servicio.
6. **Un cambio no está terminado hasta probarlo a mano en el navegador** con los casos del enunciado (vacío, fuera de rango, válido, editar, duplicado…) **y** comprobar que lo que ya funcionaba sigue funcionando.
7. Mensajes: copia el texto del enunciado **literal**.
8. Si algo falla: consola de IntelliJ → **último `Caused by:`** → sección 8 de esta guía.

Tiempo orientativo por reto: **25-30 min**. Si a los 40 min uno no sale, déjalo como esté (que compile y arranque) y pasa al siguiente.

---

## 1. ¿Qué reto me están pidiendo? (enunciados sin pistas)

| Si el enunciado dice algo como… | Es… | Sección |
|---|---|---|
| "cada X guarda/tiene ahora su **dato nuevo**", "nuevo campo", "se indica en el alta y en la edición", "el listado muestra una columna…" | **RETO 4** · columna nueva hasta la vista | 3 |
| "**no se permite** / no puede haber dos X con el mismo…", "debe ser único", "no se puede… si…" | **RETO 3** · regla de negocio | 4 |
| "al **pulsar** en X se muestra **una página** con sus…", "nueva vista", "consultar los Y de un X" | **RETO 2** · consulta + página nueva | 5 |
| "mostrar **cuántos** Y tiene cada X", "número de…", "total de…" | **RETO 1** · contar con JOIN | 6 |
| "obligatorio", "entre A y B", "máximo N caracteres", "el error aparece junto al campo y no se pierden los datos" | Validación (casi siempre va dentro de un RETO 4) | 3 · sitio 8 |

Un mismo apartado puede mezclar dos (p. ej. columna nueva + validación + que salga en otra vista). Divídelo y hazlo por partes.

---

## 2. Diccionario: lo mismo con otros nombres

El proyecto del examen puede usar cualquiera de estos estilos. **Mira cómo se llaman las cosas en el que te den** y traduce.

| Concepto | Productos (clase 10-nov) | Productos (ALUMNO / mi repo) | Biblioteca |
|---|---|---|---|
| Tabla padre (lado 1) | `fabricante` (PK `codigo`) | igual | `autor` (PK `id`) |
| Tabla hija (lado N) | `producto` (`codigo_fabricante`) | igual | `libro` (`autor_id`) |
| DTO del formulario hija | `ProductoDto` | `ProductoForm` | `LibroDto` |
| DTO del formulario padre | `FabricanteDto` | no hay: el formulario usa `Fabricante` directamente | `AutorDto` |
| Conversión modelo ↔ DTO | métodos `toDto` / `toModelEntity` **dentro del DTO** | igual | clase aparte: `mapper/AutorMapper`, `mapper/LibroMapper` |
| Textos de validación | escritos en la anotación (`message = "texto"`) | igual | `messages.properties` (`message = "{clave}"`) |
| SELECT de la hija | constante `SELECT_BASE` (con JOIN) | igual | `SELECT_BASE` (con JOIN) |
| SELECT del padre | 2 SELECT sueltos (`findById`, `findAll`) | igual | 2 SELECT sueltos |
| Lista del `<select>` del formulario | `model.addAttribute("fabricantes", …)` a mano en cada rama | igual | método `@ModelAttribute("autores")` del controlador (se carga sola) |
| Qué recibe el listado | objetos del modelo (`Producto`) | igual | DTOs (`AutorDto`, `LibroDto`) convertidos con el mapper |
| Borrar padre con hijos | prohibido (`FabricanteConProductosException`) | igual | permitido: borra en cascada (`ON DELETE CASCADE`) |
| Regla de duplicados ya hecha | `FabricanteDuplicadoException` (RETO 3) | no | `IsbnDuplicadoException` (libro) |
| Estilos | Bootstrap por internet: **sin conexión se ve sin estilos; no es un fallo tuyo** | igual | Bootstrap en local |

---

## 3. RETO 4 · Columna nueva llevada hasta la vista

**Qué es:** un dato nuevo de una entidad. Hay que añadirlo a la tabla y hacer que viaje en los dos sentidos: BD → pantalla (verlo) y pantalla → BD (guardarlo). "Un dato nuevo obliga a recorrer todas las capas."

**Ejemplos:** `stock` del producto (Productos) · año de nacimiento del autor (Biblioteca).

### Recorrido: los 10 sitios

| # | Qué haces | Productos (`stock` en producto) | Biblioteca (`anio_nacimiento` en autor) |
|---|---|---|---|
| 1 | La columna en la tabla | `schema.sql` → `producto` | `schema.sql` → `autor` (ojo a la coma de la línea anterior) |
| 2 | Valor en **todos** los INSERT | `data.sql` | `data.sql` |
| 3 | El campo en el modelo | `model/Producto` | `model/Autor` |
| 4 | **Pedir** la columna en el SELECT | `ProductoRepositoryJdbc` → `SELECT_BASE` (1 sitio) | `AutorRepositoryJdbc` → **los 2 SELECT** (`findById` y `findAll`) |
| 5 | **Leerla** en `mapRow` | `rs.getInt("stock")` | `rs.getInt("anio_nacimiento")` |
| 6 | Columna en el listado | `productos/lista.html` (`<th>` + `<td>` + `colspan`) | `autores/lista.html` |
| 7 | Campo en el formulario | `productos/formulario.html` | `autores/formulario.html` |
| 8 | Campo + validación en el DTO | `dto/ProductoDto` | `dto/AutorDto` + claves en `messages.properties` |
| 9 | Conversión modelo ↔ DTO | `ProductoDto.toDto()` y `toModelEntity()` | `mapper/AutorMapper.toDto()` y `toModelEntity()` |
| 10 | Guardar | `save()` y `update()` | `save()` y `update()` |
| EXTRA | Otros `new X(...)` que dejan de compilar | — | `LibroRepositoryJdbc.mapRow` y `LibroMapper.toModelEntity` (pasar `null`) |

**Orden de trabajo en dos bloques:**

- **Bloque A (verlo):** 1, 2, 3, 4, 5, 6 **+** lo mínimo del 8 y 9 para que compile (el campo en el DTO y el `new X(...)` de `toModelEntity`) → arrancar → **¿sale la columna en el listado?**
- **Bloque B (guardarlo):** 7, 8 (validaciones), 9 (`toDto`), 10 → arrancar → probar alta, edición y errores.

### Código clave (lo que cambia en cada sitio)

```sql
-- 1  schema.sql
stock INT NOT NULL,
-- 2  data.sql
INSERT INTO producto (nombre, precio, stock, codigo_fabricante) VALUES ('Disco duro', 86.99, 25, 5);
```
```java
// 3  modelo
private int stock;                       // (en Biblioteca: private Integer anioNacimiento;)
// 4  SELECT
SELECT p.codigo, p.nombre, p.precio, p.stock, ...
// 5  mapRow: en la posición del campo en el constructor
rs.getInt("stock"),
// 8  DTO
@NotNull(message = "El stock es obligatorio")
@PositiveOrZero(message = "El stock no puede ser negativo")
private Integer stock;
// 9  conversión
form.setStock(p.getStock());                                     // toDto
return new Producto(codigo, nombre, precio, stock, fabricante);  // toModelEntity
// 10  save: columna + ? + setX
"INSERT INTO producto (nombre, precio, stock, codigo_fabricante) VALUES (?, ?, ?, ?)"
ps.setInt(3, producto.getStock());
// 10  update: el ? del WHERE es el último -> renumerar
"UPDATE producto SET nombre = ?, precio = ?, stock = ?, codigo_fabricante = ? WHERE codigo = ?"
ps.setInt(3, producto.getStock());  ps.setInt(4, ...getFabricante().getCodigo());  ps.setInt(5, producto.getCodigo());
```
```html
<!-- 6  lista.html -->
<th>Stock</th>            ...            <td th:text="${p.stock}">0</td>
<!-- 7  formulario.html: el bloque de otro campo copiado, cambiando el nombre en los 4 sitios -->
<div class="mb-3">
    <label for="stock" class="form-label">Stock</label>
    <input type="number" step="1" th:field="*{stock}" class="form-control"
           th:classappend="${#fields.hasErrors('stock')} ? 'is-invalid'">
    <div class="invalid-feedback" th:errors="*{stock}">Error</div>
</div>
```

### Según el tipo del dato

| Dato | SQL | Java (modelo / DTO) | Leer | Escribir | Validación típica | `<input>` |
|---|---|---|---|---|---|---|
| Entero (stock, año) | `INT` | `Integer` | `rs.getInt` | `ps.setInt` | `@NotNull` `@Min` `@Max` `@PositiveOrZero` | `type="number"` |
| Decimal (precio) | `DOUBLE` | `Double` | `rs.getDouble` | `ps.setDouble` | `@NotNull` `@Positive` | `type="number" step="0.01"` |
| Texto (país, email) | `VARCHAR(n)` | `String` | `rs.getString` | `ps.setString` | `@NotBlank` `@Size(max=n)` | `type="text"` |
| Fecha | `DATE` | `LocalDate` | `rs.getObject("col", LocalDate.class)` | `ps.setObject(n, fecha)` | `@NotNull` `@PastOrPresent` + `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` | `type="date"` |

**Las anotaciones dicen lo que SE PERMITE:** "no negativo" = `@PositiveOrZero`; "mayor que 0" = `@Positive`; "entre 1000 y 2026" = `@Min(1000) @Max(2026)`.

### Trampas

- **`toDto` es silenciosa:** si falta su línea no hay error; el campo sale vacío al editar (y en Biblioteca, además, la columna del listado sale vacía, porque el listado usa DTOs).
- **`update` es silenciosa:** si no incluye la columna, guarda sin error pero el dato no cambia.
- **Orden del constructor:** `@AllArgsConstructor` = orden de los campos. Java empareja por **posición**: dos valores del mismo tipo cruzados compilan y muestran los datos cambiados.
- **SELECT ↔ `rs.getX`:** cada `getX("col")` necesita `col` en el SELECT. Si no: compila y da "Error de base de datos" al abrir la página (`Column "COL" not found`).
- **`NOT NULL`:** si falta en un INSERT de `data.sql`, no arranca; si falta en `save()`, error al crear.
- **Columna en la tabla padre** (año del autor): al cambiar el constructor del padre también dejan de compilar los archivos de la hija que crean un padre (`Ctrl+Shift+F` → `new Autor(`). En el DTO y el modelo usa `Integer` para poder pasar `null`.
- **`messages.properties`** (Biblioteca): cada `{clave}` necesita su línea; si no, en pantalla sale la clave entre llaves.

### Pruebas a mano

- [ ] El listado muestra la columna con los valores de `data.sql`.
- [ ] Alta con el campo vacío / con valor no permitido → mensaje junto al campo y **no se pierde** lo demás.
- [ ] Alta válida → aparece en el listado con su valor.
- [ ] **Editar** → el campo sale **relleno** con el valor actual. Cambiarlo → el listado muestra el nuevo.
- [ ] Lo que ya funcionaba sigue igual (en Biblioteca: listado de libros, crear y editar un libro).

---

## 4. RETO 3 · Regla de negocio: no permitir duplicados

**Qué es:** antes de guardar, comprobar una condición contra la BD; si no se cumple, **no se guarda** y el usuario ve el error **junto al campo**, sin perder lo que escribió.

**Ejemplo:** no puede haber dos fabricantes con el mismo nombre, sin distinguir mayúsculas.

**Reparto por capas (lo que se evalúa):** el repositorio solo responde *¿existe?* · el **servicio** decide y lanza la excepción · el controlador la traduce a un error del formulario.

### Recorrido: 6 pasos

| # | Archivo | Qué haces | Copia de… |
|---|---|---|---|
| 1 | `exception/FabricanteDuplicadoException` | Clase nueva: `extends RuntimeException`, mensaje en `super(...)` | `FabricanteConProductosException` |
| 2 | `FabricanteRepository` (interfaz) | Declarar `existsByNombreIgnoreCase(String)` y `existsByNombreIgnoreCaseAndCodigoNot(String, Integer)` | — |
| 3 | `FabricanteRepositoryJdbc` | Implementarlas: `SELECT 1 … WHERE LOWER(nombre) = LOWER(?)` (la 2ª: `+ AND codigo <> ?`) → `return rs.next();` | `existsByFabricanteCodigo` |
| 4 | `FabricanteService.crear` | Tras quitar espacios y **antes** de `save`: `if (repo.existsBy…(nombre)) throw new …` | — |
| 5 | `FabricanteService.actualizar` | Igual, con la versión `…AndCodigoNot(nombre, codigo)` | — |
| 6 | `FabricanteController.guardar` | `try { alta/edición } catch (…Exception e) { result.rejectValue("nombre", "", e.getMessage()); return "fabricantes/formulario"; }` | — |

### Código clave

```java
// 1
public class FabricanteDuplicadoException extends RuntimeException {
    public FabricanteDuplicadoException(String nombre) {
        super("Ya existe un fabricante llamado \"" + nombre + "\".");
    }
}
// 3
String sql = "SELECT 1 FROM fabricante WHERE LOWER(nombre) = LOWER(?) AND codigo <> ?";
ps.setString(1, nombre);
ps.setInt(2, codigo);
try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
// 4
fabricante.setNombre(fabricante.getNombre().strip());
if (fabricanteRepository.existsByNombreIgnoreCase(fabricante.getNombre())) {
    throw new FabricanteDuplicadoException(fabricante.getNombre());
}
fabricanteRepository.save(fabricante);
// 6
try {
    if (fabricanteDto.getCodigo() == null) { ...crear... } else { ...actualizar... }
} catch (FabricanteDuplicadoException e) {
    result.rejectValue("nombre", "", e.getMessage());
    return "fabricantes/formulario";
}
return "redirect:/fabricantes";
```

### Trampas

- **Edición:** sin `AND codigo <> ?` el registro choca consigo mismo al guardar sin cambios.
- `rejectValue` con **3 argumentos** (campo, código, mensaje). El campo es donde debe verse el error (`"nombre"`).
- El `return` del formulario va **dentro del `catch`**; si no, llega al `redirect` y el error no se ve.
- Si el formulario tiene un `<select>` y el proyecto carga su lista a mano (`ProductoController`), hay que **recargarla en el Model dentro del `catch`**.
- No tratar esta excepción en el `GlobalExceptionHandler`: mandaría a la página de error y se perderían los datos (la profe lo dejó comentado a propósito).
- La regla, **en el servicio**. En el controlador funcionaría, pero no puntúa.

### Variantes

- **Dos campos** (mismo título en la misma plataforma): `WHERE LOWER(titulo) = LOWER(?) AND codigo_plataforma = ?`. Lo tengo resuelto en mi `SimulacroEXSpring`.
- **En Biblioteca** (autor duplicado): `AutorDuplicadoException` · `AutorRepository` + `Jdbc` con `… AND id <> ?` · `AutorService.crear/actualizar` · `AutorController.guardar`. Biblioteca ya tiene una regla igual para copiar: `IsbnDuplicadoException` + `LibroService.crear` + el `catch` de `LibroController.guardar` (usa una clave de `messages.properties`: `rejectValue("isbn", "libro.isbn.duplicado")`).
- **Otra regla** ("no se puede borrar un X con Y"): mismo esquema; ya está en `FabricanteService.borrar` + `FabricanteConProductosException`, mostrando el error con un mensaje flash en el listado.

### Pruebas a mano

- [ ] Alta con un nombre que ya existe → error junto al campo, no se crea.
- [ ] El mismo nombre en minúsculas/mayúsculas → también da error.
- [ ] Alta con nombre nuevo → se crea.
- [ ] Editar sin cambiar el nombre → guarda sin error.
- [ ] Editar poniendo el nombre de otro → error.

---

## 5. RETO 2 · Consulta nueva + página nueva

**Qué es:** una consulta con `WHERE` que devuelve una lista, y una **URL nueva con un dato dentro** que la muestra en una **plantilla nueva**, enlazada desde un listado.

**Ejemplo:** al pulsar el nombre de un fabricante se abre `/fabricantes/{codigo}/productos` con sus productos.

### Recorrido: 6 pasos

| # | Archivo | Qué haces | Copia de… |
|---|---|---|---|
| 1 | `ProductoRepository` (interfaz) | Declarar `List<Producto> findByFabricanteCodigo(Integer codigoFabricante);` | — |
| 2 | `ProductoRepositoryJdbc` | `SELECT_BASE + "WHERE p.codigo_fabricante = ? ORDER BY p.nombre"` + `ps.setInt(1, …)` | `findAll()` |
| 3 | `ProductoService` | `listarPorFabricante(codigo)`: comprobar que el fabricante existe y llamar al repositorio | `buscarPorCodigo` |
| 4 | `FabricanteController` | Inyectar `ProductoService` · `@GetMapping("/{codigo}/productos")` + `@PathVariable` · 2 `addAttribute` · `return "fabricantes/productosDelFabricante";` | `editar(...)` |
| 5 | `templates/fabricantes/productosDelFabricante.html` | Plantilla nueva | `productos/lista.html` recortada |
| 6 | `fabricantes/lista.html` | El nombre pasa a ser un enlace a la página | el enlace de Editar |

### Código clave

```java
// 2  (findAll con WHERE y un ?; el ResultSet en un segundo try)
String sql = SELECT_BASE + "WHERE p.codigo_fabricante = ? ORDER BY p.nombre";
List<Producto> productos = new ArrayList<>();
try (Connection con = dataSource.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {
    ps.setInt(1, codigoFabricante);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) { productos.add(mapRow(rs)); }
    }
} catch (SQLException e) {
    throw new AccesoDatosException("Error al listar los productos del fabricante " + codigoFabricante, e);
}
return productos;
// 3
public List<Producto> listarPorFabricante(Integer codigoFabricante) {
    fabricanteRepository.findById(codigoFabricante).orElseThrow(
            () -> new RecursoNoEncontradoException("No existe el fabricante con código " + codigoFabricante));
    return productoRepository.findByFabricanteCodigo(codigoFabricante);
}
// 4
@GetMapping("/{codigo}/productos")
public String productosDelFabricante(@PathVariable("codigo") Integer codigo, Model model) {
    model.addAttribute("fabricante", fabricanteService.buscarPorCodigo(codigo));
    model.addAttribute("productos", productoService.listarPorFabricante(codigo));
    return "fabricantes/productosDelFabricante";
}
```
```html
<!-- 6 -->
<a th:href="@{/fabricantes/{codigo}/productos(codigo=${f.codigo})}" th:text="${f.nombre}">Nombre</a>
<!-- 5: título + tabla -->
<span th:text="${fabricante.nombre}"></span>
<tr th:if="${#lists.isEmpty(productos)}"><td colspan="3">Este fabricante no tiene productos</td></tr>
<tr th:each="p : ${productos}"> <td th:text="${p.codigo}">1</td> ... </tr>
```

### Trampas

- El `return` del controlador tiene que ser **exactamente** la ruta del archivo en `templates/` (sin `.html`). Si no: `Error resolving template`.
- La clase ya tiene `@RequestMapping("/fabricantes")`: en el método solo va `"/{codigo}/productos"`.
- Las etiquetas de `addAttribute` tienen que coincidir con los `${...}` de la plantilla. Si no coinciden no hay error: la tabla sale vacía.
- Declarar en la interfaz y no implementar en `...Jdbc` = no compila (es el recordatorio).
- Pasar el fabricante aparte al Model: si no tiene productos, el título sigue saliendo.
- En la plantilla nueva conserva los `th:replace` del layout y ajusta el `colspan`.

### En Biblioteca (libros de un autor: `/autores/{id}/libros`)

`LibroRepository.findByAutorId` (`SELECT_BASE + "WHERE l.autor_id = ? ORDER BY l.titulo"`) → `LibroService.listarPorAutor` → `AutorController` (inyectar `LibroService`; y `LibroMapper` si quieres pasar DTOs como hace el resto del proyecto) con `@GetMapping("/{id}/libros")` → `templates/autores/libros.html` copiando `libros/lista.html` **sin el formulario de filtro** → enlace en `autores/lista.html`: `@{/autores/{id}/libros(id=${a.id})}`.

### Pruebas a mano

- [ ] El enlace del listado lleva a la página y muestra solo los productos de ese fabricante.
- [ ] Un fabricante sin productos muestra el mensaje de "no tiene".
- [ ] Escribir a mano un código que no existe (`/fabricantes/999/productos`) → página de "No encontrado".
- [ ] El resto del listado (editar, borrar) sigue funcionando.

---

## 6. RETO 1 · Contar con JOIN (dato calculado en un listado)

**Qué es:** el listado muestra un número que **no está en ninguna columna**: se calcula con `COUNT`. Como el modelo no tiene dónde guardarlo, hace falta un **DTO nuevo** y una consulta nueva con su propio mapeo.

**Ejemplo:** el listado de fabricantes muestra cuántos productos tiene cada uno.

### Recorrido: 6 pasos

| # | Archivo | Qué haces | Copia de… |
|---|---|---|---|
| 1 | `dto/FabricanteResumen` | DTO nuevo: `codigo`, `nombre`, `numProductos` (`@Data @AllArgsConstructor`) | un modelo |
| 2 | `FabricanteRepository` (interfaz) | Declarar `List<FabricanteResumen> findAllWithNumProductos();` | — |
| 3 | `FabricanteRepositoryJdbc` | El SQL con `LEFT JOIN` + `COUNT` + `GROUP BY` y `new FabricanteResumen(...)` por fila | `findAll()` |
| 4 | `FabricanteService` | Método que llama al repositorio | `listar()` |
| 5 | `FabricanteController.listar` | Cambiar `fabricanteService.listar()` por el método nuevo (misma etiqueta `"fabricantes"`) | — |
| 6 | `fabricantes/lista.html` | `<th>` + `<td th:text="${f.numProductos}">` + `colspan` | otra columna |

### Código clave

```sql
SELECT f.codigo, f.nombre, COUNT(p.codigo) AS num_productos
FROM fabricante f
LEFT JOIN producto p ON p.codigo_fabricante = f.codigo
GROUP BY f.codigo, f.nombre
ORDER BY f.nombre
```
```java
// 1
@Data
@AllArgsConstructor
public class FabricanteResumen {
    private Integer codigo;
    private String nombre;
    private int numProductos;
}
// 3  (dentro del while, en vez de mapRow)
fabricantes.add(new FabricanteResumen(
        rs.getInt("codigo"),
        rs.getString("nombre"),
        rs.getInt("num_productos")));
// 5
model.addAttribute("fabricantes", fabricanteService.findAllWithNumProductos());
```

### Las tres piezas del SQL

- **`LEFT JOIN`**, no `JOIN`: con `JOIN` los fabricantes sin productos **desaparecen** del listado.
- **`COUNT(p.codigo)`**, no `COUNT(*)`: `COUNT(*)` da 1 a los que no tienen productos.
- **`GROUP BY`** con todas las columnas del SELECT que no sean el `COUNT`.
- El alias (`AS num_productos`) es el nombre que usa `rs.getInt("num_productos")`.

Prueba el SQL antes en `/h2-console` (JDBC URL: la de `application.properties`).

### Trampas

- El formulario de editar sigue usando `Fabricante`/`FabricanteDto`: el DTO resumen es **solo para el listado**.
- Si añades una columna al SELECT (p. ej. el año del autor), añádela también al `GROUP BY`, al DTO y al `new …Resumen(...)` en el mismo orden.
- `${f.numProductos}` necesita que el campo del DTO se llame exactamente `numProductos`.

### En Biblioteca (nº de libros por autor)

`dto/AutorResumen(id, nombre, anioNacimiento, numLibros)` → `AutorRepository.findAllWithNumLibros()`:

```sql
SELECT a.id, a.nombre, a.anio_nacimiento, COUNT(l.id) AS num_libros
FROM autor a
LEFT JOIN libro l ON l.autor_id = a.id
GROUP BY a.id, a.nombre, a.anio_nacimiento
ORDER BY a.nombre
```

→ `AutorService` → `AutorController.listar` pasa esa lista directamente (ya no hace falta el `map(autorMapper::toDto)` en ese método) → `autores/lista.html` con `${a.numLibros}`. (Elena Poniatowska tiene 0 libros: es la que comprueba el `LEFT JOIN`.)

### Pruebas a mano

- [ ] Cada fila muestra su número correcto (compáralo con `data.sql`).
- [ ] Los que no tienen ninguno **aparecen** y muestran **0** (Huawei, Xiaomi).
- [ ] Crear un producto de un fabricante → su número sube.
- [ ] Editar y borrar fabricantes sigue funcionando.

---

## 7. Enunciados de práctica (estilo examen, sin pistas)

Hazlos sobre los proyectos **limpios** de la profe y cronométrate (25-30 min cada uno). La solución de los de Productos está en `ProductosJdbc-retos-resueltos`; la del A de Biblioteca, en `BibliotecaJdbc-retos-resueltos`.

**Sobre `producto-jdbc-ALUMNO.zip`** (ojo: ahí el DTO se llama `ProductoForm` y el formulario de fabricante usa `Fabricante` directamente):

- **P1.** La empresa necesita controlar las existencias. Cada producto tiene un stock: número entero, obligatorio y no negativo. Debe poder indicarse al crear y al modificar un producto, y verse en el listado.
- **P2.** No pueden existir dos fabricantes con el mismo nombre (da igual mayúsculas o minúsculas). Si se intenta, se avisa junto al campo y no se pierde lo escrito.
- **P3.** Desde el listado de fabricantes, al pulsar el nombre de uno se muestran sus productos en una página propia, con opción de volver.
- **P4.** El listado de fabricantes muestra cuántos productos tiene cada uno, incluidos los que no tienen ninguno.

**Sobre `biblioteca-jdbc.zip`:**

- **B1.** Cada autor guarda su año de nacimiento: entero, obligatorio, entre 1000 y 2026. Se indica en el alta y en la edición; si no es válido, el error aparece junto al campo sin perder los datos. El listado de autores muestra una columna Nacimiento. *(Resuelto.)*
- **B2.** No pueden existir dos autores con el mismo nombre, sin distinguir mayúsculas.
- **B3.** Desde el listado de autores, al pulsar un autor se muestran sus libros en una página propia.
- **B4.** El listado de autores muestra cuántos libros tiene cada uno.
- **B5 (mezcla).** Cada libro guarda su número de páginas (entero, obligatorio, mayor que 0), visible en el listado de libros y editable. *(Columna en la tabla hija, con mappers y `messages.properties`: usa mis marcas `>>> EXAMEN [COLUMNA NUEVA]`.)*

---

## 8. Si algo falla

| Síntoma | Causa más probable |
|---|---|
| No compila tras añadir un campo | Cambió el constructor: arregla **todos** los `new Clase(...)` (`Ctrl+Shift+F` → `new Clase(`) |
| No compila: "must implement abstract method" | Declaraste un método en la interfaz y falta implementarlo en `...Jdbc` |
| No arranca, error SQL | `schema.sql`/`data.sql` no cuadran: falta la coma, falta la columna en un INSERT, nombre mal escrito |
| "Error de base de datos" al abrir una página | `Column "X" not found` → falta en el SELECT · `Table "X" not found` → nombre de tabla · nº de `?` ≠ nº de `setX` |
| "Error de base de datos" al **crear** | `save()` no incluye la columna nueva `NOT NULL` |
| Guarda pero el dato no cambia | `update()` no incluye la columna |
| Campo **vacío al editar**, sin error | Falta la línea en `toDto` (DTO o mapper) |
| Datos en columnas cambiadas, sin error | Orden cruzado en `mapRow` o en `toModelEntity` |
| El listado sale vacío, sin error | La etiqueta de `addAttribute` no coincide con el `${...}` de la vista |
| La validación rechaza valores buenos | Anotación al revés (dicen lo permitido) |
| En pantalla sale `{alguna.clave}` | Falta esa clave en `messages.properties` |
| `Error resolving template` | El `return` del controlador no coincide con el archivo de `templates/` |
| Error de plantilla al abrir el formulario | `th:field="*{x}"` con un nombre que no existe en el DTO |
| 404 | No hay `@GetMapping` para esa URL (revisa también el `@RequestMapping` de la clase) |
| El `<select>` sale vacío al volver con error | Falta recargar su lista en el Model en esa rama (también en el `catch`) |
| El error de la regla no se ve y redirige | Falta el `return` del formulario dentro del `catch` |
| Tras el error se pierde lo escrito | Hiciste `redirect` en vez de devolver la vista |
| Los cambios de un `.sql` no se ven | No reiniciaste la aplicación |
| La página se ve sin estilos | Productos usa Bootstrap por internet: sin conexión es normal |
