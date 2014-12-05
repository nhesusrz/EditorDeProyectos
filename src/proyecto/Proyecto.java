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
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class Proyecto extends Observable implements Observer{

    private String nombre;
    private String responsable;
    private Red red;
    private Algoritmo algoritmo;

    public Proyecto(String nombre, String responsable){
        this.nombre = nombre;
        this.responsable = responsable;
        red = new Red();
        red.addObserver(this);
        algoritmo = null;
    }
 
    // Métodos Set

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public void setResponsable(String responsable){
        this.responsable = responsable;
    }

    public void setAlgoritmo(Algoritmo algoritmo){
        this.algoritmo = algoritmo;
    }

    // Metodos Get

    public String getNombre(){
        return nombre;
    }

    public String getResponsable(){
        return responsable;
    }

    // Métodos sobre sucesos

    public void agregarSuceso(){
        red.agregarSuceso();
    }

    public void establecerSucesoInicial(Integer id){
        red.setSucesoInicial(red.getSuceso(id));
    }

    public void establecerSucesoInicialNulo(){
        red.setSucesoInicial(null);
    }

    public void establecerSucesoFinal(Integer id){
        red.setSucesoFinal(red.getSuceso(id));
    }

    public void establecerSucesoFinalNulo(){
        red.setSucesoFinal(null);
    }

    public void removerSuceso(Integer id){
        Suceso suceso = red.getSuceso(id);
        if(suceso != null){
            eliminarActividades(suceso.getActividadesEntrantes());
            eliminarActividades(suceso.getActividadesSalientes());
            red.removerSuceso(id);
        }
    }

    public void actualizarFechasSucesos(){        
        if(red.getSucesoInicial() != null && red.getSucesoFinal() != null){
            red.actualizarFt();
            red.actualizarFT(false);
        }
    }

    public Suceso getSucesoInicial(){
        return red.getSucesoInicial();
    }

    public Suceso getSucesoFinal(){
        return red.getSucesoFinal();
    }

    public Vector<Suceso> getSucesos(){
        return red.getSucesos();
    }

    public Suceso getSuceso(Integer id){
        return red.getSuceso(id);
    }

    public void resetearFechasSucesos(){
        red.resetearFechasSucesos();
    }

    // Métodos sobre actividades

    public Vector<Actividad> getActividades(){
        return red.getActividadesVec();
    }

    public Integer getCantActividades(){
        return red.getCantidadActividades();
    }

    public boolean agregarActividadConcreta(String descripcion, Integer s1, Integer s2, Integer duracion, Integer recurso){
        return red.agregarActividadConcreta(descripcion, s1, s2, duracion, recurso);
    }

    public boolean agregarActividadFicticia(String descripcion, Integer s1, Integer s2){
        return red.agregarActividadFicticia(descripcion, s1, s2);
    }

    public void removerActividad(Integer id){
        red.removerActividad(id);
    }

    public Actividad getActividad(Integer id){
        return red.getActiviadad(id);
    }

    // Otros
    
    public Float getPromUnidadesRequeridasDiarias(){
        if(getSucesoFinal() != null && getSucesoFinal().getFt() - 1 != 0)
            return new Float(getSumaRecDistribucion() / (getSucesoFinal().getFt() - 1));
        return new Float(0.0);
    }

    public Float getProcentajeAprovechamiento(){
        // pico son la cantidad de recursos totales que implica para absorber el pico maximo de requerimiento de recursos.
        if(getSucesoFinal() != null && getSucesoFinal().getFt() != 0){
            Integer pico = getRecursosTotalesAbsorberPico();
            if(pico != 0)
                return new Float((getSumaRecDistribucion() * 100) / pico);
                    
        }
        return new Float(0.0);
    }

    public Integer getRecursosTotalesAbsorberPico(){
        if(getSucesoFinal() != null)
            return getMaximoRecursoDistribucion() * (getSucesoFinal().getFt() - 1);
        return 0;
    }

    public Integer getRecursoMaximoActividad(){
        Vector<Actividad> vec = getActividades();
        Integer max = 0;
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.getParametrosNormales().getValor() > max)
                max = a.getParametrosNormales().getValor();
        }
        return max;
    }

    public Integer getMaximoRecursoDistribucion(){
        Integer max = -1;
        if(getSucesoFinal() != null){
            Integer dias_totales = getSucesoFinal().getFt();
            Integer[] recursos = calcularRecursos(dias_totales);
            for(int dia = 0; dia < dias_totales.intValue(); dia ++){
                if(recursos[dia] > max)
                    max = recursos[dia];
            }
        }
        return max;
    }

    public boolean definidosSucesosEspeciales(){
        if(red.getSucesoInicial() != null && red.getSucesoFinal() != null)
            return true;
        return false;
    }

    public Vector<Vector<Actividad>> getCaminosCriticos(){
        Vector<Vector<Actividad>> retorno = new Vector<Vector<Actividad>>();        
        Vector<Actividad> temp = new Vector<Actividad>();
        if(definidosSucesosEspeciales())
            alcanzoFinalCritico(red.getSucesoInicial(), retorno, temp);
        return retorno;
    }
   
    public void correrAlgoritmo(){
        if(algoritmo != null)
            algoritmo.correr(red);
    }

    // La clave es el dia y devuelve la cantidad de recursos utilizados.
    public Hashtable<Integer, Integer> distribucionRecursosUtilizados(){
        Hashtable<Integer, Integer> retorno = new Hashtable<Integer, Integer>();
        if(getSucesoFinal() != null){
            Integer dias_totales = getSucesoFinal().getFt();
            Integer[] recursos = calcularRecursos(dias_totales);
            for(int dia = 1; dia <= dias_totales; dia++){
                retorno.put(new Integer(dia), recursos[dia - 1]);
            }
        }
        return retorno;
    }

    public void notificar(String informacion){
        setChanged();
        notifyObservers(informacion);
    }

    public void update(Observable obs, Object o){
        notificar((String) o);
    }

    public Proyecto clone(){
        Proyecto retorno = new Proyecto(nombre, responsable);
        retorno.red = red.clone();
        retorno.red.addObserver(retorno);
        return retorno;
    }

    // Métodos privados

    private void eliminarActividades(Vector<Actividad> vec){
        for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            removerActividad(a.getIdentificador());
        }
    }
    
    private void alcanzoFinalCritico(Suceso s, Vector<Vector<Actividad>> retorno, Vector<Actividad> temp){
        if(red.esSucesoFinal(s)){
            retorno.add((Vector<Actividad>)temp.clone());
        }
        Vector<Actividad> vas = s.getActividadesSalientes();
        for(Iterator<Actividad> it = vas.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.esCritica()){
                temp.add(a);
                alcanzoFinalCritico(a.getSucesoFin(), retorno, temp);
                temp.remove(a);
            }
        }
    }

    private Integer[] calcularRecursos(Integer dias_totales){
        Integer[] retorno = new Integer[dias_totales];
        for(int dia = 0; dia < dias_totales; dia++)
            retorno[dia] = 0;
        Vector<Actividad> vec_ac = getActividades();
        int duracion, recurso;
        for(int dia = 0; dia < dias_totales; dia++){
            for(Iterator<Actividad> it = vec_ac.iterator(); it.hasNext();){
                Actividad a = it.next();
                if(a.getSucesoOrigen().getFt().intValue() == dia && a.getParametrosNormales().getValor() > 0){
                    duracion = a.getParametrosNormales().getTiempo().intValue();
                    recurso = a.getParametrosNormales().getValor().intValue();
                    for(int i = dia; i <= dia + (duracion - 1); i++)
                        if(i < retorno.length)
                            retorno[i] = retorno[i] + recurso;
                }
            }
        }
        return retorno;
    }

    private Integer getSumaRecDistribucion(){
        if(getSucesoFinal() != null && getSucesoFinal().getFt() != 0){
            Integer dias_totales = getSucesoFinal().getFt();
            Integer[] recursos = calcularRecursos(dias_totales);
            Integer recursos_totales = 0;
            for(int dia = 0; dia < dias_totales.intValue(); dia ++)
                recursos_totales = recursos_totales + recursos[dia];
            return recursos_totales;
        }
        return 0;
    }
    
}
