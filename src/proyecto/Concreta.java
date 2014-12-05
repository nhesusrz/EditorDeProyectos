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

public class Concreta extends Actividad{
    
    private Parametros parametros_normales, parametros_aceleracion;

    public Concreta(Integer identificador, String descripcion, Suceso s_origen, Suceso s_fin, Integer duracion, Integer recurso){
        super(identificador, descripcion, s_origen, s_fin);
        parametros_normales = new Parametros(duracion, recurso);
        parametros_aceleracion = new Parametros();
    }

    // Métodos Set

    public void setParametroNormalDuracion(Integer duracion){
        parametros_normales.setTiempo(duracion);
        notificar("Cambio la duración de la actividad " + identificador);
    }

    public void PEPE(Integer duracion){
        parametros_normales.setTiempo(duracion);
    }

    public void setParametroNormalRecurso(Integer recurso){
        parametros_normales.setValor(recurso);
        notificar("Cambio la cantidad de recursos de la actividad " + identificador);
    }

     public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
        notificar("Cambio la descripción de la actividad " + identificador);
    }

    // Métodos Get

    public Integer getMargenTotal(){
            return getSucesoFin().getFT() - (getSucesoOrigen().getFt() + parametros_normales.getTiempo());
    }

    public Integer getMargenLibre(){
        return getSucesoFin().getFt() - (getSucesoOrigen().getFt() + parametros_normales.getTiempo());
    }

    public Integer getMargenIndependiente(){
        return getSucesoFin().getFt() - (getSucesoOrigen().getFT() + parametros_normales.getTiempo());
    }

    public Parametros getParametrosNormales(){
        return parametros_normales;
    }

    public Parametros getParametrosAceleracion(){
        return parametros_aceleracion;
    }

    // Otros

    public boolean esFicticia(){
        return false;
    }
    
    /*public boolean equals(Actividad a){
        if((a.getIdentificador() == this.identificador)&&
          (a.getSucesoOrigen().equals(s_origen))&&
          (a.getSucesoFin().equals(s_fin))&&
          (a.getParametrosNormales().equals(parametros_normales))){
            return true;
        }
        return false;
    }*/

    public String toString(){
        return super.toString() + " Parametros Normales: " + parametros_normales.toString();
    }

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }

    public Actividad clone(Integer identificador, String descripcion, Suceso s_origen, Suceso s_fin, Integer duracion, Integer recurso){
        Concreta retorno = new Concreta(identificador, descripcion, s_origen, s_fin, duracion, recurso);
        return retorno;
    }

}
