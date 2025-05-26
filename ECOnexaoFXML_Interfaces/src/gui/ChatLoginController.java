package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class ChatLoginController {

    // ===== CAMPOS DA TELA DE LOGIN =====
    @FXML private TextField nomeField;
    @FXML private TextField ipField;
    @FXML private TextField salaField;
    @FXML private Button btnConectar;
    @FXML private Button btnVoltar;

    // ===== CAMPOS DA TELA DE CHAT =====
    @FXML private TextArea InputMensagem;
    @FXML private Button BtnEnvio;
    @FXML private Button BtnSairChat;
    @FXML private Button BtnArquivo;
    @FXML private Button BtnEmoji;
    @FXML private TextFlow Mensagens;

    // ===== CAMPOS VOIP =====
    @FXML private Text CHAT_VOIP;
    @FXML private Rectangle BarraVoip;
    @FXML private ImageView Lig_Des_Voip;
    @FXML private Button LigarDesligar_Voip;
    @FXML private ImageView Mut_Desm_phone;
    @FXML private Button MutarDesmutar_phone;
    @FXML private ImageView Mut_Desm_microphone;
    @FXML private Button mutarDesmutar_Microphone;

    // ===== CAMPOS EMOJIS =====
    @FXML private Rectangle BarraEmoji;
    @FXML private ImageView Emoji_beijo;
    @FXML private ImageView Emoji_bravo;
    @FXML private ImageView Emoji_enjoado;
    @FXML private ImageView Emoji_feliz;
    @FXML private ImageView Emoji_gafe;
    @FXML private ImageView Emoji_like;
    @FXML private ImageView Emoji_machucado;
    @FXML private ImageView Emoji_nerd;
    @FXML private ImageView Emoji_OlhosCora;
    @FXML private ImageView Emoji_piscada;
    @FXML private ImageView Emoji_rico;
    @FXML private ImageView Emoji_risada;
    @FXML private ImageView Emoji_shiu;
    @FXML private ImageView Emoji_sono;
    @FXML private ImageView Emoji_florestal;

    // ===== FLAGS DE ESTADO =====
    private boolean voipAtivo = false;
    private boolean telefoneMutado = false;
    private boolean microfoneMutado = false;

    private String ipServidor;
    private Socket socket;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private Thread threadRecebedor;

    private Thread voipSenderThread;
    private Thread voipReceiverThread;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private DatagramSocket voipSocket;

    @FXML
    public void initialize() {
        if (btnConectar != null) btnConectar.setOnAction(event -> conectar());
        if (btnVoltar != null) btnVoltar.setOnAction(event -> voltar());
        if (BtnEnvio != null) BtnEnvio.setOnAction(event -> enviarMensagem());
        if (BtnSairChat != null) BtnSairChat.setOnAction(event -> voltarParaLogin());
        if (BtnArquivo != null) BtnArquivo.setOnAction(event -> enviarArquivo());
        if (LigarDesligar_Voip != null) LigarDesligar_Voip.setOnAction(event -> ligarDesligarVoip());
        if (MutarDesmutar_phone != null) MutarDesmutar_phone.setOnAction(event -> mutarDesmutarTelefone());
        if (mutarDesmutar_Microphone != null) mutarDesmutar_Microphone.setOnAction(event -> mutarDesmutarMicrofone());
        if (Emoji_beijo != null) {
            inicializarAcoesEmojis();
        }      
    }
    private boolean emojisVisiveis = false;

    @FXML
    public void alternarVisibilidadeEmojis() {
        boolean visivel = !emojisVisiveis;
        double opacidade = visivel ? 1 : 0;

        BarraEmoji.setOpacity(opacidade);
        BarraEmoji.setVisible(visivel);
        Emoji_beijo.setOpacity(opacidade); Emoji_beijo.setVisible(visivel);
        Emoji_bravo.setOpacity(opacidade); Emoji_bravo.setVisible(visivel);
        Emoji_enjoado.setOpacity(opacidade); Emoji_enjoado.setVisible(visivel);
        Emoji_feliz.setOpacity(opacidade); Emoji_feliz.setVisible(visivel);
        Emoji_gafe.setOpacity(opacidade); Emoji_gafe.setVisible(visivel);
        Emoji_like.setOpacity(opacidade); Emoji_like.setVisible(visivel);
        Emoji_machucado.setOpacity(opacidade); Emoji_machucado.setVisible(visivel);
        Emoji_nerd.setOpacity(opacidade); Emoji_nerd.setVisible(visivel);
        Emoji_OlhosCora.setOpacity(opacidade); Emoji_OlhosCora.setVisible(visivel);
        Emoji_piscada.setOpacity(opacidade); Emoji_piscada.setVisible(visivel);
        Emoji_rico.setOpacity(opacidade); Emoji_rico.setVisible(visivel);
        Emoji_risada.setOpacity(opacidade); Emoji_risada.setVisible(visivel);
        Emoji_shiu.setOpacity(opacidade); Emoji_shiu.setVisible(visivel);
        Emoji_sono.setOpacity(opacidade); Emoji_sono.setVisible(visivel);
        Emoji_florestal.setOpacity(opacidade); Emoji_florestal.setVisible(visivel);

        emojisVisiveis = visivel;
    }


    private void inicializarAcoesEmojis() {
        Emoji_beijo.setOnMouseClicked(e -> enviarEmoji("😘"));
        Emoji_bravo.setOnMouseClicked(e -> enviarEmoji("😠"));
        Emoji_enjoado.setOnMouseClicked(e -> enviarEmoji("🤢"));
        Emoji_feliz.setOnMouseClicked(e -> enviarEmoji("😊"));
        Emoji_gafe.setOnMouseClicked(e -> enviarEmoji("😅"));
        Emoji_like.setOnMouseClicked(e -> enviarEmoji("👍"));
        Emoji_machucado.setOnMouseClicked(e -> enviarEmoji("🤕"));
        Emoji_nerd.setOnMouseClicked(e -> enviarEmoji("🤓"));
        Emoji_OlhosCora.setOnMouseClicked(e -> enviarEmoji("😍"));
        Emoji_piscada.setOnMouseClicked(e -> enviarEmoji("😉"));
        Emoji_rico.setOnMouseClicked(e -> enviarEmoji("🤑"));
        Emoji_risada.setOnMouseClicked(e -> enviarEmoji("😂"));
        Emoji_shiu.setOnMouseClicked(e -> enviarEmoji("🤫"));
        Emoji_sono.setOnMouseClicked(e -> enviarEmoji("😴"));
        Emoji_florestal.setOnMouseClicked(e -> enviarEmoji("🌳"));

        // Muda o cursor para indicar que é clicável
        ImageView[] emojis = {
            Emoji_beijo, Emoji_bravo, Emoji_enjoado, Emoji_feliz, Emoji_gafe,
            Emoji_like, Emoji_machucado, Emoji_nerd, Emoji_OlhosCora, Emoji_piscada,
            Emoji_rico, Emoji_risada, Emoji_shiu, Emoji_sono, Emoji_florestal
        };
        for (ImageView emoji : emojis) {
            emoji.setCursor(Cursor.HAND);
        }
    }

    private void enviarEmoji(String emoji) {
        try {
            dataOut.writeUTF(emoji);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciarVoip(String remoteIp) {
        try {
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);

            microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            voipSocket = new DatagramSocket(5556); // Porta fixa para recepção
            InetAddress serverAddress = InetAddress.getByName(remoteIp);

            voipAtivo = true;

            // Thread para enviar áudio
            voipSenderThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    while (voipAtivo) {
                        if (!microfoneMutado) {
                            int bytesRead = microphone.read(buffer, 0, buffer.length);
                            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, 5555);
                            voipSocket.send(packet);
                        } else {
                            Thread.sleep(10); // Pausa para não ocupar CPU enquanto está mutado
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Thread para receber áudio
            voipReceiverThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    while (voipAtivo) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        voipSocket.receive(packet);

                        if (!telefoneMutado) {
                            speakers.write(packet.getData(), 0, packet.getLength());
                        } else {
                            Thread.sleep(10); // Pausa se o fone estiver mutado
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            voipSenderThread.start();
            voipReceiverThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pararVoip() {
        voipAtivo = false;
        if (microphone != null) microphone.close();
        if (speakers != null) speakers.close();
        if (voipSocket != null) voipSocket.close();
    }

    @FXML
    private void ligarDesligarVoip() {
        if (voipAtivo) {
            CHAT_VOIP.setText("CHAT");
            MutarDesmutar_phone.setOpacity(0);
            mutarDesmutar_Microphone.setOpacity(0);
            Mut_Desm_phone.setOpacity(0);
            Mut_Desm_microphone.setOpacity(0);
            BarraVoip.setOpacity(0);
            pararVoip();
            Platform.runLater(() -> Mensagens.getChildren().add(new Text("\uD83D\uDCF4 Você saiu do VOIP.\n")));
        } else {
            CHAT_VOIP.setText("VOIP");
            MutarDesmutar_phone.setOpacity(0.55);
            mutarDesmutar_Microphone.setOpacity(0.55);
            Mut_Desm_phone.setOpacity(1);
            Mut_Desm_microphone.setOpacity(1);
            BarraVoip.setOpacity(1);
            voipAtivo = true;
            iniciarVoip(ipServidor);
            Platform.runLater(() -> Mensagens.getChildren().add(new Text("\uD83D\uDD0A Você entrou no VOIP.\n")));
        }
    }

    @FXML
    private void mutarDesmutarTelefone() {
        telefoneMutado = !telefoneMutado;

        // Altera a cor do botão
        MutarDesmutar_phone.setStyle("-fx-background-color: " + (telefoneMutado ? "#C4D34C;" : "#C4D49C;"));

        // Escreve no chat o status
        String status = telefoneMutado ? "🔇 Fone desligado. Você não ouvirá o áudio." 
                                       : "🎧 Fone ligado. Você ouvirá o áudio.";
        Platform.runLater(() -> Mensagens.getChildren().add(new Text(status + "\n")));
    }

    @FXML
    private void mutarDesmutarMicrofone() {
        microfoneMutado = !microfoneMutado;

        // Altera a cor do botão
        mutarDesmutar_Microphone.setStyle("-fx-background-color: " + (microfoneMutado ? "#C4D34C;" : "#C4D49C;"));

        // Escreve no chat o status
        String status = microfoneMutado ? "🎙️ Microfone desligado. Seu áudio não será enviado." 
                                        : "🎙️ Microfone ligado. Seu áudio será enviado.";
        Platform.runLater(() -> Mensagens.getChildren().add(new Text(status + "\n")));
    }



    @FXML
    private void conectar() {
        try {
            String nome = nomeField.getText();
            String ip = ipField.getText();
            String sala = salaField.getText();

            this.ipServidor = ip;

            socket = new Socket(ip, 12345);
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());

            dataOut.writeUTF(nome);
            dataOut.writeUTF(sala);
            dataOut.writeUTF("123");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/chat.fxml"));
            Scene chatScene = new Scene(loader.load());
            ChatLoginController controller = loader.getController();
            controller.setSocket(socket, dataOut, dataIn);
            controller.setIpServidor(ip);
            controller.iniciarRecebimentoMensagens();

            Stage stage = (Stage) nomeField.getScene().getWindow();
            stage.setScene(chatScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIpServidor(String ipServidor) {
        this.ipServidor = ipServidor;
    }

    private void setSocket(Socket socket, DataOutputStream out, DataInputStream in) {
        this.socket = socket;
        this.dataOut = out;
        this.dataIn = in;
    }

    @FXML
    private void voltar() {
        try {
            Stage stage = (Stage) nomeField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Main.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void enviarMensagem() {
        String mensagem = InputMensagem.getText();
        if (mensagem != null && !mensagem.trim().isEmpty()) {
            try {
                dataOut.writeUTF(mensagem);
                InputMensagem.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void enviarArquivo() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            new Thread(() -> {
                try {
                    dataOut.writeUTF("[FILE]");
                    dataOut.writeUTF(file.getName());
                    dataOut.writeLong(file.length());

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void iniciarRecebimentoMensagens() {
        threadRecebedor = new Thread(() -> {
            try {
                while (true) {
                    String tipo = dataIn.readUTF();
                    if (tipo.equals("[FILE]")) {
                        String fileName = dataIn.readUTF();
                        long fileSize = dataIn.readLong();

                        String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
                        File file = new File(downloadsPath, fileName);
                        file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long remaining = fileSize;

                        while (remaining > 0 && (bytesRead = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            remaining -= bytesRead;
                        }
                        fos.close();

                        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "arquivo";
                        Platform.runLater(() -> Mensagens.getChildren().add(new Text("\uD83D\uDCCE Arquivo recebido: " + fileName + " (" + ext + ")\n")));
                    } else if (tipo.equals("[MSG]")) {
                        String mensagem = dataIn.readUTF();
                        Platform.runLater(() -> Mensagens.getChildren().add(new Text(mensagem + "\n")));
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> Mensagens.getChildren().add(new Text("[ERRO] Conexão perdida.\n")));
            }
        });
        threadRecebedor.setDaemon(true);
        threadRecebedor.start();
    }

    @FXML
    private void voltarParaLogin() {
        try {
            if (dataOut != null) dataOut.writeUTF("[MSG]");
            if (dataOut != null) dataOut.writeUTF("/sair");
            if (socket != null && !socket.isClosed()) socket.close();

            Stage stage = (Stage) BtnSairChat.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
