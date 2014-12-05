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
import org.jgraph.JGraph;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

public class Grafico extends JGraph{

        public Grafico(GraphModel model) {
            this(model, null);
        }
        
        public Grafico(GraphModel model, GraphLayoutCache cache) {
            super(model, cache);
            setBackground(Color.WHITE);
            setGridEnabled(true);
            setGridColor(Color.darkGray);
            setGridSize(8);
            setTolerance(8);
            setInvokesStopCellEditing(true);
            setCloneable(true);
            setJumpToDefaultPort(true);
            setAutoResizeGraph(true);
            setCloneable(false);
            setInvokesStopCellEditing(true);
            setPortsVisible(!isPortsVisible());
        }

}

