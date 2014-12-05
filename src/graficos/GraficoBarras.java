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
import java.awt.GradientPaint;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class GraficoBarras extends Grafica{

    private CategoryDataset cjto_datos;
    private JFreeChart grafico;
    private ChartPanel panel_grafico;
    // Creo esta variable por que el removeSubtitles me remueve la leyenda
    private Vector<TextTitle> sub_titulos;
   
    public GraficoBarras(){
        inicializar();
    }

    // Métodos publicos

    public void dibujar(Hashtable<Integer, Integer> datos, Integer rec_max_disp, Vector<String> subtitulos){
        actualizarConjuntoDatos(datos, rec_max_disp);
        actualizarSubTitulos(subtitulos);
        grafico.getPlot().zoom(0.0);
    }

    public ChartPanel getPanelGrafico(){
        return panel_grafico;
    }

    public void destroy(){
        //grafico.clearSubtitles();
        ((DefaultCategoryDataset)cjto_datos).clear();
    }

    // Métodos privados.

    private void inicializar(){
        sub_titulos = new Vector<TextTitle>();
        cjto_datos = new DefaultCategoryDataset();
        grafico = crearGrafico(cjto_datos);
        panel_grafico = new ChartPanel(grafico);
        panel_grafico.setFillZoomRectangle(true);
        panel_grafico.setMouseWheelEnabled(true);
        panel_grafico.setPreferredSize(new Dimension(500, 270));
    }
    // Creo el conjunto de datos que utilizara el grafico para representar.
    private void actualizarConjuntoDatos(Hashtable<Integer, Integer> datos_tabla, Integer rec_max_disp){
        String serie_u = "Utilizados";
        String serie_d = "No utilizados";
        double num_u, num_d;
        Integer dias_totales = new Integer(datos_tabla.size());        
        for(Integer dia = 1; dia <= dias_totales; dia++){
            num_d = rec_max_disp - datos_tabla.get(dia);
            ((DefaultCategoryDataset)cjto_datos).addValue(num_d, serie_d, dia);
            num_u = datos_tabla.get(dia);
            ((DefaultCategoryDataset)cjto_datos).addValue(num_u, serie_u, dia);
        }                
    }

     private void actualizarSubTitulos(Vector<String> subtitulos){
        // Elimino los títulos existentes.
        for(Iterator<TextTitle> it = sub_titulos.iterator(); it.hasNext();){
            TextTitle t = it.next();
            grafico.removeSubtitle(t);
        }
        // Agrego los títulos nuevos.
        for(Iterator<String> it = subtitulos.iterator(); it.hasNext();){
            TextTitle t = new TextTitle(it.next());
            sub_titulos.add(t);
            grafico.addSubtitle(t);
        }
            
    }

     private static JFreeChart crearGrafico(CategoryDataset cjto_datos){
        JFreeChart grafico = ChartFactory.createBarChart3D(
            "Distribución de Recursos", // Título
            "Unidad de tiempo",         // Título eje x
            "Recurso",                  // Título eje y
            cjto_datos,                 // Datos
            PlotOrientation.VERTICAL,   // Orientación
            true,                       // Incluir leyenda
            true,                       // Incluir tooltips
            false                       // Incluir URLs
        );
        grafico.setBackgroundPaint(new Color(240, 240, 240));
        grafico.getPlot().zoom(0.0);
        CategoryPlot ploter = (CategoryPlot) grafico.getPlot();
        NumberAxis3D rangeAxis = (NumberAxis3D) ploter.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis3D.createIntegerTickUnits());
        BarRenderer3D renderer = (BarRenderer3D) ploter.getRenderer();
        renderer.setDrawBarOutline(false);
        GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f, 0.0f, new Color(0, 0, 64));
        GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f, 0.0f, new Color(0, 64, 0));
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        renderer.setItemMargin(0.0);
        CategoryAxis domainAxis = ploter.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.0));
        return grafico;
    }

}
