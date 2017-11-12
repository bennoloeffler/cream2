package bel.learn._22_threads.swing;

import bel.learn._06_swing.BigTablePanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static bel.learn._22_threads.ConcurrentUtils.sleep;

/**
 *
 */
public class SwingAndMultithreading {

    BigTablePanel btp;
    JProgressBar pb;
    JFrame frame2;

    public static void main(String[] args) {
        SwingAndMultithreading main = new SwingAndMultithreading();
        SwingUtilities.invokeLater(() -> main.createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame();
        frame.setTitle("los, klick den Button und die Checkbox. Oft. Öfter!");
        btp = new BigTablePanel();
        frame.setContentPane(btp);
        frame.setPreferredSize(new Dimension(1000, 800));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame2 = new JFrame("so weit sind wir schon...");
        pb = new JProgressBar(0,100);
        pb.setValue(0);
        pb.setStringPainted(true);
        //frame2.setPreferredSize(new Dimension(300, 60));
        frame2.setContentPane(pb);
        frame2.pack();
        frame2.setVisible(true);

        PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    pb.setValue(progress);
                }

            }
        };

        Task task = new Task();
        task.addPropertyChangeListener(pcl);
        task.execute();
    }

    class Task extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() throws Exception {

            for(int i = 0; i< 100; i++) {
                if(i % 7  == 0) {
                    btp.nextAction();
                    System.out.println("next action: " + i);
                }
                sleep(500);
                setProgress(Math.min(i, 100));
            }
            return null;
        }

        @Override
        protected void done() {
            //super.done();
            sleep(300);
            frame2.setVisible(false);
        }
    }
}
