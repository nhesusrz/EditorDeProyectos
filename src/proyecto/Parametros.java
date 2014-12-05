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

public class Parametros {

    private Integer tiempo;
    private Integer valor;

    public Parametros(){
        tiempo = -1;
        valor = -1;
    }

    public Parametros(Integer duracion, Integer costo){
        this.tiempo = duracion;
        this.valor = costo;
    }

    // Métodos Set

    public void setValor(Integer costo){
        this.valor = costo;
    }

    public void setTiempo(Integer duracion){
        this.tiempo = duracion;
    }

    // Métodos Get

    public Integer getValor(){
        return valor;
    }

    public Integer getTiempo(){
        return tiempo;
    }

    public boolean equals(Parametros p){
        if((p.getValor() == this.valor)&&(p.getTiempo() == this.tiempo))
            return true;
        return false;
    }

    public String toString(){
        return "Tiempo: " + tiempo + " Valor: " + valor.toString();
    }

}
