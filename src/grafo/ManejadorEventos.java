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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;

public class ManejadorEventos extends BasicMarqueeHandler{


    private Principal principal;
    private JGraph grafo;
    private Point2D start, current;
    private PortView port, firstPort;

    public ManejadorEventos(JGraph grafo, Principal principal){
        this.grafo = grafo;
        this.principal = principal;
    }

    public boolean isForceMarqueeEvent(MouseEvent e){
        if (e.isShiftDown())
            return false;
        if (SwingUtilities.isRightMouseButton(e))
            return true;
        port = getSourcePortAt(e.getPoint());
        if (port != null && grafo.isPortsVisible())
            return true;
        return super.isForceMarqueeEvent(e);
    }

    public void mousePressed(final MouseEvent e) {
        if(principal.getAreaGrafica().isEnabled()){
            if (SwingUtilities.isRightMouseButton(e)) {
                Object cell = grafo.getFirstCellForLocation(e.getX(), e.getY());
                JPopupMenu menu = principal.crearMenuEmergente(e.getPoint(), cell);
                menu.show(grafo, e.getX(), e.getY());
            }
            else{
                if (port != null && grafo.isPortsVisible()) {
                    start = grafo.toScreen(port.getLocation());
                    firstPort = port;
                }
                else{
                    super.mousePressed(e);
                }
            }
            if(SwingUtilities.isMiddleMouseButton(e)){
             
             }
         }
    }

    public void mouseDragged(MouseEvent e) {
        if (start != null) {
            Graphics g = grafo.getGraphics();
            PortView newPort = getTargetPortAt(e.getPoint());
            if (newPort == null || newPort != port) {
                paintConnector(Color.black, grafo.getBackground(), g);
                port = newPort;
                if (port != null)
                    current = grafo.toScreen(port.getLocation());
                else
                    current = grafo.snap(e.getPoint());
                paintConnector(grafo.getBackground(), Color.black, g);
            }
        }
        super.mouseDragged(e);
    }

    public PortView getSourcePortAt(Point2D point) {
        grafo.setJumpToDefaultPort(false);
        PortView result;
        try {
                result = grafo.getPortViewAt(point.getX(), point.getY());
        } finally {
                grafo.setJumpToDefaultPort(true);
        }
        return result;
    }

    protected PortView getTargetPortAt(Point2D point){
        return grafo.getPortViewAt(point.getX(), point.getY());
    }

    public void mouseReleased(MouseEvent e){
        if (e != null && port != null && firstPort != null && firstPort != port) {
            principal.getPrincipal().nuevaActividad();
            e.consume();
        }
        else
            grafo.repaint();
        firstPort = port = null;
        start = current = null;
        super.mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e) {
        if (e != null && getSourcePortAt(e.getPoint()) != null && grafo.isPortsVisible()) {
                grafo.setCursor(new Cursor(Cursor.HAND_CURSOR));
                e.consume();
        } else
                super.mouseMoved(e);
    }

    protected void paintConnector(Color fg, Color bg, Graphics g) {
        g.setColor(fg);
        g.setXORMode(bg);
        paintPort(grafo.getGraphics());
        if (firstPort != null && start != null && current != null)
            g.drawLine((int) start.getX(), (int) start.getY(),(int) current.getX(), (int) current.getY());
    }

    protected void paintPort(Graphics g) {
        if (port != null) {
            boolean o = (GraphConstants.getOffset(port.getAllAttributes()) != null);
            Rectangle2D r = (o) ? port.getBounds() : port.getParentView().getBounds();
            r = grafo.toScreen((Rectangle2D) r.clone());
            r.setFrame(r.getX() - 3, r.getY() - 3, r.getWidth() + 6, r.getHeight() + 6);
            grafo.getUI().paintCell(g, port, r, true);
        }
    }

}

