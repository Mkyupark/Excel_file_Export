package dkc.cut.cut.global;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class FileService {

	// 파일 생성 경로지정
	private static final String DIRECTORY = "C:\\cuting\\info";
	
	// 초기에 프로그램 실행시 폴더 한번만 열도록 설정하기 위한 변수
	private static int folderOpen = -1;
	
	// 폴더 생성
	public void createFolder() throws IOException {
		try {
			Path path = Paths.get(DIRECTORY);
            Files.createDirectories(path);
            System.out.println("-------------- 디렉토리 생성 성공 --------------" );
            System.out.println("디렉토리 경로: "+ path);
            System.out.println("--------------------------------------------\n");
		}catch(IOException e) {
			System.out.println("-------------- 디렉토리 생성 실패 --------------");
			System.out.println(e.getMessage());
		}
	}
	// 파일에 값 쓰기
	// Content 예외처리는 BolCutWorkOrderEntity 생성후 생각
    public void createFile(String filename, String content) throws IOException {
        Path path = Paths.get(DIRECTORY, filename + ".info");	// 파일 저장 경로 지정
        Files.write(path, content.getBytes());		// path 경로에 2번째 인자값 적기
        //System.out.println("파일명: " + path.getFileName());
        //System.out.println("파일 생성 경로: " + path.toFile().getAbsolutePath() + "\n");
        openFileAndFolder(path.toFile());
    }

    // 현재 디렉토리에 있는 파일 전체 삭제 코드
    public void deleteAllFiles() throws IOException {
        Path directoryPath = Paths.get(DIRECTORY);

        // 디렉토리 내의 모든 파일 및 하위 디렉토리 스트림
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths
                .filter(Files::isRegularFile) // 하위 디렉토리는 무시하고 일반 파일만 필터링
                .forEach(path -> {
                    try {
                        Files.delete(path); // 파일 삭제
                    } catch (IOException e) {
                    	//log.warn("Failed to delete " + path + ": " + e.getMessage());
                    }
                });
        }
    }
    
    // 생성시 파일 열기
    private void openFileAndFolder(File file) {
        try {
            String parentDir = file.getParent(); // 파일이 있는 폴더 경로
            // isDesktopSupported => Windows 내장 GUI가 있어야 통과 return 값 boolean
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file); // 기본 프로그램으로 파일 실행
                    desktop.open(new File(parentDir)); // 파일이 있는 폴더도 함께 열기
                } else {
                    throw new UnsupportedOperationException("파일 열기 기능이 지원되지 않는 OS");
                }
                // Windows 내장 GUI가 없는경우 CMD 명령어를 통해 폴더 열기
            } else{ 	
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    // Windows에서 파일 및 폴더 실행
                	new ProcessBuilder("explorer", parentDir).start(); // 파일이 있는 폴더도 함께 열기
                    new ProcessBuilder("cmd", "/c", "start", file.getAbsolutePath()).start();
                } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                    // Linux/macOS에서 파일 및 폴더 실행
                	new ProcessBuilder("xdg-open", parentDir).start(); // 파일이 있는 폴더 열기
                    new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
                } else {
                    throw new UnsupportedOperationException("이 운영체제에서는 파일 실행이 지원되지 않습니다.");
                }
            }
            System.out.println("파일 생성 경로: " + file.getName());
            System.out.println("파일 생성 경로: " + file.getAbsolutePath() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}