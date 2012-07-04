for i in `ls | grep -v m.sh | grep -v Id.java` ;
do 
    c=`echo $i | cut -d. -f1`
    j=`echo $c | cut -c 3-100`

    echo "package controllers.crud;

import controllers.CRUD;
import controllers.Secure;
import models.${c};
import play.mvc.With;

@With(Secure.class)
@CRUD.For( ${c}.class )
public class ${j}s extends CrudBaseController {
}
" > "../controllers/crud/${j}s.java"
done

