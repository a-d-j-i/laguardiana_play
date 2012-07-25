package models.lov;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
@DiscriminatorValue( "USER_CODE_REFERENCE" )
public class DepositUserCodeReference extends LgLov {
}
