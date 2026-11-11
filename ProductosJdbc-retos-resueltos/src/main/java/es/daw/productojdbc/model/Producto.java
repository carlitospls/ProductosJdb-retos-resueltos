package es.daw.productojdbc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila de la tabla PRODUCTO.
 *
 * En la tabla hay una columna codigo_fabricante (un INT, la FK).
 * En Java NO guardamos ese INT suelto: guardamos el objeto Fabricante completo.
 * Rellenarlo es trabajo nuestro (JOIN + mapeo manual). En la UT04, JPA lo hará con @ManyToOne.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Integer codigo;
    private String nombre;
    private double precio;

    // >>> RETO 4 [COLUMNA NUEVA] · sitio 3/10 · El campo en el modelo.
    //     @AllArgsConstructor genera el constructor con los campos EN ESTE ORDEN: (codigo, nombre, precio, stock, fabricante).
    //     En cuanto lo añades dejan de compilar todos los "new Producto(...)": mapRow (sitio 5) y toModelEntity (sitio 9).
    //     Ese rojo es tu lista de tareas. Java empareja por POSICIÓN, no por nombre: mismo orden en los tres sitios.
    private int stock;

    private Fabricante fabricante;
}