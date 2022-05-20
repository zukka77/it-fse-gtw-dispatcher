/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.io.Serializable;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * The Class LivenessCheckController.
 *
 * @author AndreaPerquoti
 */
@Component
@Endpoint(id = "live")
public class LivenessCheckController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5960076491278265396L;

	/**
	 * Return system state.
	 * 
	 * @return	system state
	 */
	 @ReadOperation
	 public Health health() {
		return Health.up().build();
	}
 
}
