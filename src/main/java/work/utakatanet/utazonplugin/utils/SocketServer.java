package work.utakatanet.utazonplugin.utils;

import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.github.kuripasanda.economyutilsapi.api.impl.EconomyUtilsApiImpl;
import work.utakatanet.utazonplugin.UtazonPlugin;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class SocketServer implements Runnable {

    public static UtazonPlugin utazonPlugin = UtazonPlugin.getPlugin();
    public static EconomyUtilsApi ecoApi = UtazonPlugin.getEcoApi();

    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public void start() {
        new Thread(this).start();
        utazonPlugin.getLogger().info("socketサーバーを起動しました。");
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(GetSocketPort());
            isRunning = true;

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {

        ecoApi = new EconomyUtilsApiImpl();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String receivedData = new String(buffer, 0, bytesRead);

                try{
                    utazonPlugin.getLogger().info("プレイヤーの残高がsocketから参照されました。 参照UUID: " + receivedData);

                    UUID uuid = UUID.fromString(receivedData);
                    double PlayerBalance = ecoApi.getBalance(uuid);
                    utazonPlugin.getLogger().info(String.valueOf(PlayerBalance));

                    String responseData = String.valueOf(PlayerBalance);
                    outputStream.write(responseData.getBytes());
                    outputStream.flush();

                } catch (IllegalArgumentException e){
                    outputStream.write("Invalid UUID".getBytes());
                    outputStream.flush();
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    public static int GetSocketPort(){
        return utazonPlugin.getConfig().getInt("socket.port");
    }
}
