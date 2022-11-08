/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.logging;


 
public class NoKeyKeyingStrategy implements KeyingStrategy<Object> {

    @Override
    public byte[] createKey(Object e) {
        return null;
    }
} 