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

import java.util.Iterator;
import java.util.Vector;
import javax.swing.JComboBox;
import org.jdesktop.application.Action;
import proyecto.Suceso;

public class BorrarSuceso extends javax.swing.JFrame {

    public BorrarSuceso(EditorDeProyectosView itv) {
        initComponents();
        this.itv = itv;
        llenar();
        if(jComboBox1.getItemCount() > 0)
            jButton2.setEnabled(true);
        else
            jButton2.setEnabled(false);
    }

    @Action
    public void borrar(){
        if(id_suceso != null){
            Suceso s_inicial = itv.getProyecto().getSucesoInicial();
            Suceso s_final = itv.getProyecto().getSucesoFinal();
            if(s_inicial != null && s_inicial.getIdentificador() == id_suceso){
                itv.getProyecto().establecerSucesoInicialNulo();
                itv.desabilitarElementosCantAct();
            }
            if(s_final != null && s_final.getIdentificador() == id_suceso){
                itv.getProyecto().establecerSucesoFinalNulo();
                itv.desabilitarElementosCantAct();
            }
            itv.getProyecto().removerSuceso(id_suceso);
            itv.setBanderaModificacion(true);
            id_suceso = null;
            llenar();
            if(itv.getProyecto().getSucesos() == null || itv.getProyecto().getSucesos().size() < 1)
                itv.desabilitarelementosCantSucesos();
        }
    }

    @Action
    public void aceptar(){
        EditorDeProyectosApp.getApplication().getMainFrame().setEnabled(true);
        dispose();
    }

    private void llenar(){
        jComboBox1.removeAllItems();
        Vector<Suceso> vec_s = itv.getProyecto().getSucesos();
        if(vec_s != null)
            for(Iterator<Suceso> it = vec_s.iterator(); it.hasNext();){
                Suceso s = it.next();
                jComboBox1.addItem(s.getIdentificador());
            }
        if(jComboBox1.getItemCount() > 0)
            jButton2.setEnabled(true);
        else
            jButton2.setEnabled(false);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(interfaz.EditorDeProyectosApp.class).getContext().getResourceMap(BorrarSuceso.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(resourceMap.getFont("jComboBox1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox1.setFont(resourceMap.getFont("jComboBox1.font")); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(interfaz.EditorDeProyectosApp.class).getContext().getActionMap(BorrarSuceso.class, this);
        jButton1.setAction(actionMap.get("aceptar")); // NOI18N
        jButton1.setFont(resourceMap.getFont("jComboBox1.font")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("borrar")); // NOI18N
        jButton2.setFont(resourceMap.getFont("jComboBox1.font")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        id_suceso = (Integer)cb.getSelectedItem();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        EditorDeProyectosApp.getApplication().getMainFrame().setEnabled(true);
        dispose();
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    private EditorDeProyectosView itv;
    private Integer id_suceso;
}
