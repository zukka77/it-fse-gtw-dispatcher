package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client;

import org.bson.Document;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author vincenzoingenito
 *
 *	DTO used to return document reference creation result.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class TransformResDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2618965716083072681L;
	
	private String errorMessage;
	
	private Document json;
	
}
