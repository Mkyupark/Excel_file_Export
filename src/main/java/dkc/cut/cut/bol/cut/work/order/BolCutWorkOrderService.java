package dkc.cut.cut.bol.cut.work.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
			StringBuilder content = new StringBuilder("info=" + cuttingLots.size() + "\n");

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

			return content.toString();
		} catch (Exception e) {
            System.err.println("처리 중 예외가 발생했습니다: " + e.getMessage());
            e.printStackTrace(); // 상세한 예외 추적
            throw new RuntimeException("서버 처리 중 예외 발생", e);
		}
	}
	
	// DB에서 데이터 조회하기 위한 함수
    public List<BolCutWorkOrderDTO> findAndUpdateCuttingLot(List<String> receivedData) {
        if (receivedData == null || receivedData.size() < 4) {
            throw new IllegalArgumentException("잘못된 데이터를 전송받았습니다." +"\n 전송받은 데이터 : " + receivedData);
        }
        
        try {
            int organizationId = Integer.parseInt(receivedData.get(1));
            String rawLotNumber = receivedData.get(2);
            int resourceId = Integer.parseInt(receivedData.get(3));

            List<BolCutWorkOrderDTO> cuttingLots = bolCutWorkOrderRepository.findCuttingLot(organizationId, rawLotNumber, resourceId);
            
            if (cuttingLots.isEmpty()) {
                return null;
            }else {
            	int updateCount = bolCutWorkOrderRepository.updateRecordStatus(organizationId, rawLotNumber, resourceId);
            	System.out.println(updateCount + "건이 Inactive 상태로 Update 되었습니다.\n");
            } 
            System.out.printf("| %-6s | %-15s | %-8s | %-8s | %-6s | %-6s | %-6s |\n",
                "절단순서",
                "제품 판번호",                
                "너비",
                "길이",
                "너비공차",
                "길이공차",
                "폭/너비 전환"
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
            System.err.println("처리 중 예외가 발생했습니다: " + e.getMessage());
            e.printStackTrace(); // 상세한 예외 추적
            throw new RuntimeException("서버 처리 중 예외 발생", e);
        }
    }	
}
