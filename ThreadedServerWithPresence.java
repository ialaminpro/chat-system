import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ThreadedServerWithPresence{
    public static void main(String[] args ){
        ArrayList<ThreadedHandlerWithPresence> handlers;
        ArrayList<String> userList;
        try{
            handlers = new ArrayList<ThreadedHandlerWithPresence>();
            userList = new ArrayList<String>();
            ServerSocket s = new ServerSocket(3033);
            for(;;){
                Socket incoming = s.accept( );
                new ThreadedHandlerWithPresence(incoming, handlers, userList).start();
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}

class ThreadedHandlerWithPresence extends Thread{

    Socket incoming;
    ArrayList<ThreadedHandlerWithPresence> handlers;
    PrintWriter pw;
    BufferedReader br;
    String userName;
    ArrayList<String> userList;
    int private_message_id;

    public ThreadedHandlerWithPresence(Socket i, ArrayList<ThreadedHandlerWithPresence> handlers, ArrayList<String> userList){
        incoming = i;
        this.handlers = handlers;
        this.userList= userList;
        handlers.add(this);
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public String getUserName(){
        return userName;
    }
    public String converttoString(ArrayList<String> u){
        return Arrays.toString(u.toArray()).replace("[", "").replace("]", "");
    }

    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            Boolean userNameFlag = true;
            pw = new PrintWriter(new OutputStreamWriter(incoming.getOutputStream()),true);

            while(true) {
                String choice = br.readLine();
                //System.out.println(choice);
                String temp = br.readLine();
                if (choice.equals("0")){
                    ///) means normal chat room
                    if (temp.equals("disconnect client")) {
                        //discconecting the client
                        userNameFlag = true;
                        String tem = getUserName() + " has left the room.";
                        System.out.println(temp);
                        userList.remove(getUserName());
                        //sending the chat message
                        for (int i = 0; i < handlers.size(); i++) {
                            handlers.get(i).pw.println(tem);
                            handlers.get(i).pw.println(converttoString(userList));
                        }
                        break;
                    } else {

                        if (userNameFlag) {
                            setUserName(temp);
                            temp = getUserName() + " has entered the room.";
                            //System.out.println(temp);
                            userList.add(getUserName());
                            for (int i = 0; i < handlers.size(); i++) {
                                System.out.println(getUserName() + ": " + i);
                                handlers.get(i).pw.println(temp);
                                handlers.get(i).pw.println(converttoString(userList));
                            }

                            userNameFlag = false;
                        } else {
                            System.out.println(getUserName() + ": " + temp);

                            for (int i = 0; i < handlers.size(); i++) {
                                handlers.get(i).pw.println(getUserName() + " : " + temp);
                                handlers.get(i).pw.println((String) null);
                            }
                        }
                    }
                }
                else if(choice.equals("1")){
                    //setting the private reciever
                    //after this user can chat privately with the reciever
                    private_message_id = Integer.valueOf(temp);
                    System.out.println(private_message_id);

                }
                else if(choice.equals("3")){
                    //sending private message
                    System.out.println(getUserName() + ": " + temp);
                    handlers.get(private_message_id).pw.println(getUserName() + " : " + temp);
                    ArrayList<String> send_bits= new ArrayList<String>();
                    //sending the reciever information about who is sending the private message
                    send_bits.add("private");
                    send_bits.add(String.valueOf(private_message_id));
                    send_bits.add(getUserName());
                    //sending private message and username of sender
                    handlers.get(private_message_id).pw.println(converttoString(send_bits));
                    pw.println(getUserName() + " : " + temp);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }finally{
            handlers.remove(this);
        }
    }
}
