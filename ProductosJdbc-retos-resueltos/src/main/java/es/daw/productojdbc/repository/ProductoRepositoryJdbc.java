package es.daw.productojdbc.repository;

import es.daw.productojdbc.exception.AccesoDatosException;
import es.daw.productojdbc.model.Fabricante;
import es.daw.productojdbc.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductoRepositoryJdbc implements ProductoRepository {

    /*
     * SELECT común a findById y findAll.
     * Hacemos JOIN con fabricante para traer también su nombre y poder construir
     * el objeto Fabricante dentro de cada Producto.
     * Los alias (f_codigo, f_nombre) evitan la ambigüedad: las dos tablas tienen "codigo" y "nombre".
     */
    // >>> RETO 4 [COLUMNA NUEVA] · sitio 4/10 · PEDIR la columna a la BD (p.stock).
    //     Cada rs.getX("col") de mapRow (sitio 5) necesita su "col" en este SELECT: son pareja.
    //     Si falta aquí: compila, pero al abrir el listado sale "Error de base de datos"
    //     y en la consola, en el último "Caused by:", Column "STOCK" not found.
    //     Como findById, findAll y findByFabricanteCodigo usan SELECT_BASE, se toca UNA sola vez.
    private static final String SELECT_BASE = """
            SELECT p.codigo, p.nombre, p.precio, p.stock,
                   f.codigo AS f_codigo, f.nombre AS f_nombre
            FROM producto p
            JOIN fabricante f ON p.codigo_fabricante = f.codigo
            """;

    private final DataSource dataSource;

    // Spring crea el DataSource a partir de application.properties y lo inyecta aquí
//    public ProductoRepositoryJdbc(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }

    // ---------------------------------------------------------------- READ (uno)
    @Override
    public Optional<Producto> findById(Integer codigo) {
        String sql = SELECT_BASE + "WHERE p.codigo = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

                //---------------------------
                // si devuelve una fila, es que ha encontrado el producto por código
//                if (rs.next()) {
//                    Fabricante fabricante = new Fabricante(
//                            rs.getInt("f_codigo"),
//                            rs.getString("f_nombre"));
//
//                    Producto producto=  new Producto(
//                            rs.getInt("codigo"),
//                            rs.getString("nombre"),
//                            rs.getDouble("precio"),
//                            fabricante);
//
//                    return Optional.of(producto);
//                }else{ // no ha encontrado el producto por código
//                    return Optional.empty();
//                }
                //---------------------------

            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el producto " + codigo, e);
        }
    }

    // ---------------------------------------------------------------- READ (todos)
    // PLANTILLA para cualquier consulta que devuelve una LISTA (la copió el RETO 2, más abajo):
    //   SQL -> lista vacía -> try(conexión; sentencia; resultado) -> while (rs.next()) añadir mapRow(rs) -> return lista.
    @Override
    public List<Producto> findAll() {
        String sql = SELECT_BASE + "ORDER BY p.codigo";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapRow(rs));

            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los productos", e);
        }
        return productos;
    }

    // ---------------------------------------------------------------- CREATE
    // >>> RETO 4 [COLUMNA NUEVA] · sitio 10/10 (a) · INSERT: tres cosas que tienen que cuadrar:
    //     1) la columna en la lista   2) un ? más en VALUES   3) su ps.setX(n, ...) en la MISMA posición que su ?
    //     setInt / setString / setDouble según el tipo. Los ? se numeran desde 1.
    //     Si se te olvida y la columna es NOT NULL: "Error de base de datos" al CREAR un producto.
    @Override
    public void save(Producto producto) {
        String sql = "INSERT INTO producto (nombre, precio, stock, codigo_fabricante) VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getFabricante().getCodigo());

            ps.executeUpdate();

            // Recuperamos el código que ha generado AUTO_INCREMENT y se lo asignamos al objeto
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setCodigo(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al guardar el producto", e);
        }
    }

    // ---------------------------------------------------------------- UPDATE
    // >>> RETO 4 [COLUMNA NUEVA] · sitio 10/10 (b) · UPDATE: "stock = ?" en el SET y su setX.
    //     TRAMPA: el ? del WHERE es SIEMPRE el último. Al meter un ? antes, RENUMERA los setX (el código pasó de 4 a 5).
    //     Si se te olvida este método: no da error, pero al editar el stock nunca cambia.
    @Override
    public void update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ?, codigo_fabricante = ? WHERE codigo = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getFabricante().getCodigo());
            ps.setInt(5, producto.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar el producto " + producto.getCodigo(), e);
        }
    }

    // ---------------------------------------------------------------- DELETE
    @Override
    public void delete(Integer codigo) {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al borrar el producto " + codigo, e);
        }
    }

    // ---------------------------------------------------------------- Consulta propia
    // PLANTILLA para cualquier "¿existe...?" (la copió el RETO 3 en FabricanteRepositoryJdbc):
    //   SELECT 1 FROM tabla WHERE condición = ?   ->   return rs.next();   (true si hay al menos una fila)
    @Override
    public boolean existsByFabricanteCodigo(Integer codigoFabricante) {
        String sql = "SELECT 1 FROM producto WHERE codigo_fabricante = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoFabricante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();   // con que haya una fila, basta
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(
                    "Error al comprobar los productos del fabricante " + codigoFabricante, e);
        }
    }

    // >>> RETO 2 [CONSULTA + PÁGINA NUEVA] · paso 2/6 · Implementar la consulta declarada en la interfaz.
    //     Es findAll() copiado con dos cambios:  SELECT_BASE + "WHERE p.codigo_fabricante = ? ..."  y  ps.setInt(1, ...).
    //     Como hay un ? que rellenar ANTES de ejecutar, el ResultSet va en un segundo try (igual que en findById).
    @Override
    public List<Producto> findByFabricanteCodigo(Integer codigoFabricante) {
        String sql = SELECT_BASE + "WHERE p.codigo_fabricante = ? ORDER BY p.nombre"; // por defecto es ASC

        List<Producto> productos = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoFabricante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapRow(rs));
                }

            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los productos del fabricante " + codigoFabricante, e);
        }

        return productos;
    }

    // ---------------------------------------------------------------- Mapeo fila → objeto
    // >>> RETO 4 [COLUMNA NUEVA] · sitio 5/10 · LEER la columna de la fila: rs.getInt("stock").
    //     Va en la posición que tiene el campo en la clase Producto (4º, entre precio y fabricante).
    //     getInt / getString / getDouble según el tipo de la columna.
    //     TRAMPA: dos valores del mismo tipo cruzados COMPILAN y muestran los datos cambiados sin avisar.
    private Producto mapRow(ResultSet rs) throws SQLException {
        Fabricante fabricante = new Fabricante(
                rs.getInt("f_codigo"),
                rs.getString("f_nombre"));

        return new Producto(
                rs.getInt("codigo"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                fabricante);
    }
}