import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.awt.EventQueue;
import java.util.HashMap;

public class Interface extends Agent{
    
    public KeyBindings apresentacao;
    public Ambiente ambiente;
    public HashMap<String,Localizacao> aeronaves;
    public HashMap<String,Movimentacao> direcoes_aeronaves;
    
    protected void setup(){
        Object[] args = getArguments();
        this.ambiente = (Ambiente)args[0];
        this.aeronaves = new HashMap<String,Localizacao>();
        this.direcoes_aeronaves = new HashMap<String,Movimentacao>(); //Associação de aviões -> movimentacao
        super.setup();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("interface");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        addBehaviour(new showInfo());
        addBehaviour(new recebeLocalizacoes());
    }
    
    private class showInfo extends OneShotBehaviour{

        public void action() {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new KeyBindings(ambiente,aeronaves,direcoes_aeronaves);
                }
            });
        }

    }
    

    private class recebeLocalizacoes extends CyclicBehaviour{

        @Override
        public synchronized void action() {
            ACLMessage msg = receive();
            String[] parts;
            double x,y,mov_x,mov_y,vel;
            if(msg!=null){
                if(msg.getPerformative()==7){
                     parts= msg.getContent().split(";");
                     if(parts[0].equals("Localizacao")){
                        x=Double.parseDouble(parts[1]);
                        y=Double.parseDouble(parts[2]);
                        mov_x= Double.parseDouble(parts[4]);
                        mov_y= Double.parseDouble(parts[5]);
                        vel= Double.parseDouble(parts[6]);
                        Localizacao l = new Localizacao(x,y);
                        Movimentacao m = new Movimentacao(mov_x,mov_y,vel);
                        aeronaves.put(msg.getSender().getLocalName(),l);
                        direcoes_aeronaves.put(msg.getSender().getLocalName(),m);
                     }
                }
            }
        }
        
        
    }
    
 
    
}
