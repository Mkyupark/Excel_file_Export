package dkc.cut.cut.bol.cut.work.order;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;


@Repository
public class BolCutWorkOrderRepository {

	@PersistenceContext
	private EntityManager em;

	/**
	 * @brief
	 * JPA 스펙이 엔티티에 PK가 반드시 있어야 하기 때문에, Native Query를 사용하였다.
     * DB에서 PK 없는 테이블(BOL_CUT_WORK_ORDER)을 Native Query로 조회하고,
     * 결과를 BolCutWorkOrderDTO 리스트로 반환한다.
     *
     * @param organizationId 재고조직
     * @param rawLotNumber   원재 판번호
     * @param resourceId     설비 ID
     * 
     */
    public List<BolCutWorkOrderDTO> findCuttingLot(Number organizationId,
                                                   String rawLotNumber,
                                                   Number resourceId) {
        String sql = 
            "SELECT CUT_SEQ, " +
            "       ORDER_WIDTH, " +
            "       ORDER_LENGTH, " +
            "       WIDTH_TOLERANCE, " +
            "       LENGTH_TOLERANCE, " +
            "       WL_CHANGE, " +
            "		LOT_NUMBER" +
            "  FROM BOL_CUT_WORK_ORDER " +
            " WHERE ORGANIZATION_ID = :organizationId " +
            "   AND RAW_LOT_NUMBER  = :rawLotNumber " +
            "   AND RESOURCE_ID     = :resourceId " +
            "   AND RECORD_STATUS   = 'ACTIVE' " +
            " ORDER BY CUT_SEQ";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                                .setParameter("organizationId", organizationId)
                                .setParameter("rawLotNumber",  rawLotNumber)
                                .setParameter("resourceId",    resourceId)
                                .getResultList();
        
        List<BolCutWorkOrderDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
        	BolCutWorkOrderDTO dto = new BolCutWorkOrderDTO();
        	dto.setBolCutWorkOrderDTO(row);
            result.add(dto);
        }

        return result;
    }
    
    /**
     * 
     * @param organizationId 재고조직
     * @param rawLotNumber   원재 판번호
     * @param resourceId     설비 ID
     */
    @Transactional
    public int updateRecordStatus(Number organizationId,
						           String rawLotNumber,
						           Number resourceId) {
        String sql = 
                "UPDATE BOL_CUT_WORK_ORDER "+ 
                "SET 	RECORD_STATUS = 'INACTIVE' " +
                "WHERE  ORGANIZATION_ID = :organizationId AND " +
                "       RAW_LOT_NUMBER = :rawLotNumber AND " +
                "       RESOURCE_ID = :resourceId";

        int updatedCount = em.createNativeQuery(sql)
	                         .setParameter("organizationId", organizationId)
	                         .setParameter("rawLotNumber", rawLotNumber)
	                         .setParameter("resourceId", resourceId)
	                         .executeUpdate();
        return updatedCount;
    }
}
