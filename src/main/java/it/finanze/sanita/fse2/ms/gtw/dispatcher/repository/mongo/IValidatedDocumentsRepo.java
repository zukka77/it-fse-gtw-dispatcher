package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo;
import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;

public interface IValidatedDocumentsRepo extends Serializable {
    ValidatedDocumentsETY create(ValidatedDocumentsETY ety);
    boolean deleteItem(String hash);
    ValidatedDocumentsETY findItemById(String id);
    ValidatedDocumentsETY findItemByHash(String hash);
    boolean isItemInserted(String hash);
}
