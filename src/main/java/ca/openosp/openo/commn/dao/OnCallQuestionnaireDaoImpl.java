//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.OnCallQuestionnaire;

@Repository
public class OnCallQuestionnaireDaoImpl extends AbstractDaoImpl<OnCallQuestionnaire> implements OnCallQuestionnaireDao {

    public OnCallQuestionnaireDaoImpl() {
        super(OnCallQuestionnaire.class);
    }
}
