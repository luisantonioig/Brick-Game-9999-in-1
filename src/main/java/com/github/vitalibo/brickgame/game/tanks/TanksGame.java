package com.github.vitalibo.brickgame.game.tanks;

import com.github.vitalibo.brickgame.core.Context;
import com.github.vitalibo.brickgame.game.Game;
import com.github.vitalibo.brickgame.util.CanvasTranslator;
import com.github.vitalibo.brickgame.util.Random;

import com.github.vitalibo.brickgame.Run;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.vitalibo.brickgame.game.Point;
import com.github.vitalibo.brickgame.game.tanks.Direction;
import java.io.IOException;
import java.io.PrintWriter;
import com.github.vitalibo.brickgame.core.Controllable;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.UnknownHostException;

public class TanksGame extends Game {

    private final Battlefield battlefield = new Battlefield();
    private final ArtificialIntelligence ai = new ArtificialIntelligence();
    private final Physics physics = new Physics();

    Map<String,Shot> mapa = new HashMap<String,Shot>();

    private List<Shot> arrayShots = new CopyOnWriteArrayList<>();
    private List<Shot> myShots = new CopyOnWriteArrayList<>();
    private List<Shot> shots = new CopyOnWriteArrayList<>();

    Tank myTank;
    Tank enemyTank;    
    private List<Tank> tanks;

    private Socket socket;
    private static int PORT = 8903;
    private BufferedReader in;
    private PrintWriter out;
    int positions;

    private MyCiclo listener;

    public TanksGame(Context context) {
        super(context);
        try {
            socket = new Socket(Run.ip, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
	    String recib = in.readLine();
	    String pos = in.readLine();
	    positions = Integer.parseInt(pos);
	    String players = in.readLine(); 
        }catch(UnknownHostException e){
            System.out.println("Unknown host: " + e.getMessage());
        }catch(IOException ioe){
            System.out.println("Input-Ouput: " + ioe.getMessage());
        }

	        
    }

    @Override
    public void init(){
        score.set(0);
        myTank = Battlefield.Spawn.myTank(positions);
	enemyTank = Battlefield.Spawn.myTank(positions == 1 ? 2: 1);

	
        tanks = battlefield.getTanks();
        //tanks.addAll(Battlefield.Spawn.enemyTanks());
        tanks.add(myTank);
	tanks.add(enemyTank);

	repaint();

        //kernel.job(this, 1000 - speed.get() * 45, job -> physics.tanksMove());
        //kernel.job("shots", 150, job -> physics.bulletFlight());
        listener = new MyCiclo(this, this.in, this.out);
        listener.start();
    }

    @Override
    public void doDown() {
	//Point point = myTank.point;
	//System.out.println("Punto x = " + point.x);
	//System.out.println("Punto y = " + point.y);	
        //listener.downServer();
	if (doDown(myTank)){
	    Point point = myTank.point;
	    out.println("down " + point.x + " " + point.y);
	    repaint();
	}
    }

    @Override
    public void doLeft() {
	//Point point = myTank.point;
	//System.out.println("Punto x = " + point.x);
	//System.out.println("Punto y = " + point.y);
        //listener.leftServer();
	if (doLeft(myTank)){
	    Point point = myTank.point;
	    out.println("left " + point.x + " " + point.y);
	    repaint();
	}

    }

    @Override
    public void doRight() {
	//Point point = myTank.point;
	//System.out.println("Punto x = " + point.x);
	//System.out.println("Punto y = " + point.y);
        //listener.rightServer();
	if (doRight(myTank)){
	    Point point = myTank.point;
	    out.println("right " + point.x + " " + point.y);
	    repaint();
	}

    }

    @Override
    public void doUp() {
	//	Point point = myTank.point;
	//System.out.println("Punto x = " + point.x);
	//System.out.println("Punto y = " + point.y);
        //listener.upServer();
	if (doUp(myTank)){
	    Point point = myTank.point;
	    out.println("up " + point.x + " " + point.y);
	    repaint();
	}

    }

    @Override
    public void doRotate() {
	Direction direction = myTank.direction;
	Point point = myTank.point;
	listener.shotServer(direction, point);
        //myShots.add(myTank.doShot());
        //repaint();
    }

    public void myShot(int x, int y, int dir, int num){

	Direction direction = Direction.UP;

	switch(dir){
	case 1: direction = Direction.UP; break;
	case 2: direction = Direction.RIGHT; break;
	case 3: direction = Direction.DOWN; break;
	case 4: direction = Direction.LEFT;
	}
	Shot shot = new Shot(
			     Point.of(x + 1 + Tank.shifting(direction, Direction.UP, Direction.DOWN),y + 1 + Tank.shifting(direction, Direction.LEFT,Direction.RIGHT))
			     ,direction);
	myShots.add(shot);
	if(mapa.containsKey(""+num)){
	    myShots.remove(mapa.get(""+num));
	}
	mapa.put(""+num, shot);
	repaint();
    }

    public void enemyDown() {
        if (doDown(enemyTank)) repaint();
    }

    public void enemyLeft() {
        if (doLeft(enemyTank)) repaint();
    }

    public void enemyRight() {
        if (doRight(enemyTank)) repaint();
    }

    public void enemyUp() {
        if (doUp(enemyTank)) repaint();
    }

    
    boolean doDown(Tank tank) {
        return doStep(tank, Battlefield::canDoDown, Tank::doDown);
    }

    boolean doLeft(Tank tank) {
        return doStep(tank, Battlefield::canDoLeft, Tank::doLeft);
    }

    boolean doRight(Tank tank) {
        return doStep(tank, Battlefield::canDoRight, Tank::doRight);
    }

    boolean doUp(Tank tank) {
        return doStep(tank, Battlefield::canDoUp, Tank::doUp);
    }

    private boolean doStep(Tank tank, BiFunction<Battlefield, Tank, Integer> function, Consumer<Tank> consumer) {
        int steps = function.apply(battlefield, tank);
        for (int step = 0; step < steps; step++) {
            consumer.accept(tank);
        }

        return steps > 0;
    }

    private boolean doShot(Tank tank) {
        shots.add(tank.doShot());
        return true;
    }

    void repaint() {
        board.draw(CanvasTranslator.from(
            shots().map(Shot::getPoint),
            tanks.stream().flatMap(Tank::stream)));
    }

    void iCrash(){
	crash(myTank.getPoint());
    }

    void enemyCrash(){
	crash(enemyTank.getPoint());
    }

    private Stream<Shot> shots() {
        return Stream.of(myShots, shots).flatMap(Collection::stream);
    }

    private class Physics {

        private void tanksMove() {
            tanks.stream().filter(tank -> tank != myTank)
                .forEach(ai::doNextStep);

            if (tanks.size() < 5) {
                battlefield.spawn();
            }

            repaint();
        }

        private void bulletFlight() {
            shots().forEach(Shot::run);

            Map<Shot, Tank> killed = killedTanks();
            tanks.removeAll(killed.values());
            myShots.removeAll(killed.keySet());
            repaint();

            score.inc(killed.size() * 100);

            shots.stream().filter(o -> myTank.hasKilled(o))
                .findFirst().ifPresent(o -> crash(o.getPoint()));

            List<Shot> outside = shots()
                .filter(Shot::verify).collect(Collectors.toList());
            myShots.removeAll(outside);
            shots.removeAll(outside);
        }

        private Map<Shot, Tank> killedTanks() {
            return tanks.stream()
                .filter(o -> o != myTank)
                .flatMap(tank -> myShots.stream()
                    .filter(tank::hasKilled).findFirst()
                    .map(o -> new AbstractMap.SimpleEntry<>(o, tank))
                    .map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

    }

    private class ArtificialIntelligence {

        boolean doNextStep(Tank tank) {
            return Stream.generate(this::decideStep)
                .limit(5).anyMatch(o -> o.apply(TanksGame.this, tank));
        }

        private BiFunction<TanksGame, Tank, Boolean> decideStep() {
            switch (Random.nextInt(4)) {
                case 0:
                    return direction(Direction.random());
                case 1:
                case 2:
                    return (game, tank) -> direction(tank.getDirection()).apply(game, tank);
                case 3:
                    return TanksGame::doShot;
                default:
                    throw new IllegalStateException();
            }
        }

        private BiFunction<TanksGame, Tank, Boolean> direction(Direction direction) {
            switch (direction) {
                case UP:
                    return TanksGame::doUp;
                case RIGHT:
                    return TanksGame::doRight;
                case DOWN:
                    return TanksGame::doDown;
                case LEFT:
                    return TanksGame::doLeft;
                default:
                    throw new IllegalStateException();
            }
        }

    }

}
