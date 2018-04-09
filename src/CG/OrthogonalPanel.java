package CG;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

public class OrthogonalPanel extends JPanel implements ComponentListener, MouseListener, MouseMotionListener {
    double[][] orthogonalMatrix;
    int screenWidth;
    int screenHeight;
    int cdr; // camera drawing range
    String side;
    boolean posClicked;
    boolean eyeClicked;
    double[][] scaleToWindowSize;
    double[][] translate;
    double[][] translate2;
    double[][] reflexYAxis;
    double[][] scaleByOrthogonal;
    double[][] toScreenMatrix;
    double[][] invertedToScreenMatrix;
    double[][] zBufer;
    double[][] colorBuffer;
    double[][] scaleByHalf;
    double aspect;
    static Triangle[] triangles;
    static Camera camera;
    static Vertex Ptl, Ptr, Pbl, Pbr;

    Vertex ptk000, ptk100, ptk010, ptk001;
    Point currentMousePos;

    public OrthogonalPanel(String s) {
        this.addComponentListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        screenWidth = getWidth();
        screenHeight = getHeight();
        aspect = (double) screenWidth / (double) screenHeight;
        side = s;
        cdr = 4;
        ptk000 = new Vertex(0, 0, 0);
        ptk100 = new Vertex(1, 0, 0);
        ptk010 = new Vertex(0, 1, 0);
        ptk001 = new Vertex(0, 0, 1);

        translate = new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {1, 1, 1, 1}
        };

        scaleByHalf = new double[][]{
                {0.5, 0, 0, 0},
                {0, 0.5, 0, 0},
                {0, 0, 0.5, 0},
                {0, 0, 0, 1}
        };

        scaleToWindowSize = new double[][]{
                {(screenHeight), 0, 0, 0},
                {0, (screenHeight), 0, 0},
                {0, 0, (screenHeight), 0},
                {0, 0, 0, 1}
        };
        translate2 = new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {(screenWidth) / 4, (screenHeight) / 4, (screenHeight) / 4, 1}
        };

        reflexYAxis = new double[][]{
                {1, 0, 0, 0},
                {0, -1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
    }

    public void setPanelCoordinates() {
        double[] d = findCoordinates();
        double top = d[0];
        double bottom = d[1];
        double right = d[2];
        double left = d[3];
        double znear = d[4];
        double zfar = d[5];

        orthogonalMatrix = new double[][]{
                {2 / (right - left), 0, 0, 0},
                {0, 2 / (top - bottom), 0, 0},
                {0, 0, 2 / (zfar - znear), 0},
                {-(right + left) / (right - left), -(top + bottom) / (top - bottom), -(zfar + znear) / (zfar - znear), 1}
        };

        scaleByOrthogonal = new double[][]{
                {1 / orthogonalMatrix[0][0], 0, 0, 0},
                {0, 1 / orthogonalMatrix[1][1], 0, 0},
                {0, 0, 1 / orthogonalMatrix[2][2], 0, 0},
                {0, 0, 0, 1}
        };
        toScreenMatrix = new double[4][4];
        toScreenMatrix = Frame.multiplyMatrices(orthogonalMatrix, scaleByOrthogonal);
        toScreenMatrix = Frame.multiplyMatrices(toScreenMatrix, reflexYAxis);
        toScreenMatrix = Frame.multiplyMatrices(toScreenMatrix, translate);
        toScreenMatrix = Frame.multiplyMatrices(toScreenMatrix, scaleByHalf);
        toScreenMatrix = Frame.multiplyMatrices(toScreenMatrix, scaleToWindowSize);
        toScreenMatrix = Frame.multiplyMatrices(toScreenMatrix, translate2);
        double[][] matrixCopy = Frame.copyMatrix(toScreenMatrix);
        invertedToScreenMatrix = Frame.invert(matrixCopy);
    }

    public double[] findCoordinates() {
        double[] coord = new double[6];
        coord[0] = Double.MIN_VALUE; // top
        coord[1] = Double.MAX_VALUE; // bottom
        coord[2] = Double.MIN_VALUE; // right
        coord[3] = Double.MAX_VALUE; // left
        coord[4] = Double.MAX_VALUE; // near
        coord[5] = Double.MIN_VALUE; // far
        Vertex temp;
        for (int i = 0; i < Frame.vertexes.length; i++) {
            temp = Frame.vertexes[i];
            if (temp.xTT > coord[2]) {
                coord[2] = temp.xTT;
            }
            if (temp.xTT < coord[3]) {
                coord[3] = temp.xTT;
            }
            if (temp.yTT > coord[0]) {
                coord[0] = temp.yTT;
            }
            if (temp.yTT < coord[1]) {
                coord[1] = temp.yTT;
            }
            if (temp.zTT > coord[5]) {
                coord[5] = temp.zTT;
            }
            if (temp.zTT < coord[4]) {
                coord[4] = temp.zTT;
            }
        }
        ViewPanel.persp.minZ = coord[4];
        ViewPanel.persp.maxZ = coord[5];
        return coord;
    }

    public double[] computeBoundingBox(Triangle t) {
        double[] box = new double[6];
        box[0] = Double.MAX_VALUE; // minx
        box[1] = Double.MAX_VALUE; // miny
        box[2] = Double.MIN_VALUE; // maxx
        box[3] = Double.MIN_VALUE; // maxy
        box[4] = Double.MAX_VALUE; // minz
        box[5] = Double.MIN_VALUE; // maxz
        Vertex[] v = {t.v1, t.v2, t.v3};
        for (Vertex vert : v) {
            if (vert.x < box[0]) {
                box[0] = vert.x;
            }
            if (vert.x > box[2]) {
                box[2] = vert.x;
            }
            if (vert.y < box[1]) {
                box[1] = vert.y;
            }
            if (vert.y > box[3]) {
                box[3] = vert.y;
            }
            if (vert.z < box[4]) {
                box[4] = vert.z;
            }
            if (vert.z > box[5]) {
                box[5] = vert.z;
            }
        }
        return box;
    }

    public double edgeFunctionXY(Vertex a, Vertex b, Vertex c) {
        return (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
    }

    public double edgeFunctionYZ(Vertex a, Vertex b, Vertex c) {
        return (c.z - a.z) * (b.y - a.y) - (c.y - a.y) * (b.z - a.z);
    }

    public double edgeFunctionXZ(Vertex a, Vertex b, Vertex c) {
        return (c.x - a.x) * (b.z - a.z) - (c.z - a.z) * (b.x - a.x);
    }

    public double computeDepth(Vertex v0, Vertex v1, Vertex v2, double w0, double w1, double w2) {
        return 1 / (w0 * v0.zT + w1 * v1.zT + w2 * v2.zT);
    }

    public double computeDepth2(Vertex v0, Vertex v1, Vertex v2, double w0, double w1, double w2) {
        return (w0 * v0.xT + w1 * v1.xT + w2 * v2.xT);
    }

    public double computeDepth3(Vertex v0, Vertex v1, Vertex v2, double w0, double w1, double w2) {
        return 1 / (w0 * v0.yT + w1 * v1.yT + w2 * v2.yT);
    }

    void initializeBuffers(double[][] zb, double[][] cb) {
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                zb[i][j] = Double.MAX_VALUE;
                cb[i][j] = 13158600;
            }
        }
    }

    public double length(Camera c) {
        return Math.sqrt((c.lookAt.x - c.eye.x) * (c.lookAt.x - c.eye.x)
                + (c.lookAt.y - c.eye.y) * (c.lookAt.y - c.eye.y) + (c.lookAt.z - c.eye.z) * (c.lookAt.z - c.eye.z));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (OptionPanel.objectLoaded) {
            for (int i = 0; i < Frame.vertexes.length; i++) {
                Frame.vertexes[i].x = Frame.vertexes[i].xTT;
                Frame.vertexes[i].y = Frame.vertexes[i].yTT;
                Frame.vertexes[i].z = Frame.vertexes[i].zTT;
                Frame.vertexes[i].multiplyByMatrix(toScreenMatrix);
            }
            Frame.camera.resetCoords();

            Vertex upVec = new Vertex(ViewPanel.persp.up);
            Vertex rightVec = new Vertex(ViewPanel.persp.right);
            double cameraLength = length(camera);
            double height = 2 * Math.tan(Math.toRadians(camera.fov / 2)) * cameraLength;
            double width = height * aspect;
            upVec.multiplyByScalar(height / 2);
            rightVec.multiplyByScalar(width / 2);
            Ptl = new Vertex(camera.lookAt.x + upVec.x - rightVec.x,
                    camera.lookAt.y + upVec.y - rightVec.y, camera.lookAt.z + upVec.z - rightVec.z);
            Ptr = new Vertex(camera.lookAt.x + upVec.x + rightVec.x,
                    camera.lookAt.y + upVec.y + rightVec.y, camera.lookAt.z + upVec.z + rightVec.z);
            Pbl = new Vertex(camera.lookAt.x - upVec.x - rightVec.x,
                    camera.lookAt.y - upVec.y - rightVec.y, camera.lookAt.z - upVec.z - rightVec.z);
            Pbr = new Vertex(camera.lookAt.x - upVec.x + rightVec.x,
                    camera.lookAt.y - upVec.y + rightVec.y, camera.lookAt.z - upVec.z + rightVec.z);

            Ptl.multiplyByMatrix(toScreenMatrix);
            Ptr.multiplyByMatrix(toScreenMatrix);
            Pbl.multiplyByMatrix(toScreenMatrix);
            Pbr.multiplyByMatrix(toScreenMatrix);

            Frame.camera.eye.multiplyByMatrix(toScreenMatrix);
            Frame.camera.lookAt.multiplyByMatrix(toScreenMatrix);
            if (side.equals("xy")) {
                if (Frame.showSurfaces) {
                    zBufer = new double[screenHeight][screenWidth];
                    colorBuffer = new double[screenHeight][screenWidth];
                    initializeBuffers(zBufer, colorBuffer);
                    for (int i = 0; i < Frame.triangles.length; i++) {
                        double[] boundingBox = computeBoundingBox(triangles[i]);
                        double triangleArea = edgeFunctionXY(triangles[i].v1, triangles[i].v2, triangles[i].v3);
                        if (boundingBox[2] > 0 && boundingBox[3] > 0 && boundingBox[0] < screenWidth && boundingBox[1] < screenHeight) {
                            for (int a = (int) boundingBox[1]; a < boundingBox[3]; a++) {
                                for (int b = (int) boundingBox[0]; b < boundingBox[2]; b++) {
                                    if (a > 0 && a < screenHeight && b < screenWidth && b > 0) {
                                        Vertex pixel = new Vertex(b, a, 0);
                                        double w0 = edgeFunctionXY(triangles[i].v2, triangles[i].v3, pixel);
                                        double w1 = edgeFunctionXY(triangles[i].v3, triangles[i].v1, pixel);
                                        double w2 = edgeFunctionXY(triangles[i].v1, triangles[i].v2, pixel);

                                        if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                                            w0 /= triangleArea;
                                            w1 /= triangleArea;
                                            w2 /= triangleArea;
                                            double z = computeDepth(triangles[i].v1, triangles[i].v2, triangles[i].v3, w0, w1, w2);
                                            if (z < zBufer[a][b]) {
                                                zBufer[a][b] = z;
                                                int type = triangles[i].getSurfaceType();
                                                int color = Frame.int2RGB((int) (255 * Frame.surfaces[type].R),
                                                        (int) (255 * Frame.surfaces[type].G), (int) (255 * Frame.surfaces[type].B));
                                                colorBuffer[a][b] = color;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (int h = 0; h < screenHeight; h++) {
                        for (int w = 0; w < screenWidth; w++) {
                            g2d.setColor(new Color((int) colorBuffer[h][w]));
                            g2d.drawLine(w, h, w, h);
                        }
                    }
                }
                g2d.setColor(Color.black);
                if (Frame.showWireFrame) {
                    for (int i = 0; i < triangles.length; i++) {
                        g2d.drawLine((int) (Frame.triangles[i].v1.x), (int) (Frame.triangles[i].v1.y),
                                (int) (Frame.triangles[i].v2.x), (int) (Frame.triangles[i].v2.y));
                        g2d.drawLine((int) (Frame.triangles[i].v2.x), (int) (Frame.triangles[i].v2.y),
                                (int) (Frame.triangles[i].v3.x), (int) (Frame.triangles[i].v3.y));
                        g2d.drawLine((int) (Frame.triangles[i].v3.x), (int) (Frame.triangles[i].v3.y),
                                (int) (Frame.triangles[i].v1.x), (int) (Frame.triangles[i].v1.y));
                    }
                }
                g2d.setColor(Color.WHITE);
                g2d.fillRect((int) Frame.camera.eye.x - cdr, (int) Frame.camera.eye.y - cdr, 2 * cdr, 2 * cdr);
                g2d.setColor(Color.black);
                g2d.fillRect((int) Frame.camera.lookAt.x - cdr, (int) Frame.camera.lookAt.y - cdr, 2 * cdr, 2 * cdr);

                g2d.drawLine((int) camera.eye.x, (int) camera.eye.y, (int) Ptl.x, (int) Ptl.y);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.y, (int) Ptr.x, (int) Ptr.y);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.y, (int) Pbl.x, (int) Pbl.y);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.y, (int) Pbr.x, (int) Pbr.y);

                g2d.drawLine((int) Ptl.x, (int) Ptl.y, (int) Ptr.x, (int) Ptr.y);
                g2d.drawLine((int) Ptr.x, (int) Ptr.y, (int) Pbr.x, (int) Pbr.y);
                g2d.drawLine((int) Pbr.x, (int) Pbr.y, (int) Pbl.x, (int) Pbl.y);
                g2d.drawLine((int) Pbl.x, (int) Pbl.y, (int) Ptl.x, (int) Ptl.y);

                g2d.drawLine(screenWidth - 1, 0, screenWidth - 1, screenHeight);
                g2d.drawLine(0, screenHeight - 1, screenWidth, screenHeight - 1);

                int size = 25;
                g2d.setColor(new Color(0, 175, 35));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(size, size, size, 2 * size);
                g2d.drawString("y", size + 5, size + 2);
                g2d.setColor(Color.red);
                g2d.drawLine(size, 2 * size + 1, 2 * size + 1, 2 * size + 1);
                g2d.drawString("x", 2 * size - 5, 2 * size - 2);

                ViewPanel.ort2.repaint();
                ViewPanel.ort3.repaint();
            }
            if (side.equals("yz")) {
                if (Frame.showSurfaces) {
                    zBufer = new double[screenHeight][screenWidth];
                    colorBuffer = new double[screenHeight][screenWidth];
                    initializeBuffers(zBufer, colorBuffer);
                    for (int i = 0; i < Frame.triangles.length; i++) {
                        double[] boundingBox = computeBoundingBox(triangles[i]);
                        double triangleArea = edgeFunctionYZ(triangles[i].v1, triangles[i].v2, triangles[i].v3);
                        if (boundingBox[5] > 0 && boundingBox[3] > 0 && boundingBox[4] < screenWidth && boundingBox[1] < screenHeight) {
                            for (int a = (int) boundingBox[1]; a < boundingBox[3]; a++) {
                                for (int b = (int) boundingBox[4]; b < boundingBox[5]; b++) {
                                    if (a > 0 && a < screenHeight && b < screenWidth && b > 0) {
                                        Vertex pixel = new Vertex(0, a, b);
                                        double w0 = edgeFunctionYZ(triangles[i].v2, triangles[i].v3, pixel);
                                        double w1 = edgeFunctionYZ(triangles[i].v3, triangles[i].v1, pixel);
                                        double w2 = edgeFunctionYZ(triangles[i].v1, triangles[i].v2, pixel);

                                        if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                                            w0 /= triangleArea;
                                            w1 /= triangleArea;
                                            w2 /= triangleArea;
                                            double z = computeDepth2(triangles[i].v1, triangles[i].v2, triangles[i].v3, w0, w1, w2);
                                            if (z < zBufer[a][b]) {
                                                zBufer[a][b] = z;
                                                int type = triangles[i].getSurfaceType();
                                                int color = Frame.int2RGB((int) (255 * Frame.surfaces[type].R),
                                                        (int) (255 * Frame.surfaces[type].G), (int) (255 * Frame.surfaces[type].B));
                                                colorBuffer[a][b] = color;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    for (int h = 0; h < screenHeight; h++) {
                        for (int w = 0; w < screenWidth; w++) {
                            g2d.setColor(new Color((int) colorBuffer[h][w]));
                            g2d.drawLine(w, h, w, h);
                        }
                    }
                }
                g2d.setColor(Color.black);
                if (Frame.showWireFrame) {
                    for (int i = 0; i < triangles.length; i++) {
                        g2d.drawLine((int) (Frame.triangles[i].v1.z), (int) (Frame.triangles[i].v1.y),
                                (int) (Frame.triangles[i].v2.z), (int) (Frame.triangles[i].v2.y));
                        g2d.drawLine((int) (Frame.triangles[i].v2.z), (int) (Frame.triangles[i].v2.y),
                                (int) (Frame.triangles[i].v3.z), (int) (Frame.triangles[i].v3.y));
                        g2d.drawLine((int) (Frame.triangles[i].v3.z), (int) (Frame.triangles[i].v3.y),
                                (int) (Frame.triangles[i].v1.z), (int) (Frame.triangles[i].v1.y));
                    }
                }

                g2d.setColor(Color.WHITE);
                g2d.fillRect((int) Frame.camera.eye.z - cdr, (int) Frame.camera.eye.y - cdr, 2 * cdr, 2 * cdr);
                g2d.setColor(Color.black);
                g2d.fillRect((int) Frame.camera.lookAt.z - cdr, (int) Frame.camera.lookAt.y - cdr, 2 * cdr, 2 * cdr);

                g2d.drawLine((int) camera.eye.z, (int) camera.eye.y, (int) Ptl.z, (int) Ptl.y);
                g2d.drawLine((int) camera.eye.z, (int) camera.eye.y, (int) Ptr.z, (int) Ptr.y);
                g2d.drawLine((int) camera.eye.z, (int) camera.eye.y, (int) Pbl.z, (int) Pbl.y);
                g2d.drawLine((int) camera.eye.z, (int) camera.eye.y, (int) Pbr.z, (int) Pbr.y);

                g2d.drawLine((int) Ptl.z, (int) Ptl.y, (int) Ptr.z, (int) Ptr.y);
                g2d.drawLine((int) Ptr.z, (int) Ptr.y, (int) Pbr.z, (int) Pbr.y);
                g2d.drawLine((int) Pbr.z, (int) Pbr.y, (int) Pbl.z, (int) Pbl.y);
                g2d.drawLine((int) Pbl.z, (int) Pbl.y, (int) Ptl.z, (int) Ptl.y);

                g2d.drawLine(0, 0, 0, screenHeight);
                g2d.drawLine(0, screenHeight - 1, screenWidth - 1, screenHeight - 1);


                int size = 25;
                g2d.setColor(new Color(0, 175, 35));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(size, size, size, 2 * size);
                g2d.drawString("y", size + 5, size + 2);
                g2d.setColor(Color.blue);
                g2d.drawLine(size, 2 * size + 1, 2 * size + 1, 2 * size + 1);
                g2d.drawString("z", 2 * size - 5, 2 * size - 2);
                ViewPanel.ort1.repaint();
                ViewPanel.ort3.repaint();
            }
            if (side.equals("xz")) {
                if (Frame.showSurfaces) {
                    zBufer = new double[screenHeight][screenWidth];
                    colorBuffer = new double[screenHeight][screenWidth];
                    initializeBuffers(zBufer, colorBuffer);
                    for (int i = 0; i < Frame.triangles.length; i++) {
                        double[] boundingBox = computeBoundingBox(triangles[i]);
                        double triangleArea = edgeFunctionXZ(triangles[i].v1, triangles[i].v2, triangles[i].v3);
                        if (boundingBox[5] > 0 && boundingBox[2] > 0 && boundingBox[4] < screenHeight && boundingBox[0] < screenWidth) {
                            for (int a = (int) boundingBox[0]; a <= boundingBox[2]; a++) {
                                for (int b = (int) boundingBox[4]; b <= boundingBox[5]; b++) {
                                    if (a > 0 && a < screenWidth && b < screenHeight && b > 0) {
                                        Vertex pixel = new Vertex(a, 0, b);
                                        double w0 = edgeFunctionXZ(triangles[i].v2, triangles[i].v3, pixel);
                                        double w1 = edgeFunctionXZ(triangles[i].v3, triangles[i].v1, pixel);
                                        double w2 = edgeFunctionXZ(triangles[i].v1, triangles[i].v2, pixel);

                                        if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                                            w0 /= triangleArea;
                                            w1 /= triangleArea;
                                            w2 /= triangleArea;

                                            double z = computeDepth3(triangles[i].v1, triangles[i].v2, triangles[i].v3, w0, w1, w2);
                                            if (z < zBufer[b][a]) {
                                                zBufer[b][a] = z;
                                                int type = triangles[i].getSurfaceType();
                                                int color = Frame.int2RGB((int) (255 * Frame.surfaces[type].R),
                                                        (int) (255 * Frame.surfaces[type].G), (int) (255 * Frame.surfaces[type].B));
                                                colorBuffer[b][a] = color;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    for (int h = 0; h < screenHeight; h++) {
                        for (int w = 0; w < screenWidth; w++) {
                            g2d.setColor(new Color((int) colorBuffer[h][w]));
                            g2d.drawLine(w, h, w, h);
                        }
                    }
                }
                g2d.setColor(Color.black);
                if (Frame.showWireFrame) {
                    for (int i = 0; i < triangles.length; i++) {
                        g2d.drawLine((int) (Frame.triangles[i].v1.x), (int) (Frame.triangles[i].v1.z),
                                (int) (Frame.triangles[i].v2.x), (int) (Frame.triangles[i].v2.z));
                        g2d.drawLine((int) (Frame.triangles[i].v2.x), (int) (Frame.triangles[i].v2.z),
                                (int) (Frame.triangles[i].v3.x), (int) (Frame.triangles[i].v3.z));
                        g2d.drawLine((int) (Frame.triangles[i].v3.x), (int) (Frame.triangles[i].v3.z),
                                (int) (Frame.triangles[i].v1.x), (int) (Frame.triangles[i].v1.z));
                    }
                }

                g2d.setColor(Color.WHITE);
                g2d.fillRect((int) Frame.camera.eye.x - cdr, (int) Frame.camera.eye.z - cdr, 2 * cdr, 2 * cdr);
                g2d.setColor(Color.black);
                g2d.fillRect((int) Frame.camera.lookAt.x - cdr, (int) Frame.camera.lookAt.z - cdr, 2 * cdr, 2 * cdr);

                g2d.drawLine((int) camera.eye.x, (int) camera.eye.z, (int) Ptl.x, (int) Ptl.z);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.z, (int) Ptr.x, (int) Ptr.z);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.z, (int) Pbl.x, (int) Pbl.z);
                g2d.drawLine((int) camera.eye.x, (int) camera.eye.z, (int) Pbr.x, (int) Pbr.z);

                g2d.drawLine((int) Ptl.x, (int) Ptl.z, (int) Ptr.x, (int) Ptr.z);
                g2d.drawLine((int) Ptr.x, (int) Ptr.z, (int) Pbr.x, (int) Pbr.z);
                g2d.drawLine((int) Pbr.x, (int) Pbr.z, (int) Pbl.x, (int) Pbl.z);
                g2d.drawLine((int) Pbl.x, (int) Pbl.z, (int) Ptl.x, (int) Ptl.z);

                g2d.drawLine(0, 0, screenWidth, 0);
                g2d.drawLine(screenWidth - 1, 0, screenWidth - 1, screenHeight);

                int size = 25;
                g2d.setColor(Color.blue);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(size, size, size, 2 * size);
                g2d.drawString("z", size + 5, size + 2);
                g2d.setColor(Color.red);
                g2d.drawLine(size, 2 * size + 1, 2 * size + 1, 2 * size + 1);
                g2d.drawString("x", 2 * size - 5, 2 * size - 2);

                ViewPanel.ort1.repaint();
                ViewPanel.ort2.repaint();

            }
            ViewPanel.persp.repaint();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        screenHeight = getHeight();
        screenWidth = getWidth();
        aspect = (double) screenWidth / (double) screenHeight;
        scaleToWindowSize = new double[][]{
                {(screenHeight) / 2, 0, 0, 0},
                {0, (screenHeight) / 2, 0, 0},
                {0, 0, (screenHeight) / 2, 0},
                {0, 0, 0, 1}
        };
        translate2 = new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {(screenWidth) / 4, (screenHeight) / 4, (screenHeight) / 4, 1}
        };
        if (OptionPanel.objectLoaded) {
            setPanelCoordinates();
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        if (OptionPanel.objectLoaded) {
            if (p.x > Frame.camera.eye.x - 1.5 * cdr && p.x < Frame.camera.eye.x + 1.5 * cdr
                    && p.y > Frame.camera.eye.y - 1.5 * cdr && p.y < Frame.camera.eye.y + 1.5 * cdr) {
                posClicked = true;
            } else if (p.x > Frame.camera.lookAt.x - 1.5 * cdr && p.x < Frame.camera.lookAt.x + 1.5 * cdr
                    && p.y > Frame.camera.lookAt.y - 1.5 * cdr && p.y < Frame.camera.lookAt.y + 1.5 * cdr) {
                eyeClicked = true;
            } else if (p.x > Frame.camera.eye.z - 1.5 * cdr && p.x < Frame.camera.eye.z + 1.5 * cdr
                    && p.y > Frame.camera.eye.y - 1.5 * cdr && p.y < Frame.camera.eye.y + 1.5 * cdr) {
                posClicked = true;
            } else if (p.x > Frame.camera.lookAt.z - 1.5 * cdr && p.x < Frame.camera.lookAt.z + 1.5 * cdr
                    && p.y > Frame.camera.lookAt.y - 1.5 * cdr && p.y < Frame.camera.lookAt.y + 1.5 * cdr) {
                eyeClicked = true;
            } else if (p.x > Frame.camera.eye.x - 1.5 * cdr && p.x < Frame.camera.eye.x + 1.5 * cdr
                    && p.y > Frame.camera.eye.z - 1.5 * cdr && p.y < Frame.camera.eye.z + 1.5 * cdr) {
                posClicked = true;
            } else if (p.x > Frame.camera.lookAt.x - 1.5 * cdr && p.x < Frame.camera.lookAt.x + 1.5 * cdr
                    && p.y > Frame.camera.lookAt.z - 1.5 * cdr && p.y < Frame.camera.lookAt.z + 1.5 * cdr) {
                eyeClicked = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        posClicked = false;
        eyeClicked = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        currentMousePos = p;
        if (OptionPanel.objectLoaded) {
            if (side.equals("xy")) {
                if (posClicked) {
                    Frame.camera.eye.x = currentMousePos.x;
                    Frame.camera.eye.y = currentMousePos.y;
                }
                if (eyeClicked) {
                    Frame.camera.lookAt.x = currentMousePos.x;
                    Frame.camera.lookAt.y = currentMousePos.y;
                }

            } else if (side.equals("yz")) {
                if (posClicked) {
                    Frame.camera.eye.z = currentMousePos.x;
                    Frame.camera.eye.y = currentMousePos.y;
                }
                if (eyeClicked) {
                    Frame.camera.lookAt.z = currentMousePos.x;
                    Frame.camera.lookAt.y = currentMousePos.y;
                }
            } else if (side.equals("xz")) {
                if (posClicked) {
                    Frame.camera.eye.x = currentMousePos.x;
                    Frame.camera.eye.z = currentMousePos.y;
                }
                if (eyeClicked) {
                    Frame.camera.lookAt.x = currentMousePos.x;
                    Frame.camera.lookAt.z = currentMousePos.y;
                }
            }
            Frame.camera.eye.multiplyByMatrix(invertedToScreenMatrix);
            Frame.camera.lookAt.multiplyByMatrix(invertedToScreenMatrix);
            Frame.camera.updateCoords();
            ViewPanel.persp.transformCamera();
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
}
