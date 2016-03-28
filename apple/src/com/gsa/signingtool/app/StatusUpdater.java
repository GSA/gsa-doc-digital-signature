/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsa.signingtool.app;

import javax.swing.SwingWorker;
import static com.gsa.signingtool.app.Gui.status;

/**
 *
 * @author jorpac01
 */
public class StatusUpdater extends SwingWorker<String, Void>{

    @Override
    protected String doInBackground() throws Exception {
        System.out.println(Thread.currentThread().getName());
        String update = "Applying Signature\n";
        status.append(update);
        return update;
    }

    @Override
    public void done() {
        try {
            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
