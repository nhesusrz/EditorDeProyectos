/*
 *  Editor de Proyectos. Permite crear proyectos basados en la teoría de Camino Critico.
 *  Copyright (C) 2010 Martín I. Pacheco
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Correo electrónico: mpacheco@alumnos.exa.unicen.edu.ar
 */

package manajadorArchivos;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Observable;
import javax.imageio.ImageIO;
import org.jgraph.JGraph;

public class ManejadorImagen extends Observable{

    public void escribirObjeto(JGraph grafo, File archivo, String extension) {
        try {
            BufferedOutputStream salida = new BufferedOutputStream(new FileOutputStream(archivo + "." + extension));
            BufferedImage imagen = grafo.getImage(grafo.getBackground(),10);
            ImageIO.write(imagen, extension, salida);
            salida.flush();
            salida.close();
        }
        catch (Exception e) {
            notificar("Error al grabar el archivo de imagen " + archivo.toString());
            e.printStackTrace();
        }
    }

    public Object leerObjeto(String nombre_archivo) {
        try {
            ObjectInputStream entrada = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(nombre_archivo)));
            Object object = entrada.readObject();
            entrada.close();
            return object;
        }
        catch (Exception e) {
            notificar("Error al leer el archivo de imagen " + nombre_archivo);
            e.printStackTrace();
        }
        return null;
    }

    // Métodos privados.

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }

}

