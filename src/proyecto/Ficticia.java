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

public class Ficticia extends Actividad{

    // Aqui solo se tienen en cuenta los parametros normales ya que este tipo de
    // actividad no puede acelerarse.
    private Parametros parametros_normales;

    public Ficticia(Integer identificador, String descripcion, Suceso s_origen, Suceso s_fin){
        super(identificador, descripcion, s_origen, s_fin);
        parametros_normales = new Parametros(0, 0);
    }

    // Métodos Set

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
        notificar("Cambio la descripción de la actividad " + identificador);
    }

    // Métodos Get

    public Integer getMargenTotal(){
        return getSucesoFin().getFT() - getSucesoOrigen().getFt();
    }

     public Integer getMargenLibre(){
        return getSucesoFin().getFt() - getSucesoOrigen().getFt();
    }

    public Integer getMargenIndependiente(){
        return getSucesoFin().getFt() - getSucesoOrigen().getFT();
    }

     public Parametros getParametrosNormales(){
        return parametros_normales;
    }

    public Parametros getParametrosAceleracion(){
        return parametros_normales;
    }

    // Otros

    public boolean esFicticia(){
        return true;
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
        return super.toString();
    }

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }

    public Ficticia clone(Integer identificador, String descripcion, Suceso s_origen, Suceso s_fin){
        Ficticia retorno = new Ficticia(identificador, descripcion, s_origen, s_fin);
        return retorno;
    }

}
