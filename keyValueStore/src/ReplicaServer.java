import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class ReplicaServer{
	
	public static void main(String[] args){

		if(args.length != 3){
			System.out.println("Usage: ./server server1 9090 conf.txt\n" + " " + args.length);
			System.exit(0);
		}
		
		ServerContext sc = new ServerContext(args[0],Integer.parseInt(args[1]));
		FileProcessor fp = new FileProcessor(args[2]);
		sc.readFile(fp);
		
		try {
			sc.server = new ServerSocket(sc.port);
		}
		catch(IOException i) {
			System.out.println(i);
		}
		
		Socket request = null;
		while(true) {
			try {
				request = sc.server.accept();
				System.out.println("Accepted");
				InputStream in = request.getInputStream();
				KeyValue.KeyValueMessage incoming = KeyValue.KeyValueMessage.parseDelimitedFrom(in);
				if(incoming.getConnection() == 1) {
					System.out.println("Client");
					Thread cl = new Thread(new Coordinator(request,sc, incoming));
					cl.start();
				}
				if(incoming.getConnection() == 0){
					System.out.println("server");
					KeyValue.Put put = incoming.getPutKey();
					KeyStore temp = new KeyStore(put.getKey(), put.getValue(), put.getTime());	
					sc.store.put(put.getKey(),temp);
					sc.printStore();
					OutputStream out = request.getOutputStream();
					KeyValue.KeyValueMessage.Builder res = KeyValue.KeyValueMessage.newBuilder();
					KeyValue.WriteResponse.Builder wr = KeyValue.WriteResponse.newBuilder();
					wr.setId(put.getKey());
					wr.setWriteReply(true);
					res.setWriteResponse(wr.build());
					res.build().writeDelimitedTo(out);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
