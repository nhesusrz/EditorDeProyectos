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

import java.util.Observable;

public abstract class Actividad extends Observable implements Comparable {

    // Identificador unico de la actividad.
    protected Integer identificador;
    // Descripsión breve.
    protected String descripcion;
    // Los sucesos que componen la actividad.
    protected Suceso s_origen, s_fin;

    public Actividad(Integer identificador, String descripcion, Suceso s_origen, Suceso s_fin){
        this.identificador = identificador;
        this.descripcion = descripcion;
        this.s_origen = s_origen;
        this.s_fin = s_fin;
    }

    // Métodos Set

    public void setSucesoOrigen(Suceso s){
        s_origen = s;
    }

    public void setSucesoFin(Suceso s){
        s_fin = s;
    }

    // Métodos Get

    public Integer getIdentificador(){
        return identificador;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public Suceso getSucesoOrigen(){
        return s_origen;
    }

    public Suceso getSucesoFin(){
        return s_fin;
    }
    // Indica el margen total de la actividad.
    public abstract Integer getMargenTotal();
    
    public abstract Integer getMargenLibre();
    
    public abstract Integer getMargenIndependiente();

    // Parametos nomales se refieren a la duración y a la cantidad de recursos que insume
    // por unidad de tiempo.
    public abstract Parametros getParametrosNormales();
    // Parametros de aceleración involucran el costo y el limite técnico.
    public abstract Parametros getParametrosAceleracion();

    // Otros

    // Indica si la actividad es ficticia o no.
    public abstract boolean esFicticia();
    // Indica si la actividad es critica o no.
    public boolean esCritica(){
        if(s_origen.esCritico() && s_fin.esCritico())
            return true;
        return false;
    }    
    
    public String toString(){
        return "Identificador: " + identificador + " Descripción: "+ descripcion;
    }
    // No pueden existir dos actividades con los mismos sucesos.
    public boolean equals(Actividad a){
        if(a.getSucesoOrigen().equals(s_origen) && a.getSucesoFin().equals(s_fin)){
            return true;
        }
        return false;
    }

    public int compareTo(Object o){
        Actividad a = (Actividad)o;
        if(this.getSucesoOrigen().getFt() == a.getSucesoOrigen().getFt())
            return 0;
        else if(this.getSucesoOrigen().getFt() < a.getSucesoOrigen().getFt())
            return -1;
        else
            return 1;
    }

}
