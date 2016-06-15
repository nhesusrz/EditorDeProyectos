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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
// import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Observable;

public class ManejadorXML extends Observable{

    public void escribirObjeto(Object o, File archivo) {
        XStream xs = new XStream();
        //xs.setMode(XStream.ID_REFERENCES);
	try {
            BufferedOutputStream salida = new BufferedOutputStream(new FileOutputStream(archivo + ".xml"));
            xs.toXML(o, salida);
            salida.flush();
            salida.close();
        }
        catch (Exception e) {
            notificar("Error al guardar el archivo " + archivo.toString());
            e.printStackTrace();
        }
    }

    public Object leerObjeto(File archivo){
        XStream xs = new XStream(new DomDriver()); 
        try {
            FileInputStream entrada= new FileInputStream(archivo.getAbsoluteFile());            
            return xs.fromXML(entrada);
        }
        catch (Exception e) {
            notificar("Error al leer el archivo " + archivo.toString());           
        }
        return null;
    }

    // Métodos provados.

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }
    
}
