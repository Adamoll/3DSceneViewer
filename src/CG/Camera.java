package CG;

public class Camera {
    Vertex eye;
    Vertex lookAt;
    Vertex up;
    double fov;
    double twistAngle;

    public Camera(double x, double y, double z, double xc, double yc, double zc, double fov, double ta) {
        eye = new Vertex(x, y, z);
        eye.xT = x;
        eye.yT = y;
        eye.zT = z;
        lookAt = new Vertex(xc, yc, zc);
        lookAt.xT = xc;
        lookAt.yT = yc;
        lookAt.zT = zc;
        this.fov = fov;
        this.twistAngle = ta;
    }
    
    public void resetCoords()
    {
        eye.x = eye.xTT;
        eye.y = eye.yTT;
        eye.z = eye.zTT;
        lookAt.x = lookAt.xTT;
        lookAt.y = lookAt.yTT;
        lookAt.z = lookAt.zTT;
    }
    
    public void updateCoords()
    {
        eye.xTT = eye.x;
        eye.yTT = eye.y;
        eye.zTT = eye.z;
        lookAt.xTT = lookAt.x;
        lookAt.yTT = lookAt.y;
        lookAt.zTT = lookAt.z;
        
        eye.xT = eye.x;
        eye.yT = eye.y;
        eye.zT = eye.z;
        lookAt.xT = lookAt.x;
        lookAt.yT = lookAt.y;
        lookAt.zT = lookAt.z;
    }
    
    public void updateFov(double f) {
        fov = f;
    }
    
    public void updateTwistAngle(double ta){
        this.twistAngle = ta;
    }
}
