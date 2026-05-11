import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Properties;
import java.util.LinkedList;


// ============================================================
// SEZIONE 1 — COMPONENTE CUSTOM: ToggleSwitch
// ============================================================
// Poiché Swing non ha uno switch nativo, lo creiamo da zero.
// Estende JComponent e disegna uno switch animato via paintComponent().
// Espone lo stesso contratto di JToggleButton: isSelected(), setSelected(),
// setEnabled(), addActionListener() — così il resto del codice non cambia.

class ToggleSwitch extends JComponent {

    private boolean selected = false;
    private float thumbPosition = 0f;   // 0.0 = sinistra (OFF), 1.0 = destra (ON)
    private Timer animationTimer;

    private static final int WIDTH  = 56;
    private static final int HEIGHT = 28;
    private static final Color COLOR_ON       = new Color(50, 180, 80);
    private static final Color COLOR_OFF      = new Color(180, 180, 180);
    private static final Color COLOR_DISABLED = new Color(210, 210, 210);
    private static final Color COLOR_THUMB    = Color.WHITE;

    public ToggleSwitch() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isEnabled()) return;
                setSelected(!selected);
                fireActionPerformed();
            }
        });
    }

    private void animateThumb(boolean toOn) {
        if (animationTimer != null) animationTimer.stop();
        animationTimer = new Timer(16, null);
        animationTimer.addActionListener(e -> {
            float target = toOn ? 1f : 0f;
            float delta  = toOn ? 0.1f : -0.1f;
            thumbPosition += delta;
            if ( toOn && thumbPosition >= target) { thumbPosition = target; animationTimer.stop(); }
            if (!toOn && thumbPosition <= target) { thumbPosition = target; animationTimer.stop(); }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Traccia
        Color trackColor = !isEnabled() ? COLOR_DISABLED : (selected ? COLOR_ON : COLOR_OFF);
        g2.setColor(trackColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, WIDTH, HEIGHT, HEIGHT, HEIGHT));

        // Pollice
        int thumbDiameter = HEIGHT - 4;
        float maxTravel   = WIDTH - thumbDiameter - 4;
        float thumbX      = 2 + thumbPosition * maxTravel;
        g2.setColor(COLOR_THUMB);
        g2.fill(new Ellipse2D.Float(thumbX, 2, thumbDiameter, thumbDiameter));

        // Etichetta ON / OFF
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        String label = selected ? "ON" : "OFF";
        FontMetrics fm = g2.getFontMetrics();
        int textX = selected
            ? (int)(thumbX / 2 - fm.stringWidth(label) / 2f)
            : (int)(thumbX + thumbDiameter + (WIDTH - thumbX - thumbDiameter) / 2f - fm.stringWidth(label) / 2f);
        g2.drawString(label, textX, HEIGHT / 2 + fm.getAscent() / 2 - 1);

        g2.dispose();
    }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) {
        this.selected = selected;
        animateThumb(selected);
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    private void fireActionPerformed() {
        for (ActionListener l : listenerList.getListeners(ActionListener.class))
            l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "toggle"));
    }
}


// ============================================================
// SEZIONE 2 — FINESTRA PRINCIPALE: Helldivers2
// ============================================================

public class Helldivers2 extends JFrame {

    private ToggleSwitch swCompagni;
    private ToggleSwitch swStratagemmi;
    private ToggleSwitch swEsplosioni;

    private static final String CONFIG_FILE = "helldivers2_state.properties";


    // ============================================================
    // SEZIONE 3 — LOGICA FIFO (massimo 2 switch attivi)
    // ============================================================

    private final LinkedList<ToggleSwitch> activeQueue = new LinkedList<>();

    private void onSwitchToggled(ToggleSwitch sw) {
        if (sw.isSelected()) {
            activeQueue.remove(sw); 
            activeQueue.add(sw);

            if (activeQueue.size() > 2) {
                ToggleSwitch oldest = activeQueue.poll();
                oldest.setSelected(false);
            }
        } else {
            activeQueue.remove(sw);
        }
    }


    public Helldivers2() {
        setTitle("Helldivers 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        buildUI();
        loadState();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAndPrintState();
            }
        });
    }


    // ============================================================
    // SEZIONE 3 — COSTRUZIONE UI
    // ============================================================

    private void buildUI() {
        JLabel titleLabel = new JLabel("Helldivers 2", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel switchPanel = new JPanel();
        switchPanel.setLayout(new BoxLayout(switchPanel, BoxLayout.Y_AXIS));
        switchPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 20, 60));

        swCompagni    = new ToggleSwitch();
        swStratagemmi = new ToggleSwitch();
        swEsplosioni  = new ToggleSwitch();

        swCompagni.addActionListener(e    -> onSwitchToggled(swCompagni));
        swStratagemmi.addActionListener(e -> onSwitchToggled(swStratagemmi));
        swEsplosioni.addActionListener(e  -> onSwitchToggled(swEsplosioni));

        switchPanel.add(makeSwitchRow("Compagni",    swCompagni));
        switchPanel.add(Box.createVerticalStrut(12));
        switchPanel.add(makeSwitchRow("Stratagemmi", swStratagemmi));
        switchPanel.add(Box.createVerticalStrut(12));
        switchPanel.add(makeSwitchRow("Esplosioni",  swEsplosioni));

        add(switchPanel, BorderLayout.CENTER);
    }

    private JPanel makeSwitchRow(String label, ToggleSwitch sw) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 15));
        row.add(lbl, BorderLayout.WEST);
        row.add(sw,  BorderLayout.EAST);
        return row;
    }


    // ============================================================
    // SEZIONE 4 — SALVATAGGIO STATO
    // ============================================================

    private void saveAndPrintState() {
        Properties props = new Properties();
        props.setProperty("compagni",    String.valueOf(swCompagni.isSelected()));
        props.setProperty("stratagemmi", String.valueOf(swStratagemmi.isSelected()));
        props.setProperty("esplosioni",  String.valueOf(swEsplosioni.isSelected()));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Helldivers 2 - Stato degli switch");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("=== Stato salvato ===");
        System.out.println("Compagni:    " + (swCompagni.isSelected()    ? "ON" : "off"));
        System.out.println("Stratagemmi: " + (swStratagemmi.isSelected() ? "ON" : "off"));
        System.out.println("Esplosioni:  " + (swEsplosioni.isSelected()  ? "ON" : "off"));
    }


    // ============================================================
    // SEZIONE 5 — CARICAMENTO STATO
    // ============================================================
    private void loadState() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            swCompagni.setSelected(   Boolean.parseBoolean(props.getProperty("compagni",    "false")));
            swStratagemmi.setSelected(Boolean.parseBoolean(props.getProperty("stratagemmi", "false")));
            swEsplosioni.setSelected( Boolean.parseBoolean(props.getProperty("esplosioni",  "false")));

            
            activeQueue.clear();
            if (swCompagni.isSelected())    activeQueue.add(swCompagni);
            if (swStratagemmi.isSelected()) activeQueue.add(swStratagemmi);
            if (swEsplosioni.isSelected())  activeQueue.add(swEsplosioni);

            System.out.println("=== Stato caricato ===");
            System.out.println("Compagni:    " + (swCompagni.isSelected()    ? "ON" : "off"));
            System.out.println("Stratagemmi: " + (swStratagemmi.isSelected() ? "ON" : "off"));
            System.out.println("Esplosioni:  " + (swEsplosioni.isSelected()  ? "ON" : "off"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    // ============================================================
    // ENTRY POINT
    // ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Helldivers2().setVisible(true));
    }
}