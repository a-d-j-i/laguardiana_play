package models.lov;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import models.db.LgLov;

/**
 *
 * @author adji
 */
@Entity
@DiscriminatorValue( "MONEY_UNIT" )
public class MoneyUnit extends LgLov {
}
