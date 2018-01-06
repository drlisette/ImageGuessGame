package com.ImageGuess;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

/**
 * ImageGuess Game
 * MyApp where put all global variables
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

public class MyApp extends Application {
    private String user_name;
    private int port_number;
    private String player_state;
    private int player_number;
    private String remote_ip;
    private int remote_port;
    private String room_state;
    private String server_ip;
    private int server_port;
    private String room_number;
    private int current_drawer;
    private int game_round;

    public void setUserName(String userName){
        this.user_name = userName;
    }

    public void setPortNumber(int portNumber){
        this.port_number = portNumber;
    }

    public void setPlayerState(String playerState){
        this.player_state = playerState;
    }

    public void setPlayerNumber(int playerNumber){
        this.player_number = playerNumber;
    }

    public void setRemoteIP(String remoteIp){
        this.remote_ip = remoteIp;
    }

    public void setRemotePort(int remotePort){
        this.remote_port = remotePort;
    }

    public void setRoomState(String roomState){
        this.room_state = roomState;
    }

    public void setServerIP(String serverIp){
        this.server_ip = serverIp;
    }

    public void setServerPort(int serverPort){
        this.server_port = serverPort;
    }

    public void setRoomNumber(String roomNumber){
        this.room_number = roomNumber;
    }

    public void setCurrentDrawer(int currentDrawer){
        this.current_drawer = currentDrawer;
    }

    public void setGameRound(int gameRound){
        this.game_round = gameRound;
    }

    public String getUserName(){
        return user_name;
    }

    public int getPortNumber(){
        return port_number;
    }

    public String getPlayerState(){
        return player_state;
    }

    public int getPlayerNumber(){
        return player_number;
    }

    public String getRemoteIP(){
        return remote_ip;
    }

    public int getRemotePort(){
        return remote_port;
    }

    public String getRoomState(){
        return room_state;
    }

    public String getServerIP(){
        return server_ip;
    }

    public int getServerPort(){
        return server_port;
    }

    public String getRoomNumber(){
        return room_number;
    }

    public int getCurrentDrawer(){
        return current_drawer;
    }

    public int getGameRound(){
        return game_round;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        server_ip = "172.20.10.10";
        server_port = 8000;
        current_drawer = 0;
        game_round = 5;
    }
}
