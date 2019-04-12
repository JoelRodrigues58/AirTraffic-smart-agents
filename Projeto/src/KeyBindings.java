import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public class KeyBindings extends JFrame {

    private static final int D_W = 1920;
    private static final int D_H = 1080;

    private HashMap<String,Localizacao> aeronaves;
    private Ambiente ambiente;
    public HashMap<String,Movimentacao> direcoes_aeronaves;
    public ArrayList<Localizacao> rotas; 
    BufferedImage aeronave; 
    BufferedImage aeroporto; 
    BufferedImage mapa;

    DrawPanel drawPanel = new DrawPanel();

    public KeyBindings(Ambiente ambiente,HashMap<String,Localizacao> aeronaves,HashMap<String,Movimentacao> direcoes_aeronaves) {
        try {
            this.aeronaves=aeronaves;
            this.ambiente=ambiente;
            this.direcoes_aeronaves=direcoes_aeronaves;
            this.rotas= new ArrayList<Localizacao>();
            
            aeronave = ImageIO.read(new File("imagens/aeronave.png"));
            aeroporto =  ImageIO.read(new File("imagens/aeroporto.png"));
            mapa =ImageIO.read(new File("imagens/mapa.png"));
            
            ActionListener listener = new AbstractAction() {
                
                public void actionPerformed(ActionEvent e) {
                }
            };
            
            Timer timer = new Timer(10, listener);
            timer.start();
            add(drawPanel);
            
            pack();
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(KeyBindings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class DrawPanel extends JPanel {
        
        //Pode-se apagar isto
        /*public BufferedImage resize(BufferedImage img, int height, int width) {
            Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            return resized;
        }*/

        
        protected synchronized void paintComponent(Graphics g) {
            super.paintComponent(g);            
                Image aeroporto_recized = aeroporto.getScaledInstance( 50, 50,  Image.SCALE_SMOOTH ) ;
                Image aeronave_recized = aeronave.getScaledInstance( 24, 24,  Image.SCALE_SMOOTH ) ;

                g.drawImage(mapa,0,0, 1920,1080,this);

                Graphics2D g2 = (Graphics2D) g;
                
                
                if(aeronaves.size()!=0){
                    for(Localizacao l : aeronaves.values()){
                        String aviao=null;
                        rotas.add(l);
                            for(String a : aeronaves.keySet()){
                                if(aeronaves.get(a).equals(l))  aviao=a;
                            }
                            double teta = Math.toDegrees(Math.atan(direcoes_aeronaves.get(aviao).getY()/direcoes_aeronaves.get(aviao).getX()));
                          
                            AffineTransform old = g2.getTransform();
                            g2.translate((int)l.getX(), (int)l.getY());

                            if(direcoes_aeronaves.get(aviao).getX()<=0){
 
                                g2.rotate(Math.PI+Math.toRadians(teta));
                            }
                            
                            else g2.rotate(Math.toRadians(teta));
                            g2.drawImage(aeronave_recized, -12 , -12, null);  
                        
                         g2.setTransform(old);
                        
                        //g.drawImage(aeronave_recized, (int)l.getX()-12 , (int)l.getY()-12, null);

                    }
                }
                
                //isto foi para ver se se estavam a desviar. Puxa pelo PC.
                for(Localizacao l2 : rotas){
                    g.drawOval((int)l2.x,(int)l2.y,1, 1);
                }
                
                ambiente.aeroportos.forEach((k, a) -> {
                    g.drawImage(aeroporto_recized,(int) a.getX()-25,(int) a.getY()-25, null);
                    g.setColor(Color.blue);
                    g.drawOval((int)a.getX()-120,(int)a.getY()-120,240,240);

                    g.setColor(Color.green);
                    g.drawOval((int)a.getX()-75,(int)a.getY()-75,150,150);

                    g.setColor(Color.red);
                    g.drawOval((int)a.getX()-40,(int)a.getY()-40,80,80);

                    drawPanel.repaint();
                });
               
            
        }
        

        public Dimension getPreferredSize() {
            return new Dimension(D_W, D_H);
        }
    }

   
}
