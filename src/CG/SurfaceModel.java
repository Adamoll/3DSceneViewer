package CG;

public class SurfaceModel {
    double R;
    double G;
    double B;
    Vertex kd;
    Vertex ks;
    Vertex ka;
    double gp;

    public SurfaceModel(double r, double g,double b, Vertex kd, Vertex ks, Vertex ka, double gg) {
        this.R = r;
        this.G = g;
        this.B = b;
        this.kd = kd;
        this.ks = ks;
        this.ka = ka;
        this.gp = gg;
        
    }
    
    
}
