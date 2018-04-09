package CG;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;

public class PerspectivePanel extends JPanel implements ComponentListener {

    static double[][] perspectiveProjectionMatrix;
    static double[][] cameraTransformMatrix;
    static double[][] translateOfOrgin;
    static double[][] reflexYAxis;
    static double[][] scale;
    static double[][] viewportMatrix;
    static double[][] translationOfCamera;
    double[][] windowSizeScale;
    double[][] zBufer;
    double[][] colorBuffer;
    SurfaceModel[] surfaces;
    Vertex ptk000, ptk100, ptk010, ptk001;
    Vertex right, up, forward;

    double minZ;
    double maxZ;
    Camera camera;
    Triangle[] triangles;

    int screenWidth;
    int screenHeight;
    static int fovValue;
    static int twistAngleValue;
    static double aspect;
    double znear;
    double zfar;

    public PerspectivePanel() {
        this.addComponentListener(this);

        aspect = screenWidth / (screenHeight + 1);
        znear = 0.7;
        zfar = 100;
        screenWidth = getWidth();
        screenHeight = getHeight();
        ptk000 = new Vertex(0, 0, 0);
        ptk100 = new Vertex(1, 0, 0);
        ptk010 = new Vertex(0, 1, 0);
        ptk001 = new Vertex(0, 0, 1);

        translateOfOrgin = new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0.5, 0.5, 0.5, 1}
        };

        reflexYAxis = new double[][]{
                {1, 0, 0, 0},
                {0, -1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        scale = new double[][]{
                {0.5, 0, 0, 0},
                {0, 0.5, 0, 0},
                {0, 0, 0.5, 0},
                {0, 0, 0, 1}
        };

        windowSizeScale = new double[][]{
                {screenWidth, 0, 0, 0},
                {0, screenHeight, 0, 0},
                {0, 0, screenHeight, 0},
                {0, 0, 0, 1}
        };

        viewportMatrix = Frame.multiplyMatrices(reflexYAxis, scale);
        viewportMatrix = Frame.multiplyMatrices(viewportMatrix, translateOfOrgin);

    }

    public void setPerspectiveMatrix() {
        if (OptionPanel.objectLoaded) {
            perspectiveProjectionMatrix = new double[][]{
                    {cot(camera.fov / 2) / aspect, 0, 0, 0},
                    {0, cot(camera.fov / 2), 0, 0},
                    {0, 0, -(znear + zfar) / (zfar - znear), 1},
                    {0, 0, (2 * zfar * znear) / (zfar - znear), 0}
            };
            transformCamera();
        }
    }

    public void transformCamera() {
        if (OptionPanel.objectLoaded) {

            translationOfCamera = new double[][]{
                    {1, 0, 0, 0},
                    {0, 1, 0, 0},
                    {0, 0, 1, 0},
                    {-camera.eye.xT, -camera.eye.yT, -camera.eye.zT, 1}
            };

            double katoz = Math.toRadians(camera.twistAngle);
            double[][] ozrotate = {
                    {Math.cos(katoz), -Math.sin(katoz), 0, 0},
                    {Math.sin(katoz), Math.cos(katoz), 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            };

            Vertex upVec = new Vertex(0, 1, 0);
            upVec.multiplyByMatrix(ozrotate);
            camera.up = upVec;
            Vertex zaxis = norm(new Vertex(camera.lookAt.xT - camera.eye.xT, camera.lookAt.yT - camera.eye.yT,
                    camera.lookAt.zT - camera.eye.zT));
            Vertex xaxis = norm(crossProduct(zaxis, norm(new Vertex(0, 1, 0))));
            Vertex yaxis = norm(crossProduct(xaxis, zaxis));

            cameraTransformMatrix = new double[][]{
                    {xaxis.x, yaxis.x, -zaxis.x, 0},
                    {xaxis.y, yaxis.y, -zaxis.y, 0},
                    {xaxis.z, yaxis.z, -zaxis.z, 0},
                    {0, 0, 0, 1}
            };
            double[][] trans = Frame.multiplyMatrices(translationOfCamera, cameraTransformMatrix);
            cameraTransformMatrix = Frame.multiplyMatrices(cameraTransformMatrix, ozrotate);
            cameraTransformMatrix = Frame.multiplyMatrices(translationOfCamera, cameraTransformMatrix);

            double[][] trans2 = Frame.copyMatrix(trans);
            double[][] inverted = Frame.invert(trans);
            xaxis.multiplyByMatrix(trans2);
            xaxis.multiplyByMatrix(ozrotate);
            xaxis.multiplyByMatrix(inverted);
            yaxis.multiplyByMatrix(trans2);
            yaxis.multiplyByMatrix(ozrotate);
            yaxis.multiplyByMatrix(inverted);
            up = norm(yaxis);
            right = norm(xaxis);
        }
    }

    public double[] computeBoundingBox(Triangle t) {
        double[] box = new double[4];//0 - minx, 1 - miny, 2 - maxx, 3 - maxy
        box[0] = Double.MAX_VALUE;
        box[1] = Double.MAX_VALUE;
        box[2] = Double.MIN_VALUE;
        box[3] = Double.MIN_VALUE;
        Vertex[] v = {t.v1, t.v2, t.v3};
        for (Vertex vert : v) {
            if (vert.xT < box[0]) {
                box[0] = vert.xT - 3;
            }
            if (vert.xT > box[2]) {
                box[2] = vert.xT + 3;
            }
            if (vert.yT < box[1]) {
                box[1] = vert.yT - 3;
            }
            if (vert.yT > box[3]) {
                box[3] = vert.yT + 3;
            }
        }

        return box;
    }

    public double edgeFunction(Vertex a, Vertex b, Vertex c) {
        return (c.xT - a.xT) * (b.yT - a.yT) - (c.yT - a.yT) * (b.xT - a.xT);
    }

    public double computeDepth(Vertex v0, Vertex v1, Vertex v2, double w0, double w1, double w2) {
        return (w0 * v0.zT + w1 * v1.zT + w2 * v2.zT);
    }

    public Vertex computeLight(Triangle t, Illumination i, Vertex v) {
        double diffuseR = 0, diffuseG = 0, diffuseB = 0;
        double ambientR = 0, ambientG = 0, ambientB = 0;
        double spectacularR = 0, spectacularG = 0, spectacularB = 0;
        double ox = (t.v1.xT + t.v2.xT + t.v3.xT) / 3;
        double oy = (t.v1.yT + t.v2.yT + t.v3.yT) / 3;
        double oz = (t.v1.zT + t.v2.zT + t.v3.zT) / 3;
        Vertex surfToLight = norm(new Vertex(v.x - ox, v.y - oy, v.z - oz));
        surfToLight.multiplyByScalar(-1);
        double r = surfToLight.length();
        t.computeNormal();

        double n_dot_l = Math.max(0, Frame.dot(t.normal, surfToLight));
        r = fDist(r);
        r = 1;
        int type = t.getSurfaceType();

        ambientR = surfaces[type].ka.x;
        ambientG = surfaces[type].ka.y;
        ambientB = surfaces[type].ka.z;

        if (n_dot_l > 0) {
            diffuseR = surfaces[type].kd.x * n_dot_l * i.R * surfaces[type].R;
            diffuseG = surfaces[type].kd.y * n_dot_l * i.G * surfaces[type].G;
            diffuseB = surfaces[type].kd.z * n_dot_l * i.B * surfaces[type].B;
            Vertex camMult = new Vertex(camera.eye.xTT, camera.eye.yTT, camera.eye.zTT);
            camMult.multiplyByMatrix(scale);
            camMult.multiplyByMatrix(cameraTransformMatrix);
            camMult.multiplyByMatrix(perspectiveProjectionMatrix);
            camMult.x /= camMult.z;
            camMult.y /= camMult.z;
            camMult.multiplyByMatrix(viewportMatrix);
            camMult.multiplyByMatrix(windowSizeScale);
            Vertex cameraV = norm(new Vertex((camMult.x - ox), (camMult.y - oy),
                    (camMult.z - oz)));
            double scalar = 2 * Frame.dot(t.normal, surfToLight);
            Vertex R = norm(new Vertex(scalar * t.normal.x - surfToLight.x, scalar * t.normal.y - surfToLight.y,
                    scalar * t.normal.z - surfToLight.z));
            R.multiplyByScalar(-1);

            double spec = Math.pow(Math.max(0, Frame.dot(R, cameraV)), surfaces[type].gp);

            spectacularR = r * i.R * spec * surfaces[type].ks.x;
            spectacularG = r * i.G * spec * surfaces[type].ks.y;
            spectacularB = r * i.B * spec * surfaces[type].ks.z;

        }
        return new Vertex(diffuseR + spectacularR + ambientR, diffuseG + spectacularG + ambientG, diffuseB + spectacularB + ambientB);
    }

    public double fDist(double r) {
        return Math.min(1, 1 / (0.1 * r * r + 0.2 * r + 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (OptionPanel.objectLoaded) {
            for (int i = 0; i < Frame.vertexes.length; i++) {
                Frame.vertexes[i].multiplyByMatrixT(cameraTransformMatrix);
                Frame.vertexes[i].multiplyByMatrixT(perspectiveProjectionMatrix);
                Frame.vertexes[i].xT /= (Frame.vertexes[i].zT);
                Frame.vertexes[i].yT /= (Frame.vertexes[i].zT);
                Frame.vertexes[i].multiplyByMatrixT(viewportMatrix);
                Frame.vertexes[i].multiplyByMatrixT(windowSizeScale);
            }

            if (Frame.showSurfaces) {
                zBufer = new double[screenHeight][screenWidth];
                colorBuffer = new double[screenHeight][screenWidth];
                initializeBuffers(zBufer, colorBuffer);
                for (int i = 0; i < triangles.length; i++) {
                    double[] boundingBox = computeBoundingBox(triangles[i]);
                    double triangleArea = edgeFunction(triangles[i].v1, triangles[i].v2, triangles[i].v3);
                    //Vertex light = computeLight(triangles[i], Frame.lighting, lightPos);
                    if (boundingBox[2] > 0 && boundingBox[3] > 0 && boundingBox[0] < screenWidth && boundingBox[1] < screenHeight) {
                        for (int a = (int) boundingBox[1] - 1; a < boundingBox[3]; a++) {
                            for (int b = (int) boundingBox[0] - 1; b < boundingBox[2]; b++) {
                                if (a > 0 && a < screenHeight && b < screenWidth && b > 0) {
                                    Vertex pixel = new Vertex(b, a, 0);
                                    double w0 = edgeFunction(triangles[i].v2, triangles[i].v3, pixel);
                                    double w1 = edgeFunction(triangles[i].v3, triangles[i].v1, pixel);
                                    double w2 = edgeFunction(triangles[i].v1, triangles[i].v2, pixel);
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
                for (Triangle t : triangles) {
                    g2d.drawLine((int) (t.v1.xT), (int) (t.v1.yT),
                            (int) (t.v2.xT), (int) (t.v2.yT));
                    g2d.drawLine((int) (t.v2.xT), (int) (t.v2.yT),
                            (int) (t.v3.xT), (int) (t.v3.yT));
                    g2d.drawLine((int) (t.v3.xT), (int) (t.v3.yT),
                            (int) (t.v1.xT), (int) (t.v1.yT));
                }
            }
            ptk000.multiplyByMatrixT(cameraTransformMatrix);
            ptk000.multiplyByMatrixT(perspectiveProjectionMatrix);
            ptk000.xT /= ptk000.zT;
            ptk000.yT /= ptk000.zT;
            ptk000.multiplyByMatrixT(viewportMatrix);
            ptk000.multiplyByMatrixT(windowSizeScale);

            ptk100.multiplyByMatrixT(cameraTransformMatrix);
            ptk100.multiplyByMatrixT(perspectiveProjectionMatrix);
            ptk100.xT /= ptk100.zT;
            ptk100.yT /= ptk100.zT;
            ptk100.multiplyByMatrixT(viewportMatrix);
            ptk100.multiplyByMatrixT(windowSizeScale);

            ptk010.multiplyByMatrixT(cameraTransformMatrix);
            ptk010.multiplyByMatrixT(perspectiveProjectionMatrix);
            ptk010.xT /= ptk010.zT;
            ptk010.yT /= ptk010.zT;
            ptk010.multiplyByMatrixT(viewportMatrix);
            ptk010.multiplyByMatrixT(windowSizeScale);

            ptk001.multiplyByMatrixT(cameraTransformMatrix);
            ptk001.multiplyByMatrixT(perspectiveProjectionMatrix);
            ptk001.xT /= ptk001.zT;
            ptk001.yT /= ptk001.zT;
            ptk001.multiplyByMatrixT(viewportMatrix);
            ptk001.multiplyByMatrixT(windowSizeScale);

            g2d.setColor(Color.RED);
            g2d.drawLine((int) ptk000.xT, (int) ptk000.yT, (int) ptk100.xT, (int) ptk100.yT);
            g2d.setColor(Color.GREEN);
            g2d.drawLine((int) ptk000.xT, (int) ptk000.yT, (int) ptk010.xT, (int) ptk010.yT);
            g2d.setColor((Color.BLUE));
            g2d.drawLine((int) ptk000.xT, (int) ptk000.yT, (int) ptk001.xT, (int) ptk001.yT);

            g2d.setColor((Color.black));
            g2d.drawLine(0, 0, screenWidth, 0);
            g2d.drawLine(0, 0, 0, screenHeight);

            for (Vertex v : Frame.vertexes) {
                v.xT = v.xTT;
                v.yT = v.yTT;
                v.zT = v.zTT;
                v.wT = v.wTT;
            }
            ptk000 = new Vertex(0, 0, 0);
            ptk100 = new Vertex(1, 0, 0);
            ptk010 = new Vertex(0, 1, 0);
            ptk001 = new Vertex(0, 0, 1);

        }
    }

    void initializeBuffers(double[][] zb, double[][] cb) {
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                zb[i][j] = Double.MAX_VALUE;
                cb[i][j] = 13158600;
            }
        }
    }

    static public double cot(double kat) {
        return 1.0 / Math.tan(Math.toRadians(kat));
    }

    @Override
    public void componentResized(ComponentEvent e) {
        screenHeight = getHeight();
        screenWidth = getWidth();
        aspect = (double) screenWidth / (double) screenHeight;
        scale = new double[][]{
                {0.5, 0, 0, 0},
                {0, 0.5, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };

        windowSizeScale = new double[][]{
                {screenWidth, 0, 0, 0},
                {0, screenHeight, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        translateOfOrgin = new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0.5, 0.5, 0, 1}
        };
        if (OptionPanel.objectLoaded) {
            perspectiveProjectionMatrix[0][0] = cot(camera.fov / 2) / aspect;
            perspectiveProjectionMatrix[1][1] = cot(camera.fov / 2);
        }
        //setPerspectiveMatrix();
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
}
