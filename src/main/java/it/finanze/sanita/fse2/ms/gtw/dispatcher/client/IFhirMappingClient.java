package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.TransformResDTO;

public interface IFhirMappingClient extends Serializable {

	TransformResDTO callConvertCdaInBundle(final FhirResourceDTO resourceToConvert);
}
