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

package interfaz;

import graficos.GraficoBarras;
import manajadorArchivos.ManejadorImagen;
import grafo.Principal;
import graficos.GraficoGantt;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import manajadorArchivos.ManejadorXML;
import org.jfree.ui.ExtensionFileFilter;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphCell;
import proyecto.Actividad;
import proyecto.Concreta;
import proyecto.Ficticia;
import proyecto.Proyecto;
import proyecto.Suceso;

public class EditorDeProyectosView extends FrameView implements GraphSelectionListener,	KeyListener, Observer{

    public EditorDeProyectosView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        inicializarElementos();
        desabilitarElementosInicio();
    }

    // Acciones.

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = EditorDeProyectosApp.getApplication().getMainFrame();
            aboutBox = new EditorDeProyectosAcercaDe(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        EditorDeProyectosApp.getApplication().show(aboutBox);
    } 
    
    @Action
    public void nuevo(){
        salvarProyecto(EnumOperaciones.nuevo);
    }    
   
    @Action
    public void abrir(){
        salvarProyecto(EnumOperaciones.abrir);
    }

    @Action
    public void guardar(){
        jFileChooserAbrirGuardar.setCurrentDirectory(new File(".//EditorDeProyectos//proyectos"));
        int returnVal = jFileChooserAbrirGuardar.showSaveDialog(jButton3);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File archivo = jFileChooserAbrirGuardar.getSelectedFile();
            ManejadorXML mxml = new ManejadorXML();
            mxml.addObserver(this);
            proyecto_actual.deleteObservers();
            mxml.escribirObjeto(proyecto_actual, archivo);
            mostrarMensaje("El proyecto se guardo con exito en " + archivo.getPath() + ".xml");
        }
        else {
            jTextArea1.setEnabled(true);
            // mostrarMensaje("El proyecto actual no se guardo.");
        }
    }

    @Action
    public void salir(){
        salvarProyecto(EnumOperaciones.salida);
    }

    @Action
    public void nuevaActividad(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        nueva_actividad = new NuevaActividad(this);
        nueva_actividad.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(nueva_actividad);
        mainFrame.setEnabled(false);
    }

    @Action
    public void borrarActividad(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        borrar_actividad = new BorrarActividad(this);
        borrar_actividad.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(borrar_actividad);
        mainFrame.setEnabled(false);
    }

    @Action
    public void definirSucesos(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        definir_estados = new DefinirSucesos(this);
        definir_estados.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(definir_estados);
        mainFrame.setEnabled(false);
    }

    @Action
    public void nuevoSuceso(){
        proyecto_actual.agregarSuceso();
        setBanderaModificacion(true);
        refrescarGraficaGrafo();
        if(proyecto_actual.getSucesos().size() > 1){
            habilitarElementosCantSucesos();
        }
    }

    @Action
    public void borrarSuceso(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        borrar_sucesos = new BorrarSuceso(this);
        borrar_sucesos.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(borrar_sucesos);
        mainFrame.setEnabled(false);
    }

    @Action
    public void cambiarCaracteristicasAdicionales(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        caracteristicas_ad = new CaracteristicasAdicionales(this);
        caracteristicas_ad.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(caracteristicas_ad);
        mainFrame.setEnabled(false);
    }

    @Action
    public void correrAlgortimo(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        ejecutar_algoritmo = new EjecutarAlgoritmo(this);
        ejecutar_algoritmo.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(ejecutar_algoritmo);
        mainFrame.setEnabled(false);
        refrescarTabla();
    }

    @Action
    public void escalaPredeterminada(){
        grafo_visual_actual.getGrafo().setScale(1.0);
    }
   
    @Action
    public void cambiarAlineacion(){
        URL conexion;
        if(horizontal){
            horizontal = false;
            conexion = getClass().getClassLoader().getResource("interfaz/resources/iconos/grafica/Vertical.png");
            grafo_visual_actual.disposicion(false, horizontal);
        }
        else{
            horizontal = true;
            conexion = getClass().getClassLoader().getResource("interfaz/resources/iconos/grafica/Horizontal.png");
            grafo_visual_actual.disposicion(false, horizontal);
        }
        ImageIcon icono = new ImageIcon(conexion);
        jButton15.setIcon(icono);
    }

    @Action
    public void cambiarConexion(){
        grafo_visual_actual.getGrafo().setPortsVisible(!grafo_visual_actual.getGrafo().isPortsVisible());
        URL conexion;
        if(grafo_visual_actual.getGrafo().isPortsVisible()){
            conexion = getClass().getClassLoader().getResource("interfaz/resources/iconos/grafica/Conexion.png");
        }
        else{
            conexion = getClass().getClassLoader().getResource("interfaz/resources/iconos/grafica/Sin conexion.png");
        }
        ImageIcon icono = new ImageIcon(conexion);
        jButton16.setIcon(icono);
    }

    @Action
    public void removerElementosSeleccionados(){
        if(!grafo_visual_actual.getGrafo().isSelectionEmpty()){
            Object[] cells = grafo_visual_actual.getGrafo().getSelectionCells();
            cells = grafo_visual_actual.getGrafo().getDescendants(cells);
            Hashtable<DefaultGraphCell, Integer> grafica_sucesos = grafo_visual_actual.getGraficaSucesos();
            Hashtable<DefaultGraphCell, Integer> grafica_actividades = grafo_visual_actual.getGraficaActividades();
            for(int i = 0; i < cells.length; i++ ){
                if(grafica_actividades != null){
                    Integer id = grafica_actividades.get(cells[i]);
                    if(id != null){
                        proyecto_actual.removerActividad(id);
                        setBanderaModificacion(true);
                        if(proyecto_actual.getActividades() == null || proyecto_actual.getActividades().size() < 1)
                            desabilitarElementosCantAct();
                    }
                }
                if(grafica_sucesos != null){
                    Integer id = grafica_sucesos.get(cells[i]);
                    if(id != null){
                        Suceso s = proyecto_actual.getSuceso(id);
                        if(s.getActividadesEntrantes() != null){
                            Vector<Actividad> vec = s.getActividadesEntrantes();
                            for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
                                Actividad a = it.next();
                            }
                        }
                        if(s.getActividadesSalientes() != null){
                            Vector<Actividad> vec = s.getActividadesSalientes();
                            for(Iterator<Actividad> it = vec.iterator(); it.hasNext();){
                                Actividad a = it.next();
                            }
                        }
                        proyecto_actual.removerSuceso(id);
                        setBanderaModificacion(true);
                        if(proyecto_actual.getSucesos() == null || proyecto_actual.getSucesos().size() < 1)
                            desabilitarelementosCantSucesos();
                    }
                }
            }
            update(null, null);
        }
    }

    @Action
    public void exportarImagen(){
        jFileChooserExpImagen.setCurrentDirectory(new File(".//EditorDeProyectos//exportar"));
        int retorno = jFileChooserExpImagen.showSaveDialog(jButton18);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File archivo = jFileChooserExpImagen.getSelectedFile();
            ManejadorImagen exportador = new ManejadorImagen();
            exportador.addObserver(this);
            exportador.escribirObjeto(grafo_visual_actual.getGrafo(), archivo, asoc_formatos_graficos.get(jFileChooserExpImagen.getFileFilter().getDescription()));
            mostrarMensaje("Se exporto una imagen del diagrama actual en " + archivo.getPath() + "." + asoc_formatos_graficos.get(jFileChooserExpImagen.getFileFilter().getDescription()));
        }        
    }

   // Métodos publicos

    public Principal getGrafoVisual(){
        return grafo_visual_actual;
    }

    public void setGrafoVisual(Principal grafo_visual){
        this.grafo_visual_actual = grafo_visual;
    }

    public void setBanderaModificacion(boolean valor){
        modificado = valor;
    }

    public boolean getBanderaModificacion(){
        return modificado;
    }

    public void mostrarMensaje(String mensaje){
        jTextArea1.append(jTextArea1.getLineCount() +" - " + mensaje + "\n");
    }

    public Proyecto getProyecto(){
        return proyecto_actual;
    }

    public void setProyecto(Proyecto p){
        proyecto_actual = p;
    }

    public void habilitarElementosAlgoritmos(){
        jButton11.setEnabled(true);
    }

    public void desabilitarElementosAlgoritmos(){
        jButton11.setEnabled(false);
    }

    public void habilitarElementosCantAct(){
        jButton8.setEnabled(true);
    }

    public void desabilitarElementosCantAct(){
        jButton8.setEnabled(false);        
    }

    public void habilitarElementosCantSucesos(){
        jButton3.setEnabled(true);
        jButton5.setEnabled(true);
        jButton6.setEnabled(true);
        jButton9.setEnabled(true);
    }

    public void desabilitarelementosCantSucesos(){
        jButton3.setEnabled(false);
        jButton5.setEnabled(false);
        jButton6.setEnabled(false);
        jButton8.setEnabled(false);
        jButton9.setEnabled(false);
        jButton11.setEnabled(false);
    }

    public void habilitarElementosInicio(){
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        jButton7.setEnabled(true);
        jTextArea1.setEnabled(true);
        jTable_actual.setEnabled(true);
        jTabbedPane1.setEnabled(true);
        if(jTabbedPane1.getSelectedIndex() != 0)
            desabilitarElementosGrafo();
        else
            habilitarElementosGrafo();
    }

    public void  desabilitarElementosInicio(){
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);
        jButton5.setEnabled(false);
        jButton6.setEnabled(false);
        jButton7.setEnabled(false);
        jButton8.setEnabled(false);
        jButton9.setEnabled(false);
        jButton11.setEnabled(false);
        jTextArea1.setEnabled(false);
        jTable_actual.setEnabled(false);
        jTabbedPane1.setEnabled(false);
        desabilitarElementosGrafo();
    }

    public void inicializarElementos(){
        proyecto_actual = null;
        setBanderaModificacion(false);
        inicializarAreasDeTexto();
        inicializarTabla();
        cambiarEtiquetas("", "");
        inicializarFileChoosers();
        iniciarCaracteristicasAdicionales();
    }

    public void cambiarEtiquetas(String nombre_proyecto, String autor){
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nombre Proyecto: " + nombre_proyecto + " - Autor: " + autor, javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, Font.decode("Tahoma-Bold-10"), Color.BLACK));
    }

    public void nuevo_proyecto(){
        JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
        nuevo_proyecto = new NuevoProyecto(this);
        nuevo_proyecto.setLocationRelativeTo(mainFrame);
        EditorDeProyectosApp.getApplication().show(nuevo_proyecto);
        mainFrame.setEnabled(false);
        setBanderaModificacion(false);
    }

    public void abrir_proyecto(){
        jFileChooserAbrirGuardar.setCurrentDirectory(new File(".//EditorDeProyectos//proyectos"));
        int retorno = jFileChooserAbrirGuardar.showOpenDialog(jButton2);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            File archivo = jFileChooserAbrirGuardar.getSelectedFile();
            ManejadorXML mxml = new ManejadorXML();
            mxml.addObserver(this);
            // El proyecto que habro lo vuelco en uno temporal para luego clonarlo.
            // Esto se debe a que el metodo addObserver de la variable proyecto (ahora proyecto_tmp)
            // daba error de puntero nulo.            
            Proyecto proyecto_tmp = (Proyecto) mxml.leerObjeto(archivo);
            habilitarElementosInicio();
            if(proyecto_tmp != null)
                if(proyecto_tmp.getSucesos() != null && proyecto_tmp.getSucesos() != null && proyecto_tmp.getSucesos().size() > 1)
                    habilitarElementosCantSucesos();
                else
                    desabilitarelementosCantSucesos();
                if(proyecto_tmp.definidosSucesosEspeciales())
                    habilitarElementosAlgoritmos();
                else
                    desabilitarElementosAlgoritmos();
                if(proyecto_tmp.getCantActividades() == 0)
                    desabilitarElementosCantAct();
                else
                    habilitarElementosCantAct();
            proyecto_actual = null;
            proyecto_actual = proyecto_tmp.clone();
            agregarObservador();
            cambiarEtiquetas(proyecto_tmp.getNombre(), proyecto_tmp.getResponsable());
            setBanderaModificacion(false);
            update(null, null);
            mostrarMensaje("Se abrio con exito el proyecto desde " + archivo.getPath());
        }
    }

    public void cerrar(){
        proyecto_actual = null;
        cerrarGraficos();
        inicializarElementos();
        desabilitarElementosInicio();
        escalaPredeterminada();
    }

    public void proximaOperacion(EnumOperaciones operacion){
        if(operacion == EnumOperaciones.nuevo){
            cerrar();
            nuevo_proyecto();
        }
        else if(operacion == EnumOperaciones.abrir)
            abrir_proyecto();
        else
            System.exit(0);
    }

    public void agregarObservador(){
        proyecto_actual.addObserver(this);
    }

    public void refrescoParcialMAP(Integer cant_max_rec ){
        refrescarGraficaGrafo();
        refrescarGraficaBarras(cant_max_rec);
        refrescarGraficaGantt();
        refrescarTabla();
    }

    public void setCaracteristicasAdicionales(Date fecha_com, int unidad_tiempo){
        this.fecha_com = fecha_com;
        this.unidad_tiempo = unidad_tiempo;
    }

    public String getUnidadTiempoString(){
        if(unidad_tiempo == Calendar.DATE)
            return "dia";
        if(unidad_tiempo == Calendar.MONTH)
            return "mes";
        return "año";
    }

    public void refrescarGraficaGantt(){
        grafico_gantt.destroy();
        if(getProyecto().definidosSucesosEspeciales())
            grafico_gantt.dibujar(getProyecto().getSucesoInicial(), unidad_tiempo, fecha_com);
    }

    public void update(Observable obs, Object o){
        if(o != null)
            mostrarMensaje((String) o);
      refrescarElementos();
    }

    public void valueChanged(GraphSelectionEvent arg0) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    // Metodos privados.

    private void desabilitarElementosGrafo(){
        jButton12.setEnabled(false);
        jButton15.setEnabled(false);
        jButton16.setEnabled(false);
        jButton17.setEnabled(false);
        jButton18.setEnabled(false);
    }

    private void habilitarElementosGrafo(){
        jButton12.setEnabled(true);
        jButton15.setEnabled(true);
        jButton16.setEnabled(true);
        jButton17.setEnabled(true);
        jButton18.setEnabled(true);
    }

    private void vaciarTabla(javax.swing.table.DefaultTableModel modelo){
        try{
            while(modelo.getRowCount()>0){
                modelo.removeRow(0);
            }
        }
        catch(Exception e){
        }
    }

    private void  celdaTabla(){
        // Centrar las columnas de la tabla.
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        tcr.setVerticalAlignment(SwingConstants.CENTER);
        tcr.setHorizontalTextPosition(SwingConstants.LEFT);
        for(int c = 0; c < jTable_actual.getColumnCount();c++)
            jTable_actual.getColumnModel().getColumn(c).setCellRenderer(tcr);
        // Edición de celdas.
        CellEditorListener cel = new CellEditorListener(){
            public void editingStopped(ChangeEvent e) {
                int fila = jTable_actual.getSelectedRow();
                int col = jTable_actual.getSelectedColumn();
                Integer id_actividad = (Integer)jTable_actual.getValueAt(fila, 0);
                if(fila > -1 && col > -1){
                    Actividad a = getProyecto().getActividad(id_actividad);
                    if(!a.esFicticia()){
                        Concreta ac = (Concreta) a;
                        if(jTable_actual.getColumnName(col).equals("Duración")){
                            ac.setParametroNormalDuracion((Integer)jTable_actual.getValueAt(fila, col));
                        }
                        else if(jTable_actual.getColumnName(col).equals("Recurso")){
                            ac.setParametroNormalRecurso((Integer)jTable_actual.getValueAt(fila, col));
                        }
                        else if(jTable_actual.getColumnName(col).equals("Descripción"))
                            ac.setDescripcion((String)jTable_actual.getValueAt(fila, col));
                    }
                    else{
                        Ficticia af = (Ficticia) a;
                        af.setDescripcion((String)jTable_actual.getValueAt(fila, col));
                    }
                }
            }

            public void editingCanceled(ChangeEvent e) {
            }
        };
        jTable_actual.getDefaultEditor(Integer.class).addCellEditorListener(cel);
        jTable_actual.getDefaultEditor(String.class).addCellEditorListener(cel);
    }

    private void refrescarElementos(){
        refrescarFechas();
        refrescarGraficaGrafo();
        refrescarGraficaBarras(getProyecto().getMaximoRecursoDistribucion());
        refrescarGraficaGantt();
        refrescarTabla();
    }

    private void refrescarFechas(){
        proyecto_actual.resetearFechasSucesos();
        proyecto_actual.actualizarFechasSucesos();
    }

    private void refrescarTabla(){
        modelo_tabla_actual = (javax.swing.table.DefaultTableModel)jTable_actual.getModel();
        vaciarTabla(modelo_tabla_actual);
        Vector<Actividad> vec_a = proyecto_actual.getActividades();
        for(Iterator<Actividad> it = vec_a.iterator(); it.hasNext();){
            Actividad a = it.next();
            if(a.getSucesoOrigen().getFT() == Integer.MAX_VALUE && a.getSucesoFin().getFT() == Integer.MAX_VALUE)
                modelo_tabla_actual.addRow(new Object[]{a.getIdentificador(), a.getSucesoOrigen().getFt(), a.getSucesoOrigen().getFT(), a.getSucesoFin().getFt(), a.getSucesoFin().getFT(), a.getParametrosNormales().getTiempo(), a.getParametrosNormales().getValor(), a.getDescripcion(), a.getMargenTotal(), a.getMargenLibre(), a.getMargenIndependiente()});
            else if(a.getSucesoOrigen().getFT() == Integer.MAX_VALUE && a.getSucesoFin().getFT() != Integer.MAX_VALUE)
                modelo_tabla_actual.addRow(new Object[]{a.getIdentificador(), a.getSucesoOrigen().getFt(), a.getSucesoOrigen().getFT(), a.getSucesoFin().getFt(), a.getSucesoFin().getFT(), a.getParametrosNormales().getTiempo(), a.getParametrosNormales().getValor(), a.getDescripcion(), a.getMargenTotal(), a.getMargenLibre(), a.getMargenIndependiente()});
            else if(a.getSucesoOrigen().getFT() != Integer.MAX_VALUE && a.getSucesoFin().getFT() == Integer.MAX_VALUE)
                modelo_tabla_actual.addRow(new Object[]{a.getIdentificador(), a.getSucesoOrigen().getFt(), a.getSucesoOrigen().getFT(), a.getSucesoFin().getFt(), a.getSucesoFin().getFT(), a.getParametrosNormales().getTiempo(), a.getParametrosNormales().getValor(), a.getDescripcion(), a.getMargenTotal(), a.getMargenLibre(), a.getMargenIndependiente()});
            else
                modelo_tabla_actual.addRow(new Object[]{a.getIdentificador(), a.getSucesoOrigen().getFt(), a.getSucesoOrigen().getFT(), a.getSucesoFin().getFt(), a.getSucesoFin().getFT(), a.getParametrosNormales().getTiempo(), a.getParametrosNormales().getValor(), a.getDescripcion(), a.getMargenTotal(), a.getMargenLibre(), a.getMargenIndependiente()});
        }
    }

    private void refrescarGraficaGrafo(){
        grafo_visual_actual.destroy();
        grafo_visual_actual.dibujar();
    }

    private void refrescarGraficaBarras(Integer rec_max_disp){
        grafico_barras.destroy();
        grafico_barras.dibujar(getProyecto().distribucionRecursosUtilizados(), rec_max_disp, crearSubTitulos());
    }

    private Vector<String> crearSubTitulos(){
        Vector<String> retorno = new Vector<String>();
        retorno.add("Nivel de aprovechamiento = " + getProyecto().getProcentajeAprovechamiento() + " %");
        retorno.add("Disponibilidad promedio = " + getProyecto().getPromUnidadesRequeridasDiarias() + " unidades/" + getUnidadTiempoString());
        retorno.add("Total de recursos para absorber pico = " + getProyecto().getRecursosTotalesAbsorberPico() + " unidades");
        return retorno;
    }
 
    private void salvarProyecto(EnumOperaciones operacion){
        if(proyecto_actual != null && getBanderaModificacion()){
            JFrame mainFrame= EditorDeProyectosApp.getApplication().getMainFrame();
            salvar_proyecto = new SalvarProyecto(this, operacion);
            salvar_proyecto.setLocationRelativeTo(mainFrame);
            EditorDeProyectosApp.getApplication().show(salvar_proyecto);
            mainFrame.setEnabled(false);
        }
        else
            proximaOperacion(operacion);
    }

    private void aumentarEscala(Point2D punto){
        grafo_visual_actual.getGrafo().setScale(1.5 * grafo_visual_actual.getGrafo().getScale(), punto);
    }

    private void reducirEscala(Point2D punto){
        grafo_visual_actual.getGrafo().setScale(grafo_visual_actual.getGrafo().getScale() / 1.5, punto);
    }

    private void iniciarCaracteristicasAdicionales(){
        fecha_com = new Date();
        fecha_com.setHours(0);
        fecha_com.setMinutes(0);
        fecha_com.setSeconds(0);
        unidad_tiempo = Calendar.DATE;
    }

    private void cerrarGraficos(){
        grafo_visual_actual.destroy();
        grafico_barras.destroy();
        grafico_gantt.destroy();
    }
    
    private void inicializarAreasDeTexto(){
        jTextArea1.setText("");
        jTextArea1.removeAll();
    }       
    
    public void inicializarTabla(){
        vaciarTabla((javax.swing.table.DefaultTableModel)jTable_actual.getModel());
        celdaTabla();
    }

    private void inicializarFileChoosers(){
        ExtensionFileFilter filtro_xml = new ExtensionFileFilter("Formato XML (Extensible Markup Language) (*.xml)", ".xml");
        jFileChooserAbrirGuardar.setFileFilter(filtro_xml);

        asoc_formatos_graficos = new Hashtable<String, String>();
        ExtensionFileFilter filtro_gif = new ExtensionFileFilter("Formato GIF (Graphics Interchange Format) (*.gif)", ".gif");
        asoc_formatos_graficos.put("Formato GIF (Graphics Interchange Format) (*.gif)", "gif");
        ExtensionFileFilter filtro_jpg = new ExtensionFileFilter("Formato JPG (Joint Photographic Experts Group) (*.jpg)", ".jpg");
        asoc_formatos_graficos.put("Formato JPG (Joint Photographic Experts Group) (*.jpg)", "jpg");
        ExtensionFileFilter filtro_jpeg = new ExtensionFileFilter("Formato JPG (Joint Photographic Experts Group) (*.jpeg)", ".jpeg");
        asoc_formatos_graficos.put("Formato JPG (Joint Photographic Experts Group) (*.jpeg)", "jpeg");
        ExtensionFileFilter filtro_bmp = new ExtensionFileFilter("Formato BMP (Bits Map) (*.bmp)", "bmp");
        asoc_formatos_graficos.put("Formato BMP (Bits Map) (*.bmp)", "bmp");
        ExtensionFileFilter filtro_png = new ExtensionFileFilter("Formato PNG (Portable NetWork Graphics) (*.png)", ".png");
        asoc_formatos_graficos.put("Formato PNG (Portable NetWork Graphics) (*.png)", "png");
        jFileChooserExpImagen.setFileFilter(filtro_gif);
        jFileChooserExpImagen.setFileFilter(filtro_jpg);
        jFileChooserExpImagen.setFileFilter(filtro_jpeg);
        jFileChooserExpImagen.setFileFilter(filtro_bmp);
        jFileChooserExpImagen.setFileFilter(filtro_png);
        
    }

    public boolean correBajoWindows(){
        if(System.getProperty("os.name").substring(0, 7).matches("Windows"))
            return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton18 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel_actual = new javax.swing.JPanel();
        grafo_visual_actual = new grafo.Principal(this);
        grafo_visual_actual.edicionGrafica();
        jScrollPane_grafo_actual = new javax.swing.JScrollPane(grafo_visual_actual.getGrafo());
        grafo_visual_actual.setAreaGrafica(jScrollPane_grafo_actual);
        jPanel_sub_actual = new javax.swing.JPanel();
        jScrollPane_tabla = new javax.swing.JScrollPane();
        jTable_actual = new javax.swing.JTable();
        grafico_barras = new GraficoBarras();
        jScrollPane2 = new javax.swing.JScrollPane(grafico_barras.getPanelGrafico());
        grafico_gantt = new GraficoGantt();
        jScrollPane3 = new javax.swing.JScrollPane(grafico_gantt.getPanelGrafico());
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jFileChooserAbrirGuardar = new javax.swing.JFileChooser();
        jFileChooserExpImagen = new javax.swing.JFileChooser();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(interfaz.EditorDeProyectosApp.class).getContext().getResourceMap(EditorDeProyectosView.class);
        mainPanel.setFont(resourceMap.getFont("mainPanel.font")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel1.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel1.border.titleFont"), resourceMap.getColor("jPanel1.border.titleColor"))); // NOI18N
        jPanel1.setToolTipText(resourceMap.getString("jPanel1.toolTipText")); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setBackground(resourceMap.getColor("jTextArea1.background")); // NOI18N
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(resourceMap.getFont("jTextArea1.font")); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setToolTipText(resourceMap.getString("jTextArea1.toolTipText")); // NOI18N
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
        );

        jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar1.setRollover(true);
        jToolBar1.setToolTipText(resourceMap.getString("jToolBar1.toolTipText")); // NOI18N
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(interfaz.EditorDeProyectosApp.class).getContext().getActionMap(EditorDeProyectosView.class, this);
        jButton1.setAction(actionMap.get("nuevo")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton2.setAction(actionMap.get("abrir")); // NOI18N
        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jButton3.setAction(actionMap.get("guardar")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setToolTipText(resourceMap.getString("jButton3.toolTipText")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        jButton5.setAction(actionMap.get("definirSucesos")); // NOI18N
        jButton5.setIcon(resourceMap.getIcon("jButton5.icon")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setToolTipText(resourceMap.getString("jButton5.toolTipText")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton5);

        jButton7.setAction(actionMap.get("nuevoSuceso")); // NOI18N
        jButton7.setIcon(resourceMap.getIcon("jButton7.icon")); // NOI18N
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setToolTipText(resourceMap.getString("jButton7.toolTipText")); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton7);

        jButton6.setAction(actionMap.get("borrarSuceso")); // NOI18N
        jButton6.setIcon(resourceMap.getIcon("jButton6.icon")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setToolTipText(resourceMap.getString("jButton6.toolTipText")); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton6);

        jButton8.setAction(actionMap.get("borrarActividad")); // NOI18N
        jButton8.setIcon(resourceMap.getIcon("jButton8.icon")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setToolTipText(resourceMap.getString("jButton8.toolTipText")); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton8);

        jButton9.setAction(actionMap.get("nuevaActividad")); // NOI18N
        jButton9.setIcon(resourceMap.getIcon("jButton9.icon")); // NOI18N
        jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
        jButton9.setToolTipText(resourceMap.getString("jButton9.toolTipText")); // NOI18N
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton9);

        jButton11.setAction(actionMap.get("correrAlgortimo")); // NOI18N
        jButton11.setIcon(resourceMap.getIcon("jButton11.icon")); // NOI18N
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setToolTipText(resourceMap.getString("jButton11.toolTipText")); // NOI18N
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setName("jButton11"); // NOI18N
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton11);

        jButton4.setAction(actionMap.get("cambiarCaracteristicasAdicionales")); // NOI18N
        jButton4.setIcon(resourceMap.getIcon("jButton4.icon")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setToolTipText(resourceMap.getString("jButton4.toolTipText")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton4);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        jButton18.setAction(actionMap.get("exportarImagen")); // NOI18N
        jButton18.setIcon(resourceMap.getIcon("jButton18.icon")); // NOI18N
        jButton18.setText(resourceMap.getString("jButton18.text")); // NOI18N
        jButton18.setToolTipText(resourceMap.getString("jButton18.toolTipText")); // NOI18N
        jButton18.setFocusable(false);
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setName("jButton18"); // NOI18N
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton18);

        jButton12.setAction(actionMap.get("escalaPredeterminada")); // NOI18N
        jButton12.setIcon(resourceMap.getIcon("jButton12.icon")); // NOI18N
        jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
        jButton12.setToolTipText(resourceMap.getString("jButton12.toolTipText")); // NOI18N
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setName("jButton12"); // NOI18N
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton12);

        jButton15.setAction(actionMap.get("cambiarAlineacion")); // NOI18N
        jButton15.setIcon(resourceMap.getIcon("jButton15.icon")); // NOI18N
        jButton15.setText(resourceMap.getString("jButton15.text")); // NOI18N
        jButton15.setToolTipText(resourceMap.getString("jButton15.toolTipText")); // NOI18N
        jButton15.setFocusable(false);
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setName("jButton15"); // NOI18N
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton15);

        jButton16.setAction(actionMap.get("cambiarConexion")); // NOI18N
        jButton16.setIcon(resourceMap.getIcon("jButton16.icon")); // NOI18N
        jButton16.setText(resourceMap.getString("jButton16.text")); // NOI18N
        jButton16.setToolTipText(resourceMap.getString("jButton16.toolTipText")); // NOI18N
        jButton16.setFocusable(false);
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setName("jButton16"); // NOI18N
        jButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton16);

        jButton17.setAction(actionMap.get("removerElementosSeleccionados")); // NOI18N
        jButton17.setIcon(resourceMap.getIcon("jButton17.icon")); // NOI18N
        jButton17.setText(resourceMap.getString("jButton17.text")); // NOI18N
        jButton17.setToolTipText(resourceMap.getString("jButton17.toolTipText")); // NOI18N
        jButton17.setFocusable(false);
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setName("jButton17"); // NOI18N
        jButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton17);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jTabbedPane1.setForeground(resourceMap.getColor("jTabbedPane1.foreground")); // NOI18N
        jTabbedPane1.setFont(resourceMap.getFont("jTabbedPane1.font")); // NOI18N
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jTabbedPane1MouseWheelMoved(evt);
            }
        });
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jPanel_actual.setName("jPanel_actual"); // NOI18N

        jScrollPane_grafo_actual.setToolTipText(resourceMap.getString("jScrollPane_grafo_actual.toolTipText")); // NOI18N
        jScrollPane_grafo_actual.setName("jScrollPane_grafo_actual"); // NOI18N
        jScrollPane_grafo_actual.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jScrollPane_grafo_actualMouseWheelMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel_actualLayout = new javax.swing.GroupLayout(jPanel_actual);
        jPanel_actual.setLayout(jPanel_actualLayout);
        jPanel_actualLayout.setHorizontalGroup(
            jPanel_actualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane_grafo_actual, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
        );
        jPanel_actualLayout.setVerticalGroup(
            jPanel_actualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane_grafo_actual, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel_actual.TabConstraints.tabTitle"), jPanel_actual); // NOI18N

        jPanel_sub_actual.setToolTipText(resourceMap.getString("jPanel_sub_actual.toolTipText")); // NOI18N
        jPanel_sub_actual.setName("jPanel_sub_actual"); // NOI18N

        jScrollPane_tabla.setName("jScrollPane_tabla"); // NOI18N

        jTable_actual.setAutoCreateRowSorter(true);
        jTable_actual.setBackground(resourceMap.getColor("jTable_actual.background")); // NOI18N
        jTable_actual.setFont(resourceMap.getFont("jTable_actual.font")); // NOI18N
        jTable_actual.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Identificador", "Ft i", "FT i", "Ft j", "FT j", "Duración", "Recurso", "Descripción", "Margen Total", "Margen Libre", "Margen Independiente"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class , java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, true, true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable (int row, int column){
                int fila = jTable_actual.getSelectedRow();
                int col = jTable_actual.getSelectedColumn();
                if(fila > -1 && col > -1){
                    Integer id_actividad = (Integer)jTable_actual.getValueAt(fila, 0);
                    Actividad a = getProyecto().getActividad(id_actividad);
                    if(a.esFicticia()){
                        if (col == 5 || col == 6)
                        return false;
                    }
                }
                return canEdit [column];
            }
        });
        jTable_actual.setToolTipText(resourceMap.getString("jTable_actual.toolTipText")); // NOI18N
        jTable_actual.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        jTable_actual.setName("jTable_actual");
        jScrollPane_tabla.setViewportView(jTable_actual);

        javax.swing.GroupLayout jPanel_sub_actualLayout = new javax.swing.GroupLayout(jPanel_sub_actual);
        jPanel_sub_actual.setLayout(jPanel_sub_actualLayout);
        jPanel_sub_actualLayout.setHorizontalGroup(
            jPanel_sub_actualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane_tabla, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
        );
        jPanel_sub_actualLayout.setVerticalGroup(
            jPanel_sub_actualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane_tabla, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel_sub_actual.TabConstraints.tabTitle"), jPanel_sub_actual); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N
        jTabbedPane1.addTab(resourceMap.getString("jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N
        jTabbedPane1.addTab(resourceMap.getString("jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        menuBar.setFont(resourceMap.getFont("menuBar.font")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setFont(resourceMap.getFont("fileMenu.font")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("salir")); // NOI18N
        exitMenuItem.setFont(resourceMap.getFont("exitMenuItem.font")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setFont(resourceMap.getFont("helpMenu.font")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setFont(resourceMap.getFont("aboutMenuItem.font")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 590, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jFileChooserAbrirGuardar.setFont(resourceMap.getFont("jFileChooserAbrirGuardar.font")); // NOI18N
        jFileChooserAbrirGuardar.setName("jFileChooserAbrirGuardar"); // NOI18N

        jFileChooserExpImagen.setName("jFileChooserExpImagen"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jScrollPane_grafo_actualMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jScrollPane_grafo_actualMouseWheelMoved
        if(evt.getWheelRotation() < 0)
            aumentarEscala(evt.getPoint());
        else
            reducirEscala(evt.getPoint());
    }//GEN-LAST:event_jScrollPane_grafo_actualMouseWheelMoved

    private void jTabbedPane1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseWheelMoved
        if(proyecto_actual != null){
            int contador = jTabbedPane1.getTabCount() - 1;
            int actual = jTabbedPane1.getSelectedIndex();
            if(evt.getWheelRotation() < 0){
                actual++;
                if(actual > contador)
                    actual = 0;
            }
            else{
                actual--;
                if(actual < 0)
                    actual = contador;
            }
            jTabbedPane1.setSelectedIndex(actual);
            if(jTabbedPane1.getSelectedIndex() != 0)
                desabilitarElementosGrafo();
            else
                habilitarElementosGrafo();
        }
    }//GEN-LAST:event_jTabbedPane1MouseWheelMoved

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        if(proyecto_actual != null){
            if(jTabbedPane1.getSelectedIndex() != 0)
                desabilitarElementosGrafo();
            else
                habilitarElementosGrafo();
        }
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JFileChooser jFileChooserAbrirGuardar;
    private javax.swing.JFileChooser jFileChooserExpImagen;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_actual;
    private javax.swing.JPanel jPanel_sub_actual;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane_grafo_actual;
    private javax.swing.JScrollPane jScrollPane_tabla;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable_actual;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private javax.swing.table.DefaultTableModel modelo_tabla_actual;

    // Variables de Proyecto
    private Proyecto proyecto_actual;

    // Variale de modificación de proyecto actual
    private boolean modificado;

    // Variables de JFrames.
    private JDialog aboutBox;
    private NuevoProyecto nuevo_proyecto;
    private NuevaActividad nueva_actividad;
    private BorrarActividad borrar_actividad;
    private DefinirSucesos definir_estados;
    private BorrarSuceso borrar_sucesos;
    private CaracteristicasAdicionales caracteristicas_ad;
    private EjecutarAlgoritmo ejecutar_algoritmo;
    private SalvarProyecto salvar_proyecto;

    // Variables de la grafica para el grafo.
    private Principal grafo_visual_actual;
    private boolean horizontal;

    // Variables de graficos estadisticos.
    private GraficoBarras grafico_barras;
    private GraficoGantt grafico_gantt;
    private Date fecha_com;
    private int unidad_tiempo;

    // Variable para exportación a formato gráfico. (Porque ExtensionFileFilter no tiene un metodo getExtension).
    private Hashtable<String,String> asoc_formatos_graficos;

}
