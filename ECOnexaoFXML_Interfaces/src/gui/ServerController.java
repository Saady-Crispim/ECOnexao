package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerController {

	@FXML private Text ipText;
	@FXML private Button BtnVoltar;
	@FXML private Button btnConectar;
	@FXML private TextFlow mensagensServidor;

	private Map<String, Sala> salas = new HashMap<>();
	private DatagramSocket voipSocketUDP;

	@FXML
	public void initialize() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			if (ipText != null) ipText.setText("IP: " + ip);
			BtnVoltar.setOnAction(event -> voltarParaMain());
			btnConectar.setOnAction(event -> iniciarServidor());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void iniciarServidor() {
		new Thread(() -> {
			try (ServerSocket servidor = new ServerSocket(12345)) {
				logMensagem("Servidor aguardando conexões...");
				iniciarVoipListener();
				while (true) {
					Socket clienteSocket = servidor.accept();
					new Thread(() -> tratarCliente(clienteSocket)).start();
				}
			} catch (IOException e) {
				logMensagem("Erro no servidor: " + e.getMessage());
			}
		}).start();
	}

	private void iniciarVoipListener() {
	    new Thread(() -> {
	        try {
	            voipSocketUDP = new DatagramSocket(5555);
	            byte[] buffer = new byte[4096];

	            while (true) {
	                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	                voipSocketUDP.receive(packet);

	                InetAddress senderAddress = packet.getAddress();
	                int senderPort = packet.getPort();

	                for (Socket sock : getAllClientSockets()) {
	                    if (!sock.getInetAddress().equals(senderAddress)) {
	                        DatagramPacket forwardPacket = new DatagramPacket(
	                                packet.getData(),
	                                packet.getLength(),
	                                sock.getInetAddress(),
	                                5556 // Porta fixa de recepção dos clientes
	                        );
	                        voipSocketUDP.send(forwardPacket);
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }).start();
	}


	private Set<Socket> getAllClientSockets() {
		Set<Socket> all = new HashSet<>();
		for (Sala sala : salas.values()) {
			all.addAll(sala.getSockets());
		}
		return all;
	}

	private void tratarCliente(Socket socket) {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			String nome = in.readUTF();
			String nomeSala = in.readUTF();
			String senhaSala = in.readUTF();

			Sala sala;
			synchronized (salas) {
				if (!salas.containsKey(nomeSala)) {
					sala = new Sala(nomeSala, senhaSala);
					salas.put(nomeSala, sala);
					logMensagem("Sala criada: " + nomeSala);
				} else {
					sala = salas.get(nomeSala);
					if (!sala.autenticar(senhaSala)) {
						out.writeUTF("Senha incorreta. Conexão encerrada.");
						socket.close();
						return;
					}
				}
			}

			sala.adicionarSocket(socket);
			sala.enviarMensagem("[INFO] >> " + nome + " entrou na sala.");

			for (String msg : sala.carregarHistorico()) {
				out.writeUTF("[MSG]");
				out.writeUTF("[HIST] " + msg);
			}

			while (true) {
				String linha = in.readUTF();
				if (linha.equals("[FILE]")) {
					String fileName = in.readUTF();
					long fileSize = in.readLong();
					byte[] buffer = new byte[4096];
					int bytesRead;
					long remaining = fileSize;
					ByteArrayOutputStream fileContent = new ByteArrayOutputStream();

					while (remaining > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
						fileContent.write(buffer, 0, bytesRead);
						remaining -= bytesRead;
					}

					sala.enviarArquivo(fileName, fileSize, fileContent.toByteArray());
				} else if (linha.equalsIgnoreCase("/sair")) {
					break;
				} else {
					sala.enviarMensagem(nome + ": " + linha);
				}
			}

			sala.removerSocket(socket);
			sala.enviarMensagem("[INFO] << " + nome + " saiu da sala.");
			socket.close();

		} catch (IOException e) {
			logMensagem("Erro com cliente: " + e.getMessage());
		}
	}

	private void logMensagem(String msg) {
		Platform.runLater(() -> mensagensServidor.getChildren().add(new Text(msg + "\n")));
	}

	@FXML
	private void voltarParaMain() {
		try {
			Stage stage = (Stage) BtnVoltar.getScene().getWindow();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Main.fxml"));
			stage.setScene(new Scene(loader.load()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Sala {
	private final String nome;
	private final String senha;
	private final List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());
	private final List<String> historico = Collections.synchronizedList(new ArrayList<>());

	public Sala(String nome, String senha) {
		this.nome = nome;
		this.senha = senha;
	}

	public boolean autenticar(String senha) {
		return this.senha.equals(senha);
	}

	public void adicionarSocket(Socket socket) {
		sockets.add(socket);
	}

	public void removerSocket(Socket socket) {
		sockets.remove(socket);
	}

	public void enviarMensagem(String msg) {
		historico.add(msg);
		synchronized (sockets) {
			for (Socket sock : sockets) {
				try {
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("[MSG]");
					out.writeUTF(msg);
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void enviarArquivo(String fileName, long fileSize, byte[] data) {
		synchronized (sockets) {
			for (Socket sock : sockets) {
				try {
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("[FILE]");
					out.writeUTF(fileName);
					out.writeLong(fileSize);
					out.write(data);
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<String> carregarHistorico() {
		return historico;
	}

	public List<Socket> getSockets() {
		return sockets;
	}
}