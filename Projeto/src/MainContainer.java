import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 */

/**
 * @author Filipe Gonçalves
 *
 */
public class MainContainer {

    Runtime rt;
    ContainerController container;

    public ContainerController initContainerInPlatform(String host, String port, String containerName) {
        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, containerName);
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        // create a non-main agent container
        ContainerController container = rt.createAgentContainer(profile);
        return container;
    }

    public void initMainContainerInPlatform(String host, String port, String containerName) {

        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile prof = new ProfileImpl();
        prof.setParameter(Profile.CONTAINER_NAME, containerName);
        prof.setParameter(Profile.MAIN_HOST, host);
        prof.setParameter(Profile.MAIN_PORT, port);
        prof.setParameter(Profile.MAIN, "true");
        prof.setParameter(Profile.GUI, "true");

        // create a main agent container
        this.container = rt.createMainContainer(prof);
        rt.setCloseVM(true);
    }

    public void startAgentInPlatform(String name, String classpath, Object[] argumentos) {
        try {
            AgentController ac = container.createNewAgent(name, classpath, argumentos);
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        MainContainer a = new MainContainer();
        a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
        

        
        //========================================AEROPORTOS================================================

        /**
         * Aeroporto
         * arg1 = localizacao;
         * arg2 = pistas;
         * arg3 = limite_AA_estacionadas
         */



        
        //Aeroporto1
        List aeroporto1 = new ArrayList<Object>();
        Localizacao l1 = new Localizacao(904,601); // África
        aeroporto1.add(l1);
        aeroporto1.add(10);
        aeroporto1.add(80);
        List<String> aeronavesEstacionadas_1 = new ArrayList<>();
        
        //Aeroporto2
        List aeroporto2 = new ArrayList<Object>();
        Localizacao l2 = new Localizacao(782,324); // Espanha
        aeroporto2.add(l2);
        aeroporto2.add(10);
        aeroporto2.add(50);
        List<String> aeronavesEstacionadas_2 = new ArrayList<>();
        
        //Aeroporto3
        List aeroporto3 = new ArrayList<Object>();
        Localizacao l3 = new Localizacao(291,304); // América Norte
        aeroporto3.add(l3);
        aeroporto3.add(10);
        aeroporto3.add(50);
        List<String> aeronavesEstacionadas_3 = new ArrayList<>();

        //Aeroporto4
        List aeroporto4 = new ArrayList<Object>();
        Localizacao l4 = new Localizacao(545,702); // Brasil
        aeroporto4.add(l4);
        aeroporto4.add(10);
        aeroporto4.add(50);
        List<String> aeronavesEstacionadas_4 = new ArrayList<>();

        //Aeroporto5
        List aeroporto5 = new ArrayList<Object>();
        Localizacao l5 = new Localizacao(1196,285); // Rússia
        aeroporto5.add(l5);
        aeroporto5.add(10);
        aeroporto5.add(50);
        List<String> aeronavesEstacionadas_5 = new ArrayList<>();

        //Aeroporto6
        List aeroporto6 = new ArrayList<Object>();
        Localizacao l6 = new Localizacao(1557, 803); // Austrália
        aeroporto6.add(l6);
        aeroporto6.add(10);
        aeroporto6.add(50);
        List<String> aeronavesEstacionadas_6 = new ArrayList<>();


        List<String> nomesAeroportos = new ArrayList<>();
        nomesAeroportos.add("Aeroporto_1");
        nomesAeroportos.add("Aeroporto_2");
        nomesAeroportos.add("Aeroporto_3");
        nomesAeroportos.add("Aeroporto_4");
        nomesAeroportos.add("Aeroporto_5");
        nomesAeroportos.add("Aeroporto_6");

        //==================================================================================================

        //======================================AMBIENTE====================================================
        HashMap<String,Localizacao> aeroportos = new HashMap<String,Localizacao>();
        aeroportos.put("Aeroporto_1",l1);
        aeroportos.put("Aeroporto_2",l2);
        aeroportos.put("Aeroporto_3",l3);
        aeroportos.put("Aeroporto_4",l4);
        aeroportos.put("Aeroporto_5",l5);
        aeroportos.put("Aeroporto_6",l6);


        Ambiente ambiente = new Ambiente(aeroportos);

        //Enviar o ambiente para os aeroportos VER SE FAZ FALTA
        aeroporto1.add(ambiente);
        aeroporto2.add(ambiente);
        aeroporto3.add(ambiente);
        aeroporto4.add(ambiente);
        aeroporto5.add(ambiente);
        aeroporto6.add(ambiente);
        //==================================================================================================

        //=======================================AVIÕES====================================================
        /** Aeronave
         *  arg1 = localizacao;
         *  arg2 = passageiros;
         *  arg3 = companhia;
         *  arg4 = combustivel;
         *  arg5 = ambiente;
         *  arg6 = id;
         * */

        List<String> companhias = new ArrayList<>();
        companhias.add("Raynair");
        companhias.add("TEP");
        companhias.add("Fly Debagar");
        companhias.add("Turkish Erlaines");

        List<Localizacao> localizacaos = new ArrayList<>();
        localizacaos.add(l1);
        localizacaos.add(l2);
        localizacaos.add(l3);
        localizacaos.add(l4);
        localizacaos.add(l5);
        localizacaos.add(l6);

        for (int i = 1; i <= 10; i++){
            Random r = new Random();

            String nomeAeronave = "Aeronave_" + i;
            List aeronave = new ArrayList<Object>();
            int aeroporto = r.nextInt(localizacaos.size());


            aeronave.add((Localizacao) localizacaos.get(aeroporto).clone());
            aeronave.add(100+r.nextInt(200));
            aeronave.add(companhias.get(r.nextInt(companhias.size()-1)) );
            aeronave.add(  (double) (100+r.nextInt()) );
            aeronave.add(ambiente);
            aeronave.add(i);

            switch (aeroporto) {
                case 0:
                    aeronavesEstacionadas_1.add(nomeAeronave);
                    break;
                case 1:
                    aeronavesEstacionadas_2.add(nomeAeronave);
                    break;
                case 2:
                    aeronavesEstacionadas_3.add(nomeAeronave);
                    break;
                case 3:
                    aeronavesEstacionadas_4.add(nomeAeronave);
                    break;
                case 4:
                    aeronavesEstacionadas_5.add(nomeAeronave);
                    break;
                case 5:
                    aeronavesEstacionadas_6.add(nomeAeronave);
                    break;
                default:
                    break;
            }

            a.startAgentInPlatform(nomeAeronave, "Aeronave",aeronave.toArray());
        }
        aeroporto1.add(aeronavesEstacionadas_1);
        aeroporto2.add(aeronavesEstacionadas_2);
        aeroporto3.add(aeronavesEstacionadas_3);
        aeroporto4.add(aeronavesEstacionadas_4);
        aeroporto5.add(aeronavesEstacionadas_5);
        aeroporto6.add(aeronavesEstacionadas_6);


         //Inserir as aeronaves
    /*    a.startAgentInPlatform("Aeronave_1", "Aeronave", aeronave1.toArray());
        a.startAgentInPlatform("Aeronave_2", "Aeronave", aeronave2.toArray());
        a.startAgentInPlatform("Aeronave_3", "Aeronave", aeronave3.toArray());
        a.startAgentInPlatform("Aeronave_4", "Aeronave", aeronave4.toArray());
    */
        //Inserir os Aeroportos
        a.startAgentInPlatform("Aeroporto_1", "Aeroporto", aeroporto1.toArray());
        a.startAgentInPlatform("Aeroporto_2", "Aeroporto", aeroporto2.toArray());
        a.startAgentInPlatform("Aeroporto_3", "Aeroporto", aeroporto3.toArray());
        a.startAgentInPlatform("Aeroporto_4", "Aeroporto", aeroporto4.toArray());
        a.startAgentInPlatform("Aeroporto_5", "Aeroporto", aeroporto5.toArray());
        a.startAgentInPlatform("Aeroporto_6", "Aeroporto", aeroporto6.toArray());


        List ambienteList = new ArrayList<Object>();
        ambienteList.add(ambiente);
        a.startAgentInPlatform("Interface", "Interface", ambienteList.toArray());

    }
}
