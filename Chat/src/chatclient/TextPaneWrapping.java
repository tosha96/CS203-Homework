import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
 
public class TextPaneWrapping extends JFrame {
 
    private static JPanel noWrapPanel;
    private static JScrollPane scrollPane;
    private static JTextPane textPane;
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TextPaneWrapping scrollPaneFrame = new TextPaneWrapping();
        textPane = new JTextPane();
        noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.setPreferredSize(new Dimension(200,200));
        noWrapPanel.add(textPane);
        scrollPane = new JScrollPane(noWrapPanel);
        scrollPane.setPreferredSize(new Dimension(200,200));
        scrollPane.setViewportView(textPane); // creates a wrapped scroll pane using the text pane as a viewport.
         
        scrollPaneFrame.add(scrollPane);
        scrollPaneFrame.setPreferredSize(new Dimension(200,200));
        scrollPaneFrame.pack();
        scrollPaneFrame.setVisible(true);
    }
}