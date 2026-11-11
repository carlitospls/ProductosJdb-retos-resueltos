-- Se borran en orden inverso a las dependencias (primero la tabla "hija")
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS fabricante;

-- Tabla "padre": lado 1 de la relación
CREATE TABLE fabricante (
                            codigo INT          AUTO_INCREMENT PRIMARY KEY,
                            nombre VARCHAR(100) NOT NULL
);

-- Tabla "hija": lado N de la relación (cada producto tiene UN fabricante)
CREATE TABLE producto (
                          codigo            INT          AUTO_INCREMENT PRIMARY KEY,
                          nombre            VARCHAR(100) NOT NULL,
                          precio            DOUBLE       NOT NULL,
                          -- >>> RETO 4 [COLUMNA NUEVA] · sitio 1/10 · La columna en la tabla.
                          --     Tipo según el dato: unidades/años = INT · dinero = DOUBLE · texto = VARCHAR(n) · fecha = DATE.
                          --     NOT NULL te obliga a darle valor en TODOS los INSERT de data.sql (sitio 2) y en save() (sitio 10).
                          --     Tras tocar un .sql hay que REINICIAR la app: la BD está en memoria y se recrea al arrancar.
                          stock             INT          NOT NULL,
                          codigo_fabricante INT          NOT NULL,
                          CONSTRAINT fk_producto_fabricante
                              FOREIGN KEY (codigo_fabricante) REFERENCES fabricante (codigo)
);