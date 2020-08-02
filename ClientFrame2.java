import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientFrame2 extends Frame{
    public ClientFrame2(){
        setSize(500,500);
        setTitle("Chat Client");
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent We){
                System.exit(0);
            }
        });
        add(new ClientPanel2(this), BorderLayout.CENTER);
        setVisible(true);
    }
    public static void main(String[] args){
        new ClientFrame2();
    }
}

class ClientPanel2 extends Panel implements ActionListener, Runnable {
    TextField tf;
    TextArea ta;
    List list;
    Button connect, disconnect;
    Socket socketToServer;
    PrintWriter pw;
    BufferedReader br;
    Thread t;
    String userName;
    Frame mainFrame;
    String choice;
    Boolean privateflag;

    public ClientPanel2(Frame mainFrame){
        privateflag = true;
        setLayout(new BorderLayout());
        choice="0";
        this.mainFrame = mainFrame;
        tf = new TextField();
        ta = new TextArea();
        list = new List();
        connect = new Button("Connect");
        disconnect = new Button("Disconnect");
        Panel bPanel = new Panel();
        bPanel.add(connect);
        bPanel.add(disconnect);
        list.addActionListener( this);
        tf.addActionListener(this);
        connect.addActionListener(this);
        disconnect.addActionListener(this);
        add(tf, BorderLayout.NORTH);
        add(ta, BorderLayout.CENTER);
        add(list, BorderLayout.EAST);
        add(bPanel, BorderLayout.SOUTH);

    }
    public void actionPerformed(ActionEvent ae){
        //checking which button is pressed
        //message send pressed enter
        if (ae.getSource() == tf){
            //getting the input text and sending it to the server
            String temp = tf.getText();
            if(!temp.isEmpty()) {
                pw.println(choice);
                pw.println(temp);
                tf.setText("");
            }
            else{

            }
        }
        //connect button pressed
        else if (ae.getSource() == connect){
            userName = tf.getText();
            if(!userName.isEmpty()){
                //connecting to server
                startConnection();
                t = new Thread(this);
                t.start();
                tf.setText("");
                pw.println("0");
                pw.println(userName);
                mainFrame.setTitle(userName);
            }
            else {
                ta.append("Enter the Username and then connect to server." + "\n");
            }
        }
        //disconnect button pressed
        else if (ae.getSource() == disconnect){
            pw.println("0");
            pw.println("disconnect client");
            stopConnection();
            list.removeAll();
            ta.setText("");
        }
        else if(ae.getSource() == list){
            String recievername = list.getSelectedItem();
            ta.setText("");
            ta.setBackground(Color.lightGray);
            //sending the private message
            ta.append("You are now sending message privately to "+recievername+ "\n");
            int privateid= list.getSelectedIndex();
            choice= "1";
            pw.println(choice);
            pw.println(privateid);
            choice="3";
            list.removeAll();
            list.add(recievername);
        }

    }

    public void startConnection(){
        try{
            socketToServer = new Socket("127.0.0.1", 3033);
            pw = new PrintWriter(new OutputStreamWriter	(socketToServer.getOutputStream()), true);
            br = new BufferedReader(new InputStreamReader (socketToServer.getInputStream()));
        }catch(UnknownHostException uhe){
            System.out.println(uhe.getMessage());
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }
    public void stopConnection() {
        try {
            //dissconnecting the user
            socketToServer.close();
            pw.close();
            br.close();

        }
        catch(UnknownHostException uhe){
            System.out.println(uhe.getMessage());
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }

    public void run(){
        for(;;){
            try{
                String temp = br.readLine();
                String user = br.readLine();

                if(!user.equals("null")) {
                    ArrayList <String> ne = new ArrayList<String>(Arrays.asList(user.split(",")));
                    //checking if it is a private message
                    if (ne.get(0).equals("private")){

                        ta.setBackground(Color.lightGray);
                        list.removeAll();
                        list.add(ne.get(2));
                        list.setForeground(Color.blue);
                        if (privateflag){
                            ta.append("-------------------------------------------\n");
                            ta.append("You are now chatting privately with "+ ne.get(2)+"\n");
                            privateflag=false;
                        }
                        choice="3";
                    }
                    else{
                        list.removeAll();
                        for (int i =0; i< ne.size(); i++)
                            list.add(ne.get(i));
                    }

                }
                //appending chat to text area
                ta.append(temp + "\n");
            }catch(IOException ioe){
                System.out.println(ioe.getMessage());
            }
        }
    }


}
