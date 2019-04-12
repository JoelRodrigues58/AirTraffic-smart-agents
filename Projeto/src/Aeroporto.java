import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

public class Aeroporto extends Agent{


    private Localizacao localizacao;
    private float alertZone;
    private float protectedZone;
    private float requestZone;
    private int pistasAterragem;
    private int limiteAeronavesEstacionadas;
    private int aeronavesEstacionadasAtualmente;
    private Ambiente ambiente;
    private List<String> aeronavesEstacionadas;
    private HashMap<String, Double> pedidosAterragem;
    private HashMap<String, ArrayList<Object>> aeronavesProximas;

  protected void setup(){
        
        Object[] args = getArguments();
        this.localizacao= (Localizacao) args[0];
        this.pistasAterragem = (int)args[1];
        this.limiteAeronavesEstacionadas = (int)args[2]; 
        this.ambiente = (Ambiente)args[3];
        if(args.length==5) {
            this.aeronavesEstacionadas = (List<String>) args[4];
        }
        else this.aeronavesEstacionadas = new ArrayList<String>();

        this.pedidosAterragem = new HashMap<>();
        this.aeronavesProximas = new HashMap<>();

        super.setup();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("aeroporto");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new PermissaoDescolagem(this, 5000));
        addBehaviour(new RecebeTempoChegada());
        
    }

    private class PermissaoDescolagem extends TickerBehaviour {

        public PermissaoDescolagem (Agent a, long t){
            super(a,t);
        }

        @Override
        public void onTick() {
            if(aeronavesEstacionadas.size() > 0){
                AID aeronave = new AID(aeronavesEstacionadas.get(0), AID.ISLOCALNAME);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(aeronave);
                msg.setContent("PermissaoDescolagem");
                myAgent.send(msg);
                aeronavesEstacionadas.remove(0);
            }else{/*Não faz nada*/}
        }
    }
    
    private class RecebeTempoChegada extends CyclicBehaviour{

        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                if(msg.getPerformative()==ACLMessage.INFORM){
                    String[] parts = msg.getContent().split(";");
                    if(parts[0].equals("TempoChegada")){
                        pedidosAterragem.put(parts[1],Double.parseDouble(parts[2]));
                        resolveConflitosChegada_Velocidade();
                    }
                    else if(parts[0].equals("Informacao")){
                        ArrayList<Object> informacao = new ArrayList<Object>();
                        informacao.add(Integer.parseInt(parts[2]));
                        informacao.add(Integer.parseInt(parts[3]));
                        informacao.add(Double.parseDouble(parts[4]));
                        informacao.add(Double.parseDouble(parts[5]));
                        informacao.add(parts[6]);
                        informacao.add(Double.parseDouble(parts[7]));
                        aeronavesProximas.put(parts[1],informacao);       
                    }
                    else if(parts[0].equals("TempoChegadaAlert")){

                        if(aeronavesProximas.values().size()+aeronavesEstacionadasAtualmente > limiteAeronavesEstacionadas 
                                ||
                            aeronavesProximas.values().size()>pistasAterragem){
          
                            String aeronave = resolveConflitosChegada_Rota();
                            if(aeronave!=null){
                                ACLMessage msg1 = new ACLMessage(ACLMessage.REQUEST);
                                AID receiver1 = new AID(aeronave,AID.ISLOCALNAME);
                                msg1.addReceiver(receiver1);
                                msg1.setContent("AlteraRota");
                                send(msg1);
                            }
                        }
                        
                    }
                    else if(parts[0].equals("TempoChegadaProtected")){
                        pistasAterragem--;
                    }
                    else if(parts[0].equals("AterragemEfetuada")){
                        System.out.println( parts[1] +" Aterrou");
                        pistasAterragem++;
                        aeronavesEstacionadasAtualmente++;
                        //aeronavesEstacionadas.add(parts[1]);
                        pedidosAterragem.remove(parts[1]);
                        aeronavesProximas.remove(parts[1]);
                    }
                }
            }
        }
        
        
    }

    private void resolveConflitosChegada_Velocidade() {
        
        for (String aeronave1: pedidosAterragem.keySet()) {
            double tempo1 = pedidosAterragem.get(aeronave1);
            for(String aeronave2 : pedidosAterragem.keySet()){
                double tempo2 = pedidosAterragem.get(aeronave2);
                if (!aeronave1.equals(aeronave2) &&
                    Math.abs(tempo1 - tempo2) < 5){
                        ACLMessage msg1 = new ACLMessage(ACLMessage.REQUEST);
                        AID receiver1 = new AID(aeronave1,AID.ISLOCALNAME);
                        msg1.addReceiver(receiver1);
                        msg1.setContent("AumentaVelocidade");
                        send(msg1);

                        ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
                        AID receiver2 = new AID(aeronave2,AID.ISLOCALNAME);
                        msg2.addReceiver(receiver2);
                        msg2.setContent("DiminuiVelocidade");
                        send(msg2);
                }
            }
        }
    }
    
    private String resolveConflitosChegada_Rota(){
        String aeronaveFinal=null;
        String aeronaveTemporaria;
        double pesoTemporario;
        double pesoFinal=0;
        double peso1=0;
        double peso2=0;

        if (aeronavesProximas.keySet().size() == 1){
            aeronaveFinal = (String) aeronavesProximas.keySet().toArray()[0];
        }

        else {
            for (String aeronave1 : aeronavesProximas.keySet()) {
                ArrayList<Object> info1 = aeronavesProximas.get(aeronave1);
                for (String aeronave2 : aeronavesProximas.keySet()) {
                    ArrayList<Object> info2 = aeronavesProximas.get(aeronave2);

                    if ((Integer) info1.get(0) > (Integer) info2.get(0)) peso1 += 0.1;
                    else if ((Integer) info1.get(0) < (Integer) info2.get(0)) peso2 += 0.1;
                    if ((Integer) info1.get(1) > (Integer) info2.get(1)) peso1 += 0.1;
                    else if ((Integer) info1.get(1) < (Integer) info2.get(1)) peso2 += 0.1;
                    if ((Double) info1.get(2) > (Double) info2.get(2)) peso1 += 0.1;
                    else if ((Double) info1.get(2) < (Double) info2.get(2)) peso2 += 0.1;
                    if ((Double) info1.get(3) > (Double) info2.get(3)) peso1 += 0.4;
                    else if ((Double) info1.get(3) < (Double) info2.get(3)) peso2 += 0.4;
                    if ((Double) info1.get(5) < (Double) info2.get(5)) peso1 += 0.3;
                    else if ((Double) info1.get(5) > (Double) info2.get(5)) peso2 += 0.3;

                    if (peso1 >= peso2) {
                        pesoTemporario = peso1;
                        aeronaveTemporaria = aeronave1;
                    } else {
                        pesoTemporario = peso2;
                        aeronaveTemporaria = aeronave2;
                    }

                    if (pesoTemporario > pesoFinal) {
                        pesoFinal = pesoTemporario;
                        aeronaveFinal = aeronaveTemporaria;
                    }
                }
            }
        }
        return aeronaveFinal;
         
    }
    
}
