import jade.core.Agent;
import java.util.ArrayList;
import java.util.HashMap;

public class Ambiente{
    public static final double limiteX = 500;
    public static final double limiteY = 500;

    public Metereologia[] metereologia;
    public HashMap<String,Localizacao> aeroportos;

    public Ambiente(HashMap<String,Localizacao> localizacoes){
        this.aeroportos = new HashMap<String,Localizacao>();
        
        for (String key : localizacoes.keySet()) {
           this.aeroportos.put(key,localizacoes.get(key));
        }
    }

}
