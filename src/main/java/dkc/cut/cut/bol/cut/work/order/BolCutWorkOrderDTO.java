package dkc.cut.cut.bol.cut.work.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class BolCutWorkOrderDTO {

	// 절단순서 
	private Number cutSeq;
	
	// 폭 
	private Number orderWidth;

	// 길이
	private Number orderLength;
	
	// 폭 공차 
	private Number widthTolerance;

	// 길이 공차 
	private Number lengthTolerance;

	// 폭 길이 전환 'Y' / 'N'
	private String wlChange;
	
	// 제품 판번호
	private String lotNumber;

	public void setBolCutWorkOrderDTO(Object[] row) {
		
		final String changeWidthToLength = (String) row[5];
		this.setCutSeq((Number) row[0]);
	    this.setWlChange((String) row[5]);
	    this.setLotNumber((String) row[6]);
	    
		if(changeWidthToLength.equalsIgnoreCase("Y")) {
			this.setOrderWidth((Number) row[2]);
		    this.setOrderLength((Number) row[1]); 
		    this.setWidthTolerance((Number) row[4]); 
		    this.setLengthTolerance((Number) row[3]);	
		}else {
			this.setOrderWidth((Number) row[1]);
		    this.setOrderLength((Number) row[2]); 
		    this.setWidthTolerance((Number) row[3]); 
		    this.setLengthTolerance((Number) row[4]);				
		}
    }

}
