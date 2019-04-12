public class Movimentacao {
    private double X;
    private double Y;
    private double velocidade;

    public Movimentacao(double movimentoX, double movimentoY, double velocidade) {
        this.X = movimentoX;
        this.Y = movimentoY;
        this.velocidade = velocidade;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        this.X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        this.Y = y;
    }

    public double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(double velocidade) {
        this.velocidade = velocidade;
    }
}