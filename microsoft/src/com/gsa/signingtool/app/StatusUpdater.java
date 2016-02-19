package com.gsa.signingtool.app;

import javax.swing.SwingWorker;
import static com.gsa.signingtool.app.Gui.status;

public class StatusUpdater extends SwingWorker<String, Void> {

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
