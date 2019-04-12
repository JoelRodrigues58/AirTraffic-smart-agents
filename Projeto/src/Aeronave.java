import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


public class Aeronave extends Agent {
    public static final double alertZone = 80;
    public static final double protectedZone = 50;

    public static final double aeroportoRequestZone = 120;
    public static final double aeroportoAlertZone = 75;
    public static final double aeroportoProtectedZone = 40;
    
    private Localizacao localizacao;
    private int passageiros;
    private double distanciaPercorrida;
    private String companhia;
    private double combustivel;
    private Localizacao destino;
    private Movimentacao movimentacao;
    private int nColisoes;
    private Ambiente ambiente;
    private int id;
    private boolean master;
    private boolean permissaoDescolagem;
    private boolean informacoesEnviadas;
    private boolean estacionado;
    private boolean tempoProtectedEnviado;



    public void init(){
        this.master = false;
        this.permissaoDescolagem = false;
        this.informacoesEnviadas = false;
        this.tempoProtectedEnviado=false;
    }


    protected void setup(){
        
        Object[] args = getArguments();
        this.localizacao= (Localizacao) args[0];
        this.passageiros = (int)args[1];
        this.companhia = args[2].toString(); 
        this.combustivel = (double)args[3];
        this.ambiente = (Ambiente)args[4];
        this.id= (int)args[5];
        init();

        super.setup();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("aeronave");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new iniciaViagem());
        Viagem viagem = new Viagem();

        viagem.addSubBehaviour(new enviaLocalizacao(this,100));
        viagem.addSubBehaviour(new efetuaViagem(this,100));
        viagem.addSubBehaviour(new detetaColisao());
        viagem.addSubBehaviour(new preparaAterragem(this,100));
        addBehaviour(viagem);

    }

    private class Viagem extends ParallelBehaviour {
        @Override
        public void addSubBehaviour(Behaviour b) {
            super.addSubBehaviour(b);
        }
    }

    private class iniciaViagem extends OneShotBehaviour{

        @Override
        public void action() {
                Random generator = new Random();
                ArrayList<Localizacao> values = new ArrayList<Localizacao>((ambiente.aeroportos).values());

                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).equals(localizacao)) values.remove(values.get(i));
                }


                // Escolhe aeroporto de destino
                destino = (Localizacao) values.get(generator.nextInt(values.size()));
                System.out.println("Destino : " + destino.x + ", " + destino.y + "; Localizacao: " + localizacao.x + ", " + localizacao.y);

                double distanciaX = destino.x - localizacao.x;
                distanciaX = (distanciaX == 0) ? 1 : distanciaX;
                double distanciaY = destino.y - localizacao.y;
                distanciaY = (distanciaY == 0) ? 1 : distanciaY;

                double modulo = calculaModulo(distanciaX, distanciaY);


                Random r = new Random();
                movimentacao = new Movimentacao(distanciaX / modulo, distanciaY / modulo, 1 + (0.1 * r.nextDouble()));

            }

        }
    
    private class efetuaViagem extends TickerBehaviour{

         public efetuaViagem(Agent a, long time){
            super(a,time);
        }
        @Override
        protected void onTick() {
            if (permissaoDescolagem && !estacionado){
               
                    
                if (Math.round(localizacao.x) != Math.round(destino.x)
                        || Math.round(localizacao.y) != Math.round(destino.y)) {
                    localizacao.x += movimentacao.getX() * movimentacao.getVelocidade();
                    localizacao.y += movimentacao.getY() * movimentacao.getVelocidade();
                    distanciaPercorrida += Math.sqrt(Math.pow(movimentacao.getX() * movimentacao.getVelocidade(), 2) +
                            Math.pow(movimentacao.getY() * movimentacao.getVelocidade(), 2));
                }
                else if (Math.sqrt( Math.pow(localizacao.x-destino.x,2) + Math.pow(localizacao.y - destino.y,2)) < 5){
                    init();
                    System.out.println("Aterrei " + permissaoDescolagem);
                    Localizacao l = new Localizacao(destino.x,destino.y);
                    String aeroporto=null;
                    Iterator it = ambiente.aeroportos.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        if(pair.getValue().equals(l)) aeroporto = (String) pair.getKey();
                    }
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    AID receiver = new AID(aeroporto,AID.ISLOCALNAME);
                    msg.addReceiver(receiver);
                    msg.setContent("AterragemEfetuada;"+myAgent.getLocalName());
                    myAgent.send(msg);
                    estacionado=true;
                }

            }
        }
    }
    
    private class enviaLocalizacao extends TickerBehaviour{
        
        public enviaLocalizacao(Agent a, long time){
            super(a,time);
        }
        
        @Override
        protected void onTick() {
            if (permissaoDescolagem) {
                DFAgentDescription dfd = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("aeronave");
                dfd.addServices(sd);

                // Search DF
                try {
                    DFAgentDescription[] results = DFService.search(this.myAgent, dfd);
                    ArrayList<DFAgentDescription> resultsList = new ArrayList<>();
                    for (DFAgentDescription df : results
                    ) {
                        if (!df.getName().equals(myAgent.getName()))
                            resultsList.add(df);
                    }

                    if (resultsList.size() > 0) {
                        for (int i = 0; i < resultsList.size(); ++i) {
                            DFAgentDescription dfd1 = resultsList.get(i);
                            AID provider = dfd1.getName();
                            AID interfaceName = new AID("Interface", AID.ISLOCALNAME);
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(provider);
                            msg.addReceiver(interfaceName);
                               msg.setContent("Localizacao;" + localizacao.toString() + 
                                ";Movimentacao;"+movimentacao.getX() +";" + movimentacao.getY()+";" +movimentacao.getVelocidade());
                            //System.out.println(msg.getContent());
                            myAgent.send(msg); //ver otimização
                        }
                    } else {
                        System.out.println("Não existem aeronaves no sistema!");
                    }

                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    private class detetaColisao extends CyclicBehaviour{
        HashMap<String, Localizacao> aeronavesAlert = new HashMap<>();

        @Override
        public void action() {

            //if (permissaoDescolagem) {
                ACLMessage msg = receive();
                Double x_aa2;
                Double y_aa2;


                if (msg != null) {
                    ACLMessage resp = msg.createReply();
                    //System.out.println("Mensagem: " + msg.getContent());
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        if (msg.getContent().equals("PermissaoDescolagem"))
                            permissaoDescolagem = true;
                    }
                    if(permissaoDescolagem){
                    if (msg.getPerformative() == ACLMessage.INFORM) { //Informs
                        String[] parts = msg.getContent().split(";");
                        if (parts[0].equals("Localizacao")) { //Mensagem recebida for a localizacao de AA2
                            x_aa2 = Double.parseDouble(parts[1]);
                            y_aa2 = Double.parseDouble(parts[2]);
                            String res = calculaDistancia(localizacao.x, localizacao.y, x_aa2, y_aa2,0);
                            if (res.equals("alert")) { //AA2 dentro da alertZone-> pede-se a direção

                                resp.setPerformative(ACLMessage.REQUEST);
                                resp.setContent("RequestDirecao");
                                myAgent.send(resp);
                                aeronavesAlert.put(msg.getSender().getLocalName(), new Localizacao(x_aa2, y_aa2));
                            } else if (res.equals("protected")) { //AA2 dentro da protectedzone-> informa-se de colisao
                                aeronavesAlert.put(msg.getSender().getLocalName(), new Localizacao(x_aa2, y_aa2));
                                int tipoColisao = existePossibilidadeColisao(msg.getSender().getLocalName(),
                                        Double.parseDouble(parts[1]),
                                        Double.parseDouble(parts[2]));
                                if (tipoColisao == 0)
                                    tipoColisao = 2;

                                resp.setPerformative(ACLMessage.INFORM);
                                resp.setContent("Colisao;" + tipoColisao);
                                myAgent.send(resp);

                            }
                        } else if (parts[0].equals("Colisao")) { // Mensagem recebida Colisão -> enviar as infos
                            System.out.println("Detetada Colisão.");
                            if (Integer.parseInt(msg.getSender().getLocalName().split("_")[1]) < id)
                                master = true;
                            else master = false;

                            if (master == false) {
                                resp.setPerformative(ACLMessage.INFORM);
                                resp.setContent("Informacao;" + passageiros + ";" + nColisoes + ";" + distanciaPercorrida + ";" +
                                        movimentacao.getVelocidade() + ";" + combustivel + ";" + localizacao.x + ";" +
                                        localizacao.y + ";" + companhia + ";" + parts[1]);
                                myAgent.send(resp);
                            }
                        } else if (parts[0].equals("InformDirecao")) { //Mensagem recebida InforDirecao -> Verificar se ha colisao

                            int tipoColisao = existePossibilidadeColisao(msg.getSender().getLocalName(),
                                    Double.parseDouble(parts[1]),
                                    Double.parseDouble(parts[2]));
                            if (tipoColisao > 0) {
                                resp.setPerformative(ACLMessage.INFORM);
                                resp.setContent("Colisao;" + tipoColisao);
                                myAgent.send(resp);
                            }


                        } else if (parts[0].equals("Informacao")) { //Mensagem recebida Informacao -> receber os dados de AA2 e calcular melhor Alternativa
                            int melhorAlternativa = calculaMelhorAlternativa(parts);
                            switch (melhorAlternativa) {
                                case 1:
                                    alterarRota();
                                    break;
                                case 2:
                                    resp.setPerformative(ACLMessage.REQUEST);
                                    resp.setContent("AlteraRota");
                                    myAgent.send(resp);
                                    break;
                                case 3:
                                    resp.setPerformative(ACLMessage.REQUEST);
                                    resp.setContent("DiminuiVelocidade");
                                    myAgent.send(resp);
                                    aumentaVelocidade();
                                    break;
                                case 4:
                                    resp.setPerformative(ACLMessage.REQUEST);
                                    resp.setContent("AumentaVelocidade");
                                    myAgent.send(resp);
                                    diminuiVelocidade();
                                    break;

                            }

                            /**
                             * 1 - Alterar rota A1
                             * 2 - Alterar rota A2
                             * 3 - Aumentar velocidade A1 e diminuir A2
                             * 4 - Aumentar velocidade A2 e diminuir A3
                             * */

                        }

                    } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                        String[] parts = msg.getContent().split(";");
                        if (msg.getContent().equals("RequestDirecao")) { //Mensagem recebida for RequestDirecao -> enviar a direcao.
                            resp.setPerformative(ACLMessage.INFORM);
                            resp.setContent("InformDirecao;" + movimentacao.getX() + ";" + movimentacao.getY());
                            myAgent.send(resp);
                        } else if (msg.getContent().equals("AlteraRota")) {
                            resp.setPerformative(ACLMessage.AGREE);
                            resp.setContent("RotaAlterada");
                            myAgent.send(resp);
                            alterarRota();
                        } else if (msg.getContent().equals("DiminuiVelocidade")) {
                            System.out.println("Vou diminuir");
                            resp.setPerformative(ACLMessage.AGREE);
                            resp.setContent("VelocidadeDiminuida");
                            myAgent.send(resp);
                            diminuiVelocidade();
                        } else if (msg.getContent().equals("AumentaVelocidade")) {
                            System.out.println("Vou aumentar!!!");
                            resp.setPerformative(ACLMessage.AGREE);
                            resp.setContent("VelocidadeAumentada");
                            myAgent.send(resp);
                            aumentaVelocidade();
                        }

                    } else if (msg.getPerformative() == ACLMessage.AGREE) {
                        if (msg.getContent().equals("VelocidadeAumentada")) {
                            diminuiVelocidade();
                        } else if (msg.getContent().equals("VelocidadeDiminuida")) {
                            aumentaVelocidade();
                        }
                    }
                    }
                }
                block();
        }

        private int calculaMelhorAlternativa(String[] parts) {
            /**
             * 1 - Alterar rota A1
             * 2 - Alterar rota A2
             * 3 - Aumentar velocidade A1 e diminuir A2
             * 4 - Aumentar velocidade A2 e diminuir A3
             *
             * */
            int result = 1;

            int a2_passageiros,a2_colisoes, tipoColisao;
        /*    double x_aa2;
            double y_aa2;
            x_aa2=Double.parseDouble(parts[6]);
            y_aa2=Double.parseDouble(parts[7]);
            */
            double a2_distanciaPercorrida,a2_velocidade,a2_combustivel;
            Localizacao a2;
            String a2_companhia;


            a2_passageiros = Integer.parseInt(parts[1]);
            a2_colisoes = Integer.parseInt(parts[2]);
            a2_distanciaPercorrida=Double.parseDouble(parts[3]);
            a2_velocidade=Double.parseDouble(parts[4]);
            a2_combustivel=Double.parseDouble(parts[5]);
            a2_companhia = parts[8];
            tipoColisao = Integer.parseInt(parts[9]);

            double peso1 = 0;
            double peso2 = 0;

            // Tipo colisão 1 -> Colisão "cruz"
            // Tipo colisão 2 -> Colisão "em frente"

            if (tipoColisao == 1){
                if (passageiros > a2_passageiros) peso1 += 0.1; else if(passageiros < a2_passageiros) peso2 += 0.1;
                if (nColisoes > a2_colisoes) peso1 += 0.2; else if(nColisoes < a2_colisoes) peso2 += 0.2;
                if (distanciaPercorrida > a2_distanciaPercorrida) peso1 += 0.1; else if(distanciaPercorrida < a2_distanciaPercorrida) peso2 += 0.1;
                if (combustivel < a2_combustivel) peso1 += 0.15; else if(combustivel > a2_combustivel) peso2 += 0.15;
                if (movimentacao.getVelocidade() > a2_velocidade) peso1 += 0.45; else if(movimentacao.getVelocidade() < a2_velocidade) peso2 += 0.45;

                result = (peso1 >= peso2) ?  3 : 4 ; // Alterar velocidades

            }
            else if (tipoColisao == 2) {
                if (passageiros > a2_passageiros) peso1 += 0.2; else if(passageiros < a2_passageiros) peso2 += 0.2;
                if (nColisoes > a2_colisoes) peso1 += 0.3; else if(nColisoes < a2_colisoes) peso2 += 0.3;
                if (distanciaPercorrida > a2_distanciaPercorrida) peso1 += 0.3; else if(distanciaPercorrida < a2_distanciaPercorrida) peso2 += 0.3;
                if (combustivel < a2_combustivel) peso1 += 0.15; else if(combustivel > a2_combustivel) peso2 += 0.15;
                if (movimentacao.getVelocidade() > a2_velocidade) peso1 += 0.05; else if(movimentacao.getVelocidade() < a2_velocidade) peso2 += 0.05;

                result = (peso1 >= peso2) ?  1 : 2 ; // Alterar rotas
            }

            return result;


        }

        private int existePossibilidadeColisao(String nomeAeronave, double movX, double movY){
            /**
             * Tipo colisão = 0 -> sem colisão
             * Tipo colisão = 1 -> colisão em "X"
             * Tipo colisão = 2 -> colisão em " frente "
             * */
             int tipoColisao = 0;
            if(permissaoDescolagem){


            Localizacao l = aeronavesAlert.get(nomeAeronave);

            double m1,m2;
            m1 = movimentacao.getY()/movimentacao.getX();
            m2 = movY/movX;

            double b1 = localizacao.y - m1*localizacao.x;
            double b2 = l.y - m2*l.x;
            

            if (m1 == m2 ){
                if (Math.round(b1) == Math.round(b2))
                    tipoColisao = 2;
            }
            else {
                double x_colisao;
                double y_colisao;

                x_colisao = (b2-b1)/(m1+m2);
                y_colisao = m1*x_colisao + b1;

                double dirCol_x = x_colisao - localizacao.x;
                double dirCol_y = y_colisao - localizacao.y;

                if( dirCol_x * localizacao.x < 0 || dirCol_y * localizacao.y < 0) tipoColisao = 0;

                else if ( calculaDistancia(localizacao.x,localizacao.y, x_colisao, y_colisao,0).equals("alert") )
                    tipoColisao = 1;

            }
            }
            return tipoColisao;
        
        }
    }

    private class preparaAterragem extends TickerBehaviour{

       public preparaAterragem(Agent a, long period) {
           super(a, period);
       }

       @Override
       protected void onTick() {
           if(!estacionado){
                ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
                Localizacao l = new Localizacao(destino.x,destino.y);
                double tempoChegada = calculaTempoChegada();

                String aeroporto=null;
                Iterator it = ambiente.aeroportos.entrySet().iterator();
                //Descobrir o nome do aeroporto destino
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if(pair.getValue().equals(l)) aeroporto = (String) pair.getKey();
                }

                AID receiver = new AID(aeroporto,AID.ISLOCALNAME);
                msg1.addReceiver(receiver);

                if (calculaDistancia(localizacao.x, localizacao.y, destino.x, destino.y,1).equals("protected")){
                    if(!tempoProtectedEnviado){
                    msg1.setContent("TempoChegadaProtected;" + myAgent.getLocalName() +";"+tempoChegada);
                    myAgent.send(msg1);
                    tempoProtectedEnviado=true;
                    }
                    
                }
                else if (calculaDistancia(localizacao.x, localizacao.y, destino.x, destino.y,1).equals("alert")){
                    if (informacoesEnviadas){
                        msg1.setContent("TempoChegadaAlert;" + myAgent.getLocalName() +";"+tempoChegada);
                        myAgent.send(msg1);
                    } else {
                        informacoesEnviadas = true;
                        msg1.setContent("Informacao;" +myAgent.getLocalName()+";"+ passageiros + ";" + nColisoes + ";" + distanciaPercorrida + ";" +
                                combustivel + ";" + companhia +";"+ movimentacao.getVelocidade());
                        myAgent.send(msg1);
                    }
                }
                else if(calculaDistancia(localizacao.x, localizacao.y, destino.x, destino.y,1).equals("request")){
                    msg1.setContent("TempoChegada;" + myAgent.getLocalName() +";"+tempoChegada);
                     myAgent.send(msg1);

                }
           }
       }
   }

  
    

    public String calculaDistancia(double x_aa1,double y_aa1,double x_aa2,double y_aa2, int tipo){

        double pZone, aZone, rZone;

        aZone = (tipo == 0) ? alertZone : aeroportoAlertZone;
        pZone = (tipo == 0) ? protectedZone : aeroportoProtectedZone;
        rZone = (tipo == 0) ? 0 : aeroportoRequestZone;

        if (( Math.sqrt( Math.pow(x_aa2-x_aa1,2) + Math.pow(y_aa1 - y_aa2,2)) ) < pZone)
            return "protected";
        else
        if (( Math.sqrt( Math.pow(x_aa2-x_aa1,2) + Math.pow(y_aa1 - y_aa2,2)) ) < aZone)
            return "alert";
        else if (( Math.sqrt( Math.pow(x_aa2-x_aa1,2) + Math.pow(y_aa1 - y_aa2,2)) ) < rZone){
            System.out.println("REQUEST ZONE!!!!");
            return "request";
        }

        else return "free";
    }
    
    private void alterarRota() {
        double alfa = Math.atan(movimentacao.getX()/movimentacao.getY());

        this.movimentacao.setX(Math.cos(alfa + Math.PI/2));
        this.movimentacao.setY(Math.sin(alfa + Math.PI/2));

        double locInicialX = localizacao.x;
        double locInicialY = localizacao.y;

            localizacao.x += movimentacao.getX() * movimentacao.getVelocidade();
            localizacao.y += movimentacao.getY() * movimentacao.getVelocidade();

            distanciaPercorrida += Math.sqrt(Math.pow(movimentacao.getX() * movimentacao.getVelocidade(), 2) +
                    Math.pow(movimentacao.getY() * movimentacao.getVelocidade(), 2));



        double distanciaX = destino.x-localizacao.x;
            distanciaX = (distanciaX == 0) ? 1 : distanciaX;
        double distanciaY = destino.y-localizacao.y;
            distanciaY = (distanciaY == 0) ? 1 : distanciaY;
        double modulo = calculaModulo(distanciaX,distanciaY);

        movimentacao.setX (distanciaX/modulo);
        movimentacao.setY(distanciaY/modulo);

        }
    
    private double calculaModulo (double distanciaX, double distanciaY){
        return Math.sqrt(distanciaX*distanciaX + distanciaY*distanciaY);
    }
    
    private void aumentaVelocidade() {
        movimentacao.setVelocidade(movimentacao.getVelocidade() * 1.1);
    }

    private void diminuiVelocidade() {

        movimentacao.setVelocidade(movimentacao.getVelocidade() * 0.8);
    }

    private double calculaTempoChegada() {
        double result = Math.sqrt(
                                Math.pow((destino.x-localizacao.x),2)
                              + Math.pow((destino.y-localizacao.y),2)
                                ) / movimentacao.getVelocidade();

        return result;
    }

}
