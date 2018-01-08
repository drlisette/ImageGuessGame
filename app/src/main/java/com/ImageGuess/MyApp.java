package com.ImageGuess;

import android.app.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ImageGuess Game
 * MyApp where put all global variables
 * Created by Stanislas, Lisette, Faustine on 2017/12 in SJTU.
 */

//存放应用全局变量
public class MyApp extends Application {
    private String user_name;
    private int port_number;
    private String player_state;
    private int player_number;
    private String remote_ip;
    private int remote_port;
    private String remote_name;
    private String room_state;
    private String server_ip;
    private int server_port;
    private String room_number;
    private int current_drawer;
    private int game_round;
    private int current_index;
    private int user_score;
    private int remote_score;
    private JSONArray words_data;

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

    public void setRemoteName(String remoteName){
        this.remote_name = remoteName;
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

    public void setWordsData(JSONArray wordsData){
        this.words_data = wordsData;
    }

    public void setCurrentIndex(int currentWord){
        this.current_index = currentWord;
    }

    public void setUserScore(int userScore){
        this.user_score = userScore;
    }

    public void setRemoteScore(int remoteScore){
        this.remote_score = remoteScore;
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

    public String getRemoteName(){
        return remote_name;
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

    public JSONArray getWordsData(){
        return words_data;
    }

    public int getCurrentIndex(){
        return current_index;
    }

    public int getUserScore(){
        return user_score;
    }

    public int getRemoteScore(){
        return remote_score;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //server_ip = "172.20.10.10";
        server_ip = "192.168.43.24";
        server_port = 8000;
        current_drawer = 0;
        game_round = 5;
        current_index = 0;
        user_score = 0;
        remote_score = 0;
        remote_name = "undefine";
        words_data = new JSONArray();
    }
}
