package com.github.vitalibo.brickgame.game.tanks;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.github.vitalibo.brickgame.game.Point;
import com.github.vitalibo.brickgame.game.tanks.Direction;
import com.github.vitalibo.brickgame.game.tanks.TanksGame;


class MyCiclo extends Thread{

    private Socket socket;
    private static int PORT = 8903;
    private BufferedReader in;
    private static PrintWriter out;

    private TanksGame instance;

    public MyCiclo(TanksGame instance, BufferedReader in, PrintWriter out){
        this.instance = instance;
	this.in = in;
	this.out = out;
        
    }

    public void up(){
        out.println("Arriba");
    }

    public static void upServer(){
        out.println("Arriba");
    }
    public static void leftServer(){
        out.println("Izquierda");
    }
    public static void rightServer(){
        out.println("Derecha");
    }
    public static void downServer(){
        out.println("Abajo");
    }

    public static void shotServer(Direction dir, Point point){
	int direction = 0;
	switch(dir){
	case UP: direction = 1; break;
	case RIGHT: direction = 2; break;
	case DOWN: direction = 3; break;
	case LEFT: direction = 4;
	}
	out.println("shot " + point.x + " " + point.y + " " + direction);
    }

    @Override
    public void run(){
        try {
	    while(true){
                String recibo = in.readLine();
		System.out.println("Recibí " + recibo);
		String[] comando = recibo.split(" ");
		if (recibo.contains("remove_shot")){
		    System.out.println("Quitar el disparo " + recibo.split(" ")[1]);
		}else if(recibo.contains("disparo")){
		    String[] data = recibo.split(" ");
		    instance.myShot(Integer.parseInt(data[2]),Integer.parseInt(data[1]),Integer.parseInt(data[3]),Integer.parseInt(data[4]));
		}else if(recibo.contains("shot")){
		    System.out.println("contiene shot: " + recibo);
		    String[] data = recibo.split(" ");
		    instance.myShot(Integer.parseInt(data[2]),Integer.parseInt(data[1]),Integer.parseInt(data[3]),Integer.parseInt(data[4]));
		}else if(recibo.equals("YOU_DIED")){
		    instance.iCrash();
		}else if(recibo.equals("OPPONENT_DIED")){
		    instance.enemyCrash();
		}else{
		    switch(comando[1]){
		    case "down": instance.enemyDown(); break;
		    case "up": instance.enemyUp(); break;
		    case "left": instance.enemyLeft(); break;
		    case "right": instance.enemyRight(); break;
                }
		}
            }
        }catch(IOException ioe){
            System.out.println("error: " + ioe.getMessage());
        }
        try {
            socket.close();
        }catch(IOException ioe){
            System.out.println("sin socket: " + ioe.getMessage());
        }
        
    }
}
