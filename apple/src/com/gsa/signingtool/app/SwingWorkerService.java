/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsa.signingtool.app;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SwingWorkerService {
  private static JProgressBar PROGRESS_BAR;
  private static JLabel OUTPUT_LABEL;
  private static JFrame createGUI(){
    JFrame testFrame = new JFrame( "TestFrame" );

    PROGRESS_BAR = new JProgressBar(  );
    PROGRESS_BAR.setMinimum( 0 );
    PROGRESS_BAR.setMaximum( 100 );

    OUTPUT_LABEL = new JLabel( "Processing" );

    testFrame.getContentPane().add( PROGRESS_BAR, BorderLayout.CENTER );
    testFrame.getContentPane().add( OUTPUT_LABEL, BorderLayout.SOUTH );

    //add a checkbox as well to proof the UI is still responsive
    testFrame.getContentPane().add( 
            new JCheckBox( "Click me to proof UI is responsive" ), BorderLayout.NORTH );



    testFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    return testFrame;
  }

  public static void main( String[] args ) throws InvocationTargetException, InterruptedException {
    EventQueue.invokeAndWait( new Runnable() {
      @Override
      public void run() {
        JFrame frame = createGUI();

        frame.pack();
        frame.setVisible( true );
      }
    } );
    //start the SwingWorker outside the EDT
    MySwingWorker worker = new MySwingWorker( PROGRESS_BAR );
    worker.execute();
  }
  private static class MySwingWorker extends SwingWorker<String, Double>{
    private final JProgressBar fProgressBar;
    private MySwingWorker( JProgressBar aProgressBar ) {
      fProgressBar = aProgressBar;
    }

    @Override
    protected String doInBackground() throws Exception {
      int maxNumber = 10;
      for( int i = 0; i < maxNumber; i++ ){
        Thread.sleep( 2000 );//simulate long running process
        double factor = ((double)(i+1) / maxNumber);
        System.out.println("Intermediate results ready");
        publish( factor );//publish the progress
      }
      return "Finished";
    }

    @Override
    protected void process( List<Double> aDoubles ) {
      //update the percentage of the progress bar that is done
      int amount = fProgressBar.getMaximum() - fProgressBar.getMinimum();
      fProgressBar.setValue( ( int ) (fProgressBar.getMinimum() 
              + ( amount * aDoubles.get( aDoubles.size() - 1 ))) );
    }

  }
}