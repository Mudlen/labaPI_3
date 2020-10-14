import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Server extends JFrame {
    final int port = 1777;
    static final String keyGeneration = "KEY_GENERATION";
    static final String message = "MESSAGE";
    static JTextArea textArea;
    static String msg;
    static GenerationKey generationKey ;

    public Server(){
        super("Server");
        setSize(600,600);
        setLocation(100,100);
        setResizable(false);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        panel.setBackground(Color.BLACK);
        getContentPane().add(panel);

        textArea = new JTextArea(18,40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(20, 19, 19));
        textArea.setForeground(new Color(2, 161, 7));
        textArea.setMargin(new Insets(10,5,5,5));
        textArea.append("                 [server]"+"\n"+"\n");
        textArea.setEditable(false);
        textArea.setFont(new Font("Lucida Console",Font.BOLD,20));
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setBackground(new Color(20, 19, 19));
        scrollPane.getVerticalScrollBar().setUI(new CustomSBUI());

        JButton readMSG = new JButton
                ("<html>Decrypt<p>message</html>");
        readMSG.setBackground(new Color(20, 19, 19));
        readMSG.setForeground(new Color(2, 161, 7));
        readMSG.setBorder(BorderFactory.createLineBorder(Color.white,2));
        readMSG.setFont(new Font("Lucida Console",Font.BOLD,15));
        readMSG.setPreferredSize(new Dimension(100,50));
        readMSG.addActionListener(e -> {
            try {
                String decrypt = decryptMSG(msg);
                textArea.append("[server]: decrypted message : "+decrypt);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException
                    | InvalidKeyException | BadPaddingException
                    | IllegalBlockSizeException noSuchPaddingException) {
                noSuchPaddingException.printStackTrace();
            }
        });

        panel.add(scrollPane);
        panel.add(readMSG);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args){
        connect(1777);
    }

    public static void connect( int port){
        Server server = new Server();
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                textArea.append("[server]: Waiting for a connection on port: "+port+"\n");
                try(Socket localSocket = serverSocket.accept();
                    PrintWriter pw = new PrintWriter(localSocket.getOutputStream(),true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(localSocket.getInputStream())))
                {
                    String str;
                    while ((str = br.readLine()) != null){
                        if(str.length() > 20){
                            msg = str.substring(message.length());
                            str = str.substring(0,message.length());
                        }
                        //то что нам пришло от клиента выводим в консоль
                        textArea.append("[client]: The message: " + str+"\n");
                        if(str.equals("Disconnect")){
                            pw.println("Disconnect");
                            break;
                        }else {
                            //то что сервер возвращает клиенту
                            if(str.equals(keyGeneration)){
                                generationKey = new GenerationKey(1024);
                                str = "[server]: public key sent!"+ Arrays
                                        .toString(generationKey.getPublicKey().getEncoded());
                                textArea.append("[server]: key created successfully!"+"\n");
                                pw.println(str);

                            }else if(str.equals(message)){

                                textArea.append("[server]: encrypted message received: "+msg+"\n");
                                pw.println(str);
                            }
                        }
                    }
                } catch (IOException ex){
                    ex.printStackTrace(System.out);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException ex){
            ex.printStackTrace(System.out);
        }
    }

    public String decryptMSG(String encryptedMSG) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,generationKey.getPrivateKey());
        return new String(cipher.doFinal(Base64.getDecoder().
                decode(encryptedMSG)),StandardCharsets.UTF_8);
    }
}
