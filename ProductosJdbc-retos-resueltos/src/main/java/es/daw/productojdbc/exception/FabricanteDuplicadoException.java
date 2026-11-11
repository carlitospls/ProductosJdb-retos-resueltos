package es.daw.productojdbc.exception;

// >>> RETO 3 [REGLA DE NEGOCIO: NO DUPLICADOS] · paso 1/6 · Excepción propia para la regla.
//     Copia de FabricanteConProductosException: extends RuntimeException + el mensaje en super(...).
//     Ese mensaje es el que acaba viendo el usuario (el controlador lo saca con e.getMessage()).
public class FabricanteDuplicadoException extends RuntimeException {
    public FabricanteDuplicadoException(String nombre) {
        //super("Ya existe un fabricante con el nombre: " + nombre);
        super("Ya existe un fabricante llamado \""+nombre+"\".");
    }
}
