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

package graficos;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.gantt.Task;
import proyecto.Actividad;
import proyecto.Suceso;


public class GraficoGantt extends Grafica{

    private TaskSeries serie_tareas_nc;
    private TaskSeries serie_tareas_c;
    private TaskSeriesCollection cjto_datos;
    private JFreeChart grafico;
    private ChartPanel panel_grafico;

    public GraficoGantt(){
        inicializar();
    }

    // Métodos publicos

    public void dibujar(Suceso s_inicial, int unidad_tiempo, Date fecha_com){
        actualizarConjuntoDatos(s_inicial, unidad_tiempo, fecha_com);
        grafico.getPlot().zoom(0.0);
    }

    public ChartPanel getPanelGrafico(){
        return panel_grafico;
    }

     public void destroy(){
        serie_tareas_nc.removeAll();
        serie_tareas_c.removeAll();
        cjto_datos.removeAll();
    }

    // Métodos privados

    private void inicializar(){
        cjto_datos = new TaskSeriesCollection();
        serie_tareas_nc = new TaskSeries("No Criticas");
        serie_tareas_c = new TaskSeries("Criticas");
        cjto_datos.add(serie_tareas_nc);
        cjto_datos.add(serie_tareas_c);
        grafico = crearGrafico(cjto_datos);
        panel_grafico = new ChartPanel(grafico);
        panel_grafico.setFillZoomRectangle(true);
        panel_grafico.setMouseWheelEnabled(true);
        panel_grafico.setPreferredSize(new Dimension(500, 270));
    }

    private void actualizarConjuntoDatos(Suceso s_inicial, int unidad_tiempo, Date fecha_com){
        agregarTareasConcretas(s_inicial, fecha_com, unidad_tiempo);
        cjto_datos.add(serie_tareas_nc);
        cjto_datos.add(serie_tareas_c);
    }

    private void agregarTareasConcretas(Suceso suceso, Date fecha_com, int unidad_tiempo){
         Vector<Actividad> vec = suceso.getActividadesSalientes();         
         for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(!a.esFicticia()){
                Date fecha_fin = getFechaIncremento(fecha_com,unidad_tiempo, a.getParametrosNormales().getTiempo());
                Task tarea;
                if(a.esCritica()){
                    tarea = new Task("("+a.getIdentificador().toString() +") " +a.getDescripcion(),new SimpleTimePeriod((Date)fecha_com.clone(), fecha_fin));
                    serie_tareas_c.add(tarea);
                }
                else{
                    tarea = new Task("("+a.getIdentificador().toString() +") " +a.getDescripcion(),new SimpleTimePeriod((Date)fecha_com.clone(), fecha_fin));
                    serie_tareas_nc.add(tarea);
                }
                agregarTareasConcretas(a.getSucesoFin(), fecha_fin, unidad_tiempo);
            }
            else
                agregarTareasConcretas(a.getSucesoFin(), fecha_com, unidad_tiempo);
         }
    }

    private void agregarTareasFicticias(Hashtable<Integer, Date> actFecha_com, Hashtable<Integer, Date> actFecha_fin, Vector<Actividad> vec_ficticias){
        for(Iterator<Actividad> it = vec_ficticias.iterator(); it.hasNext();){
            Actividad a = it.next();
            Vector<Actividad> vec_ent = a.getSucesoOrigen().getActividadesEntrantes();
            Vector<Actividad> vec_sal = a.getSucesoFin().getActividadesSalientes();
            boolean listo_fc = false;
            boolean listo_ff = false;
            Date fecha_com = new Date();
            Date fecha_fin = new Date();
            for(Iterator<Actividad> it0 = vec_ent.iterator(); it0.hasNext() && !listo_fc;){
                Actividad ae = it0.next();
                if(!ae.esFicticia()){
                    fecha_com = actFecha_fin.get(ae.getIdentificador());
                    listo_fc = true;
                }
            }
            for(Iterator<Actividad> it0 = vec_sal.iterator(); it0.hasNext() && !listo_ff;){
                Actividad as = it0.next();
                if(!as.esFicticia()){
                    fecha_fin = actFecha_com.get(as.getIdentificador());
                    listo_ff = true;
                }
            }
            if(listo_fc && listo_ff){
                Task tarea = new Task("F ("+a.getIdentificador().toString() +") " +a.getDescripcion(),new SimpleTimePeriod(fecha_com, fecha_fin));
                serie_tareas_nc.add(tarea);
            }
        }
    }
       
    private Date getFechaIncremento(Date fecha_com, int unidad_tiempo, int incremento){
        Calendar calendario = Calendar.getInstance();
        calendario.set(fecha_com.getYear() + 1900,fecha_com.getMonth() ,fecha_com.getDate());
        if(unidad_tiempo == Calendar.DATE)
            calendario.add(Calendar.DATE, incremento);
        else if(unidad_tiempo == Calendar.MONTH)
            calendario.add(Calendar.MONTH, incremento);
        else
            calendario.add(Calendar.YEAR, incremento);
        Date fecha = calendario.getTime();
        return fecha;
    }

     private static JFreeChart crearGrafico(IntervalCategoryDataset cjto_datos) {
        JFreeChart grafico = ChartFactory.createGanttChart(
            "Diagrama de Gantt",        // Título
            "Actividad",                // Título eje x
            "Fecha",                    // Título eje y
            cjto_datos,                 // Datos
            true,                       // Incluir leyenda
            true,                       // Incluir tooltips
            false                       // Incluir URLs
        );
        grafico.setBackgroundPaint(new Color(240, 240, 240));
        grafico.getPlot().zoom(0.0);
        CategoryPlot categoriaPlot = (CategoryPlot)grafico.getPlot();
        GanttRenderer renderer = (GanttRenderer) categoriaPlot.getRenderer();
        renderer.setDrawBarOutline(true);
        // GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, new Color(102, 255, 102), 0.0f, 0.0f, new Color(102, 255, 102));
        renderer.setSeriesPaint(0,new Color(48, 239, 48));
        renderer.setSeriesPaint(1,Color.RED);
        grafico.getPlot().setOutlineVisible(true);
        return grafico;
    }

}
