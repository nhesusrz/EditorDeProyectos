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
 *  Correo electrónico: mpacheco@alumnos.exa.unicen.edu.ar
 */

package proyecto;

import java.util.Vector;

public class EntradaTabla {

    // Contiene la cantidad disponible actualmente.
    private int disponible;
    // Contiene las actividades que se ejecutan.
    private Vector<Integer> actividades;

    public EntradaTabla(Integer disponible){
        this.disponible = disponible.intValue();
        actividades = new Vector<Integer>();
    }

    public void agregarActividad(Actividad a){
        actividades.add(a.getIdentificador());
        disponible = disponible - a.getParametrosNormales().getValor();
    }

    public boolean entraActividad(Actividad a, Integer reloj){
        if(a.getParametrosNormales().getValor() <= disponible)
            return true;
        return false;
    }

    public Integer recursosDiponibles(){
        return new Integer(disponible);
    }

    public String toString(){
        return new String("Disponible: "+disponible);
    }

}
