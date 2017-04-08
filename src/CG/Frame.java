package CG;

import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Frame extends JFrame{
    JFrame frame;
    static Vertex[] vertexes;
    static Triangle[] triangles;
    static SurfaceModel[] surfaces;
    static Illumination lighting;
    static Camera camera;
    static boolean showWireFrame;
    static boolean showSurfaces;
    static int backgroundColor;
    public static FileDialog fileDialog;


    Frame()
    {
        frame = new JFrame("Okno");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 600);
        FramePanel framepanel = new FramePanel();
        frame.setContentPane(framepanel);
        Dimension minimal = new Dimension(750, 620);
        frame.setMinimumSize(minimal);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        fileDialog = new FileDialog(this, "Choose scene", FileDialog.LOAD);

        showWireFrame = false;
        showSurfaces = true;
        backgroundColor = 13158600;
        
    }
    public static void main(String[] args) {
        Frame f = new Frame();
    }
    
    public static double[][] multiplyMatrices(double[][] tab1, double[][] tab2) {
        double[][] macierzPomnozona = new double[tab1.length][tab2[0].length];
        if (tab1[0].length == tab2.length) {
            for (int i = 0; i < tab1.length; i++) {//ilosc wierszy tab1
                for (int j = 0; j < tab2[0].length; j++) { //ilosc kolumn tab2
                    double temp = 0;
                    for (int w = 0; w < tab2.length; w++) { //ilosc wierszy tab2
                        temp += tab1[i][w] * tab2[w][j];
                    }
                    macierzPomnozona[i][j] = temp;
                }
            }
        } else {
            throw new RuntimeException("Podane tablice mają niewłasciwe wymiary");
        }
        return macierzPomnozona;
    }
    
    
    
    public static double[][] invert(double a[][]) {

        int n = a.length;
        double x[][] = new double[n][n];
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i = 0; i < n; ++i) {
            b[i][i] = 1;
        }
        // Transform the matrix into an upper triangle
        gaussian(a, index);
        // Update the matrix b[i][j] with the ratios stored
        for (int i = 0; i < n - 1; ++i) {
            for (int j = i + 1; j < n; ++j) {
                for (int k = 0; k < n; ++k) {
                    b[index[j]][k]
                            -= a[index[j]][i] * b[index[i]][k];
                }
            }
        }

        // Perform backward substitutions
        for (int i = 0; i < n; ++i) {

            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) {
                    x[j][i] -= a[index[j]][k] * x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }

// Method to carry out the partial-pivoting Gaussian
// elimination.  Here index[] stores pivoting order.
    public static void gaussian(double a[][], int index[]) {
        int n = index.length;
        double c[] = new double[n];

        // Initialize the index
        for (int i = 0; i < n; ++i) {
            index[i] = i;
        }

        // Find the rescaling factors, one from each row
        for (int i = 0; i < n; ++i) {
            double c1 = 0;
            for (int j = 0; j < n; ++j) {
                double c0 = Math.abs(a[i][j]);

                if (c0 > c1) {
                    c1 = c0;
                }
            }
            c[i] = c1;
        }
        // Search the pivoting element from each column
        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            double pi1 = 0;

            for (int i = j; i < n; ++i) {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }

            // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                double pj = a[index[i]][j] / a[index[j]][j];
                // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;
                // Modify other elements accordingly
                for (int l = j + 1; l < n; ++l) {
                    a[index[i]][l] -= pj * a[index[j]][l];
                }
            }
        }
    }
    
    public static double[][] copyMatrix(double[][] m)
    {
        double[][] temp = new double[m.length][];
        for(int i = 0 ; i < m.length; i ++)
        {
            temp[i] = new double[m[i].length];
            for(int j = 0; j < m[i].length; j++)
            {
                temp[i][j] = m[i][j];
            }
        }
        return temp;
    }
    
    
    static int int2RGB(int R, int G, int B) {
        R = R & 0x000000FF;
        G = G & 0x000000FF;
        B = B & 0x000000FF;

        return (R << 16) + (G << 8) + B;
    }
    
    static public Vertex crossProduct(Vertex v1, Vertex v2) {
        Vertex result = new Vertex();
        result.x = v1.y * v2.z - v1.z * v2.y;
        result.y = v1.z * v2.x - v1.x * v2.z;
        result.z = v1.x * v2.y + v1.y * v2.x;
        return result;
    }

    static public Vertex norm(Vertex v) {
        Vertex result = new Vertex();
        double length = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        result.x = v.x / length;
        result.y = v.y / length;
        result.z = v.z / length;
        return result;
    }
    
    static double dot(Vertex v1, Vertex v2){
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        
    }
    
}

class FramePanel extends JPanel
{
    GridBagLayout mainLayout;
    GridBagConstraints gbc;
    ViewPanel viewPanel;
    OptionPanel optionPanel;
    public FramePanel() {
        viewPanel = new ViewPanel();
        optionPanel = new OptionPanel();
        
        mainLayout = new GridBagLayout();
        setLayout(mainLayout);
        gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.95;
        gbc.weighty = 1;
        add(viewPanel, gbc);
        gbc.gridx ++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0.05;
        add(optionPanel, gbc);
        
    }
}
    
class ViewPanel extends JPanel {

    GridBagLayout mainLayout;
    GridBagConstraints gbc;
    static OrthogonalPanel ort1;
    static OrthogonalPanel ort2;
    static OrthogonalPanel ort3;
    static PerspectivePanel  persp;

    ViewPanel() {
        ort1 = new OrthogonalPanel("xy");
        ort1.setBackground(new Color(200, 200, 200));
        ort2 = new OrthogonalPanel("yz");
        ort2.setBackground(new Color(200, 200, 200));
        ort3 = new OrthogonalPanel("xz");
        ort3.setBackground(new Color(200, 200, 200));
        persp = new PerspectivePanel();
        persp.setBackground(new Color(200, 200, 200));

        mainLayout = new GridBagLayout();
        setLayout(mainLayout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(ort1, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        add(ort2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(ort3, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(persp, gbc);

    }
    
    

}
