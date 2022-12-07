/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.logging;

public interface KeyingStrategy<E> {

    /**
     * creates a byte array key for the given {@link ch.qos.logback.classic.spi.ILoggingEvent}
     * @param e the logging event
     * @return a key
     */
    byte[] createKey(E e);

}