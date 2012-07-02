for i in `ls | grep -v m.sh | grep -v Id.java` ;
do 
    c=`echo $i | cut -d. -f1`
    j=`echo $c | cut -c 3-100`

    echo "package controllers.crud;

import controllers.CRUD; 
import models.${c};

@CRUD.For( ${c}.class )
public class ${j}s extends CRUD {
}
" > "../controllers/crud/${j}s.java"
done

