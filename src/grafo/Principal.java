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

package grafo;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import interfaz.EditorDeProyectosView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jgraph.JGraph;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;
import javax.swing.event.UndoableEditEvent;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.GraphContext;
import proyecto.Actividad;
import proyecto.Suceso;

public class Principal {

    private JGraph grafo;
    private DefaultGraphCell[] elementos_graficos;
    private GraphUndoManager undoManager;
    private int cont_cells = -1;
    private Hashtable<Integer, DefaultGraphCell> sucesos_grafica;
    private Hashtable<DefaultGraphCell, Integer> grafica_sucesos;
    private Hashtable<Integer, DefaultGraphCell> actividades_grafica;
    private Hashtable<DefaultGraphCell, Integer> grafica_actividades;

    private EditorDeProyectosView itv;
    private JScrollPane areaGrafica;

    public Principal(EditorDeProyectosView itv){
        this.itv = itv;
        areaGrafica = null;
        edicionGrafica();
    }

    public void destroy(){
        PortView.renderer = new PortRenderer();
        EdgeView.renderer = new EdgeRenderer();
        AbstractCellView.cellEditor = new DefaultGraphCellEditor();
        VertexView.renderer = new VertexRenderer();
        ((DefaultGraphModel) grafo.getModel()).remove(grafo.getRoots());
        grafo.clearOffscreen();
    }

    // Metodos publicos

    public JGraph getGrafo(){
        return grafo;
    }

    public JScrollPane getAreaGrafica(){
        return areaGrafica;
    }

    public void setAreaGrafica(JScrollPane areaGrafica){
        this.areaGrafica = areaGrafica;
    }

    public void edicionGrafica(){
	grafo = crearGrafico();
     	grafo.setMarqueeHandler(createMarqueeHandler());
	undoManager = new GraphUndoManager() {
            public void undoableEditHappened(UndoableEditEvent e) {
                super.undoableEditHappened(e);
                //updateHistoryButtons();
            }
        };
        instalarlListeners(grafo);
    }

    public void disposicion(boolean organic, boolean horizontal){
        JGraphFacade facade = createFacade(grafo);
        if (!organic){
            JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
            if (horizontal)
                layout.setOrientation(SwingConstants.WEST);
            else
                layout.setOrientation(SwingConstants.NORTH);
            layout.setInterRankCellSpacing(50);
            layout.setIntraCellSpacing(60);
            try {
                    layout.run(facade);
            } catch (Exception e) {
                    System.err.println(e.getMessage());
                    organic = true;
            }
        }
        if (organic){
            JGraphLayout layout = new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE);
            layout.run(facade);
            JGraphFastOrganicLayout fastOrganicLayout = new JGraphFastOrganicLayout();
            fastOrganicLayout.setForceConstant(80);
            fastOrganicLayout.run(facade);
            JGraphOrganicLayout organicLayout = new JGraphOrganicLayout();
            organicLayout.setRadiusScaleFactor(0.9);
            organicLayout.setNodeDistributionCostFactor(8000000.0);
            organicLayout.setOptimizeBorderLine(false);
            organicLayout.setDeterministic(true);
            organicLayout.run(facade);
        }
        Map map = facade.createNestedMap(true, true);
        grafo.getGraphLayoutCache().edit(map, null, null, null);
    }

     public JPopupMenu crearMenuEmergente(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();
        if (!grafo.isSelectionEmpty()) {
            menu.addSeparator();
            menu.add(new AbstractAction("Remover Elemento") {
                public void actionPerformed(ActionEvent e) {
                   itv.removerElementosSeleccionados();
                }
            });
        }
        if(itv.getProyecto() != null){
            menu.addSeparator();
            menu.add(new AbstractAction("Crear suceso") {
                public void actionPerformed(ActionEvent ev) {
                    itv.nuevoSuceso();
                }
            });            
        }
        if(itv.getProyecto() != null && itv.getProyecto().getSucesos() != null && itv.getProyecto().getSucesos().size() > 1){
            menu.addSeparator();
            menu.add(new AbstractAction("Crear actividad") {
                public void actionPerformed(ActionEvent ev) {
                    itv.nuevaActividad();
                }
            });
        }
        if(itv.getProyecto() != null){
            menu.addSeparator();
            menu.add(new AbstractAction("Exportar imagen") {
                public void actionPerformed(ActionEvent ev) {
                    itv.exportarImagen();
                }
            });
        }
        menu.addSeparator();
        return menu;
    }

    public void dibujar(){
        cont_cells = -1;
        if(itv.getProyecto().getSucesos() != null && itv.getProyecto().getSucesos().size() != 0 && itv.getProyecto().getActividades() != null && itv.getProyecto().getActividades().size() != 0){
            elementos_graficos = new DefaultGraphCell[itv.getProyecto().getSucesos().size() + itv.getProyecto().getActividades().size()];
            crearSucesos();
            crearActividades();            
            grafo.getGraphLayoutCache().insert(elementos_graficos);
            disposicion(false, true);
        }
        else if(itv.getProyecto().getSucesos() != null && itv.getProyecto().getSucesos().size() != 0 && itv.getProyecto().getActividades() != null && itv.getProyecto().getActividades().size() == 0){
            elementos_graficos = new DefaultGraphCell[itv.getProyecto().getSucesos().size()];
            crearSucesos();
            grafo.getGraphLayoutCache().insert(elementos_graficos);
            disposicion(false, true);
        }
    }

    public Hashtable<Integer, DefaultGraphCell> getSucesosGrafica(){
        return sucesos_grafica;
    }

    public Hashtable<DefaultGraphCell, Integer> getGraficaSucesos(){
        return grafica_sucesos;
    }

    public Hashtable<Integer, DefaultGraphCell> getActividadesGrafica(){
        return actividades_grafica;
    }

    public Hashtable<DefaultGraphCell, Integer> getGraficaActividades(){
        return grafica_actividades;
    }

    public EditorDeProyectosView getPrincipal(){
        return itv;
    }

    // Metodos privados

    // Crea los sucesos graficos.
    private void crearSucesos(){
        Font fuente_suceso = new Font("Suceso", Font.BOLD, 10);
        Vector<Suceso> sucesos = itv.getProyecto().getSucesos();
        sucesos_grafica = new Hashtable<Integer, DefaultGraphCell>();
        grafica_sucesos = new Hashtable<DefaultGraphCell, Integer>();
        for(Iterator<Suceso> it = sucesos.iterator(); it.hasNext();){
            Suceso s = it.next();
            cont_cells ++;
            if(s.getFT() == Integer.MAX_VALUE)
                elementos_graficos[cont_cells] = new DefaultGraphCell(new String(" [ " + s.getIdentificador() + " ] [ Ft: " + s.getFt() + " ] [ FT:] "));
            else
                elementos_graficos[cont_cells] = new DefaultGraphCell(new String(" [ " + s.getIdentificador() + " ] [ Ft: " + s.getFt() + " ] [ FT: " + s.getFT() + " ] "));
            GraphConstants.setBounds(elementos_graficos[cont_cells].getAttributes(), new Rectangle2D.Double(150,30,150,30));
            if(itv.getProyecto().definidosSucesosEspeciales() && (itv.getProyecto().getSucesoInicial().equals(s) ||itv.getProyecto().getSucesoFinal().equals(s))){
                GraphConstants.setBackground(elementos_graficos[cont_cells].getAttributes(), Color.blue);
            }
            else{
                GraphConstants.setBackground(elementos_graficos[cont_cells].getAttributes(), Color.green);
            }
            GraphConstants.setGradientColor(elementos_graficos[cont_cells].getAttributes(),Color.lightGray);
            GraphConstants.setForeground(elementos_graficos[cont_cells].getAttributes(), Color.black);
            GraphConstants.setBorder(elementos_graficos[cont_cells].getAttributes(), BorderFactory.createRaisedBevelBorder());
            GraphConstants.setBorder(elementos_graficos[cont_cells].getAttributes(), BorderFactory.createLineBorder(Color.green, 1));
            GraphConstants.setOpaque(elementos_graficos[cont_cells].getAttributes(), true);
            GraphConstants.setEditable(elementos_graficos[cont_cells].getAttributes(), false);
            GraphConstants.setAutoSize(elementos_graficos[cont_cells].getAttributes(), false);
            GraphConstants.setFont(elementos_graficos[cont_cells].getAttributes(), fuente_suceso);
            DefaultPort port_cont_cells = new DefaultPort();
            elementos_graficos[cont_cells].add(port_cont_cells);
            sucesos_grafica.put(s.getIdentificador(), elementos_graficos[cont_cells]);
            grafica_sucesos.put(elementos_graficos[cont_cells], s.getIdentificador());
        }
    }
    // Crear las actividades graficas.
    private void crearActividades(){
        Font fuente_actividad = new Font("Actividad", Font.BOLD, 10);
        Vector<Actividad> actividades = itv.getProyecto().getActividades();
        Vector<Actividad> actividades_criticas = new Vector<Actividad>();
        marcarCaminosCriticos(actividades_criticas);
        actividades_grafica = new Hashtable<Integer, DefaultGraphCell>();
        grafica_actividades = new Hashtable<DefaultGraphCell, Integer>();
        for(Iterator<Actividad> it = actividades.iterator(); it.hasNext();){
            Actividad a = it.next();
            cont_cells ++;
            DefaultEdge flecha = new DefaultEdge("Id: " + a.getIdentificador());
            int estilo_flecha = GraphConstants.ARROW_CLASSIC;
            int estilo_linea = GraphConstants.STYLE_SPLINE;
            GraphConstants.setLineStyle(flecha.getAttributes(), estilo_linea);
            GraphConstants.setLineEnd(flecha.getAttributes(), estilo_flecha);
            GraphConstants.setEndFill(flecha.getAttributes(), true);
            GraphConstants.setEditable(flecha.getAttributes(), false);
            GraphConstants.setLabelPosition(flecha.getAttributes(),new Point2D.Double(GraphConstants.PERMILLE/2, -6) );
            GraphConstants.setFont(flecha.getAttributes(), fuente_actividad);
            GraphConstants.setForeground(flecha.getAttributes(), Color.blue);
            if(a.esCritica())
                GraphConstants.setLineColor(flecha.getAttributes(), Color.RED);
            flecha.setSource(sucesos_grafica.get(a.getSucesoOrigen().getIdentificador()).getChildAt(0));
            flecha.setTarget(sucesos_grafica.get(a.getSucesoFin().getIdentificador()).getChildAt(0));
            if(a.esFicticia()){ // actividades_criticas.contains(a)
              float[] interlineado = new float[2];
              interlineado[0] = 5;  // Espacio en color.
              interlineado[1] = 5;  // Espacio vacio.
              GraphConstants.setDashPattern(flecha.getAttributes(), interlineado);
            }
            elementos_graficos[cont_cells] = flecha;
            actividades_grafica.put(a.getIdentificador(), flecha);
            grafica_actividades.put(flecha, a.getIdentificador());
        }
    }

    private void  marcarCaminosCriticos(Vector<Actividad> vec){
        Vector<Vector<Actividad>> vec_caminos = itv.getProyecto().getCaminosCriticos();
        for(Iterator<Vector<Actividad>> it  = vec_caminos.iterator(); it.hasNext();){
            Vector<Actividad> vec_actividades = it.next();
            for(Iterator<Actividad> it0 = vec_actividades.iterator(); it0.hasNext();)
                vec.add(it0.next());
        }
    }
    
    private JGraph crearGrafico(){
        Modelo modelo = new Modelo();
        JGraph graph = new Grafico(modelo);
        graph.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
            protected EdgeView createEdgeView(Object cell) {
                return new EdgeView(cell) {
                    public CellHandle getHandle(GraphContext context) {
                            return new ManejadorArco(this, context);
                    }
                };
            }
        });
        return graph;
    }

    private void instalarlListeners(JGraph graph){
        graph.getModel().addUndoableEditListener(undoManager);
        graph.getSelectionModel().addGraphSelectionListener(itv);
        graph.addKeyListener((KeyListener) itv);
    }

    private BasicMarqueeHandler createMarqueeHandler() {
        return new ManejadorEventos(grafo, this);
    }

    private JGraphFacade createFacade(JGraph graph){
        JGraphFacade facade = new JGraphFacade(graph);
        facade.setIgnoresUnconnectedCells(true);
        facade.setIgnoresCellsInGroups(true);
        facade.setIgnoresHiddenCells(true);
        facade.setDirected(true);
        return facade;
    }

}
