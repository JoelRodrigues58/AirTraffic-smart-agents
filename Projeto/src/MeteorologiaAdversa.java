public class MeteorologiaAdversa {
    private Localizacao localizacao;
    private double raio;
    private boolean estado;

    // true -> possível movimentar nesta zona
    // false -> caso contrário


    public MeteorologiaAdversa(Localizacao localizacao, double raio, boolean estado) {
        this.localizacao = localizacao;
        this.raio = raio;
        this.estado = estado;
    }
}
