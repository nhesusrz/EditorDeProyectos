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

import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class Red extends Observable implements Observer{

    private Hashtable<Integer, Actividad> actividades;
    private Hashtable<Integer, Suceso> sucesos;
    private Suceso s_inicial;
    private Suceso s_final;
    private Integer cont_sucesos;
    private Integer cont_actividades;

    public Red(){
        actividades = new Hashtable<Integer, Actividad>();
        sucesos = new Hashtable<Integer, Suceso>();
        s_inicial = null;
        s_final = null;
        cont_sucesos = 1;
        cont_actividades = 1;
    }

    // Métodos sobre sucesos

    public boolean setSucesoInicial(Suceso s){
        if(s != null){
            s_inicial = s;
            notificar("Se definio: Identificador: " + s.getIdentificador() + " como suceso inicial");
        }
        else{
            s_inicial = null;
        }
        return true;
    }

    public boolean setSucesoFinal(Suceso s){
        if(s != null){
            s_final = s;
            notificar("Se definio: Identificador: " + s.getIdentificador() + " como suceso final.");
        }
        else{
            s_final = null;
        }
        return true;
    }

    public Suceso getSucesoInicial(){
        return s_inicial;
    }

    public Suceso getSucesoFinal(){
        return s_final;
    }

    public Suceso getSuceso(Integer id){
        return sucesos.get(id);
    }

     public boolean esSucesoInicial(Suceso s){
        if(s_inicial.equals(s))
            return true;
        return false;
    }

    public boolean esSucesoFinal(Suceso s){
        if(s_final.equals(s))
            return true;
        return false;
    }

     public void agregarSuceso(){
        Suceso s = new Suceso(this,cont_sucesos);
        sucesos.put(cont_sucesos, s);
        cont_sucesos++;
        notificar(null);
    }

    public boolean removerSuceso(Integer id){
        // TODO: Ver el tema contador
        if(sucesos.containsKey(id)){
            sucesos.remove(id);
            notificar("Se borro el suceso: Identificador: " + id + ".");
            return true;
        }
        return false;
    }

    public Vector<Suceso> getSucesos(){
        Vector<Suceso> retorno = new Vector<Suceso>();
        for(Integer i = 1; i <= cont_sucesos; i++){
            Suceso s = sucesos.get(i);
            if(s != null){
                retorno.add(s);
            }
        }
        if(retorno.size() == 0)
            return null;
        return retorno;
    }

    public void resetearFechasSucesos(){
        for(Integer i = 0; i <= cont_sucesos; i++){
            if(sucesos.get(i) != null){
                sucesos.get(i).setDefectoFtFT();
            }
        }
    }

    // Métodos sobre actividades

    public Actividad getActiviadad(Integer key){
        return actividades.get(key);

    }
    
    public Actividad[] getActividades(){
        int cantidad = actividades.size();
        int indice = 0;
        Actividad[] retorno = new Actividad[cantidad];
        for(Integer i = 0; i <= cont_actividades; i++){
            if(actividades.get(i) != null){
                retorno[indice] = actividades.get(i);
                indice++;
            }
        }
        return retorno;
    }

    public Vector<Actividad> getActividadesVec(){
        Vector<Actividad> retorno = new Vector<Actividad>();
        for(Integer i = 0; i <= cont_actividades; i++){
            Actividad a = actividades.get(i);
            if(a != null){
                retorno.add(a);
            }
        }
        return retorno;
    }

    public Integer getCantidadActividades(){
        return new Integer(actividades.size());
    }

     public boolean agregarActividadConcreta(String descripcion, Integer s1, Integer s2, Integer duracion, Integer recurso){
         Suceso su1_temp = this.getSuceso(s1);
         Suceso su2_temp = this.getSuceso(s2);
         Actividad a_temp = new Concreta(0, "", su1_temp, su2_temp, 0, 0);
         if(!exiteActividad(a_temp) && sucesos.containsKey(s1) && sucesos.containsKey(s2)){
             Suceso su1 = this.getSuceso(s1);
             Suceso su2 = this.getSuceso(s2);
             Actividad a = new Concreta(cont_actividades, descripcion, su1, su2, duracion, recurso);
             su1.agregarActSaliente(a);
             su2.agregarActEntrante(a);
             a.addObserver(this);
             actividades.put(cont_actividades, a);
             cont_actividades++;
             notificar("Se agrego una actividad concreta: Suceso Origen: " + s1 + " Suceso Fin: " + s2 + " Duración: " + duracion + " Recurso: "+ recurso);
             return true;
         }
         notificar("La actividad ya existe.");
         return false;
    }

    public boolean agregarActividadFicticia(String descripcion, Integer s1, Integer s2){
         Suceso su1_temp = this.getSuceso(s1);
         Suceso su2_temp = this.getSuceso(s2);
         Actividad a_temp = new Ficticia(0, "", su1_temp, su2_temp);
         if(!exiteActividad(a_temp) && sucesos.containsKey(s1) && sucesos.containsKey(s2)){
            Suceso su1 = this.getSuceso(s1);
            Suceso su2 = this.getSuceso(s2);
            Actividad a = new Ficticia(cont_actividades, descripcion, su1, su2);
            su1.agregarActSaliente(a);
            su2.agregarActEntrante(a);
            a.addObserver(this);
            actividades.put(cont_actividades, a);
            cont_actividades++;
            notificar("Se agrego una actividad ficticia: Suceso Origen: " + s1 + " Suceso Fin: " + s2);
            return true;
         }
         notificar("La actividad ya existe.");
         return false;
    }

    public boolean removerActividad(Integer id){
        // TODO: Ver el tema contador
        if(actividades.containsKey(id)){
            Actividad a = actividades.get(id);
            a.getSucesoOrigen().eliminarActSaliente(a);
            a.getSucesoFin().eliminarActEntrante(a);
            actividades.remove(id);
            notificar("Se elimino una actividad: Identificador: " + id + ".");
            return true;
        }
        return false;
    }

   // Otros

    public boolean exiteActividad(Actividad a_nueva){
        for(Integer i = 1; i <= cont_actividades; i++){
            Actividad a = actividades.get(i);
            if(a != null){
                if(a.equals(a_nueva))
                    return true;
            }
        }
        return false;
    }
    public void actualizarFt(){
        s_inicial.acutualizar_Ft(0, 0);
    }

    public void actualizarFt(Suceso s, Integer tiempo, Integer duracion){
        s.acutualizar_Ft(tiempo, duracion);
    }

    // El parametro restablecer permite forzar el reseteo de la FT al valor por defecto.
    public void actualizarFT(boolean restablecer){
        s_final.acutualizar_FT(s_final.getFt(), 0, restablecer);
    }

    public void actualizarFT(Suceso s, Integer tiempo, Integer duracion, boolean restablecer){
        s.acutualizar_FT(tiempo, duracion, restablecer);
    }

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }

    public void update(Observable obs, Object o){
        notificar((String) o);
    }

    public Red clone(){
        Red retorno = new Red();
        retorno.cont_sucesos = cont_sucesos;
        for(Integer i = retorno.cont_sucesos; i >= 1; i--){
            Suceso s = sucesos.get(i);
            if(s != null)
                retorno.agregarSuceso(s.clone(retorno, i));
        }
        if(s_inicial != null && s_final != null){
            retorno.s_inicial = retorno.getSuceso(s_inicial.getIdentificador());
            retorno.s_final = retorno.getSuceso(s_final.getIdentificador());
        }
        // Las actividades deben ser agregadas ya que si son obserbables por la nueva red.
        retorno.cont_actividades = cont_actividades;
        //Vector<Actividad> vec = getActividadesVec();
        for(Integer i = retorno.cont_actividades; i >= 1; i--){
            Actividad a = actividades.get(i);
            if(a != null)
                if(a.esFicticia())
                    retorno.agregarActividad(retorno, ((Ficticia) a).clone(a.getIdentificador(), a.getDescripcion(), retorno.sucesos.get(a.getSucesoOrigen().getIdentificador()), retorno.sucesos.get(a.getSucesoFin().getIdentificador())));
                else
                    retorno.agregarActividad(retorno, ((Concreta)a).clone(a.getIdentificador(), a.getDescripcion(), retorno.sucesos.get(a.getSucesoOrigen().getIdentificador()), retorno.sucesos.get(a.getSucesoFin().getIdentificador()), a.getParametrosNormales().getTiempo(),a.getParametrosNormales().getValor()));
        }
        return retorno;
    }

    private void agregarSuceso(Suceso s){
        sucesos.put(s.getIdentificador(), s);
    }

    private void agregarActividad(Red retorno, Actividad a){
        a.addObserver(retorno);
        actividades.put(a.getIdentificador(), a);
        a.getSucesoOrigen().agregarActSaliente(a);
        a.getSucesoFin().agregarActEntrante(a);
    }

    // Métodos privados

}
