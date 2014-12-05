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

public class Tabla {

    private Vector<EntradaTabla> entradas;

    public Tabla(Integer disponible, Integer reloj_maximo){
        entradas = new Vector<EntradaTabla>();
        for(Integer i = 0; i <= reloj_maximo * 2; i ++){
            EntradaTabla e = new EntradaTabla(disponible);
            entradas.add(e);
        }
    }

    public void colocarActividad(Actividad a, Integer reloj, Red red){
        EntradaTabla e = entradas.get(reloj);
        while(!e.entraActividad(a, reloj)){
            reloj++;
            e = entradas.get(reloj);        
        }
        if(a.getSucesoOrigen().getFt() < reloj){
            // Se le efectuan los cambios correspondientes de fecha a los sucesos de
            // la actividad.
            a.getSucesoOrigen().setFT(Suceso.ID_SUCESO_MAX);
            a.getSucesoFin().setFT(Suceso.ID_SUCESO_MAX);

            // Se propagan los cambios de fechas a los sucesos de las actividades afectadas.
            red.actualizarFt(a.getSucesoOrigen(), new Integer(reloj.intValue()), 0);
            red.actualizarFT(true);       
        }
        int lim;
        if(a.esFicticia())
            lim = reloj.intValue();
        else
            lim = (reloj + a.getParametrosNormales().getTiempo() - 1);
        for(Integer i = reloj; i <= lim; i++){
            e = entradas.get(i);
            e.agregarActividad(a);
        }
    }

    public Vector<EntradaTabla> getEntradas(){
        return entradas;
    }

}
