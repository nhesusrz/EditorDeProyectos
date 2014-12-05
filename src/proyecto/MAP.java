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

/*  MAP : Manpower Allocation Procedure (Procedimiento de asignación de mano de obra)
 *  Esta clase implementa el algoritmo para nivelar el uso de recursos reprogramando
 *  el uso de los mismos. Los recursos tenidos en cuenta son no acumulables.
 */

package proyecto;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class MAP implements Algoritmo{

    private Integer disponible; // Cantidad de recursos diarios disponibles.
    private Tabla tabla;        // Estructura usada para el computo del algoritmo.
    private Red red;            // Red sobre la cual trabaja el algoritmo.

    public MAP(Integer cantidad_recursos){
        disponible = cantidad_recursos; 
        tabla = null;
    }

    public void correr(Red red){
        this.red = red;
        // Se inicializa el reloj.
        Integer reloj = 0;
        Actividad[] actividades  = red.getActividades();
        // Se pasan a un vector para su tratamiento.
        Vector<Actividad> disponibles = actividadesDisponibles(red, actividades, null);
        // Se inicializa la tabla con la cantidad minima necesaria de entradas que
        // debe contener y tambien se le indica el recurso disponible para cada entrada.
        tabla = new Tabla(disponible, red.getSucesoFinal().getFT());
        // Contiene las actividades a ser colocadas en un determinado momento.
        Vector<Actividad> a_colocar = aEjecutar(reloj, disponibles);
        // Contiene las actividades ya programadas.
        Vector<Actividad> colocadas = new Vector<Actividad>();
        while(disponibles.size() != 0){ // Mientras existan actividades a ser programadas.
            if(a_colocar != null){ // Si hay actividades para colocar.
                copiar(a_colocar, colocadas); // Se copian al vector de colocadas.
                colocarActividades(a_colocar, reloj); // Se colocan las actividades en la tabla.
            }
            reloj++; // Se pasa al siguiente momento.
            // Se recalculan la actividades disponibles.
            disponibles = actividadesDisponibles(red, actividades, colocadas);
            // Se vuelven a seleccionar la actividades que se pueden ejecutar para el reloj actual.
            a_colocar = aEjecutar(reloj, disponibles);
        }
    }

    // Métodos privados

    // Copia los elementos del vector origen al vector destino.
    private void copiar(Vector<Actividad> origen, Vector<Actividad> destino){
        for(Iterator<Actividad> it = origen.iterator();it.hasNext();){
            Actividad a = it.next();
            destino.add(a);
         }
    }
    // Se encarga de colocar las actividades en la tabla.
    private void colocarActividades(Vector<Actividad> vec, Integer reloj){
        while(vec.size() != 0){ // Mientras no se hayan colocado todas.
            Actividad a = seleccionar(vec); // Se selecciona una actividad.
            if(a != null){
                vec.remove(a); // Se remueve del vector.
                // Se le dice a la tabla que coloque dicha actividad.
                tabla.colocarActividad(a, reloj, red);
            }
        }
    }
    // Elije la actividad de acuerdo a ciertos parametros.
    private Actividad seleccionar(Vector<Actividad> vec){
        Actividad a = null;
        a = buscarFicticia(vec);
        if(a != null)
            return a;
        a = buscarRecNulo(vec);
        if(a != null)
            return a;
        a = buscarMtMin(vec);
        if(a != null)
            return a;
        a = buscarMayorRec(vec);
        if(a != null)
            return a;
        a = vec.firstElement();
        return a;
    }

    private Actividad buscarMayorRec(Vector<Actividad> vec){
        Actividad a_max = null;
        Integer recurso = 0;
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.getParametrosNormales().getValor() > recurso){
                a_max = a;
                recurso = a.getParametrosNormales().getValor();
            }
        }
        return a_max;
    }

    private Actividad buscarMtMin(Vector<Actividad> vec){
        Actividad a_min = null;
        Integer mt_min = Integer.MAX_VALUE;
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.getMargenTotal() < mt_min){
                a_min = a;
                mt_min = a.getMargenTotal();
            }
        }
        return a_min;
    }

    private Actividad buscarRecNulo(Vector<Actividad> vec){
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.getParametrosNormales().getValor() == 0){
                vec.remove(a);
                return a;
            }
        }
        return null;
    }

    private Actividad buscarFicticia(Vector<Actividad> vec){
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.esFicticia()){
                vec.remove(a);
                return a;
            }
        }
        return null;
    }
   // Dado el reloj y el vector de actividades selecciona las que se pueden ejecutar.
    private Vector<Actividad> aEjecutar(Integer reloj, Vector<Actividad> vec){
        boolean alguna = false;
        Vector<Actividad> retorno = new Vector<Actividad>();
        for(Iterator<Actividad> it = vec.iterator();it.hasNext();){
            Actividad a = it.next();
            if(a.getSucesoOrigen().getFt() == reloj){
                retorno.add(a);
                alguna = true;
            }
        }
        if(!alguna)
            return null;
        return retorno;
    }
    // Devuelve las actividades que se encuentran disponibles para su ejecución. Las devuelve ordenadas
    // de acuerdo a su Fti.
    private Vector<Actividad> actividadesDisponibles(Red red, Actividad[] array, Vector<Actividad> vec){
        Arrays.sort(array);
        Vector<Actividad> retorno = new Vector<Actividad>();
        int lim = red.getCantidadActividades();
        for(int i = 0; i < lim; i++){
            retorno.add(array[i]);
        }
        // Elimino las actividades ya programadas.
        if(vec != null){ // Para la primera vez.
            for(Iterator<Actividad> it = vec.iterator();it.hasNext();){
                Actividad a = it.next();
                retorno.remove(a);
            }
        }
        return retorno;
    }

}
