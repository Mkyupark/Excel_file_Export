package dkc.cut.cut.global;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import dkc.cut.cut.bol.cut.work.order.BolCutWorkOrderService;

/*
 * Oracle ERP과 연결하는 Socket 코드
 * FileService 객체와 DB 객체를 호출하는 MAIN 코드
 */
@Component
public class SocketConnect implements CommandLineRunner{
	// Socket 연결 포트 설정 - Oracle 포트와 일치해야함.
	private static final int PORT = 12345; 
	// DB 코드 작성전 파일 CRUD, SOCKET 서버 테스트용 더미데이터
	// private final String dummyData = "info=3,10\n0=540,200,10\n1=540,200,10\n2=250,300,10";	
	
	// file 입출력 객체
	@Autowired
	private FileService fileService;
	
	@Autowired
	private BolCutWorkOrderService bolCutWorkOrderService;
	
	// 동시성 처리를 위한 Thread pool 설정
	private final ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	public void run(String... args) throws Exception {
	    // 별도의 스레드에서 서버 소켓 실행 (메인 스레드 블로킹 방지)
	    new Thread(this::StartServer).start();
	}
	public void StartServer() {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("소켓 서버가 포트 " + PORT + "에서 실행 중입니다.");
            fileService.createFolder(); // 처음 연결시 경로에 폴더 생성 
            // 무한루프 클라이언트 연결 대기
            while (true) {
            	// 소켓을 통해 전송받은 데이터
                Socket clientSocket = serverSocket.accept();          
                // 클라이언트의 데이터를 읽기 위한 스레드 생성 (동시 다중 처리 가능)
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("소켓 서버 실행 중 오류 발생: " + e.getMessage());
        }
	}
	
    // 클라이언트에서 받은 데이터를 처리하기 위한 작업 
	private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
        	String receivedData = in.readLine(); // Socket으로 받은 데이터 
        	// log.info("전송받은 데이터:"+ receivedData); - thread pool 확인
        	//System.out.println("전송받은 데이터: "+ receivedData);

        	List<String> formatReceivedData = Arrays.stream(receivedData.split("\\|")).collect(Collectors.toList());
        	
        	String result = bolCutWorkOrderService.fileContent(formatReceivedData);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("[" + LocalDateTime.now().format(formatter) + "]");
            
        	if (result != null) {
            	fileService.deleteAllFiles();	// 기존에 있는 파일 모두 삭제
            	fileService.createFile(formatReceivedData.get(2), result);         		
        	}else {
            	System.out.println("자를 판이 없습니다.\n원재판번호를 확인해주세요.");
            	System.out.println("입력받은 원재판번호 : " + formatReceivedData.get(2) +"\n");
        	}

        } catch (Exception e) {
            System.err.println("클라이언트 처리 중 오류 발생: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception ex) {
                // 연결 종료 오류 무시
            }
        }
    }
	
}
