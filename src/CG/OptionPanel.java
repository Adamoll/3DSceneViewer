package CG;

import static CG.PerspectivePanel.perspectiveProjectionMatrix;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OptionPanel extends JPanel implements ActionListener, ChangeListener{
    JButton load;
    JButton save;
    JLabel fovLabel;
    JLabel twistAngleLabel;
    JSlider fov;
    JSlider twistAngle;
    GridBagLayout mainLayout;
    GridBagConstraints mainGBC;
    JToggleButton toggleWireFrame;
    JToggleButton toggleSurfaces;
    
    FileReader fromFile;
    BufferedReader buff;

    String path;
    String objectName;
    String cameraName;
    static boolean objectLoaded;
    public OptionPanel() {
        mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);
        load = new JButton("Load");
        objectLoaded = false;
        save = new JButton("Save");
        fov = new JSlider(JSlider.VERTICAL, 1, 90, 70);
        twistAngle = new JSlider(JSlider.VERTICAL, (-180), 180, 0);
        load.addActionListener(this);
        save.addActionListener(this);
        fov.addChangeListener(this);
        twistAngle.addChangeListener(this);
        fov.setMajorTickSpacing(20);
        fov.setMinorTickSpacing(4);
        fov.setPaintLabels(true);
        fov.setPaintTicks(true);
        twistAngle.setMajorTickSpacing(60);
        twistAngle.setMinorTickSpacing(10);
        twistAngle.setPaintLabels(true);
        twistAngle.setPaintTicks(true);
        fovLabel = new JLabel("Field of view: " + fov.getValue());
        twistAngleLabel = new JLabel("Twist angle: 00" + twistAngle.getValue());
        PerspectivePanel.fovValue = fov.getValue();
        PerspectivePanel.twistAngleValue = twistAngle.getValue();
        
        toggleWireFrame = new JToggleButton("Show wireframe");
        toggleSurfaces = new JToggleButton("Show surfaces");

        ActionListener actionListenerWireFrame = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                Frame.showWireFrame = abstractButton.getModel().isSelected();
            }
        };
        ActionListener actionListenerSurfaces = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                Frame.showSurfaces = abstractButton.getModel().isSelected();
            }
        };
        toggleWireFrame.addActionListener(actionListenerWireFrame);
        toggleSurfaces.addActionListener(actionListenerSurfaces);
        toggleSurfaces.setSelected(true);


        mainGBC = new GridBagConstraints();
        mainGBC.anchor = GridBagConstraints.NORTH;
        mainGBC.insets = new Insets(0, 0, 0, 0);
        mainGBC.gridx = 0;
        mainGBC.gridy = 1;
        this.add(load, mainGBC);
        mainGBC.gridy ++;
        this.add(save, mainGBC);
        mainGBC.insets = new Insets(15, 0, 0, 0);
        mainGBC.gridy++;
        this.add(toggleWireFrame, mainGBC);
        mainGBC.insets = new Insets(0, 0, 0, 0);
        mainGBC.gridy++;
        this.add(toggleSurfaces, mainGBC);
        mainGBC.anchor = GridBagConstraints.CENTER;
        mainGBC.gridwidth = 1;
        mainGBC.insets = new Insets(10, 0, 0, 0);
        mainGBC.gridy++;
        this.add(fovLabel, mainGBC);
        mainGBC.insets = new Insets(0, 0, 0, 0);
        mainGBC.gridy++;
        this.add(fov, mainGBC);
        mainGBC.gridy++;
        mainGBC.insets = new Insets(20, 0, 0, 0);
        mainGBC.anchor = GridBagConstraints.SOUTH;
        this.add(twistAngleLabel, mainGBC);
        mainGBC.gridy++;
        mainGBC.insets = new Insets(0, 0, 0, 0);
        this.add(twistAngle, mainGBC);

        
        path = "scene files\\";
        cameraName = "camera.txt";
    }
    
    public void loadObject() throws FileNotFoundException, IOException
    {
        fromFile = new FileReader(new File(path + objectName));
        buff = new BufferedReader(fromFile);
        
        String numberOfVertexesString = buff.readLine();
        while(numberOfVertexesString.equals("") || numberOfVertexesString.contains("//"))
        {
            numberOfVertexesString = buff.readLine();
        }
        String[] number = numberOfVertexesString.split(" ");
        int numberOfVertexes = Integer.parseInt(number[1]);
        Frame.vertexes = new Vertex[numberOfVertexes];
        for(int i = 0; i < numberOfVertexes; i ++)
        {
            String vert = buff.readLine();
            String[] vertElements = vert.split(" ");
            double x = Double.parseDouble(vertElements[0]);
            double y = Double.parseDouble(vertElements[1]);
            double z = - Double.parseDouble(vertElements[2]);
            Frame.vertexes[i] = new Vertex(x, y, z);
        }
        
        String numberOfTrianglesString = buff.readLine();
        while(numberOfTrianglesString.equals("") || numberOfTrianglesString.contains("//"))
        {
            numberOfTrianglesString = buff.readLine();
        }
        number = numberOfTrianglesString.split(" ");
        int numberOfTriangles = Integer.parseInt(number[1]);
        Frame.triangles = new Triangle[numberOfTriangles];
        for(int i = 0; i < numberOfTriangles; i ++)
        {
            String trian = buff.readLine();
            String[] trianElements = trian.split(" ");
            int v1 = Integer.parseInt(trianElements[0]);
            int v2 = Integer.parseInt(trianElements[1]);
            int v3 = Integer.parseInt(trianElements[2]);
            Frame.triangles[i] = new Triangle(Frame.vertexes[v1], Frame.vertexes[v2], Frame.vertexes[v3]);
        }
        

        String line = buff.readLine();
        while(line.equals("") || line.contains("//"))
        {
            line = buff.readLine();
        }
        String[] trianglesSurface = buff.readLine().split(" ");
        for(int i = 0 ; i < trianglesSurface.length; i++)
        {
            Frame.triangles[i].setSurfaceType(Integer.parseInt(trianglesSurface[i]));
        }
        
        String numberOfSurfacesString = buff.readLine();
        while(numberOfSurfacesString.equals("") || numberOfSurfacesString.contains("//"))
        {
            numberOfSurfacesString = buff.readLine();
        }
        int numberOfSurfaces = Integer.parseInt(numberOfSurfacesString);
        Frame.surfaces = new SurfaceModel[numberOfSurfaces];
        for(int i = 0; i < numberOfSurfaces; i ++)
        {
            String lines = buff.readLine();
            String[] surfElements = lines.split(" ");
            double r = Double.parseDouble(surfElements[1]);
            double g = Double.parseDouble(surfElements[2]);
            double b = Double.parseDouble(surfElements[3]);
            //TODO add Phone reflection model elements
//            lines = buff.readLine();
//            surfElements = lines.split(" ");
//            double kdr = Double.parseDouble(surfElements[1]);
//            double kdg = Double.parseDouble(surfElements[2]);
//            double kdb = Double.parseDouble(surfElements[3]);
//
//            lines = buff.readLine();
//            surfElements = lines.split(" ");
//            double ksr = Double.parseDouble(surfElements[1]);
//            double ksg = Double.parseDouble(surfElements[2]);
//            double ksb = Double.parseDouble(surfElements[3]);
//
//            lines = buff.readLine();
//            surfElements = lines.split(" ");
//            double kar = Double.parseDouble(surfElements[1]);
//            double kag = Double.parseDouble(surfElements[2]);
//            double kab = Double.parseDouble(surfElements[3]);
//
//            lines = buff.readLine();
//            surfElements = lines.split(" ");
//            double gp = Double.parseDouble(surfElements[1]);

            Frame.surfaces[i] = new SurfaceModel(r, g, b, new Vertex(0, 0, 0),
                    new Vertex(0, 0, 0), new Vertex(0, 0, 0), 0);
        }
        //TODO read light sources
//        String illumination = buff.readLine();
//        while (illumination.equals("") || illumination.contains("//") || illumination.contains("/"))
//        {
//            illumination = buff.readLine();
//        }
//        String[] illuminationElements = illumination.split(" ");
//        double x = Double.parseDouble(illuminationElements[0]);
//        double y = Double.parseDouble(illuminationElements[1]);
//        double z = Double.parseDouble(illuminationElements[2]);
//        double R = Double.parseDouble(illuminationElements[3]);
//        double G = Double.parseDouble(illuminationElements[4]);
//        double B = Double.parseDouble(illuminationElements[5]);
//        Frame.lighting = new Illumination(x, y, z, R, G, B);
        buff.close();
        ViewPanel.ort1.repaint();
        ViewPanel.ort2.repaint();
        ViewPanel.ort3.repaint();
        ViewPanel.persp.repaint();
        
    }

    
    public void loadCamera() throws FileNotFoundException, IOException
    {
        fromFile = new FileReader(path + cameraName);
        buff = new BufferedReader(fromFile);
        String coordinates = buff.readLine();
        String centerCoordinates = buff.readLine();
        String fov = buff.readLine();
        String twistAngle = buff.readLine();
 
        String[] split1 = coordinates.split(" ");
        String[] split2 = centerCoordinates.split(" ");
        String[] split3 = fov.split(" ");
        String[] split4 = twistAngle.split(" ");
        double xPos = Double.parseDouble(split1[1]);
        double yPos = Double.parseDouble(split1[2]);
        double zPos = Double.parseDouble(split1[3]);

        double xDir = Double.parseDouble(split2[1]);
        double yDir = Double.parseDouble(split2[2]);
        double zDir = Double.parseDouble(split2[3]);
        double fovValue = Double.parseDouble(split3[1]);
        double tAngle = Double.parseDouble(split4[1]);
        Frame.camera = new Camera(xPos, yPos, zPos, xDir, yDir, zDir, fovValue, tAngle);
    }
    
    public void saveCamera() throws FileNotFoundException, UnsupportedEncodingException
    {
        if (OptionPanel.objectLoaded) {
            try (PrintWriter writer = new PrintWriter(new File(path + "camera.txt"), "utf-8")) {
                writer.println("pos " + Frame.camera.eye.xTT + " " + Frame.camera.eye.yTT + " " + Frame.camera.eye.zTT);
                writer.println("lookAt " + Frame.camera.lookAt.xTT + " " + Frame.camera.lookAt.yTT + " " + Frame.camera.lookAt.zTT);
                writer.println("fov " + Frame.camera.fov);
                writer.println("rotation " + Frame.camera.twistAngle);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == load)
        {
            try {
                Frame.fileDialog.setVisible(true);
                objectName = Frame.fileDialog.getFile();
                path = Frame.fileDialog.getDirectory();
                objectLoaded = true;
                loadObject();
                loadCamera();
                ViewPanel.ort1.setPanelCoordinates();
                ViewPanel.ort2.setPanelCoordinates();
                ViewPanel.ort3.setPanelCoordinates();
                ViewPanel.persp.camera = Frame.camera;
                ViewPanel.persp.triangles = Frame.triangles;
                ViewPanel.ort1.triangles = Frame.triangles;
                ViewPanel.ort1.camera = Frame.camera;
                ViewPanel.persp.setPerspectiveMatrix();
                ViewPanel.persp.surfaces = Frame.surfaces;
                initiateVertexesValue();
                fov.setValue((int)Frame.camera.fov);
                twistAngle.setValue((int)Frame.camera.twistAngle);
                
            } catch (IOException ex) {
                Logger.getLogger(OptionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(e.getSource() == save)
        {
            try {
                saveCamera();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(OptionPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(OptionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void initiateVertexesValue() {
        for (int i = 0; i < Frame.vertexes.length; i++) {
            Frame.vertexes[i].multiplyByMatrix(ViewPanel.ort1.toScreenMatrix);
        }
    }
    
        @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider) e.getSource();
        if (slider == fov) {
            PerspectivePanel.fovValue = slider.getValue();
            String text = slider.getValue() < 10 ? "Field of view: 0" + slider.getValue(): "Field of view: " + slider.getValue();
            fovLabel.setText(text);
            Frame.camera.updateFov(slider.getValue());
            perspectiveProjectionMatrix[0][0] = ViewPanel.persp.cot(Frame.camera.fov / 2) / ViewPanel.persp.aspect;
            perspectiveProjectionMatrix[1][1] = ViewPanel.persp.cot(Frame.camera.fov / 2);
        } else if (slider == twistAngle) {
            Frame.camera.updateTwistAngle(slider.getValue());
            ViewPanel.persp.transformCamera();
            ViewPanel.persp.repaint();
            String text = "";
            if(Math.abs(slider.getValue()) < 10)
                text = slider.getValue() >= 0 ? "Twist angle: 00" + slider.getValue() : "Twist angle:-00" + Math.abs(slider.getValue());
            else if(Math.abs(slider.getValue()) > 9 && Math.abs(slider.getValue()) < 99)
            {
                text = slider.getValue() > 0 ? "Twist angle: 0" + slider.getValue() : "Twist angle:-0" + Math.abs(slider.getValue());
            }
            else if(Math.abs(slider.getValue()) > 99)
            {
                text = slider.getValue() > 0 ? "Twist angle: " + slider.getValue() : "Twist angle:-" + Math.abs(slider.getValue());
            }
            twistAngleLabel.setText(text);
        }

    }

}
