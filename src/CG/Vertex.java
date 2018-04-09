package CG;

public class Vertex {
    double x, xT, xTT;
    double y, yT, yTT;
    double z, zT, zTT;
    double w, wT, wTT;

    Vertex() {
    }

    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        w = 1;
        wT = 1;
        xT = x;
        yT = y;
        zT = z;
        wTT = 1;
        xTT = x;
        yTT = y;
        zTT = z;
    }

    Vertex(Vertex v) {
        this(v.x, v.y, v.z);
    }

    public void multiplyByMatrix(double[][] tab2) {
        double[][] tab1 = {{x, y, z, w}};
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
        x = macierzPomnozona[0][0];
        y = macierzPomnozona[0][1];
        z = macierzPomnozona[0][2];
    }

    public void multiplyByMatrixT(double[][] tab2) {
        double[][] tab1 = {{xT, yT, zT, wT}};
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
        xT = macierzPomnozona[0][0];
        yT = macierzPomnozona[0][1];
        zT = macierzPomnozona[0][2];
    }

    public void multiplyByScalar(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
    }

    public void addVector(Vertex v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
