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

import ch.qos.logback.classic.Logger;
import dkc.cut.cut.bol.cut.work.order.BolCutWorkOrderService;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

/*
 * Oracle ERP과 연결하는 Socket 코드
 * FileService 객체와 DB 객체를 호출하는 MAIN 코드
 */
@Log4j2
@Component
public class SocketConnect implements CommandLineRunner{
	// Socket 연결 포트 설정 - Oracle 포트와 일치해야함.
	private static final int PORT = 12345; 
	
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
			log.info("소켓 서버가 포트 " + PORT + "에서 실행 중입니다.");
            System.out.println("소켓 연결에 성공하였습니다.");
            fileService.createFolder(); // 처음 연결시 경로에 폴더 생성 
            // 무한루프 클라이언트 연결 대기
            while (true) {
            	// 클라이언트와 연결할 소켓 객체 생성
                Socket clientSocket = serverSocket.accept();          
                // 클라이언트의 데이터를 읽기 위한 스레드 생성 (동시 다중 처리 가능)
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
        	log.info("소켓 서버 실행 중 오류 발생: " + e.getMessage());
        	e.printStackTrace();
        }
	}
	
    // 클라이언트에서 받은 데이터를 처리하기 위한 작업 
	private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
        	log.info("********************데이터 전송 시작********************");
        	String receivedData = in.readLine(); // Socket 객체로 부터 전송 받은 데이터 
        	log.info("DB로부터 전송받은 데이터: {}", receivedData);
        	
        	List<String> formatReceivedData = Arrays.stream(receivedData.split("\\|")).collect(Collectors.toList());
        	String result = bolCutWorkOrderService.fileContent(formatReceivedData);
        	// 커멘드 창에 현재날짜 찍어주기 위해 선언
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("[" + LocalDateTime.now().format(formatter) + "]");
        	if (result != null) {
            	fileService.deleteAllFiles();	// 기존에 있는 파일 모두 삭제
            	fileService.createFile(formatReceivedData.get(2), result);         		
        	}else {
        		log.warn("[NO_DATA_FOUND] 쿼리 조회 했지만 아무것도 조회가 되지 않았습니다.");
            	System.out.println("자를 판이 없습니다.\n원재판번호를 확인해주세요.");
            	System.out.println("입력받은 원재판번호 : " + formatReceivedData.get(2) +"\n");
        	}
        } catch (Exception e) {
        	log.error("클라이언트 처리 중 오류 발생: {}", e);
        } finally {
            try {
            	// socket 객체 메모리 해제
                clientSocket.close();
                log.info("********************데이터 전송 종료********************");
            } catch (Exception ex) {
                // 연결 종료 오류 무시
            	log.warn("에러로 인해 프로그램 종료: {}", ex);
            	System.out.println("에러로 인해 프로그램이 종료되었습니다.");
            	System.exit(1); 
            }
        }
    }
	
}
