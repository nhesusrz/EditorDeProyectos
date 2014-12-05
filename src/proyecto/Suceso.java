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

import java.util.Iterator;
import java.util.Vector;

public class Suceso {

    public static final Integer ID_SUCESO_MAX = Integer.MAX_VALUE;

    private Red red;
    private Integer identificador;
    private Integer Ft, FT;
    private Vector<Actividad> actividades_entrantes;
    private Vector<Actividad> actividades_salientes;

    public Suceso(Red red, Integer identificador){
        this.red = red;
        this.identificador = identificador;
        setDefectoFtFT();
        actividades_entrantes = new Vector<Actividad>();
        actividades_salientes = new Vector<Actividad>();
    }

    // Método Set

    public void setIdentificador(Integer identificador){
        this.identificador = identificador;
    }

    public void setFt(Integer Ft){
        this.Ft = Ft;
    }

    public void setFT(Integer FT){
        this.FT = FT;
    }

    public void setDefectoFtFT(){
        this.Ft = 0;
        this.FT = ID_SUCESO_MAX;
    }

    // Método Get

    public Integer getIdentificador(){
        return identificador;
    }

    public Integer getFt(){
        return Ft;
    }

    public Integer getFT(){
        return FT;
    }

    public Vector<Actividad> getActividadesSalientes(){
        Vector<Actividad> retorno = new Vector<Actividad>();
        for(Iterator<Actividad> it = actividades_salientes.iterator(); it.hasNext();){
            retorno.add(it.next());
        }
        return retorno;
    }

    public Vector<Actividad> getActividadesEntrantes(){
        Vector<Actividad> retorno = new Vector<Actividad>();
        for(Iterator<Actividad> it = actividades_entrantes.iterator(); it.hasNext();){
            retorno.add(it.next());
        }
        return retorno;
    }

    // Otros

    public String toString(){
        return "Nro.: "+identificador.toString()+" Ft: "+Ft.toString()+" FT: "+FT.toString();
    }


    public void agregarActEntrante(Actividad a){
        actividades_entrantes.add(a);
    }

    public void agregarActSaliente(Actividad a){
        actividades_salientes.add(a);
    }

    public boolean eliminarActEntrante(Actividad a){
        return eliminarActividad(actividades_entrantes, a);
    }

    public boolean eliminarActSaliente(Actividad a){
        return eliminarActividad(actividades_salientes, a);
    }

    public void acutualizar_Ft(Integer tiempo, Integer duracion){
        if(red.esSucesoInicial(this)){
            Ft = tiempo;
        }
        else{
            if(Ft < tiempo + duracion){Ft = tiempo + duracion;}
        }
        for(Iterator<Actividad> it = actividades_salientes.iterator();it.hasNext();){
            Actividad a = it.next();
            Suceso s = a.getSucesoFin();
            s.acutualizar_Ft(Ft, a.getParametrosNormales().getTiempo());
        }
    }

    public void acutualizar_FT(Integer tiempo, Integer duracion, boolean restablecer){
        if(red.esSucesoFinal(this)){
            FT = tiempo;
        }
        else{
            Integer dif = tiempo - duracion;
            if(dif < 0){
                dif = dif * (-1);
            }
            if(dif < FT){
                FT = dif;
            }
        }
        for(Iterator<Actividad> it = actividades_entrantes.iterator();it.hasNext();){
            Actividad a = it.next();
            Suceso s = a.getSucesoOrigen();
            if(restablecer)
               s.setFT(Integer.MAX_VALUE);
            s.acutualizar_FT(FT, a.getParametrosNormales().getTiempo(), restablecer);
        }
    }

    public boolean esCritico(){
        if(Ft != null && FT != null){
            if(Ft == FT)
                return true;
        }
        return false;
    }

    public boolean equals(Suceso s){
        if((identificador == s.getIdentificador())&&(FT == s.getFT())&&(Ft == s.getFt()))
            return true;
        return false;
    }
    // Se devuelve el clon solo tiene las mismas fechas (Ft y FT).
    public Suceso clone(Red red, Integer id){
        Suceso retorno = new Suceso(red, id);
        retorno.Ft = Ft;
        retorno.FT = FT;
        retorno.actividades_entrantes = new Vector<Actividad>();
        retorno.actividades_salientes = new Vector<Actividad>();
        return retorno;
    }

    // Métodos Privados

    private boolean eliminarActividad(Vector<Actividad> va, Actividad a){
        for(Iterator<Actividad> it = va.iterator(); it.hasNext();){
            Actividad x = it.next();
            if(x.equals(a)){
                va.remove(a);
                return true;
            }
        }
        return false;
    }

}
