package CG;

public class Triangle {
    Vertex v1;
    Vertex v2;
    Vertex v3;
    Vertex normal;
    int surfaceType;
    
    Triangle(Vertex v1, Vertex v2, Vertex v3)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;

        normal = Frame.crossProduct(new Vertex(v1.xTT - v3.xTT, v1.yTT -  v3.yTT, v1.zTT - v3.zTT),
                new Vertex(v2.xTT - v3.xTT, v2.yTT - v3.yTT, v2.zTT - v3.zTT));
        normal = Frame.crossProduct(new Vertex(v3.xTT - v2.xTT, v3.yTT -  v2.yTT, v3.zTT - v2.zTT),
                new Vertex(v1.xTT - v2.xTT, v1.yTT - v2.yTT, v1.zTT - v2.zTT));
        normal = Frame.norm(normal);
    }
    public void setSurfaceType(int i)
    {
        surfaceType = i;
    }
    
    public int getSurfaceType()
    {
        return surfaceType;
    }
    
    public void computeNormal() {
        normal = Frame.crossProduct(new Vertex(v1.xT - v3.xT, v1.yT -  v3.yT, v1.zT - v3.zT),
                new Vertex(v2.xT - v3.xT, v2.yT - v3.yT, v2.zT - v3.zT));
        normal = Frame.norm(normal);
    }
}
