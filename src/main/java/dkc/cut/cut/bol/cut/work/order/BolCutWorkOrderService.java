package dkc.cut.cut.bol.cut.work.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class BolCutWorkOrderService {

	private BolCutWorkOrderRepository bolCutWorkOrderRepository;

    @Autowired
    public BolCutWorkOrderService(BolCutWorkOrderRepository bolCutWorkOrderRepository) {
        this.bolCutWorkOrderRepository = bolCutWorkOrderRepository;
    }
    
    /**
     * @brief
     * .info 파일에 적을 문자열을 생성하는 함수
     */
	public String fileContent(List<String> receivedData) {
		try {
			List<BolCutWorkOrderDTO> cuttingLots = findAndUpdateCuttingLot(receivedData);
			if (cuttingLots == null) {
				return null;
			}
			StringBuilder content = new StringBuilder("info=" + cuttingLots.size() + "," + cuttingLots.get(0).getOrderThickness() + "\n");

			for (BolCutWorkOrderDTO dto : cuttingLots) {
			    content.append(dto.getCutSeq()).append("=")
			           .append(dto.getOrderWidth()).append(",")
			           .append(dto.getOrderLength()).append(",")
			           .append(dto.getWidthTolerance()).append(",")
			           .append(dto.getLengthTolerance()).append("\n");
			}

			// 마지막에 추가된 줄바꿈 문자 제거
			if (content.length() > 0) {
			    content.deleteCharAt(content.length() - 1);
			}
			log.info("문자열 생성 성공");
			return content.toString();
		} catch (Exception e) {
			log.error("파일 문자열 생성시 오류 발생", e);
            throw new RuntimeException("서버 처리 중 예외 발생", e);
		}
	}
	
	// DB에서 데이터 조회하기 위한 함수
    public List<BolCutWorkOrderDTO> findAndUpdateCuttingLot(List<String> receivedData) {
        if (receivedData == null || receivedData.size() != 4) {
        	System.out.println("DB 데이터 전송 형식 오류 : "+ receivedData);
        	log.error("DB 데이터 전송 형식 오류 : {} ", receivedData);     	
            throw new IllegalArgumentException("잘못된 데이터를 전송받았습니다." +"\n 전송받은 데이터 : " + receivedData);
        }
        
        try {
            int organizationId = Integer.parseInt(receivedData.get(1));
            String rawLotNumber = receivedData.get(2);
            int resourceId = Integer.parseInt(receivedData.get(3));

            List<BolCutWorkOrderDTO> cuttingLots = bolCutWorkOrderRepository.findCuttingLot(organizationId, rawLotNumber, resourceId);
            log.info("{} 건이 조회가 되었습니다.", cuttingLots.size());
            if (cuttingLots.isEmpty()) {
                return null;
            }else {
            	int updateCount = bolCutWorkOrderRepository.updateRecordStatus(organizationId, rawLotNumber, resourceId);
            	log.info("{} 건이 Inactive 상태로 Update 되었습니다.", updateCount);
            	System.out.println(updateCount + "건이 Inactive 상태로 Update 되었습니다.\n");
            } 
            System.out.printf("| %-6s | %-15s | %-9s | %-8s | %-7s | %-6s | %-6s |\n",
                "절단순서",
                "제품 판번호",                
                "폭",
                "길이",
                "폭공차",
                "길이공차",
                "폭/길이 전환"
            );
            System.out.println("--------------------------------------------------------------------------------------------------------");
            
            for (BolCutWorkOrderDTO dto : cuttingLots) {
                System.out.printf("| %-10d | %-20s | %-10d | %-10d | %-10d | %-10d | %-12s |\n",
                    dto.getCutSeq().intValue(),
                    dto.getLotNumber(),
                    dto.getOrderWidth().intValue(),
                    dto.getOrderLength().intValue(),
                    dto.getWidthTolerance().intValue(),
                    dto.getLengthTolerance().intValue(),
                    (dto.getWlChange().equalsIgnoreCase("Y") ? "Yes" : "No")
                );
            }
            System.out.println("--------------------------------------------------------------------------------------------------------\n");	            
            return cuttingLots;
        } catch (Exception e) {
        	log.error("DB 조회 / 수정 중 오류 발생 : {}", e);
            System.err.println("처리 중 예외가 발생했습니다: " + e);
            throw new RuntimeException("서버 처리 중 예외 발생", e);
        }
    }	
}
