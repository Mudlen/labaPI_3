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
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Client extends JFrame {
    String msg;
    byte[] byteKey;
    final String keyGeneration = "KEY_GENERATION";
    final String message = "MESSAGE";
    final int port = 1777;
    JTextArea textArea;
    String str;
    public Client(){
        super("Client");
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
        textArea.append("                 [client]"+"\n"+"\n");
        textArea.setEditable(false);
        textArea.setFont(new Font("Lucida Console",Font.BOLD,20));
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setBackground(new Color(20, 19, 19));
        scrollPane.getVerticalScrollBar().setUI(new CustomSBUI());

        JButton getKey = new JButton
                ("<html>Request<p>а key</html>");
        getKey.setBackground(new Color(20, 19, 19));
        getKey.setForeground(new Color(2, 161, 7));
        getKey.setBorder(BorderFactory.createLineBorder(Color.white,2));
        getKey.setFont(new Font("Lucida Console",Font.BOLD,15));
        getKey.setPreferredSize(new Dimension(100,50));
        getKey.addActionListener(e -> {
            msg = keyGeneration;
            try {
                connect(keyGeneration);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        JTextField writing = new JTextField();
        writing.setBackground(new Color(20, 19, 19));
        writing.setForeground(new Color(2, 161, 7));
        writing.setBorder(BorderFactory.createLineBorder(Color.white,2));
        writing.setFont(new Font("Lucida Console",Font.BOLD,15));
        writing.setPreferredSize(new Dimension(350,50));

        JButton send = new JButton
                ("<html>Send<p>message</html>");
        send.setBackground(new Color(20, 19, 19));
        send.setForeground(new Color(2, 161, 7));
        send.setBorder(BorderFactory.createLineBorder(Color.white,2));
        send.setFont(new Font("Lucida Console",Font.BOLD,15));
        send.setPreferredSize(new Dimension(100,50));
        send.addActionListener(e -> {
            msg = message;
            try {
                str = writing.getText();
                try {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(byteKey));
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.ENCRYPT_MODE,publicKey);
                    msg = message+Base64.getEncoder()
                            .encodeToString(cipher
                                    .doFinal(str.getBytes(StandardCharsets.UTF_8)));
                    String s = Base64.getEncoder()
                            .encodeToString(cipher
                                    .doFinal(str.getBytes(StandardCharsets.UTF_8)));
                    textArea.append("[client]: message to sent:"+ str +"\n");
                    textArea.append("[client]: encrypted message sent: "+s+"\n");
                } catch (NoSuchAlgorithmException
                        | NoSuchPaddingException
                        | InvalidKeySpecException
                        | InvalidKeyException
                        | BadPaddingException
                        | IllegalBlockSizeException ex) {
                    ex.printStackTrace();
                }
                connect(message);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        panel.add(scrollPane);
        panel.add(getKey);
        panel.add(writing);
        panel.add(send);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client();
    }

    public void connect(String btn) throws IOException {
        System.out.println("Client is started");
        Socket socket = new Socket("127.0.0.1",port);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
        pw.println(msg);//отправка на сервер сообщения
        while ((msg = br.readLine()) != null){
            if(msg.equals("Disconnect")){
                System.out.println("Disconnect from server");
                break;
            }
            System.out.println(msg);//получение сообщения от сервера
            if(btn.equals(keyGeneration)) {
                int pos = msg.indexOf("!") + 1;
                String sub_str = msg.substring(0, pos);
                textArea.append(sub_str + "\n");
                byteKey = getByteKey(msg, "!");
                pw.println("Disconnect");
            }
            if(btn.equals(message)){
                pw.println("Disconnect");
            }
        }
        br.close();
        pw.close();
        socket.close();
    }
    public byte[] getByteKey(String str,String separator){
        int pos = str.indexOf(separator)+1;
        String sub_str = str.substring(pos);
        sub_str = sub_str.substring(1,sub_str.length()-1);
        sub_str = sub_str.replaceAll("\\s","");
        String[] stringsByte = sub_str.split(",");
        byte[] keyBytes = new byte[stringsByte.length];
        for(int i = 0; i < keyBytes.length; i++){
            keyBytes[i] = Byte.parseByte(stringsByte[i]);
        }
        System.out.println(Arrays.toString(keyBytes));
        return keyBytes;
    }

}
